package donggolf.android.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.adapters.FushFragAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_notice2.*
import org.json.JSONArray
import org.json.JSONObject

class AlarmActivity : RootActivity() {

    private lateinit var context: Context
    private var progressDialog: ProgressDialog? = null

    private var member_id = -1;
    private var page = 1;
    private var totalPage = 1;

    private var adapterData = ArrayList<JSONObject>()
    private lateinit var adapter : FushFragAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice2)

        context = this
        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

        member_id = PrefUtils.getIntPreference(context, "member_id")

        finishLL.setOnClickListener {
            finish()
        }

        settingLV.setOnClickListener {
            var intent = Intent(context, SetNoticeActivity::class.java)
            startActivity(intent)
        }

        adapter = FushFragAdapter(context, R.layout.item_fushfrag, adapterData)
        noticeLV.adapter = adapter
        noticeLV.setOnItemClickListener { parent, view, position, id ->

            var data = adapterData.get(position)

            var alarm = data.getJSONObject("Alarm")

            var type = Utils.getInt(alarm, "type")

            if(type == 1) {

                var intent = Intent(context, MainDetailActivity::class.java)
                intent.putExtra("id", Utils.getString(alarm, "content_id"))
                startActivity(intent)

            } else if (type == 2) {

                var intent = Intent(context, GoodsDetailActivity::class.java)
                intent.putExtra("product_id", Utils.getInt(alarm, "market_id"))
                startActivity(intent)

            } else {

                var intent = Intent(context, RequestFriendActivity::class.java)
                intent.putExtra("type","waiting")
                startActivity(intent)

            }

        }

        loadData()

    }

    fun loadData() {

        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("page", page)

        MemberAction.alarms(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                val result = response!!.getString("result")

                if (result == "ok") {
                    noDataTV.visibility = View.GONE
                    noticeLV.visibility = View.VISIBLE

                    page = Utils.getInt(response, "page")
                    totalPage = Utils.getInt(response, "totalPage")

                    var list = response.getJSONArray("list")

                    for (i in 0 until list.length()) {
                        adapterData.add(list.get(i) as JSONObject)
                    }

                    adapter.notifyDataSetChanged()

                } else if(result == "empty") {
                    noDataTV.visibility = View.VISIBLE
                    noticeLV.visibility = View.GONE
                } else {

                }

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                 System.out.println(responseString);

                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONArray?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onStart() {
                // show dialog
                if (progressDialog != null) {

                    progressDialog!!.show()
                }
            }

            override fun onFinish() {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
            }

            private fun error() {
                Utils.alert(context, "조회중 장애가 발생하였습니다.")
            }

        })

    }

    override fun onDestroy() {
        super.onDestroy()

        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }

        progressDialog = null

    }
}

