package com.yaochin.battleship.domain

/**
  * Created on 1/31/17.
  */
case class Battleship(locations: Map[Location, ShipLocationState.Value]) {

  def isAlive: Boolean = {
    locations.values.exists(_ == ShipLocationState.Normal)
  }

  def contains(target: Location): Boolean = {
    locations.contains(target)
  }

  def updateShipIfNecessary(target: Location): Battleship = {
    if ( locations.contains(target) ) {
      copy(locations = locations + (target -> ShipLocationState.Hit))
    } else {
      this
    }
  }

}

case class Location(x: Int, y: Int)

object ShipLocationState extends Enumeration {
  val Normal, Hit = Value
}

object AttackReturn extends Enumeration {
  type AttackReturn = AttackReturn.Value
  val Hit, Miss, Sunk, AlreadyTaken, Win = Value
}
