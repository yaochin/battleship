package com.yaochin.battleship.util

import org.joda.time.DateTime
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, FreeSpec}
import org.mockito.Mockito._

import scala.collection.mutable

/**
  * Created on 2/5/17.
  */
class ProcessHelperTest extends FreeSpec with Matchers with MockitoSugar{

  "waitUntil" - {
    "should wait until pass totalInSeconds" in {
      // Given
      val helper = new ProcessHelper {}
      val start = new DateTime()

      // When
      helper.waitUntil(5, 1){None}

      // Then
      start.plusSeconds(5).isBefore(new DateTime()) should be(true)
    }

    "should return" in {
      val helper = new ProcessHelper {}
      val mockQueue = mock[mutable.Queue[Int]]
      when(mockQueue.headOption)
        .thenReturn(None)
        .thenReturn(None)
        .thenReturn(Some(1))

      // When
      helper.waitUntil(5, 1){mockQueue.headOption}

      // Then
      verify(mockQueue, times(3)).headOption
    }
  }

}
