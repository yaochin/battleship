package com.yaochin.battleship.domain.representation

/**
  * Created on 2/6/17.
  */

case class JoinRequestRepresentation(battleships: List[BattleshipRepresentation])

case class BattleshipRepresentation(locations: List[LocationRepresentation])

case class LocationRepresentation(x: Int, y: Int)