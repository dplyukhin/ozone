package choral.examples.ozone

import akka.actor.ActorRef
import choral.examples.ozone.modelserving.{Image, Predictions, ProcessedImages}

package object modelservingakka {

  case class DFut(imgID: Int, workerID: Int)

  trait Message
  case class ClientReady(ref: ActorRef) extends Message
  case class PreprocessRequest(img: Image, imgID: Int) extends Message
  case class NewImage(dfut: DFut) extends Message
  case class ComputePredictions(batchIDs: BatchIDs) extends Message
  case class GetBatch(batchIDs: BatchIDs, replyTo: ActorRef) extends Message
  case class BatchReply(batchIDs: BatchIDs, images: ProcessedImages) extends Message
  case class NewPredictions(predictions: Predictions) extends Message
}
