package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.TextView
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
    var founder_id = ""
    var chatTitle = ""

    var first_id = -1
    var last_id = -1

    var friendyn = 0

    var room_type = -1

    private  var memberList:ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  var chattingList:ArrayList<JSONObject> = ArrayList<JSONObject>()
    private lateinit var adapter: ChattingAdapter

    var text_size = ""

    var my_nick = ""

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

        adapter = ChattingAdapter(this, R.layout.item_opponent_words, chattingList)

        chatLV.adapter = adapter
        chatLV.setOnScrollListener(this)

        contentET.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length==1000){
                    Toast.makeText(context,"한번에 입력되는 글자 크기는 1,000자 입니다.",Toast.LENGTH_SHORT).show()
                }
            }

            override fun afterTextChanged(count: Editable) {


            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
        })


        if (intent.getStringExtra("founder") != null){
            founder = intent.getStringExtra("founder")
            if (founder.toInt() != member_id){
                chatsetLL.visibility = View.GONE
//                chatsizeLL.visibility = View.GONE
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

            Utils.hideKeyboard(this)

            drawerMenu.openDrawer(chat_right_menu)
        }

        chat_right_menu.setOnClickListener {
            drawerMenu.closeDrawer(chat_right_menu)
        }

        chatLV.setOnItemClickListener { parent, view, position, id ->
            drawerMenu.closeDrawer(chat_right_menu)

        }


        showMoreTV.setOnClickListener {
            val intent = Intent(context, ChatMemberActivity::class.java)
            intent.putExtra("founder",founder)
            intent.putExtra("room_id",room_id)
            intent.putExtra("founder_id",founder_id)
            intent.putExtra("division","0")
            startActivity(intent)
        }

        allviewLL.setOnClickListener {
            val intent = Intent(context, ChatMemberActivity::class.java)
            intent.putExtra("founder",founder)
            intent.putExtra("room_id",room_id)
            intent.putExtra("founder_id",founder_id)
            intent.putExtra("division","0")
            startActivity(intent)
        }

        finishaLL.setOnClickListener {
            var intent = Intent()
            intent.putExtra("reset", "reset")
            setResult(RESULT_OK, intent)
            Utils.hideKeyboard(this)
            finish()

        }

        addchattingTV.setOnClickListener {
            add_chatting()
        }

        addChatMemberLL.setOnClickListener {

            if (founder_id.toInt() == PrefUtils.getIntPreference(context,"member_id") || friendyn == 1){
                val intent = Intent(context, SelectMemberActivity::class.java)
                intent.putExtra("founder",founder)
                intent.putExtra("room_id",room_id)
                intent.putExtra("member_count",memberList.size)
                intent.putExtra("member_ids",mate_id)
                intent.putExtra("member_nicks",mate_nick)
                intent.putExtra("division","0")
                startActivityForResult(intent,RESET)
            } else {
                Toast.makeText(context,"1촌이 아니시면 대화멤버를 추가하실 수 없습니다..",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                //println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                //println(errorResponse)
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

                    val members = response.getJSONArray("members")


                    countTV.setText(members.length().toString())

                    memberlistLL.removeAllViews()
                    addNickLL.removeAllViews()
                    mate_id.clear()
                    val count = members.length() - 3

                    for (i in 0 until members.length()){
                        val item = members.get(i) as JSONObject
                        val member = item.getJSONObject("Member")
                        val id = Utils.getString(member,"id")
                        mate_id.add(id)
                        val gander = Utils.getString(member,"sex")
                        val nick = Utils.getString(member,"nick")
                        val profile_img = Utils.getString(member,"profile_img")

                        var view:View = View.inflate(context, R.layout.item_profile, null)
                        var profileIV:CircleImageView = view.findViewById(R.id.profileIV)

                        var image = Config.url + profile_img
                        ImageLoader.getInstance().displayImage(image, profileIV, Utils.UILoptionsUserProfile)

                        memberlistLL.addView(view)


                        var v = View.inflate(context, R.layout.item_nick, null)
                        var fv = View.inflate(context, R.layout.item_nick, null)
                        val nickTV = v.findViewById(R.id.nickTV) as TextView
                        val female = fv.findViewById(R.id.nickTV) as TextView
                        nickTV.setTextColor(Color.parseColor("#000000"))
                        female.setTextColor(Color.parseColor("#EF5C34"))

                        if (i == 0) {
                            nickTV.setText(nick)
                            if (gander == "1"){
                                nickTV.setText("/")
                                addNickLL.addView(v)
                                female.setText(nick)
                                addNickLL.addView(fv)
                            } else {
                                addNickLL.addView(v)
                            }
                        } else if (i < 3) {
                            nickTV.setText("/"+nick)
                            if (gander == "1"){
                                nickTV.setText("/")
                                addNickLL.addView(v)
                                female.setText(nick)
                                addNickLL.addView(fv)
                            } else {
                                addNickLL.addView(v)
                            }
                        } else if (i == 3) {
                            nickTV.setText("외 " + count.toString() + "명")
                            addNickLL.addView(v)
                        }

                    }

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

                            chatLV.post {
                                chatLV.setSelection(adapter.count - 1)
                            }

                        }
                    }

                    if (list.length() > 0) {
                        adapter.notifyDataSetChanged()
                    }

                    if (chattingList.size > 0) {
                        val data = chattingList[chattingList.size - 1]
                        val chatting = data.getJSONObject("Chatting")
                        last_id = Utils.getInt(chatting, "id")
                    }

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
        val read_first_id = Utils.getInt(first_chat, "id")

        val last_data = chattingList[chattingList.size - 1]
        val last_chat = last_data.getJSONObject("Chatting")
        val read_last_id = Utils.getInt(last_chat, "id")

        val params = RequestParams()
        params.put("first_id", read_first_id)
        params.put("last_id", read_last_id)
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
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }
        })

    }

    fun add_chatting(){
        val content = contentET.text.toString()
        contentET.setText("")

        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))

        if (mate_id.size > 0){
            for (i in 0..mate_id!!.size - 1){
                params.put("mate_ids[" + i + "]", mate_id.get(i))
            }
        }

        params.put("mate_id", mate_id)


        params.put("nick",PrefUtils.getStringPreference(dialogContext, "nickname"))
        params.put("content",content)

        var type = ""

        if (comment_path.size > 0) {
            //            params.put("img", ByteArrayInputStream(Utils.getByteArray(comment_path)))
            type = "i"

            params.put("type", type)

//            comment_path = null

            for (i in 0..comment_path!!.size - 1){

                var bt: Bitmap = Utils.getImage(context.contentResolver, comment_path!!.get(i))

                params.put("files[" + i + "]",  ByteArrayInputStream(Utils.getByteArray(bt)))
            }

        } else {
            type = "c"

            if (content == null || content == ""){
                Toast.makeText(context,"입력한 내용이 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }

            params.put("type", type)
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
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
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

                    val room = response.getJSONObject("chatroom")
                    room_type = Utils.getInt(room, "type")
                    founder_id = Utils.getString(room,"member_id")

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
                    friendyn = response!!.getInt("friendyn")
                    var roomtitle = ""
                    memberlistLL.removeAllViews()
                    if (members != null && members.length() > 0){
                        if (members.length() > 2){
                            chatblockLL.visibility = View.GONE
                        }
                        val count = members.length() - 3
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
                            val gander = Utils.getString(memberinfo,"sex")
                            var v = View.inflate(context, R.layout.item_nick, null)
                            var fv = View.inflate(context, R.layout.item_nick, null)
                            val nickTV = v.findViewById(R.id.nickTV) as TextView
                            val female = fv.findViewById(R.id.nickTV) as TextView
                            nickTV.setTextColor(Color.parseColor("#000000"))
                            female.setTextColor(Color.parseColor("#EF5C34"))

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
                                my_nick = nick
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

                    if (room_type == 1) {
                        for (i in 0 until mate_nick.size) {
                            roomtitle += mate_nick.get(i) + " "
                        }
                    } else {
                        roomtitle = Utils.getString(room, "title")
                    }

                    chattitleTV.setText(roomtitle)
                    countTV.setText(memberList.size.toString())
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
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
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
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
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }
        })
    }

    fun delete_chat_member(last_id:Int,type:String){
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", room_id)
        params.put("chat_id",last_id)
        params.put("type",type)
        params.put("my_nick",my_nick)

        ChattingAction.delete_chat_member(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    if (type == "out"){
                        var intent = Intent()
                        intent.putExtra("reset","reset")
                        intent.putExtra("division","my")
                        setResult(RESULT_OK, intent);
                        finish()
                    } else {
                        chattingList.clear()
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
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
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }
        })
    }

    fun set_block(){
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("room_id", room_id)
        params.put("mate_id",chat_member_id)
        params.put("block_yn","Y")

        // println("-----set_block 가쟈~~")

        ChattingAction.set_block(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                // println("-----result : $result")
                if (result == "block") {
                    Toast.makeText(context,"이미 차단한 대화방 입니다.",Toast.LENGTH_SHORT).show()
                } else if (result == "ok"){
                    var intent = Intent()
                    intent.putExtra("reset","reset")
                    intent.action = "RESET_CHATTING"
                    sendBroadcast(intent)
                    setResult(RESULT_OK, intent);
                    finish()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
                // println("----------------1")
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
                // println("----------------2")
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
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
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

                    }

                    add_chatting()
                    set_in_yn("Y")

                    timerStart()

                }
            }
        }
    }

    override fun onBackPressed() {
        var intent = Intent()
        intent.putExtra("reset", "reset")
        Utils.hideKeyboard(context)
        setResult(RESULT_OK, intent)

        if (chat_right_menu.visibility == View.VISIBLE) {
            drawerMenu.closeDrawers()
        } else {
            finish()
        }

    }

    override fun finish() {
        super.finish()
        Utils.hideKeyboard(context)
        if (chattingList.size > 0) {
            val data = chattingList[chattingList.size - 1]

            val chatting = data.getJSONObject("Chatting")

            val content = Utils.getString(chatting, "content")
            val chatting_type = Utils.getString(chatting, "type")
            val chat_created = Utils.getString(chatting, "created")
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        if (resetReceiver != null) {
            context.unregisterReceiver(resetReceiver)
        }
    }

}
