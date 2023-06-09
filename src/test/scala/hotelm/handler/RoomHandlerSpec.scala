package hotelm.handler

import hotelm.Reservation
import hotelm.Room
import hotelm.Spec
import hotelm.fixture.ReservationFixture
import hotelm.fixture.RoomFixture
import hotelm.manager.RoomManager
import hotelm.manager.RoomManagerMock
import java.io.IOException
import java.nio.charset.StandardCharsets
import zio.ZIO
import zio.ZLayer
import zio.http.*
import zio.http.Header.ContentType
import zio.mock
import zio.mock.Expectation
import zio.test.Assertion
import zio.test.assertTrue

object RoomHandlerSpec extends Spec:

  private val roomHandlerLayer = ZLayer.fromFunction(RoomHandler.apply)

  override def spec = suite("RoomHandlerSpec")(
    suite("During room adding.")(
      test("It should inform the user the room was added correctly.") {
        val expectedRoom    = Room("44B", 5)
        val expectedRequest = Request
          .post(Body.fromString("""{"number": "44B", "beds": 5}"""), URL(Root / "room"))
          .withHeader(Header.ContentType(MediaType.application.json))

        val roomManagerLayer = RoomManagerMock.Add(
          assertion = Assertion.equalTo(expectedRoom),
          result = Expectation.value(expectedRoom)
        )
        (
          for
            handler  <- ZIO.service[RoomHandler]
            response <- handler.add(expectedRequest)
            body     <- response.body.asString(StandardCharsets.UTF_8)
          yield assertTrue(
            response.status == Status.Ok,
            body == """{"number":"44B","beds":5}"""
          )
        ).provide(roomHandlerLayer, roomManagerLayer)
      },
      test("It should report to the user that JSON was send is invalid.") {
        val expectedRequest = Request
          .post(Body.fromString("""{"numbers": "44B", "bed": 5}"""), URL(Root / "room"))
          .withHeader(Header.ContentType(MediaType.application.json))
        (
          for
            handler  <- ZIO.service[RoomHandler]
            response <- handler.add(expectedRequest).catchAll(ZIO.succeed)
            body     <- response.body.asString(StandardCharsets.UTF_8)
          yield assertTrue(
            response.status == Status.BadRequest,
            body == """{"message":".number(missing)"}"""
          )
        ).provide(roomHandlerLayer, RoomManagerMock.empty)
      },
      test("It should report to the user any internal failure.") {
        val expectedRoom    = Room("44B", 5)
        val expectedRequest = Request
          .post(Body.fromString("""{"number": "44B", "beds": 5}"""), URL(Root / "room"))
          .withHeader(Header.ContentType(MediaType.application.json))

        val roomManagerLayer = RoomManagerMock.Add(
          assertion = Assertion.equalTo(expectedRoom),
          result = Expectation.failure(IOException("Internal error!"))
        )
        (
          for
            handler  <- ZIO.service[RoomHandler]
            response <- handler.add(expectedRequest).catchAll(ZIO.succeed)
            body     <- response.body.asString(StandardCharsets.UTF_8)
          yield assertTrue(
            response.status == Status.InternalServerError,
            body == """{"message":"Unexpected internal error!"}"""
          )
        ).provide(roomHandlerLayer, roomManagerLayer)
      }
    ),
    suite("During room booking.")(
      test("It should inform the user that the booking was successfully completed.") {
        val (reservation, room) = ReservationFixture.createNewWithRoom()

        val expectedRequest = Request
          .post(
            Body.fromString(
              s"""{"client":"${reservation.client}","checkIn":"${reservation.checkIn}","checkOut":"${reservation.checkOut}"}"""
            ),
            URL(Root / "room" / room.number / "booking")
          )
          .withHeader(ContentType(MediaType.application.json))

        val mockLayer = RoomManagerMock.Accept(
          assertion = Assertion.equalTo(reservation),
          result = Expectation.value((reservation, room))
        )

        val idGeneratorLayer = ZLayer.succeed {
          new Reservation.IdGenerator:
            override def nextId: String = reservation.id

        }

        (for
          handler  <- ZIO.service[RoomHandler]
          response <- handler.book(expectedRequest, room.number).catchAll(ZIO.succeed)
          body     <- response.body.asString(StandardCharsets.UTF_8)
        yield assertTrue(
          response.status == Status.Accepted,
          body == s"""{"number":"${room.number}","client":"${reservation.client}","checkIn":"${reservation.checkIn}","checkOut":"${reservation.checkOut}"}"""
        )).provide(roomHandlerLayer, mockLayer, idGeneratorLayer)
      }
    ),
    suite("During room removing")(
      test("It should inform the user that the room was removed correctly.") {
        val room        = RoomFixture.createNew()
        val req         = Request.delete(URL(Root / "room" / room.number))
        val roomManager = RoomManagerMock.Remove(
          assertion = Assertion.equalTo(room.number),
          result = Expectation.value(room)
        )

        (for
          response <- ZIO.serviceWithZIO[RoomHandler](_.remove(req, room.number))
          body     <- response.body.asString
        yield assertTrue(
          response.status == Status.Ok,
          body == s"""{"number":"${room.number}","beds":${room.beds}}"""
        )).provide(roomHandlerLayer, roomManager)
      }
    )
  )
