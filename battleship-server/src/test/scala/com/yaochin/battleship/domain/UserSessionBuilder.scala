package com.yaochin.battleship.domain

import com.yaochin.battleship.domain.UserState.UserState
import com.yaochin.battleship.util.IdGenerator

import scala.collection.mutable.ListBuffer


/**
  * Created on 2/4/17.
  */
case class UserSessionBuilder(userId: String = IdGenerator.next,
                              opponentId: Option[String] = Some(IdGenerator.next),
                              state: UserState = UserState.Active,
                              fleet: Seq[Battleship] = Seq.empty,
                              events: List[Event] = List.empty) {

  def withoutOpponentId = copy(opponentId = None)

  def withFleet(fleet: Seq[Battleship]) = copy(fleet = fleet)

  def withEvents(events: List[Event]) = copy(events = events)

  def build = {
    val userSession = User(
      id = userId,
      opponentId = opponentId,
      state = state,
      fleet = fleet,
      events = events
    )

    userSession
  }

}
