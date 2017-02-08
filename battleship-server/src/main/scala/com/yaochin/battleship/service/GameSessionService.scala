package com.yaochin.battleship.service

import javax.inject.{Inject, Singleton}

import com.twitter.util.{Future, FuturePool}
import com.yaochin.battleship.domain._
import com.yaochin.battleship.domain.representation._
import com.yaochin.battleship.util.{IdGenerator, ProcessHelper}
import grizzled.slf4j.Logging

import scala.collection.mutable.ListBuffer

/**
  * Created on 2/5/17.
  */
trait GameSessionService {
  def join(joinRequest: JoinRequestRepresentation): Future[JoinResponseRepresentation]
  def listUserSessions: Future[UserSessionsRepresentation]
}

@Singleton
class GameSessionServiceImpl @Inject()(pool: FuturePool, gameMatrix: GameMatrix, idGenerator: IdGenerator)
  extends GameSessionService
  with ProcessHelper
  with Logging {

  override def join(joinRequest: JoinRequestRepresentation): Future[JoinResponseRepresentation] = {
    pool {
      val newUserId = idGenerator.next
      val maybeOpponent = waitUntil(totalInSeconds = 5, sleepInSeconds = 1) {
        gameMatrix.withWriteLock{
          gameMatrix.nextAvailableSession match {
            case Some(session) =>
              val updatedSession = session.copy(opponentId = Some(newUserId), state = UserState.Active)
              gameMatrix.addOrUpdate(updatedSession)
              Some(updatedSession)
            case None => None
          }
        }
      }

      val userSession = createNewUserSession(newUserId, maybeOpponent.map(_.id), joinRequest.battleships)
      gameMatrix.addOrUpdate(userSession)

      JoinResponseRepresentation(userSession.id, userSession.opponentId, joinRequest)
    }

  }

  override def listUserSessions: Future[UserSessionsRepresentation] = {
    pool {
      gameMatrix.withReadLock {
        UserSessionsRepresentation(gameMatrix.listUserSessions.map(fromUserSession _))
      }
    }
  }

  private[service] def createNewUserSession(userId: String, opponentId: Option[String], battleships: Seq[BattleshipRepresentation]): User = {

    val initState = if (opponentId.isDefined) UserState.Passive
                    else UserState.Initial

    val fleet = battleships.map(fromBattleshipRepresentation _)

    User(id = userId,
      opponentId = opponentId,
      state = initState,
      fleet = fleet,
      events = fleet.map(SetupEvent(_)).toList
    )
  }

  private[service] def fromBattleshipRepresentation(battleshipRepresentation: BattleshipRepresentation): Battleship = {
    val locationInfo: Map[Location, ShipLocationState.Value] = battleshipRepresentation.locations
      .map(loc => Location(loc.x, loc.y) -> ShipLocationState.Normal)
      .toMap
    Battleship(locationInfo)
  }

  private[service] def fromUserSession(session: User): UserSessionRepresentation = {
    UserSessionRepresentation(session.id, session.opponentId, session.state)
  }

}
