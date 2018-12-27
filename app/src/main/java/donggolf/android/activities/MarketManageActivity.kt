package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_market_manage.*
import org.json.JSONObject

class MarketManageActivity : RootActivity() {

    private lateinit var context: Context

    var todayCount = 0
    var monthCount = 0
    var contentCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_manage)

        context = this

        finishaBT.setOnClickListener { finish() }

        servicecenterLL.setOnClickListener {
            val intent = Intent(this, InquireActivity::class.java)
            startActivity(intent)
        }

        myMarketPostLL.setOnClickListener {
            val intent = Intent(this, SellerActivity::class.java)
            intent.putExtra("type","my")
            startActivity(intent)
        }

        get_content_cnt()
    }

    fun get_content_cnt(){
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        MarketAction.get_content_cnt(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    todayCount = Utils.getInt(response,"today")
                    monthCount = Utils.getInt(response,"month")
                    contentCount = Utils.getInt(response,"contentcnt")

                    availDayTV.setText(todayCount.toString())
                    availMonthTV.setText(monthCount.toString())
                    mycontentTV.setText("내가 올린글(" + contentCount.toString() + ")")

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })
    }

}
