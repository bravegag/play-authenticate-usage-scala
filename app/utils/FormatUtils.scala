package utils

import java.text.SimpleDateFormat
import java.util.Date

import scala.util.DynamicVariable

object FormatUtils {
  val DateFormat = new DynamicVariable[SimpleDateFormat](null)
  def formatTimestamp(timestamp: Long): String = {
    val dateFormat = DateFormat.value match {
      case null => DateFormat.value_=(new SimpleDateFormat("yyyy-dd-MM HH:mm:ss")); DateFormat.value
      case dateFormat: SimpleDateFormat => dateFormat
    }
    dateFormat.format(new Date(timestamp))
  }
}
