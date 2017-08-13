package junit

import akka.actor.ActorSystem
import context.PersonTestContext
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import org.specs2.mutable._
import streams.{ AdultsStream, KidsStream }
import util.KafkaUtils._
import util.Utils._
import utils.PersonGenerator

class AdultKidsStreamSpec extends Specification {
  implicit val formats = new DefaultFormats {}
  implicit val as = ActorSystem("ForTest")

  "AdultsStream should persist adults " in new PersonTestContext {
    givenPersonsOnTopic(10, adultsTopic)
    new AdultsStream(adultsTopic, persistence).run
    tryForTwentySeconds(mongo("adults").find(MDB("batchId" -> batchId)).count() must_=== (10))
  }

  "KidsStream should persist kids" in new PersonTestContext {
    givenPersonsOnTopic(10, kidsTopic)
    new KidsStream(kidsTopic, persistence).run
    tryForTwentySeconds(mongo("kids").find(MDB("batchId" -> batchId)).count() must_=== (10))
  }

  "KidsStream should persist kids with school: kindergarten, elementary or high" in new PersonTestContext {
    givenPersonsOnTopic(10, kidsTopic)
    new KidsStream(kidsTopic, persistence).run
    tryForTwentySeconds(mongo("kids").find(MDB("batchId" -> batchId,
      "school" -> MDB("$in" -> Array("kindergarten", "elementary", "high")))).count() must_=== (10))
  }

}

