package com.yaochin.battleship.service

import com.yaochin.battleship.domain.{AttackEvent, Event, UserBuilder}
import org.scalatest.{FreeSpec, Matchers}

/**
  * Created on 2/4/17.
  */
class BattlefieldServiceImplTest extends FreeSpec with Matchers {

  def createUserSession(events: List[Event] = List(AttackEvent(1, 2), AttackEvent(2, 2))) = {
    UserBuilder()
      .withEvents(events)
      .build
  }

//  "saveAttack" - {
//    "return AlreadyTaken" in {
//      // Given
//      val userSession = createUserSession()
//      val management = new UserSessionManagement(userSession)
//
//      // When / Then
//      management.saveAttackEvent(Location(1,2)) should be(Location(1, 2), Some(AttackReturn.AlreadyTaken))
//    }
//
//    "save event" in {
//      // Given
//      val events = Seq(AttackEvent(1,2), AttackEvent(2,2))
//      val userSession = createUserSession(events)
//      val management = new UserSessionManagement(userSession)
//
//      // When
//      management.saveAttackEvent(Location(1,3))
//
//      // Then
//      userSession.events should be(events :+ AttackEvent(1,3))
//    }
//  }

}
