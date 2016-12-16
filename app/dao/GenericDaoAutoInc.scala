package dao

import scala.concurrent._
import generated._
import generated.Tables._
import profile.api._

/**
  * Generic DAO strong entity definition
  */
trait GenericDaoAutoInc[T <: Table[E] with IdentifyableTable[PK], E <: AutoIncEntity[PK, E], PK] extends GenericDao[T, E, PK] {
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
  def createAndFetch(entity: E)(implicit mkLens: MkFieldLens.Aux[E, Symbol @@ Witness.`"id"`.T, PK]): Future[Option[E]]
}