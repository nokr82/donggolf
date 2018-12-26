package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_goods_detail.*
import org.json.JSONObject
import java.util.ArrayList

class GoodsDetailActivity : RootActivity() {

    private lateinit var context: Context

    var _Images: ArrayList<String> = ArrayList<String>()
    var product_id = 0
    var seller_phone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_detail)

        context = this
        product_id = intent.getIntExtra("product_id",0)
        getProductData()

        finishLL.setOnClickListener {
            finish()
        }

        moresellerpostTV.setOnClickListener {
            MoveSellerActivity()
        }

        reportTV.setOnClickListener {
            MoveReportActivity()
        }


    }

    fun MoveSellerActivity(){
        var intent = Intent(this, SellerActivity::class.java)
        startActivity(intent)
    }

    fun MoveReportActivity(){
        var intent = Intent(this, ReportActivity::class.java)
        startActivity(intent)
    }

    fun getProductData(){
        val params = RequestParams()
        params.put("product_id", product_id)

        MarketAction.get_product_detail(params,object :JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    val product = response.getJSONObject("product")

                    categoryTV.text = "[${Utils.getString(product,"form").substringBefore(" ")}형]" +
                            "[${Utils.getString(product,"form").substringAfter(" ")}용]" +
                            " 브랜드 > ${Utils.getString(product,"brand")}"
                    tagTV.text = "# ${Utils.getString(product,"brand")} 정품"
                    sale_statusTV.text = Utils.getString(product,"status")
                    titleTV.text = Utils.getString(product,"title")
                    writtenDateTV.text = Utils.getString(product,"created")
                    descriptionTV.text = Utils.getString(product,"description")
                    priceTV.text = Utils._comma(Utils.getString(product,"price"))
                    if (Utils.getString(product,"deliv_pay") == "구매자 부담"){
                        delivPayTV.visibility = View.VISIBLE
                    } else {
                        delivPayTV.visibility = View.INVISIBLE
                    }
                    sale_regionTV.text = Utils.getString(product,"region").substring(0,2)
                    trade_methodTV.text = Utils.getString(product,"trade_way")
                    seller_phone = Utils.getString(product,"phone")
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
