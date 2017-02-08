package com.yaochin.battleship.domain.representation

import com.yaochin.battleship.domain.UserState.UserState
import io.swagger.annotations.ApiModelProperty

/**
  * Created on 2/6/17.
  */
case class UserSessionsRepresentation(@ApiModelProperty(required = true)
                                      sessions: Seq[UserSessionRepresentation])

case class UserSessionRepresentation(@ApiModelProperty(required = true)
                                     userId: String,
                                     opponentId: Option[String],
                                     state: UserState)
