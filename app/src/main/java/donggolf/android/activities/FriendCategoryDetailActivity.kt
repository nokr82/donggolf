package donggolf.android.activities

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
import kotlinx.android.synthetic.main.activity_friend_category_detail.*
import org.json.JSONException
import org.json.JSONObject

class FriendCategoryDetailActivity : RootActivity() {

    lateinit var context: Context

    var mateList = ArrayList<JSONObject>()
    var mateBlock = ArrayList<Int>()
    var mateMove = ArrayList<Int>()
    lateinit var friendAdapter : MateManageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_category_detail)

        context = this

        friendAdapter = MateManageAdapter(context, R.layout.item_mate_manage, mateList)


        //val getItt = intent.getStringExtra("groupTitle")
        //titleFrdCateTV.text = getItt
        val type = intent.getStringExtra("type")

        if (type == "block") {
            //Block 테이블 조회
        } else if (type == "first") {
            getFriendList("m",1)
        } else {
            var cateID = intent.getIntExtra("category_id", 0)
            getFriendList("m", cateID)
        }

        frdReq_check_all.setOnClickListener {
            checkIcon.visibility = View.VISIBLE
        }

        categoryManagementIV.setOnClickListener {
            val itt = Intent(context, FriendGrpDetailSettingActivity::class.java)
            //itt.putExtra("groupTitle",)
            startActivity(itt)
        }
    }

    fun getFriendList(status : String, category_id : Int) {
        //친구 리스트 받아오기
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("status", status)
        params.put("category_id", category_id)

        MateAction.get_mates_list(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
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
