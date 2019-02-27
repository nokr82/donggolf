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
import donggolf.android.adapters.ChattingAdapter
import donggolf.android.base.*
import donggolf.android.models.ImagesPath
import donggolf.android.models.TmpContent
import kotlinx.android.synthetic.main.activity_chat_detail.*
import kotlinx.android.synthetic.main.dlg_set_text_size.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*

class ChatDetailActivity : RootActivity(), AbsListView.OnScrollListener {

    private var userScrolled: Boolean = false
    private var lastItemVisibleFlag: Boolean = false

    lateinit var context: Context

    var member_id = 0
    var mate_id: ArrayList<String> = ArrayList<String>()
    var mate_nick:ArrayList<String> = ArrayList<String>()
    var division = 0
    var chat_member_id = 0

    var room_id = ""
    var founder = ""
    var chatTitle = ""

    var first_id = -1
    var last_id = -1

    private  var memberList:ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  var chattingList:ArrayList<JSONObject> = ArrayList<JSONObject>()
    private lateinit var adapter: ChattingAdapter

    var text_size = ""

    internal var loadDataHandler: Handler = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            chatting()
            readCount()
        }
    }

    internal var resetReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                detail_chatting()
            }
        }
    }

//    var comment_path: Bitmap? = null
    var comment_path:ArrayList<String> = ArrayList<String>()

    var RESET = 100
    var GALLERY = 1000

    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        context = this

        val intent = getIntent()

        var filter1 = IntentFilter("RESET_CHATTING")
        registerReceiver(resetReceiver, filter1)

        division = intent.getIntExtra("division",0)
        member_id = PrefUtils.getIntPreference(context,"member_id")
        chattitleTV.setText(chatTitle)

        adapter = ChattingAdapter(this, R.layout.item_opponent_words, chattingList)

        chatLV.adapter = adapter
        chatLV.setOnScrollListener(this)


        if (intent.getStringExtra("founder") != null){
            founder = intent.getStringExtra("founder")
            if (founder.toInt() != member_id){
                chatsetLL.visibility = View.GONE
                chatsizeLL.visibility = View.GONE
            }
        }


        if (division == 0 ){        //0 신규생성
            mate_id = intent.getStringArrayListExtra("mate_id")
            mate_nick = intent.getStringArrayListExtra("mate_nick")

            if (mate_nick.size > 0 && mate_nick != null){
                for (i in 0 until mate_nick.size){
                    if (i == mate_nick.size - 1){
                        chatTitle += mate_nick.get(i)
                    } else {
                        chatTitle += mate_nick.get(i) + ","
                    }
                }
            }

//            addchat()
            detail_chatting()
        } else if (division == 1){        //1 기존
            room_id = intent.getStringExtra("id")
            timerStart()
            detail_chatting()
            set_in_yn("Y")
        }

        var author = intent.getStringExtra("Author")
        author = "개설자"
        if (author.equals("개설자")) {
            chatListRemoveLL.visibility = View.VISIBLE

        } else if (author.equals("권한자")) {
            chatListRemoveLL.visibility = View.VISIBLE
        } else {
            chatListRemoveLL.visibility = View.VISIBLE
        }

        btn_opMenu.setOnClickListener {
            drawerMenu.openDrawer(chat_right_menu)
        }

        chatLV.setOnItemClickListener { parent, view, position, id ->
            drawerMenu.closeDrawer(chat_right_menu)

            val item = chattingList.get(position) as JSONObject
//            val chatting = item.getJSONObject("Chatting")
//            val type = Utils.getString(chatting,"type")
//            println("--------typ[e====== $type")
//            if (type == "i"){
//                val img = Utils.getString(chatting,"img")
//                val imglist:ArrayList<String> = ArrayList<String>()
//                imglist.add(img)
//                val id = intent.getStringExtra("id")
//                var intent = Intent(context, PictureDetailActivity::class.java)
//                intent.putExtra("id", id)
//                intent.putExtra("adPosition",0)
//                intent.putExtra("paths",imglist)
//                intent.putExtra("type","chat")
//                startActivity(intent)
//            }

        }


        showMoreTV.setOnClickListener {
            val intent = Intent(context, ChatMemberActivity::class.java)
            intent.putExtra("founder",founder)
            intent.putExtra("room_id",room_id)
            intent.putExtra("division","0")
            startActivity(intent)
        }

        allviewLL.setOnClickListener {
            val intent = Intent(context, ChatMemberActivity::class.java)
            intent.putExtra("founder",founder)
            intent.putExtra("room_id",room_id)
            intent.putExtra("division","0")
            startActivity(intent)
        }

        finishaLL.setOnClickListener {
            var intent = Intent()
            intent.putExtra("reset", "reset")
            setResult(RESULT_OK, intent);
            finish()
            Utils.hideKeyboard(this)
        }

        addchattingTV.setOnClickListener {
            add_chatting()
        }

        addChatMemberLL.setOnClickListener {
            val intent = Intent(context, SelectMemberActivity::class.java)
            intent.putExtra("founder",founder)
            intent.putExtra("room_id",room_id)
            intent.putExtra("member_count",memberList.size)
            intent.putExtra("member_ids",mate_id)
            intent.putExtra("member_nicks",mate_nick)
            intent.putExtra("division","0")
            startActivityForResult(intent,RESET)
        }

        settingmoreRL.setOnClickListener{
            if (chatsettingLL.visibility == View.VISIBLE){
                chatsettingLL.visibility = View.GONE
            } else {
                chatsettingLL.visibility = View.VISIBLE
            }
        }

        chatvisibleLL.setOnClickListener {
            chatvisibleIV.visibility = View.VISIBLE
            privateIV.visibility = View.GONE
            private_invisibleIV.visibility = View.GONE
            set_chatting_setting("1")
        }

        privateLL.setOnClickListener {
            chatvisibleIV.visibility = View.GONE
            privateIV.visibility = View.VISIBLE
            private_invisibleIV.visibility = View.GONE
            set_chatting_setting("2")
        }

        private_invisibleLL.setOnClickListener {
            chatvisibleIV.visibility = View.GONE
            privateIV.visibility = View.GONE
            private_invisibleIV.visibility = View.VISIBLE
            set_chatting_setting("3")
        }

        chatblockLL.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("차단하시겠습니까 ?")

                    .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                        set_block()
                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })

            val alert = builder.create()
            alert.show()
        }

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
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("채팅방에서 나가시겠습니까 ?")

                    .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        if (chattingList.size > 0) {
                            val lastMSG = chattingList.get(chattingList.size - 1)
                            val chatting = lastMSG.getJSONObject("Chatting")
                            val last_id = Utils.getInt(chatting, "id")
                        }
                        delete_chat_member(last_id,"out")
                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })

            val alert = builder.create()
            alert.show()
        }

        chatListRemoveLL.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("대화내용을 삭제하시겠습니까 ?")

                    .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        if (chattingList.size > 0){
                        val lastMSG = chattingList.get(chattingList.size - 1)
                        val chatting = lastMSG.getJSONObject("Chatting")
                        val last_id = Utils.getInt(chatting, "id")
                        delete_chat_member(last_id,"delete")
                        } else {
                            Toast.makeText(context,"대화내용이 아무것도 없습니다.", Toast.LENGTH_SHORT).show()

                        }
                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })

            val alert = builder.create()
            alert.show()
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

        gofindpictureLL.setOnClickListener {
//            val galleryIntent = Intent(Intent.ACTION_PICK,
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            permissionimage()
        }



    }

    private fun permissionimage() {

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                var intent = Intent(context, FindPictureActivity::class.java);
                intent.putExtra("image","image")
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

    fun addchat(){

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mate_id)
        params.put("chatTitle", chatTitle)
        params.put("regions", "")
        params.put("intro", "")
        params.put("type", "1")
        params.put("division",0)

        ChattingAction.add_chat(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    timerStart()
                    room_id = response!!.getString("lastid")
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
                    chattitleTV.setText(roomtitle)

                    if (first_id > 0) {
                        for (i in 0 until list.length()) {
                            val data = list.get(i) as JSONObject

                            if (insertCheckData(data.getJSONObject("Chatting"))) {
                                chattingList.add(0, data)
                                chattingList.get(i).put("text_size",text_size)
                            }

                        }

                    } else {
                        for (i in 0 until list.length()) {
                            val data = list.get(i) as JSONObject

                            if (insertCheckData(data.getJSONObject("Chatting"))) {
                                chattingList.add(data)
                                chattingList.get(i).put("text_size",text_size)
                            }

                            chatLV.setSelection(adapter.count - 1)
                        }
                    }

                    if (chattingList.size > 0) {
                        val data = chattingList[chattingList.size - 1]
                        val chatting = data.getJSONObject("Chatting")
                        last_id = Utils.getInt(chatting, "id")
                    }

                    (adapter as BaseAdapter).notifyDataSetChanged()
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

    fun insertCheckData(data: JSONObject) : Boolean {

        val check_id = Utils.getInt(data, "id")

        var add = true

        for (i in 0 until chattingList.size) {
            val data = chattingList[i]
            val chat = data.getJSONObject("Chatting")

            val id = Utils.getInt(chat, "id")

            if (check_id == id) {
                add = false
                break
            }

        }

        return add
    }

    fun readCount(){

        if (chattingList.size < 1) {
            return
        }

        val first_data = chattingList[0]
        val first_chat = first_data.getJSONObject("Chatting")
        val first_id = Utils.getInt(first_chat, "id")

        val last_data = chattingList[chattingList.size - 1]
        val last_chat = last_data.getJSONObject("Chatting")
        val last_id = Utils.getInt(last_chat, "id")

        val params = RequestParams()
        params.put("first_id", first_id)
        params.put("last_id", last_id)
        params.put("room_id", room_id)

        ChattingAction.chatting_read_count(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                val result = response!!.getString("result")
                if (result == "ok") {
                    val list = response.getJSONArray("list")

                    for (i in 0 until list.length()) {
                        val data = list[i] as JSONObject
                        val chatting = data.getJSONObject("Chatting")

                        val id = Utils.getInt(chatting, "id")

                        for (j in 0 until chattingList.size) {
                            val data2 = chattingList[j]
                            val chatting2 = data2.getJSONObject("Chatting")

                            val id2 = Utils.getInt(chatting2, "id")

                            if (id == id2) {
                                chatting2.put("peoplecount", Utils.getInt(chatting, "peoplecount"))
                                chatting2.put("read_count", Utils.getInt(chatting, "read_count"))
                                break
                            }
                        }

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
                comment_path.clear()
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

        set_in_yn("N")
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

    fun detail_chatting(){
        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))

        ChattingAction.detail_chatting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    memberlistLL.removeAllViews()
                    if (mate_nick != null){
                        mate_nick.clear()
                    }

                    if (mate_id != null){
                        mate_id.clear()
                    }

                    if (memberList != null){
                        memberList.clear()
                    }

                    val members = response!!.getJSONArray("chatmember")
                    var roomtitle = ""
                    memberlistLL.removeAllViews()
                    if (members != null && members.length() > 0){
                        if (members.length() > 2){
                            chatblockLL.visibility = View.GONE
                        }
                        for (i in 0 until members.length()){
                            val item = members.get(i) as JSONObject
                            val chatmember = item.getJSONObject("Chatmember")
                            val chatroom = item.getJSONObject("Chatroom")
                            val memberinfo = item.getJSONObject("Member")
                            val id = Utils.getString(chatmember,"member_id")
                            if (PrefUtils.getIntPreference(context,"member_id") != id.toInt()){
                                chat_member_id = id.toInt()
                            }

//                            roomtitle = Utils.getString(chatroom,"title")
                            val visible = Utils.getString(chatroom,"visible")
                            val nick = Utils.getString(memberinfo,"nick")
//                            chattitleTV.setText(roomtitle)

                            var view:View = View.inflate(context, R.layout.item_profile, null)
                            var profileIV:CircleImageView = view.findViewById(R.id.profileIV)

                            var image = Config.url + Utils.getString(memberinfo, "profile_img")
                            ImageLoader.getInstance().displayImage(image, profileIV, Utils.UILoptionsUserProfile)

                            memberlistLL.addView(view)

                            if (visible == "1"){

                            } else if (visible == "2"){
                                privateIV.visibility = View.VISIBLE
                            }

                            if (PrefUtils.getIntPreference(context,"member_id") == id.toInt()){
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
                            }

                            mate_id.add(id)
                            memberList.add(memberinfo)
                            mate_nick.add(nick)

                        }

                    }

                    if (roomtitle == "") {
                        for (i in 0 until mate_nick.size) {
                            roomtitle += mate_nick.get(i) + " "
                        }
                    }

                    chattitleTV.setText(roomtitle)
                    countTV.setText(memberList.size.toString())
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

    fun delete_chat_member(last_id:Int,type:String){
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", room_id)
        params.put("chat_id",last_id)
        params.put("type",type)

        ChattingAction.delete_chat_member(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    var intent = Intent()
                    intent.putExtra("reset","reset")
                    intent.putExtra("division","my")
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

    fun set_block(){
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", room_id)
        params.put("mate_id",chat_member_id)
        params.put("block_yn","N")

        ChattingAction.set_block(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                println("-----result : $result")
                if (result == "block") {
                    Toast.makeText(context,"이미 차단한 대화방 입니다.",Toast.LENGTH_SHORT).show()
                } else if (result == "ok"){
                    var intent = Intent()
                    intent.putExtra("reset","reset")
                    intent.putExtra("division","my")
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

    fun set_in_yn(in_yn:String){
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", room_id)
        params.put("in_yn",in_yn)

        ChattingAction.set_in_yn(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")

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
                RESET -> {
                    if (data!!.getStringExtra("reset") != null) {
                        detail_chatting()
                    }

                    if (data!!.getStringExtra("finish") != null){
                        finish()
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
                    set_in_yn("Y")

                    timerStart()

                    if (data != null)
                    {

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



                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        var intent = Intent()
        intent.putExtra("reset", "reset")
        setResult(RESULT_OK, intent);
        finish()

    }

}
