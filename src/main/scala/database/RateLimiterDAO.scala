package rate.limiter
package database

import cats.effect.IO
import utils.DatabaseDetails

import java.sql.{Connection, DriverManager, ResultSet}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.language.reflectiveCalls

case class Row(resultSet: ResultSet) {
  val year: Int = resultSet.getInt("year")
  val day: Int = resultSet.getInt("day")
  val part: Int = resultSet.getInt("part")
  val isSolved: Boolean = resultSet.getBoolean("isSolved")
  val answer: Long = resultSet.getLong("answer")
  val updateTime: FiniteDuration = FiniteDuration(resultSet.getLong("updateTime"), TimeUnit.NANOSECONDS)
}

case class RateLimiterDAO()(implicit databaseDetails: DatabaseDetails = DatabaseDetails()) {
  private def using[A <: {def close(): Unit}, B](resource: A)(f: A => B): B =
    try f(resource) finally if (resource != null) resource.close()

  private def getConnection: Connection = {
    Class.forName("org.sqlite.JDBC")
    DriverManager.getConnection(databaseDetails.databaseName)
  }

  def createTable: IO[Unit] = IO {
    using(getConnection.createStatement()) { ps =>
      ps.executeUpdate(
        s"""|CREATE TABLE IF NOT EXISTS ${databaseDetails.tableName} (
            |    year INTEGER NOT NULL,
            |    day INTEGER NOT NULL,
            |    part INTEGER NOT NULL,
            |    isSolved BOOLEAN NOT NULL,
            |    answer INTEGER,
            |    updateTime INTEGER,
            |    PRIMARY KEY (year, day, part)
            |)""".stripMargin)
    }
  }

  def dropTable: IO[Unit] = IO {
    using(getConnection.createStatement()) { ps =>
      ps.executeUpdate(s"""|DROP TABLE IF EXISTS ${databaseDetails.tableName}""".stripMargin)
    }
  }

  def findByPrimaryKey(year: Int, day: Int, part: Int): IO[Either[Throwable, Option[Row]]] =
    IO {
      try {
        using(getConnection.prepareStatement(
          s"""|SELECT year,
              |       day,
              |       part,
              |       isSolved,
              |       answer,
              |       updateTime
              |FROM   ${databaseDetails.tableName}
              |WHERE  year = ?
              |       AND day = ?
              |       AND part = ?
              |""".stripMargin)) { ps => {
          ps.setInt(1, year)
          ps.setInt(2, day)
          ps.setInt(3, part)
          using(ps.executeQuery()) { rs => if (rs.next()) Right(Some(Row(rs))) else Right(None) }
        }}
      } catch { case ex: Throwable => Left(ex) }
    }

  def insertOrReplace(year: Int, day: Int, part: Int, isSolved: Boolean, answer: Any, updateTime: FiniteDuration): IO[Either[Throwable, Int]] =
    IO {
      try {
        using(getConnection.prepareStatement(
          s"""|INSERT OR REPLACE
              |INTO ${databaseDetails.tableName}
              |     (
              |            year,
              |            day,
              |            part,
              |            isSolved,
              |            answer,
              |            updateTime
              |     )
              |     VALUES (?, ?, ?, ?, ?, ?)""".stripMargin)) { ps => {
          ps.setInt(1, year)
          ps.setInt(2, day)
          ps.setInt(3, part)
          ps.setBoolean(4, isSolved)
          ps.setString(5, answer.toString)
          ps.setLong(6, updateTime.toNanos)
          Right(ps.executeUpdate())
        }}
      } catch { case ex: Throwable => Left(ex) }
  }
}
