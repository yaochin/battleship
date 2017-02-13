package com.yaochin.battleship.domain

import com.fasterxml.jackson.core.`type`.TypeReference
import com.yaochin.battleship.domain.ShipLocationState.ShipLocationState

/**
  * Created on 1/31/17.
  */
case class Battleship(locationAndStateMap: Map[Location, ShipLocationState]) {

  def isAlive: Boolean = {
    locationAndStateMap.values.exists(_ == ShipLocationState.Normal)
  }

  def contains(target: Location): Boolean = {
    locationAndStateMap.contains(target)
  }

  def updateIfNecessary(target: Location): Battleship = {
    if ( locationAndStateMap.contains(target) ) {
      copy(locationAndStateMap = locationAndStateMap + (target -> ShipLocationState.Hit))
    } else {
      this
    }
  }

}

case class Location(x: Int, y: Int)

object ShipLocationState extends Enumeration {
  type ShipLocationState = Value
  val Normal, Hit = Value
}
class ShipLocationStateType extends TypeReference[ShipLocationState.type]

object AttackResult extends Enumeration {
  type AttackResult = Value
  val Hit, Miss, Sunk, AlreadyTaken, Win = Value
}
class AttackResultType extends TypeReference[AttackResult.type]
