package donggolf.android.fragment

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.activities.*
import donggolf.android.adapters.ChatFragAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.fragment_chat.*
import org.json.JSONObject

class ChatFragment : android.support.v4.app.Fragment() {

    var ctx: Context? = null

    private var mAuth: FirebaseAuth? = null

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  lateinit var  adapter : ChatFragAdapter

    lateinit var tabMyChat : ImageView
    lateinit var tabTownChat : ImageView
    lateinit var btn_myChat_mng : ImageView
    lateinit var btn_make_chat : ImageView
    lateinit var txMyChat : TextView
    lateinit var txTownChat : TextView
    lateinit var chat_list : ListView
    lateinit var viewpagerChat : ViewPager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val ctx = context
        if (null != ctx) {
            doSomethingWithContext(ctx)
        }

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()

        val db = FirebaseFirestore.getInstance()

        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*tabMyChat = view.findViewById(R.id.tabMyChat)
        tabTownChat = view.findViewById(R.id.tabTownChat)
        btn_myChat_mng = view.findViewById(R.id.btn_myChat_mng)
        btn_make_chat = view.findViewById(R.id.btn_make_chat)
        txMyChat = view.findViewById(R.id.txMyChat)
        txTownChat = view.findViewById(R.id.txTownChat)*/

        chat_list = view.findViewById(R.id.chat_list)

        viewpagerChat = view.findViewById(R.id.viewpagerChat)

        var dataObj : JSONObject = JSONObject();

        ctx = context

        adapter = ChatFragAdapter(ctx!!,R.layout.item_my_chat_list,adapterData)

        chat_list.adapter = adapter

        var isMyChat = true

        getmychat(1)

        chat_list.setOnItemClickListener { parent, view, position, id ->
            var json = adapterData.get(position)
            var room = json.getJSONObject("Chatroom")
            val id = Utils.getString(room,"id")

            if (isMyChat) {
                var intent = Intent(activity, ChatDetailActivity::class.java)
                intent.putExtra("division",1)
                intent.putExtra("id",id)
                startActivity(intent)
            } else {
                var intent = Intent(activity, DongchatProfileActivity::class.java)
                startActivity(intent)
            }

        }

        addmychatIV.setOnClickListener {
            var intent = Intent(activity, SelectMemberActivity::class.java)
            startActivity(intent)
        }

        //new section
        myChatOnRL.setOnClickListener {
            myChatOnRL.visibility = View.GONE
            townChatOnRL.visibility = View.VISIBLE
            getmychat(1)
        }


        townChatOnRL.setOnClickListener {
            myChatOnRL.visibility = View.VISIBLE
            townChatOnRL.visibility = View.GONE
            getmychat(2)
        }


    }

    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
    }


    fun getmychat(type : Int){

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("type", type)

        ChattingAction.load_chatting(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {

                    if (adapterData != null){
                        adapterData.clear()
                    }
                    val chatlist = response!!.getJSONArray("chatlist")
                    if (chatlist.length() > 0 && chatlist != null){
                        for (i in 0 until chatlist.length()){
                            adapterData.add(chatlist.get(i) as JSONObject)
                        }
                    }

                    chatcountTV.setText(adapterData.size.toString())

                    adapter.notifyDataSetChanged()
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
