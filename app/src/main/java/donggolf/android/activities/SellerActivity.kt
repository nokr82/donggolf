package donggolf.android.activities

import android.app.AlertDialog
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
import donggolf.android.adapters.MarketMainAdapter
import donggolf.android.adapters.SellerAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_market_manage.*
import kotlinx.android.synthetic.main.activity_seller.*
import kotlinx.android.synthetic.main.dlg_comment_menu.view.*
import org.json.JSONObject

class SellerActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  adapter : SellerAdapter

    private  var adapterData = ArrayList<JSONObject>()

    var seller_id = 0
    var type = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        context = this

        if (intent.getIntExtra("seller_id",0) != null){
            seller_id = intent.getIntExtra("seller_id",0)
        }

        if (intent.getStringExtra("type") != null){
            type = intent.getStringExtra("type")
        }

        finishaLL.setOnClickListener {
            finish()
        }

        adapter = SellerAdapter(context,R.layout.item_seller,adapterData)

        gridGV.adapter = adapter

        //분류전체
        sel_formTV.setOnClickListener {

        }

        //브랜드전체
        sel_brandTV.setOnClickListener {

        }

        //종류 전체
        sel_typeTV.setOnClickListener {

        }

        //판매상태 전체
        sel_statusTV.setOnClickListener {

        }

        gridGV.setOnItemClickListener { parent, view, position, id ->
            val product_id = adapterData[position].getInt("prodId")
            MoveGoodsDetailActivity(product_id)
        }

        get_my_market_item()
    }

    fun get_my_market_item(){
        val params = RequestParams()
        if (type == "seller"){
            params.put("member_id", seller_id)
        } else {
            params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        }

        MarketAction.get_my_market_item(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    val dataList = response.getJSONArray("allContent")
                    if (dataList.length() > 0 && dataList != null){
                        for (i in 0 until dataList.length()){
                            adapterData.add(dataList.get(i) as JSONObject)
                        }
                    }
                    adapter.notifyDataSetChanged()

                    if (type == "my"){
                        titleTV.setText("내가 올린글(" +adapterData.size.toString() + ")")
                    }
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })

    }

    fun MoveGoodsDetailActivity(product_id : Int){
        var intent = Intent(this, GoodsDetailActivity::class.java)
        intent.putExtra("product_id", product_id)
        startActivity(intent)
    }


}
