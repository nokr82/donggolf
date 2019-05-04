package donggolf.android.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.EventsAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.dlg_event.*
import org.json.JSONObject


class DlgEventActivity : RootActivity() {

    lateinit var context: Context
    private var progressDialog: ProgressDialog? = null

    var member_id = -1

    var event_id = -1
    var participation = "N"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_event)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        this.context = this
        progressDialog = ProgressDialog(context, R.style.CustomProgressBar)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)

        member_id = PrefUtils.getIntPreference(context, "member_id")

        event_id = intent.getIntExtra("event_id", -1)
        participation = intent.getStringExtra("participation")
        val myNumber1 = intent.getStringExtra("myNumber1")
        val myNumber2 = intent.getStringExtra("myNumber2")

        number1ET.setText(myNumber1)
        number2ET.setText(myNumber2)

        if (participation == "Y") {
            titleTV.text = "이벤트 참여번호 수정"
        } else {
            titleTV.text = "이벤트 참여번호 등록"
        }

        finishLL.setOnClickListener {
            finish()
        }

        doneTV.setOnClickListener {

            errorTV.visibility = View.GONE

            val number1 = Utils.getInt(number1ET)
            val number2 = Utils.getInt(number2ET)

            if (number1 < 1 || number2 < 1 || number1 > 64 || number2 > 64) {
                errorTV.text = "1 ~ 64번 사이에 숫자를 입력해 주세요"
                errorTV.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if(number1 == number2) {
                errorTV.text = "중복되지 않게 숫자를 입력해 주세요"
                errorTV.visibility = View.VISIBLE
                return@setOnClickListener
            }

            var number1_str = number1.toString()
            var number2_str = number2.toString()

            if (number1 < 10) {
                number1_str = "0$number1_str"
            }

            if (number2 < 10) {
                number2_str = "0$number2_str"
            }

            eventParticipation(number1_str, number2_str)
        }

    }

    fun eventParticipation(number1: String, number2: String) {
        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("event_id", event_id)
        params.put("number1", number1)
        params.put("number2", number2)

        EventsAction.participation(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                val result = response!!.getString("result")

                if (result == "ok") {

                    Utils.hideKeyboard(context)

                    var intent = Intent()
                    intent.putExtra("number", Utils.getString(response, "number"))
                    intent.putExtra("eventMembers", Utils.getInt(response, "eventMembers"))
                    setResult(Activity.RESULT_OK, intent)

                    Toast.makeText(context, "이벤트 참여가 완료되었습니다.", Toast.LENGTH_LONG).show()

                    finish()

                } else {

                }

            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }

        })

    }


}
