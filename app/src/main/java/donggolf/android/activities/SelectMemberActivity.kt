package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MateAction
import donggolf.android.adapters.SelectMemberAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_select_member.*
import kotlinx.android.synthetic.main.item_select_member.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class SelectMemberActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  memberAdapter : SelectMemberAdapter
    private  var memberList: ArrayList<JSONObject> = ArrayList<JSONObject>()

    var mate_ids:ArrayList<String> = ArrayList<String>()
    var mate_nicks:ArrayList<String> = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_member)

        context = this

        memberAdapter = SelectMemberAdapter(context,R.layout.item_select_member,memberList)

        selMemList.adapter = memberAdapter

        getFriendList("m")

        selMemList.setOnItemClickListener { parent, view, position, id ->
            var json = memberList.get(position)
            var chk = Utils.getBoolen(json,"isSelectedOp")
            if (chk == false){
                memberList[position].put("isSelectedOp", true)
                memberAdapter.notifyDataSetChanged()
            } else {
                memberList[position].put("isSelectedOp", false)
                memberAdapter.notifyDataSetChanged()
            }

            if (memberList.size > 0 && memberList != null){
                var count = 0
                for (i in 0 until memberList.size){
                   val json =  memberList.get(i)
                    var chk = Utils.getBoolen(json,"isSelectedOp")
                    if (chk == true){
                        count++
                    }
                }
                countTV.setText(count.toString())
            }
        }

        addchatTV.setOnClickListener {
            val count = countTV.text.toString().toInt()
            if (count > 0 ){
                if (memberList.size > 0 && memberList != null){
                    var count = 0
                    for (i in 0 until memberList.size){
                        val json =  memberList.get(i)
                        var chk = Utils.getBoolen(json,"isSelectedOp")
                        if (chk == true){
                            var member = json.getJSONObject("MateMember")
                            var mate_id = Utils.getString(member,"id")
                            var mate_nick = Utils.getString(member,"nick")

                            mate_ids.add(mate_id)
                            mate_nicks.add(mate_nick)
                        }
                    }


                    val intent = Intent(context, ChatDetailActivity::class.java)
                    intent.putExtra("member_id",PrefUtils.getIntPreference(context,"member_id"))
                    intent.putExtra("mate_id",mate_ids)
                    intent.putExtra("mate_nick",mate_nicks)
                    intent.putExtra("division",0)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(context, "1명 이상 초대해야 합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun getFriendList(status : String) {
        //친구 리스트 받아오기
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("status", status)
        params.put("division",0)

        MateAction.mateList(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val friendList = response!!.getJSONArray("mates")
                        if (friendList != null && friendList.length() > 0){
                            for (i in 0 until friendList.length()){
                                memberList.add(friendList.get(i) as JSONObject)
                                memberList.get(i).put("isSelectedOp", false)
                            }
                        }
                        memberAdapter.notifyDataSetChanged()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
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
