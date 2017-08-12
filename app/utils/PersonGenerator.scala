package utils

import java.util.UUID

import persistence.{ Adult, Person }

import scala.util.Random

object PersonGenerator {
  def get(batchId: String) = {
    Person(UUID.randomUUID().toString, Random.nextInt(100), batchId)
  }
}

