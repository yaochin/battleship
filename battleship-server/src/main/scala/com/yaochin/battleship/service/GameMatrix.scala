package com.yaochin.battleship.service

import com.yaochin.battleship.domain.User
import com.yaochin.battleship.util.ReadWriteLockHelper

import scala.collection.mutable

/**
  * Created on 1/31/17.
  */
trait GameMatrix extends ReadWriteLockHelper{

  private val userSessions = mutable.Map[String, User]()

  def get(userId: String): Option[User] = {
    userSessions.get(userId)
  }

  def nextAvailableSession: Option[User] = {
    userSessions.values.find(_.opponentId.isEmpty)
  }

  def remove(userId: String): Unit = {
    userSessions -= userId
  }

  def addOrUpdate(userSession: User): Unit = {
    userSessions += (userSession.id -> userSession)
  }

  def list: Seq[User] = {
    userSessions.values.toSeq
  }

}

object GameMatrix extends GameMatrix
