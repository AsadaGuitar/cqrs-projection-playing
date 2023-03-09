package com.github.asadaguitar.projection.userAccountEvent

import cats.effect.IO
import com.github.asadaguitar.projection.production.doobie.AdapterBase
import com.github.asadaguitar.projection.production.doobie.implicits._
import doobie.Read

class UserAccountEventTableReader extends AdapterBase {

  def read(implicit
      read: Read[UserAccountEvent]
  ): IO[List[UserAccountEvent]] = {
    sql"""|SELECT event_id, user_account_id, event_type, created_at, applied_at
          |FROM develop.user_account_event AS this
          |WHERE this.applied_at IS NULL
          |ORDER BY this.created_at ASC
          |""".stripMargin
      .query[UserAccountEvent]
      .to[List]
      .transact(xa)
  }
}

object UserAccountEventTableReader {
  def apply(): UserAccountEventTableReader = new UserAccountEventTableReader()
}
