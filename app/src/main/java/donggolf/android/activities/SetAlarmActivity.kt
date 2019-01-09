package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ChattingAction
import donggolf.android.adapters.SetAlarmAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_set_alarm.*
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
            finish()
        }

        setAllAlarm.setOnClickListener {
            setAllAlarm.visibility = View.GONE
            setAlarm.visibility = View.VISIBLE
        }

        setAlarm.setOnClickListener {
            setAllAlarm.visibility = View.VISIBLE
            setAlarm.visibility = View.GONE
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
