package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.actions.RegionAction
import donggolf.android.adapters.*
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_new_add_goods.*
import kotlinx.android.synthetic.main.dlg_market_select_option.view.*
import kotlinx.android.synthetic.main.item_addgoods.view.*
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.ArrayList

class NewAddGoodsActivity : RootActivity() {

    private val SELECT_PICTURE: Int = 101

    var gugunList: ArrayList<JSONObject> = ArrayList<JSONObject>()

    private lateinit var context: Context

    private var addPicturesLL: LinearLayout? = null

    private val imgSeq = 0
    var sk: String? = null
    var pk: String? = null
    var images_path: ArrayList<String>? = null
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null

    private lateinit var categoryAdapter: GoodsCategoryAdapter
    private lateinit var productTypeAdapter: ProductTypeAdaapter
    private lateinit var productCategoryAdatper: ProductCategoryAdapter
    private lateinit var pdtCategoryAdapter: PdtCategoryAdapter
    private lateinit var tradeTypeAdatper: TradeTypeAdapater
    private lateinit var regionAdatper: RegionAdapter
    private lateinit var gugunadapter: DlgGugunAdapter
    private lateinit var adapter: GoodsImageAdapter

    private var categoryData = ArrayList<JSONObject>()
    private var productData = ArrayList<JSONObject>()
    private var productCategoryData = ArrayList<JSONObject>()
    private var pdtCategoryData = ArrayList<JSONObject>()
    private var tradeTypeData = ArrayList<JSONObject>()
    private var regionData = ArrayList<JSONObject>()

    //var category = 1

    //등록을 위해 전송할 데이터
    var prod_type = "" //제품 종류
    var prod_brand = ""
    var prod_form = ""//성향/형태
    var prod_regoin = ""
    var trade_type = ""//거래 방법
    var deliv_way = ""
    var product_id = -1

    var modified_product_id = 0
    var todayCount = 0
    var monthCount = 0

    private var progressDialog: ProgressDialog? = null

    var imageDatas = ArrayList<JSONObject>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_add_goods)

        context = this

        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList()
        images = ArrayList()
        images_url = ArrayList()

        categoryAdapter = GoodsCategoryAdapter(context, R.layout.item_dlg_market_sel_op, categoryData)
        productTypeAdapter = ProductTypeAdaapter(context, R.layout.item_dlg_market_sel_op, productData)
        productCategoryAdatper = ProductCategoryAdapter(context, R.layout.item_dlg_market_sel_op, productCategoryData)
        tradeTypeAdatper = TradeTypeAdapater(context, R.layout.item_dlg_market_sel_op, tradeTypeData)
        regionAdatper = RegionAdapter(context, R.layout.item_dlg_market_sel_op, regionData)
        gugunadapter = DlgGugunAdapter(context, R.layout.item_right_radio_btn_list, gugunList)
        pdtCategoryAdapter = PdtCategoryAdapter(context, R.layout.item_dlg_market_sel_op, pdtCategoryData)


        val gridLayoutManager = GridLayoutManager(context, 1)
        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL)
        imagesRV.layoutManager = gridLayoutManager

        adapter =  GoodsImageAdapter(context, imageDatas)
        imagesRV.adapter = adapter

        getCategory()

        addpictureLL.setOnClickListener {

            permission()

        }

        finishLL.setOnClickListener {
            dlg()
        }

        goodsinfoLL.setOnClickListener {
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
            productData.removeAt(0)
            dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->
                for (i in 0 until productData.size){
                    productData[position].put("isSelectedOp", false)
                }


                var json = productTypeAdapter.getItem(position)
                var type = json.getJSONObject("ProductType")
                val title = Utils.getString(type, "title")
                if (title=="종류전체"){
                    product_id = -1
                }else{
                    product_id = Utils.getInt(type,"id")
                }
                producttypeTV.text = title
                tendencyTV.text = "선택"
                prod_type = title
                productData[position].put("isSelectedOp", true)
                productTypeAdapter.notifyDataSetChanged()
                //println("title ------ $title")
                getCategory()
                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        brandLL.setOnClickListener {

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
                for (i in 0 until categoryData.size){
                    categoryData[position].put("isSelectedOp", false)
                }

                var json = categoryAdapter.getItem(position)
                var type = json.getJSONObject("GoodsCategory")
                val title = Utils.getString(type, "title")
                brandTV.text = title
                prod_brand = title
                categoryData[position].put("isSelectedOp", true)
                categoryAdapter.notifyDataSetChanged()

                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        configRV.setOnClickListener {

            val builder = android.app.AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dlg_market_select_option, null)
            builder.setView(dialogView)
            val alert = builder.show()
            dialogView.dlg_titleTV.text = "형태/성향 선택"
            dialogView.dlg_btn_okTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.visibility = View.VISIBLE
            dialogView.dlg_exitIV.visibility = View.VISIBLE
            dialogView.dlg_titleTV.visibility = View.VISIBLE
            val product = producttypeTV.text.toString()
            dialogView.dlg_marketLV.adapter = productCategoryAdatper

            dialogView.dlg_exitIV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->
                for (i in 0 until productCategoryData.size){
                    productCategoryData[position].put("isSelectedOp", false)
                }
                var json = productCategoryAdatper.getItem(position)
                var type = json.getJSONObject("ProductCategory")
                val title = Utils.getString(type, "title")
                tendencyTV.text = title
                prod_form = title
                productCategoryData[position].put("isSelectedOp", true)
                productCategoryAdatper.notifyDataSetChanged()

                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        areaRL.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dlg_market_select_option, null)
            builder.setView(dialogView)
            val alert = builder.show()
            dialogView.dlg_titleTV.text = "지역 선택"
            dialogView.dlg_btn_okTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.visibility = View.VISIBLE
            dialogView.dlg_exitIV.visibility = View.VISIBLE
            dialogView.dlg_titleTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.adapter = regionAdatper
            dialogView.dlg_exitIV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->

                var position = regionAdatper.getItem(position)
                var type = position.getJSONObject("Region")
                val title = Utils.getString(type, "name")
                val id = Utils.getInt(type, "id")
                if (id != 0){
                    getGugun(id)
                }
                regionTV.text = title
                prod_regoin = title
                alert.dismiss()




            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        dealRL.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dlg_market_select_option, null)
            builder.setView(dialogView)
            val alert = builder.show()
            dialogView.dlg_titleTV.text = "거래 방법"
            dialogView.dlg_btn_okTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.visibility = View.VISIBLE
            dialogView.dlg_exitIV.visibility = View.VISIBLE
            dialogView.dlg_titleTV.visibility = View.VISIBLE
            dialogView.dlg_marketLV.adapter = tradeTypeAdatper
            dialogView.dlg_exitIV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->
                var position = tradeTypeAdatper.getItem(position)
                var type = position.getJSONObject("TradeType")
                val title = Utils.getString(type, "title")
                tradetypeTV.text = title
                trade_type = title
                if (title == "택배거래" || title == "직+택배거래" || title == "안전거래(준비중입니다)") {
                    pay_wayLL.visibility = View.VISIBLE
                } else {
                    pay_wayLL.visibility = View.GONE
                }

                if (title == "안전거래(준비중입니다)"){
                    Toast.makeText(context, "준비중입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnItemClickListener
                }
                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        var phoneNum = PrefUtils.getStringPreference(context, "userPhone")
        sellerPhoneNumTV.text = phoneNum.substring(0, 3) + "-" + phoneNum.substring(3, 7) + "-" + phoneNum.substring(7)

        //등록버튼
        registerLL.setOnClickListener {
            if (modified_product_id != 0){
                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("수정하시겠습니까 ?")

                        .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()

                            modifyProductInform()

                            Utils.hideKeyboard(this)

                        })
                        .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()


                            Utils.hideKeyboard(this)


                        })
                val alert = builder.create()
                alert.show()

            }else {


                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("등록하시겠습니까 ?")

                        .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()

                            register_product()

                            Utils.hideKeyboard(this)

                        })
                        .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()


                            Utils.hideKeyboard(this)


                        })
                val alert = builder.create()
                alert.show()


            }
        }

        modified_product_id = intent.getIntExtra("product_id", 0)
        if (modified_product_id != 0) {
            val params = RequestParams()
            params.put("product_id", modified_product_id)

            MarketAction.get_product_detail(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    //println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val data = response.getJSONObject("product")
                        val market = data.getJSONObject("Market")
                        titleTV.setText(Utils.getString(market, "title"))
                        prod_type = Utils.getString(market, "product_type")
                        producttypeTV.text = prod_type
                        prod_brand = Utils.getString(market, "brand")
                        brandTV.text = prod_brand
                        prod_form = Utils.getString(market, "form")
                        tendencyTV.text = prod_form
                        priceTV.setText(Utils.getString(market, "price"))
                        prod_regoin = Utils.getString(market, "region")
                        regionTV.text = prod_regoin
                        trade_type = Utils.getString(market, "trade_way")
                        tradetypeTV.text = trade_type
                        if (trade_type == "택배거래" || trade_type == "직+택배거래") {
                            pay_wayLL.visibility = View.VISIBLE
                            var tmpPayMtd = Utils.getString(market, "deliv_pay")
                            if (tmpPayMtd.contains("판매자")) {
                                seller_payRB.isSelected = true
                                deliv_way = "판매자 부담"
                            } else {
                                buyer_payRB.isSelected = true
                                deliv_way = "구매자 부담"
                            }
                        } else {
                            pay_wayLL.visibility = View.GONE
                        }
                        descriptionET.setText(Utils.getString(market, "description"))

                        val images = data.getJSONArray("MarketImg")
                        if (images.length() != 0) {

                            for (i in 0 until images.length()) {
                                var tmpImg = images.getJSONObject(i)
                                tmpImg.put("mediaType", 1)

                                imageDatas.add(tmpImg)

                            }

                            adapter.notifyDataSetChanged()

                        }
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

    }

    private fun permission() {

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {

//                var intent = Intent(context, FindPictureGridActivity::class.java)
                var intent = Intent(context, FindPictureActivity::class.java);
                intent.putExtra("image","image")

                startActivityForResult(intent, SELECT_PICTURE)

            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {

            }

        }

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                SELECT_PICTURE -> {

                    var item = data?.getStringArrayExtra("images") //photoPath

                    for (i in 0..(item!!.size - 1)) {

                        val json = JSONObject()
                        json.put("mediaType", 1)
                        json.put("path", item[i])
                        imageDatas.add(json)

                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }


    fun gogundlg(){
        val builder = android.app.AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.dlg_market_select_option, null)
        builder.setView(dialogView)
        val alert = builder.show()
        dialogView.dlg_titleTV.text = "지역 선택"
        dialogView.dlg_btn_okTV.visibility = View.VISIBLE
        dialogView.dlg_marketLV.visibility = View.VISIBLE
        dialogView.dlg_exitIV.visibility = View.VISIBLE
        dialogView.dlg_titleTV.visibility = View.VISIBLE
        dialogView.dlg_marketLV.adapter = gugunadapter
        dialogView.dlg_exitIV.setOnClickListener {
            alert.dismiss()
        }

        dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->
            var title = Utils.getString(regionTV)

            var position = gugunadapter.getItem(position)
            var region = position.getJSONObject("Regions")
            var name: String = Utils.getString(region, "name")
            regionTV.text = title+"/"+name
            prod_regoin =title+"/" + name
            alert.dismiss()
        }

        dialogView.dlg_btn_okTV.setOnClickListener {
            alert.dismiss()
        }
    }

    override fun onBackPressed() {
        dlg()
    }

    fun getGugun(position: Int) {
        if (gugunList != null) {
            gugunList.clear()
        }

        val params = RequestParams()
        params.put("sido", position)

        RegionAction.api_gugun(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                var datalist = response!!.getJSONArray("gugun")

                if (datalist.length() > 0 && datalist != null) {
                    for (i in 0 until datalist.length()) {
                        gugunList.add(datalist.get(i) as JSONObject)
                        gugunList[i].put("isSelectedOp", false)
                    }
                    gugunadapter.notifyDataSetChanged()
                    gogundlg()
                }else{
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                Toast.makeText(context, "불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getCategory() {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("product_id", product_id)

        if (categoryData != null) {
            categoryData.clear()
        }

        if (productData != null) {
            productData.clear()
        }

        if (productCategoryData != null) {
            productCategoryData.clear()
        }

        if (tradeTypeData != null) {
            tradeTypeData.clear()
        }

        if (regionData != null) {
            regionData.clear()
        }

        MarketAction.load_category(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    val category = response.getJSONArray("category")
                    val producttype = response.getJSONArray("producttype")
                    val productcategory = response.getJSONArray("productcategory")
                    val pdtcategory = response.getJSONArray("pdtcategory")
                    val tradetype = response.getJSONArray("tradetype")
                    val region = response.getJSONArray("region")

                    todayCount = Utils.getInt(response, "today")
                    monthCount = Utils.getInt(response, "month")

                    if (producttype.length() > 0 && producttype != null) {
                        for (i in 0 until producttype.length()) {
                            productData.add(producttype.get(i) as JSONObject)
                            productData.get(i).put("isSelectedOp", false)
                        }
                    }

                    if (category.length() > 0 && category != null) {
                        for (i in 0 until category.length()) {
                            var json = category.get(i)as JSONObject
                            Log.d("자이",json.toString())
                            var type = json.getJSONObject("GoodsCategory")
                            val title = Utils.getString(type, "title")
                            if (title != "브랜드전체"){
                                categoryData.add(json)
                            }
                        }
                    }

                    if (productcategory.length() > 0 && productcategory != null) {
                        for (i in 0 until productcategory.length()) {
                            productCategoryData.add(productcategory.get(i) as JSONObject)
                            productCategoryData.get(i).put("isSelectedOp", false)
                        }
                    }


                    if (pdtcategory.length() > 0 && pdtcategory != null) {
                        for (i in 0 until pdtcategory.length()) {
                            pdtCategoryData.add(pdtcategory.get(i) as JSONObject)
                            pdtCategoryData.get(i).put("isSelectedOp", false)
                        }
                    }

                    if (tradetype.length() > 0 && tradetype != null) {
                        for (i in 0 until tradetype.length()) {
                            tradeTypeData.add(tradetype.get(i) as JSONObject)
                            tradeTypeData.get(i).put("isSelectedOp", false)
                        }
                    }

                    if (region.length() > 0 && region != null) {
                        for (i in 0 until region.length()) {
                            regionData.add(region.get(i) as JSONObject)
                            regionData.get(i).put("isSelectedOp", false)
                        }
                    }

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })
    }

    fun register_product() {

        if (todayCount >= 5) {
            Toast.makeText(context, "하루에 5개 이상 등록하실 수 없습니다", Toast.LENGTH_SHORT).show()
        } else if (monthCount >= 40) {
            Toast.makeText(context, "한달에 40개 이상 등록하실 수 없습니다", Toast.LENGTH_SHORT).show()
        } else {
            val title = titleTV.text.toString()
            if (title == "" || title == null){
                Toast.makeText(context, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            if (prod_type == ""){
                Toast.makeText(context, "제품종류는 필수 선택입니다.", Toast.LENGTH_SHORT).show()
                return
            }

            if (prod_brand == ""){
                Toast.makeText(context, "브랜드는 필수 선택입니다.", Toast.LENGTH_SHORT).show()
                return
            }

            val price = priceTV.text.toString()
            if (price == "" || price == null){
                Toast.makeText(context, "가격을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }

            if (prod_form == "" || prod_form == null){
                Toast.makeText(context, "형태/성향은 필수 선택입니다.", Toast.LENGTH_SHORT).show()
                return
            }

            if (prod_regoin == "" || prod_regoin == null){
                Toast.makeText(context, "지역은 필수 선택입니다.", Toast.LENGTH_SHORT).show()
                return
            }

            if (trade_type == "" || trade_type == null){
                Toast.makeText(context, "거래방법은 필수 선택입니다.", Toast.LENGTH_SHORT).show()
                return
            }

            if (images_path!!.size == 0){
                Toast.makeText(context, "상품 사진 등록은 필수 입니다.", Toast.LENGTH_SHORT).show()
                return
            }



            when (delivery_typeRG.checkedRadioButtonId) {
                R.id.seller_payRB -> {
                    deliv_way = "판매자 부담"
                }
                R.id.buyer_payRB -> {
                    deliv_way = "구매자 부담"
                }
            }

            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
            params.put("title", Utils.getString(titleTV))
            params.put("product_type", prod_type)
            params.put("form", prod_form)
            params.put("brand", prod_brand)
            params.put("price", Utils.getString(priceTV))
            params.put("region", prod_regoin)
            params.put("trade_way", trade_type)
            params.put("deliv_pay", deliv_way)
            params.put("phone", Utils.getString(sellerPhoneNumTV))
            params.put("description", Utils.getString(descriptionET))
            params.put("nick", PrefUtils.getStringPreference(context, "nickname"))

            var seq = 0
            if (addPicturesLL != null){
                for (i in 0 until addPicturesLL!!.childCount) {
                    val v = addPicturesLL?.getChildAt(i)
                    val imageIV = v?.findViewById<ImageView>(R.id.addedImgIV)
                    if (imageIV is ImageView) {
                        val bitmap = imageIV.drawable as BitmapDrawable
                        params.put("files[$seq]", ByteArrayInputStream(Utils.getByteArrayFromImageView(imageIV)))
                        seq++
                    }
                }
            }


            //println(params)

            MarketAction.add_market_product(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    if (progressDialog != null) {
                        progressDialog!!.dismiss()
                    }

                    var intent = Intent()
                    intent.action = "GOODS_ADD"
                    sendBroadcast(intent)

                    finish()
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                    if (progressDialog != null) {
                        progressDialog!!.dismiss()
                    }
                    // println(responseString)
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                    // println(errorResponse)
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
            })
        }


    }

    fun dlg(){
        val builder = AlertDialog.Builder(context)
        builder
                .setMessage("글쓰기를 종료할까요 ?")

                .setPositiveButton("나가기", DialogInterface.OnClickListener { dialog, id ->

                    finish()
                    dialog.cancel()
                    Utils.hideKeyboard(this)

                })
                .setNegativeButton("계속쓰기", DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                    Utils.hideKeyboard(this)

                })
        val alert = builder.create()
        alert.show()
    }

    fun modifyProductInform() {
        val params = RequestParams()
        params.put("product_id", modified_product_id)
        params.put("title", Utils.getString(titleTV))
        params.put("product_type", prod_type)
        params.put("form", prod_form)
        params.put("brand", prod_brand)
        params.put("price", Utils.getString(priceTV))
        params.put("region", prod_regoin)
        params.put("trade_way", trade_type)
        params.put("deliv_pay", deliv_way)
        params.put("description", Utils.getString(descriptionET))

        var seq = 0
        if (addPicturesLL != null){
            for (i in 0 until addPicturesLL!!.childCount) {
                val v = addPicturesLL?.getChildAt(i)
                val imageIV = v?.findViewById<ImageView>(R.id.addedImgIV)
                if (imageIV is ImageView) {
                    val bitmap = imageIV.drawable as BitmapDrawable
                    params.put("files[$seq]", ByteArrayInputStream(Utils.getByteArrayFromImageView(imageIV)))
                    seq++
                }
            }
        }


        MarketAction.modify_item_info(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                //println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    var intent = Intent()
                    setResult(RESULT_OK,intent)
                    finish()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
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
        })
    }

}
