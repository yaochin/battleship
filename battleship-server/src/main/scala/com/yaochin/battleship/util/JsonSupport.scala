package com.yaochin.battleship.util

import java.lang.reflect.{ParameterizedType => JParameterizedType, Type => JType}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.twitter.finatra.json.modules.FinatraJacksonModule

import scala.reflect.runtime.universe._

/**
  * Created on 1/31/17.
  */
object JsonSupport {
  val mapper = {
    val m = new ObjectMapper() with ScalaObjectMapper

    m.registerModule(DefaultScalaModule)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    FinatraJacksonModule.provideCamelCaseFinatraObjectMapper(m)
  }
}

trait JsonSupport {
  protected val mapper = JsonSupport.mapper


  def toJson[T](v: T): String = {
    mapper.writeValueAsString(v)
  }

  def fromJson[T](json: String)(implicit t: Manifest[T]) =  mapper.parse[T](json)


//  def fromJson[T](json: String)(implicit t: TypeTag[T]): T = mapper.readValue(json, typeReference[T])

//  private def typeReference[T](implicit m: TypeTag[_]) = new TypeReference[T] {
//    override def getType = jTypeFromType(m)
//  }
//
//  private def jTypeFromType(m: TypeTag[_]): JType = {
//    val tpe = m.tpe
//
//    if (tpe.typeArgs.isEmpty) {
//      m.mirror.runtimeClass(tpe)
//    }
//    else new JParameterizedType {
//      def getRawType = m.mirror.runtimeClass(tpe)
//      def getActualTypeArguments = tpe.resultType.typeArgs.map(m.mirror.runtimeClass(_)).toArray
//      def getOwnerType = m.mirror.runtimeClass(tpe)
//    }
//
//  }
}

