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
      val result = matrix.withWriteLock{
        for {
          source <- matrix.get(userId)
          targetId <- source.opponentId
          target <- matrix.get(targetId)
        } yield {
          processAttack(AttackInputOutput(source, target, Location(attackRequest.x, attackRequest.y)))
        }
      }

      result.flatten.getOrElse {
        val source = matrix.get(userId)
        val target = source.flatMap(u => u.opponentId.flatMap(matrix.get))
          throw new IllegalStateException(s"Unexpected state: source: $source, target: $target, loc: $attackRequest")
      }
    }
  }

  def processAttack(attack: AttackInputOutput): Option[AttackResponse] = {
    val inputOutput = (record andThen process andThen modifyUsers) (attack)
    matrix.addOrUpdate(inputOutput.source)
    matrix.addOrUpdate(inputOutput.target)

    inputOutput.result.map(output =>
      AttackResponse(output,
        UserDetailsResponse(inputOutput.source),
        UserDetailsResponse(inputOutput.target)
      )
    )
  }

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
        source = source.copy(state = newSourceState),
        target = target.copy(state = newTargetState),
        loc = loc,
        result = Some(attackReturn)
      )
  }

}

case class AttackInputOutput(source: User, target: User, loc: Location, result: Option[AttackResult] = None)