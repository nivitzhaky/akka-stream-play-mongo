package streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{ Flow, Sink }
import org.json4s.Extraction
import org.json4s.native.JsonMethods._
import persistence.{ Adult, Person, PersonMongoPersistence }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

class AdultsStream(adultTopic: String, mongo: PersonMongoPersistence)(implicit as: ActorSystem) extends BaseStream {

  val sourceOrig = Consumer.plainSource(consumerSettings.withGroupId("adults"), Subscriptions.topics(adultTopic))
  val source = sourceOrig
    .map(consumerRecord => consumerRecord.value())

  val toPerson: Flow[String, Person, NotUsed] =
    Flow[String]
      .mapAsyncUnordered(30) {
        str =>
          Future {
            Extraction.extract[Person](parse(str))
          }
      }
  val toAdult: Flow[Person, Adult, NotUsed] =
    Flow[Person]
      .mapAsyncUnordered(30) {
        p =>
          Future {
            Adult(p.id, p.age, Random.nextBoolean(), p.batchId)
          }
      }

  val persist: Flow[Adult, Unit, NotUsed] =
    Flow[Adult]
      .mapAsyncUnordered(30) {
        a =>
          Future {
            mongo.addAdult(a)
          }
      }

  def run = {
    source
      .via(toPerson)
      .via(toAdult)
      .via(persist)
      .runWith(Sink.ignore)
  }

}
