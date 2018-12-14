package donggolf.android.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.actions.PostAction
import donggolf.android.adapters.MyPostAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_my_post_mng.*
import kotlinx.android.synthetic.main.item_my_post_manage.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MyPostMngActivity : RootActivity() {

    lateinit var context : Context
    var member_id = 0

    //adapter 3개 연결
    lateinit var myPostAdapter : MyPostAdapter
    lateinit var myCommentPostAdapter: MyPostAdapter
    lateinit var myStoredPostAdapter: MyPostAdapter

    var myPostList = ArrayList<JSONObject>()
    var myCommentPostList = ArrayList<JSONObject>()
    var myStoredPostList = ArrayList<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_post_mng)

        context = this
        member_id = PrefUtils.getIntPreference(context,"member_id")

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

                            val data = response!!.getJSONArray("content")
                            myPostList.clear()
                            for (i in 0 until data.length()){
                                myPostList.add(data[i] as JSONObject)
                            }
                            myPostAdapter.notifyDataSetChanged()

                            myPost_myPostTV.text = "내 게시물(" + data.length() +")"
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
                        println("내 글 보기 :: $response")
                        val result = response!!.getString("result")
                        if ("ok" == result) {

                            val data = response!!.getJSONArray("content")
                            myCommentPostList.clear()
                            for (i in 0 until data.length()){
                                myCommentPostList.add(data[i] as JSONObject)
                            }
                            myCommentPostAdapter.notifyDataSetChanged()

                            myPostContTV.text = "댓글단 글(" + data.length() +")"
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

                            val data = response!!.getJSONArray("content")
                            myStoredPostList.clear()
                            for (i in 0 until data.length()){
                                myStoredPostList.add(data[i] as JSONObject)
                            }
                            myStoredPostAdapter.notifyDataSetChanged()

                            myPost_commentTV.text = "보관 글(" + data.length() +")"
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

                        val data = response!!.getJSONArray("content")

                        myPostList.clear()
                        for (i in 0 until data.length()){
                            myPostList.add(data[i] as JSONObject)
                        }
                        myPostAdapter.notifyDataSetChanged()
                        myPost_myPostTV.text = "내 게시글(" + data.length() +")"
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

        myPost_myPostTV.text = "내 게시글"
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
