package com.yaochin.battleship

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.github.xiaodongw.swagger.finatra.{SwaggerController, WebjarsController}
import javax.inject.{Inject, Singleton}
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.exceptions.ExceptionMapper
import com.twitter.finatra.http.filters.ExceptionMappingFilter
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.json.modules.FinatraJacksonModule
import com.twitter.finatra.json.utils.CamelCasePropertyNamingStrategy
import com.yaochin.battleship.controller.{BattlefieldController, GameSessionController}
import com.yaochin.battleship.domain.api.ErrorResponse
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
    router.filter[ExceptionMappingFilter[Request]]
      .exceptionMapper[IllegalArgumentExceptionMapper]
      .add[WebjarsController]
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

@Singleton
class IllegalArgumentExceptionMapper @Inject()(response: ResponseBuilder)
  extends ExceptionMapper[IllegalArgumentException] {

  override def toResponse(request: Request, e: IllegalArgumentException): Response = {
    response.status(422).json(ErrorResponse("Unprocessable Entity", e.getMessage))
  }
}

@Singleton
class IllegalStateExceptionMapper @Inject()(response: ResponseBuilder)
  extends ExceptionMapper[IllegalStateException] {

  override def toResponse(request: Request, e: IllegalStateException): Response = {
    response.internalServerError.json(ErrorResponse("Internal Server Error", e.getMessage))
  }
}