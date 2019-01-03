package donggolf.android.activities

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.R.drawable.btn_alarm_off
import donggolf.android.actions.MemberAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_set_notice.*
import org.json.JSONArray
import org.json.JSONObject

class SetNoticeActivity : RootActivity() {

    private lateinit var context: Context
    private var progressDialog: ProgressDialog? = null

    var chatting_yn = ""
    var comments_yn = ""
    var friend_yn = ""

    var all = false;

    var member_id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_notice)

        context = this
        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

        member_id = PrefUtils.getIntPreference(context, "member_id")

        finishLV.setOnClickListener {
            finish()
        }

        allLL.setOnClickListener {
            if (!all) {
                comments_yn = "Y"
                chatting_yn = "Y"
                friend_yn = "Y"
            } else {
                comments_yn = "N"
                chatting_yn = "N"
                friend_yn = "N"
            }

            updateInfo()
        }

        commentsLL.setOnClickListener {

            if (comments_yn == "N") {
                comments_yn = "Y"
            } else {
                comments_yn = "N"
            }

            updateInfo()
        }

        chattingLL.setOnClickListener {
            if (chatting_yn == "N") {
                chatting_yn = "Y"
            } else {
                chatting_yn = "N"
            }
            updateInfo()
        }

        friendLL.setOnClickListener {
            if (friend_yn == "N") {
                friend_yn = "Y"
            } else {
                friend_yn = "N"
            }

            updateInfo()
        }

        loadData()

    }

    fun loadData() {

        val params = RequestParams()
        params.put("member_id", member_id)

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                val result = response!!.getString("result")

                if (result == "ok") {

                    val member = response.getJSONObject("Member")

                    comments_yn = Utils.getString(member, "push_comments_yn")
                    chatting_yn = Utils.getString(member, "push_chatting_yn")
                    friend_yn = Utils.getString(member, "push_friend_yn")

                    if(comments_yn == "Y") {
                        switchReply.setImageResource(R.drawable.btn_alarm_on)
                    } else {
                        switchReply.setImageResource(R.drawable.btn_alarm_off)
                    }

                    if(chatting_yn == "Y") {
                        switchChat.setImageResource(R.drawable.btn_alarm_on)
                    } else {
                        switchChat.setImageResource(R.drawable.btn_alarm_off)
                    }

                    if(friend_yn == "Y") {
                        switchReq.setImageResource(R.drawable.btn_alarm_on)
                    } else {
                        switchReq.setImageResource(R.drawable.btn_alarm_off)
                    }

                    if(friend_yn == "Y" && comments_yn == "Y" && chatting_yn == "Y") {
                        all = true
                        switchAll.setImageResource(R.drawable.btn_alarm_on)
                    } else {
                        all = false
                        switchAll.setImageResource(R.drawable.btn_alarm_off)
                    }

                } else {

                }

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                // System.out.println(responseString);

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

    fun updateInfo() {

        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("push_comments_yn", comments_yn)
        params.put("push_chatting_yn", chatting_yn)
        params.put("push_friend_yn", friend_yn)

        MemberAction.m_update_info(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                val result = response!!.getString("result")

                if (result == "ok") {

                    val member = response.getJSONObject("member")

                    comments_yn = Utils.getString(member, "push_comments_yn")
                    chatting_yn = Utils.getString(member, "push_chatting_yn")
                    friend_yn = Utils.getString(member, "push_friend_yn")

                    if(comments_yn == "Y") {
                        switchReply.setImageResource(R.drawable.btn_alarm_on)
                    } else {
                        switchReply.setImageResource(R.drawable.btn_alarm_off)
                    }

                    if(chatting_yn == "Y") {
                        switchChat.setImageResource(R.drawable.btn_alarm_on)
                    } else {
                        switchChat.setImageResource(R.drawable.btn_alarm_off)
                    }

                    if(friend_yn == "Y") {
                        switchReq.setImageResource(R.drawable.btn_alarm_on)
                    } else {
                        switchReq.setImageResource(R.drawable.btn_alarm_off)
                    }

                    if(friend_yn == "Y" && comments_yn == "Y" && chatting_yn == "Y") {
                        all = true
                        switchAll.setImageResource(R.drawable.btn_alarm_on)
                    } else {
                        all = false
                        switchAll.setImageResource(R.drawable.btn_alarm_off)
                    }

                } else {

                }

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                // System.out.println(responseString);

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
