package com.yaochin.battleship.injection

import java.util.concurrent.Executors

import com.google.inject.AbstractModule
import com.twitter.util.FuturePool
import com.yaochin.battleship.service._
import com.yaochin.battleship.util.IdGenerator

/**
  * Created on 2/5/17.
  */
class ServiceModule extends AbstractModule {
  override def configure() = {
    val executor = Executors.newFixedThreadPool(24)
    bind(classOf[FuturePool]).toInstance(FuturePool(executor))

    bind(classOf[GameMatrix]).toInstance(GameMatrix)

    bind(classOf[IdGenerator]).toInstance(IdGenerator)

    bind(classOf[UserService]).to(classOf[UserServiceImpl])
    bind(classOf[BattleService]).to(classOf[BattleServiceImpl])
  }
}
