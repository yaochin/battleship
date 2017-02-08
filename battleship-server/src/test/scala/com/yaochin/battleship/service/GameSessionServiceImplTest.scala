package com.yaochin.battleship.service

import java.util.concurrent.Executors

import com.twitter.util.FuturePool
import com.yaochin.battleship.domain._
import com.yaochin.battleship.domain.representation.{BattleshipRepresentation, LocationRepresentation}
import com.yaochin.battleship.util.IdGenerator
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.mutable.ListBuffer

/**
  * Created on 2/5/17.
  */
class GameSessionServiceImplTest extends FreeSpec with Matchers {

  private val futurePool = FuturePool(Executors.newFixedThreadPool(2))

  def createService() = {
    new GameSessionServiceImpl(futurePool, GameMatrix, IdGenerator)
  }


//  "listQueue" - {
//    "return game waiting queue" in {
//      // Given
//      val service = createService()
//
//      // When / Then
//      Await.result(service.listUserSessions()) should be(UserSessionsRepresentation(List.empty))
//    }
//  }

  "createNewUserSession" - {
    "return a user session" in {
      // Given
      val service = createService()
      val userId = IdGenerator.next
      val opponentId = IdGenerator.next
      val ships = Seq(
        BattleshipRepresentation(List(LocationRepresentation(1, 2), LocationRepresentation(2, 2))),
        BattleshipRepresentation(List(LocationRepresentation(3, 4)))
      )

      // When
      val actual = service.createNewUserSession(userId, Some(opponentId), ships)

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

}
