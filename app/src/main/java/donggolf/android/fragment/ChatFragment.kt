package donggolf.android.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.activities.*
import donggolf.android.adapters.ChatFragAdapter
import donggolf.android.adapters.DongChatAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.dlg_chat_blockcode.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import org.json.JSONObject
import java.util.*

class ChatFragment : android.support.v4.app.Fragment() {

    var ctx: Context? = null

    private var mAuth: FirebaseAuth? = null

    private var adapterData: ArrayList<JSONObject> = ArrayList<JSONObject>()
    private var dongAdapterData: ArrayList<JSONObject> = ArrayList<JSONObject>()

    private lateinit var adapter: ChatFragAdapter
    private lateinit var dongAdapter: DongChatAdapter

    lateinit var chat_list: ListView
    lateinit var viewpagerChat: ViewPager
    lateinit var dong_chat_list: ListView

    val RESET = 1000
    val CHATRESET = 1001
    val ADDCHAT = 1002

    var todayCount = 0

    private var page = 1
    private var totalPage = 0
    private val visibleThreshold = 10

    private var userScrolled = false
    private var lastItemVisibleFlag = false
    private var lastcount = 0
    private var totalItemCountScroll = 0

    internal var resetChattingReciver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                if (townChatOnRL.visibility == View.VISIBLE) {
                    dongAdapterData.clear()
                    getmychat(2)
                } else {
                    adapterData.clear()
                    getmychat(1)
                }
            }
        }
    }


//    internal var chattingReciver: BroadcastReceiver? = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent?) {
//            if (intent != null) {
//
//                val room_id = intent.getIntExtra("room_id", -1)
//                val room_type = intent.getIntExtra("room_type", -1)
//
//                if (room_id > 0) {
//
//                    var has = false
//
//                    var content = intent.getStringExtra("content")
//                    val chatting_type = intent.getStringExtra("chatting_type")
//                    val chat_created = intent.getStringExtra("chat_created")
//
//                    if (chatting_type == "i") {
//                        content = "사진"
//                    }
//
//                    for (i in 0 until adapterData.size) {
//                        val data = adapterData[i]
//                        val chatroom = data.getJSONObject("Chatroom")
//
//                        val id = Utils.getInt(chatroom, "id")
//
//                        if (id == room_id) {
//                            has = true
//                            chatroom.put("contents", content)
//                            chatroom.put("created", chat_created)
//                            chatroom.put("readdiv", "0")
//                            adapterData.removeAt(i)
//                            adapterData.add(0, data)
//                            break
//                        }
//
//                    }
//
//                    adapter.notifyDataSetChanged()
//
//                    if (room_type != 1) {
//
//                        for (i in 0 until dongAdapterData.size) {
//                            val data = dongAdapterData[i]
//                            val chatroom = data.getJSONObject("Chatroom")
//
//                            val id = Utils.getInt(chatroom, "id")
//
//                            if (id == room_id) {
//                                has = true
//                                chatroom.put("contents", content)
//                                chatroom.put("created", chat_created)
//                                chatroom.put("readdiv", "0")
//                                dongAdapterData.removeAt(i)
//                                dongAdapterData.add(0, data)
//                                break
//                            }
//
//                        }
//
//                        dongAdapter.notifyDataSetChanged()
//
//                    }
//
//                    if (!has) {
//                        load_add_new_chatting(room_id)
//                    }
//
//
//                }
//
//            }
//        }
//    }

    internal var loadDataHandler: Handler = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            if (townChatOnRL.visibility == View.VISIBLE) {
                getmychat(2)
            } else {
                getmychat(1)
            }
        }
    }


    internal var reloadchatReciver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                getmychat(2)
                getmychat(1)
            }
        }
    }


    private var timer: Timer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val ctx = context
        if (null != ctx) {
            doSomethingWithContext(ctx)
        }

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()

        val db = FirebaseFirestore.getInstance()

//        timerStart()

        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        chat_list = view.findViewById(R.id.chat_list)
        dong_chat_list = view.findViewById(R.id.dong_chat_list)

        viewpagerChat = view.findViewById(R.id.viewpagerChat)

        var dataObj: JSONObject = JSONObject();

        ctx = context

        adapter = ChatFragAdapter(ctx!!, R.layout.item_my_chat_list, adapterData)
        dongAdapter = DongChatAdapter(ctx!!, R.layout.item_my_chat_list, dongAdapterData)

        chat_list.adapter = adapter
        dong_chat_list.adapter = dongAdapter

        var isMyChat = true

        // getmychat(1)

        var filter1 = IntentFilter("RESET_CHATTING")
        ctx!!.registerReceiver(resetChattingReciver, filter1)

//        var filter2 = IntentFilter("CHATTING")
//        ctx!!.registerReceiver(chattingReciver, filter2)

        var filter3 = IntentFilter("CHAT_CHANGE")
        ctx!!.registerReceiver(reloadchatReciver, filter3)

        var filter4 = IntentFilter("SET_REGION")
        ctx!!.registerReceiver(reloadchatReciver, filter4)


        dong_chat_list.setOnItemClickListener { parent, view, position, id ->
            var json = dongAdapterData.get(position)
            var room = json.getJSONObject("Chatroom")
            val id = Utils.getString(room, "id")
            val founder = Utils.getString(room, "member_id")
            val type = Utils.getString(room, "type")
            val block_code = Utils.getString(room, "block_code")
            val max_count = Utils.getString(room,"max_count")
            //println("----------------------id : $id")

            if (founder.toInt() == PrefUtils.getIntPreference(context, "member_id")) {
                if (type == "1") {
                    var intent = Intent(activity, ChatDetailActivity::class.java)
                    intent.putExtra("division", 1)
                    intent.putExtra("id", id)
                    intent.putExtra("founder", founder)
                    startActivityForResult(intent, RESET)
                } else {
                    var intent = Intent(activity, DongchatProfileActivity::class.java)
                    intent.putExtra("room_id", id)
                    startActivityForResult(intent, RESET)
                }
            } else {
                if (block_code != null && block_code.length > 0) {
                    val builder = AlertDialog.Builder(ctx!!)
                    val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
                    builder.setView(dialogView)
                    val alert = builder.show()

                    dialogView.dlgTitle.setText("비공개 코드 입력")
                    dialogView.categoryTitleET.setHint("코드를 입력해 주세요.")
                    dialogView.codevisibleLL.visibility = View.GONE

                    dialogView.btn_title_clear.setOnClickListener {
                        alert.dismiss()
                    }

                    dialogView.cancleTV.setOnClickListener {
                        alert.dismiss()
                    }

                    dialogView.okTV.setOnClickListener {
                        val code = dialogView.categoryTitleET.text.toString()
                        if (code == null || code == "") {
                            Toast.makeText(context, "빈칸은 입력하실 수 없습니다", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (code == block_code) {
                            var chatmember = json.getJSONArray("Chatmember")

                            if (max_count.toInt() <= chatmember.length()){
                                Toast.makeText(context, "멤버(FULL)상태라 입장 할 수 없습니다.", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            if (type == "1") {
                                var intent = Intent(activity, ChatDetailActivity::class.java)
                                intent.putExtra("division", 1)
                                intent.putExtra("id", id)
                                intent.putExtra("founder", founder)
                                startActivityForResult(intent, RESET)
                            } else {
                                var intent = Intent(activity, DongchatProfileActivity::class.java)
                                intent.putExtra("room_id", id)
                                startActivityForResult(intent, RESET)
                            }
                        } else {
                            Toast.makeText(context, "코드가 다릅니다", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        alert.dismiss()
                    }
                } else {
                    if (type == "1") {
                        var intent = Intent(activity, ChatDetailActivity::class.java)
                        intent.putExtra("division", 1)
                        intent.putExtra("id", id)
                        intent.putExtra("founder", founder)
                        startActivityForResult(intent, RESET)
                    } else {
                        var intent = Intent(activity, DongchatProfileActivity::class.java)
                        intent.putExtra("room_id", id)
                        startActivityForResult(intent, RESET)
                    }
                }
            }
            room.put("readdiv", "1")
            dongAdapter.notifyDataSetChanged()
        }

        chat_list.setOnItemClickListener { parent, view, position, id ->
            if (myChatOnRL.visibility == View.VISIBLE) {
                var json = adapterData.get(position)
                var room = json.getJSONObject("Chatroom")
                val id = Utils.getString(room, "id")
                val founder = Utils.getString(room, "member_id")
                val type = Utils.getString(room, "type")
                val block_code = Utils.getString(room, "block_code")

                if (founder.toInt() == PrefUtils.getIntPreference(context, "member_id")) {
                    if (type == "1") {
                        var intent = Intent(activity, ChatDetailActivity::class.java)
                        intent.putExtra("division", 1)
                        intent.putExtra("id", id)
                        intent.putExtra("founder", founder)
                        startActivityForResult(intent, RESET)
                    } else {
                        var intent = Intent(activity, DongchatProfileActivity::class.java)
                        intent.putExtra("room_id", id)
                        startActivityForResult(intent, RESET)
                    }
                } else {
                    if (block_code != null && block_code.length > 0) {
                        val builder = AlertDialog.Builder(ctx!!)
                        val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
                        builder.setView(dialogView)
                        val alert = builder.show()

                        dialogView.dlgTitle.setText("비공개 코드 입력")
                        dialogView.categoryTitleET.setHint("코드를 입력해 주세요.")
                        dialogView.codevisibleLL.visibility = View.GONE

                        dialogView.btn_title_clear.setOnClickListener {
                            dialogView.blockcodeTV.setText("")
                            alert.dismiss()

                        }

                        dialogView.cancleTV.setOnClickListener {
                            alert.dismiss()
                        }

                        dialogView.okTV.setOnClickListener {
                            val code = dialogView.categoryTitleET.text.toString()
                            if (code == null || code == "") {
                                Toast.makeText(context, "빈칸은 입력하실 수 없습니다", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            if (code == block_code) {
                                if (type == "1") {
                                    var intent = Intent(activity, ChatDetailActivity::class.java)
                                    intent.putExtra("division", 1)
                                    intent.putExtra("id", id)
                                    intent.putExtra("founder", founder)
                                    startActivityForResult(intent, RESET)
                                } else {
                                    var intent = Intent(activity, DongchatProfileActivity::class.java)
                                    intent.putExtra("room_id", id)
                                    startActivityForResult(intent, RESET)
                                }
                            } else {
                                Toast.makeText(context, "코드가 다릅니다", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            alert.dismiss()
                        }
                    } else {
                        if (type == "1") {
                            var intent = Intent(activity, ChatDetailActivity::class.java)
                            intent.putExtra("division", 1)
                            intent.putExtra("id", id)
                            intent.putExtra("founder", founder)
                            startActivityForResult(intent, RESET)
                        } else {
                            var intent = Intent(activity, DongchatProfileActivity::class.java)
                            intent.putExtra("room_id", id)
                            startActivityForResult(intent, RESET)
                        }
                    }
                }

                room.put("readdiv", "1")

            } else {
                var json = dongAdapterData.get(position)
                var room = json.getJSONObject("Chatroom")
                val id = Utils.getString(room, "id")
                val founder = Utils.getString(room, "member_id")
                val type = Utils.getString(room, "type")
                val block_code = Utils.getString(room, "block_code")

                if (founder.toInt() == PrefUtils.getIntPreference(context, "member_id")) {
                    if (type == "1") {
                        var intent = Intent(activity, ChatDetailActivity::class.java)
                        intent.putExtra("division", 1)
                        intent.putExtra("id", id)
                        intent.putExtra("founder", founder)
                        startActivityForResult(intent, RESET)
                    } else {
                        var intent = Intent(activity, DongchatProfileActivity::class.java)
                        intent.putExtra("room_id", id)
                        startActivityForResult(intent, RESET)
                    }
                } else {
                    if (block_code != null && block_code.length > 0) {
                        val builder = AlertDialog.Builder(ctx!!)
                        val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
                        builder.setView(dialogView)
                        val alert = builder.show()

                        dialogView.dlgTitle.setText("비공개 코드 입력")
                        dialogView.categoryTitleET.setHint("코드를 입력해 주세요.")
                        dialogView.codevisibleLL.visibility = View.GONE

                        dialogView.btn_title_clear.setOnClickListener {
                            dialogView.blockcodeTV.setText("")
                            alert.dismiss()
                        }

                        dialogView.cancleTV.setOnClickListener {
                            alert.dismiss()
                        }

                        dialogView.okTV.setOnClickListener {
                            val code = dialogView.categoryTitleET.text.toString()
                            if (code == null || code == "") {
                                Toast.makeText(context, "빈칸은 입력하실 수 없습니다", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            if (code == block_code) {
                                if (type == "1") {
                                    var intent = Intent(activity, ChatDetailActivity::class.java)
                                    intent.putExtra("division", 1)
                                    intent.putExtra("id", id)
                                    intent.putExtra("founder", founder)
                                    startActivityForResult(intent, RESET)
                                } else {
                                    var intent = Intent(activity, DongchatProfileActivity::class.java)
                                    intent.putExtra("room_id", id)
                                    startActivityForResult(intent, RESET)
                                }
                            } else {
                                Toast.makeText(context, "코드가 다릅니다", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            alert.dismiss()
                        }
                    } else {
                        if (type == "1") {
                            var intent = Intent(activity, ChatDetailActivity::class.java)
                            intent.putExtra("division", 1)
                            intent.putExtra("id", id)
                            intent.putExtra("founder", founder)
                            startActivityForResult(intent, RESET)
                        } else {
                            var intent = Intent(activity, DongchatProfileActivity::class.java)
                            intent.putExtra("room_id", id)
                            startActivityForResult(intent, RESET)
                        }
                    }
                }
                room.put("readdiv", "1")
            }

            adapter.notifyDataSetChanged()
            dongAdapter.notifyDataSetChanged()

        }

        chat_list.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(p0: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount
                totalItemCountScroll = totalItemCount
            }

            override fun onScrollStateChanged(chat_list: AbsListView, newState: Int) {

                if (!chat_list.canScrollVertically(-1)) {
                    page = 1
                    getmychat(1)
                }

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
//                    activity.maintitleLL.visibility=View.GONE
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag) {
                    userScrolled = false
//                    activity.maintitleLL.visibility=View.VISIBLE

                    //화면이 바닥에 닿았을때
                    if (totalPage > page) {
                        page++;
                        lastcount = totalItemCountScroll;

                        getmychat(1)
                    }
                }

                /*
                if (!chat_list.canScrollVertically(-1)) {
                    page = 1

                    if (myChatOnRL.visibility == View.VISIBLE) {
                        getmychat(1)
                    } else {
                        getmychat(2)
                    }

                } else if (!chat_list.canScrollVertically(1)) {
                    if (totalPage > page) {
                        page++
                        lastcount = totalItemCountScroll
                        if (myChatOnRL.visibility == View.VISIBLE) {
                            getmychat(1)
                        } else {
                            getmychat(2)
                        }
                    }
                }
                */
            }
        })

        dong_chat_list.setOnScrollListener(object : AbsListView.OnScrollListener {

            override fun onScroll(p0: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount
                totalItemCountScroll = totalItemCount

            }

            override fun onScrollStateChanged(dong_chat_list: AbsListView, newState: Int) {

                if (!dong_chat_list.canScrollVertically(-1)) {
                    page = 1
                    getmychat(1)
                }

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
//                    activity.maintitleLL.visibility=View.GONE
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag) {
                    userScrolled = false
//                    activity.maintitleLL.visibility=View.VISIBLE

                    //화면이 바닥에 닿았을때
                    if (totalPage > page) {
                        page++;
                        lastcount = totalItemCountScroll;

                        getmychat(2)
                    }
                }

                /*
                if (!dong_chat_list.canScrollVertically(-1)) {
                    page = 1

                    if (myChatOnRL.visibility == View.VISIBLE) {
                        getmychat(1)
                    } else {
                        getmychat(2)
                    }

                } else if (!dong_chat_list.canScrollVertically(1)) {
                    if (totalPage > page) {
                        page++
                        lastcount = totalItemCountScroll
                        if (myChatOnRL.visibility == View.VISIBLE) {
                            getmychat(1)
                        } else {
                            getmychat(2)
                        }
                    }
                }
                */

            }
        })

        addmychatIV.setOnClickListener {
            var intent = Intent(activity, SelectMemberActivity::class.java)
            intent.putExtra("new", "new")
            startActivity(intent)
        }

        //new section
//        myChatOnRL.setOnClickListener {
//            if (PrefUtils.getStringPreference(context,"region_id") != null) {
//                myChatOnRL.visibility = View.GONE
//                townChatOnRL.visibility = View.VISIBLE
//                getmychat(2)`
//            } else {
//                Toast.makeText(context, "지역설정부터 해주세요.", Toast.LENGTH_SHORT).show()
//            }
//        }

        dongChatOnRL.setOnClickListener {
            if (PrefUtils.getStringPreference(context, "region_id") != null) {
                myChatOnRL.visibility = View.GONE
                townChatOnRL.visibility = View.VISIBLE
                chat_list.visibility = View.GONE
                dong_chat_list.visibility = View.VISIBLE
                page = 1
                getmychat(2)
            } else {
                Toast.makeText(context, "지역설정부터 해주세요.", Toast.LENGTH_SHORT).show()
            }
        }


        ChatOnRL.setOnClickListener {
            myChatOnRL.visibility = View.VISIBLE
            townChatOnRL.visibility = View.GONE
            chat_list.visibility = View.VISIBLE
            dong_chat_list.visibility = View.GONE
            page = 1
            getmychat(1)
        }

        chatsettingIV.setOnClickListener {
            var intent = Intent(activity, SetAlarmActivity::class.java)
            startActivityForResult(intent, CHATRESET)
        }

        adddongchatIV.setOnClickListener {
            if (todayCount == 5) {
                Toast.makeText(context, "하루에 5개 이상 채팅방을 생성하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var intent = Intent(activity, AddDongChatActivity::class.java)
            startActivityForResult(intent, ADDCHAT)
        }


    }

    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
    }


    fun getmychat(type: Int) {

        val params = RequestParams()
        params.put("page", page)

        if (type == 1) {
            if (PrefUtils.getIntPreference(context, "member_id") == -1) {
                Toast.makeText(context, "비회원은 채팅을 이용하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (PrefUtils.getStringPreference(context, "region_id") != null) {
            var region_id = PrefUtils.getStringPreference(context, "region_id")
            params.put("region", region_id)
        }

        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        params.put("type", type)

        if (type == 2) {
            if (PrefUtils.getStringPreference(context, "region_id") != null) {
                var region_id = PrefUtils.getStringPreference(context, "region_id")
                var region_id2 = PrefUtils.getStringPreference(context, "region_id2")
                params.put("region", region_id)
                params.put("region2", region_id2)
                //println("----------region_id : $region_id")
            } else {
                Toast.makeText(context, "지역설정부터 해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        ChattingAction.new_load_chatting(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {

                    if (activity == null || activity!!.isDestroyed || activity!!.isFinishing || response == null) {
                        return
                    }

                    todayCount = response!!.getInt("todayCount")
                    val mychat_count = response!!.getInt("mychat_count")
                    val dongchat_count = response!!.getInt("dongchat_count")

                    chatcountTV.text=mychat_count.toString()
                    dongchatcountTV.text=dongchat_count.toString()
                    mychatcountTV.text=mychat_count.toString()
                    dongcountTV.text=dongchat_count.toString()

                    if (type == 1) {
                        if (page == 1) {
                            adapterData.clear()
                            adapter.notifyDataSetChanged()
                        }
                        totalPage = response.getInt("totalPage");
                        page = response.getInt("page");

                        val chatlist = response!!.getJSONArray("chatlist")
                        if (chatlist.length() > 0 && chatlist != null) {
                            for (i in 0 until chatlist.length()) {
                                adapterData.add(chatlist.get(i) as JSONObject)
                            }
                        }

//                        if (type == 1) {
//                            chatcountTV.setText(adapterData.size.toString())
//                        } else {
//                            dongcountTV.setText(adapterData.size.toString())
//                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        if (page == 1) {
                            dongAdapterData.clear()
                            dongAdapter.notifyDataSetChanged()
                        }
                        totalPage = response.getInt("totalPage");
                        page = response.getInt("page");

                        if (dongAdapterData != null) {
                            val chatlist = response!!.getJSONArray("chatlist")
                            if (chatlist.length() > 0 && chatlist != null) {
                                for (i in 0 until chatlist.length()) {
                                    dongAdapterData.add(chatlist.get(i) as JSONObject)
                                }
                            }
//                            dongcountTV.setText(dongAdapterData.size.toString())
                        }
                        dongAdapter.notifyDataSetChanged()
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

    fun load_add_new_chatting(room_id: Int) {

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("room_id", room_id)
        params.put("region", PrefUtils.getStringPreference(context, "region_id"))
        params.put("region2", PrefUtils.getStringPreference(context, "region_id2"))

        ChattingAction.load_add_chatting(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")

                if (result == "ok") {

                    val room = response.getJSONObject("room")
                    val chatroom = room.getJSONObject("Chatroom")

                    val mychat_count = response!!.getInt("mychat_count")
                    val dongchat_count = response!!.getInt("dongchat_count")

                    chatcountTV.setText(mychat_count.toString())
                    dongchatcountTV.setText(dongchat_count.toString())
                    mychatcountTV.setText(mychat_count.toString())
                    dongcountTV.setText(dongchat_count.toString())

                    val type = Utils.getInt(chatroom, "type")

                    adapterData.add(0, room)
                    adapter.notifyDataSetChanged()

                    if (type != 1) {
                        dongAdapterData.add(0, room)
                        dongAdapter.notifyDataSetChanged()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                RESET -> {
                    //println("data::::::::::::::::::::::::::::::::::::::${data!!.getStringExtra("reset")}")
                    if (data!!.getStringExtra("reset") != null) {
                        var division = data!!.getStringExtra("division")
                        if (division == "my") {
                            //println("--------reset")
                            adapterData.clear()
                            page = 1
                            getmychat(1)
//                            timerStart()
                        } else {
                            dongAdapterData.clear()
                            page = 1
                            getmychat(2)
//                            timerStart()
                        }
                    }
                }

                CHATRESET -> {
                    adapterData.clear()
                    page = 1
                    getmychat(1)
//                    timerStart()
                }

                ADDCHAT -> {
                    if (data!!.getStringExtra("reset") != null) {
                        adapterData.clear()
                        page = 1
                        if (ChatOnRL.visibility == View.VISIBLE) {
                            getmychat(1)
                        } else {
                            getmychat(2)
                        }
//                        timerStart()
                    }
                }

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (resetChattingReciver != null) {
            context!!.unregisterReceiver(resetChattingReciver)
        }
        /*if (chattingReciver != null) {
            context!!.unregisterReceiver(chattingReciver)
        }*/
        if (reloadchatReciver != null) {
            context!!.unregisterReceiver(reloadchatReciver)
        }

     /*   if (setregionReciver != null) {
            context!!.unregisterReceiver(setregionReciver)
        }*/
    }

    fun timerStart() {
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

    override fun onResume() {
        super.onResume()
        getmychat(1)
        getmychat(2)
        if (timer != null) {
            timer!!.cancel()
        }
    }

}
