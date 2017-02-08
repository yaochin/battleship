package com.yaochin.battleship.util

import com.twitter.inject.Logging
import org.joda.time.DateTime

/**
  * Created on 2/5/17.
  */
trait ProcessHelper extends Logging{

  def waitUntil[T](totalInSeconds: Int, sleepInSeconds: Int)(func: => Option[T]): Option[T] = {

    val start = new DateTime()

//    appLogger.info(s"waiting result up to $totalInSeconds seconds")

    while (start.plusSeconds(totalInSeconds).isAfter(new DateTime())){
      func match {
        case Some(r) => return Some(r)
        case None =>
//          appLogger.info(s"waiting $sleepInSeconds")
          Thread.sleep(sleepInSeconds * 1000)
      }
    }

//    appLogger.info("no return after timeout")
    None
  }

}
