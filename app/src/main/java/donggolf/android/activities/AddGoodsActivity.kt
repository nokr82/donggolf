package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.joooonho.SelectableRoundedImageView
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.adapters.*
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_add_goods.*
import kotlinx.android.synthetic.main.dlg_market_select_option.*
import kotlinx.android.synthetic.main.dlg_market_select_option.view.*
import kotlinx.android.synthetic.main.item_addgoods.view.*
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.ArrayList

class AddGoodsActivity : RootActivity() {

    private val SELECT_PICTURE: Int = 101

    private lateinit var context: Context

    private var addPicturesLL: LinearLayout? = null

    private val imgSeq = 0

    var pk: String? = null
    var images_path: ArrayList<String>? = null
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null

    private lateinit var categoryAdapter: GoodsCategoryAdapter
    private lateinit var productTypeAdapter: ProductTypeAdaapter
    private lateinit var productCategoryAdatper: ProductCategoryAdapter
    private lateinit var tradeTypeAdatper: TradeTypeAdapater
    private lateinit var regionAdatper: RegionAdapter

    private var categoryData = ArrayList<JSONObject>()
    private var productData = ArrayList<JSONObject>()
    private var productCategoryData = ArrayList<JSONObject>()
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

    var modified_product_id = 0

    var todayCount = 0
    var monthCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goods)

        context = this

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList()
        images = ArrayList()
        images_url = ArrayList()

        categoryAdapter = GoodsCategoryAdapter(context, R.layout.item_dlg_market_sel_op, categoryData)
        productTypeAdapter = ProductTypeAdaapter(context, R.layout.item_dlg_market_sel_op, productData)
        productCategoryAdatper = ProductCategoryAdapter(context, R.layout.item_dlg_market_sel_op, productCategoryData)
        tradeTypeAdatper = TradeTypeAdapater(context, R.layout.item_dlg_market_sel_op, tradeTypeData)
        regionAdatper = RegionAdapter(context, R.layout.item_dlg_market_sel_op, regionData)

        getCategory()

        addpictureLL.setOnClickListener {
            var intent = Intent(context, FindPictureGridActivity::class.java)
            startActivityForResult(intent, SELECT_PICTURE)
        }

        finishLL.setOnClickListener {
            finish()
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

            dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->
                var json = productTypeAdapter.getItem(position)
                var type = json.getJSONObject("ProductType")
                val title = Utils.getString(type, "title")
                producttypeTV.text = title
                prod_type = title
                productData[position].put("isSelectedOp", true)
                productTypeAdapter.notifyDataSetChanged()
                //println("title ------ $title")
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
            dialogView.dlg_marketLV.adapter = productCategoryAdatper
            dialogView.dlg_exitIV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.dlg_marketLV.setOnItemClickListener { parent, view, position, id ->
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
                modifyProductInform()
            }else {
                register_product()
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
                                val img_uri = Utils.getString(tmpImg, "img_uri")
                                val image = Config.url + img_uri

                                images_path!!.add(img_uri)

                                var imgView = View.inflate(context, R.layout.item_addgoods, null)
                                ImageLoader.getInstance().displayImage(image, imgView.addedImgIV, Utils.UILoptionsProfile)
                                addPicturesLL?.addView(imgView)
                            }
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

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                SELECT_PICTURE -> {
                    println("onActivityResult로 돌아와서")
                    var item = data?.getStringArrayExtra("images")//photoPath
                    //var name = data?.getStringArrayExtra("displayname")

                    for (i in 0..(item!!.size - 1)) {
                        val str = item[i]

                        images_path!!.add(str)
                        println(str)

                        var add_file = Utils.getImage(context.contentResolver, str)

                        var imageView = View.inflate(context, R.layout.item_addgoods, null)
                        val imageIV: ImageView = imageView.findViewById(R.id.addedImgIV)
                        val delIV: ImageView = imageView.findViewById(R.id.delIV)
                        imageIV.setImageBitmap(add_file)
                        addPicturesLL?.addView(imageView)

                        delIV.setOnClickListener {
                            if (addPicturesLL != null) {
                                addPicturesLL!!.removeView(imageView)
                            }
                        }

//                        if (images?.size == 0) {
//                            images?.add(add_file)
//                        } else {
//                            try {
//                                images?.set(images!!.size, add_file)
//                            } catch (e: IndexOutOfBoundsException) {
//                                images?.add(add_file)
//                            }
//                        }
//                        reset(str, i)
                    }

                    /*val child = addPicturesLL!!.childCount
                    for (i in 0 until child) {

                        val v = addPicturesLL!!.getChildAt(i)

                        val delIV = v.findViewById(R.id.delIV) as ImageView

                    }

                    setResult(RESULT_OK, intent)*/

                    /*if (data != null)
                    {
                        val contentURI = data!!.data
                        Log.d("uri",contentURI.toString())
                        //content://media/external/images/media/1200

                        try
                        {
                            var thumbnail = MediaStore.Images.Media.getBitmap(context.contentResolver, contentURI)
                            //비트맵배열에 비트맵추가
                            images?.add(thumbnail)


                            Log.d("이미지 추가",images.toString())
                            val userView = View.inflate(context, R.layout.item_add_image, null)
                            val imageIV :ImageView = userView.findViewById(R.id.imageIV)
                            val delIV :ImageView = userView.findViewById(R.id.delIV)
                            imageIV.setImageBitmap(thumbnail)
                            addPicturesLL?.addView(userView)
                            //배열사이즈값 -해줘서
                            if (images?.size != 0) {
                                userView.tag = images!!.size - 1
                            }

                            delIV.setOnClickListener {
                                Log.d("태그",userView.tag.toString())
                                if (addPicturesLL != null){
                                    addPicturesLL!!.removeView(userView)
                                }
                            }

                        }
                        catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(context, "바꾸기실패", Toast.LENGTH_SHORT).show()
                        }

                    }*/
                }
            }
        }
    }

    fun reset(str: String, i: Int) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(str, options)
        options.inJustDecodeBounds = false
        options.inSampleSize = 1
        if (options.outWidth > 96) {
            val ws = options.outWidth / 96 + 1
            if (ws > options.inSampleSize) {
                options.inSampleSize = ws
            }
        }
        if (options.outHeight > 96) {
            val hs = options.outHeight / 96 + 1
            if (hs > options.inSampleSize) {
                options.inSampleSize = hs
            }
        }
        val bitmap = BitmapFactory.decodeFile(str)
        val v = View.inflate(context, R.layout.item_add_image, null)
        val imageIV = v.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
        val delIV = v.findViewById<View>(R.id.delIV) as ImageView
        imageIV.setImageBitmap(bitmap)
        delIV.tag = i

        if (imgSeq == 0) {
            addPicturesLL!!.addView(v)
        }

    }

    fun clickMethod(v: View) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                    addPicturesLL!!.removeAllViews()
                    images!!.clear()
                    val tag = v.tag as Int
                    images_path!!.removeAt(tag)

                    for (k in images_url!!.indices) {
                        val vv = View.inflate(context, R.layout.item_add_image, null)
                        val imageIV = vv.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
                        val delIV = vv.findViewById<View>(R.id.delIV) as ImageView
                        delIV.visibility = View.GONE
                        val del2IV = vv.findViewById<View>(R.id.del2IV) as ImageView
                        del2IV.visibility = View.VISIBLE
                        del2IV.tag = k
                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        if (imgSeq == 0) {
                            addPicturesLL!!.addView(vv)
                        }
                    }
                    for (j in images_path!!.indices) {

                        val paths = images_path!!.get(j).split("/")
                        val file_name = paths.get(paths.size - 1)
                        val getPk = file_name.split("_")
                        val pathPk = getPk.get(0)
                        val add_file = Utils.getImage(context.contentResolver, images_path!!.get(j))
                        if (images!!.size == 0) {
                            images!!.add(add_file)
                        } else {
                            try {
                                images!!.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images!!.add(add_file)
                            }
                        }
                        reset(images_path!!.get(j), j)
                    }
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()
    }

    fun clickMethod2(v: View) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                    addPicturesLL!!.removeAllViews()
                    val tag = v.tag as Int
                    images_url!!.removeAt(tag)
                    images_url_remove!!.add(images_id!!.get(tag).toString())
                    images_id!!.removeAt(tag)

                    for (k in images_url!!.indices) {
                        val vv = View.inflate(context, R.layout.item_add_image, null)
                        val imageIV = vv.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
                        val delIV = vv.findViewById<View>(R.id.delIV) as ImageView
                        delIV.visibility = View.GONE
                        val del2IV = vv.findViewById<View>(R.id.del2IV) as ImageView
                        del2IV.visibility = View.VISIBLE
                        del2IV.tag = k
                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        if (imgSeq == 0) {
                            addPicturesLL!!.addView(vv)
                        }
                    }
                    for (j in images_path!!.indices) {

                        val paths = images_path!!.get(j).split("/")
                        val file_name = paths.get(paths.size - 1)
                        val getPk = file_name.split("_")
                        val pathPk = getPk.get(0)
                        val add_file = Utils.getImage(context.contentResolver, images_path!!.get(j))
                        if (images!!.size == 0) {
                            images!!.add(add_file)
                        } else {
                            try {
                                images!!.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images!!.add(add_file)
                            }
                        }
                        reset(images_path!!.get(j), j)
                    }

                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()

    }

    fun getCategory() {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

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
                            categoryData.add(category.get(i) as JSONObject)
                            categoryData[i].put("isSelectedOp", false)
                        }
                    }

                    if (productcategory.length() > 0 && productcategory != null) {
                        for (i in 0 until productcategory.length()) {
                            productCategoryData.add(productcategory.get(i) as JSONObject)
                            productCategoryData.get(i).put("isSelectedOp", false)
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

            if (prod_regoin == "" || prod_regoin == null){
                Toast.makeText(context, "지역은 필수 선택입니다.", Toast.LENGTH_SHORT).show()
                return
            }

            if (trade_type == "" || trade_type == null){
                Toast.makeText(context, "거래방법은 필수 선택입니다.", Toast.LENGTH_SHORT).show()
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
            params.put("nick", PrefUtils.getStringPreference(context, "login_nick"))

            //이미지
//        var seq = 0
//        if (addPicturesLL != null){
//            for (i in 0 until addPicturesLL!!.childCount) {
//                val v = addPicturesLL?.getChildAt(i)
//                val imageIV = v?.findViewById<ImageView>(R.id.addedImgIV)
//                if (imageIV is ImageView) {
//                    val bitmap = imageIV.drawable as BitmapDrawable
//                    params.put("files[$seq]", ByteArrayInputStream(Utils.getByteArray(bitmap.bitmap)))
//                    seq++
//                }
//            }
//        }

            if (images_path != null) {
                if (images_path!!.size != 0) {
                    for (i in 0..images_path!!.size - 1) {

                        var bt: Bitmap = Utils.getImage(context.contentResolver, images_path!!.get(i))

                        params.put("files[$i]", ByteArrayInputStream(Utils.getByteArray(bt)))

                    }
                }
            }

            //println(params)

            MarketAction.add_market_product(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        //Utils.alert(context,"상품이 성공적으로 등록되었습니다.")
                        finish()
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
                    params.put("files[$seq]", ByteArrayInputStream(Utils.getByteArray(bitmap.bitmap)))
                    seq++
                }
            }
        }

        /*if (images_path != null) {
            if (images_path!!.size != 0) {
                for (i in 0 until images_path!!.size) {
                    *//*if (images_path!!.get(i).substring(2, 6) == "data") {
                        continue
                    } else {*//*
                        var bt: Bitmap = Utils.getImage(context.contentResolver, images_path!!.get(i))

                        params.put("files[$i]", ByteArrayInputStream(Utils.getByteArray(bt)))
                    //}
                }
            }
        }*/

        MarketAction.modify_item_info(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                //println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    var intent = Intent()
                    setResult(RESULT_OK,intent)
                    finish()
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
