package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MateAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.FriendCategory
import kotlinx.android.synthetic.main.activity_friend_grp_detail_setting.*
import kotlinx.android.synthetic.main.activity_notice2.view.*
import kotlinx.android.synthetic.main.dialog_add_category.view.*
import org.json.JSONArray
import org.json.JSONObject

class FriendGrpDetailSettingActivity : RootActivity() {

    lateinit var context: Context
    private var progressDialog: ProgressDialog? = null

    var entireOnOff = false

    var new_post = ""
    var chat = ""
    var login = ""
    var open_mate = ""
    var new_market = ""
    var title = ""

    var category_id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_grp_detail_setting)

        this.context = this
        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

        var groupName = intent.getStringExtra("groupTitle")
//        titleFrdCateTV.text = "$groupName 설정"
        category_id = intent.getIntExtra("cate_id", 0)

        changeCategNameLL.setOnClickListener {
            if (title == "1촌 골퍼"){
                Toast.makeText(context,"1촌 골퍼는 카테고리명을 변경하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null) //사용자 정의 다이얼로그 xml 붙이기
            val alert = builder.show() //builder를 끄기 위해서는 alertDialog에 이식해줘야 함
            dialogView.dlgTitle.text = "카테고리 이름변경"
            dialogView.categoryTitleET.setText(title)
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
                        title = dialogView.categoryTitleET.text.toString()

                        //DB의 카테고리 이름 변경
                        updateCategory("title")
                    }
                    .show()
            dialogView.btn_title_clear.setOnClickListener {
                alert.dismiss()
            }
        }
        btn_back.setOnClickListener {
            finish()
        }

        //전체
        frdGroupSetAlarm.setOnClickListener {

            if (!entireOnOff){
                new_post = "y"
                chat = "y"
                login = "y"
                open_mate = "y"
                new_market = "y"
            } else {
                new_post = "n"
                chat = "n"
                login = "n"
                open_mate = "n"
                new_market = "n"
            }

            updateCategory("alarm")

        }

        btn_allow_chat.setOnClickListener {
            if(chat == "y") {
                chat = "n"
            } else {
                chat = "y"
            }

            updateCategory("alarm")
        }

        btn_newPostAlarm.setOnClickListener {
            if(new_post == "y") {
                new_post = "n"
            } else {
                new_post = "y"
            }

            updateCategory("alarm")
        }

        loginAlarmSetting.setOnClickListener {
            if(login == "y") {
                login = "n"
            } else {
                login = "y"
            }

            updateCategory("alarm")
        }

        myFriendOpenSetting.setOnClickListener {
            if(open_mate == "y") {
                open_mate = "n"
            } else {
                open_mate = "y"
            }

            updateCategory("alarm")
        }

        marketNewWritingAlarm.setOnClickListener {
            if(new_market == "y") {
                new_market = "n"
            } else {
                new_market = "y"
            }

            updateCategory("alarm")
        }

        loadData()

    }

    fun loadData() {
        val params = RequestParams()
        params.put("category_id",category_id)

        if (category_id == -1){
            params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        }

        MateAction.category_detail(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                var result = response!!.getString("result");

                if("ok" == result) {

                    val category = response.getJSONObject("category")

                    title = Utils.getString(category, "category")

                    titleFrdCateTV.text = title + " 설정"

                    new_post = Utils.getString(category, "new_post")
                    chat = Utils.getString(category, "chat")
                    login = Utils.getString(category, "login")
                    open_mate = Utils.getString(category, "open_mate")
                    new_market = Utils.getString(category, "new_market")

                    setSwitchView()

                } else {

                }

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

//                System.out.println(responseString);

                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONArray?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onStart() {
                // show dialog
                if (progressDialog != null) {

                    progressDialog!!.show()
                }
            }

            override fun onFinish() {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
            }

            private fun error() {
                Utils.alert(context, "조회중 장애가 발생하였습니다.")
            }

        })
    }

    fun setSwitchView() {

        if("y" == new_post) {
            btn_newPostAlarm.setImageResource(R.drawable.btn_alarm_on)
        } else {
            btn_newPostAlarm.setImageResource(R.drawable.btn_alarm_off)
        }

        if("y" == chat) {
            btn_allow_chat.setImageResource(R.drawable.btn_alarm_on)
        } else {
            btn_allow_chat.setImageResource(R.drawable.btn_alarm_off)
        }

        if("y" == login) {
            loginAlarmSetting.setImageResource(R.drawable.btn_alarm_on)
        } else {
            loginAlarmSetting.setImageResource(R.drawable.btn_alarm_off)
        }

        if("y" == open_mate) {
            myFriendOpenSetting.setImageResource(R.drawable.btn_alarm_on)
        } else {
            myFriendOpenSetting.setImageResource(R.drawable.btn_alarm_off)
        }

        if("y" == new_market) {
            marketNewWritingAlarm.setImageResource(R.drawable.btn_alarm_on)
        } else {
            marketNewWritingAlarm.setImageResource(R.drawable.btn_alarm_off)
        }

        if("y" == new_post && "y" == chat && "y" == login && "y" == open_mate && "y" == new_market) {
            entireOnOff = true
            frdGroupSetAlarm.setImageResource(R.drawable.btn_alarm_on)
        } else {
            entireOnOff = false
            frdGroupSetAlarm.setImageResource(R.drawable.btn_alarm_off)
        }
    }

    fun updateCategory(type : String) {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("category_id",category_id)

        if(type == "title") {
            params.put("category", title)
        } else {
            params.put("new_post", new_post)
            params.put("chat", chat)
            params.put("login", login)
            params.put("open_mate", open_mate)
            params.put("new_market", new_market)

            // println("-----new_post : $new_post chat : $chat login : $login open_mate : $open_mate new_market $new_market")
        }

        MateAction.updateCategory(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                var result = response!!.getString("result");

                if("ok" == result) {
                    val category = response.getJSONObject("category")

                    title = Utils.getString(category, "category")

                    titleFrdCateTV.text = title + " 설정"

                    new_post = Utils.getString(category, "new_post")
                    chat = Utils.getString(category, "chat")
                    login = Utils.getString(category, "login")
                    open_mate = Utils.getString(category, "open_mate")
                    new_market = Utils.getString(category, "new_market")

                    setSwitchView()

                    var intent = Intent()
                    intent.putExtra("title", title)
                    setResult(Activity.RESULT_OK, intent)

                }

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

//                System.out.println(responseString);

                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONArray?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onStart() {
                // show dialog
                if (progressDialog != null) {

                    progressDialog!!.show()
                }
            }

            override fun onFinish() {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
            }

            private fun error() {
                Utils.alert(context, "조회중 장애가 발생하였습니다.")
            }

        })
    }

}
