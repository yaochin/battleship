package com.yaochin.battleship.domain.api

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.yaochin.battleship.domain.ShipLocationState.ShipLocationState
import com.yaochin.battleship.domain.{Battleship, ShipLocationStateType}
import io.swagger.annotations.ApiModelProperty

/**
  * Created on 2/6/17.
  */
case class JoinRequest(battleships: Seq[BattleshipRequest])

case class BattleshipRequest(locations: Seq[LocationRequest])

case class BattleshipResponse(locations: Seq[LocationAndStateResponse])

object BattleshipResponse {
  def apply(ship: Battleship): BattleshipResponse = {
    val locations = ship.locationAndStateMap.map{ case (loc, state) =>
      LocationAndStateResponse(loc.x, loc.y, Some(state))
    }.toSeq
    BattleshipResponse(locations)
  }
}

case class LocationRequest(@ApiModelProperty(required = true)
                           x: Int,
                           @ApiModelProperty(required = true)
                           y: Int)

case class LocationAndStateResponse(@ApiModelProperty(required = true)
                                    x: Int,
                                    @ApiModelProperty(required = true)
                                    y: Int,
                                    @JsonScalaEnumeration(classOf[ShipLocationStateType])
                                    @ApiModelProperty(required = true, dataType = "string")
                                    state: Option[ShipLocationState])

case class JoinResponse(@ApiModelProperty(required = true)
                        userId: String,
                        @ApiModelProperty(required = false, dataType = "String")
                        opponentId: Option[String],
                        @ApiModelProperty(required = true)
                        joinRequest: JoinRequest)