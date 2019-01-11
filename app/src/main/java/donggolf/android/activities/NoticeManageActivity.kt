package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_notice_manage.*
import org.json.JSONException
import org.json.JSONObject

class NoticeManageActivity : RootActivity() {

    lateinit var context: Context
    var room_id = ""
    var top_division = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_manage)

        context = this

        room_id = intent.getStringExtra("room_id")


        get_announcement()

        resetTV.setOnClickListener {
            founderTV.setText("")
            radio_public.setImageResource(R.drawable.btn_radio_on)
            radio_secret.setImageResource(R.drawable.btn_radio_off)
            top_division = 1
        }

        publicLL.setOnClickListener {
            radio_public.setImageResource(R.drawable.btn_radio_on)
            radio_secret.setImageResource(R.drawable.btn_radio_off)
            top_division = 1
        }

        secretLL.setOnClickListener {
            radio_public.setImageResource(R.drawable.btn_radio_off)
            radio_secret.setImageResource(R.drawable.btn_radio_on)
            top_division = 2
        }

        finishLL.setOnClickListener {
            finish()
        }

        saveTV.setOnClickListener {
            set_notice()
        }


    }

    fun get_announcement(){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        ChattingAction.get_announcement(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val item = response.getJSONObject("announcement")
                        val Announcement = item.getJSONObject("Announcement")
                        val Member = item.getJSONObject("Member")
                        val Chatroom = item.getJSONObject("Chatroom")

                        val nick = Utils.getString(Member,"nick")
                        nickTV.setText(nick)

                        val readcount = Utils.getString(Announcement,"readcount")
                        readcountTV.setText(readcount + "명 읽음")
                        val content = Utils.getString(Announcement,"content")
                        founderTV.setText(content)
                        noticeTV.setText(content)
                        val top_yn = Utils.getString(Announcement,"top_yn")
                        val created = Utils.getString(Announcement,"created")
                        val since = Utils.since(created)
                        createdTV.setText(since)

                        if (top_yn == "Y"){
                            radio_public.setImageResource(R.drawable.btn_radio_on)
                            radio_secret.setImageResource(R.drawable.btn_radio_off)
                            top_division = 1
                        } else {
                            radio_public.setImageResource(R.drawable.btn_radio_off)
                            radio_secret.setImageResource(R.drawable.btn_radio_on)
                            top_division = 2
                        }

                        if (PrefUtils.getIntPreference(context,"member_id") != Utils.getInt(Chatroom,"member_id")){
                            chatvisibleLL.visibility = View.GONE
                            resetTV.visibility = View.GONE
                            founderLL.visibility = View.GONE
                        } else {
                            noticeLL.visibility = View.GONE
                            founderLL.visibility = View.VISIBLE
                        }

                        var image = Config.url + Utils.getString(Member, "profile_img")
                        ImageLoader.getInstance().displayImage(image, profileIV, Utils.UILoptionsUserProfile)





                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }
        })
    }

    fun set_notice(){
        val params = RequestParams()
        params.put("room_id", room_id)
        val notice = founderTV.text.toString()
        if (notice == null || notice == ""){
            Toast.makeText(context, "빈칸은 입력하실 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        params.put("notice", notice)
        if (top_division == 1){
            params.put("top_yn", "Y")
        } else {
            params.put("top_yn", "N")
        }

        ChattingAction.set_notice(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        var intent = Intent()
                        intent.putExtra("reset","reset")
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
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
