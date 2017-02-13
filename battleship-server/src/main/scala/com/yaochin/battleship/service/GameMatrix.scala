package com.yaochin.battleship.service

import com.yaochin.battleship.domain.User
import com.yaochin.battleship.util.ReadWriteLockHelper

import scala.collection.mutable

/**
  * Created on 1/31/17.
  */
trait GameMatrix extends ReadWriteLockHelper{

  private val users = mutable.Map[String, User]()

  def get(userId: String): Option[User] = {
    users.get(userId)
  }

  def nextAvailableUser: Option[User] = {
    users.values.find(_.opponentId.isEmpty)
  }

  def remove(userId: String): Unit = {
    users -= userId
  }

  def addOrUpdate(user: User): Unit = {
    users += (user.id -> user)
  }

  def list: Seq[User] = {
    users.values.toSeq
  }

}

object GameMatrix extends GameMatrix
