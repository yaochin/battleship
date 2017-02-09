package com.yaochin.battleship.domain

/**
  * Created on 2/6/17.
  */
sealed trait Event {
  val details: Any
}

case class AttackEvent(loc: Location) extends Event {
  override val details = loc
}
case class SetupEvent(ship: Battleship) extends Event {
  override val details = ship.locationAndStateMap.keys
}

object AttackEvent{
  def apply(x: Int, y: Int): AttackEvent = {
    AttackEvent(Location(x, y))
  }
}
