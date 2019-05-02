package donggolf.android.activities

import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.EventsAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_event_detail.*
import org.json.JSONObject

class EventDetailActivity : RootActivity() {

    private lateinit var context: Context
    private var progressDialog: ProgressDialog? = null

    var event_id = -1
    var member_id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_detail)

        context = this

        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

        member_id = PrefUtils.getIntPreference(context, "member_id")

        event_id = intent.getIntExtra("event_id", -1)

        loadData()

    }

    fun loadData() {
        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("event_id", event_id)

        EventsAction.detail(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                val result = response!!.getString("result")

                if (result == "ok") {

                    val event = response.getJSONObject("Event")


                    webWV.loadUrl("/events/view?id=${Utils.getInt(event, "id")}")

                } else {

                }

            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }

        })

    }

}
