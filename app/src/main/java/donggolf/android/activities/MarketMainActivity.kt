package donggolf.android.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.adapters.*
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
    lateinit var genderAdatper: PdtCategoryAdapter

    private  var adapterData = ArrayList<JSONObject>()
    var productData = ArrayList<JSONObject>()//type 종류
    var brandData = ArrayList<JSONObject>() // 브랜드
    var formData = ArrayList<JSONObject>() // 분류
    private var genderData = ArrayList<JSONObject>()

    var values = ""
    var product_type = ""
    var form  = ""
    var brand = ""

    var type = "all"

    var page = 1
    var totalPage = 1
    var todayCount = 0
    var monthCount = 0
    var product_id = -1


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
                getSecondHandMarketItems(type,page)
            }
        }
    }
    internal var pullupReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                page = 1
                getSecondHandMarketItems(type,page)
            }
        }
    }
    internal var deleteReciver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                page = 1
                getSecondHandMarketItems(type,page)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_main)

        context = this
        var filter1 = IntentFilter("GOODS_ADD")
        registerReceiver(reLoadDataReceiver, filter1)
        var filter2 = IntentFilter("DELETE_OK")
        registerReceiver(deleteReciver, filter2)
        var filter3 = IntentFilter("PULL_UP")
        registerReceiver(pullupReceiver, filter3)

        finishmLL.setOnClickListener {
            finish()
        }

        init_menu()
        entireTypeTV.setTextColor(Color.parseColor("#0EDA2F"))



        //목록 가져와서 array에 추가
        productCategoryAdatper = ProductCategoryAdapter(context, R.layout.item_dlg_market_sel_op, formData)
        categoryAdapter = GoodsCategoryAdapter(context, R.layout.item_dlg_market_sel_op, brandData)
        productTypeAdapter = ProductTypeAdaapter(context, R.layout.item_dlg_market_sel_op, productData)
        genderAdatper = PdtCategoryAdapter(context, R.layout.item_dlg_market_sel_op, genderData)


        getSecondHandMarketItems(type,1)

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
            if (PrefUtils.getIntPreference(context, "member_id") == -1){
                Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (todayCount >= 5){
                Toast.makeText(context, "하루에 5개 이상 등록하실 수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if (monthCount >= 40) {
                Toast.makeText(context, "한달에 40개 이상 등록하실 수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
            var product = entireTypeTV.text.toString()
            if (product == "골프백" || product == "골프화" || product == "의류" || product == "모자" || product == "볼" || product == "기타용품" ) {
                dialogView.dlg_marketLV.adapter = genderAdatper
            } else {
                dialogView.dlg_marketLV.adapter = productCategoryAdatper
            }
            dialogView.dlg_exitIV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->
                    var json = productCategoryAdatper.getItem(position)
                    var type2 = json.getJSONObject("ProductCategory")
                    var title = Utils.getString(type2, "title")
                    entireClassificationTV.text = title
                    if (title.equals("분류전체")){
                        title = ""
                    }
                    form = title
                    type = "form"
//                formData[position].put("isSelectedOp", true)
                    getSecondHandMarketItems(type,1)

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
                var type2 = json.getJSONObject("GoodsCategory")
                var title = Utils.getString(type2, "title")
                entireBrandTV.text = title
                if (title.equals("브랜드전체")){
                    title = ""
                }
                brand = title
//                brandData[position].put("isSelectedOp", true)
                categoryAdapter.notifyDataSetChanged()
                type = "brand"
                getSecondHandMarketItems(type,1)

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
                var type2 = json.getJSONObject("ProductType")
                var title = Utils.getString(type2, "title")
                if (title=="종류전체"){
                    product_id = -1
                }else{
                    product_id = Utils.getInt(type2,"id")
                }
                //Log.d("타이틀",title)
                entireTypeTV.text = title
                if (title.equals("종류전체")){
                    title = ""
                }
                product_type = title
                type = "type"
//                productData[position].put("isSelectedOp", true)

                productTypeAdapter.notifyDataSetChanged()
                getSecondHandMarketItems(type,1)
                getCategory()
                alert.dismiss()
                entireClassificationTV.setText("분류 전체")
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        market_mngIV.setOnClickListener {
            if (PrefUtils.getIntPreference(context, "member_id") == -1){
                Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(context,MarketManageActivity::class.java))
        }

        getCategory()

    }

    fun getCategory() {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("product_id", product_id)
        if (brandData != null) {
            brandData.clear()
        }

        if (productData != null) {
            productData.clear()
        }

        if (formData != null) {
            formData.clear()
        }

        if (genderData != null) {
            genderData.clear()
        }

        MarketAction.load_category(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    Log.d("마켓목록",response.toString())
                    val category = response.getJSONArray("category")
                    val productType = response.getJSONArray("producttype")
                    val productForm = response.getJSONArray("productcategory")
                    val pdtcategory = response.getJSONArray("pdtcategory")

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

                    if (pdtcategory.length() > 0 && pdtcategory != null) {
                        for (i in 0 until pdtcategory.length()) {
                            genderData.add(pdtcategory.get(i) as JSONObject)
                            genderData.get(i).put("isSelectedOp", false)
                        }
                    }

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })
    }

    override fun onResume() {
        super.onResume()
        getSecondHandMarketItems(type,1)
        //Log.d("로그",type+page.toString())
    }

    //마켓 목록뽑기
    fun getSecondHandMarketItems(type : String ,page :Int){
        val params = RequestParams()
        params.put("type", type)
        params.put("value", values)
        params.put("page", page)
        params.put("product_type", product_type)
        params.put("form", form)
        params.put("brand", brand)
        params.put("member_id",PrefUtils.getIntPreference(context, "member_id"))

        MarketAction.get_market_product(params,object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                //println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    val marketItems = response.getJSONArray("marketItems")

                    //Log.d("마켓목록",marketItems.toString())
                    this@MarketMainActivity.page = response.getInt("page")
                    totalPage = response.getInt("totalPage")
                    todayCount = response.getInt("todayCount")
                    monthCount = response.getInt("monthCount")

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
                //println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                //println(responseString)
            }
        })
    }

    fun init_menu(){
        entireClassificationTV.setTextColor(Color.parseColor("#000000"))
        entireBrandTV.setTextColor(Color.parseColor("#000000"))
        entireTypeTV.setTextColor(Color.parseColor("#000000"))
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        if (!view.canScrollVertically(-1)) {
            getSecondHandMarketItems("all",1)
        }
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            userScrolled = true
        } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag) {
            userScrolled = false

            //화면이 바닥에 닿았을때
            if (totalPage > page) {
                page++
                lastcount = totalItemCountScroll

                getSecondHandMarketItems("all",page)
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
