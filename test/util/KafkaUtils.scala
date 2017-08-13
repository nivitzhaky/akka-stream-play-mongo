package util

import java.util
import java.util.{ Properties, UUID }

import kafka.admin.AdminUtils
import kafka.utils.ZkUtils
import org.apache.kafka.clients.consumer.{ ConsumerConfig, KafkaConsumer }
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import org.scalatest.Matchers

import scala.collection.JavaConverters._
//import org.specs2.matcher.{Matchers, MustMatchers}

import scala.collection.mutable.ArrayBuffer
import scala.util.Try
import Utils._;

object KafkaUtils extends Matchers {

  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("client.id", "test_group")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[String, String](props)

  def givenKafkaMessages(m: List[String], topic: String): Unit = {
    m.foreach(msg => producer.send(new ProducerRecord[String, String](topic, msg)).get())
  }

  val collected = ArrayBuffer.empty[String]
  def KafkaShouldGatherMessages(topics: List[String], uniqueContent: String, number: Int = 1): Unit = {
    val consumerProps = createConsumerConfig(UUID.randomUUID().toString)
    val consumer = new KafkaConsumer[String, String](consumerProps)
    consumer.subscribe(topics.asJavaCollection)

    tryForTwentySeconds {
      val iter = consumer.poll(100).iterator()
      while (iter.hasNext) {
        val x = iter.next()
        collected += x.value()
      }
      collected.count(x => x.contains(uniqueContent)) should ===(number)
    }
  }

  def createTopic(name: String): Unit = {
    Try { AdminUtils.createTopic(ZkUtils("localhost:2181", 2000, 2000, false), name, 1, 1) }
  }

  def createConsumerConfig(groupId: String): Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }

}

object app extends App {
  KafkaUtils.createTopic("niv")

  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("client.id", "ScalaProducerExample")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[String, String](props)
  producer.send(new ProducerRecord[String, String]("niv", "test")).get()

  //KafkaUtils.givenKafkaMessages(List("x","y"),"niv")
  println("success")
}