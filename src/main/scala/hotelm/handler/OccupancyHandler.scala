package hotelm.handler

import com.softwaremill.macwire.wire
import hotelm.HotelmException
import hotelm.handler.protocol.OccupancyReportResponse
import hotelm.reporter.OccupancyReporter
import java.time.LocalDate
import zio.IO
import zio.Task
import zio.ZIO
import zio.http.Request
import zio.http.Response
import zio.http.Status

trait OccupancyHandler:

  def search(req: Request, date: String): IO[Response, Response]

object OccupancyHandler:

  def apply(reporter: OccupancyReporter): OccupancyHandler =
    wire[Default]

  private class Default(reporter: OccupancyReporter) extends OccupancyHandler:

    override def search(req: Request, date: String): IO[Response, Response] =
      for
        localDate <- parseLocalDate(date)
                       .mapError(handleError)
        report    <- reporter
                       .report(localDate)
                       .mapError(handleError)
      yield responseAsJson(Status.Ok, OccupancyReportResponse(report))

    private def parseLocalDate(date: String): Task[LocalDate] =
      ZIO
        .attempt(LocalDate.parse(date))
        .mapError(HotelmException.InvalidUserInput("Invalid date!", _))
