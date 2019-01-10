package donggolf.android.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.actions.RegionAction
import donggolf.android.adapters.DlgGugunAdapter
import donggolf.android.adapters.DlgRegionAdapter
import donggolf.android.adapters.PeopleCountAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_add_dong_chat.*
import kotlinx.android.synthetic.main.dlg_people_count.view.*
import kotlinx.android.synthetic.main.dlg_select_chat_region.*
import kotlinx.android.synthetic.main.dlg_select_chat_region.view.*
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException

class AddDongChatActivity : RootActivity() {

    lateinit var context: Context

    var regionList1 = ArrayList<Map<String,Boolean>>()
    var regionList2 = ArrayList<Map<String,Boolean>>()

    var peoplecounts: ArrayList<String> = ArrayList<String>()
    lateinit var countAdapter:PeopleCountAdapter

    var bigcitylist: ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  lateinit var  cityadapter : DlgRegionAdapter
    var gugunList: ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  lateinit var  gugunadapter : DlgGugunAdapter
    var region_id = "48"

    private val GALLERY = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dong_chat)

        context = this

        getpeoplecount()
        countAdapter = PeopleCountAdapter(this, R.layout.item_people_count, peoplecounts)
        cityadapter = DlgRegionAdapter(context, R.layout.item_right_radio_btn_list, bigcitylist)
        gugunadapter = DlgGugunAdapter(context, R.layout.item_right_radio_btn_list, gugunList)
        getBigCity()
        getGugun(2)

        btn_cancelAddDongchat.setOnClickListener {
            finish()
        }

        region1TV.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_select_chat_region, null)
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlg_region_LV.adapter = cityadapter
            dialogView.dlg_region_LV.setOnItemClickListener { parent, view, position, id ->
                val item = bigcitylist.get(position)
                for (i in 0 until bigcitylist.size){
                    bigcitylist[i].put("isSelectedOp",false)
                }
                item.put("isSelectedOp",true)
                cityadapter.notifyDataSetChanged()
            }

            dialogView.btn_regionOK.setOnClickListener {
                for (i in 0 until bigcitylist.size){
                    val item = bigcitylist.get(i)
                    var isSel = item.getBoolean("isSelectedOp")
                    if (isSel){
                        var region = item.getJSONObject("Regions")
                        var name:String = Utils.getString(region,"name")
                        val parent_id = Utils.getString(region,"id")
                        leftregionTV.setText(name)
                        getGugun(parent_id.toInt())
                    }
                }
                alert.dismiss()
            }

            dialogView.btn_dlg_dismiss.setOnClickListener {
                alert.dismiss()
            }

        }

        region2TV.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_select_chat_region, null)
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlg_region_LV.adapter = gugunadapter
            dialogView.dlg_region_LV.setOnItemClickListener { parent, view, position, id ->
                val item = gugunList.get(position)
                for (i in 0 until gugunList.size){
                    gugunList[i].put("isSelectedOp",false)
                }
                item.put("isSelectedOp",true)
                gugunadapter.notifyDataSetChanged()
            }

            dialogView.btn_regionOK.setOnClickListener {
                for (i in 0 until gugunList.size){
                    val item = gugunList.get(i)
                    var isSel = item.getBoolean("isSelectedOp")
                    if (isSel){
                        var region = item.getJSONObject("Regions")
                        var name:String = Utils.getString(region,"name")
                        val id = Utils.getString(region,"id")
                        region_id = id
                        rightregionTV.setText(name)
                    }
                }
                alert.dismiss()
            }

            dialogView.btn_dlg_dismiss.setOnClickListener {
                alert.dismiss()
            }

        }

        moreRL.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_people_count, null)
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlg_count_LV.adapter = countAdapter

            dialogView.dlg_count_LV.setOnItemClickListener { parent, view, position, id ->
                val title = peoplecounts.get(position)
                peoplecountTV.setText(title)
                alert.dismiss()
            }

            dialogView.btn_dlg_dismisss.setOnClickListener {
                alert.dismiss()
            }

            dialogView.btn_countOK.setOnClickListener {
                alert.dismiss()
            }


        }

        profileRL.setOnClickListener {
            choosePhotoFromGallary()
        }

        profiledtIV.setOnClickListener {
            profileIV.setBackgroundResource(R.drawable.btn_add_picture)
            basicIV.visibility = View.VISIBLE
            profiledtIV.visibility = View.GONE
        }


    }


    fun adddongchat(){
        val title = titleET.text.toString()
        if (title.isEmpty()) {
            Utils.alert(context, "제목을 입력해주세요.")
            return
        }

        val content = contentET.text.toString()
        if (content.isEmpty()) {
            Utils.alert(context, "내용을 입력해주세요.")
            return
        }

        if (!agreeCB.isChecked){
            Utils.alert(context, "운영정책 동의를 체크해 주세요.")
            return
        }


    }

    fun getpeoplecount(){
        val item = "2"
        val item2 = "5"
        val item3 = "10"
        val item4 = "20"
        val item5 = "30"
        val item6 = "40"
        val item7 = "50"
        val item8 = "60"
        val item9 = "70"
        val item10 = "80"
        val item11 = "90"
        val item12 = "100"

        peoplecounts.add(item)
        peoplecounts.add(item2)
        peoplecounts.add(item3)
        peoplecounts.add(item4)
        peoplecounts.add(item5)
        peoplecounts.add(item6)
        peoplecounts.add(item7)
        peoplecounts.add(item8)
        peoplecounts.add(item9)
        peoplecounts.add(item10)
    }

    fun getBigCity(){

        val params = RequestParams()

        RegionAction.api_sido(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val datalist = response!!.getJSONArray("sido")

                if (datalist.length() > 0 && datalist != null){
                    for (i in 0 until datalist.length()){
                        bigcitylist.add(datalist.get(i) as JSONObject)
                        bigcitylist[i].put("isSelectedOp",false)
                    }

                    cityadapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
            }

        })

    }

    fun getGugun(position: Int){
        if (gugunList != null){
            gugunList.clear()
        }

        val params = RequestParams()
        params.put("sido",position)

        RegionAction.api_gugun(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                var datalist = response!!.getJSONArray("gugun")

                if (datalist.length() > 0 && datalist != null){
                    for (i in 0 until datalist.length()){
                        gugunList.add(datalist.get(i) as JSONObject)
                        gugunList[i].put("isSelectedOp",false)
                    }
                    gugunadapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                Toast.makeText(context, "불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                GALLERY -> {
                    if (data != null)
                    {
                        val contentURI = data.data
                        Log.d("uri",contentURI.toString())
                        //content://media/external/images/media/1200

                        try
                        {
                            //갤러리에서 가져온 이미지를 프로필에 세팅
                            var thumbnail = MediaStore.Images.Media.getBitmap(context.contentResolver, contentURI)
                            val resized = Utils.resizeBitmap(thumbnail, 4000)
                            profileIV.setImageBitmap(resized)
                            basicIV.visibility = View.GONE
                            profiledtIV.visibility = View.VISIBLE

                            //전송하기 위한 전처리
                            //먼저 ImageView에 세팅하고 세팅한 이미지를 기반으로 작업
                            val bitmap = profileIV.drawable as BitmapDrawable
                            val img = ByteArrayInputStream(Utils.getByteArray(bitmap.bitmap))

                        }
                        catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(context, "바꾸기실패", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        }

    }




}
