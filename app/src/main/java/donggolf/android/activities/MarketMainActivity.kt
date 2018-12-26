package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.adapters.MarketMainAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_market_main.*
import org.json.JSONObject

class MarketMainActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  adapter : MarketMainAdapter

    private  var adapterData = ArrayList<JSONObject>()
    var classifData = ArrayList<JSONObject>()//분류별로
    var assortData = ArrayList<JSONObject>()//종류별로
    var brandData = ArrayList<JSONObject>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_main)

        context = this


        finishmLL.setOnClickListener {
            finish()
        }

        init_menu()
        entireClassificationTV.setTextColor(Color.parseColor("#0EDA2F"))

        //목록 가져와서 array에 추가
        getSecondHandMarketItems("tmpType")
//        var dataObj = JSONObject()
//
//        adapterData.add(dataObj)
//        adapterData.add(dataObj)
//        adapterData.add(dataObj)

        //set adapter
        adapter = MarketMainAdapter(context,R.layout.item_market_main,adapterData)
        maingridGV.adapter = adapter

        maingridGV.setOnItemClickListener { parent, view, position, id ->

            MoveGoodsDetailActivity(position)
        }

        addgoodsTV.setOnClickListener {
            MoveAddGoodsAcitity()
        }

        //분류전체(form)
        entireClassificationTV.setOnClickListener {
            init_menu()
            entireClassificationTV.setTextColor(Color.parseColor("#0EDA2F"))
        }

        //브랜드전체(brand)
        entireBrandTV.setOnClickListener {
            init_menu()
            entireBrandTV.setTextColor(Color.parseColor("#0EDA2F"))
        }

        //종류전체(type)
        entireTypeTV.setOnClickListener {
            init_menu()
            entireTypeTV.setTextColor(Color.parseColor("#0EDA2F"))
        }

    }

    fun getSecondHandMarketItems(type : String){
        val params = RequestParams()
        params.put("type", type)

        MarketAction.get_market_product(params,object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    val allClassif = response.getJSONArray("allClassif")
                    val allType = response.getJSONArray("allType")
                    val allBrand = response.getJSONArray("allBrand")

                    for (i in 0 until allClassif.length()){
                        assortData.add(allClassif[i] as JSONObject)
                        classifData.add(allType[i] as JSONObject)
                        brandData.add(allBrand[i] as JSONObject)
                    }
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }
        })
    }

    fun MoveGoodsDetailActivity(product_id : Int){
        var intent = Intent(this, GoodsDetailActivity::class.java)
        intent.putExtra("product_id", product_id)
        startActivity(intent)
    }

    fun MoveAddGoodsAcitity(){
        var intent = Intent(this, AddGoodsActivity::class.java)
        startActivity(intent)
    }

    fun init_menu(){
        entireClassificationTV.setTextColor(Color.parseColor("#000000"))
        entireBrandTV.setTextColor(Color.parseColor("#000000"))
        entireTypeTV.setTextColor(Color.parseColor("#000000"))
    }
}
