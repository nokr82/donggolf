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
    var division = ""
    var founder_id = ""
    var member_id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_member)

        context = this

        var intent = getIntent()

        room_id = intent.getStringExtra("room_id")
        founder = intent.getStringExtra("founder")
        division = intent.getStringExtra("division")
        founder_id = intent.getStringExtra("founder_id")
        member_id = PrefUtils.getIntPreference(context,"member_id")

        chatMemberAdapter = ChatMemberAdapter(context, R.layout.item_chat_member_list, chatMemberList)
        joinMemberLV.adapter = chatMemberAdapter

        detail_chatting()

        addmemberLL.setOnClickListener {

            if (founder_id == "1"){
                Toast.makeText(context,"1촌이 아니시면 대화멤버를 추가하실 수 없습니다..",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (division == "0") {
                val intent = Intent(context, SelectMemberActivity::class.java)
                intent.putExtra("founder", founder)
                intent.putExtra("room_id", room_id)
                intent.putExtra("member_count", chatMemberList.size)
                intent.putExtra("member_ids", member_ids)
                intent.putExtra("member_nicks", member_nicks)
                intent.putExtra("division", "0")
                startActivity(intent)
            } else {
                val member_count = intent.getIntExtra("member_count",0)
                val mate_ids = intent.getStringArrayListExtra("member_ids")
                val get_mate_nicks = intent.getStringArrayListExtra("member_ids")
                val mate_nicks = intent.getStringArrayListExtra("member_nicks")
                val max_count = intent.getIntExtra("max_count",0)
                val people_count = intent.getIntExtra("people_count",0)

                val intent = Intent(context, SelectMemberActivity::class.java)
                intent.putExtra("founder",founder)
                intent.putExtra("room_id",room_id)
                intent.putExtra("member_count",member_count)
                intent.putExtra("member_ids",mate_ids)
                intent.putExtra("member_nicks",mate_nicks)
                intent.putExtra("division","1")
                intent.putExtra("max_count",max_count)
                intent.putExtra("people_count",people_count)
                startActivity(intent)
            }

        }

        joinMemberLV.setOnItemClickListener { parent, view, position, id ->
            val json = chatMemberAdapter.getItem(position)

            var member = json!!.getJSONObject("Member")
            var member_id = Utils.getString(member,"id")

            if ( PrefUtils.getIntPreference(context,"member_id") == member_id.toInt()){
                Toast.makeText(context,"자기 자신은 프로필을 볼 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }


            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("member_id", member_id)
            startActivity(intent)





        }

        btnBack.setOnClickListener {
            finish()
        }

    }
    fun detail_chatting(){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("member_id", member_id)

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
