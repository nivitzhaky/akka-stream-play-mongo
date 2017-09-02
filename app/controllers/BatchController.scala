package controllers

import javax.inject._

import akka.actor.ActorSystem
import com.github.tototoshi.play2.json4s.native.Json4s
import org.json4s.Extraction
import persistence.{ Batch, DBName, PersonMongoPersistence }
import streams.{ BatchCreationStream, CombinedPersonKidsAdultStream }
import utils.Kafka

import scala.concurrent.ExecutionContext.Implicits.global

//import play.api.libs.json.Json
import org.json4s._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

//case class MessageAndId(id: String, message: CommentMessage)
case class BatchNoTimestamp(id: String, wanted: Int)

@Singleton
class BatchController @Inject() (system: ActorSystem) extends Controller with Json4s {
  import persistence.TableName._
  implicit val formats = new DefaultFormats {}
  implicit val as = system
  val mongoPersistence = new PersonMongoPersistence(DBName.persons)

  init()

  def addBatch = Action.async(json) { implicit request =>
    Future {
      println("************************\n" +
        "******************************\n" +
        "add bach wass called")
      val wanted_batch = request.body.extract[BatchNoTimestamp]
      val batch = Batch(wanted_batch.id, System.currentTimeMillis(), wanted_batch.wanted)
      mongoPersistence.addBatch(batch)
      new BatchCreationStream(batch, persons).run
      Ok(Json.obj()).withHeaders(headers: _*)
    }
  }

  def getStats(batchId: String) = Action { implicit request =>
    Ok(Extraction.decompose(mongoPersistence.getBatchStats(batchId))).withHeaders(headers: _*).withHeaders(("Content-Type", "application/json;"))
  }

  def getBatches() = Action { implicit request =>
    Ok(Extraction.decompose(mongoPersistence.getBatchList())).withHeaders(headers: _*)
  }

  def index = Action {
    Ok(views.html.index(mongoPersistence.getBatchList()))
  }

  def stats(batchId: String) = Action {
    Ok(views.html.batch_details(mongoPersistence.getBatchStats(batchId)))
  }

  def headers = List(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS, DELETE, PUT",
    "Access-Control-Max-Age" -> "3600",
    "Access-Control-Allow-Headers" -> "Origin, Content-Type, Accept, Authorization",
    "Access-Control-Allow-Credentials" -> "true"
  )
  def options(p: String) = Action { request =>
    NoContent.withHeaders(headers: _*)
  }

  def init() = {
    mongoPersistence.createIndexes()
    Kafka.createTopic(persons)
    Kafka.createTopic(adults)
    Kafka.createTopic(kids)
    new CombinedPersonKidsAdultStream(persons, kids, adults, mongoPersistence).run()
  }

}
