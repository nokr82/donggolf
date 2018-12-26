package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
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
    var tmp_member_phone = ""

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

        contact_sellerLL.setOnClickListener {
            var myPhoneNum = PrefUtils.getStringPreference(context,"userPhone")
            val smsMng = SmsManager.getDefault()
            smsMng.sendTextMessage(seller_phone,null,
                    "[동네골프]안녕하세요? 중고장터에 올라온 게시글을 보고 연락드립니다. 판매의사 있으시면 회신 바랍니다.",
                    null, null)
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
                    val market = product.getJSONObject("Market")

                    categoryTV.text = "[${Utils.getString(market,"form").substringBefore(" ")}형]" +
                            "[${Utils.getString(market,"form").substringAfter(" ")}용]" +
                            " 브랜드 > ${Utils.getString(market,"brand")}"
                    tagTV.text = "# ${Utils.getString(market,"brand")} 정품"
                    sale_statusTV.text = Utils.getString(market,"status")
                    titleTV.text = Utils.getString(market,"title")
                    writtenDateTV.text = Utils.getString(market,"created")
                    descriptionTV.text = Utils.getString(market,"description")
                    priceTV.text = Utils._comma(Utils.getString(market,"price"))
                    if (Utils.getString(market,"deliv_pay") == "구매자 부담"){
                        delivPayTV.visibility = View.VISIBLE
                    } else {
                        delivPayTV.visibility = View.INVISIBLE
                    }
                    sale_regionTV.text = Utils.getString(market,"region").substring(0,2)
                    trade_methodTV.text = Utils.getString(market,"trade_way")
                    seller_phone = Utils.getString(market,"phone")



                    val seller = response.getJSONObject("seller")
                    val member = seller.getJSONObject("Member")
                    nickTV.text = Utils.getString(member,"nick")

                    val img_uri = Utils.getString(member,"profile_img")
                    val image = Config.url + img_uri
                    ImageLoader.getInstance().displayImage(image, profileImgIV, Utils.UILoptionsProfile)

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
