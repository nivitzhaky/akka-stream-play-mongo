package junit

import akka.actor.ActorSystem
import context.PersonTestContext
import org.specs2.mutable._
import persistence.Batch
import streams.BatchCreationStream
import util.KafkaUtils._

class BatchCreationSpec extends Specification {
  implicit val as = ActorSystem("ForTest")

  "BatchCreator should generate batch " in new PersonTestContext {
    new BatchCreationStream(Batch(batchId, System.currentTimeMillis(), 10), srcTopic).run
    KafkaShouldGatherMessages(List(srcTopic), batchId, 10)
  }

}

