package donggolf.android.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import org.json.JSONException
import org.json.JSONObject

class RequestFriendActivity : RootActivity() {

    lateinit var context : Context

    var mateRequestList = ArrayList<JSONObject>()
    lateinit var matesRequestAdapter: MateManageAdapter
    var mateList = ArrayList<Int>()

    var checkAll = false

    val SELECT_CATEGORY = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_friend)

        context = this

        matesRequestAdapter = MateManageAdapter(context, R.layout.item_mate_manage, mateRequestList)
        requestFriends.adapter = matesRequestAdapter

        val type = intent.getStringExtra("type")
        if (type == "waiting") {
            println("type----$type")
            getFriendList("w", 0)
        }/* else if (type == "block") {

        } else if (type == "first") {
            getFriendList("m",1)
        } else {
            var cateID = intent.getIntExtra("category_id", 0)
            getFriendList("m", cateID)
        }*/

        //전체선택버튼
        frdReq_check_all.setOnClickListener {
            if (!checkAll){
                checkAll = true
                checkIcon.visibility = View.VISIBLE


//                matesRequestAdapter.isCheckedMate = true
//
//                for (i in 0 until mateRequestList.size) {
//                    matesRequestAdapter.isCheckedMate = true
//
//                }
//
//                matesRequestAdapter.notifyDataSetChanged()

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
                    println(response)
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
                        getFriendList("w",0)
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
    }

    fun getFriendList(status : String, category_id : Int) {
        //친구 리스트 받아오기
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("status", status)
        params.put("category_id", category_id)

        MateAction.get_mates_list(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val mates = response.getJSONArray("mates")
                        mateRequestList.clear()
                        for (i in 0 until mates.length()) {
                            val mate = mates[i] as JSONObject

                            mate.put("check", false)

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
                    val selCateg = data!!.getIntExtra("CategoryID", 1)

                    acceptMates(selCateg)
                }
            }
        }
    }

    fun acceptMates(category_id: Int) {
        mateList.clear()

        for (i in 0 until mateRequestList.size) {
            if(mateRequestList.get(i).getBoolean("check")) {
                mateList.add(mateRequestList.get(i).getInt("mate_id"))
            }
        }

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mateList.get(0))
        params.put("category_id", category_id)
        params.put("status", "m")

        MateAction.accept_mates(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
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




}
