package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
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
    var backgroundPath = ""
    var profilePath = ""

    private lateinit var backgroundAdapter: FullScreenImageAdapter

    var founder_id = ""
    var notice = ""
    var SET_NOTICE = 1000

    var PROFILE = 100
    var BACKGROUND = 101

    var profile: Uri? = null
    var background:Uri? = null

    private var progressDialog: ProgressDialog? = null

    var people_count = 0
    var max_count = 0

    var chatMemberids:ArrayList<String> = ArrayList<String>()

    val DONG_CHAT = 300
    var DONG_CHAT_RESULT = false

    var block_yn = "N"

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

        backLL.setOnClickListener {
            finish()
            Utils.hideKeyboard(this)
        }

        joinDongChatRL.setOnClickListener {
            var chkData = false

            if (PrefUtils.getIntPreference(context, "member_id") == -1){
                Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (block_yn == "Y"){
                Toast.makeText(context,"차단된 채팅방 입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            for (i in 0 until chatMemberids.size){
                if (chatMemberids.get(i).toInt() == PrefUtils.getIntPreference(context, "member_id")){
                    chkData = true
                }
            }

            if (chkData == true){
                val intent = Intent(context, DongChatDetailActivity::class.java)
                intent.putExtra("room_id", room_id)
                startActivityForResult(intent, DONG_CHAT)
                finish()
            } else {
                if (people_count >= max_count) {
                    Toast.makeText(context,"정원초과 입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else if (people_count < max_count){
                    add_chat_member()
                }
            }

        }


        backgroundVP.setOnClickListener {
            println("-------backgroundPath : $backgroundPath")
            if (backgroundPath == ""){
                return@setOnClickListener
            }
            val imglist:ArrayList<String> = ArrayList<String>()
            imglist.add(backgroundPath)
            var intent = Intent(context, PictureDetailActivity::class.java)
            intent.putExtra("id", "")
            intent.putExtra("adPosition",0)
            intent.putExtra("paths",imglist)
            intent.putExtra("type","chat")
            startActivity(intent)

        }

        profileIV.setOnClickListener {
            println("-------profilePath : $profilePath")
            if (profilePath == ""){
                return@setOnClickListener
            }

            val imglist:ArrayList<String> = ArrayList<String>()
            imglist.add(profilePath)
            var intent = Intent(context, PictureDetailActivity::class.java)
            intent.putExtra("id", "")
            intent.putExtra("adPosition",0)
            intent.putExtra("paths",imglist)
            intent.putExtra("type","chat")
            startActivity(intent)
        }


        room_id = intent.getStringExtra("room_id")

        detail_chatting("detail")

//        backgroundAdapter = FullScreenImageAdapter(this@DongchatProfileActivity, Image_path)
//        backgroundVP.adapter = backgroundAdapter

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

        cntIV.setOnClickListener {
            if (founder_id.toInt() != PrefUtils.getIntPreference(context,"member_id")){
                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
            builder.setView(dialogView)
            val alert = builder.show()
            dialogView.dlgtextTV.setTextColor(Color.parseColor("#000000"))
            dialogView.dlgtextTV.setText("가용인원수는 최대 80명 입니다.")
            dialogView.categoryTitleET.setHint("가용인원수를 입력해주세요.")
            dialogView.dlgTitle.setText("채팅방 인원")
            dialogView.codevisibleLL.visibility = View.GONE
            dialogView.settitleTV.visibility = View.GONE
            dialogView.blockcodeTV.visibility = View.GONE

            dialogView.categoryTitleET.addTextChangedListener(object : TextWatcher {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    var cnt = 0
                    var num = s.toString()
                    if (num != "") {
                        cnt = num.toInt()
                        if (cnt > 80) {
                            Toast.makeText(context, "가용인원수는 최대80명입니다.", Toast.LENGTH_SHORT).show()
                            dialogView.categoryTitleET.setText("80")
                            return
                        }
                    }
                }

                override fun afterTextChanged(count: Editable) {
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                }
            })

            dialogView.btn_title_clear.setOnClickListener {
                alert.dismiss()
            }

            dialogView.cancleTV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.okTV.setOnClickListener {
                alert.dismiss()
            }
        }


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

        introduceLL.setOnClickListener {
            if (founder_id.toInt() != PrefUtils.getIntPreference(context,"member_id")){
                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlgtextTV.setText("변경할 내용을 입력하세요")
            dialogView.dlgTitle.setText("채팅방 소개 입력")
            dialogView.categoryTitleET.setHint("변경할 소개내용을 입력해 주세요.")
            val title = introduceTV.text.toString()
            dialogView.settitleTV.setText("현재 채팅방 소개는 ")
            dialogView.blockcodeTV.setText(title)



            dialogView.btn_title_clear.setOnClickListener {
                alert.dismiss()
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

                set_introduce(code)
                alert.dismiss()
            }

        }

        setroomtitleTV.setOnClickListener {
            if (founder_id.toInt() != PrefUtils.getIntPreference(context,"member_id")){
                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlgtextTV.visibility = View.GONE
            dialogView.categoryTitleET.setHint("변경할 제목을 입력해 주세요.")
            dialogView.dlgTitle.setText("제목 입력")
            val title = roomtitleTV.text.toString()
            dialogView.settitleTV.setText("현재 채팅방 제목은 ")
            dialogView.blockcodeTV.setText(title)
            dialogView.categoryTitleET.addTextChangedListener(object : TextWatcher {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.length>22){
                        Toast.makeText(context,"최대제목길이는 22자입니다.",Toast.LENGTH_SHORT).show()
                        return
                    }
                }

                override fun afterTextChanged(count: Editable) {
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                }
            })

            dialogView.btn_title_clear.setOnClickListener {
                alert.dismiss()
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

        founderIV.setOnClickListener {
            var intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("member_id", founder_id)
            startActivity(intent)
        }

    }
    fun detail_chatting(type:String){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("type",type)

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
                        if (chatMemberids != null){
                            chatMemberids.clear()
                        }
                        for (i in 0 until members.length()){
                            val item = members.get(i) as JSONObject
                            val chatmember = item.getJSONObject("Chatmember")
                            val member_id = Utils.getString(chatmember,"member_id")
                            val chatroom = item.getJSONObject("Chatroom")
                            val memberinfo = item.getJSONObject("Member")
                            val title = Utils.getString(chatroom,"title")
                            val visible = Utils.getString(chatroom,"visible")
                            val introduce = Utils.getString(chatroom,"introduce")
                            val created = Utils.getString(chatroom,"created")
                            val intro = Utils.getString(chatroom,"intro")
                            val background = Utils.getString(chatroom,"background")
                            val block_code = Utils.getString(chatroom,"block_code")
                            if (block_code != null && block_code != ""){
                                lockIV.visibility = View.VISIBLE
                            }

                            if (PrefUtils.getIntPreference(context,"member_id") == member_id.toInt()){
                                block_yn = Utils.getString(chatmember,"block_yn")
                            }


//                            var split = created.split(" ")
//                            dongcreatedTV.setText(split.get(0))

                            people_count = Utils.getString(chatroom,"peoplecount").toInt()
                            max_count = Utils.getString(chatroom,"max_count").toInt()
                            notice = Utils.getString(chatroom,"notice")
                            if (notice != null && notice.length > 0){
                                noticeTV.setText(notice)
                                notice = notice
                            }

                            chatMemberids.add(member_id)
                            membercountTV.setText("("+people_count.toString() + "/" + max_count.toString()+")")
                            if (background != null && background.length > 0){
//                                Image_path.add(Config.url + Utils.getString(chatroom,"background"))
                                backgroundPath = Utils.getString(chatroom,"background")
                            }

                            val backgroundimage = Config.url + Utils.getString(chatroom, "background")
                            ImageLoader.getInstance().displayImage(backgroundimage, backgroundVP, Utils.UILoptionsProfile)

                            profilePath = Utils.getString(chatroom, "intro")
                            val introimage = Config.url + intro

                            ImageLoader.getInstance().displayImage(introimage, profileIV, Utils.UILoptionsProfile)

                            founder_id = Utils.getString(chatroom,"member_id")

                            val createdsplit = created.split(" ")
                            dongcreatedTV.setText("시작일 " + createdsplit.get(0))
                            createdTV.setText(createdsplit.get(0))
                            roomcreatedTV.setText(createdsplit.get(0))

                            roomtitleTV.setText(title)
                            introduceTV.setText(introduce)

                            if (visible == "1"){

                            } else if (visible == "2"){
//                                privateIV.visibility = View.VISIBLE
                            }

                            if (PrefUtils.getIntPreference(context,"member_id") != Utils.getInt(chatroom,"member_id")){
                                cntIV.visibility = View.GONE
                                setnoticeTV.visibility = View.GONE
                                setroomtitleIV.visibility = View.GONE
                                setimageIV.visibility = View.GONE
                                introduceIV.visibility = View.GONE
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
//                    backgroundAdapter.notifyDataSetChanged()
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
        params.put("room_id", room_id)
        params.put("type", "in")

        println("--------add_chat_member room_id : $room_id ")

        ChattingAction.add_chat_member(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val intent = Intent(context, DongChatDetailActivity::class.java)
                        intent.putExtra("room_id", room_id)
                        startActivityForResult(intent, DONG_CHAT)
                        finish()
                    } else if (result == "alreay"){
                        val intent = Intent(context, DongChatDetailActivity::class.java)
                        intent.putExtra("room_id", room_id)
                        startActivityForResult(intent, DONG_CHAT)
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

    fun set_introduce(introduce: String){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("introduce", introduce)

        ChattingAction.set_introduce(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        introduceTV.setText(introduce)
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
                        detail_chatting("reset")
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
                            ImageLoader.getInstance().displayImage(contentURI.toString(), backgroundVP, Utils.UILoptionsProfile)
                            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)

                            val cursor = context.contentResolver.query(contentURI, filePathColumn, null, null, null)
                            if (cursor!!.moveToFirst()) {
                                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                                val picturePath = cursor.getString(columnIndex)

                                cursor.close()

                                val prof = Utils.getImage(context.contentResolver, picturePath.toString())

                                Image_path.clear()
                                Image_path.add(contentURI.toString())
//                                backgroundAdapter.notifyDataSetChanged()

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

                DONG_CHAT -> {
                    DONG_CHAT_RESULT = true
                }

            }
        }
    }


    fun doSomethingWithContext(context: Context) {
        this.context = context
        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)
    }

    override fun finish() {
        super.finish()

        println("DONG_CHAT_RESULT::::::::::::::::::::::::::::::$DONG_CHAT_RESULT")

        if (DONG_CHAT_RESULT == true) {
            var intent = getIntent()
            intent.putExtra("reset","reset")
            intent.putExtra("division","dong")
            setResult(RESULT_OK, intent);
        }
    }



}
