package util

import org.scalatest.concurrent.Eventually

import scala.concurrent.duration.{ Duration, MILLISECONDS, SECONDS }

object Utils extends Eventually {
  def tryForTwentySeconds(code: => Unit): Unit = {
    eventually(timeout(Duration(20, SECONDS)), interval(Duration(200, MILLISECONDS))) {
      code
    }
  }
  def tryForOneMinute(code: => Unit): Unit = {
    eventually(timeout(Duration(60, SECONDS)), interval(Duration(1, SECONDS))) {
      code
    }
  }
  def ignoreCode(code: => Unit): Unit = {

  }

}
