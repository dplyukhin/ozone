package choral.examples.ozone.modelservingakka

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, RootActorPath}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import choral.examples.ozone.modelserving.{ClientState, Config, Image, ModelState, ProcessedImages}

import java.nio.file.{Files, Paths, StandardOpenOption}
import scala.collection.mutable

object Behaviors {

  class Timer extends Actor {
     def receive: Receive = {
       case ClientReady(ref) =>
         var requestStart = System.currentTimeMillis()
         for (_ <- 0 to (Config.NUM_REQUESTS + Config.WARMUP_ITERATIONS)) {
           ref ! NewRequest()
           requestStart += Config.REQUEST_INTERVAL
           val sleepTime = requestStart - System.currentTimeMillis()
           if (sleepTime > 0) try Thread.sleep(sleepTime)
           catch {
             case _: InterruptedException =>
           }
         }
     }
  }

  class Client(img: Image, timer: ActorRef) extends Actor with ActorLogging {

    private val cluster = Cluster(context.system)
    private var batcher: ActorSelection = _
    private var worker1: ActorSelection = _
    private var worker2: ActorSelection = _
    private var model1: ActorSelection = _
    private var model2: ActorSelection = _
    private val state: ClientState = new ClientState()
    private var benchmarkStart: Long = 0
    private var benchmarkEnd: Long = 0
    private var imgID: Int = 0
    private val startTimes: mutable.Map[Int, Long] = mutable.Map[Int, Long]()
    private val endTimes: mutable.Map[Int, Long] = mutable.Map[Int, Long]()

    override def preStart(): Unit = {
      cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[MemberRemoved])
    }
    override def postStop(): Unit = cluster.unsubscribe(self)

    private def start(): Unit = {
      println("Starting. Effective reqs per second: " + Config.EFFECTIVE_REQUEST_RATE + ", batch size: " +
        Config.BATCH_SIZE)
      println("Receiving requests every " + Config.REQUEST_INTERVAL + "ms")
      Thread.sleep(5000) // Wait for the other services to connect to each other
      timer ! ClientReady(self)
    }

    def receive: Receive = {
      case NewRequest() =>
        val workerID = state.chooseWorker(2)
        workerID match {
          case 0 =>
            worker1 ! PreprocessRequest(img, imgID)
          case 1 =>
            worker2 ! PreprocessRequest(img, imgID)
        }
        if (imgID == Config.WARMUP_ITERATIONS) {
          benchmarkStart = System.currentTimeMillis()
        }
        if (imgID >= Config.WARMUP_ITERATIONS) {
          startTimes(imgID - Config.WARMUP_ITERATIONS) = System.currentTimeMillis()
        }
        if (imgID == Config.WARMUP_ITERATIONS + Config.NUM_REQUESTS) {
          val end = System.currentTimeMillis()
          println("Received all requests in " + (end - benchmarkStart) + "ms")
          println("Average request rate: " + Config.NUM_REQUESTS * 1000 / (end - benchmarkStart) + "req/s")
        }
        val dfut = DFut(imgID, workerID)
        batcher ! NewImage(dfut)
        imgID += 1
      case NewPredictions(predictions) =>
        for (imgID <- predictions.getImgIDs) {
          if (imgID >= Config.WARMUP_ITERATIONS) {
            endTimes(imgID - Config.WARMUP_ITERATIONS) = System.currentTimeMillis()
          }
        }
        if (endTimes.size == Config.NUM_REQUESTS) {
          benchmarkEnd = System.currentTimeMillis()

          val suffix: String = "akka-rate" + Config.EFFECTIVE_REQUEST_RATE + "-batch" + Config.BATCH_SIZE + ".csv"
          val latencyPath: String = "data/modelserving/latency-" + suffix
          val throughputPath: String = "data/modelserving/throughput-" + suffix

          // Write throughput data, overwriting existing data if it's there
          val throughputWriter = Files.newBufferedWriter(Paths.get(throughputPath),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
          val throughput = Config.NUM_REQUESTS * 1000 / (benchmarkEnd - benchmarkStart)
          throughputWriter.write(throughput.toString)
          throughputWriter.newLine()
          throughputWriter.close()

          // Write latency data, overwriting existing data if it's there
          val latencyWriter = Files.newBufferedWriter(Paths.get(latencyPath),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
          var i = 0
          while (i < Config.NUM_REQUESTS) {
            val latency = endTimes(i) - startTimes(i)
            latencyWriter.write(latency.toString)
            latencyWriter.newLine()

            i += 1
          }
          latencyWriter.close()

          context.system.terminate()
        }

      case MemberUp(member) =>
        if (member.hasRole("batcher")) {
          batcher = context.actorSelection(RootActorPath(member.address) / "user" / "batcher")
        }
        else if (member.hasRole("worker1")) {
          worker1 = context.actorSelection(RootActorPath(member.address) / "user" / "worker1")
        }
        else if (member.hasRole("worker2")) {
          worker2 = context.actorSelection(RootActorPath(member.address) / "user" / "worker2")
        }
        else if (member.hasRole("model1")) {
          model1 = context.actorSelection(RootActorPath(member.address) / "user" / "model1")
        }
        else if (member.hasRole("model2")) {
          model2 = context.actorSelection(RootActorPath(member.address) / "user" / "model2")
        }
        if (batcher != null && worker1 != null && worker2 != null && model1 != null && model2 != null) {
          start()
        }
      case _: MemberRemoved =>
        context.system.terminate()
      case _: MemberEvent =>
    }
  }

  class Batcher extends Actor with ActorLogging {

    private val cluster = Cluster(context.system)
    private var client: ActorSelection = _
    private var model1: ActorSelection = _
    private var model2: ActorSelection = _
    private val state: BatcherState = new BatcherState()

    // subscribe to cluster changes, re-subscribe when restart
    override def preStart(): Unit = {
      log.info("Batcher started")
      cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[MemberRemoved])
    }
    override def postStop(): Unit = cluster.unsubscribe(self)

    def receive: Receive = {
      case NewImage(DFut(imgID, workerID)) =>
        state.newImage(imgID, workerID)
        val batchIDs = state.getBatchIfFull
        if (batchIDs != null) {
          val modelID = state.chooseModel(2)
          modelID match {
            case 0 => model1 ! ComputePredictions(batchIDs)
            case 1 => model2 ! ComputePredictions(batchIDs)
          }
        }
      case NewPredictions(predictions) =>
        client ! NewPredictions(predictions)

      case MemberUp(member) =>
        if (member.hasRole("client")) {
          client = context.actorSelection(RootActorPath(member.address) / "user" / "client")
        }
        else if (member.hasRole("model1")) {
          model1 = context.actorSelection(RootActorPath(member.address) / "user" / "model1")
        }
        else if (member.hasRole("model2")) {
          model2 = context.actorSelection(RootActorPath(member.address) / "user" / "model2")
        }
      case _: MemberRemoved =>
        context.system.terminate()
      case _: MemberEvent =>
    }
  }

  class Worker(workerID: Int) extends Actor with ActorLogging {

    private val cluster = Cluster(context.system)
    private var client: ActorSelection = _
    private var model1: ActorSelection = _
    private var model2: ActorSelection = _
    private val state: WorkerState = new WorkerState(workerID)
    private val waiters: mutable.Map[BatchIDs, ActorRef] = mutable.Map[BatchIDs, ActorRef]()

    // subscribe to cluster changes, re-subscribe when restart
    override def preStart(): Unit = {
      log.info("Worker started")
      cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[MemberRemoved])
    }
    override def postStop(): Unit = cluster.unsubscribe(self)

    def receive: Receive = {
      case PreprocessRequest(img, imgID) =>
        val processed = state.preprocess(img)
        state.store(imgID, processed)

        for ((batchIDs, replyTo) <- waiters) {
          if (state.canDumpBatch(batchIDs)) {
            val batch = state.dumpBatch(batchIDs)
            replyTo ! BatchReply(batchIDs, batch)
          }
        }

      case GetBatch(batchIDs, replyTo) =>
        if (state.canDumpBatch(batchIDs)) {
          val batch = state.dumpBatch(batchIDs)
          replyTo ! BatchReply(batchIDs, batch)
        }
        else {
          waiters(batchIDs) = replyTo
        }

      case MemberUp(member) =>
        if (member.hasRole("client")) {
          client = context.actorSelection(RootActorPath(member.address) / "user" / "client")
        }
        else if (member.hasRole("model1")) {
          model1 = context.actorSelection(RootActorPath(member.address) / "user" / "model1")
        }
        else if (member.hasRole("model2")) {
          model2 = context.actorSelection(RootActorPath(member.address) / "user" / "model2")
        }
      case _: MemberRemoved =>
        context.system.terminate()
      case _: MemberEvent =>
    }
  }

  class Model extends Actor with ActorLogging {

    private val cluster = Cluster(context.system)
    private var batcher: ActorSelection = _
    private var worker1: ActorSelection = _
    private var worker2: ActorSelection = _
    private val state: ModelState = new ModelState()
    private val batchMap: mutable.Map[BatchIDs, ProcessedImages] = mutable.Map[BatchIDs, ProcessedImages]()

    // subscribe to cluster changes, re-subscribe when restart
    override def preStart(): Unit = {
      log.info("Model started")
      cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[MemberRemoved])
    }
    override def postStop(): Unit = cluster.unsubscribe(self)

    def receive: Receive = {
      case ComputePredictions(batchIDs) =>
        worker1 ! GetBatch(batchIDs, self)
        worker2 ! GetBatch(batchIDs, self)

      case BatchReply(batchIDs, images) =>
        if (!batchMap.contains(batchIDs)) {
          batchMap(batchIDs) = images
        }
        else {
          val batch = batchMap(batchIDs)
          batchMap -= batchIDs
          batch.addAll(images)
          val predictions = state.classify(batch)
          batcher ! NewPredictions(predictions)
        }

      case MemberUp(member) =>
        if (member.hasRole("batcher")) {
          batcher = context.actorSelection(RootActorPath(member.address) / "user" / "batcher")
        }
        else if (member.hasRole("worker1")) {
          worker1 = context.actorSelection(RootActorPath(member.address) / "user" / "worker1")
        }
        else if (member.hasRole("worker2")) {
          worker2 = context.actorSelection(RootActorPath(member.address) / "user" / "worker2")
        }
      case _: MemberRemoved =>
        context.system.terminate()
      case _: MemberEvent =>
    }
  }

}
