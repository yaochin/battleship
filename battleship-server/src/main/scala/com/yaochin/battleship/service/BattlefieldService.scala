package com.yaochin.battleship.service

import javax.inject.Inject

import com.twitter.util.{FuturePool, Future}
import com.yaochin.battleship.domain.AttackReturn
import com.yaochin.battleship.domain.AttackReturn._
import com.yaochin.battleship.domain._
import com.yaochin.battleship.domain.representation.AttackRequestRepresentation
import com.yaochin.battleship.domain.representation.AttackRequestRepresentation._

/**
  * Created on 1/31/17.
  */
trait BattlefieldService {
  def attack(userId: String, attackRequest: AttackRequestRepresentation): Future[AttackReturn]
}

class BattlefieldServiceImpl @Inject()(pool: FuturePool, matrix: GameMatrix)
  extends BattlefieldService {

  override def attack(userId: String, attackRequest: AttackRequestRepresentation): Future[AttackReturn] = {
    pool{
      val result = for {
        source <- matrix.get(userId)
        target <- matrix.get(attackRequest.opponentId)
      } yield {
        processAttack(AttackInputOutput(source, target, attackRequest.location))
      }

      result.flatten.getOrElse(
        throw new IllegalStateException(s"Unexpected state: source: ${matrix.get(userId)}, target: ${matrix.get(attackRequest.opponentId)}, target: $attackRequest")
      )
    }
  }

  def processAttack(attack: AttackInputOutput): Option[AttackReturn] = {
    matrix.withWriteLock {
      val inputOutput = (record andThen process andThen modifyUsers) (attack)
      matrix.addOrUpdate(inputOutput.source)
      matrix.addOrUpdate(inputOutput.target)
      inputOutput.result
    }
  }

  val record: (AttackInputOutput) => (AttackInputOutput) = {
    case AttackInputOutput(source, target, loc, _) =>
      if (target.hasTaken(loc)){
        AttackInputOutput(source, target, loc, Some(AttackReturn.AlreadyTaken))
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
            AttackReturn.Win
          } else if (targetUpdated.hasSunk(loc)) {
            AttackReturn.Sunk
          } else {
            AttackReturn.Hit
          }

          AttackInputOutput(source, targetUpdated, loc, Some(attackReturn))
        case _ =>
          // miss
          AttackInputOutput(source, target, loc, Some(AttackReturn.Miss))
      }
  }

  val modifyUsers: (AttackInputOutput) => (AttackInputOutput) = {
    case AttackInputOutput(source, target, loc, Some(attackReturn)) =>
      val (newSourceState, newTargetState) = if (attackReturn == AttackReturn.Win)
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

case class AttackInputOutput(source: User, target: User, loc: Location, result: Option[AttackReturn] = None)