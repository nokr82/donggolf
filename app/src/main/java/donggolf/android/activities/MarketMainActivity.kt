package donggolf.android.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.adapters.GoodsCategoryAdapter
import donggolf.android.adapters.MarketMainAdapter
import donggolf.android.adapters.ProductCategoryAdapter
import donggolf.android.adapters.ProductTypeAdaapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_market_main.*
import kotlinx.android.synthetic.main.dlg_market_select_option.view.*
import org.json.JSONObject

class MarketMainActivity : RootActivity(), AbsListView.OnScrollListener {

    private lateinit var context: Context

    private  lateinit var  adapter : MarketMainAdapter
    lateinit var productTypeAdapter: ProductTypeAdaapter
    lateinit var productCategoryAdatper: ProductCategoryAdapter
    lateinit var categoryAdapter: GoodsCategoryAdapter//brand

    private  var adapterData = ArrayList<JSONObject>()
    var productData = ArrayList<JSONObject>()//type 종류
    var brandData = ArrayList<JSONObject>() // 브랜드
    var formData = ArrayList<JSONObject>() // 분류

    var values = ""

    var page = 1
    var totalPage = 1
    private var userScrolled = false
    private var lastItemVisibleFlag = false
    private var totalItemCountScroll = 0
    private val itemCount = 0
    private val totalItemCount = 0
    private var lastcount = 0
    private val visibleThreshold = 10

    internal var reLoadDataReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                page = 1
                getSecondHandMarketItems("all")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_main)

        context = this

        var filter1 = IntentFilter("GOODS_ADD")
        registerReceiver(reLoadDataReceiver, filter1)

        finishmLL.setOnClickListener {
            finish()
        }

        init_menu()
        entireClassificationTV.setTextColor(Color.parseColor("#0EDA2F"))

        //목록 가져와서 array에 추가
        productCategoryAdatper = ProductCategoryAdapter(context, R.layout.item_dlg_market_sel_op, formData)
        categoryAdapter = GoodsCategoryAdapter(context, R.layout.item_dlg_market_sel_op, brandData)
        productTypeAdapter = ProductTypeAdaapter(context, R.layout.item_dlg_market_sel_op, productData)

        getSecondHandMarketItems("all")

        //set adapter
        adapter = MarketMainAdapter(context,R.layout.item_market_main,adapterData)
        maingridGV.adapter = adapter
        maingridGV.setOnScrollListener(this)
        maingridGV.setOnItemClickListener { parent, view, position, id ->
            val product_id = adapterData[position].getInt("prodId")
            var intent = Intent(this, GoodsDetailActivity::class.java)
            intent.putExtra("product_id", product_id)
            startActivity(intent)
        }

        addgoodsTV.setOnClickListener {
            var intent = Intent(this, AddGoodsActivity::class.java)
            startActivity(intent)
        }

        //분류전체(form)
        entireClassificationTV.setOnClickListener {
            init_menu()
            entireClassificationTV.setTextColor(Color.parseColor("#0EDA2F"))

            val builder = android.app.AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dlg_market_select_option, null)
            builder.setView(dialogView)
            val alert = builder.show()
            dialogView.dlg_titleTV.text = "형태/성향 선택"
            dialogView.dlg_btn_okTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.visibility = View.VISIBLE
            dialogView.dlg_exitIV.visibility = View.VISIBLE
            dialogView.dlg_titleTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.adapter = productCategoryAdatper
            dialogView.dlg_exitIV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->
                var json = productCategoryAdatper.getItem(position)
                var type = json.getJSONObject("ProductCategory")
                val title = Utils.getString(type, "title")
                entireClassificationTV.text = title
                values = title
                formData[position].put("isSelectedOp", true)
                productCategoryAdatper.notifyDataSetChanged()

                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }


        }

        //브랜드전체(brand)
        entireBrandTV.setOnClickListener {
            init_menu()
            entireBrandTV.setTextColor(Color.parseColor("#0EDA2F"))

            val builder = android.app.AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dlg_market_select_option, null)
            builder.setView(dialogView)
            val alert = builder.show()
            dialogView.dlg_titleTV.text = "브랜드 선택"
            dialogView.dlg_btn_okTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.visibility = View.VISIBLE
            dialogView.dlg_exitIV.visibility = View.VISIBLE
            dialogView.dlg_titleTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.adapter = categoryAdapter
            dialogView.dlg_exitIV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->
                var json = categoryAdapter.getItem(position)
                var type = json.getJSONObject("GoodsCategory")
                val title = Utils.getString(type, "title")
                entireBrandTV.text = title
                values = title
                brandData[position].put("isSelectedOp", true)
                categoryAdapter.notifyDataSetChanged()

                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }


        }

        //종류전체(type)
        entireTypeTV.setOnClickListener {
            init_menu()
            entireTypeTV.setTextColor(Color.parseColor("#0EDA2F"))

            val builder = android.app.AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dlg_market_select_option, null)
            builder.setView(dialogView)
            val alert = builder.show()
            dialogView.dlg_titleTV.text = "제품 종류"
            dialogView.dlg_btn_okTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.visibility = View.VISIBLE
            dialogView.dlg_exitIV.visibility = View.VISIBLE
            dialogView.dlg_titleTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.adapter = productTypeAdapter
            dialogView.dlg_exitIV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->
                var json = productTypeAdapter.getItem(position)
                var type = json.getJSONObject("ProductType")
                val title = Utils.getString(type, "title")
                entireTypeTV.text = title
                values = title
                productData[position].put("isSelectedOp", true)
                productTypeAdapter.notifyDataSetChanged()

                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        market_mngIV.setOnClickListener {
            startActivity(Intent(context,MarketManageActivity::class.java))
        }

        getCategory()

    }

    fun getCategory() {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        if (brandData != null) {
            brandData.clear()
        }

        if (productData != null) {
            productData.clear()
        }

        if (formData != null) {
            formData.clear()
        }

        MarketAction.load_category(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    val category = response.getJSONArray("category")
                    val productType = response.getJSONArray("producttype")
                    val productForm = response.getJSONArray("productcategory")

                    if (productType.length() > 0 && productType != null) {
                        for (i in 0 until productType.length()) {
                            productData.add(productType.get(i) as JSONObject)
                            productData.get(i).put("isSelectedOp", false)
                        }
                    }

                    if (category.length() > 0 && category != null) {
                        for (i in 0 until category.length()) {
                            brandData.add(category.get(i) as JSONObject)
                            brandData[i].put("isSelectedOp", false)
                        }
                    }

                    if (productForm.length() > 0 && productForm != null) {
                        for (i in 0 until productForm.length()) {
                            formData.add(productForm.get(i) as JSONObject)
                            formData.get(i).put("isSelectedOp", false)
                        }
                    }

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })
    }

    fun getSecondHandMarketItems(type : String){
        val params = RequestParams()
        params.put("type", type)
        params.put("value", values)
        params.put("page", page)

        MarketAction.get_market_product(params,object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    val marketItems = response.getJSONArray("marketItems")

                    page = response.getInt("page")
                    totalPage = response.getInt("totalPage")

                    if(page == 1) {
                        adapterData.clear()
                    }

                    for (i in 0 until marketItems.length()){
                        adapterData.add(marketItems[i] as JSONObject)
                    }

                    adapter.notifyDataSetChanged()
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

    fun init_menu(){
        entireClassificationTV.setTextColor(Color.parseColor("#000000"))
        entireBrandTV.setTextColor(Color.parseColor("#000000"))
        entireTypeTV.setTextColor(Color.parseColor("#000000"))
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            userScrolled = true
        } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag) {
            userScrolled = false

            //화면이 바닥에 닿았을때
            if (totalPage > page) {
                page++
                lastcount = totalItemCountScroll

                getSecondHandMarketItems("all")
            }
        }
    }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (userScrolled && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold && itemCount < this.totalItemCount && this.totalItemCount > 0) {
            if (totalPage > page) {
            }
        }

        //현재 화면에 보이는 첫번째 리스트 아이템의 번호(firstVisibleItem)
        // + 현재 화면에 보이는 리스트 아이템의갯수(visibleItemCount)가
        // 리스트 전체의 갯수(totalOtemCount)-1 보다 크거나 같을때
        lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount
        totalItemCountScroll = totalItemCount
    }
}
