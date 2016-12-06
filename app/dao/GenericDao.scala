package dao

import play.api.db.slick._
import slick.driver.JdbcProfile

import scala.concurrent.{Await, Future}
import generated._
import generated.Tables._
import profile.api._
import slick.lifted.CanBeQueryCondition

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
  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Seq[E]]

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
trait GenericDaoAutoInc[T <: Table[E] with IdentifyableTable[PK], E <: AutoIncEntity[PK], PK] extends GenericDao[T, E, PK] {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  /**
    * Returns newly created entity with updated id
    * @param entity entity to create, input id is ignored
    * @return newly created entity with updated id
    */
  def createAndFetch(entity: E): Future[Option[E]]
}

/**
  * Blocks the future until done implicitly
  */
object ExecHelper {
  implicit def exec[A](f: Future[A]) = Await.result(f, Duration.Inf)
}