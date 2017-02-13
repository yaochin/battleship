package com.yaochin.battleship.domain

import org.scalatest.{FreeSpec, Matchers}

/**
  * Created on 2/6/17.
  */
class UserTest extends FreeSpec with Matchers {

  "User" - {
    "addEvent" in {
      // Given
      val battleship = BattleshipBuilder()
        .withLocation(1, 2)
        .build
      val user = UserBuilder()
        .withEvents(List(SetupEvent(battleship)))
        .build

      // When
      val updated = user.addEvent(AttackEvent(1, 2))

      // Then
      user.events should be(List(
        SetupEvent(battleship)
      ))
      updated.events should be(List(
        SetupEvent(battleship),
        AttackEvent(1, 2)
      ))
    }

    "isAlreadyTaken" in {
      // Given
      val user = UserBuilder()
        .addEvent(AttackEvent(1, 2))
        .build

      // When / Then
      user.hasTaken(Location(1, 2)) should be(true)
    }

    "contain" - {
      "return true" in {
        // Given
        val ship = BattleshipBuilder()
          .withLocation(1,2)
          .withLocation(1,3)
          .build

        val user = UserBuilder()
          .addShip(ship)
          .build

        // When / Then
        user.contains(Location(1,2)) should be(true)
      }

      "return false" in {
        val ship = BattleshipBuilder()
          .withLocation(1,2)
          .withLocation(1,3)
          .build

        val user = UserBuilder()
          .addShip(ship)
          .build

        // When / Then
        user.contains(Location(1,4)) should be(false)
      }
    }
  }

}
