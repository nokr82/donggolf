package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.actions.PostAction
import donggolf.android.adapters.ChattingAdapter
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_dong_chat_detail.*
import kotlinx.android.synthetic.main.dlg_chat_blockcode.view.*
import kotlinx.android.synthetic.main.dlg_set_text_size.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class DongChatDetailActivity : RootActivity() , AbsListView.OnScrollListener{

    lateinit var context: Context

    private var userScrolled: Boolean = false
    private var lastItemVisibleFlag: Boolean = false

    var room_id = ""
    var founder_id = ""

    var text_size = ""

    var mate_id: ArrayList<String> = ArrayList<String>()
    var mate_nick: ArrayList<String> = ArrayList<String>()
    private  var memberList:ArrayList<JSONObject> = ArrayList<JSONObject>()

    var first_id = -1
    var last_id = -1
    private  var chattingList:ArrayList<JSONObject> = ArrayList<JSONObject>()
    private lateinit var adapter: ChattingAdapter
    var max_count = 0
    var people_count = 0
    var block_code = ""
    var SET_NOTICE = 100

//    var comment_path: Bitmap? = null
    var comment_path: java.util.ArrayList<String> = java.util.ArrayList<String>()

    var block_member_ids:ArrayList<String> = ArrayList<String>()

    internal var loadDataHandler: Handler = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            chatting()
        }
    }

    internal var resetReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                detail_chatting()
            }
        }
    }

    private var timer: Timer? = null

    val BLOCK_MEMBER = 500
    val GALLERY = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dong_chat_detail)

        context = this

        room_id = intent.getStringExtra("room_id")

        var filter1 = IntentFilter("RESET_CHATTING")
        registerReceiver(resetReceiver, filter1)

        adapter = ChattingAdapter(this, R.layout.item_opponent_words, chattingList)

        chatCont.adapter = adapter
        chatCont.setOnScrollListener(this)

        chatCont.setOnItemClickListener { parent, view, position, id ->
            val item = chattingList.get(position) as JSONObject
            val chatting = item.getJSONObject("Chatting")
            val type = Utils.getString(chatting,"type")
            println("--------typ[e====== $type")
            if (type == "i"){
                val img = Utils.getString(chatting,"img")
                val imglist: java.util.ArrayList<String> = java.util.ArrayList<String>()
                imglist.add(img)
                val id = intent.getStringExtra("id")
                var intent = Intent(context, PictureDetailActivity::class.java)
                intent.putExtra("id", id)
                intent.putExtra("adPosition",0)
                intent.putExtra("paths",imglist)
                intent.putExtra("type","chat")
                startActivity(intent)
            }
        }

        btn_opDongchatMenu.setOnClickListener {
            dongchat_drawerMenu.openDrawer(dongchat_right_menu)
        }

        publicLL.setOnClickListener {
            radio_public.setImageResource(R.drawable.btn_radio_on)
            radio_secret.setImageResource(R.drawable.btn_radio_off)
            block_code = ""
            set_chatting_setting("1")
        }

        secretLL.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlgtextTV.visibility = View.GONE
            dialogView.blockcodeTV.setText(block_code)

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
                radio_public.setImageResource(R.drawable.btn_radio_off)
                radio_secret.setImageResource(R.drawable.btn_radio_on)
                block_code = code
                set_chatting_setting("1")
                alert.dismiss()
            }
        }

        detail_chatting()

        cancleLL.setOnClickListener {
            noticevisibleLL.visibility = View.GONE
            set_notice_yn("Y")
        }

        addchattingTV.setOnClickListener {
            add_chatting()
            addedImgIV.setImageResource(0)
            commentLL.visibility = View.GONE
            gofindpictureLL.visibility = View.VISIBLE
//            comment_path = null
            comment_path.clear()
        }

        chatsizeLL.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_set_text_size, null)
            builder.setView(dialogView)
            val alert = builder.show()

            if (text_size == "1"){
                dialogView.dlg_smallIV.setImageResource(R.drawable.btn_radio_on)
            } else if (text_size == "2"){
                dialogView.dlg_usuallyIV.setImageResource(R.drawable.btn_radio_on)
            } else if (text_size == "3"){
                dialogView.dlg_bigIV.setImageResource(R.drawable.btn_radio_on)
            } else if (text_size == "4"){
                dialogView.dlg_mostIV.setImageResource(R.drawable.btn_radio_on)
            }

            dialogView.dlg_smallLL.setOnClickListener {
                set_text_size("1")
                alert.dismiss()
                text_size = "1"
                textsizeTV.setText("작게")
                for (i in 0 until chattingList.size){
                    chattingList.get(i).put("text_size",text_size)
                }
                adapter.notifyDataSetChanged()
            }

            dialogView.dlg_usuallyLL.setOnClickListener {
                set_text_size("2")
                alert.dismiss()
                text_size = "2"
                textsizeTV.setText("보통")
                for (i in 0 until chattingList.size){
                    chattingList.get(i).put("text_size",text_size)
                }
                adapter.notifyDataSetChanged()
            }

            dialogView.dlg_bigLL.setOnClickListener {
                set_text_size("3")
                alert.dismiss()
                text_size = "3"
                textsizeTV.setText("크게")
                for (i in 0 until chattingList.size){
                    chattingList.get(i).put("text_size",text_size)
                }
                adapter.notifyDataSetChanged()
            }

            dialogView.dlg_mostLL.setOnClickListener {
                set_text_size("4")
                alert.dismiss()
                text_size = "4"
                textsizeTV.setText("아주 크게")
                for (i in 0 until chattingList.size){
                    chattingList.get(i).put("text_size",text_size)
                }
                adapter.notifyDataSetChanged()
            }
        }

        timerStart()

        pushoffIV.setOnClickListener {
            pushoffIV.visibility = View.GONE
            pushonIV.visibility = View.VISIBLE
            set_push("Y")
        }

        pushonIV.setOnClickListener {
            pushonIV.visibility = View.GONE
            pushoffIV.visibility = View.VISIBLE
            set_push("N")
        }

        outchatIV.setOnClickListener {

            if (PrefUtils.getIntPreference(context,"member_id") == founder_id.toInt()){
                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("채팅방을 삭제하시겠습니까 ?")

                        .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                            delete_chatting_room()
                        })
                        .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                        })

                val alert = builder.create()
                alert.show()
            } else {
                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("채팅방에서 나가시겠습니까 ?")

                        .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                            val lastMSG = chattingList.get(chattingList.size - 1)
                            val chatting = lastMSG.getJSONObject("Chatting")
                            val last_id = Utils.getInt(chatting, "id")
                            delete_chat_member(last_id)
                            finish()
                        })
                        .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                        })

                val alert = builder.create()
                alert.show()
            }
        }

        addChatMemberLL.setOnClickListener {
            val intent = Intent(context, SelectMemberActivity::class.java)
            intent.putExtra("founder",founder_id)
            intent.putExtra("room_id",room_id)
            intent.putExtra("member_count",memberList.size)
            intent.putExtra("member_ids",mate_id)
            intent.putExtra("member_nicks",mate_nick)
            intent.putExtra("division","1")
            intent.putExtra("max_count",max_count)
            intent.putExtra("people_count",people_count)
            startActivity(intent)
        }

        allviewLL.setOnClickListener {
            val intent = Intent(context, ChatMemberActivity::class.java)
            intent.putExtra("founder",founder_id)
            intent.putExtra("room_id",room_id)
            intent.putExtra("member_count",memberList.size)
            intent.putExtra("member_ids",mate_id)
            intent.putExtra("member_nicks",mate_nick)
            intent.putExtra("division","1")
            intent.putExtra("max_count",max_count)
            intent.putExtra("people_count",people_count)
            startActivity(intent)
        }

        downcancleLL.setOnClickListener {
            downnoticevisibleLL.visibility = View.GONE
            set_notice_yn("Y")
        }

        noticevisibleLL.setOnClickListener {
            val intent = Intent(context, NoticeManageActivity::class.java)
            intent.putExtra("room_id",room_id)
            startActivityForResult(intent,SET_NOTICE)
        }

        downnoticevisibleLL.setOnClickListener {
            val intent = Intent(context, NoticeManageActivity::class.java)
            intent.putExtra("room_id",room_id)
            startActivityForResult(intent,SET_NOTICE)
        }

        chatSettingLL.setOnClickListener {
            val intent = Intent(context, SelectMemberActivity::class.java)
            intent.putExtra("block","block")
            intent.putExtra("room_id",room_id)
            intent.putExtra("block_yn","Y")
            startActivityForResult(intent,BLOCK_MEMBER)
        }

        chatSettingmemberLL.setOnClickListener {
            val intent = Intent(context, SelectMemberActivity::class.java)
            intent.putExtra("block","block")
            intent.putExtra("room_id",room_id)
            intent.putExtra("block_yn","N")
            startActivity(intent)
        }

        chatReportLL.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("신고하시겠습니까 ?")

                    .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        add_repot()
                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        finish()
                    })

            val alert = builder.create()
            alert.show()
        }

        chatListRemoveLL.setOnClickListener {
            val intent = Intent(context, DongchatProfileActivity::class.java)
            intent.putExtra("room_id",room_id)
            startActivity(intent)
        }

        gofindpictureLL.setOnClickListener {
            permissionimage()
        }

        delIV.setOnClickListener {
            addedImgIV.setImageResource(0)
            commentLL.visibility = View.GONE
            gofindpictureLL.visibility = View.VISIBLE
//            comment_path = null
            comment_path.clear()
        }

    }

    private fun permissionimage() {

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                var intent = Intent(context, FindPictureActivity::class.java);
                intent.putExtra("image","image")

//            val galleryIntent = Intent(Intent.ACTION_PICK,
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                startActivityForResult(intent, GALLERY)
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(context,"권한설정을 해주셔야 합니다.",Toast.LENGTH_SHORT).show()
            }

        }

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

    }


    fun detail_chatting(){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        ChattingAction.detail_chatting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    val members = response!!.getJSONArray("chatmember")
                    var my_top_yn = ""
                    var my_notice_yn = ""
                    var mynotice = ""

                    if (members != null && members.length() > 0){
                        for (i in 0 until members.length()){
                            val item = members.get(i) as JSONObject
                            val chatmember = item.getJSONObject("Chatmember")
                            val chatroom = item.getJSONObject("Chatroom")
                            val memberinfo = item.getJSONObject("Member")
                            val title = Utils.getString(chatroom,"title")
                            val visible = Utils.getString(chatroom,"visible")
                            val top_yn = Utils.getString(chatroom,"top_yn")
                            block_code = Utils.getString(chatroom,"block_code")

                            val notice = Utils.getString(chatroom,"notice")
                            val id = Utils.getString(chatmember,"member_id")
                            val nick = Utils.getString(chatmember,"nick")
                            val notice_yn = Utils.getString(chatmember,"notice_yn")
                            max_count = Utils.getInt(chatroom,"max_count")
                            people_count = Utils.getInt(chatroom,"peoplecount")

                            if (id == PrefUtils.getIntPreference(context,"member_id").toString()){
                                my_notice_yn = notice_yn
                                my_top_yn = top_yn
                                mynotice = notice
                            }

                            nicknameTV.setText(title + "(" + members.length().toString() + ")")
                            founder_id = Utils.getString(memberinfo,"id")

                            if (visible == "1"){
                                radio_public.setImageResource(R.drawable.btn_radio_on)
                                radio_secret.setImageResource(R.drawable.btn_radio_off)
                                if (block_code != ""){
                                    radio_public.setImageResource(R.drawable.btn_radio_off)
                                    radio_secret.setImageResource(R.drawable.btn_radio_on)
                                }
                            } else if (visible == "2"){
                                radio_public.setImageResource(R.drawable.btn_radio_off)
                                radio_secret.setImageResource(R.drawable.btn_radio_on)
                            }

                            var view:View = View.inflate(context, R.layout.item_profile, null)
                            var profileIV:CircleImageView = view.findViewById(R.id.profileIV)

                            var image = Config.url + Utils.getString(memberinfo, "profile_img")
                            ImageLoader.getInstance().displayImage(image, profileIV, Utils.UILoptionsUserProfile)

                            memberlistLL.addView(view)

                            if (PrefUtils.getIntPreference(context,"member_id") != Utils.getInt(chatroom,"member_id")){
                                chatSettingLL.visibility = View.GONE
                                chatSettingmemberLL.visibility  = View.GONE
                                chatvisibleLL.visibility = View.GONE
                            } else {
                                chatReportLL.visibility = View.GONE
                                val push_yn = Utils.getString(chatmember,"push_yn")
                                text_size = Utils.getString(chatmember,"text_size")
                                if (push_yn == "Y"){
                                    pushoffIV.visibility = View.GONE
                                    pushonIV.visibility = View.VISIBLE
                                } else {
                                    pushoffIV.visibility = View.VISIBLE
                                    pushonIV.visibility = View.GONE
                                }

                                if (text_size == "1"){
                                    textsizeTV.setText("작게")
                                } else if (text_size == "2"){
                                    textsizeTV.setText("보통")
                                } else if (text_size == "3"){
                                    textsizeTV.setText("크게")
                                } else if (text_size == "4"){
                                    textsizeTV.setText("아주 크게")
                                }

                                mate_id.add(id)
                                memberList.add(memberinfo)
                                mate_nick.add(nick)
                            }

                        }
                    }

                    if (my_top_yn == "Y"){
                        downnoticevisibleLL.visibility = View.GONE
                        noticevisibleLL.visibility = View.VISIBLE
                    } else {
                        downnoticevisibleLL.visibility = View.VISIBLE
                        noticevisibleLL.visibility = View.GONE
                    }

                    if (my_notice_yn == "Y"){
                        downnoticevisibleLL.visibility = View.GONE
                        noticevisibleLL.visibility = View.GONE
                    }

                    if (mynotice != null && mynotice.length > 0){
                        noticeTV.setText(mynotice)
                        downnoticeTV.setText(mynotice)
                    } else {
                        noticeTV.setText("공지사항이 없습니다.")
                        downnoticeTV.setText("공지사항이 없습니다.")
                        noticevisibleLL.visibility = View.GONE
                        downnoticevisibleLL.visibility = View.GONE
                    }



                    chatmembercountTV.setText(members.length().toString())

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

    fun add_chatting(){
        val content = contentET.text.toString()
        contentET.setText("")

        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mate_id)
        params.put("nick",PrefUtils.getStringPreference(dialogContext, "nickname"))
        params.put("content",content)

        if (comment_path.size > 0) {
            //            params.put("img", ByteArrayInputStream(Utils.getByteArray(comment_path)))
            params.put("type", "i")
//            comment_path = null

            for (i in 0..comment_path!!.size - 1){

                var bt: Bitmap = Utils.getImage(context.contentResolver, comment_path!!.get(i))

                params.put("files[" + i + "]",  ByteArrayInputStream(Utils.getByteArray(bt)))
            }

        } else {
            params.put("type", "c")
        }


        ChattingAction.add_chatting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "block") {
                    Toast.makeText(context,"차단당한 게시물 입니다.", Toast.LENGTH_SHORT).show()
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

    fun chatting(){

        if (first_id < 1) {
            if (chattingList.size > 0) {
                try {
                    try {
                        val lastMSG = chattingList.get(chattingList.size - 1)
                        val chatting = lastMSG.getJSONObject("Chatting")
                        last_id = Utils.getInt(chatting, "id")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } catch (e: NumberFormatException) {

                }

            }
        }

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("first_id", first_id)
        params.put("last_id", last_id)
        params.put("room_id", room_id)

        ChattingAction.chatting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    val list = response.getJSONArray("list")
                    val room = response.getJSONObject("chatroom")
                    val roomtitle = Utils.getString(room,"title")
//                    nicknameTV.setText(roomtitle)

                    if (first_id > 0) {
                        for (i in 0 until list.length()) {
                            val data = list.get(i) as JSONObject
                            chattingList.add(0, data)
                            chattingList.get(i).put("text_size",text_size)
                        }

                    } else {
                        for (i in 0 until list.length()) {
                            val data = list.get(i) as JSONObject
                            chattingList.add(data)
                            chattingList.get(i).put("text_size",text_size)
                            chatCont.setSelection(adapter.count - 1)
                        }
                    }

                    if (chattingList.size > 0) {
                        val data = chattingList[chattingList.size - 1]
                        val chatting = data.getJSONObject("Chatting")
                        last_id = Utils.getInt(chatting, "id")
                    }


                    if (list.length() > 0) {
                        (adapter as BaseAdapter).notifyDataSetChanged()
                    }
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

    fun timerStart(){
        val task = object : TimerTask() {
            override fun run() {
                loadDataHandler.sendEmptyMessage(0)
            }
        }

        timer = Timer()
        timer!!.schedule(task, 0, 1000)

    }

    override fun onPause() {
        super.onPause()

        if (timer != null) {
            timer!!.cancel()
        }
    }


    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount

        if (firstVisibleItem == 0 && firstVisibleItem + visibleItemCount < totalItemCount) {
            if (chattingList.size > 0) {
                try {
                    val firstMSG = chattingList[0]
                    val chatting = firstMSG.getJSONObject("Chatting")
                    first_id = Utils.getInt(chatting, "id")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                last_id = -1
            } else {
                first_id = -1
                if (chattingList.size > 0) {
                    try {
                        val lastMSG = chattingList[chattingList.size - 1]
                        val chatting = lastMSG.getJSONObject("Chatting")
                        last_id = Utils.getInt(chatting, "id")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } else {
                    last_id = -1
                }

            }
        } else {
            first_id = -1
            if (chattingList.size > 0) {
                try {
                    val lastMSG = chattingList[chattingList.size - 1]
                    val chatting = lastMSG.getJSONObject("Chatting")
                    last_id = Utils.getInt(chatting, "id")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                last_id = -1
            }
        }
    }

    override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {

        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            userScrolled = true
            if (timer != null) {
                timer!!.cancel()
            }
        } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            if (timer != null) {
                timer!!.cancel()
            }
        } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (lastItemVisibleFlag) {
                if (chattingList.size > 0) {
                    val lastMSG = chattingList[chattingList.size - 1]
                    first_id = -1
                    try {
                        val chatting = lastMSG.getJSONObject("Chatting")
                        last_id = Utils.getInt(chatting, "id")
                    } catch (e: NumberFormatException) {
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            } else {
                /*
                if (first_id > 0) {
                    loadData();
                }
                */
            }

            if (chattingList.size > 0) {
                if (timer != null) {
                    timer!!.cancel()
                }

                val task = object : TimerTask() {
                    override fun run() {
                        loadDataHandler.sendEmptyMessage(0)
                    }
                }

                timer = Timer()
                timer!!.schedule(task, 1000, 2000)
            }

        } else {

        }
    }

    fun set_text_size(text_size:String){
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", room_id)
        params.put("text_size",text_size)

        ChattingAction.set_text_size(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {

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

    fun set_chatting_setting(visible: String){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("visible",visible)
        params.put("block_code",block_code)

        ChattingAction.set_chatting_setting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {

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

    fun set_push(push:String){
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", room_id)
        params.put("push_yn",push)

        ChattingAction.set_push(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {

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

    fun delete_chat_member(last_id:Int){
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", room_id)
        params.put("last_id",last_id)

        ChattingAction.delete_chat_member(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {

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



    fun delete_chatting_room(){
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", room_id)

        ChattingAction.delete_chatting_room(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    var intent = Intent()
                    intent.putExtra("reset","reset")
                    intent.putExtra("division","dong")
                    setResult(RESULT_OK, intent);
                    finish()
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

    fun add_repot(){
        var params = RequestParams()
        params.put("content_id", room_id)
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("type", 2)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
              SET_NOTICE -> {
                  if (data!!.getStringExtra("reset") != null) {
                      if (mate_id != null){
                          mate_id.clear()
                      }

                      if (memberList != null){
                          memberList.clear()
                      }

                      if (mate_nick != null){
                          mate_nick.clear()
                      }
                      memberlistLL.removeAllViews()

                      detail_chatting()
                  }
              }

                GALLERY -> {

                    var item = data?.getStringArrayExtra("images")
                    var name = data?.getStringArrayExtra("displayname")

                    if (comment_path != null){
                        comment_path.clear()
                    }

                    for (i in 0..(item!!.size - 1)) {
                        val str = item[i]

                        comment_path!!.add(str)

                        val add_file = Utils.getImage(context.contentResolver, str)
                    }

                    add_chatting()

                    timerStart()

//                    if (data != null)
//                    {
//
//                        val contentURI = data.data
//
//                        try
//                        {
////                            commentLL.visibility = View.VISIBLE
////                            gofindpictureLL.visibility = View.GONE
//
//                            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
//
//                            val cursor = context.contentResolver.query(contentURI, filePathColumn, null, null, null)
//                            if (cursor!!.moveToFirst()) {
//                                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
//                                val picturePath = cursor.getString(columnIndex)
//
//                                cursor.close()
//
//                                comment_path = Utils.getImage(context.contentResolver,picturePath.toString())
////                                addedImgIV.setImageBitmap(comment_path)
//                                add_chatting()
//
//                            }
//
//                        }
//                        catch (e: IOException) {
//                            e.printStackTrace()
//                        }
//
//
//
//                    }
                }

                BLOCK_MEMBER ->{
                    if (data!!.getStringExtra("reset") != null) {
                        if (mate_id != null){
                            mate_id.clear()
                        }

                        if (memberList != null){
                            memberList.clear()
                        }

                        if (mate_nick != null){
                            mate_nick.clear()
                        }
                        memberlistLL.removeAllViews()

                        detail_chatting()
                    }
                }
            }
        }
    }

    fun set_notice_yn(type:String){

        val params = RequestParams()
        params.put("room_id",room_id)
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("type", type)

        ChattingAction.set_notice_yn(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "block") {
                    Toast.makeText(context,"차단당한 게시물 입니다.", Toast.LENGTH_SHORT).show()
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
