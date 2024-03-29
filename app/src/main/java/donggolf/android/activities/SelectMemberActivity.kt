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
import donggolf.android.adapters.SelectBlockMemberAdapter
import donggolf.android.adapters.SelectMemberAdapter
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_select_member.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class SelectMemberActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  memberAdapter : SelectMemberAdapter
    private  var memberList: ArrayList<JSONObject> = ArrayList<JSONObject>()

    private  lateinit var  blockMemberAdapter : SelectBlockMemberAdapter
    private  var blockMemberList: ArrayList<JSONObject> = ArrayList<JSONObject>()

    var mate_ids:ArrayList<String> = ArrayList<String>()
    var mate_nicks:ArrayList<String> = ArrayList<String>()
    var block_member_ids:ArrayList<String> = ArrayList<String>()
    var get_mate_nicks:ArrayList<String> = ArrayList<String>()

    var founder = ""
    var room_id = ""
    var member_count = 0
    var max_count = 0
    var people_count = 0

    var chatTitle = ""

    var division = ""

    var block = "nomal"
    var searchKeyword = ""

    var block_yn = ""
    var last_id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_member)

        context = this

        memberAdapter = SelectMemberAdapter(context,R.layout.item_select_member,memberList)
        blockMemberAdapter = SelectBlockMemberAdapter(context,R.layout.item_select_member,blockMemberList)

        var intent = getIntent()

        if (intent.getStringExtra("new") != null){
            division = "0"
        }

        if (intent.getStringExtra("room_id") != null && intent.getStringExtra("founder") != null){
            room_id = intent.getStringExtra("room_id")
            founder = intent.getStringExtra("founder")
            member_count = intent.getIntExtra("member_count",0)
            mate_ids = intent.getStringArrayListExtra("member_ids")
            get_mate_nicks = intent.getStringArrayListExtra("member_ids")
            mate_nicks = intent.getStringArrayListExtra("member_nicks")
            division = intent.getStringExtra("division")
            if (intent.getIntExtra("max_count",0) != null){
                max_count = intent.getIntExtra("max_count",0)
                people_count = intent.getIntExtra("people_count",0)
            }
        }

        if (intent.getStringExtra("block") != null){
            block = intent.getStringExtra("block")
            room_id = intent.getStringExtra("room_id")
            block_yn = intent.getStringExtra("block_yn")
            last_id = intent.getIntExtra("last_id",0)
            if (block_yn == "Y"){
                titleTV.setText("차단하기")
            } else {
                titleTV.setText("차단해제")
            }
//            titleTV.setText("차단하기/차단해제")
            countTV.visibility = View.GONE
            addchatTV.setText("확인")
        }

        if (block == "nomal") {
            selMemList.adapter = memberAdapter
            getFriendList("m")
        } else {
            selMemList.adapter = blockMemberAdapter
            getChatMember()
        }

        selMemList.setOnItemClickListener { parent, view, position, id ->
            if (block == "nomal") {
                var json = memberList.get(position)
                var chk = Utils.getBoolen(json, "isSelectedOp")

                var view: View = View.inflate(context, R.layout.item_selectmember, null)
                var profileIV: CircleImageView = view.findViewById(R.id.profileIV)
                var deleteIV: ImageView = view.findViewById(R.id.deleteIV)

                deleteIV.setOnClickListener {
                    chatmemberlistLL.removeView(view)
                    memberList[position].put("isSelectedOp", false)
                    memberAdapter.notifyDataSetChanged()

                    if (memberList.size > 0 && memberList != null) {
                        var count = 0
                        for (i in 0 until memberList.size) {
                            val json = memberList.get(i)
                            var chk = Utils.getBoolen(json, "isSelectedOp")
                            if (chk == true) {
                                count++
                            }
                        }
                        countTV.setText(count.toString())
                    }
                }

                var mateMember = json.getJSONObject("MateMember")

                var image = Config.url + Utils.getString(mateMember, "profile_img")
                ImageLoader.getInstance().displayImage(image, profileIV, Utils.UILoptionsUserProfile)

                if (chk == false) {
                    memberList[position].put("isSelectedOp", true)
                    memberAdapter.notifyDataSetChanged()
                    chatmemberlistLL.addView(view)
                }
//            else {
//                memberList[position].put("isSelectedOp", false)
//                memberAdapter.notifyDataSetChanged()
////                chatmemberlistLL.removeViewAt(position)
//            }

                if (memberList.size > 0 && memberList != null) {
                    var count = 0
                    for (i in 0 until memberList.size) {
                        val json = memberList.get(i)
                        var chk = Utils.getBoolen(json, "isSelectedOp")
                        if (chk == true) {
                            count++
                        }
                    }
                    countTV.setText(count.toString())
                }
            } else {
                var json = blockMemberList.get(position)
                var chk = Utils.getBoolen(json, "isSelectedOp")

                var view: View = View.inflate(context, R.layout.item_selectmember, null)
                var profileIV: CircleImageView = view.findViewById(R.id.profileIV)
                var deleteIV: ImageView = view.findViewById(R.id.deleteIV)

                deleteIV.setOnClickListener {
                    chatmemberlistLL.removeView(view)
                    blockMemberList[position].put("isSelectedOp", false)
                    blockMemberAdapter.notifyDataSetChanged()

                    if (blockMemberList.size > 0 && blockMemberList != null) {
                        var count = 0
                        for (i in 0 until blockMemberList.size) {
                            val json = blockMemberList.get(i)
                            var chk = Utils.getBoolen(json, "isSelectedOp")
                            if (chk == true) {
                                count++
                            }
                        }
                        countTV.setText(count.toString())
                    }
                }

                var mateMember = json.getJSONObject("Member")

                var image = Config.url + Utils.getString(mateMember, "profile_img")
                ImageLoader.getInstance().displayImage(image, profileIV, Utils.UILoptionsUserProfile)

                if (chk == false) {
                    blockMemberList[position].put("isSelectedOp", true)
                    blockMemberAdapter.notifyDataSetChanged()
                    chatmemberlistLL.addView(view)
                }
//            else {
//                memberList[position].put("isSelectedOp", false)
//                memberAdapter.notifyDataSetChanged()
////                chatmemberlistLL.removeViewAt(position)
//            }

                if (blockMemberList.size > 0 && blockMemberList != null) {
                    var count = 0
                    for (i in 0 until blockMemberList.size) {
                        val json = blockMemberList.get(i)
                        var chk = Utils.getBoolen(json, "isSelectedOp")
                        if (chk == true) {
                            count++
                        }
                    }
                    countTV.setText(count.toString())
                }
            }
        }

        addchatTV.setOnClickListener {
            if (block == "nomal") {
                if (mate_ids != null) {
                    mate_ids.clear()
                }

                for (i in 0 until memberList.size) {
                    val item = memberList.get(i)
                    var member = item.getJSONObject("MateMember")
                    var member_id = Utils.getString(member, "id")
                    var isSel = item.getBoolean("isSelectedOp")
                    if (isSel) {
                        mate_ids.add(member_id)
                    }

                }
                val count = countTV.text.toString().toInt()
                if (count > 0) {
                    if (memberList.size > 0 && memberList != null) {

                        if (division == "0") {
                            var count = 0
                            for (i in 0 until memberList.size) {
                                val json = memberList.get(i)
                                var chk = Utils.getBoolen(json, "isSelectedOp")
                                if (chk == true) {
                                    var member = json.getJSONObject("MateMember")
                                    var mate_id = Utils.getString(member, "id")
                                    var mate_nick = Utils.getString(member, "nick")
                                    var chkData = false

                                    for (j in 0 until mate_ids.size) {
                                        if (mate_ids.get(j) == mate_id) {
                                            chkData = true
                                        }
                                    }

                                    if (chkData == false) {
                                        mate_ids.add(mate_id)
                                    }
                                    mate_nicks.add(mate_nick)
                                }
                            }

                            // println("member_count ----- $member_count")

                            if (room_id == "") {
                                for (i in 0 until mate_nicks.size) {
                                    chatTitle += mate_nicks.get(i) + " "
                                }
//                                addchat()
                            }
//                            else if (member_count == 2) {
//                                for (i in 0 until mate_nicks.size) {
//                                    chatTitle += mate_nicks.get(i) + " "
//                                }
//
//                                for (i in 0 until get_mate_nicks.size){
//                                    mate_ids.add(get_mate_nicks.get(i))
//                                }
//                                add_chat_member()
////                                addchat()
//                            }
                            else {
                                for (i in 0 until mate_nicks.size) {
                                    chatTitle += mate_nicks.get(i) + " "
                                }
                                add_chat_member()
                            }

                        } else {
//                            println("--------add mate_ids${mate_ids.size} ---- $people_count ----- $max_count")
                            if (max_count < people_count + mate_ids.size) {
                                Toast.makeText(context, "정원초과 입니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                add_chat_member()
                            }
                        }

                    }
                } else {
                    Toast.makeText(context, "1명 이상 초대해야 합니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                set_dongchat_block()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

        searchTV.setOnClickListener {
            val keyword = searchET.text.toString()
            if (keyword == "" || keyword == null){
                Toast.makeText(context, "빈칸은 입력하실 수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            searchKeyword = keyword

            if (block == "nomal"){
                getFriendList("m")
            } else {
                getChatMember()
            }
        }

        removesearchLL.setOnClickListener {
            Utils.hideKeyboard(context!!)

            if(searchET.text.toString() == "") {
                return@setOnClickListener
            }

            searchET.setText("")

            searchKeyword = ""
        }
    }

    fun getFriendList(status : String) {
        //친구 리스트 받아오기
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("status", status)

        if (searchKeyword != "" && searchKeyword.length > 0){
            params.put("searchKeyword", searchKeyword)
        }

        MateAction.mateList(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    // println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        if (memberList != null){
                            memberList.clear()
                        }
                        val friendList = response!!.getJSONArray("mates")
                        if (friendList != null && friendList.length() > 0){
                            for (i in 0 until friendList.length()){
                                memberList.add(friendList.get(i) as JSONObject)
                                memberList.get(i).put("isSelectedOp", false)
                            }
                        }
                        searchKeyword = ""
                        memberAdapter.notifyDataSetChanged()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }
        })
    }

    fun add_chat_member(){
        val params = RequestParams()
        params.put("mate_id", mate_ids)
        params.put("room_id", room_id)
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("type", "add")

        ChattingAction.add_chat_member(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        var intent = Intent()
                        intent.putExtra("reset","reset")
                        intent.action = "RESET_CHATTING"
                        sendBroadcast(intent)
                        setResult(RESULT_OK, intent);
                        finish()
                    } else if (result == "already"){
                        Toast.makeText(context,"이미 대화방에 선택하신 멤버가 있습니다.", Toast.LENGTH_SHORT).show()
                    } else if (result == "block"){
                        Toast.makeText(context,"선택하신 멤버가 대화방에 차단되어 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }
        })
    }

    fun addchat(){

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mate_ids)
//        params.put("title", chatTitle)
        params.put("regions", "")
        params.put("intro", "")
        params.put("type", "1")

        ChattingAction.add_chat(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    var intent = Intent()
                    intent.putExtra("finish","finish")
                    intent.action = "RESET_CHATTING"
                    sendBroadcast(intent)
                    setResult(RESULT_OK, intent);
                    finish()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }
        })

    }


    fun getChatMember() {
        //친구 리스트 받아오기
        val params = RequestParams()
        params.put("room_id", room_id)

        if (searchKeyword != "" && searchKeyword.length > 0){
            params.put("searchKeyword", searchKeyword)
        }

        if (block_yn == "Y"){
            params.put("type", "block_n")
        } else {
            params.put("type", "block_y")
        }

        ChattingAction.get_chat_member(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    // println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        if (blockMemberList != null){
                            blockMemberList.clear()
                        }
                        val friendList = response!!.getJSONArray("chatmember")
                        if (friendList != null && friendList.length() > 0){
                            for (i in 0 until friendList.length()){
                                val item = friendList.get(i) as JSONObject
                                val Chatroom = item.getJSONObject("Chatroom")
                                val room_member_id = Utils.getString(Chatroom,"member_id")

                                val Member = item.getJSONObject("Member")
                                val member_id = Utils.getString(Member,"id")

                                if (room_member_id != member_id) {
                                    blockMemberList.add(friendList.get(i) as JSONObject)
                                    blockMemberList.get(i).put("isSelectedOp", false)
                                }
                            }
                        }
                        searchKeyword = ""
                        blockMemberAdapter.notifyDataSetChanged()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }
        })
    }

    fun set_dongchat_block(){
        if (block_member_ids != null){
            block_member_ids.clear()
        }

        for (i in 0 until blockMemberList.size){
            val item = blockMemberList.get(i)
            var Chatmember = item.getJSONObject("Chatmember")
            var isSel = item.getBoolean("isSelectedOp")
            val id = Utils.getString(Chatmember,"member_id")
            if (isSel){
              block_member_ids.add(id)
            }
        }

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("block_member_ids",block_member_ids)
        params.put("room_id", room_id)
        params.put("last_id", last_id)

        // println("-------blocks")


        ChattingAction.set_dongchat_block(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    var intent = Intent()
                    intent.putExtra("reset","reset")
                    intent.action = "RESET_CHATTING"
                    sendBroadcast(intent)
                    setResult(RESULT_OK, intent);
                    finish()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }
        })
    }


}
