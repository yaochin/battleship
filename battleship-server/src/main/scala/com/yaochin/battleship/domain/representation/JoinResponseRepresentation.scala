package com.yaochin.battleship.domain.representation

import io.swagger.annotations.ApiModelProperty

/**
  * Created on 2/4/17.
  */
case class JoinResponseRepresentation(@ApiModelProperty(required = true)
                                      sessionId: String,
                                      @ApiModelProperty(required = false, dataType = "String")
                                      opponentId: Option[String],
                                      @ApiModelProperty(required = true)
                                      joinRequest: JoinRequestRepresentation)