package streams

import java.util.UUID

import akka.actor.ActorSystem
import akka.kafka.{ ConsumerSettings, ProducerSettings }
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }
import org.json4s.DefaultFormats

class BaseStream(implicit as: ActorSystem) extends LazyLogging {
  implicit val formats = new DefaultFormats {}
  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(as).withSupervisionStrategy(commonSteps.alwaysResume))

  val consumerSettings = ConsumerSettings(as, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

  val producerSettings = ProducerSettings(as, new StringSerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

}

object commonSteps extends LazyLogging {

  val alwaysResume: Supervision.Decider = {

    case e1: Throwable =>
      e1.printStackTrace()
      //logger.error(e1.getMessage)
      Supervision.stop
  }

}