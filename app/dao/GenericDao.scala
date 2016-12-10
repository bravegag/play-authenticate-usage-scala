package dao

import play.api.db.slick._
import slick.driver.JdbcProfile
import scala.concurrent.{Await, Future}
import generated._
import generated.Tables._
import profile.api._
import slick.lifted.CanBeQueryCondition
import slick.profile.BasicProfile
import scala.concurrent.duration.Duration

/**
  * Generic DAO definition
  */
trait GenericDao[T <: Table[E] with IdentifyableTable[PK], E <: Entity[PK], PK] extends HasDatabaseConfigProvider[JdbcProfile] {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  /**
    * Returns the row count for this Model
    * @return the row count for this Model
    */
  def count(): Future[Int]

  //------------------------------------------------------------------------
  /**
    * Returns the matching entity for the given id
    * @param id identifier
    * @return the matching entity for the given id
    */
  def findById(id: PK): DBIO[Option[E]]

  //------------------------------------------------------------------------
  /**
    * Returns all entities in this model
    * @return all entities in this model
    */
  def findAll(): Future[Seq[E]]

  //------------------------------------------------------------------------
  /**
    * Returns entities that satisfy the filter expression.
    * @param expr input filter expression
    * @param wt
    * @tparam C
    * @return entities that satisfy the filter expression.
    */
  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Query[T, E, Seq]

  //------------------------------------------------------------------------
  /**
    * Creates (and forgets) a new entity, returns a unit future
    * @param entity entity to create, input id is ignored
    * @return returns a unit future
    */
  def create(entity: E): Future[Unit]

  //------------------------------------------------------------------------
  /**
    * Returns number of inserted entities
    * @param entities to be inserted
    * @return number of inserted entities
    */
  def create(entities: Seq[E]): Future[Unit]

  //------------------------------------------------------------------------
  /**
    * Updates the given entity and returns a Future
    * @param update Entity to update (by id)
    * @return returns a Future
    */
  def update(update: E): Future[Unit]

  //------------------------------------------------------------------------
  /**
    * Deletes the given entity by Id and returns a Future
    * @param id The Id to delete
    * @return returns a Future
    */
  def delete(id: PK): Future[Unit]
}

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

/**
  * Blocks the future until done implicitly
  */
object ExecHelper {
  import scala.concurrent.ExecutionContext.Implicits.global
  /**
    * Returns the result of executing the action and retrieving the Future result
    * @param action The action to be executed
    * @param db The db to run under implicit parameter
    * @tparam E Concrete Entity result type
    * @tparam P Concrete Profile type
    * @return the result of executing the action and retrieving the Future result
    */
  implicit def exec[E, P <: BasicProfile](action: DBIO[E])(implicit db: P#Backend#Database) = Await.result(db.run(action), Duration.Inf)

  /**
    * Returns the result of executing and waiting the given Future
    * @param f Future to execute and wait for
    * @tparam E concrete Entity result type
    * @return the result of executing and waiting the given Future
    */
  implicit def exec[E](f: Future[E]) = Await.result(f, Duration.Inf)
}