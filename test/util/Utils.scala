package util

import org.scalatest.concurrent.Eventually

import scala.concurrent.duration.{ Duration, MILLISECONDS, SECONDS }

object Utils extends Eventually {
  def tryForTenSeconds(code: => Unit): Unit = {
    eventually(timeout(Duration(10, SECONDS)), interval(Duration(100, MILLISECONDS))) {
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
