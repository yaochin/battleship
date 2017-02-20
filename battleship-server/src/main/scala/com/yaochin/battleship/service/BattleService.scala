package com.yaochin.battleship.service

import javax.inject.Inject

import com.twitter.finatra.http.exceptions.NotFoundException
import com.twitter.util.{Future, FuturePool}
import com.yaochin.battleship.domain.AttackResult._
import com.yaochin.battleship.domain.{AttackResult, _}
import com.yaochin.battleship.domain.api.{UserDetailsResponse, AttackResponse, AttackRequest}
import com.twitter.inject.Logging

/**
  * Created on 1/31/17.
  */
trait BattleService {
  def attack(userId: String, attackRequest: AttackRequest): Future[AttackResponse]
}

class BattleServiceImpl @Inject()(pool: FuturePool, matrix: GameMatrix)
  extends BattleService
  with Logging {

  override def attack(userId: String, attackRequest: AttackRequest): Future[AttackResponse] = {
    pool{

      info(s"New user attack request: $userId, $attackRequest")

      val response = matrix.withWriteLock{
        val maybeOutput = for {
          source <- matrix.get(userId)
          targetId <- source.opponentId
          target <- matrix.get(targetId)
        } yield {

          validation(source, target)

          val input = AttackInputOutput(self = source,
            opponent = target,
            loc = Location(attackRequest.x, attackRequest.y))

          handler(input)
        }

        maybeOutput.map{output =>

          matrix.addOrUpdate(output.self)
          matrix.addOrUpdate(output.opponent)

          AttackResponse(
            result = output.result.getOrElse(
              throw new IllegalStateException(s"Unable to retrieve the attack result: $userId, attackRequest: $attackRequest")
            ),
            self = UserDetailsResponse(matrix.get(output.self.id).getOrElse(
              throw new IllegalStateException(s"Unable to retrieve the user: ${output.self.id}"))
            ),
            opponent = UserDetailsResponse(matrix.get(output.opponent.id).getOrElse(
              throw new IllegalStateException(s"Unable to retrieve the opponent: ${output.opponent.id}"))
            )
          )
        }.getOrElse(
          throw new NotFoundException(s"User Id/opponent Id not found, userId: $userId, attackRequest: $attackRequest")
        )
      }

      info(s"Attack response: $userId, $attackRequest, ${response.result}")

      response

    }
  }

  def validation(self: User, opponent: User): Unit = {
    if ( self.state != UserState.Active || opponent.state != UserState.Passive)
      throw new IllegalArgumentException(s"User state is not active, userId: ${self.id}, state: ${self.state}")
  }

  def handler = record andThen process andThen changeUserState

  val record: (AttackInputOutput) => (AttackInputOutput) = {
    case AttackInputOutput(self, opponent, loc, _) =>
      if (opponent.hasTaken(loc)){
        AttackInputOutput(self, opponent, loc, Some(AttackResult.AlreadyTaken))
      } else {
        val updatedTarget = opponent.addEvent(AttackEvent(loc))
        AttackInputOutput(self, updatedTarget, loc, None)
      }
  }

  val process: (AttackInputOutput) => (AttackInputOutput) = {
    case AttackInputOutput(self, opponent, loc, maybeReturn) =>
      maybeReturn match {
        case Some(r) =>
          // already taken
          AttackInputOutput(self, opponent, loc, maybeReturn)
        case None if opponent.contains(loc) =>
          // hit and possible sunk or win
          val updatedOpponent = opponent.handleAttack(loc)

          val attackReturn = if (updatedOpponent.hasLost) {
            AttackResult.Win
          } else if (updatedOpponent.hasSunk(loc)) {
            AttackResult.Sunk
          } else {
            AttackResult.Hit
          }

          AttackInputOutput(self, updatedOpponent, loc, Some(attackReturn))
        case _ =>
          // miss
          AttackInputOutput(self, opponent, loc, Some(AttackResult.Miss))
      }
  }

  val changeUserState: (AttackInputOutput) => (AttackInputOutput) = {
    case AttackInputOutput(self, opponent, loc, Some(attackReturn)) =>
      val (newSourceState, newOpponentState) = if (attackReturn == AttackResult.Win)
        (UserState.Won, UserState.Lost)
      else
        (UserState.Passive, UserState.Active)

      AttackInputOutput(
        self = self.copy(state = newSourceState),
        opponent = opponent.copy(state = newOpponentState),
        loc = loc,
        result = Some(attackReturn)
      )
  }

}

case class AttackInputOutput(self: User, opponent: User, loc: Location, result: Option[AttackResult] = None)