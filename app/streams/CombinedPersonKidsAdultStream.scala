package streams

import akka.actor.ActorSystem
import persistence.PersonMongoPersistence

class CombinedPersonKidsAdultStream(sourceTopic: String, kidsTopic: String, adultsTopic: String, mongo: PersonMongoPersistence)(implicit as: ActorSystem) {
  def run() = {
    new PersonSorter(sourceTopic, kidsTopic, adultsTopic, mongo).run
    new AdultsStream(adultsTopic, mongo).run
    new KidsStream(kidsTopic, mongo).run
  }

}
