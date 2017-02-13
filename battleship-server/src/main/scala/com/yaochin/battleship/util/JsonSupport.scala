package com.yaochin.battleship.util

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.twitter.finatra.json.modules.FinatraJacksonModule

/**
  * Created on 1/31/17.
  */
object JsonSupport {
  val mapper = {
    val m = new ObjectMapper() with ScalaObjectMapper

    m.registerModule(DefaultScalaModule)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setSerializationInclusion(Include.NON_ABSENT)

    FinatraJacksonModule.provideCamelCaseFinatraObjectMapper(m)
  }
}

trait JsonSupport {
  protected val mapper = JsonSupport.mapper


  def toJson[T](v: T): String = {
    mapper.writeValueAsString(v)
  }

  def fromJson[T](json: String)(implicit t: Manifest[T]) =  mapper.parse[T](json)
}

