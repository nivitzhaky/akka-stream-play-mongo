package streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.stream.scaladsl.Flow
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s.Extraction
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.write
import persistence.{ Person, PersonMongoPersistence }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PersonSorter(sourceTopic: String, kidsTopic: String, adultTopic: String, mongo: PersonMongoPersistence)(implicit as: ActorSystem) extends BaseStream {

  val source = Consumer.plainSource(consumerSettings.withGroupId("persons"), Subscriptions.topics(sourceTopic))
    .map(consumerRecord => consumerRecord.value())

  val toPerson: Flow[String, Person, NotUsed] =
    Flow[String]
      .mapAsyncUnordered(30) {
        str =>
          Future {
            Extraction.extract[Person](parse(str))
          }
      }
  val persist: Flow[Person, Person, NotUsed] =
    Flow[Person]
      .mapAsyncUnordered(30) {
        p =>
          Future {
            mongo.addPerson(p)
            p
          }
      }

  val sortTopicOut: Flow[Person, ProducerRecord[String, String], NotUsed] =
    Flow[Person]
      .mapAsyncUnordered(30) {
        person =>
          Future {
            val jsonStr = write(person)
            if (person.age < 16)
              new ProducerRecord[String, String](kidsTopic, jsonStr)
            else
              new ProducerRecord[String, String](adultTopic, jsonStr)
          }
      }

  def run = {
    source
      .via(toPerson)
      .via(persist)
      .via(sortTopicOut)
      .runWith(Producer.plainSink(producerSettings))
  }

}

