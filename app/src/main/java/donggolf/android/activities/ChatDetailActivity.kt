package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceActivity
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_chat_detail.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ChatDetailActivity : RootActivity() {

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

    internal var loadDataHandler: Handler = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {

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
        } else if (division == 1){        //1 기존
            room_id = intent.getStringExtra("id")

            detailchat()
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

        chatCont.setOnItemClickListener { parent, view, position, id ->
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

    fun detailchat(){

        val params = RequestParams()
        params.put("room_id", room_id)

        ChattingAction.detail_chatting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    val room = response!!.getJSONObject("chatroom")
                    val roomtitle = Utils.getString(room,"title")
                    chattitleTV.setText(roomtitle)

                    val members = response!!.getJSONArray("chatmember")
                    if (members.length() > 0 && members != null){
                        for (i in 0 until members.length()){
                            memberList.add(members.get(i) as JSONObject)

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
        params.put("member_id",PrefUtils.getStringPreference(context,"member_id"))
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

}
