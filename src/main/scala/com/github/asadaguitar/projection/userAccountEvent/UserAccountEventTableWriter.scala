package com.github.asadaguitar.projection.userAccountEvent

import cats.effect.IO
import com.github.asadaguitar.projection.production.doobie.AdapterBase
import doobie.{Update, Write}

class UserAccountEventTableWriter extends AdapterBase {
  import com.github.asadaguitar.projection.production.doobie.implicits._

  def write(
      events: Seq[UserAccountEvent]
  )(implicit write: Write[UserAccountEvent]): IO[Unit] = {
    Update[UserAccountEvent](
      """INSERT INTO develop.user_account_event (event_id, user_account_id, event_type, created_at, applied_at)
        |VALUES (?, ?, ?, ?, ?)
        |""".stripMargin
    )
      .updateMany(events)
      .transact(xa)
      .void
  }
}

object UserAccountEventTableWriter {
  def apply() = new UserAccountEventTableWriter
}
