package donggolf.android.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.EventsAction
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_event_detail.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class EventDetailActivity : RootActivity() {

    private lateinit var context: Context
    private var progressDialog: ProgressDialog? = null

    val PARTICIPATION_EVENT = 100

    var event_id = -1
    var member_id = -1

    var finish = ""
    var participation = ""
    var participation_possible = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        context = this

        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

        member_id = PrefUtils.getIntPreference(context, "member_id")

        event_id = intent.getIntExtra("event_id", -1)

        finishLL.setOnClickListener {
            finish()
        }

        numberTV.setOnClickListener {

            if (finish == "Y") {
                Utils.alert(context, "마감된 이벤트입니다.")
            } else {
                if (participation_possible != "Y") {
                    Utils.alert(context, "이벤트 참여는 내정보에서 프로필사진과 우리동네 설정을 하셔야 합니다.")
                } else {
                    var intent = Intent(context, DlgEventActivity::class.java)
                    intent.putExtra("event_id", event_id)
                    intent.putExtra("participation", participation)
                    startActivityForResult(intent, PARTICIPATION_EVENT)
                }
            }

        }

        membersLL.setOnClickListener {
            var intent = Intent(context, EventMembersActivity::class.java)
            intent.putExtra("event_id", event_id)
            intent.putExtra("finish", finish)
            startActivity(intent)
        }

        resultLL.setOnClickListener {
            var intent = Intent(context, EventMembersActivity::class.java)
            intent.putExtra("event_id", event_id)
            intent.putExtra("finish", finish)
            startActivity(intent)
        }

        loadData()

    }

    fun loadData() {
        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("event_id", event_id)

        EventsAction.detail(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                val result = response!!.getString("result")

                if (result == "ok") {

                    val event = response.getJSONObject("Event")

                    finish = Utils.getString(response, "finish")
                    participation = Utils.getString(response, "participation")
                    participation_possible = Utils.getString(response, "participation_possible")

                    if (finish == "Y") {
                        membersLL.visibility = View.GONE
                        resultLL.visibility = View.VISIBLE


                        val number1 = Utils.getString(event, "number1")
                        val number2 = Utils.getString(event, "number2")

                        if (number1.length == 2 && number2.length == 2) {
                            num1TV.text = number1.substring(0, 1)
                            num2TV.text = number1.substring(1, 2)
                            num3TV.text = number2.substring(0, 1)
                            num4TV.text = number2.substring(1, 2)
                        }

                        leftTimeTV.text = "마감"

                    } else {
                        membersLL.visibility = View.VISIBLE
                        resultLL.visibility = View.GONE

                        leftTimeTV.dest_date_time = Utils.getInt(response, "timer")
                        leftTimeTV.start()
                    }

                    membersTV.text = "${Utils.getInt(response, "eventMembers")}명 참여자 보기"

                    if (participation == "Y") {
                        val eventMember = response.getJSONObject("EventMember")

                        numberTV.text = Utils.getString(eventMember, "number1") + Utils.getString(eventMember, "number2")
                    } else {
                        numberTV.text = "참여하기"
                    }

                    val title = Utils.getString(event, "title")
                    val created = Utils.getString(event, "created")

                    val created_str = SimpleDateFormat("yyyy년MM월dd일 a HH:mm", Locale.KOREA).format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(created))

                    titleTV.text = title
                    dateTV.text = created_str

                    webWV.loadUrl( Config.url + "/events/view?id=${Utils.getInt(event, "id")}")

                } else {

                }

            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }

        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PARTICIPATION_EVENT -> {

                    if (data != null) {
                        val number = data.getStringExtra("number")
                        val eventMembers = data.getIntExtra("eventMembers", 0)

                        if (number != "" && number != null) {
                            numberTV.text = number
                            participation = "Y"
                        }

                        membersTV.text = "${eventMembers}명 참여자 보기"

                    }
                }
            }
        }

    }

}
