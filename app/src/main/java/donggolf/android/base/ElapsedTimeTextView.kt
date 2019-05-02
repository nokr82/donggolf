package donggolf.android.base

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import kotlin.concurrent.schedule
import android.os.Looper
import android.os.Handler
import java.util.*


class ElapsedTimeTextView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {

    var type:Int = 1
    var dest_date_time:Int = 0

    fun start() {
        Timer().schedule(0, 1000) {
            if(dest_date_time > 0) {

                val handler = Handler(Looper.getMainLooper())
                handler.post(Runnable {
                    dest_date_time = dest_date_time - 1

                    text = "추첨 : " + Utils.dateString2(context, dest_date_time) + "전"
                })

            }
        }
    }

    private fun convertFromDuration(timeInSeconds: Long): TimeInHours {
        var time = timeInSeconds / 1000
        val hours = time / 3600
        time %= 3600
        val minutes = time / 60
        time %= 60
        val seconds = time
        return TimeInHours(hours.toInt(), minutes.toInt(), seconds.toInt())
    }

    class TimeInHours(val hours: Int, val minutes: Int, val seconds: Int) {
    }
}