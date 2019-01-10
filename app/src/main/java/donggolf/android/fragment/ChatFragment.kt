package donggolf.android.fragment

import android.app.AlertDialog
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
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.dlg_chat_blockcode.view.*
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
            val founder = Utils.getString(room,"member_id")
            val type = Utils.getString(room,"type")
            val block_code = Utils.getString(room,"block_code")

            if (founder.toInt() == PrefUtils.getIntPreference(context,"member_id")){
                if (type == "1") {
                    var intent = Intent(activity, ChatDetailActivity::class.java)
                    intent.putExtra("division",1)
                    intent.putExtra("id",id)
                    intent.putExtra("founder",founder)
                    startActivity(intent)
                } else {
                    var intent = Intent(activity, DongchatProfileActivity::class.java)
                    intent.putExtra("room_id",id)
                    startActivity(intent)
                }
            } else {
                if (block_code != null && block_code.length > 0){
                    val builder = AlertDialog.Builder(ctx!!)
                    val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
                    builder.setView(dialogView)
                    val alert = builder.show()

                    dialogView.dlgTitle.setText("비공개 코드 입력")
                    dialogView.codevisibleLL.visibility = View.GONE

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

                        if (code == block_code){
                            if (type == "1") {
                                var intent = Intent(activity, ChatDetailActivity::class.java)
                                intent.putExtra("division",1)
                                intent.putExtra("id",id)
                                intent.putExtra("founder",founder)
                                startActivity(intent)
                            } else {
                                var intent = Intent(activity, DongchatProfileActivity::class.java)
                                intent.putExtra("room_id",id)
                                startActivity(intent)
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
                        intent.putExtra("division",1)
                        intent.putExtra("id",id)
                        intent.putExtra("founder",founder)
                        startActivity(intent)
                    } else {
                        var intent = Intent(activity, DongchatProfileActivity::class.java)
                        intent.putExtra("room_id",id)
                        startActivity(intent)
                    }
                }
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
            getmychat(2)
        }


        townChatOnRL.setOnClickListener {
            myChatOnRL.visibility = View.VISIBLE
            townChatOnRL.visibility = View.GONE
            getmychat(1)
        }

        chatsettingIV.setOnClickListener {
            var intent = Intent(activity, SetAlarmActivity::class.java)
            startActivity(intent)
        }

        adddongchatIV.setOnClickListener {
            var intent = Intent(activity, AddDongChatActivity::class.java)
            startActivity(intent)
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

                    if (type == 1) {
                        chatcountTV.setText(adapterData.size.toString())
                    } else {
                        dongcountTV.setText(adapterData.size.toString())
                    }

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
