package com.yaochin.battleship.domain

import com.yaochin.battleship.domain.UserState.UserState
import com.yaochin.battleship.util.IdGenerator


/**
  * Created on 2/4/17.
  */
case class UserBuilder(id: String = IdGenerator.next,
                       opponentId: Option[String] = Some(IdGenerator.next),
                       state: UserState = UserState.Active,
                       fleet: Seq[Battleship] = Seq.empty,
                       events: List[Event] = List.empty) {

  def withId(id: String) = copy(id = id)

  def withOpponentId(opponentId: String) = copy(opponentId = Some(opponentId))

  def withoutOpponentId = copy(opponentId = None)

  def withFleet(fleet: Seq[Battleship]) = copy(fleet = fleet)

  def withEvents(events: List[Event]) = copy(events = events)

  def addEvent(event: Event) = copy(events = events :+ event)

  def addShip(ship: Battleship) = copy(fleet = fleet :+ ship)

  def withState(state: UserState) = copy(state = state)

  def build = {
    val userSession = User(
      id = id,
      opponentId = opponentId,
      state = state,
      fleet = fleet,
      events = events
    )

    userSession
  }

}
