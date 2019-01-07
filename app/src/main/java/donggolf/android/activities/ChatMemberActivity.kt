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
import donggolf.android.actions.MateAction
import donggolf.android.adapters.ChatMemberAdapter
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.MutualFriendData
import kotlinx.android.synthetic.main.activity_chat_member.*
import org.json.JSONException
import org.json.JSONObject

class ChatMemberActivity : RootActivity() {

    lateinit var context: Context
    var chatMemberList:ArrayList<JSONObject> = ArrayList<JSONObject>()
    lateinit var chatMemberAdapter :ChatMemberAdapter

    var member_ids: java.util.ArrayList<String> = java.util.ArrayList<String>()
    var member_nicks: java.util.ArrayList<String> = java.util.ArrayList<String>()

    var room_id = ""
    var founder = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_member)

        context = this

        var intent = getIntent()

        room_id = intent.getStringExtra("room_id")
        founder = intent.getStringExtra("founder")

        chatMemberAdapter = ChatMemberAdapter(context, R.layout.item_chat_member_list, chatMemberList)
        joinMemberLV.adapter = chatMemberAdapter

        detail_chatting()

        addmemberLL.setOnClickListener {
            val intent = Intent(context, SelectMemberActivity::class.java)
            intent.putExtra("founder",founder)
            intent.putExtra("room_id",room_id)
            intent.putExtra("member_count",chatMemberList.size)
            intent.putExtra("member_ids",member_ids)
            intent.putExtra("member_nicks",member_nicks)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            finish()
        }

    }
    fun detail_chatting(){
        val params = RequestParams()
        params.put("room_id", room_id)

        ChattingAction.detail_chatting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {

                    if (chatMemberList != null){
                        chatMemberList.clear()
                    }
                    val members = response!!.getJSONArray("chatmember")
                    if (members != null && members.length() > 0){
                        for (i in 0 until members.length()){
                            val item = members.get(i) as JSONObject
                            val chatmember = item.getJSONObject("Chatmember")
                            val chatmember_id = Utils.getString(chatmember,"id")
                            val nick = Utils.getString(chatmember,"nick")

                            member_ids.add(chatmember_id)
                            member_nicks.add(nick)
                            chatMemberList.add(item)
                        }
                    }
                    membercountTV.setText(chatMemberList.size.toString())
                    chatMemberAdapter.notifyDataSetChanged()
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
