package e2e

import java.util.UUID

import akka.actor.ActorSystem
import controllers.BatchController
import play.api.libs.json.Json
import play.api.test.{ FakeRequest, PlaySpecification, WithApplication }
import util.KafkaUtils.KafkaShouldGatherMessages
import util.Utils

class E2ESpec extends PlaySpecification {
  val as = ActorSystem("forTest")
  val controller = new BatchController(as);

  Utils.ignoreCode { // strange specs2 bug
    "Add batch via api " should {
      "run the overall process" in new WithApplication() {
        val batchId: String = UUID.randomUUID().toString
        val request = FakeRequest(POST, "/").withJsonBody(Json.obj("id" -> batchId, "wanted" -> 50))
        status(call(controller.addBatch(), request)) mustEqual OK
        KafkaShouldGatherMessages(List("persons"), batchId, 50)
        Utils.tryForOneMinute {
          val res = contentAsJson(call(controller.getStats(batchId), request))
          val people = (res \ "persons").get.toString().toInt
          val kidsPlusAdults = (res \ "kids").get.toString().toInt + (res \ "adults").get.toString().toInt
          people must_=== (50)
          kidsPlusAdults must_=== (50)
          println("*************** After verify")
        }
      }
    }
  } // strange specs2 bug
}

