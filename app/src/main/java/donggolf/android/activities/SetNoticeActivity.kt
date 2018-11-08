package donggolf.android.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import donggolf.android.R
import donggolf.android.R.drawable.btn_alarm_off
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_set_notice.*

class SetNoticeActivity : RootActivity() {

    lateinit var btn_alarm_off:ImageView

    var alarm1 = false
    var alarm2 = false
    var alarm3 = false
    var alarm4 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_notice)

        finishLV.setOnClickListener {
            finish()
        }

        switchAll.setOnClickListener {
            if (alarm1 == false) {
                switchAll.setImageResource(R.drawable.btn_alarm_on)
                alarm1 = true
            } else {
                switchAll.setImageResource(R.drawable.btn_alarm_off)
                alarm1 = false
            }
        }

        switchReply.setOnClickListener {
            if (alarm2 == false) {
                switchReply.setImageResource(R.drawable.btn_alarm_on)
                alarm2 = true
            } else {
                switchReply.setImageResource(R.drawable.btn_alarm_off)
                alarm2 = false
            }
        }

        switchChat.setOnClickListener {
            if (alarm3 == false) {
                switchChat.setImageResource(R.drawable.btn_alarm_on)
                alarm3 = true
            } else {
                switchChat.setImageResource(R.drawable.btn_alarm_off)
                alarm3 = false
            }
        }

        switchReq.setOnClickListener {
            if (alarm4 == false) {
                switchReq.setImageResource(R.drawable.btn_alarm_on)
                alarm4 = true
            } else {
                switchReq.setImageResource(R.drawable.btn_alarm_off)
                alarm4 = false
            }
        }


    }
}
