package com.yaochin.battleship.service

import com.yaochin.battleship.domain.{UserState, UserBuilder}
import org.scalatest.{FreeSpec, Matchers}

/**
  * Created on 2/7/17.
  */
class GameMatrixTest extends FreeSpec with Matchers{

  "nextAvailableSession" - {
    "return userSession w/o opponent" in {
      // Given
      val gameMatrix = new GameMatrix {}
      val userSession1 = UserBuilder().build
      val userSession2 = UserBuilder().build
      val userWithoutOpponent = UserBuilder().withoutOpponentId.build
      gameMatrix.addOrUpdate(userSession1)
      gameMatrix.addOrUpdate(userSession2)
      gameMatrix.addOrUpdate(userWithoutOpponent)

      // When / Then
      gameMatrix.nextAvailableSession should be(Some(userWithoutOpponent))
    }
  }

  "addOrUpdate" - {
    "replace existing one" in {
      // Given
      val gameMatrix = new GameMatrix {}
      val userSession = UserBuilder().build
      gameMatrix.addOrUpdate(userSession)
      val updated = userSession.copy(state = UserState.Won)

      // When
      gameMatrix.addOrUpdate(updated)

      // Then
      gameMatrix.get(userSession.id) should be(Some(updated))
    }
  }

}
