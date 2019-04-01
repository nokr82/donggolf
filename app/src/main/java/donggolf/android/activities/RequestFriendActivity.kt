package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MateAction
import donggolf.android.actions.MemberAction
import donggolf.android.adapters.MateManageAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_profile_manage.*
import kotlinx.android.synthetic.main.activity_request_friend.*
import kotlinx.android.synthetic.main.activity_set_notice.*
import kotlinx.android.synthetic.main.item_mate_manage.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream

class RequestFriendActivity : RootActivity() {

    lateinit var context: Context

    var mateRequestList = ArrayList<JSONObject>()
    lateinit var matesRequestAdapter: MateManageAdapter
    var mateList = ArrayList<Int>()

    var checkAll = false
    var recive_mate = "N"
    val SELECT_CATEGORY = 101
    var wait = ""
    internal var reloadReciver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_friend)

        context = this

        matesRequestAdapter = MateManageAdapter(context, R.layout.item_mate_manage, mateRequestList, this, wait)
        requestFriends.adapter = matesRequestAdapter

        var filter1 = IntentFilter("ADD_FRIEND")
        registerReceiver(reloadReciver, filter1)

        my_info()


        val type = intent.getStringExtra("type")
        if (type == "waiting") {
            wait = "wait"
            getFriendList("w", 0)
        } else if (type == "send") {
            wait = "send"
            titleTV.setText("보낸 친구 요청")
            confrandLL.visibility = View.GONE
            swc_alarm_req_frd.visibility = View.GONE
            titlevatLL.visibility = View.GONE
            getFriendList("w", 0)
        } else {
            wait = "block"
            titleTV.setText("차단 목록")
            confrandLL.visibility = View.GONE
            swc_alarm_req_frd.visibility = View.GONE
            titlevatLL.visibility = View.GONE
            getFriendList("b", 0)
        }


        swc_alarm_req_frd.setOnClickListener {
            recive_mate = "Y"
            Toast.makeText(context,"당분간 친구신청을 받을 수 없습니다.",Toast.LENGTH_SHORT).show()
            swc_alarm_req_frd_on.visibility = View.VISIBLE
            swc_alarm_req_frd.visibility = View.GONE
            updateInfo()
        }
        swc_alarm_req_frd_on.setOnClickListener {
            recive_mate = "N"
            swc_alarm_req_frd_on.visibility = View.GONE
            swc_alarm_req_frd.visibility = View.VISIBLE
            updateInfo()
        }

        requestFriends.setOnItemClickListener { parent, view, position, id ->
            val item = mateRequestList.get(position)
            val matemember = item.getJSONObject("MateMember")
            val mate_id = Utils.getString(matemember, "id")
            val mate_nick = Utils.getString(matemember, "nick")
            val member = item.getJSONObject("Member")
            val member_id = Utils.getString(member, "id")
            val member_nick = Utils.getString(member, "nick")
            val division = item.getString("division")

        }

        //전체선택버튼
        frdReq_check_all.setOnClickListener {
            if (!checkAll) {
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
            startActivityForResult(acceptItt, SELECT_CATEGORY)
        }

        rejectTV.setOnClickListener {
            mateList.clear()

            for (i in 0 until mateRequestList.size) {
                if (mateRequestList.get(i).getBoolean("check")) {
                    mateList.add(mateRequestList.get(i).getInt("mate_id"))
                }
            }

            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
            params.put("mate_id", mateList)
            params.put("status", "r")

            MateAction.rejectMateRequest(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        Utils.alert(context, "선택한 대상의 요청을 거절했습니다")
                        getFriendList("w", 0)
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
                if (mateRequestList.get(i).getBoolean("check")) {
                    mateList.add(mateRequestList.get(i).getInt("mate_id"))
                }
            }
            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
            params.put("mate_id", mateList)

            MateAction.blockMember(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        getFriendList("b", 0)
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
            getFriendList("w", 0)

        }

        btn_back.setOnClickListener {
            var intent = Intent()
            intent.putExtra("reset", "reset")
            setResult(Activity.RESULT_OK, intent)
            finish()
            Utils.hideKeyboard(this)
        }
    }

    fun getFriendList(status: String, category_id: Int) {
        //친구 리스트 받아오기
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("status", status)
        params.put("category_id", category_id)
        if (wait == "wait") {
            params.put("wait", wait)
        } else if (wait == "send") {
            params.put("wait", "send")
        } else {
            params.put("wait", "send")
        }

        MateAction.get_mates_list(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        mateRequestList.clear()
                        val mates = response.getJSONArray("mates")
//                        wait = ""
                        var division = ""
                        if (wait == "wait") {
                            division = "w"
                        } else if (wait == "send") {
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
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


    fun updateInfo() {

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("recive_mate", recive_mate)

        MemberAction.m_update_info(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {


                val result = response!!.getString("result")

                if (result == "ok") {

                } else {

                }

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {

                // System.out.println(responseString);

                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONObject?) {
                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONArray?) {

                throwable.printStackTrace()
                error()
            }

            override fun onStart() {
                // show dialog

            }

            override fun onFinish() {

            }

            private fun error() {
                Utils.alert(context, "조회중 장애가 발생하였습니다.")
            }

        })

    }

    fun refuse(mate_id: String) {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("mate_id", mate_id)
        params.put("status", "r")

        MateAction.rejectMateRequest(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    Utils.alert(context, "선택한 대상의 요청을 거절했습니다")
                    getFriendList("w", 0)
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

    fun cancle(mate_id: String) {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("mate_id", mate_id)
        params.put("status", "rc")

        MateAction.cancle_mate(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    Utils.alert(context, "선택한 대상의 친구신청을 취소했습니다")
                    getFriendList("w", 0)
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


    fun block_cancle(mate_id: String) {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("mate_id", mate_id)

        MateAction.block_cancle(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok") {
                    Toast.makeText(context,"차단이 해제되었습니다.",Toast.LENGTH_SHORT).show()
                    getFriendList("b", 0)
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

    fun my_info() {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    println("response : $response")
                    if (result == "ok") {
                        val member = response.getJSONObject("Member")
                        recive_mate = Utils.getString(member, "recive_mate")
                        if (recive_mate == "N") {
                            swc_alarm_req_frd_on.visibility = View.GONE
                            swc_alarm_req_frd.visibility = View.VISIBLE
                        } else {
                            swc_alarm_req_frd_on.visibility = View.VISIBLE
                            swc_alarm_req_frd.visibility = View.GONE
                        }

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

            }
        })

    }


    override fun onDestroy() {
        super.onDestroy()
        if (reloadReciver != null) {
            context!!.unregisterReceiver(reloadReciver)
        }
    }

    override fun onBackPressed() {
        Utils.hideKeyboard(context)
        var intent = Intent()
        intent.putExtra("reset", "reset")
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


}
