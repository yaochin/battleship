package com.yaochin.battleship.service

import java.util.concurrent.Executors

import com.twitter.util.{Await, FuturePool}
import com.yaochin.battleship.domain._
import com.yaochin.battleship.domain.api._
import com.yaochin.battleship.util.IdGenerator
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FreeSpec, Matchers}
import org.mockito.Mockito._

import scala.collection.mutable.ListBuffer

/**
  * Created on 2/5/17.
  */
class UserServiceImplTest extends FreeSpec with Matchers with MockitoSugar {

  private val futurePool = FuturePool(Executors.newFixedThreadPool(2))

  def createService() = {
    val gameMatrix = new GameMatrix {}
    val idGenerator = spy(new TestableIdGenerator)
    val service = new UserServiceImpl(futurePool, gameMatrix, idGenerator)
    (service, gameMatrix, idGenerator)
  }

  "createNewUserSession" - {
    "return a user " in {
      // Given
      val (service, _, _) = createService()
      val userId = IdGenerator.next
      val opponentId = IdGenerator.next
      val ships = Seq(
        BattleshipRequest(List(LocationRequest(1, 2), LocationRequest(2, 2))),
        BattleshipRequest(List(LocationRequest(3, 4)))
      )

      // When
      val actual = service.createNewUser(userId, Some(opponentId), ships)

      // Then
      actual.id should be(userId)
      actual.opponentId should be(Some(opponentId))
      actual.state should be(UserState.Passive)
      actual.fleet should be(Seq(
        Battleship(Map(
          Location(1,2)-> ShipLocationState.Normal,
          Location(2,2)-> ShipLocationState.Normal
        )),
        Battleship(Map(
          Location(3,4)-> ShipLocationState.Normal
        ))
      ))
      actual.events should be(ListBuffer(
        SetupEvent(Battleship(Map(
          Location(1,2)-> ShipLocationState.Normal,
          Location(2,2)-> ShipLocationState.Normal
        ))),
        SetupEvent(Battleship(Map(
          Location(3,4)-> ShipLocationState.Normal
        )))
      ))
    }
  }

  "Join" - {
    "return user w/o opponent" in {
      // Given
      val userId = "123456"
      val (service, matrix, idGenerator) = createService()
      when(idGenerator.next).thenReturn(userId)
      val joinRequest = JoinRequest(Seq(
        BattleshipRequest(Seq(LocationRequest(1,2))
      )))

      // When
      val actual = Await.result(service.join(joinRequest))

      // Then
      actual should be(JoinResponse(
        userId = userId,
        opponentId = None,
        joinRequest
      ))
      matrix.get(userId) should be(Some(
        service.createNewUser(userId, None, joinRequest.battleships)
      ))
    }

    "return user with opponent" in {
      // Given
      val userId = "123456"
      val (service, matrix, idGenerator) = createService()
      val opponent = new UserBuilder()
        .withoutOpponentId
        .build
      matrix.addOrUpdate(opponent)
      when(idGenerator.next).thenReturn(userId)
      val joinRequest = JoinRequest(Seq(
        BattleshipRequest(Seq(LocationRequest(1, 2)))
      ))

      // When
      val actual = Await.result(service.join(joinRequest))

      // Then
      actual should be(JoinResponse(
        userId = userId,
        opponentId = Some(opponent.id),
        joinRequest
      ))
      matrix.get(userId) should be(Some(
        service.createNewUser(userId, Some(opponent.id), joinRequest.battleships)
      ))

      matrix.get(opponent.id) should be(Some(
        opponent.copy(opponentId = Some(userId), state = UserState.Active)
      ))
    }

  }

  "get" - {
    "return Some" in {
      // Given
      val (service, matrix, _) = createService()
      val user = UserBuilder().build
      matrix.addOrUpdate(user)

      // When / Then
      Await.result(service.get(user.id)) should be(
        Some(UserDetailsResponse(user))
      )
    }

    "return None" in {
      val (service, _, _) = createService()
      val user = UserBuilder().build

      // When / Then
      Await.result(service.get(user.id)) should be(None)
    }
  }

  "listUsers" - {
    "return list" in {
      // Given
      val (service, matrix, _) = createService()
      val user1 = UserBuilder().build
      val user2 = UserBuilder().build
      matrix.addOrUpdate(user1)
      matrix.addOrUpdate(user2)

      // When / Then
      Await.result(service.list).users.toSet should be(
        Set(UserResponse(user1), UserResponse(user2))
      )
    }

    "return empty list" in {
      // Given
      val (service, _, _) = createService()

      // When / Then
      Await.result(service.list) should be(
        UserResponses(Seq.empty)
      )

    }
  }

}

class TestableIdGenerator extends IdGenerator