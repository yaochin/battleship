package com.yaochin.battleship.controller

import javax.inject.Inject

import com.github.xiaodongw.swagger.finatra.SwaggerSupport
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.yaochin.battleship.BattleshipSwagger
import com.yaochin.battleship.domain.representation.AttackRequestRepresentation
import com.yaochin.battleship.service.BattlefieldService
import com.yaochin.battleship.util.JsonSupport

/**
  * Created on 2/4/17.
  */
class BattlefieldController @Inject()(service: BattlefieldService) extends Controller with SwaggerSupport with JsonSupport{
  implicit protected val swagger = BattleshipSwagger

  postWithDoc("/battleship/sessions/:sessionId/attack") { o =>
    o.summary("Fire an attack")
      .tag("Battle Field")
      .routeParam[String]("sessionId")
      .bodyParam[AttackRequestRepresentation]("AttackRequest")
      .responseWith(200, "")
  } { request: Request =>
    val sessionId = request.getParam("sessionId")
    val attackRequest = fromJson[AttackRequestRepresentation](request.contentString)
    service.attack(sessionId, attackRequest).map{
      response.ok.body(_)
    }

  }

}
