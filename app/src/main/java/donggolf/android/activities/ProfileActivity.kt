package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.actions.MateAction
import donggolf.android.actions.MemberAction
import donggolf.android.actions.PostAction
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_profile.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ProfileActivity : RootActivity() {

    lateinit var context: Context
    var member_id = ""
    var mate_ids: ArrayList<String> = ArrayList<String>()
    var type = -1
    var matediv = 0
    var chat_id = 0
    var member_recive ="N"
    var RESET = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        context = this

        //상대 홈피
        val user_id = intent.getIntExtra("other_member_id",0)

        var intent = getIntent()
        member_id = intent.getStringExtra("member_id")
        //타입이 1이면 친구신청으로
        type = intent.getIntExtra("type",-1)

        if (member_id.toInt() == PrefUtils.getIntPreference(context,"member_id")){
            knowTogether.visibility = View.GONE
        }

        member_info(member_id)
        mate_ids.add(member_id)

        if (type ==1){
            profile_opIV.setImageResource(R.mipmap.btn_add_friend)
            profile_opTV.text = "친구신청"
        } else {

        }

        knowTogether.setOnClickListener {
            val intent = Intent(context, MutualActivity::class.java)
            intent.putExtra("mate_id", member_id)
            startActivity(intent)
        }


        click_chat.setOnClickListener {
            if (PrefUtils.getIntPreference(context,"member_id") == member_id.toInt()){
                Toast.makeText(context, "자신은 요청하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val profileTV = profile_opTV.text.toString()
            val nick = txUserName.text.toString()
            if (profileTV == "친구신청"){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("친구신청하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            if (member_id != null) {

                                var params = RequestParams()
                                params.put("mate_id", member_id)
                                params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
                                params.put("category_id",0)
                                params.put("status","w")
                                PostAction.add_friend(params, object : JsonHttpResponseHandler() {
                                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                                        member_info(member_id)
                                        val result = response!!.getString("result")
                                        if (result == "yes") {
                                            Toast.makeText(context, "이미 친구신청을 하셨거나 친구신청을 받았습니다.", Toast.LENGTH_SHORT).show()
                                        }else if (result == "already"){
                                            Toast.makeText(context, "차단상태입니다.", Toast.LENGTH_SHORT).show()
                                        }else {
                                            Toast.makeText(context, "친구신청을 보냈습니다", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                                    }
                                })

                            }else{
                                Toast.makeText(context, "비회원은 신청이 불가합니다.", Toast.LENGTH_SHORT).show()
                            }

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }
            else if(profileTV == "채팅"){
                if (matediv == 0){
                    Toast.makeText(context, "1촌이 아니시면 채팅을 하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                   chk_chat()
                }
            } else if (profileTV == "차단해제"){
                val builder = AlertDialog.Builder(context)
                builder.setMessage(nick + "님을 차단취소 하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            block_cancle(member_id)
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            } else if (profileTV == "신청취소"){
                val builder = AlertDialog.Builder(context)
                builder.setMessage(nick + "님을 친구신청을 취소 하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            cancle(member_id)
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }else if (profileTV == "신청불가"){
                Toast.makeText(context,"당분간 친구신청을 받을수 없습니다.",Toast.LENGTH_SHORT).show()
            }
        }


        //프로필 사진
        otherPrfImgIV.setOnClickListener {
            val intent = Intent(context,ViewProfileListActivity::class.java)
            intent.putExtra("viewAlbumUser", member_id.toInt())
            startActivity(intent)
        }

        profBack.setOnClickListener {
            finish()
        }

        click_post.setOnClickListener {
            val intent = Intent(context, MyPostMngActivity::class.java)
            intent.putExtra("founder", member_id)
            intent.putExtra("type", "founder")
            intent.putExtra("nick",txUserName.text.toString())
            startActivity(intent)
        }

        click_friend.setOnClickListener {
            val intent = Intent(context, MutualActivity::class.java)
            intent.putExtra("mate_id", member_id)
            startActivity(intent)
        }

    }



    fun member_info(member_id:String){
        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("my_id",PrefUtils.getIntPreference(context, "member_id"))

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject) {
                try {
                    val result = response.getString("result")

                    if (result == "ok") {

                        profile_opIV.setImageResource(R.drawable.btn_chat_on)
                        profile_opTV.setText("채팅")

                        val member = response.getJSONObject("Member")
                        var sex = Utils.getString(member,"sex")
                        val friendCount = response.getString("friendCount")
                        val contentCount = response.getString("contentCount")
                        val chatCount = response.getString("chatCount")

                        if (chatCount==null){
                            txChatCnt.setText("0")
                        }else{
                            txChatCnt.setText(chatCount)

                        }

                        if (sex == "1"){
                            txUserName.setTextColor(Color.parseColor("#EF5C34"))
                        }else{
                            txUserName.setTextColor(Color.parseColor("#000000"))
                        }

                        txPostCnt.setText(contentCount)
                        friendCountTV.setText(friendCount)

                        textDate.text = Utils.getString(member,"created").substringBefore(" ")
                        txUserName.text = Utils.getString(member,"nick")

                        // 지역
                        val region1 = response.getJSONObject("region1")
                        val region2 = response.getJSONObject("region2")
                        val region3 = response.getJSONObject("region3")

                        var r_name1 = Utils.getString(region1,"name")
                        var r_name2 = Utils.getString(region2,"name")
                        var r_name3 = Utils.getString(region3,"name")

                        var region1_name = Utils.getString(region1,"region_name")
                        var region2_name = Utils.getString(region2,"region_name")
                        var region3_name = Utils.getString(region3,"region_name")

                        var region = ""

                        if (r_name1 != null && r_name1 != "") {
                            if (region1_name.contains("시")){
                                region += region1_name+">"+r_name1
                            }else{
                                region += r_name1
                            }
                        }

                        if (r_name2 != null && r_name2 != "") {
                            if (region2_name.contains("시")){
                                region += ","+region2_name+">"+r_name2
                            }else{
                                region +=","+ r_name2
                            }
                        }

                        if (r_name3 != null && r_name3 != "") {
                            if (region3_name.contains("시")){
                                region += ","+region3_name+">"+r_name3
                            }else{
                                region +=","+ r_name3
                            }
                        }

                        if (r_name1 == "전국") {
                            region = "전국"
                        }

                        /*       if (region.substring(region.length-1) == ","){
                                   region = region.substring(0, region.length-2)
                               }*/
                        txUserRegion.text = region


                        //상메
                        var statusMessage = Utils.getString(member,"status_msg")
                        if (statusMessage != null) {
                            statusMessageTV.text = statusMessage
                        }

//                        knowTogether.visibility = View.GONE

                        //해시태그
                        val data = response.getJSONArray("MemberTags")
                        if (data != null) {
                            var string_tag = ""
                            for (i in 0 until data.length()) {
                                var json = data[i] as JSONObject
                                val memberTag = json.getJSONObject("MemberTag")
                                string_tag += "#" + Utils.getString(memberTag, "tag") + " "

                                val other_member = json.getJSONObject("Member")
                                member_recive = Utils.getString(other_member,"recive_mate")

                            }
//                            hashtagTV.text = string_tag
                        }

                        //프로필 이미지
                        val imgData = response.getJSONArray("MemberImgs")
                        txPhotoCnt.text = imgData.length().toString()

                        //val tmpProfileImage = imgData.getJSONObject(0)
                        val img_uri = Utils.getString(member,"profile_img")//small_uri
                        val image = Config.url + img_uri

                        ImageLoader.getInstance().displayImage(image, otherPrfImgIV, Utils.UILoptionsProfile)

                        knowTogether.visibility = View.VISIBLE

                        var mateCount = Utils.getInt(member,"mate_cnt")
                        if (mateCount > 0){
                            mutualTV.setText("내 1촌 ${mateCount.toString()}명과 아는 사람")
                        }

                        matediv = response.getInt("mateDiv")

                        if (matediv > 0){
                            imgRelation.setBackgroundResource(R.drawable.icon_first)
                        } else {
                            imgRelation.setBackgroundResource(R.drawable.icon_second)
                            profile_opIV.setImageResource(R.mipmap.btn_add_friend)
                            profile_opTV.text = "친구신청"
                        }

                        if (mateCount == 0 && matediv == 0 ){
                            knowTogether.visibility = View.GONE
                            profile_opIV.setImageResource(R.mipmap.btn_add_friend)
                            profile_opTV.text = "친구신청"
                        }

                        val status = Utils.getString(member,"status")
                        val user_status = Utils.getString(member,"user_status")



                        if (status == "b"){
                            profile_opIV.setImageResource(R.drawable.btn_block)
                            profile_opTV.text = "차단해제"
                        } else if (status == "w"){
                            profile_opIV.setImageResource(R.drawable.btn_add_friend_cancel)
                            profile_opTV.text = "신청취소"
                        }
                        if (member_recive == "Y"){
                            profile_opIV.setImageResource(R.drawable.btn_block)
                            profile_opTV.text = "신청불가"
                        }

                        if (user_status=="b"){
                            profile_opIV.setImageResource(R.drawable.btn_add_friend_cancel)
                            profile_opTV.text = "신청불가"
                        }


                        if (PrefUtils.getIntPreference(context,"member_id") == member_id.toInt()){
                            knowTogether.visibility = View.GONE
                            profile_opIV.setImageResource(R.drawable.btn_chat_on)
                            profile_opTV.text = "채팅"
                        }



                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject) {
                //println(errorResponse.toString())
            }
        })

    }

    fun chk_chat(){

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("mate_id",member_id)

        ChattingAction.chk_chat(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject) {
                try {
                    val result = response.getString("result")

                    if (result == "ok") {
                        val chatroom = response.getJSONObject("chatroom")
                        val room = chatroom.getJSONObject("Chatroom")
                        val id = Utils.getString(room,"id")
                        val founder = Utils.getString(room,"member_id")
                        val type = Utils.getString(room, "type")

                        if (type == "1"){

                            var intent = Intent(context, ChatDetailActivity::class.java)
                            intent.putExtra("division", 1)
                            intent.putExtra("id", id)
                            intent.putExtra("founder", founder)
                            startActivityForResult(intent,RESET)
                        }

                    } else if (result == "empty"){
                        val nick = txUserName.text.toString()

                        val builder = AlertDialog.Builder(context)
                        builder.setMessage(nick + "님과 채팅을 하시겠습니까 ?").setCancelable(false)
                                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                    addchat()
                                })
                                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                        val alert = builder.create()
                        alert.show()
                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject) {
                //println(errorResponse.toString())
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
                    val lastid = response!!.getString("lastid")
                    var intent = Intent()
                    intent.putExtra("finish","finish")
                    intent.action = "RESET_CHATTING"
                    sendBroadcast(intent)
                    setResult(RESULT_OK, intent);

                    val founder = PrefUtils.getIntPreference(context,"member_id")

                    var intentst = Intent(context, ChatDetailActivity::class.java)
                    intentst.putExtra("division", 1)
                    intentst.putExtra("id", lastid)
                    intentst.putExtra("founder", founder.toString())
                    startActivityForResult(intentst,RESET)

                    finish()

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                //println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                //println(errorResponse)
            }
        })

    }

    fun cancle(mate_id:String){
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mate_id)
        params.put("status", "rc")

        MateAction.cancle_mate(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    Utils.alert(context,"선택한 대상의 친구신청을 취소했습니다")
                    member_info(member_id)
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                //println("reject action error : $errorResponse")
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                //println("reject error : $responseString")
            }
        })
    }


    fun block_cancle(mate_id : String){
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mate_id)

        MateAction.block_cancle(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                //println(response)
                val result = response!!.getString("result")
                if (result == "ok") {
                    member_info(member_id)
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                //println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                //println(responseString)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                RESET -> {

                    //println("---------------reset 타기")
                    if (data!!.getStringExtra("reset") != null) {
                        var intent = Intent()
                        intent.action = "RESET_CHATTING"
                        sendBroadcast(intent)
                        //println("-------------reset_chatting")
                    }
                }

            }
        }

    }


}
