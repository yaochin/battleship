package com.yaochin.battleship.domain.api

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.yaochin.battleship.domain.AttackResult.AttackResult
import com.yaochin.battleship.domain.AttackResultType
import io.swagger.annotations.ApiModelProperty

/**
  * Created on 2/4/17.
  */
case class AttackRequest(@ApiModelProperty(required = true)
                         x: Int,
                         @ApiModelProperty(required = true)
                         y: Int)

case class AttackResponse(@ApiModelProperty(required = true)
                          @JsonScalaEnumeration(classOf[AttackResultType])
                          result: AttackResult,
                          @ApiModelProperty(required = true)
                          self: UserDetailsResponse,
                          @ApiModelProperty(required = true)
                          opponent: UserDetailsResponse)

