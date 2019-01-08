package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.actions.MateAction
import donggolf.android.adapters.SelectMemberAdapter
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_select_member.*
import kotlinx.android.synthetic.main.item_select_member.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class SelectMemberActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  memberAdapter : SelectMemberAdapter
    private  var memberList: ArrayList<JSONObject> = ArrayList<JSONObject>()

    var mate_ids:ArrayList<String> = ArrayList<String>()
    var mate_nicks:ArrayList<String> = ArrayList<String>()

    var founder = ""
    var room_id = ""
    var member_count = 0

    var chatTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_member)

        context = this

        memberAdapter = SelectMemberAdapter(context,R.layout.item_select_member,memberList)

        selMemList.adapter = memberAdapter

        var intent = getIntent()

        if (intent.getStringExtra("room_id") != null && intent.getStringExtra("founder") != null){
            room_id = intent.getStringExtra("room_id")
            founder = intent.getStringExtra("founder")
            member_count = intent.getIntExtra("member_count",0)
            mate_ids = intent.getStringArrayListExtra("member_ids")
            mate_nicks = intent.getStringArrayListExtra("member_nicks")
        }

        getFriendList("m")

        selMemList.setOnItemClickListener { parent, view, position, id ->
            var json = memberList.get(position)
            var chk = Utils.getBoolen(json,"isSelectedOp")

            var view: View = View.inflate(context, R.layout.item_selectmember, null)
            var profileIV: CircleImageView = view.findViewById(R.id.profileIV)
            var deleteIV:ImageView = view.findViewById(R.id.deleteIV)

            deleteIV.setOnClickListener {
                chatmemberlistLL.removeView(view)
                memberList[position].put("isSelectedOp", false)
                memberAdapter.notifyDataSetChanged()

                if (memberList.size > 0 && memberList != null){
                    var count = 0
                    for (i in 0 until memberList.size){
                        val json =  memberList.get(i)
                        var chk = Utils.getBoolen(json,"isSelectedOp")
                        if (chk == true){
                            count++
                        }
                    }
                    countTV.setText(count.toString())
                }
            }

            var mateMember = json.getJSONObject("MateMember")

            var image = Config.url + Utils.getString(mateMember, "profile_img")
            ImageLoader.getInstance().displayImage(image, profileIV, Utils.UILoptionsUserProfile)

            if (chk == false){
                memberList[position].put("isSelectedOp", true)
                memberAdapter.notifyDataSetChanged()
                chatmemberlistLL.addView(view)
            }
//            else {
//                memberList[position].put("isSelectedOp", false)
//                memberAdapter.notifyDataSetChanged()
////                chatmemberlistLL.removeViewAt(position)
//            }

            if (memberList.size > 0 && memberList != null){
                var count = 0
                for (i in 0 until memberList.size){
                   val json =  memberList.get(i)
                    var chk = Utils.getBoolen(json,"isSelectedOp")
                    if (chk == true){
                        count++
                    }
                }
                countTV.setText(count.toString())
            }
        }

        addchatTV.setOnClickListener {
            val count = countTV.text.toString().toInt()
            if (count > 0 ){
                if (memberList.size > 0 && memberList != null){
                    var count = 0
                    for (i in 0 until memberList.size){
                        val json =  memberList.get(i)
                        var chk = Utils.getBoolen(json,"isSelectedOp")
                        if (chk == true){
                            var member = json.getJSONObject("MateMember")
                            var mate_id = Utils.getString(member,"id")
                            var mate_nick = Utils.getString(member,"nick")
                            var chkData = false

                            for (j in 0 until mate_ids.size){
                                if (mate_ids.get(j) == mate_id){
                                    chkData = true
                                }
                            }

                            if (chkData == false){
                                mate_ids.add(mate_id)
                            }
                            mate_nicks.add(mate_nick)
                        }
                }
                 if (room_id != ""){
                     for (i in 0 until mate_nicks.size){
                         chatTitle += mate_nicks.get(i) + " "
                     }
                     addchat()
                     finish()
                 } else if (member_count == 2){
                     for (i in 0 until mate_nicks.size){
                         chatTitle += mate_nicks.get(i) + " "
                     }
                     addchat()
                     finish()
                 } else {
                     for (i in 0 until mate_nicks.size){
                         chatTitle += mate_nicks.get(i) + " "
                     }
                    add_chat_member()
                     finish()
                 }

                }
            } else {
                Toast.makeText(context, "1명 이상 초대해야 합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    fun getFriendList(status : String) {
        //친구 리스트 받아오기
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("status", status)
        params.put("division",0)

        MateAction.mateList(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val friendList = response!!.getJSONArray("mates")
                        if (friendList != null && friendList.length() > 0){
                            for (i in 0 until friendList.length()){
                                memberList.add(friendList.get(i) as JSONObject)
                                memberList.get(i).put("isSelectedOp", false)
                            }
                        }
                        memberAdapter.notifyDataSetChanged()
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

    fun add_chat_member(){
        val params = RequestParams()
        params.put("mate_id", mate_ids)
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

    fun addchat(){

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mate_ids)
        params.put("title", chatTitle)
        params.put("regions", "")
        params.put("intro", "")
        params.put("type", "1")
        params.put("division",0)

        ChattingAction.add_chat(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
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
