package com.yaochin.battleship

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.github.xiaodongw.swagger.finatra.{SwaggerController, WebjarsController}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.json.modules.FinatraJacksonModule
import com.twitter.finatra.json.utils.CamelCasePropertyNamingStrategy
import com.yaochin.battleship.controller.{BattlefieldController, GameSessionController}
import com.yaochin.battleship.injection.ServiceModule
import io.swagger.models.{Info, Swagger}

/**
  * Created on 1/31/17.
  */
object BattleshipApp extends HttpServer{
  val info = new Info()
    .description("Battleship backend API")
    .version("0.0.1")
    .title("Battleship Service")

  BattleshipSwagger
    .info(info)

  override val defaultFinatraHttpPort: String = ":1234"

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add[WebjarsController]
      .add(new SwaggerController(swagger = BattleshipSwagger))
      .add[GameSessionController]
      .add[BattlefieldController]
  }


  override protected def modules = Seq(
    new ServiceModule()
  )

  override protected def jacksonModule = new FinatraJacksonModule{
    override protected val propertyNamingStrategy: PropertyNamingStrategy = CamelCasePropertyNamingStrategy
  }
}

object BattleshipSwagger extends Swagger
