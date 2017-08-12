package context

import java.util.UUID

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import org.specs2.specification.Scope
import persistence.PersonMongoPersistence
import util.KafkaUtils.givenKafkaMessages
import util.KafkaUtils
import utils.PersonGenerator

trait PersonTestContext extends Scope {
  implicit val formats = new DefaultFormats {}

  val batchId = UUID.randomUUID().toString
  val srcTopic = "src_" + batchId
  val kidsTopic = "kids_" + batchId
  val adultsTopic = "adults_" + batchId

  //  val srcTopic =  "test_persons"
  //  val kidsTopic = "test_kids"
  //  val adultsTopic = "test_adults"
  val persistence = new PersonMongoPersistence("test_db")
  val mongo = MongoClient().getDB("test_db")
  def MDB = MongoDBObject

  KafkaUtils.createTopic(srcTopic)
  KafkaUtils.createTopic(kidsTopic)
  KafkaUtils.createTopic(adultsTopic)

  def givenPersonsOnTopic(num: Int, topic: String): Unit = {
    givenKafkaMessages((1 to num).map(x => write(PersonGenerator.get(batchId))).toList, topic)
  }
}
