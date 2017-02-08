package com.yaochin.battleship.controller

import javax.inject.Inject

import com.github.xiaodongw.swagger.finatra.SwaggerSupport
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.yaochin.battleship.BattleshipSwagger
import com.yaochin.battleship.domain.representation.{JoinResponseRepresentation, JoinRequestRepresentation, UserSessionsRepresentation}
import com.yaochin.battleship.service.GameSessionService
import com.yaochin.battleship.util.JsonSupport

/**
  * Created on 1/31/17.
  */
class GameSessionController @Inject()(service: GameSessionService)
  extends Controller
  with SwaggerSupport
  with JsonSupport {

  implicit protected val swagger = BattleshipSwagger

  postWithDoc("/battleship/join") { o =>
    o.summary("Enter a battleship game")
      .tag("Game Session")
      .bodyParam[JoinRequestRepresentation]("JoinRequestRepresentation")
      .responseWith[JoinResponseRepresentation](200, "Join a new game")
  } { req: Request =>
    val request =  fromJson[JoinRequestRepresentation](req.contentString)

    service.join(request).map(response.ok.json _)
  }

  getWithDoc("/battleship/list_queue") { o =>
    o.summary("Return the current waiting list")
      .tag("Game Session")
      .responseWith[UserSessionsRepresentation](200, "Current waiting list")
  } { req: Request =>
    service.listUserSessions.map(response.ok.json _)
  }
}
