package com.yaochin.battleship

import com.twitter.finagle.http.Status._
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTestMixin
import com.twitter.util.Future
import com.yaochin.battleship.domain._
import com.yaochin.battleship.domain.api._
import com.yaochin.battleship.service.{BattleServiceImpl, BattleService, UserService, UserServiceImpl}
import com.yaochin.battleship.util.JsonSupport
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.FreeSpec
import org.scalatest.mockito.MockitoSugar


/**
  * Created on 2/12/17.
  */
class BattleshipAppTest extends FreeSpec with FeatureTestMixin with MockitoSugar with JsonSupport{

  private val mockUserService = mock[UserServiceImpl]
  private val mockBattleService = mock[BattleServiceImpl]

  override protected def server = new EmbeddedHttpServer(new BattleshipApp)
    .bind[UserService](mockUserService)
    .bind[BattleService](mockBattleService)

  "POST /battleship/users/" - {
    "200" in {
      // Given
      val response = JoinResponse("1234567", None, JoinRequest(Seq(BattleshipRequest(Seq(LocationRequest(1,2))))))
      when(mockUserService.join(any())).thenReturn(Future.value(response))

      // When / Then
      server.httpPost(
        path = "/battleship/users/",
        postBody =
          """
            |{
            |  "battleships": [
            |    {
            |      "locations": [
            |        {
            |          "x": 1,
            |          "y": 2
            |        }
            |      ]
            |    }
            |  ]
            |}
          """.stripMargin,
        andExpect = Ok,
        withJsonBody =
          s"""
            |${toJson(response)}
          """.stripMargin
      )
    }
  }

  "GET /battleship/users" - {
    "200" in {
      // Given
      val user1 = UserBuilder().build
      val user2 = UserBuilder().build
      val response = UserResponses(Seq(UserResponse(user1), UserResponse(user2)))
      when(mockUserService.list).thenReturn(Future.value(response))

      // When / Then
      server.httpGet(
        path = "/battleship/users",
        andExpect = Ok,
        withJsonBody =
          s"""
            |${toJson(response)}
          """.stripMargin
      )
    }

  }

  "POST /battleship/user/:userId/attack" - {
    "200" in {
      // Given
      val ship1 = BattleshipBuilder()
        .withLocation(1,2)
        .withLocation(1,3)
        .build
      val ship2 = BattleshipBuilder()
        .withLocation(5,6)
        .build
      val attacker = UserBuilder()
        .withState(UserState.Active)
        .addShip(ship1)
        .addEvent(AttackEvent(3,4))
        .build
      val receiver = UserBuilder()
        .withState(UserState.Passive)
        .addShip(ship2)
        .addEvent(SetupEvent(ship2))
        .build
      val response = AttackResponse(AttackResult.Hit, UserDetailsResponse(attacker), UserDetailsResponse(receiver))
      when(mockBattleService.attack(any(), any())).thenReturn(Future.value(response))

      // When / Then
      server.httpPost(
        path = s"/battleship/users/${attacker.id}/attack",
        postBody =
          """
            |{
            |  "x": 3,
            |  "y": 4
            |}
          """.stripMargin,
        andExpect = Ok,
        withJsonBody =
          s"""
             |${toJson(response)}
            """.stripMargin
      )
    }
  }
}
