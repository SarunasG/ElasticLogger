package helpers

import java.text.SimpleDateFormat
import java.util.Date

/**
  * Created by BC0414 on 10/05/17.
  */
object DateConverter {

  def returnCurrentDateFromMillis(timestamp: Long): String = {

    val date = new Date(timestamp)
    val formatter = new SimpleDateFormat("yyyy-MM-dd")
    formatter.format(date)


  }


}
