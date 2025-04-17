package rate.limiter

import database.RateLimiterDAO
import utils.{DatabaseDetails, Result, Status}

import cats.effect.IO

import scala.concurrent.duration.*

case class RateLimiter(year: Int, day: Int)(implicit databaseDetails: DatabaseDetails = DatabaseDetails()) {
  def check(part: Int): IO[Result] =
      RateLimiterDAO().findByPrimaryKey(year, day, part).map {
        case Left(_) => Result(Status.ERROR, None, None, None)
        case Right(value) => value match {
          case None => Result(Status.NO_RESULTS, None, None, None)
          case Some(row) if row.isSolved => Result(Status.SOLVED, None, Some(row.answer), Some(row.isSolved))
          case Some(row) if Deadline(row.updateTime).hasTimeLeft() =>
            Result(Status.WAITING, Some(Deadline(row.updateTime).timeLeft), Some(row.answer), Some(row.isSolved))
          case _ => Result(Status.READY, None, None, None)
        }
      }

  def execute(part: Int)(codeBlock: => IO [(Any, Boolean)]): IO[Result] =
    RateLimiterDAO().createTable.flatMap { _ =>
      check(part).flatMap {
        case Result(status, deadline, answer, isSolved) if status != Status.READY && status != Status.NO_RESULTS =>
          IO.pure(Result(status, deadline, answer, isSolved))
        case _ => for {
          (answer, isSolved) <- codeBlock
          _ <- RateLimiterDAO().insertOrReplace(year, day, part, isSolved, answer, databaseDetails.timeRemaining)
        } yield Result(Status.SUCCESS, Some(Deadline(databaseDetails.timeRemaining).timeLeft), Some(answer), Some(isSolved))
      }
    }
}