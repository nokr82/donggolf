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
import donggolf.android.adapters.MutualAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_mutual.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MutualActivity : RootActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    private  lateinit var  adapter : MutualAdapter

    var mate_id = ""

    companion object {
        const val TAG = "MutualActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mutual)

        context = this

        var intent = getIntent()

        mate_id = intent.getStringExtra("mate_id")

        adapter = MutualAdapter(context, R.layout.item_friend_search, adapterData)
        mutualList.adapter = adapter

        get_together_mate()

        finishLL.setOnClickListener {
            finish()
            Utils.hideKeyboard(this)
        }

        mutualList.setOnItemClickListener { parent, view, position, id ->
            val item = adapter.getItem(position)
            val member = item.getJSONObject("MateMember")
            val member_id = Utils.getString(member,"id")


            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("member_id", member_id)
            startActivity(intent)
        }


    }


    fun get_together_mate(){
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("mate_id", mate_id)

        MateAction.get_together_mate(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val mates = response!!.getJSONArray("mates")
                        if (mates.length() == 0) {
                            Toast.makeText(context, "함께 아는 친구가 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                        for (i in 0 until mates.length()){
                            val item = mates.get(i) as JSONObject
                            adapterData.add(item)
                        }
                        adapter.notifyDataSetChanged()
                    } else if (result == "fail"){
                        Toast.makeText(context, "함께 아는 친구가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONArray?) {
                //println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })
    }
}

