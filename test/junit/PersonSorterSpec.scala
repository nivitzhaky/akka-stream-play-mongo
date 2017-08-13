package junit

import akka.actor.ActorSystem
import context.PersonTestContext
import org.specs2.mutable._
import streams.PersonSorter
import util.KafkaUtils._
import util.Utils._

class PersonSorterSpec extends Specification {
  import persistence.TableName._
  implicit val as = ActorSystem("ForTest")

  "PersonSorter should sort people " in new PersonTestContext {
    givenPersonsOnTopic(10, srcTopic)
    new PersonSorter(srcTopic, kidsTopic, adultsTopic, persistence).run
    KafkaShouldGatherMessages(List(kidsTopic, adultsTopic), batchId, 10)
  }

  "PersonSorter should save raw data to mongo " in new PersonTestContext {
    givenPersonsOnTopic(10, srcTopic)
    new PersonSorter(srcTopic, kidsTopic, adultsTopic, persistence).run
    tryForTwentySeconds(mongo(persons).find(MDB("batchId" -> batchId)).count() must_=== (10))
  }

}

