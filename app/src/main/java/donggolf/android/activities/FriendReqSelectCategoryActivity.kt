package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MateAction
import donggolf.android.adapters.FriendCategoryAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_friend_req_select_category.*
import org.json.JSONObject

class FriendReqSelectCategoryActivity : RootActivity() {

    lateinit var context: Context
    lateinit var selCategAdapter : FriendCategoryAdapter
    var categoryList = ArrayList<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_req_select_category)

        context = this

        selCategAdapter = FriendCategoryAdapter(context, R.layout.item_friend_category_list, categoryList)
        selectCategoryLV.adapter = selCategAdapter

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        MateAction.getCategoryInfo(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok") {
                    val categories = response.getJSONArray("categories")
                    for (i in 0..categories.length()-1 ) {
                        categoryList.add(categories[i] as JSONObject)
                    }
                    selCategAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }
        })

        selectCategoryLV.setOnItemClickListener { parent, view, position, id ->
            var data = categoryList.get(position)
            Log.d("리스트선택", data.toString())

            val category = data.getJSONObject("MateCategory")
            val category_id = Utils.getInt(category,"id")

            var intent = Intent()
            intent.putExtra("CategoryID", category_id)
            setResult(RESULT_OK,intent)

            finish()
        }
    }
}
