package choral.examples.ozone.modelservingakka

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, RootActorPath, Timers}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.util.Timeout
import choral.examples.ozone.modelserving.{ClientState, Config, Image, ModelState, Predictions, ProcessedImages}
import choral.examples.ozone.modelservingakka.{BatchIDs, BatcherState, WorkerState}

import scala.collection.mutable.Map
import java.awt.image.BufferedImage
import java.io.{BufferedWriter, ByteArrayOutputStream, File, FileWriter, IOException}
import java.nio.file.{Files, Paths, StandardOpenOption}
import javax.imageio.ImageIO
import scala.concurrent.duration.DurationInt

object Behaviors {
  implicit val timeout: Timeout = Timeout(10.seconds)

  case class DFut(imgID: Int, worerID: Int)

  case class ClientReady(ref: ActorRef)
  case class PreprocessRequest(img: Image, imgID: Int)
  case class NewImage(dfut: DFut)
  case class ComputePredictions(batchIDs: BatchIDs)
  case class GetBatch(batchIDs: BatchIDs, replyTo: ActorRef)
  case class BatchReply(batchIDs: BatchIDs, images: ProcessedImages)
  case class NewPredictions(predictions: Predictions)

  class Timer extends Actor {
     def receive = {
       case ClientReady(ref) =>
         var requestStart = System.currentTimeMillis()
         for (i <- 0 to (Config.NUM_REQUESTS + Config.WARMUP_ITERATIONS)) {
           ref ! "request"
           requestStart += Config.REQUEST_INTERVAL
           val sleepTime = requestStart - System.currentTimeMillis()
           if (sleepTime > 0) try Thread.sleep(sleepTime)
           catch {
             case e: InterruptedException =>
           }
         }
     }
  }

  class Client(img: Image, timer: ActorRef) extends Actor with ActorLogging {

    val cluster = Cluster(context.system)
    var batcher: ActorSelection = _
    var worker1: ActorSelection = _
    var worker2: ActorSelection = _
    var model1: ActorSelection = _
    var model2: ActorSelection = _
    val state: ClientState = new ClientState()
    var requestStart: Long = 0
    var benchmarkStart: Long = 0
    var benchmarkEnd: Long = 0
    var imgID: Int = 0
    var startTimes: Map[Int, Long] = Map[Int, Long]()
    var endTimes: Map[Int, Long] = Map[Int, Long]()

    // subscribe to cluster changes, re-subscribe when restart
    override def preStart(): Unit = {
      log.info("Client started")
      cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[MemberRemoved])
    }
    override def postStop(): Unit = cluster.unsubscribe(self)

    def start(): Unit = {
      println("Starting. Effective reqs per second: " + Config.EFFECTIVE_REQUEST_RATE + ", batch size: " +
        Config.BATCH_SIZE)
      println("Receiving requests every " + Config.REQUEST_INTERVAL + "ms")
      Thread.sleep(5000) // Wait for everyone else to sync up
      timer ! ClientReady(self)
    }

    def receive = {
      case "request" =>
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
          //println(s"Got prediction $imgID: ")
        }
        if (endTimes.size == Config.NUM_REQUESTS) {
          println("Done")
          benchmarkEnd = System.currentTimeMillis()
          // Open
          val suffix: String = "akka-rate" + Config.EFFECTIVE_REQUEST_RATE + "-batch" + Config.BATCH_SIZE + ".csv"
          val latencyPath: String = "data/modelserving/latency-" + suffix
          val throughputPath: String = "data/modelserving/throughput-" + suffix

          // Write throughput data, overwriting existing data if it's there
          val throughputFile = new File(throughputPath)
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
          log.info(s"Client connected to batcher")
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

    val cluster = Cluster(context.system)

    var client: ActorSelection = _
    var model1: ActorSelection = _
    var model2: ActorSelection = _
    val state: BatcherState = new BatcherState()

    // subscribe to cluster changes, re-subscribe when restart
    override def preStart(): Unit = {
      log.info("Batcher started")
      cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[MemberRemoved])
    }
    override def postStop(): Unit = cluster.unsubscribe(self)

    def receive = {
      case NewImage(DFut(imgID, workerID)) =>
        //println(s"Batcher got image ID $imgID")
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
        //println(s"Got predictions $predictions")
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

    val cluster = Cluster(context.system)

    var client: ActorSelection = _
    var model1: ActorSelection = _
    var model2: ActorSelection = _
    val state: WorkerState = new WorkerState(workerID)
    var waiters: Map[BatchIDs, ActorRef] = Map[BatchIDs, ActorRef]()

    // subscribe to cluster changes, re-subscribe when restart
    override def preStart(): Unit = {
      log.info("Worker started")
      cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[MemberRemoved])
    }
    override def postStop(): Unit = cluster.unsubscribe(self)

    def receive = {
      case PreprocessRequest(img, imgID) =>
        //println(s"Worker got image $imgID")
        val processed = state.preprocess(img)
        state.store(imgID, processed)

        for ((batchIDs, replyTo) <- waiters) {
          if (state.canDumpBatch(batchIDs)) {
            val batch = state.dumpBatch(batchIDs)
            replyTo ! BatchReply(batchIDs, batch)
          }
        }

      case GetBatch(batchIDs, replyTo) =>
        //println(s"Got asked to send batch $batchIDs")
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

    val cluster = Cluster(context.system)

    var batcher: ActorSelection = _
    var worker1: ActorSelection = _
    var worker2: ActorSelection = _
    val state: ModelState = new ModelState()
    var batchMap: Map[BatchIDs, ProcessedImages] = Map[BatchIDs, ProcessedImages]()

    // subscribe to cluster changes, re-subscribe when restart
    override def preStart(): Unit = {
      log.info("Model started")
      cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[MemberRemoved])
    }
    override def postStop(): Unit = cluster.unsubscribe(self)

    def receive = {
      case ComputePredictions(batchIDs) =>
        //println(s"Fetching batch $batchIDs")
        worker1 ! GetBatch(batchIDs, self)
        worker2 ! GetBatch(batchIDs, self)

      case BatchReply(batchIDs, images) =>
        if (!batchMap.contains(batchIDs)) {
          //println(s"Got first batch")
          batchMap(batchIDs) = images
        }
        else {
          //println(s"Computing $batchIDs")
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
