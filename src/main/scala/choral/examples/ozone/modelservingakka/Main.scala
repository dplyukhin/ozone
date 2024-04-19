package choral.examples.ozone.modelservingakka

import akka.actor.{ActorSystem, Props}
import choral.examples.ozone.modelserving.Image
import com.typesafe.config.ConfigFactory
import choral.examples.ozone.modelserving.Config

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File, IOException}
import javax.imageio.ImageIO
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {

  def readImage(filePath: String): Image = try {
    val original = ImageIO.read(new File(filePath))
    val scaled = original.getScaledInstance(224, 224, java.awt.Image.SCALE_DEFAULT)
    val bufferedScaled = new BufferedImage(244, 244, BufferedImage.TYPE_INT_ARGB)
    bufferedScaled.getGraphics.drawImage(scaled, 0, 0, null)
    val outputStream = new ByteArrayOutputStream()
    ImageIO.write(bufferedScaled, "PNG", outputStream)
    new Image(outputStream.toByteArray)
  } catch {
    case e: IOException =>
      println("Couldn't find image " + filePath + ". Exiting.")
      null
  }

  // Parse command line argument to determine which role to start
  // and use the corresponding configuration
  val roles = Set("client", "batcher", "worker1", "worker2", "model1", "model2")
  if (args.length != 3) {
    println("Expected three arguments (role) (batchSize) (requestsPerSecond); got: " + args)
    System.exit(1)
  }
  val role = args(0)
  Config.BATCH_SIZE = args(1).toInt
  Config.REQUESTS_PER_SECOND = args(2).toInt
  System.setProperty("java.awt.headless", "true")
  val port = if (role == "client") 2551 else 0
  val img = readImage("img.jpg")

  val config = ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port=$port
      akka.cluster.roles = [$role]
      """).withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)
  role match {
    case "client" =>
      val timer = system.actorOf(Props[Behaviors.Timer]().withDispatcher("my-pinned-dispatcher"), "timer")
      system.actorOf(Props(new Behaviors.Client(img, timer)), role)
    case "batcher" => system.actorOf(Props[Behaviors.Batcher](), role)
    case "worker1" => system.actorOf(Props(new Behaviors.Worker(0)), role)
    case "worker2" => system.actorOf(Props(new Behaviors.Worker(1)), role)
    case "model1" => system.actorOf(Props[Behaviors.Model](), role)
    case "model2" => system.actorOf(Props[Behaviors.Model](), role)
  }

  Await.result(system.whenTerminated, Duration.Inf)
}