package com.yaochin.battleship.controller

import javax.inject.Inject

import com.github.xiaodongw.swagger.finatra.SwaggerSupport
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.yaochin.battleship.BattleshipSwagger
import com.yaochin.battleship.domain.api._
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

  postWithDoc("/battleship/users/") { o =>
    o.summary("Enter a battleship game")
      .tag("Game Session")
      .bodyParam[JoinRequest]("JoinRequestRepresentation")
      .responseWith[JoinResponse](200, "Join a new game")
  } { req: Request =>
    val request =  fromJson[JoinRequest](req.contentString)

    service.join(request).map(response.ok.json _)
  }

  getWithDoc("/battleship/users") { o =>
    o.summary("Return the current waiting list")
      .tag("Game Session")
      .responseWith[UserResponses](200, "List of users")
  } { req: Request =>
    service.listUsers.map(response.ok.json _)
  }

  getWithDoc("/battleship/users/:userId") { o =>
    o.summary("Get the user info")
      .tag("Game Session")
      .routeParam[String]("userId")
      .responseWith[UserDetailsResponse](200, "User")
      .responseWith(404, "Not found")
  } { req: Request =>
    val userId = req.getParam("userId")
    service.get(userId).map{
      case Some(u) => response.ok.json(u)
      case None => response.notFound
    }
  }
}
