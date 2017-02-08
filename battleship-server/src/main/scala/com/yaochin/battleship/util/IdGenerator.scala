package com.yaochin.battleship.util

import java.util.UUID

/**
  * Created on 2/5/17.
  */
trait IdGenerator {

  def next: String = {
    UUID.randomUUID().toString.replaceAll("-", "").substring(12)
  }

}

object IdGenerator extends IdGenerator
