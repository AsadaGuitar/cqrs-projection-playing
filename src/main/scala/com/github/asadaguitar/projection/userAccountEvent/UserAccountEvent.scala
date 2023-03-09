package com.github.asadaguitar.projection.userAccountEvent

import cats.effect.IO
import cats.syntax.option._
import com.github.asadaguitar.projection._
import doobie.{Read, Write}

case class UserAccountEvent(
    eventId: EventId,
    userAccountId: UserAccountId,
    eventType: EventType,
    createdAt: java.sql.Timestamp,
    appliedAt: Option[java.sql.Timestamp]
) extends Event {
  def isApplied: Boolean = appliedAt.isDefined
}

object UserAccountEvent {
  import EventType.codec._
  import com.github.asadaguitar.projection.production.doobie.implicits._

  def create(
      userAccountId: UserAccountId,
      eventType: EventType
  ): IO[UserAccountEvent] = {
    for {
      uuid <- IO.randomUUID
      now <- IO.realTimeInstant
    } yield UserAccountEvent(
      eventId = EventId(uuid.toString),
      userAccountId = userAccountId,
      eventType = eventType,
      createdAt = java.sql.Timestamp.from(now),
      appliedAt = none[java.sql.Timestamp]
    )
  }

  implicit val userAccountEventRead: Read[UserAccountEvent] = {
    Read[(String, String, Int, java.sql.Timestamp, Option[java.sql.Timestamp])]
      .map { case (eid: String, uid, etype, created_at, applied_at) =>
        UserAccountEvent(
          EventId(eid),
          UserAccountId(uid),
          UnsafetyCodec[Int, EventType].fromLeft(etype),
          created_at,
          applied_at
        )
      }
  }

  implicit val userAccountEventWrite: Write[UserAccountEvent] = {
    Write[(String, String, Int, java.sql.Timestamp, Option[java.sql.Timestamp])]
      .contramap { event =>
        (
          event.eventId.value,
          event.userAccountId.value,
          UnsafetyCodec[EventType, Int].fromLeft(event.eventType),
          event.createdAt,
          event.appliedAt
        )
      }
  }
}

sealed trait EventType
object EventType {
  case object CreateEvent extends EventType
  case object UpdateEvent extends EventType
  case object CloseEvent extends EventType

  object codec {
    implicit val eventTypeIntCodec: UnsafetyCodec[EventType, Int] =
      UnsafetyCodec.create[EventType, Int] {
        case EventType.CreateEvent => 1
        case EventType.UpdateEvent => 2
        case EventType.CloseEvent => 3
        case ow =>
          throw CodecError(s"PUT_ERROR: event_typeが不正です。event_type=${ow}")
      }

    implicit val intEventTypeCodec: UnsafetyCodec[Int, EventType] =
      UnsafetyCodec.create[Int, EventType] {
        case 1 => EventType.CreateEvent
        case 2 => EventType.UpdateEvent
        case 3 => EventType.CloseEvent
        case ow =>
          throw CodecError(s"GET_ERROR: event_typeが不正です。event_type=${ow}")
      }
  }
}
