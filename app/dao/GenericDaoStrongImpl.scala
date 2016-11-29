package dao

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.db.slick._
import slick.driver.JdbcProfile
import scala.concurrent.Future
import generated.Tables._
import profile.api._

/**
  * Generic Strong DAO implementation
  */
abstract class GenericDaoStrongImpl [T <: Table[E] with IdentifyableTable[PK], E <: StrongEntity[PK], PK: BaseColumnType]
    (dbConfigProvider: DatabaseConfigProvider, tableQuery: TableQuery[T]) extends GenericDaoImpl[T, E, PK](dbConfigProvider, tableQuery)
      with GenericDaoStrong[T, E, PK] {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  /**
    * Returns newly created entity with updated id
    * @param entity entity to create, input id is ignored
    * @return newly created entity with updated id
    */
  override def createAndFetchWithNewId(entity: E): Future[Entity[PK]] = {
    val insertQuery = tableQuery returning tableQuery.map(_.id) into ((row, id) => row.copyWithNewId(id))
    val action = insertQuery += entity
    db.run(action)
  }
}
