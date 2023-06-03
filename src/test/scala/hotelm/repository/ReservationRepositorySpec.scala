package hotelm.repository

import hotelm.Reservation
import hotelm.Room
import hotelm.fixture.LocalDateTimeFixture
import hotelm.fixture.ReservationFixture
import io.getquill.*
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object ReservationRepositorySpec extends RepositorySpec:

  def spec = suite("ReservationRepositorySpec")(
    test("It should store a reservation.") {
      val (reservation, room) = ReservationFixture.createNewWithRoom()

      for
        _          <- ZIO.serviceWithZIO[RoomRepository](_.add(room))
        repository <- ZIO.service[ReservationRepository]
        _          <- repository.add(reservation)
        stored     <- run(quote(query[Reservation].filter(_.roomNumber == lift(reservation.roomNumber)).take(1)))
      yield assertTrue(
        stored == List(reservation)
      )
    },
    test("It should search a previous reservation.") {
      val (previous, room) = ReservationFixture.createNewWithRoom()

      for
        _          <- ZIO.serviceWithZIO[RoomRepository](_.add(room))
        repository <- ZIO.service[ReservationRepository]
        _          <- repository.add(previous)
        _          <- repository.add(
                        previous.copy(
                          id = UUID.randomUUID().toString,
                          checkIn = previous.checkIn.minusDays(2),
                          checkOut = previous.checkOut.minusDays(2)
                        )
                      )
        result     <- repository.searchPrevious(previous.roomNumber, previous.checkOut.plusHours(7))
      yield assertTrue(
        result.contains(previous)
      )
    },
    test("It should search for reservations overlapping.") {
      val (previous, room) = ReservationFixture.createNewWithRoom()

      val first = previous.copy(
        id = UUID.randomUUID().toString,
        checkIn = previous.checkIn.minusDays(2),
        checkOut = previous.checkIn.minusDays(1)
      )

      val newReservation = previous.copy(
        id = UUID.randomUUID().toString,
        checkIn = first.checkIn.minusDays(2),
        checkOut = previous.checkOut.plusDays(2)
      )

      for
        _          <- ZIO.serviceWithZIO[RoomRepository](_.add(room))
        repository <- ZIO.service[ReservationRepository]
        _          <- repository.add(previous)
        _          <- repository.add(first)
        result     <- repository.searchIntersection(room.number, newReservation.checkIn, newReservation.checkOut)
      yield assertTrue(
        result == List(first, previous)
      )
    },
    test("It should search all reservations for a given day.") {
      val date         = LocalDateTimeFixture.createNew().toLocalDate
      val reservations = (for _ <- 0 until 10 yield
        val (reservation, room) = ReservationFixture.createNewWithRoom()
        reservation.copy(
          checkIn = LocalDateTime.of(date, reservation.checkIn.toLocalTime),
          checkOut = LocalDateTime.of(date, reservation.checkOut.toLocalTime)
        ) -> room
      ).toList

      for
        roomRepository <- ZIO.service[RoomRepository]
        repository     <- ZIO.service[ReservationRepository]
        _              <- ZIO.foreach(reservations)((_, room) => roomRepository.add(room))
        _              <- ZIO.foreach(reservations)((reservation, _) => repository.add(reservation))
        result         <- repository.search(date)
      yield assertTrue(
        result == reservations.map(_._1).sorted
      )
    }
  ).provideSome[Scope](
    H2DataSource.layer,
    ZLayer.fromZIO {
      for dataSource <- ZIO.service[DataSource]
      yield ReservationRepository(ctx, ZLayer.succeed(dataSource))
    },
    ZLayer.fromZIO {
      for dataSource <- ZIO.service[DataSource]
      yield RoomRepository(ctx, ZLayer.succeed(dataSource))
    }
  )
