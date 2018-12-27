package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.telephony.SmsManager
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.adapters.FullScreenImageAdapter
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
    private lateinit var prodImgAdapter: FullScreenImageAdapter
    var pressStartTime: Long?  = 0
    var pressedX: Float? = 0F
    var pressedY: Float? = 0F
    var stayedWithinClickDistance: Boolean? = false

    val MAX_CLICK_DURATION = 1000
    val MAX_CLICK_DISTANCE = 15

    var imgPosition = 0

    var product_id = 0
    var seller_phone = ""
    var tmp_member_phone = ""

    var member_id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_detail)

        context = this
        product_id = intent.getIntExtra("product_id",0)
        getProductData()

        //이미지 관련 어댑터
        prodImgAdapter = FullScreenImageAdapter(this@GoodsDetailActivity, _Images)
        pagerVP.adapter = prodImgAdapter
        pagerVP.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                imgPosition = position
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {

                if (_Images != null){
                    for (i in _Images.indices) {
                        if (i == imgPosition) {
                            imageCountTV.text = "$i/${_Images.size}"
                        } else {

                        }
                    }
                }

            }
        })

        /*pagerVP.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {

                    MotionEvent.ACTION_DOWN ->{

                        pressStartTime = System.currentTimeMillis()
                        pressedX = event.x
                        pressedY = event.y
                        stayedWithinClickDistance = true

                        println("OnTouch : ACTION_DOWN")

                        return true
                    }

                    MotionEvent.ACTION_CANCEL->{

                        if (stayedWithinClickDistance!! && distance(pressedX!!, pressedY!!, event.x, event.y) > MAX_CLICK_DISTANCE) {
                            stayedWithinClickDistance = false
                        }
                        return true

                    }

                    MotionEvent.ACTION_UP -> {

                        val pressDuration = System.currentTimeMillis() - pressStartTime!!
                        if (pressDuration < MAX_CLICK_DURATION && stayedWithinClickDistance!!) {
                        }

                        if (intent.hasExtra("id")) {
                            val id = intent.getStringExtra("id")
                            var intent = Intent(context, PictureDetailActivity::class.java)
                            intent.putExtra("id", id)
                            if (_Images != null){
                                intent.putExtra("paths",_Images)
                            }
                            startActivityForResult(intent, PICTURE_DETAIL)

                            return true
                        }

                    }

                }

                return v?.onTouchEvent(event) ?: true
            }
        })*/

        finishLL.setOnClickListener {
            finish()
        }

        moresellerpostTV.setOnClickListener {
            MoveSellerActivity()
        }

        reportTV.setOnClickListener {
            MoveReportActivity(member_id)
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

    fun MoveReportActivity(member_id : Int){

        if (member_id == PrefUtils.getIntPreference(context, "member_id")){
            Toast.makeText(this, "자신의 게시물은 신고하실 수 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            var intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("member_id", member_id)
            startActivity(intent)
        }

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

                    member_id = Utils.getInt(member,"id")

                    val img_uri = Utils.getString(member,"profile_img")
                    val image = Config.url + img_uri
                    ImageLoader.getInstance().displayImage(image, profileImgIV, Utils.UILoptionsProfile)


                    val marketImg = product.getJSONArray("MarketImg")
                    for (i in 0 until marketImg.length()){
                        val data = marketImg[i] as JSONObject
                        _Images.add(Utils.getString(data,"img_uri"))
                    }

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
