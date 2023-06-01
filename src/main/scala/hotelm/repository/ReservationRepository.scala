package hotelm.repository

import com.softwaremill.macwire.wire
import hotelm.Reservation
import io.getquill.NamingStrategy
import io.getquill.context.jdbc.JdbcContext
import io.getquill.context.sql.idiom.SqlIdiom

import java.time.LocalDateTime
import zio.Task
import zio.TaskLayer

import javax.sql.DataSource

trait ReservationRepository:

  def add(reservation: Reservation): Task[Reservation]

  def searchPrevious(room: String, checkIn: LocalDateTime): Task[Option[Reservation]]

  def searchIntersection(room: String, checkIn: LocalDateTime, checkOut: LocalDateTime): Task[List[Reservation]]

object ReservationRepository:

  def apply(ctx: JdbcContext[SqlIdiom, NamingStrategy], dataSourceLayer: TaskLayer[DataSource]): ReservationRepository =
    wire[Default]

  private class Default extends ReservationRepository:

    override def add(reservation: Reservation): Task[Reservation] = ???

    override def searchPrevious(room: String, checkIn: LocalDateTime): Task[Option[Reservation]] = ???

    override def searchIntersection(room: String, checkIn: LocalDateTime, checkOut: LocalDateTime): Task[List[Reservation]] = ???
