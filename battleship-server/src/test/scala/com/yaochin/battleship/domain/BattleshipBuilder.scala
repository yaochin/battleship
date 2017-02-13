package com.yaochin.battleship.domain

import com.yaochin.battleship.domain.ShipLocationState.ShipLocationState


/**
  * Created on 2/6/17.
  */
case class BattleshipBuilder(locations: Map[Location, ShipLocationState.Value] = Map.empty){

  def withLocation(x: Int, y: Int) = {
    copy(locations = locations + (Location(x, y) -> ShipLocationState.Normal))
  }

  def withLocation(x: Int, y: Int, state: ShipLocationState) = {
    copy(locations = locations + (Location(x, y) -> state))
  }

  def build = Battleship(locations)

}
