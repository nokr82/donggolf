package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_chat_detail.*
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class ChatDetailActivity : RootActivity() {

    lateinit var context: Context

    var member_id = ""
    var mate_id: ArrayList<String> = ArrayList<String>()
    var mate_nick:ArrayList<String> = ArrayList<String>()
    var division = 0

    var chatTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        context = this

        val intent = getIntent()

        division = intent.getIntExtra("division",0)
        member_id = intent.getStringExtra("member_id")
        mate_id = intent.getStringArrayListExtra("mate_id")
        mate_nick = intent.getStringArrayListExtra("mate_nick")

        if (division == 0 ){        //0 신규생성
            addchat()
        } else if (division == 1){        //1 기존

        }

        if (mate_nick.size > 0 && mate_nick != null){
            for (i in 0 until mate_nick.size){
                if (i == mate_nick.size - 1){
                    chatTitle += mate_nick.get(i)
                } else {
                    chatTitle += mate_nick.get(i) + ","

                }
            }
        }

        chattitleTV.setText(chatTitle)

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
}
