package hotelm.manager

import hotelm.Reservation
import hotelm.Room
import zio.Task
import zio.ZLayer
import zio.mock.Mock
import zio.mock.Proxy

object RoomManagerMock extends Mock[RoomManager]:

  object Add    extends Effect[Room, Throwable, Room]
  object All    extends Effect[Any, Throwable, List[Room]]
  object Remove extends Effect[String, Throwable, Room]
  object Accept extends Effect[Reservation, Throwable, (Reservation, Room)]

  val compose = ZLayer.fromFunction((proxy: Proxy) => {
    new RoomManager:

      override def add(room: Room): Task[Room] =
        proxy(Add, room)

      override def all: Task[List[Room]] =
        proxy(All, ())

      override def remove(number: String): Task[Room] =
        proxy(Remove, number)

      override def accept(reservation: Reservation): Task[(Reservation, Room)] =
        proxy(Accept, reservation)

  })
