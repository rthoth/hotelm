package hotelm.repository

import hotelm.HotelmException
import hotelm.Room
import io.getquill.*
import io.getquill.context.qzio.ZioJdbcContext
import io.getquill.context.sql.idiom.SqlIdiom
import javax.sql.DataSource
import zio.Task
import zio.TaskLayer
import zio.ZIO

trait RoomRepository:

  def add(room: Room): Task[Room]

  def remove(number: String): Task[Option[Room]]

object RoomRepository:

  def apply(ctx: ZioJdbcContext[SqlIdiom, NamingStrategy], dataSourceLayer: TaskLayer[DataSource]): RoomRepository =
    new Default(ctx, dataSourceLayer)

  private class Default(ctx: ZioJdbcContext[SqlIdiom, NamingStrategy], dataSourceLayer: TaskLayer[DataSource])
      extends RoomRepository:

    import ctx.*

    override def add(room: Room): Task[Room] =
      for _ <-
          run(quote { query[Room].insertValue(lift(room)) })
            .provideLayer(dataSourceLayer)
            .mapError(HotelmException.UnableToInsertRoom("An unexpected error occurred while inserting a new room!", _))
            .filterOrFail(_ == 1)(HotelmException.UnableToInsertRoom(s"It was impossible to add room $room!"))
      yield room

    override def remove(number: String): Task[Option[Room]] =
      val select = quote(query[Room].filter(_.number == lift(number)))

      (for
        list   <- run(quote(select))
        result <- list match
                    case List(stored) => run(quote(select.delete)) as Some(stored)
                    case _            => ZIO.none
      yield result).provideLayer(dataSourceLayer)
