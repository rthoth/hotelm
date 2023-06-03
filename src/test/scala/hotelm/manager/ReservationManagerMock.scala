package hotelm.manager

import hotelm.Reservation
import hotelm.Room
import java.time.LocalDate
import zio.Task
import zio.ZLayer
import zio.mock.Mock
import zio.mock.Proxy

object ReservationManagerMock extends Mock[ReservationManager]:

  object Accept extends Effect[(Reservation, Room), Throwable, (Reservation, Room)]

  object SearchAll extends Effect[LocalDate, Throwable, List[Reservation]]

  val compose = ZLayer.fromFunction((proxy: Proxy) => {
    new ReservationManager:

      override def accept(reservation: Reservation, room: Room): Task[(Reservation, Room)] =
        proxy(Accept, reservation, room)

      override def search(date: LocalDate): Task[List[Reservation]] =
        proxy(SearchAll, date)
  })
