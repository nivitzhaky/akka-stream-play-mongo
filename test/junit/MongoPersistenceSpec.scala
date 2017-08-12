package junit

import java.util.UUID

import akka.actor.ActorSystem
import com.mongodb.DBObject
import context.PersonTestContext
import org.json4s.{DefaultFormats, Extraction}
import org.json4s.mongo.JObjectParser
import org.specs2.mutable.Specification
import persistence._

class MongoPersistenceSpec extends Specification {
  implicit val formats = new DefaultFormats {}
  implicit val as = ActorSystem("ForTest")

  "Should persist person " in new PersonTestContext {
    val id = UUID.randomUUID().toString
    persistence.addPerson(Person(id, 20, id))
    mongo("persons").count(MDB("id" -> id)) must_=== (1)
  }

  "Should persist adult " in new PersonTestContext {
    val id = UUID.randomUUID().toString
    persistence.addAdult(Adult(id, 20, true, id))
    mongo("adults").count(MDB("id" -> id)) must_=== (1)
  }

  "Should persist kid " in new PersonTestContext {
    val id = UUID.randomUUID().toString
    persistence.addKid(Kid(id, 20, "high", id))
    mongo("kids").count(MDB("id" -> id)) must_=== (1)
  }

  "Should persist batch " in new PersonTestContext {
    persistence.addBatch(Batch(batchId, System.currentTimeMillis(), 100))
    mongo("batches").count(MDB("id" -> batchId)) must_=== (1)
  }

  "Should return batch stats " in new PersonTestContext {
    persistence.addPerson(Person(UUID.randomUUID().toString, 10, batchId))
    persistence.addPerson(Person(UUID.randomUUID().toString, 50, batchId))
    persistence.addPerson(Person(UUID.randomUUID().toString, 60, batchId))
    persistence.addAdult(Adult(UUID.randomUUID().toString, 50, true, batchId))
    persistence.addAdult(Adult(UUID.randomUUID().toString, 60, true, batchId))
    persistence.addAdult(Adult(UUID.randomUUID().toString, 70, false, batchId))
    persistence.addAdult(Adult(UUID.randomUUID().toString, 80, false, batchId))
    persistence.addKid(Kid(UUID.randomUUID().toString, 1, "kindergarten", batchId))
    persistence.addKid(Kid(UUID.randomUUID().toString, 10, "elementary", batchId))
    persistence.addKid(Kid(UUID.randomUUID().toString, 15, "elementary", batchId))
    persistence.addKid(Kid(UUID.randomUUID().toString, 16, "high", batchId))
    persistence.getBatchStats(batchId) should beEqualTo(BatchStats(3, 4, 4, 1, 2, 1))
  }

  "Should return batches in right order " in new PersonTestContext {
    mongo("batches").remove(MDB())
    persistence.addBatch(Batch(UUID.randomUUID().toString, 10, 50))
    persistence.addBatch(Batch(UUID.randomUUID().toString, 11, 75))
    persistence.addBatch(Batch(UUID.randomUUID().toString, 12, 100))
    persistence.getBatchList().head.wanted shouldEqual(100)
  }


}
