package streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{ Flow, Source }
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s.native.Serialization.write
import persistence.{ Batch, Person }
import utils.PersonGenerator

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BatchCreationStream(batch: Batch, topic: String)(implicit as: ActorSystem) extends BaseStream {

  val source = Source.fromIterator(() => List(batch).iterator)

  val toPersons: Flow[Batch, Person, NotUsed] =
    Flow[Batch]
      .mapAsyncUnordered(30) {
        batch =>
          Future {
            (1 to batch.wanted).map(i => PersonGenerator.get(batch.id)).toList
          }
      }.mapConcat(x => x)

  val toKafka: Flow[Person, ProducerRecord[String, String], NotUsed] =
    Flow[Person]
      .mapAsyncUnordered(30) {
        person =>
          Future {
            val jsonStr = write(person)
            new ProducerRecord[String, String](topic, jsonStr)
          }
      }

  def run = {
    source
      .via(toPersons)
      .via(toKafka)
      .runWith(Producer.plainSink(producerSettings))
  }

}
