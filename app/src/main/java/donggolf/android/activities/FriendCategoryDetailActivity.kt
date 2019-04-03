package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MateAction
import donggolf.android.adapters.MateListAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_friend_category_detail.*
import org.json.JSONException
import org.json.JSONObject

class FriendCategoryDetailActivity : RootActivity() {

    lateinit var context: Context

    var mateList = ArrayList<JSONObject>()
    var mateUpdate = ArrayList<Int>()
    var matemove = ArrayList<Int>()
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

        groupFriendLV.setOnItemClickListener { parent, view, position, id ->
            val item = mateList.get(position)

            val mateMember = item.getJSONObject("MateMember")
            val mate_id = Utils.getString(mateMember,"id")

            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("member_id", mate_id)
            startActivity(intent)
        }

        val type = intent.getStringExtra("type")
        if (type == "block") {
            titleFrdCateTV.text = "차단목록"
            //Block 테이블 조회
            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

            MateAction.blockList(params, object : JsonHttpResponseHandler(){
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
//                    println(response)
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
//                    println(errorResponse)
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
//                    println(responseString)
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
//            println(mateUpdate)
        }

        //설정
        categorySettingLL.setOnClickListener {
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
            matemove.clear()
            for (i in 0 until mateList.size) {
                //Log.d("메이트리스트",mateList.get(i).toString())
                //Log.d("불리언",mateList.get(i).getBoolean("check").toString())
                if (mateList.get(i).getBoolean("check")){
                    matemove.add(mateList.get(i).getInt("mate_id"))

                }else{

                }

            }
            //카테고리 고르라고
            val intent = Intent(context, FriendReqSelectCategoryActivity::class.java)
            intent.putExtra("category_id",category_id.toString())
            intent.putExtra("mate_id",matemove)
            //Log.d("메이트아디",matemove.toString())
            startActivityForResult(intent,MOVEtoCATEGORY)
        }

        blockTV.setOnClickListener {
            //
//            mateList.clear()

            val builder = AlertDialog.Builder(context)
            builder.setMessage("선택하신 골퍼를 차단 하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        for (i in 0 until mateList.size) {
                            if(mateList.get(i).getBoolean("check")) {
                                mateUpdate.add(mateList.get(i).getInt("mate_id"))
                            }
                        }

                        if (mateUpdate.size > 0) {

                            val params = RequestParams()
                            params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
                            params.put("mate_id", mateUpdate)

                            MateAction.blockMember(params, object : JsonHttpResponseHandler() {
                                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                                    //println(response)
                                    val result = response!!.getString("result")
                                    if (result == "ok") {
                                        getFriendList("m", category_id)
                                    }
                                }

                                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                                    //println(errorResponse)
                                }

                                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                                    //println(responseString)
                                }
                            })
                        } else {
                            Toast.makeText(context,"선택하신 골퍼가 없습니다.", Toast.LENGTH_SHORT).show()
                            return@OnClickListener
                        }
                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

            //
        }

        btn_back.setOnClickListener {
            Utils.hideKeyboard(this)
            var intent = Intent()
            intent.putExtra("reset","reset")
            setResult(Activity.RESULT_OK,intent)
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                MOVEtoCATEGORY -> {

                    if (data != null) {
                        val category_id = data!!.getStringExtra("category_id")
                        val mate_id = data!!.getSerializableExtra("mate_id")
                        val get_category_id = data!!.getStringExtra("get_category_id")
                        //Log.d("스트링",mate_id.toString())
                        //Log.d("스트링",get_category_id)
                        //Log.d("스트링",category_id)
                        moveMateOtherCategory(mate_id as ArrayList<Int>, category_id,get_category_id)
                        var intent = Intent()
                        intent.putExtra("reset","reset")
                        setResult(Activity.RESULT_OK,intent)
                        finish()
                    }
                }

                CATEGORY_SETTING -> {
                    var title = data!!.getStringExtra("title")
                    titleFrdCateTV.text = title
                }

            }
        }
    }

    fun moveMateOtherCategory(member_id:ArrayList<Int> ,nowCategory_id:String,selCateg:String){

        mateUpdate.clear()
        for (i in 0 until mateList.size) {
            if(mateList.get(i).getBoolean("check")) {
                mateUpdate.add(mateList.get(i).getInt("mate_id"))
            }
        }
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
//        params.put("mate_id", mateUpdate)//넘길 친구목록
        if (member_id != null){
            if (member_id!!.size != 0){
                for (i in 0..member_id!!.size - 1){
                    params.put("mate_id[" + i + "]",  member_id.get(i))
                }
            }
        }
        params.put("category_id", nowCategory_id)//이동할 카테고리 id
//        params.put("now_category_id", nowCategory_id)//이동할 카테고리 id

        MateAction.update_mates_status(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                //println(response)
                //getFriendList("m",category_id)
                val result = response!!.getString("result")
                //println("----result ---- $result")
                if (result == "ok") {
                    val mates = response.getJSONArray("mates")
                    mateList.clear()
//                    groupFriendLV.removeAllViews()
                    for (i in 0 until mates.length()) {
                        val mate = mates[i] as JSONObject

                        mate.put("check", false)

                        mateList.add(mate)
                    }
                    finish()
                    friendAdapter.notifyDataSetChanged()
                } else if (result == "empty") {
                    mateList.clear()
                    friendAdapter.notifyDataSetChanged()
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
                //println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                //println(errorResponse)
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
