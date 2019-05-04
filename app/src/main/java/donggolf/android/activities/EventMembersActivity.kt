package donggolf.android.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.EventsAction
import donggolf.android.adapters.EventMemberAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_event_members.*
import org.json.JSONObject

class EventMembersActivity : RootActivity() {

    lateinit var context: Context
    private var progressDialog: ProgressDialog? = null

    var member_id = -1

    lateinit var adapter: EventMemberAdapter

    var adapterData = ArrayList<JSONObject>()

    var event_id = -1
    var finish = ""

    var limit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_members)

        context = this

        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

        member_id = PrefUtils.getIntPreference(context, "member_id")

        event_id = intent.getIntExtra("event_id", -1)
        finish = intent.getStringExtra("finish")
        val numbers = intent.getStringExtra("numbers")

        if (finish == "Y") {
            titleTV.text = "추첨 결과 보기(추첨번호 : ${numbers})"
        } else {
            titleTV.text = "이벤트 참여자 보기"
        }

        adapter = EventMemberAdapter(context, R.layout.item_event_member, adapterData, this)
        memberLV.adapter = adapter

        memberLV.setOnItemClickListener { parent, view, position, id ->
            val json = adapterData[position]

            var eventMember = json.getJSONObject("EventMember")
            var member_id = Utils.getInt(eventMember,"member_id")

            if (this.member_id == member_id){
                Toast.makeText(context,"자기 자신은 프로필을 볼 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }

            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("member_id", member_id.toString())
            startActivity(intent)

        }

        btnBack.setOnClickListener {
            finish()
        }

        loadData()

    }

    fun loadData(){
        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("event_id", event_id)

        EventsAction.event_members(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")

                println("res : $response")

                if (result == "ok") {

                    val event = response.getJSONObject("Event")
                    val list = response.getJSONArray("list")

                    limit = Utils.getInt(event, "limit")

                    for (i in 0 until list.length()){
                        adapterData.add(list.get(i) as JSONObject)
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
