package rate.limiter

import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import utils.{DatabaseDetails, Status}

import cats.effect.IO

import java.nio.file.Files
import scala.concurrent.duration.*

def isSolved(isSolved: Any, part: Int): IO[Boolean] = IO.pure(isSolved.toString == "true")

class RateLimiterTest extends AnyFunSuite with Matchers {
  test("Test executing first and second time < 5 min") {
    val databasePath = Files.createTempFile("sqlite.database.", ".db")
    implicit val databaseDetails: DatabaseDetails = DatabaseDetails("unit_test", s"jdbc:sqlite:$databasePath")

    val result = RateLimiter(2000, 1).execute(false, 1)(isSolved).unsafeRunSync()
    assert(result.status == Status.SUCCESS)
    assert(!result.isSolved.get)
    assert(result.answer.get == false)
    assert(result.timeRemaining.get.toMinutes == 5.minutes.fromNow.timeLeft.toMinutes)

    val result2 = RateLimiter(2000, 1).execute(false, 1)(isSolved).unsafeRunSync()
    assert(result2.status == Status.WAITING)
    assert(result2.timeRemaining.get.toMinutes == 5.minutes.fromNow.timeLeft.toMinutes)

    Files.deleteIfExists(databasePath)
  }

  test("Test executing first and second with solved answer") {
    val databasePath = Files.createTempFile("sqlite.database.", ".db")
    implicit val databaseDetails: DatabaseDetails = DatabaseDetails("unit_test", s"jdbc:sqlite:$databasePath")

    val result = RateLimiter(2000, 1).execute(true, 1)(isSolved).unsafeRunSync()
    assert(result.status == Status.SUCCESS)
    assert(result.isSolved.get)
    assert(result.answer.get == true)
    assert(result.timeRemaining.get.toMinutes == 5.minutes.fromNow.timeLeft.toMinutes)

    val result2 = RateLimiter(2000, 1).execute(false, 1)(isSolved).unsafeRunSync()
    assert(result2.status == Status.SOLVED)
    assert(result.isSolved.get)
    assert(result.answer.get == true)
    assert(result2.timeRemaining.isEmpty)

    Files.deleteIfExists(databasePath)
  }

  test("Test executing twice after duration passed") {
    val databasePath = Files.createTempFile("sqlite.database.", ".db")
    implicit val databaseDetails: DatabaseDetails = DatabaseDetails("unit_test", s"jdbc:sqlite:$databasePath", 0.minutes.fromNow.time)

    val result = RateLimiter(2000, 1).execute(false, 1)(isSolved).unsafeRunSync()
    assert(result.status == Status.SUCCESS)
    assert(!result.isSolved.get)
    assert(result.answer.get == false)
    assert(result.timeRemaining.get.toMinutes == 0.minutes.fromNow.timeLeft.toMinutes)

    val result2 = RateLimiter(2000, 1).execute(true, 1)(isSolved).unsafeRunSync()
    assert(result2.status == Status.SUCCESS)
    assert(result2.isSolved.get)
    assert(result2.answer.get == true)
    assert(result2.timeRemaining.get.toMinutes == 0.minutes.fromNow.timeLeft.toMinutes)

    Files.deleteIfExists(databasePath)
  }

  test("Check for non ready to go values") {
    val databasePath = Files.createTempFile("sqlite.database.", ".db")
    implicit val databaseDetails: DatabaseDetails = DatabaseDetails("unit_test", s"jdbc:sqlite:$databasePath", 0.minutes.fromNow.time)

    val resultError = RateLimiter(2000, 1).check(1).unsafeRunSync()
    assert(resultError.status == Status.ERROR)

    val result = RateLimiter(2000, 1).execute(false, 2)(isSolved).unsafeRunSync()
    assert(RateLimiter(2000, 1).check(1).unsafeRunSync().status == Status.NO_RESULTS)
    assert(RateLimiter(2000, 1).check(2).unsafeRunSync().status == Status.READY)

    Files.deleteIfExists(databasePath)
  }

  test("Check for ready to go values") {
    val databasePath = Files.createTempFile("sqlite.database.", ".db")
    implicit val databaseDetails: DatabaseDetails = DatabaseDetails("unit_test", s"jdbc:sqlite:$databasePath")

    RateLimiter(2000, 1).execute(false, 1)(isSolved).unsafeRunSync()
    RateLimiter(2000, 1).execute(true, 2)(isSolved).unsafeRunSync()

    assert(RateLimiter(2000, 1).check(1).unsafeRunSync().status == Status.WAITING)
    assert(RateLimiter(2000, 1).check(2).unsafeRunSync().status == Status.SOLVED)

    Files.deleteIfExists(databasePath)
  }
}
