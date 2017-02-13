package com.yaochin.battleship.service

import com.yaochin.battleship.domain.{UserState, UserBuilder}
import org.scalatest.{FreeSpec, Matchers}

/**
  * Created on 2/7/17.
  */
class GameMatrixTest extends FreeSpec with Matchers{

  "nextAvailableSession" - {
    "return user w/o opponent" in {
      // Given
      val gameMatrix = new GameMatrix {}
      val userSession1 = UserBuilder().build
      val userSession2 = UserBuilder().build
      val userWithoutOpponent = UserBuilder().withoutOpponentId.build
      gameMatrix.addOrUpdate(userSession1)
      gameMatrix.addOrUpdate(userSession2)
      gameMatrix.addOrUpdate(userWithoutOpponent)

      // When / Then
      gameMatrix.nextAvailableUser should be(Some(userWithoutOpponent))
    }
  }

  "addOrUpdate" - {
    "replace existing user" in {
      // Given
      val gameMatrix = new GameMatrix {}
      val user = UserBuilder().build
      gameMatrix.addOrUpdate(user)
      val updated = user.copy(state = UserState.Won)

      // When
      gameMatrix.addOrUpdate(updated)

      // Then
      gameMatrix.get(user.id) should be(Some(updated))
    }
  }

  "get" - {
    "return user" in {
      // Given
      val gameMatrix = new GameMatrix {}
      val user = UserBuilder().build

      gameMatrix.addOrUpdate(user)

      // When / Then
      gameMatrix.get(user.id) should be(Some(user))

    }
  }

}
