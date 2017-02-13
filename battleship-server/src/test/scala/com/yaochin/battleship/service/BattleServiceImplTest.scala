package com.yaochin.battleship.service

import java.util.concurrent.Executors

import com.twitter.util.{Await, FuturePool}
import com.yaochin.battleship.domain.AttackResult.AttackResult
import com.yaochin.battleship.domain._
import com.yaochin.battleship.domain.api.{AttackRequest, AttackResponse, UserDetailsResponse}
import com.yaochin.battleship.util.IdGenerator
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FreeSpec, Matchers}

/**
  * Created on 2/4/17.
  */
class BattleServiceImplTest extends FreeSpec with Matchers with MockitoSugar{

  val futurePool = FuturePool(Executors.newFixedThreadPool(2))

  def testSetup(self: User, opponent: User, target: Location, result: Option[AttackResult] = None ) = {
    val gameMatrix = new GameMatrix{}
    gameMatrix.addOrUpdate(self)
    gameMatrix.addOrUpdate(opponent)
    val service = spy(new BattleServiceImpl(futurePool, gameMatrix))
    val inputOutput = AttackInputOutput(self, opponent, target, result)
    (service, inputOutput, gameMatrix)
  }

  "record" - {
    "AlreadyTaken" in {
      // Given
      val self = UserBuilder().build
      val opponent = UserBuilder().addEvent(AttackEvent(1, 2)).build
      val target = Location(1, 2)
      val (service, input, _) = testSetup(self, opponent, target)

      // When / Then
      service.record(input) should be(
        AttackInputOutput(self, opponent, target, Some(AttackResult.AlreadyTaken))
      )
    }

    "save the record" in {
      // Given
      val self = UserBuilder().build
      val opponent = UserBuilder().build
      val target = Location(1, 2)
      val (service, input, _) = testSetup(self, opponent, target)

      // When / Then
      service.record(input) should be(
        AttackInputOutput(self, opponent.addEvent(AttackEvent(target)), target, None)
      )
    }
  }

  "process" - {
    "Miss" in {
      // Given
      val self = UserBuilder().build
      val opponent = UserBuilder()
        .addShip(BattleshipBuilder()
          .withLocation(1,2)
          .build)
        .build
      val target = Location(1, 1)
      val (service, input, _) = testSetup(self, opponent, target)

      // When / Then
      service.process(input) should be(
        AttackInputOutput(self, opponent, target, Some(AttackResult.Miss))
      )
    }

    "Hit" in {
      // Given
      val self = UserBuilder().build
      val opponentShip = BattleshipBuilder()
        .withLocation(1,2)
        .withLocation(1,3)
        .build
      val opponent = UserBuilder()
        .addShip(opponentShip)
        .build
      val target = Location(1, 2)
      val (service, input, _) = testSetup(self, opponent, target)

      // When / Then
      service.process(input) should be(
        AttackInputOutput(self, opponent.handleAttack(target), target, Some(AttackResult.Hit))
      )
    }

    "Sunk" in {
      // Given
      val self = UserBuilder().build
      val opponentShip1 = BattleshipBuilder()
        .withLocation(1, 2)
        .withLocation(2, 2, ShipLocationState.Hit)
        .build
      val opponentShip2 = BattleshipBuilder()
        .withLocation(1, 3)
        .build
      val opponent = UserBuilder()
        .addShip(opponentShip1)
        .addShip(opponentShip2)
        .build
      val target = Location(1, 2)
      val (service, input, _) = testSetup(self, opponent, target)

      // When / Then
      service.process(input) should be(
        AttackInputOutput(self, opponent.handleAttack(target), target, Some(AttackResult.Sunk))
      )
    }

    "Win" in {
      // Given
      val self = UserBuilder().build
      val opponentShip1 = BattleshipBuilder()
        .withLocation(1,2)
        .withLocation(2, 2, ShipLocationState.Hit)
        .build
      val opponentShip2 = BattleshipBuilder()
        .withLocation(1,3, ShipLocationState.Hit)
        .build
      val opponent = UserBuilder()
        .addShip(opponentShip1)
        .addShip(opponentShip2)
        .build
      val target = Location(1, 2)
      val (service, input, _) = testSetup(self, opponent, target)

      // When / Then
      service.process(input) should be(
        AttackInputOutput(self, opponent.handleAttack(target), target, Some(AttackResult.Win))
      )
    }
  }

  "changeUserState" - {
    "Win and Lost" in {
      // Given
      val self = UserBuilder().build
      val opponent = UserBuilder().build
      val target = Location(1, 2)
      val (service, input, _) = testSetup(self, opponent, target, Some(AttackResult.Win))

      // When / Then
      service.changeUserState(input) should be(
        AttackInputOutput(self.copy(state = UserState.Won), opponent.copy(state = UserState.Lost), target, Some(AttackResult.Win))
      )
    }

    "Active and Passive" in {
      val self = UserBuilder().withState(UserState.Active).build
      val opponent = UserBuilder()
        .withState(UserState.Passive)
        .build
      val target = Location(1, 1)
      val (service, input, _) = testSetup(self, opponent, target, Some(AttackResult.Miss))

      // When / Then
      service.changeUserState(input) should be(
        AttackInputOutput(self.copy(state = UserState.Passive), opponent.copy(state = UserState.Active), target, Some(AttackResult.Miss))
      )
    }
  }

  "attack" - {

    "return AlreadyTaken" in {
      // Given
      val opponentId = IdGenerator.next
      val self = UserBuilder()
        .withOpponentId(opponentId)
        .withState(UserState.Active)
        .build
      val opponent = UserBuilder()
        .withId(opponentId)
        .withOpponentId(self.id)
        .withState(UserState.Passive)
        .addEvent(AttackEvent(1, 2))
        .build

      val target = Location(1, 2)
      val attack = AttackRequest(target.x, target.y)
      val (service, _, gameMatrix) = testSetup(self, opponent, target)

      // When
      val actual = Await.result(service.attack(self.id, attack))

      // Then
      actual should be(AttackResponse(
        result = AttackResult.AlreadyTaken,
        self = UserDetailsResponse(gameMatrix.get(self.id).get),
        opponent = UserDetailsResponse(gameMatrix.get(opponent.id).get)
      ))
    }

    "return Hit" in {
      // Given
      val opponentId = IdGenerator.next
      val self = UserBuilder()
        .withOpponentId(opponentId)
        .withState(UserState.Active)
        .build
      val opponent = UserBuilder()
        .withId(opponentId)
        .withOpponentId(self.id)
        .withState(UserState.Passive)
        .addShip(BattleshipBuilder()
          .withLocation(1,2)
          .withLocation(1,1)
          .build)
        .build

      val target = Location(1, 2)
      val attack = AttackRequest(target.x, target.y)
      val (service, _, gameMatrix) = testSetup(self, opponent, target)

      // When
      val actual = Await.result(service.attack(self.id, attack))

      // Then
      actual should be(AttackResponse(
        result = AttackResult.Hit,
        self = UserDetailsResponse(gameMatrix.get(self.id).get),
        opponent = UserDetailsResponse(gameMatrix.get(opponent.id).get)
      ))
    }

    "return Miss" in {
      // Given
      val opponentId = IdGenerator.next
      val self = UserBuilder()
        .withOpponentId(opponentId)
        .withState(UserState.Active)
        .build
      val opponent = UserBuilder()
        .withId(opponentId)
        .withOpponentId(self.id)
        .withState(UserState.Passive)
        .addShip(BattleshipBuilder()
          .withLocation(1,1)
          .build)
        .build

      val target = Location(1, 2)
      val attack = AttackRequest(target.x, target.y)
      val (service, _, gameMatrix) = testSetup(self, opponent, target)

      // When
      val actual = Await.result(service.attack(self.id, attack))

      // Then
      actual should be(AttackResponse(
        result = AttackResult.Miss,
        self = UserDetailsResponse(gameMatrix.get(self.id).get),
        opponent = UserDetailsResponse(gameMatrix.get(opponent.id).get)
      ))
    }

    "return Sunk" in {
      val opponentId = IdGenerator.next
      val self = UserBuilder()
        .withOpponentId(opponentId)
        .withState(UserState.Active)
        .build
      val opponentShip1 = BattleshipBuilder()
        .withLocation(1, 2)
        .withLocation(2, 2, ShipLocationState.Hit)
        .build
      val opponentShip2 = BattleshipBuilder()
        .withLocation(1, 3)
        .build
      val opponent = UserBuilder()
        .withId(opponentId)
        .withOpponentId(self.id)
        .withState(UserState.Passive)
        .addShip(opponentShip1)
        .addShip(opponentShip2)
        .build

      val target = Location(1, 2)
      val attack = AttackRequest(target.x, target.y)
      val (service, _, gameMatrix) = testSetup(self, opponent, target)

      // When
      val actual = Await.result(service.attack(self.id, attack))

      // Then
      actual should be(AttackResponse(
        result = AttackResult.Sunk,
        self = UserDetailsResponse(gameMatrix.get(self.id).get),
        opponent = UserDetailsResponse(gameMatrix.get(opponent.id).get)
      ))
    }

    "return Win" in {
      val opponentId = IdGenerator.next
      val self = UserBuilder()
        .withOpponentId(opponentId)
        .withState(UserState.Active)
        .build
      val opponentShip1 = BattleshipBuilder()
        .withLocation(1, 2)
        .withLocation(2, 2, ShipLocationState.Hit)
        .build
      val opponentShip2 = BattleshipBuilder()
        .withLocation(1, 3, ShipLocationState.Hit)
        .build
      val opponent = UserBuilder()
        .withId(opponentId)
        .withOpponentId(self.id)
        .withState(UserState.Passive)
        .addShip(opponentShip1)
        .addShip(opponentShip2)
        .build

      val target = Location(1, 2)
      val attack = AttackRequest(target.x, target.y)
      val (service, _, gameMatrix) = testSetup(self, opponent, target)

      // When
      val actual = Await.result(service.attack(self.id, attack))

      // Then
      actual should be(AttackResponse(
        result = AttackResult.Win,
        self = UserDetailsResponse(gameMatrix.get(self.id).get),
        opponent = UserDetailsResponse(gameMatrix.get(opponent.id).get)
      ))
    }


    "return IllegalArgumentException if user is not in active mode" in {
      // Given
      val opponentId = IdGenerator.next
      val self = UserBuilder()
        .withOpponentId(opponentId)
        .withState(UserState.Passive)
        .build
      val opponent = UserBuilder()
        .withId(opponentId)
        .withOpponentId(self.id)
        .build
      val target = Location(1, 2)
      val attack = AttackRequest(target.x, target.y)
      val (service, _, _) = testSetup(self, opponent, target)

      // When / Then
      intercept[IllegalArgumentException](
        Await.result(service.attack(self.id, attack))
      )
    }

    "return IllegalArgumentException when opponent is not in passive mode" in {
      // Given
      val opponentId = IdGenerator.next
      val self = UserBuilder()
        .withOpponentId(opponentId)
        .withState(UserState.Active)
        .build
      val opponent = UserBuilder()
        .withId(opponentId)
        .withState(UserState.Active)
        .withOpponentId(self.id)
        .build
      val target = Location(1, 2)
      val attack = AttackRequest(target.x, target.y)
      val (service, _, _) = testSetup(self, opponent, target)

      // When / Then
      intercept[IllegalArgumentException](
        Await.result(service.attack(self.id, attack))
      )
    }

    "return IllegalStateException if user not found" in {
      // Given
      val self = UserBuilder().build
      val opponent = UserBuilder().build
      val target = Location(1, 2)
      val attack = AttackRequest(target.x, target.y)
      val (service, _, matrix) = testSetup(self, opponent, target)

      matrix.remove(self.id)

      // When / Then
      intercept[IllegalStateException](
        Await.result(service.attack(self.id, attack))
      )
    }

    "return IllegalStateException if opponent not found" in {
      // Given
      val self = UserBuilder().build
      val opponent = UserBuilder().build
      val target = Location(1, 2)
      val attack = AttackRequest(target.x, target.y)
      val (service, _, matrix) = testSetup(self, opponent, target)

      matrix.remove(opponent.id)

      // When / Then
      intercept[IllegalStateException](
        Await.result(service.attack(self.id, attack))
      )
    }

    "return IllegalStateException if no attack result" in {
      // Given
      val opponentId = IdGenerator.next
      val self = UserBuilder()
        .withOpponentId(opponentId)
        .withState(UserState.Active)
        .build
      val opponent = UserBuilder()
        .withId(opponentId)
        .withState(UserState.Passive)
        .withOpponentId(self.id)
        .build
      val target = Location(1, 2)
      val attack = AttackRequest(target.x, target.y)
      val (service, _, _) = testSetup(self, opponent, target, Some(AttackResult.AlreadyTaken))

      when(service.changeUserState).thenReturn(
        (attackInputOutput: AttackInputOutput) => AttackInputOutput(self, opponent, target, None)
      )

      // When / Then
      intercept[IllegalStateException](
        Await.result(service.attack(self.id, attack))
      )
    }

  }

}
