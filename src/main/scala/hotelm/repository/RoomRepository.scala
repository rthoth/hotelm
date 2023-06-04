package hotelm.repository

import hotelm.HotelmException
import hotelm.Room
import io.getquill.*
import io.getquill.context.qzio.ZioJdbcContext
import io.getquill.context.sql.idiom.SqlIdiom
import io.github.arainko.ducktape.Transformer
import javax.sql.DataSource
import zio.Task
import zio.TaskLayer
import zio.ZIO

trait RoomRepository:

  def add(room: Room): Task[Room]

  def all: Task[List[Room]]

  def get(number: String): Task[Option[Room]]

  def remove(number: String): Task[Option[Room]]

object RoomRepository:

  def apply(ctx: ZioJdbcContext[SqlIdiom, NamingStrategy], dataSourceLayer: TaskLayer[DataSource]): RoomRepository =
    new Default(ctx, dataSourceLayer)

  private case class StoredRoom(number: String, beds: Int)

  private given Transformer[StoredRoom, Room] =
    Transformer.define.build()

  private given Transformer[Room, StoredRoom] =
    Transformer.define.build()

  private class Default(ctx: ZioJdbcContext[SqlIdiom, NamingStrategy], dataSourceLayer: TaskLayer[DataSource])
      extends RoomRepository:

    import ctx.*

    private inline def rooms = quote(querySchema[StoredRoom]("room"))

    override def add(room: Room): Task[Room] =
      for
        toStore <- convertTo[StoredRoom](room)
        _       <-
          run(quote { rooms.insertValue(lift(toStore)) })
            .provideLayer(dataSourceLayer)
            .mapError(HotelmException.UnableToInsertRoom("An unexpected error occurred while inserting a new room!", _))
            .filterOrFail(_ == 1)(HotelmException.UnableToInsertRoom(s"It was impossible to add room $room!"))
      yield room

    override def all: Task[List[Room]] =
      for result <- run(quote(rooms.sortBy(_.number)(Ord.asc)))
                      .provideLayer(dataSourceLayer)
                      .flatMap(ZIO.foreach(_)(convertTo[Room].apply))
      yield result

    override def get(number: String): Task[Option[Room]] =
      for result <- run(quote(rooms.filter(_.number == lift(number)).take(1)))
                      .provideLayer(dataSourceLayer)
                      .flatMap(ZIO.foreach(_)(convertTo[Room].apply))
      yield result.headOption

    override def remove(number: String): Task[Option[Room]] =
      val select = quote(rooms.filter(_.number == lift(number)))

      (for
        list   <- run(quote(select))
        result <- list match
                    case List(stored) => run(quote(select.delete)) *> convertTo[Room](stored).option
                    case _            => ZIO.none
      yield result).provideLayer(dataSourceLayer)
