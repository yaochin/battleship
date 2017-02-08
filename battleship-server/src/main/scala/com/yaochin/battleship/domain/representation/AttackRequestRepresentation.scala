package com.yaochin.battleship.domain.representation

import com.yaochin.battleship.domain.Location
import io.swagger.annotations.ApiModelProperty

/**
  * Created on 2/4/17.
  */
case class AttackRequestRepresentation(@ApiModelProperty(required = true)
                                       opponentId: String,
                                       @ApiModelProperty(required = true)
                                       location: LocationRepresentation)

object AttackRequestRepresentation {
  implicit def loc2loc(rep: LocationRepresentation): Location = {
    new Location(rep.x, rep.y)
  }
}
