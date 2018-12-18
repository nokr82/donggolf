package donggolf.android.activities

import android.app.AlertDialog
import android.content.*
import android.graphics.Paint
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.MotionEvent
import donggolf.android.R
import kotlinx.android.synthetic.main.activity_main_detail.*
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.actions.ContentAction
import donggolf.android.actions.InfoAction
import donggolf.android.actions.MemberAction
import donggolf.android.actions.PostAction
import donggolf.android.adapters.FullScreenImageAdapter
import donggolf.android.adapters.MainDeatilAdapter
import donggolf.android.base.*
import donggolf.android.models.Content
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class MainDetailActivity : RootActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    private  lateinit var  adapter : MainDeatilAdapter

    private lateinit var adverAdapter: FullScreenImageAdapter
    var adverImagePaths:ArrayList<String> = ArrayList<String>()

    var adPosition = 0

    var PICTURE_DETAIL = 1

    var detailowner: String? = ""

    var pressStartTime: Long?  = 0
    var pressedX: Float? = 0F
    var pressedY: Float? = 0F
    var stayedWithinClickDistance: Boolean? = false

    val MAX_CLICK_DURATION = 1000
    val MAX_CLICK_DISTANCE = 15

    lateinit var activity: MainDetailActivity

    var login_id = 0
    var writer = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_detail)

        detail_add_comment.paintFlags = detail_add_comment.getPaintFlags() or Paint.FAKE_BOLD_TEXT_FLAG

        context = this
        intent = getIntent()

        var dataObj : JSONObject = JSONObject();

        adapterData.add(dataObj)

        adapter = MainDeatilAdapter(context,R.layout.main_detail_listview_item,adapterData)

        main_detail_listview.adapter = adapter

        adapter.notifyDataSetChanged()

        activity = this as MainDetailActivity

        login_id = PrefUtils.getIntPreference(context, "member_id")

        var check = false

        adverAdapter = FullScreenImageAdapter(this, adverImagePaths)
        pagerVP.adapter = adverAdapter
        pagerVP.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                adPosition = position
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {
                circleLL.removeAllViews()
                for (i in adverImagePaths.indices) {
                    if (i == adPosition) {
//                        addDot(circleLL, true)
                    } else {
//                        addDot(circleLL, false)
                    }
                }
            }
        })

        pagerVP.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {

                    MotionEvent.ACTION_DOWN ->{

                        pressStartTime = System.currentTimeMillis();
                        pressedX = event.getX();
                        pressedY = event.getY();
                        stayedWithinClickDistance = true;

                        println("OnTouch : ACTION_DOWN")

                        return true
                    }

                    MotionEvent.ACTION_CANCEL->{

                        if (stayedWithinClickDistance!! && distance(pressedX!!, pressedY!!, event.getX(), event.getY()) > MAX_CLICK_DISTANCE) {
                            stayedWithinClickDistance = false;
                        }
                        return true

                    }

                    MotionEvent.ACTION_UP -> {

                        val pressDuration = System.currentTimeMillis() - pressStartTime!!
                        if (pressDuration < MAX_CLICK_DURATION && stayedWithinClickDistance!!) {
                        }

                        if (intent.hasExtra("id")) {
                            val id = intent.getStringExtra("id")
                            var intent = Intent(context, PictureDetailActivity::class.java);
                            intent.putExtra("id", id)
                            startActivityForResult(intent, PICTURE_DETAIL);

                            return true
                        }

                    }

                }

                return v?.onTouchEvent(event) ?: true
            }
        })


        finishLL.setOnClickListener {
            finish()
        }

        main_detail_gofindpicture.setOnClickListener {
            MoveFindPictureActivity()
        }

        plusBT.setOnClickListener {
            relativ_RL.visibility = View.VISIBLE

        }

        goneRL.setOnClickListener {
            relativ_RL.visibility = View.GONE
        }

        reportTV.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("신고하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        if (intent.getStringExtra("id") != null) {
                            val content_id = intent.getStringExtra("id")

                            var params = RequestParams()
                            params.put("content_id", content_id)
                            params.put("member_id", login_id)

                            PostAction.add_report(params, object : JsonHttpResponseHandler() {
                                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                                            val result = response!!.getString("result")
                                            if (result == "yes") {
                                                Toast.makeText(context, "이미 신고하셨습니다.", Toast.LENGTH_SHORT).show()
                                            }else {
                                                Toast.makeText(context, "신고 완료.", Toast.LENGTH_SHORT).show()
                                            }
                                }

                                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                                }
                            })

                        }

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        addfavoriteTV.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("보관하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        if (intent.getStringExtra("id") != null) {
                            val content_id = intent.getStringExtra("id")

                            var params = RequestParams()
                            params.put("content_id", content_id)
                            params.put("member_id", login_id)

                            PostAction.add_favorite_content(params, object : JsonHttpResponseHandler() {
                                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                                    val result = response!!.getString("result")
                                    if (result == "yes") {
                                        Toast.makeText(context, "이미 추가하셨습니다.", Toast.LENGTH_SHORT).show()
                                    }else {
                                        Toast.makeText(context, "추가 완료.", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                                }
                            })

                        }

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        deleteTV.setOnClickListener {
            delete()
        }

        modifyTV.setOnClickListener {
            modify()
        }

        addFriendTV.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("친구신청하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        if (intent.getStringExtra("id") != null) {
                            val content_id = intent.getStringExtra("id")

                            var params = RequestParams()
                            params.put("content_id", content_id)
                            params.put("mate_id", writer)
                            params.put("member_id", login_id)
                            params.put("category_id",0)
                            params.put("status","w")

                            PostAction.add_friend(params, object : JsonHttpResponseHandler() {
                                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                                    val result = response!!.getString("result")
                                    if (result == "yes") {
                                        Toast.makeText(context, "이미 친구신청을 하셨습니다.", Toast.LENGTH_SHORT).show()
                                    }else {
                                        Toast.makeText(context, "친구신청을 보냈습니다", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                                }
                            })

                        }

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        likeIV.setOnClickListener {
            if (intent.getStringExtra("id") != null) {
                val content_id = intent.getStringExtra("id")

                var params = RequestParams()
                params.put("content_id", content_id)
                params.put("member_id", login_id)

                PostAction.add_like(params, object : JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        val result = response!!.getString("result")
                        if (result == "yes") {
                            likeIV.setImageDrawable(resources.getDrawable(R.drawable.icon_like))
                            val likes = response.getJSONObject("Like")
                            heartcountTV.setText(likes.length().toString())
                            likecountTV.setText(likes.length().toString() + "명이 좋아합니다")
                        } else {
                            likeIV.setImageDrawable(resources.getDrawable(R.drawable.btn_cancel_like))
                            val likes = response.getJSONObject("Like")
                            heartcountTV.setText(likes.length().toString())
                            likecountTV.setText(likes.length().toString() + "명이 좋아합니다")
                        }
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                    }
                })
            }
        }

        getPost()
        getLooker()
    }

    fun MoveFindPictureActivity(){
        startActivity(Intent(this,FindPictureActivity::class.java))
    }

    fun modify(){
        if (intent.getStringExtra("id") != null) {
            val id = intent.getStringExtra("id")
            val intent = Intent(this, AddPostActivity::class.java)
            intent.putExtra("category",2)
            intent.putExtra("id",id)
            startActivity(intent)
        }
    }

    fun delete(){

        val builder = AlertDialog.Builder(context)
        builder
                .setMessage("정말 삭제하시겠습니까 ?")

                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                    if (intent.hasExtra("id")) {
                        val id = intent.getStringExtra("id")

                        ContentAction.deleteContent(id){
                            if(it){

                                intent = Intent()
                                intent.action = "DELETE_POST"
                                sendBroadcast(intent)

                                finish()

                            }else {

                            }

                        }
                    }

                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                })
        val alert = builder.create()
        alert.show()

    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        val distanceInPx = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        return pxToDp(distanceInPx)
    }

    private fun pxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }

    fun getPost(){
        if (intent.getStringExtra("id") != null){
            val id = intent.getStringExtra("id")
            login_id = PrefUtils.getIntPreference(context, "member_id")

            var params = RequestParams()
            params.put("id",id)
            params.put("member_id",login_id)

            PostAction.get_post(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    try {
                        val result = response!!.getString("result")
                        if (result == "ok") {

                            val data = response.getJSONObject("Content")

                            val created = Utils.getString(data,"created")
                            val title = Utils.getString(data,"title")
                            val text = Utils.getString(data,"text")
                            val member_id = Utils.getString(data,"member_id")
                            writer = member_id
                            val door_image = Utils.getString(data,"door_image")
                            val deleted = Utils.getString(data,"deleted")
                            val Looker = response.getJSONArray("Looker")
                            val Like = response.getJSONArray("Like")
                            val cht_yn = Utils.getString(data,"cht_yn")
                            val cmt_yn = Utils.getString(data,"cmt_yn")

                            val likeDiv = response.getString("LikeDiv")

                            if (likeDiv == "N") {
                                likeIV.setImageDrawable(resources.getDrawable(R.drawable.icon_like))
                            } else {
                                likeIV.setImageDrawable(resources.getDrawable(R.drawable.btn_cancel_like))
                            }

                            val tags = response.getJSONArray("tags")
                            val imageDatas = response.getJSONArray("ContentImgs")

                            if (tags != null && tags.length() > 0 ){
                                var hashtags: String = ""

                                for (i in 0 until tags.length()){
                                    var json = tags.get(i) as JSONObject
                                    var MemberTags = json.getJSONObject("MemberTags")
                                    val division = Utils.getString(MemberTags,"division")

                                    if (division == "1"){
                                        val tag = Utils.getString(MemberTags,"tag")
                                        hashtags += "#"+tag + "  "
                                    }
                                }
                                hashtagTV.setText(hashtags)
                            }

                            if (imageDatas != null && imageDatas.length() > 0){
                                var imagePaths: ArrayList<String> = ArrayList<String>()

                                for (i in 0 until imageDatas.length()){
                                    var json = imageDatas.get(i) as JSONObject

                                    var contentFile = json.getJSONObject("contentFile")
                                    var type = Utils.getInt(contentFile,"type")
                                    if (type == 1) {
                                        val path = Utils.getString(contentFile, "image_uri")
                                        imagePaths.add(path)
                                    }
                                }

                                if (adverImagePaths != null){
                                    adverImagePaths.clear()
                                }

                                for (i in 0 until imagePaths.size){
                                    val image = Config.url + imagePaths.get(i)
                                    adverImagePaths.add(image)
                                }
                                adverAdapter.notifyDataSetChanged()
                            }

                            dateTV.setText(created)
                            viewTV.setText(Looker.length().toString())
                            heartcountTV.setText(Like.length().toString())
                            titleTV.setText(title)
                            textTV.setText(text)
                            likecountTV.setText(Like.length().toString() + "명이 좋아합니다")

                            if (cht_yn  == "Y"){

                            } else {
                            }

                            if (cmt_yn == "Y"){
                                cmtTV.visibility = View.GONE
                                cmtET.visibility = View.VISIBLE
                            } else {
                                cmtTV.visibility = View.VISIBLE
                                cmtET.visibility = View.GONE
                            }

                            val params = RequestParams()
                            params.put("member_id", member_id)

                            MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
                                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                                    println("success==========")
                                    val result = response!!.getString("result")
                                    if (result == "ok") {
                                        val member = response.getJSONObject("Member")

                                        val id = Utils.getString(member,"id")
                                        val nick = Utils.getString(member, "nick")
                                        val status_msg = Utils.getString(member, "status_msg")

                                        println("nick ------$nick")

                                        nickNameTV.setText(nick)
                                        statusmsgTV.setText(status_msg)

                                        if (login_id == id.toInt()){
                                            reportTV.visibility = View.GONE
                                            addFriendTV.visibility = View.GONE
                                            addfavoriteTV.visibility = View.GONE
                                        } else {
                                            modifyTV.visibility = View.GONE
                                            deleteTV.visibility = View.GONE
                                        }

                                    }
                                }

                                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                                    println("---------fail")
                                }
                            })

                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                }
            })

        }
    }

    fun getLooker(){
        val id = intent.getStringExtra("id")
        login_id = PrefUtils.getIntPreference(context, "member_id")

        var params = RequestParams()
        params.put("content_id",id)
        params.put("member_id",login_id)

        PostAction.add_looker(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok" || result == "yes") {
                    val Looker = response.getJSONArray("Looker")
                    viewTV.setText(Looker.length().toString())
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

            }
        })
    }

}
