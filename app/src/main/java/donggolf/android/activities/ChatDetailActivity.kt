package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AbsListView
import android.widget.BaseAdapter
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.adapters.ChattingAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_chat_detail.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ChatDetailActivity : RootActivity(), AbsListView.OnScrollListener {

    private var userScrolled: Boolean = false
    private var lastItemVisibleFlag: Boolean = false

    lateinit var context: Context

    var member_id = 0
    var mate_id: ArrayList<String> = ArrayList<String>()
    var mate_nick:ArrayList<String> = ArrayList<String>()
    var division = 0

    var room_id = ""

    var chatTitle = ""

    var first_id = -1
    var last_id = -1

    private  var memberList:ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  var chattingList:ArrayList<JSONObject> = ArrayList<JSONObject>()
    private lateinit var adapter: ChattingAdapter

    internal var loadDataHandler: Handler = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            chatting()
        }
    }

    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        context = this

        val intent = getIntent()

        division = intent.getIntExtra("division",0)
        member_id = PrefUtils.getIntPreference(context,"member_id")
        chattitleTV.setText(chatTitle)

        adapter = ChattingAdapter(this, R.layout.item_opponent_words, chattingList)

        chatLV.adapter = adapter
        chatLV.setOnScrollListener(this)

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

            addchat()
            detail_chatting()
        } else if (division == 1){        //1 기존
            room_id = intent.getStringExtra("id")
            timerStart()
            detail_chatting()
        }

        var author = intent.getStringExtra("Author")
        author = "개설자"
        if (author.equals("개설자")) {
            chatListRemoveLL.visibility = View.GONE

        } else if (author.equals("권한자")) {
            chatListRemoveLL.visibility = View.GONE
        } else {
            chatListRemoveLL.visibility = View.VISIBLE
        }

        btn_opMenu.setOnClickListener {
            drawerMenu.openDrawer(chat_right_menu)
        }

        chatLV.setOnItemClickListener { parent, view, position, id ->
            drawerMenu.closeDrawer(chat_right_menu)
        }

        showMoreTV.setOnClickListener {
            val it = Intent(context, ChatMemberActivity::class.java)
            startActivity(it)
        }

        finishaLL.setOnClickListener {
            finish()
        }

        addchattingTV.setOnClickListener {
            add_chatting()
        }

        addChatMemberLL.setOnClickListener {

        }


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
                            chattingList.add(0, data)
                        }

                    } else {
                        for (i in 0 until list.length()) {
                            val data = list.get(i) as JSONObject

                            chattingList.add(data)
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

    fun add_chatting(){
        val content = contentET.text.toString()
        contentET.setText("")

        val params = RequestParams()
        params.put("room_id", room_id)
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("mate_id", mate_id)
        params.put("nick",PrefUtils.getStringPreference(dialogContext, "nickname"))
        params.put("content",content)
        params.put("img","")

        ChattingAction.add_chatting(params, object : JsonHttpResponseHandler(){
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

    fun timerStart(){
        val task = object : TimerTask() {
            override fun run() {
                loadDataHandler.sendEmptyMessage(0)
            }
        }

        timer = Timer()
        timer!!.schedule(task, 0, 2000)

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

    fun detail_chatting(){
        val params = RequestParams()
        params.put("room_id", room_id)

        ChattingAction.detail_chatting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    if (mate_id != null){
                        mate_id.clear()
                    }
                    val members = response!!.getJSONArray("chatmember")
                    if (members != null && members.length() > 0){
                        for (i in 0 until members.length()){
                            val item = members.get(i) as JSONObject
                            val chatmember = item.getJSONObject("Chatmember")
                            val id = Utils.getString(chatmember,"id")
                            mate_id.add(id)
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
}
