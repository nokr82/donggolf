package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.ArrayAdapter
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.R.id.*
import donggolf.android.actions.MarketAction
import donggolf.android.adapters.*
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_seller.*
import kotlinx.android.synthetic.main.dlg_market_select_option.view.*
import kotlinx.android.synthetic.main.dlg_simple_radio_option.view.*
import org.json.JSONObject

class SellerActivity : RootActivity() {

    private lateinit var context: Context

    private lateinit var adapter: SellerAdapter
    lateinit var productTypeAdapter: ProductTypeAdaapter
    lateinit var productCategoryAdatper: ProductCategoryAdapter
    lateinit var categoryAdapter: GoodsCategoryAdapter//brand
    lateinit var statAdapter: ArrayAdapter<String>


    private var adapterData = ArrayList<JSONObject>()

    var seller_id = 0
    var type = ""

    var values = ""
    var product_type = ""
    var stat = ""
    var form = ""
    var brand = ""

    var productData = ArrayList<JSONObject>()//type 종류
    var brandData = ArrayList<JSONObject>() // 브랜드
    var formData = ArrayList<JSONObject>() // 분류
    var statData = arrayOf("전체보기", "판매예약", "판매중", "거래중", "판매보류", "판매완료")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        context = this

        if (intent.getIntExtra("seller_id", 0) != null) {
            seller_id = intent.getIntExtra("seller_id", 0)
        }

        if (intent.getStringExtra("type") != null) {
            type = intent.getStringExtra("type")
        }

        finishaLL.setOnClickListener {
            finish()
        }

        //목록 가져와서 array에 추가
        productCategoryAdatper = ProductCategoryAdapter(context, R.layout.item_dlg_market_sel_op, formData)
        categoryAdapter = GoodsCategoryAdapter(context, R.layout.item_dlg_market_sel_op, brandData)
        productTypeAdapter = ProductTypeAdaapter(context, R.layout.item_dlg_market_sel_op, productData)
        statAdapter = ArrayAdapter(context, R.layout.item_dlg_market_sel_op, statData)

        adapter = SellerAdapter(context, R.layout.item_market_main, adapterData)

        gridGV.adapter = adapter

        //분류전체
        sel_formTV.setOnClickListener {
            init_menu()
            sel_formTV.setTextColor(Color.parseColor("#0EDA2F"))

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
                var title = Utils.getString(type, "title")
                sel_formTV.text = title
                if (title.equals("분류전체")) {
                    title = ""
                }
                form = title
//                formData[position].put("isSelectedOp", true)
                get_my_market_item()



                productCategoryAdatper.notifyDataSetChanged()

                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }


        }

        //브랜드전체
        sel_brandTV.setOnClickListener {
            init_menu()
            sel_brandTV.setTextColor(Color.parseColor("#0EDA2F"))

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
                var title = Utils.getString(type, "title")
                sel_brandTV.text = title
                if (title.equals("브랜드전체")) {
                    title = ""
                }
                brand = title
//                formData[position].put("isSelectedOp", true)
                get_my_market_item()
                categoryAdapter.notifyDataSetChanged()

                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        //종류 전체
        sel_typeTV.setOnClickListener {
            init_menu()
            sel_typeTV.setTextColor(Color.parseColor("#0EDA2F"))

            val builder = android.app.AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dlg_market_select_option, null)
            builder.setView(dialogView)
            val alert = builder.show()
            dialogView.dlg_titleTV.text = "종류 선택"
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
                var title = Utils.getString(type, "title")
                sel_typeTV.text = title
                if (title.equals("종류전체")) {
                    title = ""
                }
                product_type = title
//                formData[position].put("isSelectedOp", true)
                get_my_market_item()



                productTypeAdapter.notifyDataSetChanged()

                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        //종류 전체
        sel_statusTV.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_simple_radio_option, null)
            builder.setView(dialogView)
            val alert = builder.show()
            dialogView.dlg_sale_allLL.visibility = View.VISIBLE
            when(stat){
                "전체보기"-> dialogView.dlg_sale_bookIV.setImageResource(R.mipmap.btn_radio_on)
                "판매예약"-> dialogView.dlg_sale_bookIV.setImageResource(R.mipmap.btn_radio_on)
                "판매중"-> dialogView.dlg_saleIV.setImageResource(R.mipmap.btn_radio_on)
                "거래중"-> dialogView.dlg_in_dealIV.setImageResource(R.mipmap.btn_radio_on)
                "판매보류"-> dialogView.dlg_holdIV.setImageResource(R.mipmap.btn_radio_on)
                "판매완료"-> dialogView.dlg_completeIV.setImageResource(R.mipmap.btn_radio_on)
            }

            dialogView.dlg_sale_allLL.setOnClickListener{
                stat = ""
                alert.dismiss()
                sel_statusTV.text = "전체보기"
                get_my_market_item()
            }
            dialogView.dlg_sale_bookLL.setOnClickListener {
                stat = "판매예약"
                alert.dismiss()
                sel_statusTV.text = stat
                get_my_market_item()
            }
            dialogView.dlg_saleLL.setOnClickListener {
                stat = "판매중"
                alert.dismiss()
                sel_statusTV.text = stat
                get_my_market_item()
            }
            dialogView.dlg_in_dealLL.setOnClickListener {
                stat = "거래중"
                alert.dismiss()
                sel_statusTV.text = stat
                get_my_market_item()
            }
            dialogView.dlg_holdLL.setOnClickListener {
                stat = "판매보류"
                alert.dismiss()
                sel_statusTV.text = stat
                get_my_market_item()
            }
            dialogView.dlg_completeLL.setOnClickListener {
                stat = "판매완료"
                alert.dismiss()
                sel_statusTV.text = stat
                get_my_market_item()
            }
        }



        gridGV.setOnItemClickListener { parent, view, position, id ->
            val product_id = adapterData[position].getInt("prodId")
            MoveGoodsDetailActivity(product_id)
        }
        getCategory()
        get_my_market_item()
    }
    fun init_menu(){
        sel_formTV.setTextColor(Color.parseColor("#000000"))
        sel_brandTV.setTextColor(Color.parseColor("#000000"))
        sel_typeTV.setTextColor(Color.parseColor("#000000"))
        sel_statusTV.setTextColor(Color.parseColor("#000000"))
    }
    fun get_my_market_item(){
        val params = RequestParams()
        params.put("product_type", product_type)
        params.put("form", form)
        params.put("brand", brand)
        params.put("stat", stat)
        if (type == "seller"){
            params.put("member_id", seller_id)
        } else {
            params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        }

        MarketAction.get_my_market_item(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    adapterData.clear()
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

    fun MoveGoodsDetailActivity(product_id : Int){
        var intent = Intent(this, GoodsDetailActivity::class.java)
        intent.putExtra("product_id", product_id)
        startActivity(intent)
    }


}
