package com.yaochin.battleship.service

import javax.inject.Inject

import com.twitter.util.{Future, FuturePool}
import com.yaochin.battleship.domain.AttackResult._
import com.yaochin.battleship.domain.{AttackResult, _}
import com.yaochin.battleship.domain.api.{UserDetailsResponse, AttackResponse, AttackRequest}

/**
  * Created on 1/31/17.
  */
trait BattlefieldService {
  def attack(userId: String, attackRequest: AttackRequest): Future[AttackResponse]
}

class BattlefieldServiceImpl @Inject()(pool: FuturePool, matrix: GameMatrix)
  extends BattlefieldService {

  override def attack(userId: String, attackRequest: AttackRequest): Future[AttackResponse] = {
    pool{
      matrix.withWriteLock{
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
          AttackResponse(
            result = output.result.getOrElse(throw new IllegalStateException(s"Unable to retrieve the attack result: $userId, attackRequest: $attackRequest")),
            self = UserDetailsResponse(output.self),
            opponent = UserDetailsResponse(output.opponent)
          )
        }.getOrElse(
          throw new IllegalStateException(s"Unable to process the request: $userId, attackRequest: $attackRequest")
        )
      }

    }
  }

  def validation(source: User, target: User): Unit = {
    if ( source.state != UserState.Active && target.state != UserState.Passive)
      throw new IllegalArgumentException(s"Cannot attack when you are not in attack mode, userId: ${source.id}")
  }

  def handler = record andThen process andThen modifyUsers

  val record: (AttackInputOutput) => (AttackInputOutput) = {
    case AttackInputOutput(source, target, loc, _) =>
      if (target.hasTaken(loc)){
        AttackInputOutput(source, target, loc, Some(AttackResult.AlreadyTaken))
      } else {
        val updatedTarget = target.addEvent(AttackEvent(loc))
        AttackInputOutput(source, updatedTarget, loc, None)
      }
  }

  val process: (AttackInputOutput) => (AttackInputOutput) = {
    case AttackInputOutput(source, target, loc, maybeReturn) =>
      maybeReturn match {
        case Some(r) =>
          // already taken
          AttackInputOutput(source, target, loc, maybeReturn)
        case None if target.contains(loc) =>
          // hit and possible sunk or win
          val ships = target.fleet.map{ ship =>
            ship.updateShipIfNecessary(loc)
          }

          val targetUpdated = target.copy(fleet = ships)

          val attackReturn = if (targetUpdated.hasLost) {
            AttackResult.Win
          } else if (targetUpdated.hasSunk(loc)) {
            AttackResult.Sunk
          } else {
            AttackResult.Hit
          }

          AttackInputOutput(source, targetUpdated, loc, Some(attackReturn))
        case _ =>
          // miss
          AttackInputOutput(source, target, loc, Some(AttackResult.Miss))
      }
  }

  val modifyUsers: (AttackInputOutput) => (AttackInputOutput) = {
    case AttackInputOutput(source, target, loc, Some(attackReturn)) =>
      val (newSourceState, newTargetState) = if (attackReturn == AttackResult.Win)
        (UserState.Won, UserState.Lost)
      else
        (UserState.Passive, UserState.Active)

      AttackInputOutput(
        self = source.copy(state = newSourceState),
        opponent = target.copy(state = newTargetState),
        loc = loc,
        result = Some(attackReturn)
      )
  }

}

case class AttackInputOutput(self: User, opponent: User, loc: Location, result: Option[AttackResult] = None)