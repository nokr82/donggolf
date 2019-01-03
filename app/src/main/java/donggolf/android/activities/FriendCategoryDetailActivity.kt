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
import donggolf.android.adapters.MateListAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_friend_category_detail.*
import org.json.JSONException
import org.json.JSONObject

class FriendCategoryDetailActivity : RootActivity() {

    lateinit var context: Context

    var mateList = ArrayList<JSONObject>()
    var mateUpdate = ArrayList<Int>()
    lateinit var friendAdapter : MateListAdapter

    var checkAll = false
    val MOVEtoCATEGORY = 102
    var category_id = 0

    val CATEGORY_SETTING = 301

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_category_detail)

        context = this

        friendAdapter = MateListAdapter(context, R.layout.item_mate_manage, mateList)
        groupFriendLV.adapter = friendAdapter

        //받아온 데이터로 세팅
        val getItt = intent.getStringExtra("category_title")
        titleFrdCateTV.text = getItt

        val type = intent.getStringExtra("type")
        if (type == "block") {
            titleFrdCateTV.text = "차단목록"
            //Block 테이블 조회
            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

            MateAction.blockList(params, object : JsonHttpResponseHandler(){
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val blocks = response.getJSONArray("blocks")
                        mateList.clear()
                        for (i in 0 until blocks.length()) {
                            val mate = blocks[i] as JSONObject

                            mate.put("check", false)

                            mateList.add(mate)
                        }
                        friendAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                    println(errorResponse)
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                    println(responseString)
                }
            })

        } else {
            var cateID = intent.getIntExtra("category_id", 0)
            category_id = cateID
            getFriendList("m", cateID)
        }

        //전체 체크 리스너
        frdReq_check_all.setOnClickListener {
            //checkIcon.visibility = View.VISIBLE
            if (!checkAll){
                checkAll = true
                checkIcon.visibility = View.VISIBLE

            } else {
                checkAll = false
                checkIcon.visibility = View.GONE

            }

            mateUpdate.clear()

            for (i in 0 until mateList.size) {
                mateList.get(i).put("check", checkAll)
                mateUpdate.add(mateList.get(i).getInt("mate_id"))
            }
            friendAdapter.notifyDataSetChanged()
            println(mateUpdate)
        }

        //설정
        categoryManagementIV.setOnClickListener {
            if (type == "block"){
                val itt = Intent(context, FriendGrpDetailSettingActivity::class.java)
                itt.putExtra("groupTitle", "차단목록")
                startActivity(itt)
            }else {
                val itt = Intent(context, FriendGrpDetailSettingActivity::class.java)
                itt.putExtra("groupTitle", intent.getStringExtra("category_title"))
                itt.putExtra("cate_id", category_id)
                startActivityForResult(itt,CATEGORY_SETTING)
            }
        }

        //이동
        moveTV.setOnClickListener {
            //카테고리 고르라고
            startActivityForResult(Intent(context,FriendReqSelectCategoryActivity::class.java),MOVEtoCATEGORY)
        }

        blockTV.setOnClickListener {
            //
            mateList.clear()

            for (i in 0 until mateList.size) {
                if(mateList.get(i).getBoolean("check")) {
                    mateUpdate.add(mateList.get(i).getInt("mate_id"))
                }
            }
            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
            params.put("mate_id", mateUpdate)

            MateAction.blockMember(params, object : JsonHttpResponseHandler(){
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        getFriendList("m",category_id)
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                    println(errorResponse)
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                    println(responseString)
                }
            })
            //
        }

        btn_back.setOnClickListener {
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                MOVEtoCATEGORY -> {
                    val selCateg = data!!.getIntExtra("CategoryID", 1)
                    //println("category id : $selCateg")
                    moveMateOtherCategory(selCateg)
                }

                CATEGORY_SETTING -> {
                    var title = data!!.getStringExtra("title")
                    titleFrdCateTV.text = title
                }

            }
        }
    }

    fun moveMateOtherCategory(selCateg : Int){

        mateUpdate.clear()
        for (i in 0 until mateList.size) {
            if(mateList.get(i).getBoolean("check")) {
                mateUpdate.add(mateList.get(i).getInt("mate_id"))
            }
        }
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mateUpdate)//넘길 친구목록
        params.put("category_id", selCateg)//이동할 카테고리 id

        println(params)

        MateAction.update_mates_status(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                //getFriendList("m",category_id)
                val result = response!!.getString("result")
                if (result == "ok") {
                    val mates = response.getJSONArray("mates")
                    mateList.clear()
                    groupFriendLV.removeAllViews()
                    for (i in 0 until mates.length()) {
                        val mate = mates[i] as JSONObject

                        mate.put("check", false)

                        mateList.add(mate)
                    }
                    friendAdapter.notifyDataSetChanged()
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

    fun getFriendList(status : String, category_id : Int) {
        //친구 리스트 받아오기
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("status", status)
        params.put("category_id", category_id)
        params.put("division",1)

        MateAction.mateList(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val mates = response.getJSONArray("mates")
                        mateList.clear()
                        for (i in 0 until mates.length()) {
                            val mate = mates[i] as JSONObject

                            mate.put("check", false)

                            mateList.add(mate)
                        }
                        friendAdapter.notifyDataSetChanged()
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
