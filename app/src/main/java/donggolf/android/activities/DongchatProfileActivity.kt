package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.adapters.FullScreenImageAdapter
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_chat_detail.*
import kotlinx.android.synthetic.main.activity_dongchat_profile.*
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList


class DongchatProfileActivity : RootActivity() {

    lateinit var context: Context

    var room_id = ""

    var Image_path = ArrayList<String>()

    private lateinit var backgroundAdapter: FullScreenImageAdapter

    var founder_id = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dongchat_profile)

        context = this

        joinDongChatRL.setOnClickListener {
            val intent = Intent(context, DongChatDetailActivity::class.java)
            intent.putExtra("room_id",room_id)
            startActivity(intent)

        }

        room_id = intent.getStringExtra("room_id")

        detail_chatting()

        backgroundAdapter = FullScreenImageAdapter(this@DongchatProfileActivity, Image_path)
        backgroundVP.adapter = backgroundAdapter


    }
    fun detail_chatting(){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        ChattingAction.detail_chatting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    if (Image_path != null){
                        Image_path.clear()
                    }

                    val members = response!!.getJSONArray("chatmember")
                    if (members != null && members.length() > 0){
                        for (i in 0 until members.length()){
                            val item = members.get(i) as JSONObject
                            val chatmember = item.getJSONObject("Chatmember")
                            val chatroom = item.getJSONObject("Chatroom")
                            val memberinfo = item.getJSONObject("Member")
                            val title = Utils.getString(chatroom,"title")
                            val visible = Utils.getString(chatroom,"visible")
                            val introduce = Utils.getString(chatroom,"introduce")
                            val created = Utils.getString(chatroom,"created")
                            val intro = Utils.getString(chatroom,"intro")
                            val background = Utils.getString(chatroom,"background")
                            val peoplecount = Utils.getString(chatroom,"peoplecount")
                            val max_count = Utils.getString(chatroom,"max_count")
                            val notice = Utils.getString(chatroom,"notice")
                            if (notice != null && notice.length > 0){
                                noticeTV.setText(notice)
                            }
                            membercountTV.setText("("+peoplecount + "/" + max_count+")")
                            if (background != null && background.length > 0){
                                Image_path.add(Config.url + Utils.getString(chatroom,"background"))
                            }

                            val introimage = Config.url + intro

                            ImageLoader.getInstance().displayImage(introimage, profileIV, Utils.UILoptionsProfile)

                            founder_id = Utils.getString(chatroom,"id")

                            val createdsplit = created.split(" ")
                            createdTV.setText(createdsplit.get(0))
                            roomcreatedTV.setText(createdsplit.get(0))

                            roomtitleTV.setText(title)
                            introduceTV.setText(introduce)


                            if (visible == "1"){

                            } else if (visible == "2"){
//                                privateIV.visibility = View.VISIBLE
                            }

                            if (PrefUtils.getIntPreference(context,"member_id") != Utils.getInt(chatroom,"member_id")){
                                setnoticeTV.visibility = View.GONE
                                setroomtitleTV.visibility = View.GONE
                            }

                            if (Utils.getInt(chatroom,"member_id") == Utils.getInt(memberinfo,"id")){
                                var founderIV: CircleImageView = findViewById(R.id.founderIV)
                                var image = Config.url + Utils.getString(memberinfo, "profile_img")
                                ImageLoader.getInstance().displayImage(image, founderIV, Utils.UILoptionsUserProfile)
                                val nick = Utils.getString(memberinfo,"nick")
                                nickTV.setText(nick)
                            }

                        }

                    }
                    backgroundAdapter.notifyDataSetChanged()
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


    fun add_chat_member(){
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", PrefUtils.getIntPreference(context, room_id))

        ChattingAction.add_chat_member(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
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
