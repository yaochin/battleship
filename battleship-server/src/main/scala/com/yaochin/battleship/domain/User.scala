package com.yaochin.battleship.domain

import com.fasterxml.jackson.core.`type`.TypeReference
import com.yaochin.battleship.domain.UserState.UserState

/**
  * Created on 1/31/17.
  */
case class User(id: String,
                opponentId: Option[String],
                state: UserState,
                fleet: Seq[Battleship],
                events: List[Event] = List.empty) {

  def addEvent(event: Event): User = {
    copy(events = events :+ event)
  }

  def hasTaken(loc: Location): Boolean = {
    events.exists { e =>
        e.isInstanceOf[AttackEvent] && e.asInstanceOf[AttackEvent].loc == loc
    }
  }

  def contains(loc: Location): Boolean = {
    fleet.exists(_.contains(loc))
  }

  def hasSunk(loc: Location): Boolean = {
    fleet.exists(s => s.contains(loc) && !s.isAlive )
  }

  def hasLost: Boolean = {
    !fleet.exists(ship => ship.isAlive)
  }

  def handleAttack(loc: Location): User = {
    val updated = fleet.map{ ship =>
      ship.updateIfNecessary(loc)
    }
    copy(fleet = updated)
  }

}

object UserState extends Enumeration {
  type UserState = Value
  val Won, Lost, Active, Passive, Initial = Value
}

class UserStateType extends TypeReference[UserState.type]