package hotelm.repository

import com.softwaremill.macwire.wire
import hotelm.HotelmException
import hotelm.Reservation
import io.getquill.*
import io.getquill.context.qzio.ZioJdbcContext
import io.getquill.context.sql.idiom.SqlIdiom
import io.github.arainko.ducktape.Transformer
import io.github.arainko.ducktape.into
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource
import zio.Task
import zio.TaskLayer
import zio.ZIO

trait ReservationRepository:

  def add(reservation: Reservation): Task[Reservation]

  def searchPrevious(room: String, checkIn: LocalDateTime): Task[Option[Reservation]]

  def searchIntersection(room: String, checkIn: LocalDateTime, checkOut: LocalDateTime): Task[List[Reservation]]

object ReservationRepository:

  def apply(
      ctx: ZioJdbcContext[SqlIdiom, NamingStrategy],
      dataSourceLayer: TaskLayer[DataSource]
  ): ReservationRepository =
    wire[Default]

  private case class StoredReservation(
      id: String,
      roomNumber: String,
      client: String,
      checkIn: Timestamp,
      checkOut: Timestamp
  )

  private given Transformer[Reservation, StoredReservation] =
    _.into[StoredReservation].transform()

  private given Transformer[StoredReservation, Reservation] =
    _.into[Reservation].transform()

  private class Default(ctx: ZioJdbcContext[SqlIdiom, NamingStrategy], dataSourceLayer: TaskLayer[DataSource])
      extends ReservationRepository:

    import ctx.*

    private inline def reservations = quote(querySchema[StoredReservation]("reservation"))

    override def add(reservation: Reservation): Task[Reservation] =
      for
        toStore <- convertTo[StoredReservation](reservation)
        _       <- run(quote(reservations.insertValue(lift(toStore))))
                     .provideLayer(dataSourceLayer)
                     .filterOrFail(_ == 1)(HotelmException.UnableToInsertReservation(reservation.roomNumber))
      yield reservation

    override def searchPrevious(room: String, checkIn: LocalDateTime): Task[Option[Reservation]] =
      for
        checkIn <- convertTo[Timestamp](checkIn)
        result  <- run(quote(reservations.filter(r => r.checkOut <= lift(checkIn)).sortBy(_.checkOut)(Ord.desc).take(1)))
                     .provideLayer(dataSourceLayer)
                     .flatMap(ZIO.foreach(_)(convertTo[Reservation].apply))
      yield result.headOption

    override def searchIntersection(
        room: String,
        checkIn: LocalDateTime,
        checkOut: LocalDateTime
    ): Task[List[Reservation]] =
      for
        checkIn  <- convertTo[Timestamp](checkIn)
        checkOut <- convertTo[Timestamp](checkOut)
        result   <-
          run(
            quote(
              reservations
                .filter(r => r.checkOut >= lift(checkIn) && r.checkIn <= lift(checkOut))
                .sortBy(_.checkIn)
            )
          )
            .provideLayer(dataSourceLayer)
            .flatMap(ZIO.foreach(_)(convertTo[Reservation].apply))
      yield result
