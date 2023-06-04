package hotelm.manager

import hotelm.HotelmException
import hotelm.Reservation
import hotelm.Spec
import hotelm.fixture.LocalDateTimeFixture
import hotelm.fixture.ReservationFixture
import hotelm.repository.ReservationRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.mock.Expectation
import zio.mock.Mock
import zio.mock.Proxy
import zio.test.Assertion
import zio.test.assert
import zio.test.assertTrue

object ReservationManagerSpec extends Spec:

  private val reservationManagerLayer = ZLayer.fromFunction(ReservationManager.apply)

  private val defaultReservationManagerConfig = ZLayer.succeed(
    ReservationManager.Config(
      cleaningTime = Duration.ofHours(4),
      minimumDuration = Duration.ofHours(12),
      maximumDuration = Duration.ofDays(20)
    )
  )

  def spec = suite("ReservationManagerSpec")(
    test("It should accept a reservation.") {

      val expected @ (reservation, room) = ReservationFixture.createNewWithRoom()
      val previous                       = reservation.copy(
        checkIn = reservation.checkIn.minusDays(2),
        checkOut = reservation.checkIn.minusHours(10)
      )

      val reservationRepository = ReservationRepositoryMock.SearchPrevious(
        assertion = Assertion.equalTo((room.number, reservation.checkIn)),
        result = Expectation.value(Some(previous))
      ) and ReservationRepositoryMock.SearchIntersection(
        assertion = Assertion.equalTo((room.number, reservation.checkIn, reservation.checkOut)),
        result = Expectation.value(Nil)
      ) and ReservationRepositoryMock.Add(
        assertion = Assertion.equalTo(reservation),
        result = Expectation.value(reservation)
      )

      (for
        manager <- ZIO.service[ReservationManager]
        result  <- manager.accept(reservation, room)
      yield assertTrue(
        result == expected
      )).provide(defaultReservationManagerConfig, reservationManagerLayer, reservationRepository)
    },
    test("It should refuse a reservation when the cleanup window is not respected.") {
      val (reservation, room) = ReservationFixture.createNewWithRoom()
      val previous            = reservation.copy(
        checkIn = reservation.checkIn.minusDays(2),
        checkOut = reservation.checkIn.minusHours(3)
      )

      val reservationRepository = ReservationRepositoryMock.SearchPrevious(
        assertion = Assertion.equalTo((room.number, reservation.checkIn)),
        result = Expectation.value(Some(previous))
      )

      (for
        manager <- ZIO.service[ReservationManager]
        exit    <- manager.accept(reservation, room).exit
      yield assert(exit)(Assertion.failsWithA[HotelmException.RoomUnavailable]))
        .provide(defaultReservationManagerConfig, reservationManagerLayer, reservationRepository)
    },
    test("It should refuse a reservation when there is any overlapping.") {
      val (reservation, room) = ReservationFixture.createNewWithRoom()
      val previous            = reservation.copy(
        checkIn = reservation.checkIn.minusDays(2),
        checkOut = reservation.checkIn.minusHours(5)
      )

      val overlapping = reservation.copy(
        checkIn = reservation.checkOut.minusDays(2),
        checkOut = reservation.checkOut.minusDays(1)
      )

      val reservationRepository = ReservationRepositoryMock.SearchPrevious(
        assertion = Assertion.equalTo((room.number, reservation.checkIn)),
        result = Expectation.value(Some(previous))
      ) and ReservationRepositoryMock.SearchIntersection(
        assertion = Assertion.equalTo((reservation.roomNumber, reservation.checkIn, reservation.checkOut)),
        result = Expectation.value(List(overlapping))
      )

      (for
        manager <- ZIO.service[ReservationManager]
        exit    <- manager.accept(reservation, room).exit
      yield assert(exit)(Assertion.failsWithA[HotelmException.RoomUnavailable]))
        .provide(defaultReservationManagerConfig, reservationManagerLayer, reservationRepository)
    },
    test("It should search all reservation for a given day.") {
      val date                  = LocalDateTimeFixture.createNew().toLocalDate
      val reservations          = (0 until 10).map(_ => ReservationFixture.createNew()).toList
      val reservationRepository = ReservationRepositoryMock.Search(
        assertion = Assertion.equalTo(date),
        result = Expectation.value(reservations)
      )

      (for result <- ZIO.serviceWithZIO[ReservationManager](_.search(date))
      yield assertTrue(
        result == reservations
      )).provide(reservationManagerLayer, reservationRepository, defaultReservationManagerConfig)
    }
  )

  object ReservationRepositoryMock extends Mock[ReservationRepository]:

    object SearchPrevious extends Effect[(String, LocalDateTime), Throwable, Option[Reservation]]

    object SearchNext         extends Effect[(String, LocalDateTime), Throwable, Option[Reservation]]
    object SearchIntersection extends Effect[(String, LocalDateTime, LocalDateTime), Throwable, List[Reservation]]
    object Add                extends Effect[Reservation, Throwable, Reservation]
    object Search             extends Effect[LocalDate, Throwable, List[Reservation]]

    val compose = ZLayer.fromFunction((proxy: Proxy) => {
      new ReservationRepository:

        override def add(reservation: Reservation): Task[Reservation] =
          proxy(Add, reservation)

        override def search(date: LocalDate): Task[List[Reservation]] =
          proxy(Search, date)

        override def searchPrevious(room: String, checkIn: LocalDateTime): Task[Option[Reservation]] =
          proxy(SearchPrevious, room, checkIn)

        override def searchNext(room: String, checkOut: LocalDateTime): Task[Option[Reservation]] =
          proxy(SearchNext, room, checkOut)

        override def searchIntersection(
            room: String,
            checkIn: LocalDateTime,
            checkOut: LocalDateTime
        ): Task[List[Reservation]] =
          proxy(SearchIntersection, room, checkIn, checkOut)

    })
