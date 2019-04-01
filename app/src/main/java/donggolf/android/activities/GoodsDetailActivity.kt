package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.CommentAction
import donggolf.android.actions.CommentAction.write_comments
import donggolf.android.actions.MarketAction
import donggolf.android.adapters.FullScreenImageAdapter
import donggolf.android.adapters.GoodsComAdapter
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_goods_detail.*
import kotlinx.android.synthetic.main.dlg_comment_menu.view.*
import kotlinx.android.synthetic.main.dlg_simple_radio_option.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class GoodsDetailActivity : RootActivity() {

    private lateinit var context: Context

    var _Images = ArrayList<String>()
    private lateinit var prodImgAdapter: FullScreenImageAdapter
    var pressStartTime: Long?  = 0
    var stayedWithinClickDistance: Boolean? = false

    val MAX_CLICK_DURATION = 1000

    var imgPosition = 0
    val PRODUCT_MODIFY = 112
    var REPORT_OK = 113

    var product_id = 0
    var seller_phone = ""
    var seller_id = 0
    var tmp_prod_status = ""
    var member_id = 0

    var commentType = ""
    var commentParent = ""
    var seller_id2 = ""
    var blockYN = ""

    var x = 0.0f
    var adPosition = 0

    private  lateinit var  commentAdapter : GoodsComAdapter
    private  var commentList:ArrayList<JSONObject> = ArrayList<JSONObject>()

    var brand = ""
    var product_type = ""

    var PICTURE_DETAIL = 1

    var op_comments_id = -1
    var p_comments_id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_detail)

        context = this
        product_id = intent.getIntExtra("product_id",0)

        commentAdapter = GoodsComAdapter(context,R.layout.main_detail_listview_item,commentList)

        market_commentLV.adapter = commentAdapter
        market_commentLV.isExpanded = true

        getProductData()
        getcomment()

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
                    pagerVP.visibility = View.VISIBLE
                    for (i in _Images.indices) {
                        if (i == imgPosition) {
                            imageCountTV.text = "${i+1}/${_Images.size}"
                        }
                    }
                } else {
                    pagerVP.visibility = View.GONE
                    imageCountTV.text = "이미지가 없습니다."
                }

            }
        })

        pagerVP.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                when (event?.action) {
                    MotionEvent.ACTION_DOWN ->{
                        x = event.x
                    }

                    MotionEvent.ACTION_CANCEL->{

                    }

                    MotionEvent.ACTION_UP -> {
                        var difference = 0
                        if (x >= event.x){
                            difference = x.toInt() - event.x.toInt()
                        } else {
                            difference = event.x.toInt() - x.toInt()
                        }
                        if (difference < 10){
                            val pressDuration = System.currentTimeMillis() - pressStartTime!!
                            if (pressDuration < MAX_CLICK_DURATION && stayedWithinClickDistance!!) {
                            }

                            val id = "IMAGE"
                            var intent = Intent(context, PictureDetailActivity::class.java)
                            intent.putExtra("id", id)
                            intent.putExtra("adPosition",adPosition)
                            if (_Images != null){
                                intent.putExtra("paths",_Images)
                            }
                            startActivityForResult(intent, PICTURE_DETAIL)
                        }

                    }

                }

                return v?.onTouchEvent(event) ?: true
            }
        })



        show_mng_dlgLL.setOnClickListener {
            popupDialogView()

        }

        finish_goods_dtlLL.setOnClickListener {
            finish()
        }

        moresellerpostTV.setOnClickListener {
            MoveSellerActivity()
        }

        reportTV.setOnClickListener {
            MoveReportActivity(member_id)
        }

        addcommentTV.setOnClickListener {
            addcomment()
        }

        change_prod_stateLL.setOnClickListener {
            if (PrefUtils.getIntPreference(context,"member_id").toString()==seller_id2){
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_simple_radio_option, null)
            builder.setView(dialogView)
            val alert = builder.show()

            when(tmp_prod_status){
                "판매예약"-> dialogView.dlg_sale_bookIV.setImageResource(R.mipmap.btn_radio_on)
                "판매중"-> dialogView.dlg_saleIV.setImageResource(R.mipmap.btn_radio_on)
                "거래중"-> dialogView.dlg_in_dealIV.setImageResource(R.mipmap.btn_radio_on)
                "판매보류"-> dialogView.dlg_holdIV.setImageResource(R.mipmap.btn_radio_on)
                "판매완료"-> dialogView.dlg_completeIV.setImageResource(R.mipmap.btn_radio_on)
            }

            dialogView.dlg_sale_bookLL.setOnClickListener {
                if (tmp_prod_status == "판매완료"){
                    Toast.makeText(context, "판매완료가 된글은 변경하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                tmp_prod_status = "판매예약"
                alert.dismiss()
                updateProductStatus()

            }
            dialogView.dlg_saleLL.setOnClickListener {
                if (tmp_prod_status == "판매완료"){
                    Toast.makeText(context, "판매완료가 된글은 변경하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                tmp_prod_status = "판매중"
                alert.dismiss()
                updateProductStatus()
            }
            dialogView.dlg_in_dealLL.setOnClickListener {
                if (tmp_prod_status == "판매완료"){
                    Toast.makeText(context, "판매완료가 된글은 변경하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                tmp_prod_status = "거래중"
                alert.dismiss()
                updateProductStatus()
            }
            dialogView.dlg_holdLL.setOnClickListener {
                if (tmp_prod_status == "판매완료"){
                    Toast.makeText(context, "판매완료가 된글은 변경하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                tmp_prod_status = "판매보류"
                alert.dismiss()
                updateProductStatus()
            }
            dialogView.dlg_completeLL.setOnClickListener {

                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("판매완료 처리하면 수정할 수 없습니다.")

                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                            tmp_prod_status = "판매완료"
                            alert.dismiss()
                            updateProductStatus()
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                        })
                val alert = builder.create()
                alert.show()
            }
            }else{
                Toast.makeText(context,"다른사람의 판매글입니다.",Toast.LENGTH_SHORT).show()
            }
        }

        contact_sellerLL.setOnClickListener {

            if (seller_id == PrefUtils.getIntPreference(context,"member_id")) {
                Toast.makeText(context,"자신의 게시물에는 전송하실 수 없습니다.",Toast.LENGTH_SHORT).show()
            } else {

                var status = sale_statusTV.text.toString()
                if (status == "판매중") {
                    val permissionlistener = object : PermissionListener {
                        override fun onPermissionGranted() {
                            var myPhoneNum = PrefUtils.getStringPreference(context, "userPhone")

                            val text = "[동네골프] $brand > $product_type 판매 게시글보고 연락드립니다."
//                    try{
//                        SmsManager.getDefault().sendTextMessage(phone,null, text, null, null)
//                        Toast.makeText(context,"문자 전송 완료",Toast.LENGTH_SHORT).show()
//                    }catch (e:Exception){
//                        e.printStackTrace()
//                    }

                            var intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + seller_phone))
                            intent.putExtra("sms_body", text)
                            startActivity(intent)

                        }

                        override fun onPermissionDenied(deniedPermissions: List<String>) {
                        }

                    }

                    TedPermission.with(this)
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                            .setPermissions(
                                    android.Manifest.permission.READ_PHONE_STATE,
                                    android.Manifest.permission.SEND_SMS,
                                    android.Manifest.permission.RECEIVE_SMS
                            )
                            .check();

                } else {
                    Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

        }

        findET.setFocusableInTouchMode(false);
        findET.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://cyberbureau.police.go.kr/mobile/sub/sub_02.jsp")))
        }
        findLL.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://cyberbureau.police.go.kr/mobile/sub/sub_02.jsp")))
        }


        getLooker()

        profileImgIV.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("member_id", seller_id2)
            startActivity(intent)
        }

        market_commentLV.setOnItemLongClickListener { parent, view, position, id ->
            var commenter = commentList[position].getInt("cmt_wrt_id")

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_comment_menu, null) //사용자 정의 다이얼로그 xml 붙이기
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlg_comment_delTV.visibility = View.GONE
            dialogView.dlg_comment_blockTV.visibility = View.GONE

            member_id = PrefUtils.getIntPreference(context,"member_id")

            //댓삭
            if (commenter == member_id){
                dialogView.dlg_comment_delTV.visibility = View.VISIBLE
                dialogView.dlg_comment_delTV.setOnClickListener {
                    val params = RequestParams()
                    params.put("market_id", product_id)
                    params.put("commenter", commenter)
                    params.put("market_id", commentList[position].getInt("market_id"))

                    MarketAction.delete_market_comment(params, object :JsonHttpResponseHandler(){
                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                            println(response)

                            val result = response!!.getString("result")
                            if (result == "ok"){
                                //할일 : 리스트뷰에서 아이템을 지운다

                                commentAdapter.removeItem(position)
                                commentAdapter.notifyDataSetChanged()

                                alert.dismiss()
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

            //댓글복사
            dialogView.dlg_comment_copyTV.setOnClickListener {

                //클립보드 사용 코드
                val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Golf", commentList[position].getString("comment_content")) //클립보드에 ID라는 이름표로 id 값을 복사하여 저장
                clipboardManager.primaryClip = clipData

                Toast.makeText(context, "댓글이 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()

                alert.dismiss()
            }


            if (member_id.toString() == seller_id.toString()){
                dialogView.dlg_comment_blockTV.visibility = View.VISIBLE

                val json = commentList[position].getJSONObject("MarketComment")
                val blocked_yn = Utils.getString(json,"block_yn")

                if (blocked_yn == "Y"){
                    dialogView.dlg_comment_blockTV.text = "차단해제"
                    blockYN = "unblock"
                } else {
                    dialogView.dlg_comment_blockTV.text = "차단하기"
                    blockYN = "block"
                }

                //댓글 작성자 게시글에 차단
                dialogView.dlg_comment_blockTV.setOnClickListener {
                    /*val json = commentList.get(position)
                    val data = json.getJSONObject("")*/
                    var cmt_wrt_id = commentList[position].getInt("cmt_wrt_id")

                    val params = RequestParams()
                    params.put("market_id", product_id)
                    params.put("writer", seller_id)
                    params.put("commenter", cmt_wrt_id)
                    params.put("status", blockYN)

                    MarketAction.market_block_commenter(params, object : JsonHttpResponseHandler(){
                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                            try {
                                println(response)
                                //차단 성공하면 표시하고 토스트
                                val result = response!!.getString("result")
                                if (result == "ok") {
                                    //아이고 의미없다
                                    /*var message = response.getString("message")
                                    if (message == "registerd") {
                                        commentList[position].put("changedBlockYN", "Y")
                                        commentList[position].put("block_yn", "Y")
                                        commentAdapter.notifyDataSetChanged()
                                    } else {
                                        commentList[position].put("changedBlockYN", "Y")
                                        commentList[position].put("block_yn", "N")
                                        commentAdapter.notifyDataSetChanged()
                                    }*/
                                    commentList.clear()
                                    getcomment()

                                } else {
                                    commentList[position].put("changedBlockYN", "N")
                                }
                            }catch (e : JSONException){
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                            println(errorResponse)
                        }

                        override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                            println(responseString)
                        }
                    })

                    alert.dismiss()
                }
            }

            true
        }

        market_commentLV.setOnItemClickListener { adapterView, view, i, l ->
            if (PrefUtils.getIntPreference(context, "member_id") == -1){
                Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }
            var data = commentList.get(i)
            Log.d("데이데이",data.toString())
            val marketcomment = data.getJSONObject("MarketComment")

            val comments_id = Utils.getInt(marketcomment, "id")

            p_comments_id = Utils.getInt(marketcomment,"p_comments_id")
            op_comments_id = Utils.getInt(marketcomment,"op_comments_id")
            var user_nick =  Utils.getString(marketcomment,"nick")
            if (p_comments_id!=-1){
                op_comments_id = p_comments_id
                commentET.requestFocus()
                Utils.showKeyboard(context)
                commentET.hint = user_nick+ "님의 댓글에 대댓글"
            }else if (comments_id != -1) {
                p_comments_id = comments_id
                commentET.requestFocus()
                Utils.showKeyboard(context)
                commentET.hint = user_nick+ "님의 댓글에 답글"
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PRODUCT_MODIFY){
            getProductData()
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REPORT_OK){
            getProductData()
        }
    }

    fun popupDialogView(){

        if (seller_id == PrefUtils.getIntPreference(context,"member_id")) {

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_comment_menu, null) //사용자 정의 다이얼로그 xml 붙이기
            builder.setView(dialogView) // custom xml과 alertDialogBuilder를 붙임
            val alert = builder.show() //builder를 끄기 위해서는 alertDialog에 이식해줘야 함

            dialogView.dlg_comment_copyTV.visibility = View.GONE
            dialogView.dlg_comment_blockTV.visibility = View.GONE
            dialogView.dlg_comment_delTV.visibility = View.GONE

            dialogView.dlg_prod_modTV.visibility = View.VISIBLE
            dialogView.dlg_prod_delTV.visibility = View.VISIBLE
            dialogView.dlg_pull_LL.visibility = View.VISIBLE

            dialogView.dlg_prod_modTV.setOnClickListener {

                if (tmp_prod_status == "판매완료"){
                    Toast.makeText(context, "판매완료가 된글은 변경하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val intent = Intent(context,AddGoodsActivity::class.java)
                intent.putExtra("product_id", product_id)
                startActivityForResult(intent,PRODUCT_MODIFY)

                alert.dismiss()
            }

            dialogView.dlg_prod_delTV.setOnClickListener {
                //삭제 액션
                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("정말 삭제하시겠습니까 ?")

                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                            delete_product("del")
                            alert.dismiss()
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                        })
                val alert = builder.create()
                alert.show()
            }

            dialogView.dlg_pull_LL.setOnClickListener {

                if (tmp_prod_status == "판매완료"){
                    Toast.makeText(context, "판매완료가 된글은 변경하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                //끌올
                delete_product("pull")
                alert.dismiss()
            }
        }
    }

    private fun delete_product(type : String) {
        val params = RequestParams()
        params.put("type", type)
        params.put("product_id", product_id)

        MarketAction.delete_market_item(params,object :JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    var message = response.getString("message")
                    if (message == "delete"){
                        Toast.makeText(context,"게시물이 삭제되었습니다",Toast.LENGTH_SHORT).show()
                        var intent = Intent()
                        intent.action = "DELETE_OK"
                        sendBroadcast(intent)
                        finish()
                    } else if (message == "pullup"){
                        Utils.alert(context,"게시글을 끌어올렸습니다.")
                        var intent = Intent()
                        intent.action = "PULL_UP"
                        sendBroadcast(intent)
                    } else if (message == "already pulled-up content"){
                        Utils.alert(context,"오늘 이미 끌어올리기를 사용한 게시글입니다.\n더이상 게시글을 끌어올릴 수 없습니다.\n내일 다시 시도해주시길 바랍니다.")
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

    fun MoveSellerActivity(){
        var intent = Intent(this, SellerActivity::class.java)
        intent.putExtra("seller_id", seller_id)
        intent.putExtra("type","seller")
        startActivity(intent)
    }

    fun MoveReportActivity(member_id : Int){
        if (PrefUtils.getIntPreference(context, "member_id") == -1){
            Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
            return
        }

        if (member_id == PrefUtils.getIntPreference(context, "member_id")){
            Toast.makeText(this, "자신의 게시물은 신고하실 수 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            var intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("member_id", member_id)
            intent.putExtra("market_id",product_id)
            startActivityForResult(intent,REPORT_OK)
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
                    if (_Images != null){
                        _Images.clear()
                    }
                    val product = response.getJSONObject("product")
                    val market = product.getJSONObject("Market")
                    var seller_cnt = Utils.getString(market,"seller_cnt")
                    sellercountTV.setText("("+seller_cnt+")")
                    seller_id2 = Utils.getString(market,"member_id")

                    if (seller_id2.toInt() == PrefUtils.getIntPreference(context, "member_id")){
                        show_mng_dlgLL.visibility = View.VISIBLE
                    }

                    if (Utils.getString(market,"form").equals("여성용")||Utils.getString(market,"form").equals("남성용")||Utils.getString(market,"form").equals("공용")){
                        categoryTV.text = "[${Utils.getString(market,"form")}]" +
                                " ${Utils.getString(market,"brand")} > ${Utils.getString(market,"product_type")}"
                    }else{
                        categoryTV.text = "[${Utils.getString(market,"form").substringBefore(" ")}형]" +
                                "[${Utils.getString(market,"form").substringAfter(" ")}용]" +
                                " ${Utils.getString(market,"brand")} > ${Utils.getString(market,"product_type")}"
                    }



                    brand = Utils.getString(market,"brand")
                    product_type = Utils.getString(market,"product_type")
                    tagTV.text = "# ${Utils.getString(market,"brand")} 정품"


                    tmp_prod_status = Utils.getString(market,"status")


                    sale_statusTV.text = tmp_prod_status
                    prd_titleTV.text = Utils.getString(market,"title")
                    writtenDateTV.text = Utils.getString(market,"created")
                    descriptionTV.text = Utils.getString(market,"description")
                    if (tmp_prod_status.equals("판매완료")){
                        prd_priceTV.text ="********"
                    }else{
                        prd_priceTV.text = Utils._comma(Utils.getString(market,"price"))
                    }

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
                    seller_id = Utils.getInt(member,"id")

                    member_id = Utils.getInt(member,"id")

                    val img_uri = Utils.getString(member,"profile_img")
                    val image = Config.url + img_uri
                    ImageLoader.getInstance().displayImage(image, profileImgIV, Utils.UILoptionsProfile)


                    val marketImg = product.getJSONArray("MarketImg")
                    for (i in 0 until marketImg.length()){
                        val data = marketImg[i] as JSONObject
                        _Images.add(Config.url + Utils.getString(data,"img_uri"))
                    }
                    prodImgAdapter.notifyDataSetChanged()
                    if (_Images.size == 0){
                        imageCountTV.text = "이미지가 없는 게시글입니다"
                    } else {
                        imageCountTV.text = "1/${_Images.size}"
                    }

                    var reportcount = response.getString("reportcount")
                    reportTV.text = "신고하기($reportcount)"

                    if (reportcount == "0"){
                        reportTV.setBackgroundResource(R.drawable.btn_grey)
                    }

                    val lookers = response.getString("lookers")
                    main_item_view_count.setText(lookers)

                    val commentCount = response.getString("commentCount")
                    main_item_comment_count.setText(commentCount)

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

    fun updateProductStatus(){
        val params = RequestParams()
        params.put("status", tmp_prod_status)
        params.put("product_id", product_id)

        MarketAction.modify_item_info(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    sale_statusTV.text = tmp_prod_status
                    Toast.makeText(context,"$tmp_prod_status 으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
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


    fun getLooker(){
        product_id = intent.getIntExtra("product_id",0)
        val member_id = PrefUtils.getIntPreference(context, "member_id")

        var params = RequestParams()
        params.put("market_id",product_id)
        params.put("member_id",member_id)

        MarketAction.add_market_looker(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                println("result ----- $result")
                if (result == "ok" || result == "yes") {
                    val Looker = response.getJSONArray("Looker")
                    main_item_view_count.setText(Looker.length().toString())

                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

            }
        })
    }


    //새로운댓글
    fun getcomment() {
        val params = RequestParams()
        params.put("market_id", product_id)
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        MarketAction.get_market_comment(params,object :JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                println(response)
                val result = response!!.getString("result")
                if (result == "ok"){
                    val comments = response.getJSONArray("comments")
                    commentList.clear()
                    for (i in 0 until comments.length()){
                        commentList.add(comments[i] as JSONObject)
                        //commentList.get(i).put("changedBlockYN", "N")
                    }
                    commentAdapter.notifyDataSetChanged()
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

    fun addcomment() {
        var comment = Utils.getString(commentET)
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context, "member_id"))
        params.put("market_id", product_id)
        params.put("nick", PrefUtils.getStringPreference(context,"nickname"))
        params.put("comment", comment)
        params.put("parent", commentParent)
        params.put("type", commentType)
        params.put("p_comments_id", p_comments_id)
        params.put("op_comments_id", op_comments_id)

        MarketAction.add_market_comment(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {

                try {
                    val result = response!!.getString("result")
                    if ("ok" == result) {
                        Utils.hideKeyboard(context)
                        commentET.setText("")
                        commentET.hint = ""
                        p_comments_id = -1
                        op_comments_id = -1
                        getProductData()
                        getcomment()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONArray?) {
                super.onSuccess(statusCode, headers, response)
            }

            private fun error() {
                Utils.alert(context, "조회중 장애가 발생하였습니다.")
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONArray?) {

                throwable.printStackTrace()
                error()
            }

            override fun onStart() {
                // show dialog

            }

            override fun onFinish() {

            }
        })
    }



}
