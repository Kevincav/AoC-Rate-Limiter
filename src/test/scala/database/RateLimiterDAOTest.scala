package org.rate.limiter.database

import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.rate.limiter.utils.DatabaseDetails
import org.sqlite.SQLiteException

import java.nio.file.Files
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

class RateLimiterDAOTest extends AnyFunSuite with Matchers {
  test("Test create and delete a new database") {
    val databasePath = Files.createTempFile("sqlite.database.", ".db")
    implicit val databaseDetails: DatabaseDetails = DatabaseDetails("unit_test", s"jdbc:sqlite:$databasePath")

    assert(RateLimiterDAO().findByPrimaryKey(2000, 1, 1).unsafeRunSync().isLeft)
    RateLimiterDAO().createTable.unsafeRunSync()
    assert(RateLimiterDAO().findByPrimaryKey(2000, 1, 1).unsafeRunSync().isRight)
    RateLimiterDAO().dropTable.unsafeRunSync()
    assert(RateLimiterDAO().findByPrimaryKey(2000, 1, 1).unsafeRunSync().isLeft)

    Files.deleteIfExists(databasePath)
  }

  test("Adding and finding elements into the database") {
    val databasePath = Files.createTempFile("sqlite.database.", ".db")
    implicit val databaseDetails: DatabaseDetails = DatabaseDetails("unit_test", s"jdbc:sqlite:$databasePath")

    assert(RateLimiterDAO().insertOrReplace(2000, 1, 1, true, 1, FiniteDuration(1000, TimeUnit.MINUTES)).unsafeRunSync().isLeft)
    RateLimiterDAO().createTable.unsafeRunSync()
    assert(RateLimiterDAO().findByPrimaryKey(2000, 1, 1).unsafeRunSync().isRight)
    assert(RateLimiterDAO().insertOrReplace(2000, 1, 1, true, 1, FiniteDuration(1000, TimeUnit.MINUTES)).unsafeRunSync().isRight)
    val result = RateLimiterDAO().findByPrimaryKey(2000, 1, 1).unsafeRunSync()

    result.map { output =>
      assert(output.isDefined)
      assert(output.get.year == 2000)
      assert(output.get.day == 1)
      assert(output.get.part == 1)
      assert(output.get.isSolved)
      assert(output.get.answer == 1)
      assert(output.get.updateTime == FiniteDuration(1000, TimeUnit.MINUTES))
    }

    RateLimiterDAO().dropTable.unsafeRunSync()
    Files.deleteIfExists(databasePath)
  }
}
