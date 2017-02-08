package com.yaochin.battleship.domain

import org.scalatest.{FreeSpec, Matchers}

/**
  * Created on 2/6/17.
  */
class UserTest extends FreeSpec with Matchers {

  "UserSession" - {
    "addEvent" in {
      // Given
      val battleship = BattleshipBuilder()
        .withLocation(1, 2)
        .build
      val userSession = UserSessionBuilder()
        .withEvents(List(SetupEvent(battleship)))
        .build

      // When
      val updated = userSession.addEvent(AttackEvent(1, 2))

      // Then
      userSession.events should be(List(
        SetupEvent(battleship)
      ))
      updated.events should be(List(
        SetupEvent(battleship),
        AttackEvent(1, 2)
      ))
    }

    "isAlreadyTaken" in {
      // Given

      // When

      // Then
    }
  }

}
