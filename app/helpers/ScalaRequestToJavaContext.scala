package helpers

import java.util.concurrent.ConcurrentHashMap

import play.api.Logger
import play.mvc.Http

object ScalaRequestToJavaContext {
  private val map = new ConcurrentHashMap[Long, Http.Context]()

  def put(id: Long, context: Http.Context): Http.Context = {
    Logger.info("Putting request by id: " + id)
    map.put(id, context)
  }

  def get(id: Long): Http.Context = {
    Logger.info("Getting request by id: " + id)
    map.get(id)
  }

  def delete(id: Long): Http.Context = {
    Logger.info("Deleting request by id: " + id)
    map.remove(id)
  }
}
