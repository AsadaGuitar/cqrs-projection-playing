package com.github.asadaguitar.projection.production.doobie

import cats.effect.IO
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux

trait AdapterBase {

  protected val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:chat_app",
    "developer",
    "passw0rd"
  )
}
