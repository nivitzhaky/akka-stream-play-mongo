package persistence

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import persistence.MongoObject.client
import utils.MongoUtils

object MongoObject {
  lazy val client = MongoClient()

}
object DBName {
  val persons = "persons"
}
object TableName {
  val persons = "persons"
  val adults = "adults"
  val kids = "kids"
  val batches = "batches"
}

case class Person(id: String, age: Int, batchId: String)
case class Adult(id: String, age: Int, hasLicense: Boolean, batchId: String)
case class Kid(id: String, age: Int, school: String, batchId: String)
case class Batch(id: String, timestamp: Long, wanted: Int)
case class BatchStats(persons: Int, adults: Int, kids: Int, kindergarten: Int, elementary: Int, high: Int)

class PersonMongoPersistence(dbName: String) extends MongoUtils {

  import TableName._
  lazy val db = client.getDB(dbName)

  def MDB = MongoDBObject
  def addPerson(person: Person) = {
    db(persons).insert(toDBObj(person))
  }
  def addAdult(adult: Adult) = {
    db(adults).insert(toDBObj(adult))
  }
  def addKid(kid: Kid) = {
    db(kids).insert(toDBObj(kid))
  }
  def addBatch(batch: Batch) = {
    db(batches).insert(toDBObj(batch))
  }
  def getBatchList() = {
    db(batches).find().sort(MDB("timestamp" -> -1)).toList.map(x => dbObjTo[Batch](x))
  }

  def getBatchStats(batchId: String): BatchStats = {
    val personsNo = db(persons).count(MDB("batchId" -> batchId))
    val kidsKinder = db(kids).count(MDB("batchId" -> batchId, "school" -> "kindergarten"))
    val kidsElem = db(kids).count(MDB("batchId" -> batchId, "school" -> "elementary"))
    val kidsHigh = db(kids).count(MDB("batchId" -> batchId, "school" -> "high"))
    val adultsNo = db(adults).count(MDB("batchId" -> batchId))
    BatchStats(personsNo, adultsNo, kidsKinder + kidsElem + kidsHigh, kidsKinder, kidsElem, kidsHigh)
  }

  def createIndexes() = {
    db(persons).createIndex(MDB("batchId" -> 1))
    db(kids).createIndex(MDB("batchId" -> 1))
    db(adults).createIndex(MDB("batchId" -> 1))
  }

}

