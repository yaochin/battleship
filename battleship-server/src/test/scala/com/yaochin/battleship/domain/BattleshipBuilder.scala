package com.yaochin.battleship.domain


/**
  * Created on 2/6/17.
  */
case class BattleshipBuilder(locations: Map[Location, ShipLocationState.Value] = Map.empty){

  def withLocation(x: Int, y: Int) = {
    copy(locations = locations + (Location(x, y) -> ShipLocationState.Normal))
  }

  def build = Battleship(locations)

}
