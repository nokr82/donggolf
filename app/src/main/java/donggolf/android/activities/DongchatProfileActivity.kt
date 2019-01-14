package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.actions.MemberAction
import donggolf.android.adapters.FullScreenImageAdapter
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_chat_detail.*
import kotlinx.android.synthetic.main.activity_dongchat_profile.*
import kotlinx.android.synthetic.main.dlg_chat_blockcode.view.*
import kotlinx.android.synthetic.main.dlg_comment_menu.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.ArrayList


class DongchatProfileActivity : RootActivity() {

    lateinit var context: Context

    var room_id = ""

    var Image_path = ArrayList<String>()

    private lateinit var backgroundAdapter: FullScreenImageAdapter

    var founder_id = ""
    var notice = ""
    var SET_NOTICE = 1000

    var PROFILE = 100
    var BACKGROUND = 101

    var profile: Uri? = null
    var background:Uri? = null

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dongchat_profile)
        context = this
        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)
        if (null != context) {
            doSomethingWithContext(context)
        }
        joinDongChatRL.setOnClickListener {
            val intent = Intent(context, DongChatDetailActivity::class.java)
            intent.putExtra("room_id",room_id)
            startActivity(intent)
        }

        room_id = intent.getStringExtra("room_id")

        detail_chatting()

        backgroundAdapter = FullScreenImageAdapter(this@DongchatProfileActivity, Image_path)
        backgroundVP.adapter = backgroundAdapter

//        setnoticeTV.setOnClickListener {
//            val builder = AlertDialog.Builder(this)
//            val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
//            builder.setView(dialogView)
//            val alert = builder.show()
//
//            dialogView.dlgtextTV.setText("변경할 공지사항을 입력하세요")
//            dialogView.dlgTitle.setText("공지사항 입력")
//            dialogView.blockcodeTV.setText("")
//
//            dialogView.btn_title_clear.setOnClickListener {
//                dialogView.blockcodeTV.setText(notice)
//            }
//
//            dialogView.cancleTV.setOnClickListener {
//                alert.dismiss()
//            }
//
//            dialogView.okTV.setOnClickListener {
//                val code = dialogView.categoryTitleET.text.toString()
//                if (code == null || code == ""){
//                    Toast.makeText(context, "빈칸은 입력하실 수 없습니다", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//                notice = code
//                alert.dismiss()
//                noticeTV.setText(code)
//                set_notice(code)
//            }
//
//        }

        setimageIV.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_comment_menu, null) //사용자 정의 다이얼로그 xml 붙이기
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlg_comment_delTV.visibility = View.GONE
            dialogView.dlg_comment_copyTV.setText("프로필사진")
            dialogView.dlg_comment_blockTV.setText("배경사진")

            dialogView.dlg_comment_copyTV.setOnClickListener {
                val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                startActivityForResult(galleryIntent, PROFILE)
                alert.dismiss()
            }

            dialogView.dlg_comment_blockTV.setOnClickListener {
                val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                startActivityForResult(galleryIntent, BACKGROUND)
                alert.dismiss()
            }

        }

        setnoticeTV.setOnClickListener {
            val intent = Intent(context, NoticeManageActivity::class.java)
            intent.putExtra("room_id",room_id)
            startActivityForResult(intent,SET_NOTICE)
        }

        setroomtitleTV.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlgtextTV.setText("변경할 제목을 입력하세요")
            dialogView.dlgTitle.setText("제목 입력")
            val title = roomtitleTV.text.toString()
            dialogView.settitleTV.setText("현재 채팅방 제목은 ")
            dialogView.blockcodeTV.setText(title)

            dialogView.btn_title_clear.setOnClickListener {
                dialogView.blockcodeTV.setText("")
            }

            dialogView.cancleTV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.okTV.setOnClickListener {
                val code = dialogView.categoryTitleET.text.toString()
                if (code == null || code == ""){
                    Toast.makeText(context, "빈칸은 입력하실 수 없습니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                set_title(code)
                alert.dismiss()
            }
        }


    }
    fun detail_chatting(){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        ChattingAction.detail_chatting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                val result = response!!.getString("result")
                if (result == "ok") {
                    if (Image_path != null){
                        Image_path.clear()
                    }

                    val members = response!!.getJSONArray("chatmember")
                    if (members != null && members.length() > 0){
                        for (i in 0 until members.length()){
                            val item = members.get(i) as JSONObject
                            val chatmember = item.getJSONObject("Chatmember")
                            val chatroom = item.getJSONObject("Chatroom")
                            val memberinfo = item.getJSONObject("Member")
                            val title = Utils.getString(chatroom,"title")
                            val visible = Utils.getString(chatroom,"visible")
                            val introduce = Utils.getString(chatroom,"introduce")
                            val created = Utils.getString(chatroom,"created")
                            val intro = Utils.getString(chatroom,"intro")
                            val background = Utils.getString(chatroom,"background")
                            val peoplecount = Utils.getString(chatroom,"peoplecount")
                            val max_count = Utils.getString(chatroom,"max_count")
                            val getnotice = Utils.getString(chatroom,"notice")
                            if (getnotice != null && getnotice.length > 0){
                                noticeTV.setText(notice)
                                notice = getnotice
                            }
                            membercountTV.setText("("+peoplecount + "/" + max_count+")")
                            if (background != null && background.length > 0){
                                Image_path.add(Config.url + Utils.getString(chatroom,"background"))
                            }

                            val introimage = Config.url + intro

                            ImageLoader.getInstance().displayImage(introimage, profileIV, Utils.UILoptionsProfile)

                            founder_id = Utils.getString(chatroom,"id")

                            val createdsplit = created.split(" ")
                            createdTV.setText(createdsplit.get(0))
                            roomcreatedTV.setText(createdsplit.get(0))

                            roomtitleTV.setText(title)
                            introduceTV.setText(introduce)

                            if (visible == "1"){

                            } else if (visible == "2"){
//                                privateIV.visibility = View.VISIBLE
                            }

                            if (PrefUtils.getIntPreference(context,"member_id") != Utils.getInt(chatroom,"member_id")){
                                setnoticeTV.visibility = View.GONE
                                setroomtitleTV.visibility = View.GONE
                                setimageIV.visibility = View.GONE
                            }

                            if (Utils.getInt(chatroom,"member_id") == Utils.getInt(memberinfo,"id")){
                                var founderIV: CircleImageView = findViewById(R.id.founderIV)
                                var image = Config.url + Utils.getString(memberinfo, "profile_img")
                                ImageLoader.getInstance().displayImage(image, founderIV, Utils.UILoptionsUserProfile)
                                val nick = Utils.getString(memberinfo,"nick")
                                nickTV.setText(nick)
                            }

                        }

                    }
                    backgroundAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                // System.out.println(responseString);

                throwable.printStackTrace()
//                error()
            }

            override fun onStart() {
                // show dialog
                if (progressDialog != null) {
                    progressDialog!!.show()
                }
            }

            override fun onFinish() {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
            }
        })
    }

    fun add_chat_member(){
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", PrefUtils.getIntPreference(context, room_id))

        ChattingAction.add_chat_member(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        finish()
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

    fun set_notice(notice: String){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("notice", notice)

        ChattingAction.set_notice(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {

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

    fun set_title(title: String){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("title", title)

        ChattingAction.set_title(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        roomtitleTV.setText(title)
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

    fun set_image(type: String,image : Bitmap){
        val params = RequestParams()
        if (type == "intro"){
//            var profileBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, image)
            params.put("intro", ByteArrayInputStream(Utils.getByteArray(image)))
        } else {
//            var backgroundBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, image)
            params.put("background", ByteArrayInputStream(Utils.getByteArray(image)))
        }

        params.put("room_id", room_id)

        ChattingAction.set_dong_image(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
//                        detail_chatting()
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
                SET_NOTICE -> {
                    if (data!!.getStringExtra("reset") != null){
                        detail_chatting()
                    }
                }

                PROFILE -> {
                    if (data != null)
                    {
                        val contentURI = data.data

                        try
                        {
//                            var thumbnail = MediaStore.Images.Media.getBitmap(context.contentResolver, contentURI)
                            var thumbnail = Utils.getImage(context.contentResolver,contentURI.toString())
//                            val resized = Utils.resizeBitmap(thumbnail, 100)
//                            val bitmap = resized
//                            val img = ByteArrayInputStream(Utils.getByteArray(thumbnail))

                            ImageLoader.getInstance().displayImage(contentURI.toString(), profileIV, Utils.UILoptionsProfile)
                            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)

                            val cursor = context.contentResolver.query(contentURI, filePathColumn, null, null, null)
                            if (cursor!!.moveToFirst()) {
                                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                                val picturePath = cursor.getString(columnIndex)

                                cursor.close()

                                val prof = Utils.getImage(context.contentResolver, picturePath.toString())


//                            val resized = Utils.resizeBitmap(thumbnail, 100)
//                            profile = thumbnail
//                            profileIV.setImageURI(contentURI)
//                                profileIV.setImageBitmap(prof)
                                set_image("intro",prof)
                            }





                        }
                        catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                }

                BACKGROUND -> {
                    if (data != null)
                    {
                        val contentURI = data.data

                        try {
//                            var thumbnail = MediaStore.Images.Media.getBitmap(context.contentResolver, contentURI)
                            var thumbnail = Utils.getImage(context.contentResolver, contentURI.toString())

                            ImageLoader.getInstance().displayImage(contentURI.toString(), profileIV, Utils.UILoptionsProfile)
                            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)

                            val cursor = context.contentResolver.query(contentURI, filePathColumn, null, null, null)
                            if (cursor!!.moveToFirst()) {
                                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                                val picturePath = cursor.getString(columnIndex)

                                cursor.close()

                                val prof = Utils.getImage(context.contentResolver, picturePath.toString())


//                            val resized = Utils.resizeBitmap(thumbnail, 100)
//                            profile = thumbnail
//                            profileIV.setImageURI(contentURI)
//                                profileIV.setImageBitmap(prof)
                                set_image("background", prof)


//                            val resized = Utils.resizeBitmap(thumbnail, 100)

//                            val bitmap = resized
//                            val img = ByteArrayInputStream(Utils.getByteArray(thumbnail))
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


    fun doSomethingWithContext(context: Context) {
        this.context = context
        progressDialog = ProgressDialog(context)
    }




}
