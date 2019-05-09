package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.adapters.SetAlarmAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_set_alarm.*
import kotlinx.android.synthetic.main.dlg_chat_setting.view.*
import org.json.JSONObject

class SetAlarmActivity : RootActivity() {

    lateinit var context : Context
    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  lateinit var  adapter : SetAlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alarm)

        context = this

        adapter = SetAlarmAdapter(context,R.layout.item_set_chat,adapterData)

        set_chat_list.adapter = adapter

        btnBack.setOnClickListener {
            Utils.hideKeyboard(this)
            setResult(Activity.RESULT_OK)
            finish()
        }

        setAllAlarm.setOnClickListener {
            setAllAlarm.visibility = View.GONE
            setAlarm.visibility = View.VISIBLE
            set_all_push("Y")
        }

        setAlarm.setOnClickListener {
            setAllAlarm.visibility = View.VISIBLE
            setAlarm.visibility = View.GONE
            set_all_push("N")
        }

        set_chat_list.setOnItemClickListener { parent, view, position, id ->
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_chat_setting, null)
            builder.setView(dialogView)
            val alert = builder.show()

            val item = adapterData.get(position)
            var room = item.getJSONObject("Chatroom")
            var room_id = Utils.getString(room,"id")
            val chatmember = item.getJSONObject("Chatmember")
            val chatmember_id = Utils.getString(chatmember,"member_id")
            val push_yn = Utils.getString(chatmember,"push_yn")
            if (PrefUtils.getIntPreference(context,"member_id") == chatmember_id.toInt()) {
                if (push_yn == "Y") {
                    dialogView.pushIV.visibility = View.VISIBLE
                    dialogView.pushoffIV.visibility = View.GONE
                    dialogView.silentIV.visibility = View.GONE
                } else if (push_yn == "C"){
                    dialogView.pushIV.visibility = View.GONE
                    dialogView.pushoffIV.visibility = View.GONE
                    dialogView.silentIV.visibility = View.VISIBLE
                } else {
                    dialogView.pushIV.visibility = View.GONE
                    dialogView.pushoffIV.visibility = View.VISIBLE
                    dialogView.silentIV.visibility = View.GONE
                }
            }

            dialogView.dlg_comment_blockLL.setOnClickListener {
                set_push(room_id,PrefUtils.getIntPreference(context,"member_id").toString(),"C")
                Toast.makeText(context, "채팅방 알림이 설정 되었습니다.", Toast.LENGTH_SHORT).show()
                alert.dismiss()
            }

            dialogView.dlg_prod_modLL.setOnClickListener {
                set_push(room_id,PrefUtils.getIntPreference(context,"member_id").toString(),"Y")
                Toast.makeText(context, "채팅방 알림이 설정 되었습니다.", Toast.LENGTH_SHORT).show()
                alert.dismiss()
            }

            dialogView.dlg_comment_delLL.setOnClickListener {
                set_push(room_id,PrefUtils.getIntPreference(context,"member_id").toString(),"N")
                Toast.makeText(context, "채팅방 알림이 설정 되었습니다.", Toast.LENGTH_SHORT).show()
                alert.dismiss()
            }

            dialogView.dlg_close_IV.setOnClickListener {
                alert.dismiss()
            }

            setView()
        }

        getmychat(1)

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
                    adapter.notifyDataSetChanged()

                    setView()

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

    fun set_push(room_id:String, member_id:String,push_yn:String){
        val params = RequestParams()
        params.put("member_id",member_id)
        params.put("room_id", room_id)
        params.put("push_yn",push_yn)

        ChattingAction.set_push(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    getmychat(1)
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

    fun set_all_push(push_yn:String){
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("push_yn",push_yn)

        ChattingAction.set_all_push(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    getmychat(1)
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

    fun setView(){
        var chk = true
        for (i in 0 until adapterData.size){
            val item = adapterData.get(i)
            val chatmember = item.getJSONObject("Chatmember")
            val push_yn = Utils.getString(chatmember,"push_yn")
            if (push_yn == "N"){
                chk = false
            }
        }

        if (chk == true){
            setAllAlarm.visibility = View.GONE
            setAlarm.visibility = View.VISIBLE
        } else {
            setAllAlarm.visibility = View.VISIBLE
            setAlarm.visibility = View.GONE
        }
    }

    override fun onBackPressed() {

        Utils.hideKeyboard(context)
        setResult(Activity.RESULT_OK)
        finish()

    }
}
