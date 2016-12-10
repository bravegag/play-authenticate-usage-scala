package utils

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.DynamicVariable

object FormatUtils {
  val DateFormat = new DynamicVariable[Option[SimpleDateFormat]](None)
  def formatTimestamp(timestamp: Long): String = {
    DateFormat.value match {
      case None => DateFormat.value_=(Some(new SimpleDateFormat("yyyy-dd-MM HH:mm:ss")))
    }
    DateFormat.value.map(_.format(new Date(timestamp))).get
  }
}
