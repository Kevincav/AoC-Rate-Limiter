package rate.limiter
package utils

import scala.concurrent.duration.*

sealed trait Status

object Status {
  case object SUCCESS extends Status

  case object READY extends Status

  case object SOLVED extends Status

  case object WAITING extends Status

  case object NO_RESULTS extends Status

  case object ERROR extends Status
}

case class Result(status: Status, timeRemaining: Option[FiniteDuration], answer: Option[Any], isSolved: Option[Boolean])

case class DatabaseDetails(tableName: String = "rate_limiter",
                           databaseName: String = "jdbc:sqlite:rate_limiter.db",
                           timeRemaining: FiniteDuration = sys.env.getOrElse("AOC_SUBMISSION_THROTTLE", "5").toInt.minutes.fromNow.time)
