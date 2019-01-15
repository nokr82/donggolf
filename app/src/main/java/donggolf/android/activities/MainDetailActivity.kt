package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.MotionEvent
import donggolf.android.R
import kotlinx.android.synthetic.main.activity_main_detail.*
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import com.squareup.okhttp.internal.Util
import cz.msebera.android.httpclient.Header
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.actions.*
import donggolf.android.adapters.FullScreenImageAdapter
import donggolf.android.adapters.MainDeatilAdapter
import donggolf.android.base.*
import donggolf.android.models.Content
import kotlinx.android.synthetic.main.dlg_comment_menu.view.*
import kotlinx.android.synthetic.main.dlg_post_menu.view.*
import kotlinx.android.synthetic.main.item_chat_member_list.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainDetailActivity : RootActivity() {

    private lateinit var context: Context

    private  var commentList = ArrayList<JSONObject>()
    private var commentBlockList = ArrayList<JSONObject>()

    private  lateinit var  commentAdapter : MainDeatilAdapter

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

    //lateinit var activity: MainDetailActivity

    var login_id = 0
    var writer = "0"
    var content_id = 0
    var commentType = ""
    var commentParent = ""
    var blockYN = ""

    var MODIFYS = 70

    var x = 0.0f

    lateinit var video:Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_detail)

        detail_add_commentTV.paintFlags = detail_add_commentTV.paintFlags or Paint.FAKE_BOLD_TEXT_FLAG

        context = this
        intent = intent
        login_id = PrefUtils.getIntPreference(context, "member_id")

        cmtET.hint = ""
        //댓글 관련 어댑터
        commentAdapter = MainDeatilAdapter(context,R.layout.main_detail_listview_item,commentList)

        commentListLV.adapter = commentAdapter
        commentListLV.isExpanded = true

        commentAdapter.notifyDataSetChanged()

        videoVV.setOnPreparedListener { mp -> mp.isLooping = true }
        var mediaController: MediaController = MediaController(this);
        videoVV.setMediaController(mediaController)

        videoviewTV.setOnClickListener {
            if (videoviewTV.text.toString() == "동영상 보기") {
                videoVV.visibility = View.VISIBLE
                videoVV.start()
                videoVV.setVideoURI(video)
                videoVV.setOnPreparedListener { mp -> mp.isLooping = true }
                videoviewTV.setText("동영상 숨기기")
                pagerVP.visibility = View.GONE
            } else {
                videoviewTV.setText("동영상 보기")
                videoVV.visibility = View.GONE
                pagerVP.visibility = View.VISIBLE
            }
        }

        //댓글 리스트뷰 롱클릭
        commentListLV.setOnItemLongClickListener { parent, view, position, id ->


            var commenter = commentList[position].getInt("cmt_wrt_id")

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_comment_menu, null) //사용자 정의 다이얼로그 xml 붙이기
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlg_comment_delTV.visibility = View.GONE
            dialogView.dlg_comment_blockTV.visibility = View.GONE


            //댓삭
            if (commenter == login_id){
                dialogView.dlg_comment_delTV.visibility = View.VISIBLE
                dialogView.dlg_comment_delTV.setOnClickListener {
                    if (PrefUtils.getIntPreference(context, "member_id") == -1){
                        Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val params = RequestParams()
                    params.put("cont_id", content_id)
                    params.put("commenter", commenter)
                    params.put("comment_id", commentList[position].getInt("comment_id"))

                    CommentAction.delete_content_comment(params, object :JsonHttpResponseHandler(){
                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                            println(response)

                            val result = response!!.getString("result")
                            if (result == "ok"){
                                //할일 : 리스트뷰에서 아이템을 지운다

                                commentAdapter.removeItem(position)
                                commentAdapter.notifyDataSetChanged()

                                alert.dismiss()
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

            //댓글복사
            dialogView.dlg_comment_copyTV.setOnClickListener {

                if (PrefUtils.getIntPreference(context, "member_id") == -1){
                    Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                //클립보드 사용 코드
                val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Golf", commentList[position].getString("comment_content")) //클립보드에 ID라는 이름표로 id 값을 복사하여 저장
                clipboardManager.primaryClip = clipData

                Toast.makeText(context, "댓글이 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()

                alert.dismiss()
            }


            if (login_id.toString() == writer){
                dialogView.dlg_comment_blockTV.visibility = View.VISIBLE

                val json = commentList[position].getJSONObject("ContentComment")
                val blocked_yn = Utils.getString(json,"block_yn")

                if (blocked_yn == "Y"){
                    dialogView.dlg_comment_blockTV.text = "차단해제"
                    blockYN = "unblock"
                } else {
                    dialogView.dlg_comment_blockTV.text = "차단하기"
                    blockYN = "block"
                }

                //댓글 작성자 게시글에 차단
                dialogView.dlg_comment_blockTV.setOnClickListener {
                    /*val json = commentList.get(position)
                    val data = json.getJSONObject("")*/
                    var cmt_wrt_id = commentList[position].getInt("cmt_wrt_id")

                    val params = RequestParams()
                    params.put("content_id", content_id)
                    params.put("writer", writer)
                    params.put("commenter", cmt_wrt_id)
                    params.put("status", blockYN)

                    CommentAction.content_commenter_ben(params, object : JsonHttpResponseHandler(){
                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                            try {
                                println(response)
                                //차단 성공하면 표시하고 토스트
                                val result = response!!.getString("result")
                                if (result == "ok") {
                                    //아이고 의미없다
                                    /*var message = response.getString("message")
                                    if (message == "registerd") {
                                        commentList[position].put("changedBlockYN", "Y")
                                        commentList[position].put("block_yn", "Y")
                                        commentAdapter.notifyDataSetChanged()
                                    } else {
                                        commentList[position].put("changedBlockYN", "Y")
                                        commentList[position].put("block_yn", "N")
                                        commentAdapter.notifyDataSetChanged()
                                    }*/
                                    commentList.clear()
                                    getComments()

                                } else {
                                    commentList[position].put("changedBlockYN", "N")
                                }
                            }catch (e : JSONException){
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                            println(errorResponse)
                        }

                        override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                            println(responseString)
                        }
                    })

                    alert.dismiss()
                }
            }

            true
        }


        //댓글 달기
        detail_add_commentTV.setOnClickListener {
            if (PrefUtils.getIntPreference(context, "member_id") == -1){
                Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var comment = Utils.getString(cmtET)
            if (comment == "" || comment == null){
                Toast.makeText(context,"빈칸은 입력하실 수 없습니다.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val params = RequestParams()
            params.put("cont_id", content_id)
            params.put("member_id", login_id)
            params.put("nick", PrefUtils.getStringPreference(context,"login_nick"))
            params.put("comment", comment)
            params.put("type", commentType)
            params.put("parent", commentParent)

            CommentAction.comment_at_content(params,object :JsonHttpResponseHandler(){
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok"){
                        val comments = response.getJSONObject("comments")
                        commentList.add(comments)
                        commentAdapter.notifyDataSetChanged()
                        cmtET.setText("")
                        cmtET.hint = ""
                        Utils.hideKeyboard(this@MainDetailActivity)
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

        //대댓글
        commentListLV.setOnItemClickListener { parent, view, position, id ->
            if (PrefUtils.getIntPreference(context, "member_id") == -1){
                Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }

            val data = commentList.get(position).getJSONObject("ContentComment")

            var parentType = Utils.getString(data,"type")
            if (parentType == "d") {
                commentType = "r"
                commentParent = Utils.getString(data,"id")
                cmtET.hint = Utils.getString(data,"nick") + "님의 댓글에 답글"
            } else {
                commentType = "c"
                commentParent = Utils.getString(data,"parent")
                cmtET.hint = Utils.getString(data,"nick") + "님의 대댓글에 답글"
            }

        }

        //이미지 관련 어댑터
        adverAdapter = FullScreenImageAdapter(this, adverImagePaths)
        pagerVP.adapter = adverAdapter
        pagerVP.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                adPosition = position
            }

            override fun onPageSelected(position: Int) {
            }

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
                       x = event.x
                    }

                    MotionEvent.ACTION_CANCEL->{

                    }

                    MotionEvent.ACTION_UP -> {
                        var difference = 0
                        if (x >= event.x){
                            difference = x.toInt() - event.x.toInt()
                        } else {
                            difference = event.x.toInt() - x.toInt()
                        }
                        if (difference < 10){
                            val pressDuration = System.currentTimeMillis() - pressStartTime!!
                            if (pressDuration < MAX_CLICK_DURATION && stayedWithinClickDistance!!) {
                            }

                            if (intent.hasExtra("id")) {
                                val id = intent.getStringExtra("id")
                                var intent = Intent(context, PictureDetailActivity::class.java)
                                intent.putExtra("id", id)
                                if (adverImagePaths != null){
                                    intent.putExtra("paths",adverImagePaths)
                                }
                                startActivityForResult(intent, PICTURE_DETAIL)
                            }
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
//            MoveFindPictureActivity()
        }

        plusBT.setOnClickListener {
//            relativ_RL.visibility = View.VISIBLE
            visibleMenu()
        }

//        goneRL.setOnClickListener {
////            relativ_RL.visibility = View.GONE
//            visibleMenu()
//        }

        reportTV.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("신고하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        if (PrefUtils.getIntPreference(context, "member_id") == -1){
                            Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                            return@OnClickListener
                        }

                        if (intent.getStringExtra("id") != null) {
                            val content_id = intent.getStringExtra("id")

                            var params = RequestParams()
                            params.put("content_id", content_id)
                            params.put("member_id", login_id)
                            params.put("type", 1)

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
                        if (PrefUtils.getIntPreference(context, "member_id") == -1){
                            Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                            return@OnClickListener
                        }


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

                        if (PrefUtils.getIntPreference(context, "member_id") == -1){
                            Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                            return@OnClickListener
                        }

                        if (intent.getStringExtra("id") != null) {
                            val content_id = intent.getStringExtra("id")

                            var params = RequestParams()
                            params.put("content_id", content_id)
                            params.put("mate_id", writer)
                            params.put("member_id", login_id)
                            params.put("category_id",-1)
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

        likeLL.setOnClickListener {
            if (PrefUtils.getIntPreference(context, "member_id") == -1){
                Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (intent.getStringExtra("id") != null) {
                val content_id = intent.getStringExtra("id")

                var params = RequestParams()
                params.put("content_id", content_id)
                params.put("member_id", login_id)

                PostAction.add_like(params, object : JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        val result = response!!.getString("result")
                        var LikeCount = response.getInt("LikeCount")

                        if (result == "yes") {

                            var like_id = response.getInt("like_id")

                            var remove_idx = -1

                            for(i in 0 until likeMembersLL.childCount) {
                                var view:View = likeMembersLL.getChildAt(i)

                                var profileIV:CircleImageView = view.findViewById(R.id.profileIV)
                                var tag_like_id:Int = profileIV.tag as Int

                                if(tag_like_id == like_id) {
                                    remove_idx = i;
                                }

                            }

                            if(remove_idx > -1) {
                                likeMembersLL.removeViewAt(remove_idx)
                            }

                            likeIV.setImageResource(R.mipmap.icon_like)

                        } else if("ok" == result) {

                            var json = response.getJSONObject("like")
                            var like = json.getJSONObject("Like")
                            var member = json.getJSONObject("Member")

                            var view:View = View.inflate(context, R.layout.item_profile, null)
                            var profileIV:CircleImageView = view.findViewById(R.id.profileIV)
                            profileIV.tag = Utils.getInt(like, "id")

                            var image = Config.url + Utils.getString(member, "profile_img")
                            ImageLoader.getInstance().displayImage(image, profileIV, Utils.UILoptionsUserProfile)

                            likeMembersLL.addView(view)

                            likeIV.setImageResource(R.mipmap.btn_cancel_like)

                        }
                        likecountTV.text = LikeCount.toString() + "명이 좋아합니다"
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                    }
                })
            }
        }

        likesMoreLL.setOnClickListener {

        }

        getPost()
        getLooker()
    }

    //이미지 자세히 보기 액티비티
    fun MoveFindPictureActivity(){
        startActivity(Intent(this,FindPictureActivity::class.java))
    }

    fun modify(){
        if (intent.getStringExtra("id") != null) {
            val id = intent.getStringExtra("id")
            val intent = Intent(this, AddPostActivity::class.java)
            intent.putExtra("category",2)
            intent.putExtra("id",id)
            startActivityForResult(intent,MODIFYS)
        }
    }

    fun delete(){

        val builder = AlertDialog.Builder(context)
        builder
                .setMessage("정말 삭제하시겠습니까 ?")

                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                    if (intent.hasExtra("id")) {
                        val id = intent.getStringExtra("id")

                        val params = RequestParams()
                        params.put("content_id",id)
                        params.put("deleted","Y")

                        PostAction.update_post(params,object : JsonHttpResponseHandler(){

                            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                                finish()
                            }

                            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
                            }

                        })

                        finish()

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

                            content_id = Utils.getInt(data,"id")
                            val created = Utils.getString(data,"created")
                            val title = Utils.getString(data,"title")
                            val text = Utils.getString(data,"text")
                            val member_id = Utils.getString(data,"member_id")
                            writer = member_id
                            val door_image = Utils.getString(data,"door_image")
                            val deleted = Utils.getString(data,"deleted")
                            val Looker = response.getJSONArray("Looker")
                            val Like = response.getJSONArray("Like")
                            val Comments = response.getJSONArray("Comments")
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
                                hashtagTV.text = hashtags
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
                                    } else {
                                        val path = Utils.getString(contentFile, "image_uri")
                                        videoviewTV.visibility = View.VISIBLE
                                        println("-----------타ㅓㄴ다")
                                        video = Uri.parse(Config.url + path)
//                                        videoVV.visibility = View.VISIBLE
//                                        videoVV.start()
//                                        videoVV.setVideoURI(video)
//                                        videoVV.setOnPreparedListener { mp -> mp.isLooping = true }
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
                            } else {
                                imageRL.visibility = View.GONE
                            }

                            dateTV.text = created
                            viewTV.text = Looker.length().toString()
                            heartcountTV.text = Like.length().toString()
                            titleTV.text = title
                            textTV.text = text
                            cmtcountTV.text = Comments.length().toString()
                            likecountTV.text = Like.length().toString() + "명이 좋아합니다"

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

                            val member = response.getJSONObject("Member")

                            val id = Utils.getString(member,"id")
                            val nick = Utils.getString(member, "nick")
                            val status_msg = Utils.getString(member, "status_msg")
                            val profile_img = Utils.getString(member, "profile_img")

                            nickNameTV.text = nick
                            statusmsgTV.text = status_msg

                            if (login_id == id.toInt()){
                                reportTV.visibility = View.GONE
                                addFriendTV.visibility = View.GONE
                                addfavoriteTV.visibility = View.GONE
                            } else {
                                modifyTV.visibility = View.GONE
                                deleteTV.visibility = View.GONE
                            }

                            ImageLoader.getInstance().displayImage(Config.url + profile_img, profileIV, Utils.UILoptionsUserProfile)

                            var likes = response.getJSONArray("Like")
                            if(likes.length() < 5) {
                                likesMoreLL.visibility = View.GONE
                            } else {
                                likesMoreLL.visibility = View.VISIBLE
                            }

                            for (i in 0 until likes.length()) {

                                var json:JSONObject = likes.get(i) as JSONObject
                                var like = json.getJSONObject("Like")
                                var likeMember = json.getJSONObject("Member")

                                var view = View.inflate(context, R.layout.item_profile,null)
                                var profileIV:CircleImageView = view.findViewById(R.id.profileIV)
                                profileIV.tag = Utils.getInt(like, "id")

                                var image = Config.url + Utils.getString(likeMember, "profile_img")

                                ImageLoader.getInstance().displayImage(image, profileIV, Utils.UILoptionsProfile)

                                likeMembersLL.addView(view)
                            }

                            getComments()

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
                    viewTV.text = Looker.length().toString()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

            }
        })
    }

    //나중에 합칠 계획 ; 서버 조회 최소화
    fun getComments() {
        val params = RequestParams()
        params.put("cont_id", content_id)
        params.put("writer", writer)

        CommentAction.get_content_comment_list(params,object :JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    val comments = response.getJSONArray("comments")
                    for (i in 0 until comments.length()){
                        commentList.add(comments[i] as JSONObject)
                        //commentList.get(i).put("changedBlockYN", "N")
                    }
                    commentAdapter.notifyDataSetChanged()
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


    fun visibleMenu(){
        if (writer.toInt() == PrefUtils.getIntPreference(context,"member_id")) {

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_post_menu, null) //사용자 정의 다이얼로그 xml 붙이기
            builder.setView(dialogView) // custom xml과 alertDialogBuilder를 붙임
            val alert = builder.show() //builder를 끄기 위해서는 alertDialog에 이식해줘야 함

            alert.show()

            dialogView.modifyTV.visibility = View.VISIBLE
            dialogView.deleteTV.visibility = View.VISIBLE

            dialogView.modifyTV.setOnClickListener {
                modify()
                alert.dismiss()
            }

            dialogView.deleteTV.setOnClickListener {
                delete()
                alert.dismiss()
            }

        } else {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_post_menu, null) //사용자 정의 다이얼로그 xml 붙이기
            builder.setView(dialogView) // custom xml과 alertDialogBuilder를 붙임
            val alert = builder.show() //builder를 끄기 위해서는 alertDialog에 이식해줘야 함

            alert.show()

            dialogView.addfavoriteTV.visibility = View.VISIBLE
            dialogView.reportTV.visibility = View.VISIBLE
            dialogView.addFriendTV.visibility =View.VISIBLE

            dialogView.addfavoriteTV.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("보관하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            if (PrefUtils.getIntPreference(context, "member_id") == -1){
                                Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                                return@OnClickListener
                            }

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
                            alert.dismiss()
                        })

                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }

            dialogView.reportTV.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("신고하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            if (intent.getStringExtra("id") != null) {
                                if (PrefUtils.getIntPreference(context, "member_id") == -1){
                                    Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                                    return@OnClickListener
                                }

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
                            alert.dismiss()
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }

            dialogView.addFriendTV.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("친구신청하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            if (PrefUtils.getIntPreference(context, "member_id") == -1){
                                Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                                return@OnClickListener
                            }

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
                            alert.dismiss()
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }
        }
    }

    override fun finish() {
        super.finish()
        Utils.hideKeyboard(context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                MODIFYS -> {
                    if (data!!.getStringExtra("reset") != null) {

//                    val selCateg = data!!.getIntExtra("CategoryID", 1)

                        videoVV.visibility = View.GONE
                        getPost()
//                    if (data!!.getStringExtra("reset") != null) {

//                    }
                    }
                }
            }
        }
    }

}
