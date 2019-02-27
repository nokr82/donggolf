package donggolf.android.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MarketAction
import donggolf.android.adapters.NoticeAdapter
import donggolf.android.adapters.ProductTypeAdaapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.dlg_market_select_option.view.*
import org.json.JSONObject

class NoticeActivity : RootActivity() {

    private lateinit var context: Context

    var member_id = 0

    var noticeData: ArrayList<JSONObject> = ArrayList<JSONObject>()

    private lateinit var noticeAdapter: NoticeAdapter

    private var productData = java.util.ArrayList<JSONObject>()
    private lateinit var productTypeAdapter: ProductTypeAdaapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        context = this

        member_id =  PrefUtils.getIntPreference(context, "member_id")

        noticeAdapter = NoticeAdapter(context, R.layout.item_notice, noticeData)
        productTypeAdapter = ProductTypeAdaapter(context, R.layout.item_dlg_market_sel_op, productData)


        listviewLV.adapter = noticeAdapter

        finishaBT.setOnClickListener {
            finish()
        }

        btn_go_addpost.setOnClickListener {
            addkeyword()
        }

        getkeyword()
        getCategory()

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
                for (i in 0 until productData.size){
                    productData[position].put("isSelectedOp", false)
                }

                var json = productTypeAdapter.getItem(position)
                var type = json.getJSONObject("ProductType")
                val title = Utils.getString(type, "title")
                producttypeTV.text = title
                producttypeTV.text = "선택"
                main_edit_search.setText(title)
//                for (i in 0 until productData.size) {
//                    productData.add(productData.get(i) as JSONObject)
//                    productData.get(i).put("isSelectedOp", false)
//                }

//                productData[position].put("isSelectedOp", true)
//                productTypeAdapter.notifyDataSetChanged()
                alert.dismiss()
            }

            dialogView.dlg_btn_okTV.setOnClickListener {
                alert.dismiss()
            }
        }

    }

    fun addkeyword(){
        val keyword = main_edit_search.text.toString()

        if (keyword == "" || keyword == null){
            Toast.makeText(context, "빈칸은 입력하실 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        var keywordcnt = keywordcntTV.text.toString().toInt()
        if (keywordcnt >= 15){
            Toast.makeText(context, "키워드는 15개 이상 등록하실 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val params = RequestParams()
        params.put("keyword",keyword)
        params.put("member_id", member_id)

        MarketAction.add_keyword(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    val notice = response.getJSONArray("notice")
                    if (noticeData != null){
                        noticeData.clear()
                    }
                    if (notice.length() > 0 && notice != null){
                        for (i in 0 until notice.length()){
                            noticeData.add(notice.get(i) as JSONObject)
                        }
                    }
                    noticeAdapter.notifyDataSetChanged()
                    keywordcntTV.setText(noticeData.size.toString())
                    main_edit_search.setText("")
                } else if (result == "yes"){
                    Toast.makeText(context, "이미 설정된 키워드 입니다.", Toast.LENGTH_SHORT).show()
                    main_edit_search.setText("")
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })

    }

    fun getkeyword(){
        val params = RequestParams()
        params.put("member_id", member_id)

        MarketAction.get_keyword(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    val notice = response.getJSONArray("allNotice")
                    if (noticeData != null){
                        noticeData.clear()
                    }
                    if (notice.length() > 0 && notice != null){
                        for (i in 0 until notice.length()){
                            noticeData.add(notice.get(i) as JSONObject)
                        }
                    }
                    noticeAdapter.notifyDataSetChanged()
                    keywordcntTV.setText(noticeData.size.toString())
                } else if (result == "yes"){
                    Toast.makeText(context, "이미 설정된 키워드 입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })
    }

    fun getCategory() {
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        if (productData != null) {
            productData.clear()
        }

        MarketAction.load_category(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    val producttype = response.getJSONArray("producttype")

                    if (producttype.length() > 0 && producttype != null) {
                        for (i in 0 until producttype.length()) {
                            productData.add(producttype.get(i) as JSONObject)
                            productData.get(i).put("isSelectedOp", false)
                        }
                    }


                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {

            }
        })
    }

}
