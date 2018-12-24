package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.joooonho.SelectableRoundedImageView
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.adapters.*
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_add_goods.*
import kotlinx.android.synthetic.main.dlg_market_select_option.*
import kotlinx.android.synthetic.main.dlg_market_select_option.view.*
import org.json.JSONObject
import java.util.ArrayList

class AddGoodsActivity : RootActivity() {

    private val SELECT_PICTURE: Int = 101

    private lateinit var context: Context

    private var addPicturesLL: LinearLayout? = null

    private val imgSeq = 0

    var pk : String? = null
    var images_path: ArrayList<String>? = null
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null

    private  lateinit var  categoryAdapter : GoodsCategoryAdapter
    private  lateinit var  productTypeAdapter: ProductTypeAdaapter
    private lateinit var productCategoryAdatper : ProductCategoryAdapter
    private lateinit var tradeTypeAdatper : TradeTypeAdapater
    private lateinit var regionAdatper : RegionAdapter

    private  var categoryData : ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  var productData : ArrayList<JSONObject> = ArrayList<JSONObject>()
    private var productCategoryData : ArrayList<JSONObject> = ArrayList<JSONObject>()
    private var tradeTypeData :  ArrayList<JSONObject> = ArrayList<JSONObject>()
    private var regionData :  ArrayList<JSONObject> = ArrayList<JSONObject>()
    var category = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goods)

        context = this

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList()
        images = ArrayList()
        images_url = ArrayList()

        categoryAdapter = GoodsCategoryAdapter(context, R.layout.item_dlg_market_sel_op,categoryData)
        productTypeAdapter = ProductTypeAdaapter(context,R.layout.item_dlg_market_sel_op,productData)
        productCategoryAdatper = ProductCategoryAdapter(context,R.layout.item_dlg_market_sel_op,productCategoryData)
        tradeTypeAdatper = TradeTypeAdapater(context,R.layout.item_dlg_market_sel_op,tradeTypeData)
        regionAdatper = RegionAdapter(context,R.layout.item_dlg_market_sel_op,regionData)

        getCategory()

        addpictureLL.setOnClickListener {
            var intent = Intent(context, FindPictureGridActivity::class.java)
            startActivityForResult(intent, SELECT_PICTURE)
        }

        finishLL.setOnClickListener {
            finish()
        }

        finishaLL.setOnClickListener {
            choiceLL.visibility = View.GONE
        }

        selectedTV.setOnClickListener {
            choiceLL.visibility = View.GONE
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
                var position = productTypeAdapter.getItem(position)
                var type = position.getJSONObject("ProductType")
                val title = Utils.getString(type,"title")
                producttypeTV.setText(title)
                println("title ------ $title")
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

            dialogView.dlg_marketLV.setOnItemClickListener{ parent, view, position, id ->

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

            dialogView.dlg_marketLV.setOnItemClickListener{ parent, view, position, id ->
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
                val title = Utils.getString(type,"name")
                regionTV.setText(title)
                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        dellRL.setOnClickListener {
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
                val title = Utils.getString(type,"title")
                tradetypeTV.setText(title)
                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

        listviewLV.setOnItemClickListener { parent, view, position, id ->
            val data = categoryData.get(position)

        }

        var phoneNum = PrefUtils.getStringPreference(context,"userPhone")
        sellerPhoneNumTV.text = phoneNum.substring(0,3) + "-" + phoneNum.substring(3,7) + "-" + phoneNum.substring(7)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                SELECT_PICTURE -> {
                    var item = data?.getStringArrayExtra("images")
                    var name = data?.getStringArrayExtra("displayname")

                    for (i in 0..(item!!.size - 1)) {
                        val str = item[i]

                        images_path!!.add(str)

                        val add_file = Utils.getImage(context.contentResolver, str)

                        if (images?.size == 0) {

                            images?.add(add_file)

                        } else {
                            try {
                                images?.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images?.add(add_file)
                            }

                        }

                        reset(str, i)

                    }

                    val child = addPicturesLL!!.getChildCount()
                    for (i in 0 until child) {

                        val v = addPicturesLL!!.getChildAt(i)

                        val delIV = v.findViewById(R.id.delIV) as ImageView

                    }

                    setResult(RESULT_OK, intent)

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
                        val add_file = Utils.getImage(context!!.getContentResolver(), images_path!!.get(j))
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
                        val add_file = Utils.getImage(context!!.getContentResolver(), images_path!!.get(j))
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
    fun getCategory(){
        val params = RequestParams()
        params.put("all",1)

        if (categoryData != null){
            categoryData.clear()
        }

        if (productData != null){
            productData.clear()
        }

        if (productCategoryData != null){
            productCategoryData.clear()
        }

        if (tradeTypeData != null){
            tradeTypeData.clear()
        }

        if (regionData != null){
            regionData.clear()
        }

        MarketAction.load_category(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok"){
                    val category = response.getJSONArray("category")
                    val producttype = response.getJSONArray("producttype")
                    val productcategory = response.getJSONArray("productcategory")
                    val tradetype = response.getJSONArray("tradetype")
                    val region = response.getJSONArray("region")

                    if (category.length() > 0 && category != null){
                        for (i in 0 until category.length()){
                            categoryData.add(category.get(i) as JSONObject)
                        }
                    }

                    if (producttype.length() > 0 && producttype != null){
                        for (i in 0 until producttype.length()){
                            productData.add(producttype.get(i) as JSONObject)
                        }
                    }

                    if (productcategory.length() > 0 && productcategory != null){
                        for (i in 0 until productcategory.length()){
                            productCategoryData.add(productcategory.get(i) as JSONObject)
                        }
                    }

                    if (tradetype.length() > 0 && tradetype != null){
                        for (i in 0 until tradetype.length()){
                            tradeTypeData.add(tradetype.get(i) as JSONObject)
                        }
                    }

                    if (region.length() > 0 && region != null){
                        for (i in 0 until region.length()){
                            regionData.add(region.get(i) as JSONObject)
                        }
                    }

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })
    }



}
