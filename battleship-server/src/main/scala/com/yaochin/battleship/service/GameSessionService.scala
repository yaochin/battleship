package com.yaochin.battleship.service

import javax.inject.{Inject, Singleton}

import com.twitter.util.{Future, FuturePool}
import com.yaochin.battleship.domain._
import com.yaochin.battleship.domain.api._
import com.yaochin.battleship.util.IdGenerator
import grizzled.slf4j.Logging

/**
  * Created on 2/5/17.
  */
trait GameSessionService {
  def join(joinRequest: JoinRequest): Future[JoinResponse]
  def get(userId: String): Future[Option[UserDetailsResponse]]
  def listUsers: Future[UserResponses]
}

@Singleton
class GameSessionServiceImpl @Inject()(pool: FuturePool, gameMatrix: GameMatrix, idGenerator: IdGenerator)
  extends GameSessionService
  with Logging {

  override def join(joinRequest: JoinRequest): Future[JoinResponse] = {
    pool {
      val newUserId = idGenerator.next
      val maybeOpponent = gameMatrix.withWriteLock {
        gameMatrix.nextAvailableSession match {
          case Some(session) =>
            val updatedSession = session.copy(opponentId = Some(newUserId), state = UserState.Active)
            gameMatrix.addOrUpdate(updatedSession)
            Some(updatedSession)
          case None => None
        }
      }

      val userSession = createNewUserSession(newUserId, maybeOpponent.map(_.id), joinRequest.battleships)
      gameMatrix.addOrUpdate(userSession)

      JoinResponse(userSession.id, userSession.opponentId, joinRequest)
    }
  }

  override def get(userId: String): Future[Option[UserDetailsResponse]] = {
    Future.value(gameMatrix.get(userId).map(UserDetailsResponse(_)))
  }

  override def listUsers: Future[UserResponses] = {
    pool {
      UserResponses(gameMatrix.list.map(UserResponse(_)))
    }
  }

  private[service] def createNewUserSession(userId: String, opponentId: Option[String], battleships: Seq[BattleshipRequest]): User = {

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

  private[service] def fromBattleshipRepresentation(battleshipRepresentation: BattleshipRequest): Battleship = {
    val locationInfo: Map[Location, ShipLocationState.Value] = battleshipRepresentation.locations
      .map(loc => Location(loc.x, loc.y) -> ShipLocationState.Normal)
      .toMap
    Battleship(locationInfo)
  }

}
