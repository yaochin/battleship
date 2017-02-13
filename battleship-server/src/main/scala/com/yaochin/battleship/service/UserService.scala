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
trait UserService {
  def join(joinRequest: JoinRequest): Future[JoinResponse]
  def get(userId: String): Future[Option[UserDetailsResponse]]
  def list: Future[UserResponses]
}

@Singleton
class UserServiceImpl @Inject()(pool: FuturePool, gameMatrix: GameMatrix, idGenerator: IdGenerator)
  extends UserService
  with Logging {

  override def join(joinRequest: JoinRequest): Future[JoinResponse] = {
    pool {
      val newUserId = idGenerator.next
      val maybeOpponent = gameMatrix.withWriteLock {
        gameMatrix.nextAvailableUser match {
          case Some(session) =>
            val updatedSession = session.copy(opponentId = Some(newUserId), state = UserState.Active)
            gameMatrix.addOrUpdate(updatedSession)
            Some(updatedSession)
          case None => None
        }
      }

      val userSession = createNewUser(newUserId, maybeOpponent.map(_.id), joinRequest.battleships)
      gameMatrix.addOrUpdate(userSession)

      JoinResponse(userSession.id, userSession.opponentId, joinRequest)
    }
  }

  override def get(userId: String): Future[Option[UserDetailsResponse]] = {
    Future.value(gameMatrix.get(userId).map(UserDetailsResponse(_)))
  }

  override def list: Future[UserResponses] = {
    pool {
      UserResponses(gameMatrix.list.map(UserResponse(_)))
    }
  }

  private[service] def createNewUser(userId: String, opponentId: Option[String], battleships: Seq[BattleshipRequest]): User = {

    val initState = if (opponentId.isDefined) UserState.Passive
                    else UserState.Initial

    val fleet = battleships.map(fromBattleshipRequest _)

    User(id = userId,
      opponentId = opponentId,
      state = initState,
      fleet = fleet,
      events = fleet.map(SetupEvent(_)).toList
    )
  }

  private[service] def fromBattleshipRequest(battleship: BattleshipRequest): Battleship = {
    val locationInfo: Map[Location, ShipLocationState.Value] = battleship.locations
      .map(loc => Location(loc.x, loc.y) -> ShipLocationState.Normal)
      .toMap
    Battleship(locationInfo)
  }

}
