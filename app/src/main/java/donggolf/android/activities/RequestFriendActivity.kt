package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MateAction
import donggolf.android.adapters.MateManageAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_request_friend.*
import kotlinx.android.synthetic.main.item_mate_manage.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream

class RequestFriendActivity : RootActivity() {

    lateinit var context : Context

    var mateRequestList = ArrayList<JSONObject>()
    lateinit var matesRequestAdapter: MateManageAdapter
    var mateList = ArrayList<Int>()

    var checkAll = false

    val SELECT_CATEGORY = 101

    var wait = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_friend)

        context = this

        matesRequestAdapter = MateManageAdapter(context, R.layout.item_mate_manage, mateRequestList,this,wait)
        requestFriends.adapter = matesRequestAdapter

        val type = intent.getStringExtra("type")
        if (type == "waiting") {
            wait = "wait"
            getFriendList("w", 0)
        } else if (type == "send"){
            wait = "send"
            titleTV.setText("보낸 친구 요청")
            swc_alarm_req_frd.visibility = View.GONE
            titlevatLL.visibility = View.GONE
            getFriendList("w", 0)
        } else {
            wait = "block"
            titleTV.setText("차단 목록")
            swc_alarm_req_frd.visibility = View.GONE
            titlevatLL.visibility = View.GONE
            getFriendList("b", 0)
        }

        requestFriends.setOnItemClickListener { parent, view, position, id ->
            val item = mateRequestList.get(position)
            val matemember = item.getJSONObject("MateMember")
            val mate_id = Utils.getString(matemember,"id")
            val mate_nick = Utils.getString(matemember,"nick")
            val member = item.getJSONObject("Member")
            val member_id = Utils.getString(member,"id")
            val member_nick = Utils.getString(member,"nick")
            val division = item.getString("division")

//            view.accLL.setOnClickListener {
//                val acceptItt = Intent(context, FriendReqSelectCategoryActivity::class.java)
//                acceptItt.putExtra("mate_id",member_id)
//                startActivityForResult(acceptItt,SELECT_CATEGORY)
//            }
//
//            view.refuseLL.setOnClickListener {
//                refuse(member_id)
//            }
//
//            view.cancleLL.setOnClickListener {
//                val builder = AlertDialog.Builder(context)
//                builder
//                        .setMessage("$mate_nick 님에게 보낸 친구 요청을 취소하시겠습니까 ?")
//
//                        .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
//                            dialog.cancel()
//                            cancle(mate_id)
//                        })
//                        .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
//                            dialog.cancel()
//                        })
//
//                val alert = builder.create()
//                alert.show()
//            }
//
//            view.blockcancleLL.setOnClickListener {
//                val builder = AlertDialog.Builder(context)
//                builder
//                        .setMessage("$mate_nick 님을 차단해제 하시겠습니까 ?")
//
//                        .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
//                            block_cancle(mate_id)
//                            dialog.cancel()
//                        })
//                        .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
//                            dialog.cancel()
//                        })
//
//                val alert = builder.create()
//                alert.show()
//            }

//            var intent = Intent(context, ProfileActivity::class.java)
//            if (wait == "wait"){
//                intent.putExtra("member_id", member_id)
//            } else {
//                intent.putExtra("member_id", mate_id)
//            }
//            startActivity(intent)

        }

        //전체선택버튼
        frdReq_check_all.setOnClickListener {
            if (!checkAll){
                checkAll = true
                checkIcon.visibility = View.VISIBLE

            } else {
                mateList.clear()
                checkAll = false
                checkIcon.visibility = View.GONE

            }

            mateList.clear()

            for (i in 0 until mateRequestList.size) {
                mateRequestList.get(i).put("check", checkAll)
                mateList.add(mateRequestList.get(i).getInt("mate_id"))
            }
            matesRequestAdapter.notifyDataSetChanged()
            println(mateList)

        }


        acceptTV.setOnClickListener {
            val acceptItt = Intent(context, FriendReqSelectCategoryActivity::class.java)
            startActivityForResult(acceptItt,SELECT_CATEGORY)
        }

        rejectTV.setOnClickListener {
            mateList.clear()

            for (i in 0 until mateRequestList.size) {
                if(mateRequestList.get(i).getBoolean("check")) {
                    mateList.add(mateRequestList.get(i).getInt("mate_id"))
                }
            }

            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
            params.put("mate_id", mateList)
            params.put("status", "r")

            MateAction.rejectMateRequest(params, object : JsonHttpResponseHandler(){
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        Utils.alert(context,"선택한 대상의 요청을 거절했습니다")
                        getFriendList("w",0)
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                    println("reject action error : $errorResponse")
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                    println("reject error : $responseString")
                }
            })
        }

        blockTV.setOnClickListener {
            mateList.clear()

            for (i in 0 until mateRequestList.size) {
                if(mateRequestList.get(i).getBoolean("check")) {
                    mateList.add(mateRequestList.get(i).getInt("mate_id"))
                }
            }
            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
            params.put("mate_id", mateList)

            MateAction.blockMember(params, object : JsonHttpResponseHandler(){
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        getFriendList("b",0)
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                    println(errorResponse)
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                    println(responseString)
                }
            })
        }

        waitTV.setOnClickListener {
            wait = "wait"
            getFriendList("w",0)

        }

        btn_back.setOnClickListener {
            var intent = Intent()
            setResult(Activity.RESULT_OK)
            finish()
            Utils.hideKeyboard(this)
        }
    }

    fun getFriendList(status : String, category_id : Int) {
        //친구 리스트 받아오기
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("status", status)
        params.put("category_id", category_id)
        if (wait == "wait"){
            params.put("wait", wait)
        } else if (wait == "send"){
            params.put("wait", "send")
        } else {
            params.put("wait", "send")
        }

        MateAction.get_mates_list(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        mateRequestList.clear()
                        val mates = response.getJSONArray("mates")
//                        wait = ""
                        var division = ""
                        if (wait == "wait"){
                            division = "w"
                        } else if (wait == "send"){
                            division = "s"
                        } else {
                            division = "b"
                        }
                        for (i in 0 until mates.length()) {
                            val mate = mates[i] as JSONObject

                            mate.put("check", false)
                            mate.put("division", division)

                            mateRequestList.add(mate)
                        }
                        matesRequestAdapter.notifyDataSetChanged()
                    }
                } catch (e:JSONException) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when(requestCode) {
                SELECT_CATEGORY -> {
//                    val selCateg = data!!.getStringExtra("CategoryID")
//                    val mate_id = data!!.getStringExtra("mate_id")
//
//                    acceptMates(selCateg,mate_id)
                    finish()
                }
            }
        }
    }

    fun acceptMates(category_id: String,member_id:String) {
//        mateList.clear()
//
//        for (i in 0 until mateRequestList.size) {
//            if(mateRequestList.get(i).getBoolean("check")) {
//                mateList.add(mateRequestList.get(i).getInt("mate_id"))
//            }
//        }

        val params = RequestParams()
//        params.put("mate_id", mateList)
//        Log.d("메이트",mateList.toString())
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id",  member_id)
        /*     if (mateList != null){
                 if (mateList!!.size != 0){
                     for (i in 0..mateList!!.size - 1){
                         params.put("mate_id[" + i + "]",  mateList.get(i))
     //                    params.put("mate_id[" + i + "]",  mateList.get(i))
                     }
                 }
             }*/
        params.put("category_id", category_id)
        params.put("status", "m")

        MateAction.accept_mates(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
//              Log.d("리스븐",response.toString())
                getFriendList("w",0)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }
        })
    }

    fun refuse(mate_id :String){
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mate_id)
        params.put("status", "r")

        MateAction.rejectMateRequest(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    Utils.alert(context,"선택한 대상의 요청을 거절했습니다")
                    getFriendList("w",0)
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println("reject action error : $errorResponse")
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println("reject error : $responseString")
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
                    getFriendList("w",0)
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println("reject action error : $errorResponse")
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println("reject error : $responseString")
            }
        })
    }


    fun block_cancle(mate_id : String){
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mate_id)

        MateAction.block_cancle(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok") {
                    getFriendList("b",0)
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }
        })
    }

    override fun onBackPressed() {
        Utils.hideKeyboard(context)
        var intent = Intent()
        intent.putExtra("reset","reset")
        setResult(Activity.RESULT_OK,intent)
        finish()
    }



}
