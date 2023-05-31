package hotelm.manager

import hotelm.HotelmException
import hotelm.Room
import hotelm.Spec
import hotelm.repository.RoomRepository
import zio.Cause
import zio.Console
import zio.Scope
import zio.Task
import zio.URLayer
import zio.ZIO
import zio.ZLayer
import zio.mock.Expectation
import zio.mock.Mock
import zio.mock.Proxy
import zio.test.*

object RoomManagerSpec extends Spec:

  private val testLayer = ZLayer.fromFunction(RoomManager.apply)

  override def spec = suite("RoomManagerSpec")(
    test("It should add a valid room.") {
      val room      = Room("42C", 10)
      val mockLayer = MockRoomRepository.Add(
        assertion = Assertion.equalTo(room),
        result = Expectation.value(room)
      )

      (for
        manager <- ZIO
                     .service[RoomManager]
        added   <- manager.add(room)
      yield assertTrue(
        added == room
      )).provide(testLayer, mockLayer)
    },
    test("It should remove a room.") {
      val expected  = Room("42C", 15)
      val mockLayer = MockRoomRepository.Remove(
        assertion = Assertion.equalTo(expected.number),
        result = Expectation.value(Some(expected))
      )

      (for
        manager <- ZIO.service[RoomManager]
        removed <- manager.remove(expected.number)
      yield assertTrue(
        removed == expected
      )).provide(testLayer, mockLayer)
    },
    test("It should notify an user mistake (Room Not Found)!") {
      val mockLayer = MockRoomRepository.Remove(
        assertion = Assertion.equalTo("42C"),
        result = Expectation.value(None)
      )

      (for
        manager <- ZIO.service[RoomManager]
        _       <- manager.remove("42C")
      yield assertTrue(
        true
      )).provide(testLayer, mockLayer)
    } @@ TestAspect.failing[Throwable] {
      case TestFailure.Runtime(Cause.Fail(cause: HotelmException.RoomNotFound, _), _) if cause.getMessage == "42C" =>
        true
      case _                                                                                                       => false
    },
    test("It should reject an invalid Room (invalid beds number).") {
      for
        manager <- ZIO
                     .service[RoomManager]
                     .provide(testLayer, MockRoomRepository.empty)
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
                     .provide(testLayer, MockRoomRepository.empty)
        _       <- manager.add(Room("      ", 13))
      yield assertTrue(true)
    } @@ TestAspect.failing[Throwable] {
      case TestFailure.Runtime(Cause.Fail(cause: HotelmException.InvalidRoom, _), _)
          if cause.getMessage == "The room's number is invalid!" =>
        true
      case _ => false
    }
  )

  object MockRoomRepository extends Mock[RoomRepository]:

    val compose: URLayer[Proxy, RoomRepository] = ZLayer {
      for proxy <- ZIO.service[Proxy]
      yield new RoomRepository:
        override def add(room: Room): Task[Room] =
          proxy(Add, room)

        override def remove(number: String): Task[Option[Room]] =
          proxy(Remove, number)
    }

    object Add extends Effect[Room, Throwable, Room]

    object Remove extends Effect[String, Throwable, Option[Room]]
