package hotelm.manager

import hotelm.HotelmException
import hotelm.Reservation
import hotelm.Room
import hotelm.Spec
import hotelm.fixture.ReservationFixture
import hotelm.repository.RoomRepository
import zio.Cause
import zio.Task
import zio.URLayer
import zio.ZIO
import zio.ZLayer
import zio.mock.Expectation
import zio.mock.Mock
import zio.mock.Proxy
import zio.test.*

object RoomManagerSpec extends Spec:

  private val roomManagerLayer = ZLayer.fromFunction(RoomManager.apply)

  override def spec = suite("RoomManagerSpec")(
    test("It should add a valid room.") {
      val room      = Room("42C", 10)
      val mockLayer = RoomRepositoryMock.Add(
        assertion = Assertion.equalTo(room),
        result = Expectation.value(room)
      )

      (for
        manager <- ZIO
                     .service[RoomManager]
        added   <- manager.add(room)
      yield assertTrue(
        added == room
      )).provide(roomManagerLayer, mockLayer, ReservationManagerMock.empty)
    },
    test("It should remove a room.") {
      val expected  = Room("42C", 15)
      val mockLayer = RoomRepositoryMock.Remove(
        assertion = Assertion.equalTo(expected.number),
        result = Expectation.value(Some(expected))
      )

      (for
        manager <- ZIO.service[RoomManager]
        removed <- manager.remove(expected.number)
      yield assertTrue(
        removed == expected
      )).provide(roomManagerLayer, mockLayer, ReservationManagerMock.empty)
    },
    test("It should notify an user mistake (Room Not Found)!") {
      val mockLayer = RoomRepositoryMock.Remove(
        assertion = Assertion.equalTo("42C"),
        result = Expectation.value(None)
      )

      (for
        manager <- ZIO.service[RoomManager]
        _       <- manager.remove("42C")
      yield assertTrue(
        true
      )).provide(roomManagerLayer, mockLayer, ReservationManagerMock.empty)
    } @@ TestAspect.failing[Throwable] {
      case TestFailure.Runtime(Cause.Fail(cause: HotelmException.RoomNotFound, _), _) if cause.getMessage == "42C" =>
        true
      case _                                                                                                       => false
    },
    test("It should reject an invalid Room (invalid beds number).") {
      for
        manager <- ZIO
                     .service[RoomManager]
                     .provide(roomManagerLayer, RoomRepositoryMock.empty, ReservationManagerMock.empty)
        _       <- manager.add(Room("42A", -50))
      yield assertTrue(true)
    } @@ TestAspect.failing[Throwable] {
      case TestFailure.Runtime(Cause.Fail(cause: HotelmException.InvalidRoom, _), _)
          if cause.getMessage == "Number os beds is invalid!" =>
        true
      case _ => false
    },
    test("It should reject an invalid Room (invalid number).") {
      for
        manager <- ZIO
                     .service[RoomManager]
                     .provide(roomManagerLayer, RoomRepositoryMock.empty, ReservationManagerMock.empty)
        _       <- manager.add(Room("      ", 13))
      yield assertTrue(true)
    } @@ TestAspect.failing[Throwable] {
      case TestFailure.Runtime(Cause.Fail(cause: HotelmException.InvalidRoom, _), _)
          if cause.getMessage == "The room's number is invalid!" =>
        true
      case _ => false
    },
    test("It should accept a valid room reservation.") {
      val (reservation, room) = ReservationFixture.createNew()
      val roomRepository      = RoomRepositoryMock.Get(
        assertion = Assertion.equalTo(room.number),
        result = Expectation.value(Some(room))
      )
      val reservationManager  = ReservationManagerMock.Accept(
        assertion = Assertion.equalTo((reservation, room)),
        result = Expectation.value((reservation, room))
      )

      (for
        manager  <- ZIO
                      .service[RoomManager]
        response <- manager.accept(reservation)
      yield assertTrue(
        response == (reservation, room)
      )).provide(roomManagerLayer, roomRepository, reservationManager)
    },
    test("When the room is not available, it should report.") {
      val (reservation, room) = ReservationFixture.createNew()
      val roomRepository      = RoomRepositoryMock.Get(
        assertion = Assertion.equalTo(room.number),
        result = Expectation.value(Some(room))
      )
      val reservationManager  = ReservationManagerMock.Accept(
        assertion = Assertion.equalTo((reservation, room)),
        result = Expectation.failure(HotelmException.RoomUnavailable(room.number))
      )

      (for
        manager <- ZIO
                     .service[RoomManager]
        exit    <- manager.accept(reservation).exit
      yield assert(exit)(Assertion.failsWithA[HotelmException.RoomUnavailable]))
        .provide(roomManagerLayer, roomRepository, reservationManager)
    }
  )

  object RoomRepositoryMock extends Mock[RoomRepository]:

    val compose: URLayer[Proxy, RoomRepository] = ZLayer {
      for proxy <- ZIO.service[Proxy]
      yield new RoomRepository:
        override def add(room: Room): Task[Room] =
          proxy(Add, room)

        override def get(number: String): Task[Option[Room]] =
          proxy(Get, number)

        override def remove(number: String): Task[Option[Room]] =
          proxy(Remove, number)
    }

    object Add extends Effect[Room, Throwable, Room]

    object Remove extends Effect[String, Throwable, Option[Room]]

    object Get extends Effect[String, Throwable, Option[Room]]

  object ReservationManagerMock extends Mock[ReservationManager]:

    object Accept extends Effect[(Reservation, Room), Throwable, (Reservation, Room)]

    val compose = ZLayer.fromFunction((proxy: Proxy) => {
      new ReservationManager:

        override def accept(reservation: Reservation, room: Room): Task[(Reservation, Room)] =
          proxy(Accept, reservation, room)
    })
