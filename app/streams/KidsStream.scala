package streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{ Flow, Sink }
import org.json4s.Extraction
import org.json4s.native.JsonMethods._
import persistence.{ Adult, Kid, Person, PersonMongoPersistence }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

class KidsStream(kidsTopic: String, mongo: PersonMongoPersistence)(implicit as: ActorSystem) extends BaseStream {

  val source = Consumer.plainSource(consumerSettings.withGroupId("kids"), Subscriptions.topics(kidsTopic))
    .map(consumerRecord => consumerRecord.value())

  val toPerson: Flow[String, Person, NotUsed] =
    Flow[String]
      .mapAsyncUnordered(30) {
        str =>
          Future {
            Extraction.extract[Person](parse(str))
          }
      }
  val toKid: Flow[Person, Kid, NotUsed] =
    Flow[Person]
      .mapAsyncUnordered(30) {
        p =>
          Future {
            Kid(p.id, p.age,
              if (p.age < 6) "kindergarten"
              else if (p.age > 14) "high"
              else "elementary", p.batchId)
          }
      }

  val persist: Flow[Kid, Unit, NotUsed] =
    Flow[Kid]
      .mapAsyncUnordered(30) {
        k =>
          Future {
            mongo.addKid(k)
          }
      }

  def run = {
    source
      .via(toPerson)
      .via(toKid)
      .via(persist)
      .runWith(Sink.ignore)
  }

}
