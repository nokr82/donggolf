package donggolf.android.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.BuildConfig
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
    var number1 = -1
    var number2 = -1
    var myNumber1 = ""
    var myNumber2 = ""
    private var open_yn = "N"
    private var is_latest_version = "N"

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
                } else if (is_latest_version == "N") {
                    Utils.alert(context, "동.골 최신버전으로 다운받으신후 이벤트에 참여해주세요.", "버전업 하러가기") {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://play.google.com/store/apps/details?id=donggolf.android")
                            setPackage("com.android.vending")
                        }
                        startActivity(intent)
                    }
                } else {
                    var intent = Intent(context, DlgEventActivity::class.java)
                    intent.putExtra("event_id", event_id)
                    intent.putExtra("participation", participation)
                    intent.putExtra("myNumber1", myNumber1)
                    intent.putExtra("myNumber2", myNumber2)
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
            if(open_yn == "N") {
                Utils.alert(context, "추첨 결과 발표 전입니다.\n잠시 후에 확인해 주세요.")
            } else {
                val intent = Intent(context, EventMembersActivity::class.java)
                intent.putExtra("event_id", event_id)
                intent.putExtra("finish", finish)
                intent.putExtra("numbers", "$number1,$number2")
                startActivity(intent)
            }

        }

        loadData()

    }

    fun loadData() {

        val my_version = BuildConfig.VERSION_NAME

        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("event_id", event_id)
        params.put("my_version", my_version)

        EventsAction.detail(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                val result = response!!.getString("result")

                if (result == "ok") {

                    val event = response.getJSONObject("Event")

                    webWV.loadUrl( Config.url + "/events/view?id=${Utils.getInt(event, "id")}")

                    finish = Utils.getString(response, "finish")
                    participation = Utils.getString(response, "participation")
                    participation_possible = Utils.getString(response, "participation_possible")
                    is_latest_version = Utils.getString(response, "is_latest_version")

                    if (finish == "Y") {
                        membersLL.visibility = View.GONE
                        resultLL.visibility = View.VISIBLE

                        numberTV.visibility = View.GONE

                        number1 = Utils.getInt(event, "number1", -1)
                        number2 = Utils.getInt(event, "number2", -1)
                        open_yn = Utils.getString(event, "open_yn")

                        /*
                        if (number1.length == 2 && number2.length == 2) {
                            num1TV.text = number1.substring(0, 1)
                            num2TV.text = number1.substring(1, 2)
                            num3TV.text = number2.substring(0, 1)
                            num4TV.text = number2.substring(1, 2)
                        }
                        */

                        leftTimeTV.text = "마감"

                    } else {
                        membersLL.visibility = View.VISIBLE
                        resultLL.visibility = View.GONE

                        numberTV.visibility = View.VISIBLE

                        val dest_date_time = Utils.getInt(response, "timer")
                        leftTimeTV.text = "추첨 : ${Utils.dateString2(context, dest_date_time)}전"
                        // leftTimeTV.start()
                    }

                    val eventMemberCount = Utils.getInt(response, "eventMemberCount")
                    if (eventMemberCount > 99) {
                        membersTV.text = "${eventMemberCount}+명 참여자 보기"
                    } else {
                        membersTV.text = "${eventMemberCount}명 참여자 보기"
                    }

                    if (participation == "Y") {
                        val eventMember = response.getJSONObject("EventMember")

                        myNumber1 = Utils.getString(eventMember, "number1")
                        myNumber2 = Utils.getString(eventMember, "number2")

                        numberTV.text = myNumber1 + myNumber2
                    } else {
                        numberTV.text = "참여하기"
                    }

                    val title = Utils.getString(event, "title")
                    val created = Utils.getString(event, "created")

                    val created_str = SimpleDateFormat("yyyy년MM월dd일 a HH:mm", Locale.KOREA).format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(created))

                    titleTV.text = title
                    dateTV.text = created_str

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
