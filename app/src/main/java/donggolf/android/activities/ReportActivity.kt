package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.CommentAction
import donggolf.android.actions.MarketAction
import donggolf.android.adapters.ReportAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_report.*
import org.json.JSONObject

class ReportActivity : RootActivity() {

    var member_id = 0
    var market_id = 0
    var market_member_id = 0
    var reportListData:ArrayList<JSONObject> = ArrayList<JSONObject>()
    lateinit var reportAdapter: ReportAdapter

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        context = this

        if (intent.getIntExtra("member_id",0) != null){
            member_id = intent.getIntExtra("member_id",0)
            market_id = intent.getIntExtra("market_id",0)
            market_member_id =  intent.getIntExtra("market_member_id",0)
        }


        finishaBT.setOnClickListener {
            finish()
        }

        reportAdapter = ReportAdapter(context,R.layout.item_report,reportListData)
        reportlistLV.adapter = reportAdapter

        get_market_report()


        addreportTV.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("신고하시겠습니까 ?")

                    .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        add_report()
                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })

            val alert = builder.create()
            alert.show()
        }
    }





    fun get_market_report(){
        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("market_id",market_id)

        MarketAction.get_market_report(params,object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                // println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    val member = response!!.getJSONObject("member")
                    val nick = Utils.getString(member,"nick")
                    nickTV.setText(nick)

                    val list = response!!.getJSONArray("reportList")
                    if (list.length() > 0 && list != null){
                        for (i in 0 until list.length()){
                            reportListData.add(list.get(i) as JSONObject)
                        }
                    }

                    reportAdapter.notifyDataSetChanged()

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
            }
        })
    }

    fun add_report(){
        val content = contentTV.text.toString()
        if (content.isEmpty()) {
            Utils.alert(context, "내용을 입력해주세요.")
            return
        }

        val params = RequestParams()
        params.put("market_id", market_id)
        params.put("member_id",market_member_id)
        params.put("content",content)
        params.put("type",2)
        MarketAction.add_report(params,object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                var intent = Intent()
                setResult(RESULT_OK,intent)
                finish()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
            }
        })

    }

}
