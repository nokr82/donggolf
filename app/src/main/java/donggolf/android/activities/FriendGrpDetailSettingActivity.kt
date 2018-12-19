package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MateAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.models.FriendCategory
import kotlinx.android.synthetic.main.activity_friend_grp_detail_setting.*
import kotlinx.android.synthetic.main.activity_notice2.view.*
import kotlinx.android.synthetic.main.dialog_add_category.view.*
import org.json.JSONObject

class FriendGrpDetailSettingActivity : RootActivity() {

    lateinit var context: Context

    var entireOnOff = false
    var chatOnOff = false
    var newPostOnOff = false
    var loginOnOff = false
    var frdOpenOnOff = false
    var marketAlarmOnOff = false

    var category_id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_grp_detail_setting)

        context = this

        var groupName = intent.getStringExtra("groupTitle")
        titleFrdCateTV.text = "$groupName 설정"
        category_id = intent.getIntExtra("cate_id", 0)

        changeCategNameLL.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null) //사용자 정의 다이얼로그 xml 붙이기
            dialogView.dlgTitle.text = "카테고리 이름변경"
            dialogView.categoryTitleET.addTextChangedListener(object : TextWatcher {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // 입력되는 텍스트에 변화가 있을 때 호출된다.
                }

                override fun afterTextChanged(count: Editable) {
                    // 입력이 끝났을 때 호출된다.

                    dialogView.leftWords.text = Integer.toString(dialogView.categoryTitleET.text.toString().length)
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    // 입력하기 전에 호출된다.
                }
            })
            builder.setView(dialogView)
                    .setPositiveButton("확인") { dialog, id ->
                        //방제 바꾸는 곳
                        var title = dialogView.categoryTitleET.text.toString()
                        titleFrdCateTV.text = "$title 설정"
                        //DB의 카테고리 이름 변경
                        updateCategory(category_id, title)
                    }
                    .show()
            //val alert = builder.show() //builder를 끄기 위해서는 alertDialog에 이식해줘야 함

            dialogView.btn_title_clear.setOnClickListener {
                dialogView.categoryTitleET.setText("")
            }
        }

        btn_back.setOnClickListener {
            finish()
        }

        //전체
        frdGroupSetAlarm.setOnClickListener {
            if (!entireOnOff){
                frdGroupSetAlarm.setImageResource(R.drawable.btn_alarm_on)

                btn_allow_chat.setImageResource(R.drawable.btn_alarm_on)
                btn_newPostAlarm.setImageResource(R.drawable.btn_alarm_on)
                loginAlarmSetting.setImageResource(R.drawable.btn_alarm_on)
                myFriendOpenSetting.setImageResource(R.drawable.btn_alarm_on)
                marketNewWritingAlarm.setImageResource(R.drawable.btn_alarm_on)

                entireOnOff = !entireOnOff

            } else {
                frdGroupSetAlarm.setImageResource(R.drawable.btn_alarm_off)

                btn_allow_chat.setImageResource(R.drawable.btn_alarm_off)
                btn_newPostAlarm.setImageResource(R.drawable.btn_alarm_off)
                loginAlarmSetting.setImageResource(R.drawable.btn_alarm_off)
                myFriendOpenSetting.setImageResource(R.drawable.btn_alarm_off)
                marketNewWritingAlarm.setImageResource(R.drawable.btn_alarm_off)
            }

        }

        btn_allow_chat.setOnClickListener {
            if (!chatOnOff){
                btn_allow_chat.setImageResource(R.drawable.btn_alarm_on)
                chatOnOff = true
            } else {
                btn_allow_chat.setImageResource(R.drawable.btn_alarm_off)
                chatOnOff = false
            }
        }

        btn_newPostAlarm.setOnClickListener {
            if (!newPostOnOff){
                btn_newPostAlarm.setImageResource(R.drawable.btn_alarm_on)
                newPostOnOff = true
            } else {
                btn_newPostAlarm.setImageResource(R.drawable.btn_alarm_off)
                newPostOnOff = false
            }
        }

        loginAlarmSetting.setOnClickListener {
            if (!loginOnOff){
                loginAlarmSetting.setImageResource(R.drawable.btn_alarm_on)
                loginOnOff = true
            } else {
                loginAlarmSetting.setImageResource(R.drawable.btn_alarm_off)
                loginOnOff = false
            }
        }

        myFriendOpenSetting.setOnClickListener {
            if (!frdOpenOnOff){
                myFriendOpenSetting.setImageResource(R.drawable.btn_alarm_on)
                frdOpenOnOff = true
            } else {
                myFriendOpenSetting.setImageResource(R.drawable.btn_alarm_off)
                frdOpenOnOff = false
            }
        }

        marketNewWritingAlarm.setOnClickListener {
            if (!marketAlarmOnOff){
                marketNewWritingAlarm.setImageResource(R.drawable.btn_alarm_on)
                marketAlarmOnOff = true
            } else {
                marketNewWritingAlarm.setImageResource(R.drawable.btn_alarm_off)
                marketAlarmOnOff = false
            }
        }

    }

    fun updateCategory(categoryID : Int, title : String) {
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("category",title)
        params.put("category_id",categoryID)

        MateAction.updateCategory(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }
        })
    }
}
