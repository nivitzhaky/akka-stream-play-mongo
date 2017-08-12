package utils

import java.util.UUID

import com.mongodb.DBObject
import kafka.admin.AdminUtils
import kafka.utils.ZkUtils
import org.bson.types.ObjectId
import org.json4s.mongo.JObjectParser
import org.json4s.{ DefaultFormats, Extraction }

import scala.util.Try

trait MongoUtils {

  implicit val formats = new DefaultFormats {}

  protected def dbObjTo[A](from: DBObject)(implicit manifest: Manifest[A]): A = {
    val jValue = JObjectParser.serialize(from)
    val entity: A = Extraction.extract[A](jValue)
    entity
  }

  case class ID(_id: String)
  protected def toDBObj(any: Any): DBObject = {
    val json = Extraction.decompose(any)
    val id = Extraction.extract[String](json \ "id")
    val newJson = json merge (Extraction.decompose(ID(id)))
    val parsed = JObjectParser.parse(newJson)
    parsed
  }
}
object Kafka {
  def createTopic(name: String): Unit = {
    Try { AdminUtils.createTopic(ZkUtils("localhost:2181", 2000, 2000, false), name, 1, 1) }
  }

}
