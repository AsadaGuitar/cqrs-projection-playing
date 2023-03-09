package com.github.asadaguitar.projection.userAccountStatus

import com.github.asadaguitar.projection.{CodecError, UnsafetyCodec, UserAccountId}
import doobie.Write

case class UserAccountStatus(
    userAccountId: UserAccountId,
    status: AccountStatus,
    createdAt: java.sql.Timestamp,
    closedAt: Option[java.sql.Timestamp]
)
object UserAccountStatus {
  import AccountStatus.codec._
  import com.github.asadaguitar.projection.production.doobie.implicits._

  implicit val userAccountEventWrite: Write[UserAccountStatus] = {
    Write[(String, Int, java.sql.Timestamp, Option[java.sql.Timestamp])]
      .contramap { status =>
        (
          status.userAccountId.value,
          UnsafetyCodec[AccountStatus, Int].fromLeft(status.status),
          status.createdAt,
          status.closedAt
        )
      }
  }
}

sealed trait AccountStatus
object AccountStatus {
  case object Edit extends AccountStatus
  case object Fixed extends AccountStatus
  case object Closed extends AccountStatus

  object codec {
    implicit val accountStatusIntCodec: UnsafetyCodec[AccountStatus, Int] =
      UnsafetyCodec.create[AccountStatus, Int] {
        case Edit => 1
        case Fixed => 2
        case Closed => 3
        case ow =>
          throw CodecError(s"PUT_ERROR: account_status。account_status=${ow}")
      }

    implicit val intAccountStatusCodec: UnsafetyCodec[Int, AccountStatus] =
      UnsafetyCodec.create[Int, AccountStatus] {
        case 1 => AccountStatus.Edit
        case 2 => AccountStatus.Fixed
        case 3 => AccountStatus.Closed
        case ow =>
          throw CodecError(s"GET_ERROR: account_status。account_status=${ow}")
      }
  }
}
