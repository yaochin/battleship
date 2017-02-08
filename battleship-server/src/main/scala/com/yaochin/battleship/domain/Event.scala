package com.yaochin.battleship.domain

/**
  * Created on 2/6/17.
  */
sealed trait Event

case class AttackEvent(loc: Location) extends Event
case class SetupEvent(shipLoc: Battleship) extends Event

object AttackEvent{
  def apply(x: Int, y: Int): AttackEvent = {
    AttackEvent(Location(x, y))
  }
}
