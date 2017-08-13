package integration

import akka.actor.ActorSystem
import context.PersonTestContext
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import org.specs2.mutable._
import streams.{ CombinedPersonKidsAdultStream, PersonSorter }
import util.KafkaUtils._
import util.Utils._
import utils.PersonGenerator

class PersonKidsAdultsSpec extends Specification {
  import persistence.TableName._
  implicit val formats = new DefaultFormats {}
  implicit val as = ActorSystem("ForTest")
  "After combined flow we should have total amount of kids and adults as the persons" in new PersonTestContext {
    givenPersonsOnTopic(10, srcTopic)
    new CombinedPersonKidsAdultStream(srcTopic, kidsTopic, adultsTopic, persistence).run
    tryForTwentySeconds(
      (mongo(kids).count(MDB("batchId" -> batchId)) +
        mongo(adults).count(MDB("batchId" -> batchId)))
        must_=== (10))
  }

}

