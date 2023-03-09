package com.github.asadaguitar.projection

import cats.effect.{IO, IOApp}
import cats.syntax.traverse._
import com.github.asadaguitar.projection.userAccountEvent.{EventType, UserAccountEvent, UserAccountEventProjectionHandler, UserAccountEventTableReader, UserAccountEventTableWriter}

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

object Main extends IOApp.Simple {

  private val sleepTime = 1000
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    .withZone(ZoneId.systemDefault())

  override def run: IO[Unit] = {
    import com.github.asadaguitar.projection.userAccountEvent.UserAccountEvent._

    val project = UserAccountEventTableReader().read
      .flatMap {
        case Nil => IO.println("doesn't exists new events.")
        case l   => UserAccountEventProjectionHandler().run(l)
      }

    val app = for {
      now <- IO.realTimeInstant
      _ <- IO.println(s"--- interval [${formatter.format(now)}] ---")
      _ <- project
      _ <- IO.sleep(sleepTime.millis)
    } yield ()

    app.foreverM
  }
}

object Sub extends IOApp.Simple {
  override def run: IO[Unit] = {

    val accountSet = Seq(
      (UserAccountId("U0001"), EventType.CreateEvent),
      (UserAccountId("U0002"), EventType.CreateEvent),
      (UserAccountId("U0003"), EventType.CreateEvent),
      (UserAccountId("U0004"), EventType.CreateEvent),
      (UserAccountId("U0005"), EventType.CreateEvent),
      (UserAccountId("U0001"), EventType.CloseEvent)
    )

    accountSet
      .traverse(set => UserAccountEvent.create(set._1, set._2))
      .flatMap(UserAccountEventTableWriter().write)
  }
}
