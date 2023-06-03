package hotelm.module

import hotelm.Reservation
import hotelm.Spec
import hotelm.handler.OccupancyHandler
import hotelm.handler.RoomHandler
import zio.IO
import zio.ZIO
import zio.ZLayer
import zio.http.*
import zio.mock
import zio.mock.Expectation
import zio.mock.Mock
import zio.mock.Proxy
import zio.test.Assertion
import zio.test.assertTrue

object HttpAppModuleSpec extends Spec:

  private val routeLayer = ZLayer.fromZIO {
    ZIO.serviceWithZIO[HandlerModule](HttpAppModule.createRoute)
  }

  def spec = suite("HttpAppModuleSpec")(
    test("POST /room") {
      val req = Request.post(url = URL(Root / "room"), body = Body.empty)
      val res = Response.ok

      testRoute(req, res) {
        HandlerModuleMock.AddRoom(
          assertion = Assertion.equalTo(req),
          result = Expectation.value(res)
        )
      }
    },
    test("DELETE /room/42A") {
      val req = Request.delete(URL(Root / "room" / "42A"))
      val res = Response.status(Status.Accepted)
      testRoute(req, res) {
        HandlerModuleMock.RemoveRoom(
          assertion = Assertion.equalTo((req, "42A")),
          result = Expectation.value(res)
        )
      }
    },
    test("POST /room/42B/booking") {
      val req = Request.post(url = URL(Root / "room" / "42B" / "booking"), body = Body.empty)
      val res = Response.status(Status.Accepted)
      testRoute(req, res) {
        HandlerModuleMock.BookRoom(
          assertion = Assertion.equalTo((req, "42B")),
          result = Expectation.value(res)
        )
      }
    },
    test("GET /occupancy/2023-06-24") {
      val req = Request.get(URL(Root / "occupancy" / "2023-06-24"))
      val res = Response.ok
      testRoute(req, res) {
        HandlerModuleMock.SearchOccupancy(
          assertion = Assertion.equalTo((req, "2023-06-24")),
          result = Expectation.value(res)
        )
      }
    }
  )

  private def testRoute(req: Request, res: Response)(expectation: Expectation[HandlerModule]) =
    (for result <- ZIO.serviceWithZIO[App[Any]](_.runZIO(req))
    yield assertTrue(result == res)).provide(routeLayer, expectation)

  object HandlerModuleMock extends Mock[HandlerModule]:

    object AddRoom         extends Effect[Request, Response, Response]
    object BookRoom        extends Effect[(Request, String), Response, Response]
    object RemoveRoom      extends Effect[(Request, String), Response, Response]
    object SearchOccupancy extends Effect[(Request, String), Response, Response]

    val compose = ZLayer.fromFunction((proxy: Proxy) => {
      new HandlerModule:

        override val roomHandler: RoomHandler = new RoomHandler:

          override def add(req: Request): IO[Response, Response] =
            proxy(AddRoom, req)

          override def book(req: Request, number: String): ZIO[Reservation.IdGenerator, Response, Response] =
            proxy(BookRoom, req, number)

          override def remove(req: Request, number: String): IO[Response, Response] =
            proxy(RemoveRoom, req, number)

        override val occupancyHandler: OccupancyHandler = new OccupancyHandler:

          override def search(req: Request, date: String): IO[Response, Response] =
            proxy(SearchOccupancy, req, date)
    })
