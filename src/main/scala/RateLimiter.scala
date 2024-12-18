package org.rate.limiter.service

import cats.effect.IO
import org.rate.limiter.database.RateLimiterDAO
import org.rate.limiter.utils.{Result, Status}

import scala.concurrent.duration.*

case class RateLimiter(year: Int, day: Int) {
  def check(part: Int): IO[Result] =
    RateLimiterDAO().findByPrimaryKey(year, day, part).map {
      case None => Result(Status.READY, None, None, None)
      case Some(row) if row.isSolved => Result(Status.SOLVED, None, Some(row.answer), Some(row.isSolved))
      case Some(row) if Deadline(row.updateTime).hasTimeLeft() =>
        Result(Status.WAITING, Some(Deadline(row.updateTime).timeLeft), Some(row.answer), Some(row.isSolved))
      case _ => Result(Status.NO_RESULTS, None, None, None)
    }

  def execute(part: Int)(codeBlock: => (Long, Boolean)): IO[Result] = RateLimiterDAO().createTable.flatMap { _ =>
    check(part).flatMap {
      case Result(status, deadline, answer, isSolved) if status != Status.READY && status != Status.NO_RESULTS =>
        IO.pure(Result(status, deadline, answer, isSolved))
      case _ => for {
        duration <- IO.pure(sys.env.getOrElse("AOC_SUBMISSION_THROTTLE", "5").toInt.minutes.fromNow.time)
        (answer, isSolved) <- IO.pure(codeBlock)
        _ <- RateLimiterDAO().insertOrReplace(year, day, part, isSolved, answer, duration)
      } yield Result(Status.SUCCESS, Some(duration), Some(answer), Some(isSolved))
    }
  }
}