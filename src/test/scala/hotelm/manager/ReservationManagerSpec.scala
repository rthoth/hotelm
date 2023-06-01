package hotelm.manager

import hotelm.HotelmException
import hotelm.Reservation
import hotelm.Spec
import hotelm.fixture.ReservationFixture
import hotelm.repository.ReservationRepository
import java.time.Duration
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

      val expected @ (reservation, room) = ReservationFixture.createNew()
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
      val (reservation, room) = ReservationFixture.createNew()
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
      val (reservation, room) = ReservationFixture.createNew()
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
    }
  )

  object ReservationRepositoryMock extends Mock[ReservationRepository]:

    object SearchPrevious     extends Effect[(String, LocalDateTime), Throwable, Option[Reservation]]
    object SearchIntersection extends Effect[(String, LocalDateTime, LocalDateTime), Throwable, List[Reservation]]
    object Add                extends Effect[Reservation, Throwable, Reservation]

    val compose = ZLayer.fromFunction((proxy: Proxy) => {
      new ReservationRepository:

        override def add(reservation: Reservation): Task[Reservation] =
          proxy(Add, reservation)

        override def searchPrevious(room: String, checkIn: LocalDateTime): Task[Option[Reservation]] =
          proxy(SearchPrevious, room, checkIn)

        override def searchIntersection(
            room: String,
            checkIn: LocalDateTime,
            checkOut: LocalDateTime
        ): Task[List[Reservation]] =
          proxy(SearchIntersection, room, checkIn, checkOut)

    })
