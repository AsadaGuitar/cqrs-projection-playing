package com.github.asadaguitar.projection.userAccountEvent

import cats.arrow.FunctionK
import cats.data.NonEmptyList
import cats.effect._
import cats.syntax.traverse._
import cats.syntax.list._
import cats.~>
import com.github.asadaguitar.projection.production.doobie.AdapterBase
import com.github.asadaguitar.projection.userAccountStatus.AccountStatus
import com.github.asadaguitar.projection.{
  EventId,
  ProjectionHandler,
  UnsafetyCodec,
  UserAccountId
}
import doobie._
import cats.syntax.apply._
import cats.syntax.functor._

class UserAccountEventProjectionHandler
    extends ProjectionHandler[UserAccountEvent]
    with AdapterBase {

  import com.github.asadaguitar.projection.production.doobie.implicits._
  import AccountStatus.codec._
  import UserAccountEvent._
  import com.github.asadaguitar.projection.userAccountStatus.UserAccountStatus._

  override def run(events: List[UserAccountEvent]): IO[Unit] = {
    handleEvent(events).flatMap(_.transact(xa))
  }

  private def handleEvent(
      events: List[UserAccountEvent]
  ) = {
    for {
      h <- events.traverse { event =>
        IO.println(
          s"event_id=${event.eventId.value}, user_account_id=${event}, event_type=${event.eventType}"
        ) *>
          IO.realTimeInstant
            .map(java.sql.Timestamp.from)
            .map { ts =>
              event.eventType match {
                case EventType.CreateEvent =>
                  create(event.userAccountId, ts)
                //          case EventType.UpdateEvent => ???
                case EventType.CloseEvent =>
                  close(event.userAccountId, ts)
              }
            }
      }
      d <- IO.realTimeInstant
        .map(java.sql.Timestamp.from)
        .map { ts =>
          events.toNel match {
            case Some(nel) => deleteEvent(nel.map(_.eventId), ts)
            case None => WeakAsyncConnectionIO.unit
          }
        }
    } yield h.sequence *> d
  }

  private def deleteEvent(
      eventIds: NonEmptyList[EventId],
      timestamp: java.sql.Timestamp
  ) = {
    sql"""|UPDATE develop.user_account_event
          |SET applied_at = ${timestamp}
          |WHERE ${Fragments.in(fr"event_id", eventIds.map(_.value))}
          |""".stripMargin.update.run.void
  }

  private def create(
      userAccountId: UserAccountId,
      timestamp: java.sql.Timestamp
  ) = {
    val insertInfoQuery =
      sql"""|INSERT INTO develop.user_account_info (user_account_id, created_at)
          |VALUES (${userAccountId.value}, ${timestamp})
          |""".stripMargin.update.run

    val insertStatusQuery =
      sql"""|INSERT INTO develop.user_account_status (user_account_id, account_status, created_at, closed_at)
            |VALUES (${userAccountId.value}, ${UnsafetyCodec[AccountStatus, Int]
        .fromLeft(AccountStatus.Edit)}, ${timestamp}, NULL)
            |""".stripMargin.update.run

    for {
      _ <- insertInfoQuery
      _ <- insertStatusQuery
    } yield ()
  }

  private def close(
      userAccountId: UserAccountId,
      timestamp: java.sql.Timestamp
  ) = {

    val closeExistStatusQuery =
      sql"""|UPDATE develop.user_account_status
            |SET closed_at = ${timestamp}
            |WHERE user_account_id = ${userAccountId.value}
            |""".stripMargin.update.run

    val insertStatusQuery =
      sql"""|INSERT INTO develop.user_account_status (user_account_id, account_status, created_at, closed_at)
            |VALUES (${userAccountId.value}, ${UnsafetyCodec[AccountStatus, Int]
        .fromLeft(AccountStatus.Closed)}, ${timestamp}, NULL)
            |""".stripMargin.update.run

    for {
      _ <- closeExistStatusQuery
      _ <- insertStatusQuery
    } yield ()
  }

}

object UserAccountEventProjectionHandler {
  def apply() = new UserAccountEventProjectionHandler
}
