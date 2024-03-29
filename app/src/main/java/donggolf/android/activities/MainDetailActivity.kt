package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.MotionEvent
import donggolf.android.R
import kotlinx.android.synthetic.main.activity_main_detail.*
import android.view.View
import android.view.View.OnTouchListener
import android.webkit.JavascriptInterface
import android.widget.MediaController
import android.widget.Toast
import com.kakao.kakaostory.StringSet.text
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.actions.*
import donggolf.android.actions.CommentAction.write_comments
import donggolf.android.adapters.FullScreenImageAdapter
import donggolf.android.adapters.MainDeatilAdapter
import donggolf.android.base.*
import kotlinx.android.synthetic.main.dlg_comment_menu.view.*
import kotlinx.android.synthetic.main.dlg_post_menu.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
import kotlin.collections.ArrayList

class MainDetailActivity : RootActivity() {

    private lateinit var context: Context
    private  var commentList = ArrayList<JSONObject>()

    private  lateinit var  commentAdapter : MainDeatilAdapter

    private lateinit var adverAdapter: FullScreenImageAdapter
    var adverImagePaths:ArrayList<String> = ArrayList<String>()

    var adPosition = 0

    var PICTURE_DETAIL = 1

    var pressStartTime: Long?  = 0
    var stayedWithinClickDistance: Boolean? = false

    val MAX_CLICK_DURATION = 1000

    //lateinit var activity: MainDetailActivity


    var login_id = 0
    var writer = "0"
    var content_id = 0
    var commentType = ""
    var commentParent = ""
    var blockYN = ""
    var cht_yn = ""
    var cmt_yn = ""

    var MODIFYS = 70

    var x = 0.0f

    val GALLERY = 500

    var modify_division = ""

    var comment_path: Bitmap? = null
    var op_comments_id = -1
    var p_comments_id = -1
    lateinit var video:Uri

    var freind = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_detail)



        detail_add_commentTV.paintFlags = detail_add_commentTV.paintFlags or Paint.FAKE_BOLD_TEXT_FLAG




        context = this
        intent = intent
        login_id = PrefUtils.getIntPreference(context, "member_id")


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

        leftIV.setOnClickListener {
            if (adPosition-1 < 0){
                Toast.makeText(context,"마지막 사진입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            adPosition -= 1
            pagerVP.setCurrentItem(adPosition)
        }

        rightIV.setOnClickListener {

            if (adPosition+1 == adverImagePaths.size){
                Toast.makeText(context,"마지막 사진입니다.", Toast.LENGTH_SHORT).show()
                adPosition = adverImagePaths.size-1
                return@setOnClickListener
            } else if (adPosition < adverImagePaths.size-1){
                adPosition += 1
            }
            pagerVP.setCurrentItem(adPosition)
        }

        main_detail_gofindpicture.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            startActivityForResult(galleryIntent, GALLERY)
        }

        profileIV.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("member_id", writer)
            startActivity(intent)
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
                            // println(response)

                            val result = response!!.getString("result")
                            if (result == "ok"){
                                //할일 : 리스트뷰에서 아이템을 지운다

                                commentAdapter.removeItem(position)
                                commentAdapter.notifyDataSetChanged()

                                alert.dismiss()
                            }
                        }

                        override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                            // println(errorResponse)
                        }

                        override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                            // println(responseString)
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

                    var cmt_wrt_id = commentList[position].getInt("cmt_wrt_id")

                    val params = RequestParams()
                    params.put("content_id", content_id)
                    params.put("writer", writer)
                    params.put("commenter", cmt_wrt_id)
                    params.put("status", blockYN)

                    CommentAction.content_commenter_ben(params, object : JsonHttpResponseHandler(){
                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                            try {
                                // println(response)
                                //차단 성공하면 표시하고 토스트
                                val result = response!!.getString("result")
                                if (result == "ok") {

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
                            // println(errorResponse)
                        }

                        override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                            // println(responseString)
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

            if (cmt_yn == "N"){
                Toast.makeText(context,"댓글이 차단된 게시물 입니다.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            writeComments()

        }
        commentListLV.setOnItemClickListener { adapterView, view, i, l ->
            if (PrefUtils.getIntPreference(context, "member_id") == -1){
                Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }
            var data = commentList.get(i)
            //Log.d("데이데이",data.toString())
            val contentcomment = data.getJSONObject("ContentComment")

            val comments_id = Utils.getInt(contentcomment, "id")
            p_comments_id = Utils.getInt(contentcomment,"p_comments_id")
            op_comments_id = Utils.getInt(contentcomment,"op_comments_id")
            var user_nick =  Utils.getString(contentcomment,"nick")
            var chk = Utils.getBoolen(data, "isSelectedOp")
           if (op_comments_id != -1){
               op_comments_id = comments_id
               p_comments_id = -1
               cmtET.hint = user_nick+ "님의 댓글에 대대댓글"
           }else if (p_comments_id!=-1){
                op_comments_id = comments_id
                p_comments_id = -1
                cmtET.hint = user_nick+ "님의 댓글에 대댓글"
            } else if (comments_id != -1) {
                p_comments_id = comments_id
                cmtET.hint = user_nick + "님의 댓글에 답글"
            }
            if (chk){
                op_comments_id = -1
                p_comments_id = -1
                cmtET.hint = "댓글을 남겨주세요"
                commentList[i].put("isSelectedOp",false)
                commentAdapter.notifyDataSetChanged()
            }else{
                cmtET.requestFocus()
                Utils.showKeyboard(context)
                commentList[i].put("isSelectedOp",true)
                commentAdapter.notifyDataSetChanged()
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
                                intent.putExtra("adPosition",adPosition)
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
            var intent = Intent()
            intent.putExtra("reset", "reset")
            setResult(RESULT_OK, intent);
            finish()
        }
        plusBT.setOnClickListener {
            visibleMenu()
        }

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
                                        Toast.makeText(context, "이미 친구신청을 친구신청을 받았습니다.", Toast.LENGTH_SHORT).show()
                                    }else if (result == "already"){
                                        Toast.makeText(context, "차단상태입니다.", Toast.LENGTH_SHORT).show()
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

                            var member_id = Utils.getString(member,"id")

                            var image = Config.url + Utils.getString(member, "profile_img")
                            ImageLoader.getInstance().displayImage(image, profileIV, Utils.UILoptionsUserProfile)

                            view.setOnClickListener {
                                if (PrefUtils.getIntPreference(context, "member_id") == member_id.toInt()){
                                    Toast.makeText(context,"본인 프로필에는 이동하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                                    return@setOnClickListener
                                }
                                val intent = Intent(context, ProfileActivity::class.java)
                                intent.putExtra("member_id", member_id)
                                startActivity(intent)
                            }

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

        delIV.setOnClickListener {
            addedImgIV.setImageResource(0)
            commentLL.visibility = View.GONE
            main_detail_gofindpicture.visibility = View.VISIBLE
            comment_path = null
        }

        contentWV.addJavascriptInterface(ImageClick(context), "ImageClick")

        if (intent.getStringExtra("id") != null){
            val id = intent.getStringExtra("id")
            getPost(id)
            val url = Config.url + "/post/post"+"?content_id="+id

            contentWV.settings.javaScriptEnabled = true
            contentWV.loadUrl(url)
            getLooker(id)
        }

    }

    class ImageClick(context: Context) {
        var context = context
        @JavascriptInterface
        fun imageClick(src : String){
            val intent = Intent(context, WebPictureDetailActivity::class.java)
            intent.putExtra("src", src)
            context.startActivity(intent)
        }
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

                        // println("------content_id =-====== $id")

                        PostAction.update_post(params,object : JsonHttpResponseHandler(){

                            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                                var intent = Intent()
                                intent.putExtra("reset", "reset")
                                setResult(RESULT_OK, intent);
                                finish()

                            }

                            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
                            }

                        })

                    }

                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                })
        val alert = builder.create()
        alert.show()

    }
    //댓글
    fun writeComments() {
        var comment = Utils.getString(cmtET)
        val params = RequestParams()
        params.put("member_id",login_id)
        params.put("cont_id", content_id)
        params.put("nick", PrefUtils.getStringPreference(context,"nickname"))
        params.put("comment", comment)
        params.put("p_comments_id", p_comments_id)
        params.put("op_comments_id", op_comments_id)
        if (comment_path != null){
            params.put("file", ByteArrayInputStream(Utils.getByteArray(comment_path)))
        }
        write_comments(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {

                try {
                    val result = response!!.getString("result")
                    if ("ok" == result) {
                        getPost(content_id.toString())
                        Utils.hideKeyboard(context)
                        cmtET.setText("")
                        cmtET.hint = ""
                        p_comments_id = -1
                        op_comments_id = -1
                        addedImgIV.setImageResource(0)
                        commentLL.visibility = View.GONE
                        main_detail_gofindpicture.visibility = View.VISIBLE
                        comment_path = null
                    }else if ("block"==result){
                        Toast.makeText(context,"댓글이 차단되었습니다.",Toast.LENGTH_SHORT).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONArray?) {
                super.onSuccess(statusCode, headers, response)
            }

            private fun error() {
                Utils.alert(context, "조회중 장애가 발생하였습니다.")
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
        })
    }



    private fun pxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }

    fun getPost(id:String){
            login_id = PrefUtils.getIntPreference(context, "member_id")

            var params = RequestParams()
            params.put("id",id)
            params.put("member_id",login_id)


            PostAction.get_post(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    try {
                        val result = response!!.getString("result")
                        if (result == "ok") {
                            likeMembersLL.removeAllViews()

                            val data = response.getJSONObject("Content")

                            content_id = Utils.getInt(data,"id")
                            val created = Utils.getString(data,"created")
                            val title = Utils.getString(data,"title")
                            val text = Utils.getString(data,"text")
                            val member_id = Utils.getString(data,"member_id")
                            writer = member_id
                            val Looker = response.getJSONArray("Looker")
                            val Like = response.getJSONArray("Like")
                            val Comments = response.getJSONArray("Comments")
                            cht_yn = Utils.getString(data,"cht_yn")
                            cmt_yn = Utils.getString(data,"cmt_yn")


                            freind = Utils.getString(data,"freind")
                            val mate_cnt = Utils.getString(data,"mate_cnt")
                            //Log.d("친구",freind)
                            if (freind == "0"){
                                if (mate_cnt.toInt() > 0){
                                    freindIV.setBackgroundResource(R.drawable.icon_second)
                                }
                            }

                            if (mate_cnt == "0"){
                                freindIV.visibility = View.GONE
                            }


                            val likeDiv = response.getString("LikeDiv")

                            if (likeDiv == "N") {
                                likeIV.setImageDrawable(resources.getDrawable(R.drawable.icon_like))
                            } else {
                                likeIV.setImageDrawable(resources.getDrawable(R.drawable.btn_cancel_like))
                            }

                            val tags = response.getJSONArray("tags")
                            val imageDatas = response.getJSONArray("ContentImgs")

                            //Log.d("이미지",imageDatas.toString())
                            // println("------detail imagedatas.size ${imageDatas.length()}")

                            if (tags != null && tags.length() > 0 ){
                                var hashtags: String = ""

                                for (i in 0 until tags.length()){
                                    var json = tags.get(i) as JSONObject
                                    var MemberTags = json.getJSONObject("ContentsTags")
                                    val division = Utils.getString(MemberTags,"division")

                                    if (division == "1"){
                                        val tag = Utils.getString(MemberTags,"tag")
                                        hashtags += "#"+tag + "  "
                                    }
                                }
                                hashtagTV.text = hashtags
                            }

                            if (imageDatas != null && imageDatas.length() > 0){
                                // println("-------------visible")
                                imageRL.visibility = View.VISIBLE
                                pagerVP.visibility = View.VISIBLE
                                var imagePaths: ArrayList<String> = ArrayList<String>()

                                for (i in 0 until imageDatas.length()){
                                    var json = imageDatas.get(i) as JSONObject

                                    var contentFile = json.getJSONObject("contentFile")
                                    var type = Utils.getInt(contentFile,"type")
                                    if (type == 1) {
                                        val path = Utils.getString(contentFile, "image_uri")
                                        imagePaths.add(path)
                                    } else {
                                        val path = Utils.getString(contentFile, "video_uri")
                                        videoviewTV.text = "동영상 숨기기"
                                        videoviewTV.visibility = View.VISIBLE
                                        videoVV.visibility = View.VISIBLE
                                        leftIV.visibility = View.GONE
                                        rightIV.visibility = View.GONE
                                        pagerVP.visibility = View.GONE
                                        video = Uri.parse(Config.url + path)
                                        videoVV.start()
                                        videoVV.setVideoURI(video)
                                        videoVV.setOnPreparedListener { mp -> mp.isLooping = true }
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
                                    //Log.d("이미지2",image)
                                    adverImagePaths.add(image)
                                }
                                adverAdapter.notifyDataSetChanged()

                                if (adverImagePaths.size > 1){
                                    if (videoVV.visibility != View.VISIBLE){
                                        leftIV.visibility = View.VISIBLE
                                        rightIV.visibility = View.VISIBLE
                                    } 
                                }

                            }
                            else {
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
                                main_detail_gofindpicture.visibility = View.VISIBLE
                                detail_add_commentTV.visibility = View.VISIBLE
                                cmtLL.visibility = View.VISIBLE
                                cmtTV.visibility = View.GONE
                                cmtET.visibility = View.VISIBLE
                            } else {
                                main_detail_gofindpicture.visibility = View.GONE
                                detail_add_commentTV.visibility = View.GONE
                                cmtLL.visibility = View.GONE
                                cmtTV.visibility = View.VISIBLE
                                cmtET.visibility = View.GONE
                            }

                            val member = response.getJSONObject("Member")

                            val id = Utils.getString(member,"id")
                            val nick = Utils.getString(member, "nick")
                            val status_msg = Utils.getString(member, "status_msg")
                            val profile_img = Utils.getString(member, "profile_img")
                            val sex = Utils.getString(member,"sex")
                            if (sex == "0"){
                                nickNameTV.setTextColor(Color.parseColor("#000000"))
                            }



                            if (member_id.toInt() == PrefUtils.getIntPreference(context, "member_id")){
                                freindIV.visibility = View.GONE
                            }

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

                                view.setOnClickListener {
                                    if (PrefUtils.getIntPreference(context, "member_id") == member_id.toInt()){
                                        Toast.makeText(context,"본인 프로필에는 이동하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                                        return@setOnClickListener
                                    }

                                    val intent = Intent(context, ProfileActivity::class.java)
                                    intent.putExtra("member_id", id)
                                    startActivity(intent)
                                }

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

    fun Post(id:String){
        login_id = PrefUtils.getIntPreference(context, "member_id")

        var params = RequestParams()
        params.put("content_id",id)

        PostAction.post(params, object : JsonHttpResponseHandler() {
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

    fun getLooker(id:String){
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
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        CommentAction.get_content_comment_list(params,object :JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                // println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    val comments = response.getJSONArray("comments")
                    commentList.clear()
                    for (i in 0 until comments.length()){
                        commentList.add(comments[i] as JSONObject)
                        commentList[i].put("isSelectedOp",false)
                    }
                    commentAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
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
//            dialogView.addFriendTV.visibility =View.VISIBLE
            if (freind.toInt() > 0){
                dialogView.addFriendTV.visibility =View.GONE
            } else {
                dialogView.addFriendTV.visibility =View.VISIBLE
            }

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
                                params.put("type", "1")

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
                val intent = Intent(context, ProfileActivity::class.java)
                intent.putExtra("member_id", writer)
                intent.putExtra("type", 1)
                context.startActivity(intent)
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


                        modify_division = data!!.getStringExtra("id")
                        val id = data!!.getStringExtra("id")
                        getPost(id)

                        contentWV.loadUrl(Config.url + "/post/post"+"?content_id="+id)

                    }
                }

                GALLERY -> {
                    if (data != null)
                    {

                        val contentURI = data.data

                        try
                        {
                            commentLL.visibility = View.VISIBLE
                            main_detail_gofindpicture.visibility = View.GONE

                            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)

                            val cursor = context.contentResolver.query(contentURI, filePathColumn, null, null, null)
                            if (cursor!!.moveToFirst()) {
                                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                                val picturePath = cursor.getString(columnIndex)

                                cursor.close()

                                comment_path = Utils.getImage(context.contentResolver,picturePath.toString())
                                addedImgIV.setImageBitmap(comment_path)

                            }

                        }
                        catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (modify_division == "") {
            finish()
        } else {
            var intent = Intent()
            intent.putExtra("reset", "reset")
            setResult(RESULT_OK, intent);
            finish()
        }
    }

}
