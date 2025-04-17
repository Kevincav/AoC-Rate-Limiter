package rate.limiter.utils

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.*

class ResultTest extends AnyFunSuite with Matchers {
  test("Status Classes") {
    assert(Status.SUCCESS.toString == "SUCCESS")
    assert(Status.READY.toString == "READY")
    assert(Status.SOLVED.toString == "SOLVED")
    assert(Status.WAITING.toString == "WAITING")
    assert(Status.NO_RESULTS.toString == "NO_RESULTS")
    assert(Status.ERROR.toString == "ERROR")
  }

  test("Result builds properly with values") {
    val result = Result(Status.SUCCESS, None, None, None)
    assert(result.status == Status.SUCCESS)
    assert(result.isSolved.isEmpty)
    assert(result.answer.isEmpty)
    assert(result.timeRemaining.isEmpty)
  }

  test("Test DatabaseDetails class returns valid default values") {
    val databaseDetails = DatabaseDetails()
    assert(databaseDetails.tableName == "rate_limiter")
    assert(databaseDetails.databaseName == "jdbc:sqlite:rate_limiter.db")
    assert(databaseDetails.timeRemaining.toMinutes == 5.minutes.fromNow.time.toMinutes)
  }
}
