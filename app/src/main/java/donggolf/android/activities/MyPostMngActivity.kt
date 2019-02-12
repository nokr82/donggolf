package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.squareup.okhttp.internal.Util
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.actions.PostAction
import donggolf.android.adapters.MyPostAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_my_post_mng.*
import kotlinx.android.synthetic.main.item_my_post_manage.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MyPostMngActivity : RootActivity() {

    lateinit var context : Context
    var member_id = 0
    var type = ""
    var nick = ""

    //adapter 3개 연결
    lateinit var myPostAdapter : MyPostAdapter
    lateinit var myCommentPostAdapter: MyPostAdapter
    lateinit var myStoredPostAdapter: MyPostAdapter

    var myPostList = ArrayList<JSONObject>()
    var myCommentPostList = ArrayList<JSONObject>()
    var myStoredPostList = ArrayList<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activit

                y_my_post_mng)

        context = this
        member_id = PrefUtils.getIntPreference(context,"member_id")

        var intent = getIntent()

        if (intent.getStringExtra("founder") != null){
            member_id = intent.getStringExtra("founder").toInt()
            println("-----------member_id$member_id")
            type = intent.getStringExtra("type")
            nick = intent.getStringExtra("nick")
            titleTV.setText(nick)
        }

        btn_back.setOnClickListener {
            finish()
        }

        myPostAdapter = MyPostAdapter(context, R.layout.item_my_post_manage, myPostList)
        myCommentPostAdapter = MyPostAdapter(context, R.layout.item_my_post_manage, myCommentPostList)
        myStoredPostAdapter = MyPostAdapter(context, R.layout.item_my_post_manage, myStoredPostList)

        myPostLV.adapter = myPostAdapter
        myCommentLV.adapter = myCommentPostAdapter
        myStorePostLV.adapter = myStoredPostAdapter

        setTabInit()

        myPostTab.setOnClickListener {
            //뷰
            setTabInit()
            myPost_myPostTV.setTextColor(Color.parseColor("#0EDA2F"))
            myPost_myPost_view.visibility = View.VISIBLE
            myPostLV.visibility = View.VISIBLE

            val params = RequestParams()
            params.put("member_id", member_id)

            MemberAction.my_post_load(params,object : JsonHttpResponseHandler(){

                override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {

                    try {
                        println("내 글 보기 :: $response")
                        val result = response!!.getString("result")
                        if ("ok" == result) {

                            val data = response!!.getJSONArray("contents")
                            myPostList.clear()
                            for (i in 0 until data.length()){
                                myPostList.add(data[i] as JSONObject)
                                myPostList[i].put("willDel", false)
                            }
                            myPostAdapter.notifyDataSetChanged()

                            if (nick != ""){
                                myPost_myPostTV.text = nick + "님의 게시물(" + data.length() +")"
                            } else {
                                myPost_myPostTV.text = "내 게시물(" + data.length() +")"
                            }
//                            myPost_myPostTV.text = "내 게시물(" + data.length() +")"
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONArray?) {
                    println("DataLoad failed. Because of")
                    println(errorResponse)
                }

            })
        }
        myPostLV.setOnItemClickListener { parent, view, position, id ->
            val data = myPostList.get(position)
            val content = data.getJSONObject("Content")
            var content_id =  Utils.getString(content,"id")

            MoveMainDetailActivity(content_id)
        }





        myCommentTab.setOnClickListener {
            //뷰
            setTabInit()
            myPost_commentTV.setTextColor(Color.parseColor("#0EDA2F"))
            myPost_commentPost_view.visibility = View.VISIBLE
            myCommentLV.visibility = View.VISIBLE

            val params = RequestParams()
            params.put("member_id", member_id)
            params.put("type", "comment")

            MemberAction.my_post_load(params,object : JsonHttpResponseHandler(){

                override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {

                    try {
                        println("댓글단 글 보기 :: $response")
                        val result = response!!.getString("result")
                        if ("ok" == result) {

                            val data = response!!.getJSONArray("contents")
                            myCommentPostList.clear()
                            for (i in 0 until data.length()){
                                myCommentPostList.add(data[i] as JSONObject)
                                myCommentPostList[i].put("willDel", false)
                            }
                            myCommentPostAdapter.notifyDataSetChanged()

                            myPost_commentTV.text = "댓글단 글(" + data.length() +")"
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONArray?) {
                    println("DataLoad failed. Because of")
                    println(errorResponse)
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                    println(responseString)
                }

            })
        }
        myCommentLV.setOnItemClickListener { parent, view, position, id ->
            val data = myCommentPostList.get(position)
            val content = data.getJSONObject("Content")
            var content_id =  Utils.getString(content,"id")

            MoveMainDetailActivity(content_id)
        }
        myStorePostTab.setOnClickListener {
            //뷰
            setTabInit()
            myPost_storeTV.setTextColor(Color.parseColor("#0EDA2F"))
            myPost_storePost_view.visibility = View.VISIBLE
            myStorePostLV.visibility = View.VISIBLE

            val params = RequestParams()
            params.put("member_id", member_id)
            params.put("type", "stored")

            MemberAction.my_post_load(params,object : JsonHttpResponseHandler(){

                override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {

                    try {
                        println("내 보관 글 보기 :: $response")
                        val result = response!!.getString("result")
                        if ("ok" == result) {

                            val data = response!!.getJSONArray("contents")

                                myStoredPostList.clear()
                                for (i in 0 until data.length()) {
                                    myStoredPostList.add(data[i] as JSONObject)
                                    myStoredPostList.get(i).put("willDel", false)
                                }
                                myStoredPostAdapter.notifyDataSetChanged()

                                myPost_storeTV.text = "보관 글(" + data.length() + ")"

                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONArray?) {
                    println("DataLoad failed. Because of")
                    println(errorResponse)
                }

            })
        }
        myStorePostLV.setOnItemClickListener { parent, view, position, id ->
            val data = myStoredPostList.get(position)
            val content = data.getJSONObject("Content")
            var content_id =  Utils.getString(content,"id")

            MoveMainDetailActivity(content_id)
        }
        //보관글 지우기
        myStorePostLV.setOnItemLongClickListener { parent, view, position, id ->


            myStoredPostList[position].put("willDel",true)
            myStoredPostAdapter.notifyDataSetChanged()

            val data = myStoredPostList.get(position)
            val contentOb = data.getJSONObject("Content")

            var contIdx = Utils.getString(contentOb,"id")

            view.item_btn_del.setOnClickListener {

                if (PrefUtils.getIntPreference(context,"member_id") != member_id){
                    Toast.makeText(context,"타인의 게시물을 삭제할수없습니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val params = RequestParams()
                params.put("content_id", contIdx)
                params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

                PostAction.delete_favorite_content(params, object : JsonHttpResponseHandler(){
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        try {
                            val result = response!!.getString("result")
                            if (result == "ok") {
                                myStoredPostList.remove(myStoredPostList.removeAt(position))
                                myStoredPostAdapter.notifyDataSetChanged()
                                Toast.makeText(context,"보관글을 삭제했습니다", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "보관글 삭제에 실패했습니다", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e : JSONException) {
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

            true
        }


        //기본 화면
        myPost_myPostTV.setTextColor(Color.parseColor("#0EDA2F"))
        myPost_myPost_view.visibility = View.VISIBLE
        myPostLV.visibility = View.VISIBLE

        val params = RequestParams()
        params.put("member_id", member_id)

        MemberAction.my_post_load(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {

                try {
                    println("내 글 보기 :: $response")
                    val result = response!!.getString("result")
                    if ("ok" == result) {

                        val data = response!!.getJSONArray("contents")

                        myPostList.clear()
                        for (i in 0 until data.length()){
                            myPostList.add(data[i] as JSONObject)
                            myPostList[i].put("willDel", false)
                        }
                        myPostAdapter.notifyDataSetChanged()

                        if (nick != ""){
                            myPost_myPostTV.text = nick + "님의 게시물(" + data.length() +")"
                        } else {
                            myPost_myPostTV.text = "내 게시물(" + data.length() +")"
                        }
//                        myPost_myPostTV.text = "내 게시글(" + data.length() +")"
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONArray?) {
                println("DataLoad failed. Because of")
                println(errorResponse)
            }

        })
    }



    fun MoveMainDetailActivity(id : String){
        var intent: Intent = Intent(context, MainDetailActivity::class.java)
        intent.putExtra("id",id)
        startActivity(intent)
    }




    fun setTabInit() {
        myPost_myPostTV.setTextColor(Color.parseColor("#000000"))
        myPost_commentTV.setTextColor(Color.parseColor("#000000"))
        myPost_storeTV.setTextColor(Color.parseColor("#000000"))

        myPost_myPost_view.visibility = View.INVISIBLE
        myPost_commentPost_view.visibility = View.INVISIBLE
        myPost_storePost_view.visibility = View.INVISIBLE

        myPostLV.visibility = View.GONE
        myCommentLV.visibility = View.GONE
        myStorePostLV.visibility = View.GONE

//        myPost_myPostTV.text = "내 게시글"
        if (nick != ""){
            myPost_myPostTV.text = nick + "님의 게시물"
        } else {
            myPost_myPostTV.text = "내 게시물"
        }


        myPost_commentTV.text = "댓글단 글"
        myPost_storeTV.text = "보관 글"
    }

    /*class MyPostPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(i: Int): Fragment {

            var fragment: Fragment

            val args = Bundle()
            when (i) {
                0 -> {
                    fragment = FreeFragment()
                    fragment.arguments = args

                    return fragment
                }
                1 -> {
                    fragment = ChatFragment()
                    fragment.arguments = args

                    return fragment
                }
                2 -> {
                    fragment = FushFragment()
                    fragment.arguments = args
                    return fragment
                }
                else -> {
                    fragment = InfoFragment()
                    fragment.arguments = args
                    return fragment
                }
            }
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return ""
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }*/
}
