package hotelm.handler

import zio.IO
import zio.http.Request
import zio.http.Response

trait VacancyHandler:

  def search(req: Request, date: String): IO[Response, Response]

object VacancyHandler:

  def apply(): VacancyHandler = new Default()

  private class Default extends VacancyHandler:

    override def search(req: Request, date: String): IO[Response, Response] = ???
