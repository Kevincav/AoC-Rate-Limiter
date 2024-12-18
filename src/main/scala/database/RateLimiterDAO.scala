package org.loadbalancer
package database

import java.sql.ResultSet
import scala.language.reflectiveCalls

case class RateLimitRow(year: Int, day: Int, part: Int, isSolved: Boolean, answer: Long, creationTime: Timestamp, updateTime: Timestamp) {
  def this(resultSet: ResultSet) = this(
    year = resultSet.getInt("year"),
    day = resultSet.getInt("day"),
    part = resultSet.getInt("part"),
    isSolved = resultSet.getBoolean("isSolved"),
    answer = resultSet.getLong("answer"),
    creationTime = resultSet.getTimestamp("creationTime"),
    updateTime = resultSet.getTimestamp("updateTime"),
  )
}

case class sqlLite() {
  def getConnection: Connection = {
    Class.forName("org.sqlite.JDBC") // Load the JDBC driver
    DriverManager.getConnection(jdbcUrl)
  }

  def using[A <: {def close(): Unit}, B](resource: A)(f: A => B): B =
    try f(resource) finally if (resource != null) resource.close()
}

class RateLimiterDAO(conn: Connection) {
  // Database file path
  val database = "rate_limiter"
  val dbPath = "rate_limiter.db"
  val jdbcUrl = s"jdbc:sqlite:$dbPath"

  def createTable(): IO[Unit] =
    using(conn.executeStatement(
      """|CREATE TABLE rate_limiter (
         |    year INTEGER NOT NULL,
         |    day INTEGER NOT NULL,
         |    part INTEGER NOT NULL,
         |    isSolved BOOLEAN NOT NULL,
         |    answer INTEGER,
         |    creationTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updateTime TIMESTAMP,
         |    PRIMARY KEY (year, day, part)
          """.stripMargin))

  def findByPrimaryKey(year: Int, day: Int, part: Int, isSolved: Boolean): IO[Option[Row]] =
    using(conn.prepareStatement(
      """|SELECT year,
         |       day,
         |       part,
         |       isSolved,
         |       answer,
         |       creationTime,
         |       updateTime
         |FROM   rate_limiter
         |WHERE  year = ?
         |       AND day = ?
         |       AND part = ?
         |       AND isSolved = ?
         |""".stripMargin)) { ps => {
      ps.setInt(1, year)
      ps.setInt(2, day)
      ps.setInt(3, part)
      ps.setBoolean(4, isSolved)
      using(ps.executeQuery()) { rs =>
        if (rs.next()) Some(RateLimitRow(rs)) else None
      }
    }}

  def insertOrReplace(year: Int, day: Int, part: Int, isSolved: Boolean, answer: Long, updateTime: Timestamp): IO[Unit] =
    using(conn.prepareStatement(
      """|INSERT
         |or     REPLACE
         |into   rate_limiter
         |       (
         |              year,
         |              day,
         |              part,
         |              isSolved,
         |              answer,
         |              updateTime
         |       )
         |       VALUES (?, ?, ?, ?, ?)""".stripMargin)) { ps => {
      ps.setInt(1, year)
      ps.setInt(2, day)
      ps.setInt(3, part)
      ps.setBoolean(4, isSolved)
      ps.setLong(5, answer)
      ps.setUpdateTime(6, updateTime)
      ps.executeUpdate()
    }}
}
