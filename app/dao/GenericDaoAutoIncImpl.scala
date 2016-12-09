package dao

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.db.slick._
import scala.concurrent.Future
import generated._
import generated.Tables._
import profile.api._

/**
  * Generic Strong DAO implementation
  */
abstract class GenericDaoAutoIncImpl[T <: Table[E] with IdentifyableTable[PK], E <: AutoIncEntity[PK, E], PK: BaseColumnType]
    (dbConfigProvider: DatabaseConfigProvider, tableQuery: TableQuery[T]) extends GenericDaoImpl[T, E, PK](dbConfigProvider, tableQuery)
      with GenericDaoAutoInc[T, E, PK] {
  import shapeless._
  import tag.@@

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  /**
    * Returns newly created entity with updated id
    * @param entity entity to create, input id is ignored
    * @return newly created entity with updated id
    */
  override def createAndFetch(entity: E)(implicit mkLens: MkFieldLens.Aux[E, Symbol @@ Witness.`"id"`.T, PK]): Future[Option[E]] = {
    val insertQuery = tableQuery returning tableQuery.map(_.id) into ((row, id) => row.copyWithNewId(id))
    db.run((insertQuery += entity).flatMap(row => findById(row.id)))
  }
}
