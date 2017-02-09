package com.yaochin.battleship.domain

import org.scalatest.{FreeSpec, Matchers}

/**
  * Created on 2/3/17.
  */
class BattleshipTest extends FreeSpec with Matchers{

  "underAttack" - {
    "hit" in {
      // Given
      val ship = Battleship(Map(
        Location(1,2) -> ShipLocationState.Normal,
        Location(1,3) -> ShipLocationState.Normal
      ))

      // When
      ship.updateShipIfNecessary(Location(1,2)) should be(Battleship(Map(
        Location(1,2) -> ShipLocationState.Hit,
        Location(1,3) -> ShipLocationState.Normal
      )))
    }

    "miss" in {
      // Given
      val ship = Battleship(Map(
        Location(1,2) -> ShipLocationState.Normal,
        Location(1,3) -> ShipLocationState.Normal
      ))

      // When
      ship.updateShipIfNecessary(Location(1,4)) should be(Battleship(Map(
        Location(1,2) -> ShipLocationState.Normal,
        Location(1,3) -> ShipLocationState.Normal
      )))
    }

    "ship is immutable" in {
      // Given
      val ship = Battleship(Map(
        Location(1,2) -> ShipLocationState.Normal,
        Location(1,3) -> ShipLocationState.Normal
      ))

      // When
      val updated = ship.updateShipIfNecessary(Location(1,3))

      // Then
      ship.locationAndStateMap should be(Map(
        Location(1,2) -> ShipLocationState.Normal,
        Location(1,3) -> ShipLocationState.Normal
      ))

      updated.locationAndStateMap should be(Map(
        Location(1,2) -> ShipLocationState.Normal,
        Location(1,3) -> ShipLocationState.Hit
      ))
    }
  }


}
