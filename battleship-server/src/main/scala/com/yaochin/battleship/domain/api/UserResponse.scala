package com.yaochin.battleship.domain.api

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.yaochin.battleship.domain.UserState.UserState
import com.yaochin.battleship.domain.{User, UserStateType}
import io.swagger.annotations.ApiModelProperty

/**
  * Created on 2/6/17.
  */
case class UserResponses(@ApiModelProperty(required = true)
                         users: Seq[UserResponse])

case class UserResponse(@ApiModelProperty(required = true)
                        userId: String,
                        @ApiModelProperty(required = false, dataType = "string")
                        opponentId: Option[String],
                        @JsonScalaEnumeration(classOf[UserStateType])
                        @ApiModelProperty(required = true, dataType = "string")
                        state: UserState)

object UserResponse {
  def apply(user: User): UserResponse = {
    UserResponse(userId = user.id,
      opponentId = user.opponentId,
      state = user.state)
  }
}

case class UserDetailsResponse(@ApiModelProperty(required = true)
                               userId: String,
                               @ApiModelProperty(required = false, dataType = "string")
                               opponentId: Option[String],
                               @JsonScalaEnumeration(classOf[UserStateType])
                               @ApiModelProperty(required = true, dataType = "string")
                               state: UserState,
                               @ApiModelProperty(required = true)
                               battleships: Seq[BattleshipResponse],
                               @ApiModelProperty(required = true)
                               events: List[EventResponse])

object UserDetailsResponse {
  def apply(user: User): UserDetailsResponse = {
    UserDetailsResponse(userId = user.id,
      opponentId = user.opponentId,
      state = user.state,
      battleships = user.fleet.map(BattleshipResponse(_)),
      events = user.events.map { e => EventResponse(e.getClass.getSimpleName, e.details) }
    )
  }
}

case class EventResponse(@ApiModelProperty(required = true, name = "type")
                         `type`: String,
                         @ApiModelProperty(required = true)
                         details: Any)