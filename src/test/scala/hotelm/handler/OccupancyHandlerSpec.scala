package hotelm.handler

import hotelm.OccupancyReport
import hotelm.Spec
import hotelm.fixture.LocalDateTimeFixture
import hotelm.fixture.OccupancyReportFixture
import hotelm.handler.protocol.OccupancyReportResponse
import hotelm.reporter.OccupancyReporter
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.http.Request
import zio.http.Status
import zio.http.URL
import zio.json.given
import zio.mock.Expectation
import zio.mock.Mock
import zio.mock.Proxy
import zio.test.Assertion
import zio.test.assertTrue

object OccupancyHandlerSpec extends Spec:

  private val handlerLayer = ZLayer.fromFunction(OccupancyHandler.apply)

  def spec = suite("OccupancyHandlerSpec")(
    test("It should return to the user de occupancy report.") {
      val request = Request.get(URL.decode("/").toTry.get)
      val date    = LocalDateTimeFixture.createNew().toLocalDate
      val report  = OccupancyReportFixture.createNew(date)

      val occupancyReporter = OccupancyReporterMock.Report(
        assertion = Assertion.equalTo(date),
        result = Expectation.value(report)
      )

      (for
        response <- ZIO.serviceWithZIO[OccupancyHandler](_.search(request, date.toString))
        body     <- response.body.asString(StandardCharsets.UTF_8)
      yield assertTrue(
        response.status == Status.Ok,
        body == OccupancyReportResponse(report).toJson
      )).provide(handlerLayer, occupancyReporter)
    }
  )

  object OccupancyReporterMock extends Mock[OccupancyReporter]:

    object Report extends Effect[LocalDate, Throwable, OccupancyReport]

    val compose = ZLayer.fromFunction((proxy: Proxy) => {
      new OccupancyReporter:
        override def report(date: LocalDate): Task[OccupancyReport] =
          proxy(Report, date)
    })
