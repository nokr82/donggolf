package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.actions.ChattingAction
import donggolf.android.actions.MemberAction
import donggolf.android.actions.RegionAction
import donggolf.android.adapters.DlgGugunAdapter
import donggolf.android.adapters.DlgRegionAdapter
import donggolf.android.adapters.PeopleCountAdapter
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_add_dong_chat.*
import donggolf.android.R
import kotlinx.android.synthetic.main.dlg_chat_blockcode.view.*
import kotlinx.android.synthetic.main.dlg_people_count.view.*
import kotlinx.android.synthetic.main.dlg_select_chat_region.view.*
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException

class AddDongChatActivity : RootActivity() {

    lateinit var context: Context

    var peoplecounts: ArrayList<String> = ArrayList<String>()
    lateinit var countAdapter: PeopleCountAdapter

    var bigcitylist: ArrayList<JSONObject> = ArrayList<JSONObject>()
    private lateinit var cityadapter: DlgRegionAdapter
    var gugunList: ArrayList<JSONObject> = ArrayList<JSONObject>()
    private lateinit var gugunadapter: DlgGugunAdapter
    var region_id = "48"
    var region_ids: ArrayList<Int> = ArrayList()
    var region_names: ArrayList<String> = ArrayList()
    private val PROFILE = 1
    private val BACKGROUND = 2

    var profile: Bitmap? = null
    var background: Bitmap? = null

    var secretcode = ""
    var todaycount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dong_chat)

        context = this

//        getpeoplecount()
        countAdapter = PeopleCountAdapter(this, R.layout.item_people_count, peoplecounts)
        cityadapter = DlgRegionAdapter(context, R.layout.item_right_radio_btn_list, bigcitylist)
        gugunadapter = DlgGugunAdapter(context, R.layout.item_right_radio_btn_list, gugunList)
        getBigCity()
        getGugun(2)

        btn_cancelAddDongchat.setOnClickListener {
            Utils.hideKeyboard(this)
            finish()
        }

        gotermLL.setOnClickListener {
            val intent = Intent(this, OperatingActivity::class.java)
            startActivity(intent)
        }

        region1TV.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_select_chat_region, null)
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlg_region_LV.adapter = cityadapter
            dialogView.dlg_region_LV.setOnItemClickListener { parent, view, position, id ->
                    val item = bigcitylist.get(position)
                    //Log.d("아템",item.toString())
                    var region = item.getJSONObject("Regions")
                    var name = Utils.getString(region, "name")
                    val parent_id = Utils.getString(region, "id")

                    leftregionTV.text = name
                if (name == "전국") {
                    rightregionTV.setText("전국")
                    region_id = "0"
                } else {
                    rightregionTV.setText("선택해주세요")
                    region_id = "-1"
                }

                getGugun(parent_id.toInt())
                alert.dismiss()
                gogundlg()


            }



            dialogView.btn_dlg_dismiss.setOnClickListener {
                alert.dismiss()
            }

        }
        peoplecountET.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var cnt = 0
                var num = s.toString()
                if (num != "") {
                    cnt = num.toInt()
                    if (cnt > 80) {
                        Toast.makeText(context, "가용인원수는 최대80명입니다.", Toast.LENGTH_SHORT).show()
                        peoplecountET.setText("80")
                        return
                    }
                }
            }

            override fun afterTextChanged(count: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
        })


        region2TV.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_select_chat_region, null)
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlg_region_LV.adapter = gugunadapter
            dialogView.dlg_region_LV.setOnItemClickListener { parent, view, position, id ->

                val item = gugunList.get(position)
                Log.d("아템",item.toString())
                gugunList[position].put("isSelectedOp",true)
                var region = item.getJSONObject("Regions")
                var name = Utils.getString(region, "name")
                val id = Utils.getString(region, "id")
                region_id = id
                rightregionTV.text = name
            }
            dialogView.btn_dlg_dismiss.setOnClickListener {
                alert.dismiss()
            }

        }



        profileRL.setOnClickListener {
            //            chooseProfile()
            permissionprofile()
        }

        profiledtIV.setOnClickListener {
            profileIV.setImageResource(0)
            basicIV.visibility = View.VISIBLE
            profiledtIV.visibility = View.GONE
            profile = null
        }

        backgroundRV.setOnClickListener {
            //            chooseBackground()
            permissionbackground()
        }

        backgrounddtIV.setOnClickListener {
            backgroundIV.setImageResource(0)
            backbasicIV.visibility = View.VISIBLE
            backgrounddtIV.visibility = View.GONE
            background = null
        }

        adddongchatTV.setOnClickListener {
            adddongchatTV.isEnabled = false
            adddongchat()
        }

        invisibleRB.setOnClickListener {


            val builder = AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dlg_chat_blockcode, null)
            builder.setView(dialogView)
            val alert = builder.show()

            dialogView.dlgTitle.setText("비공개 코드 입력")
            dialogView.categoryTitleET.setHint("코드를 입력해 주세요.")

            if (secretcode == "") {
                dialogView.blockcodeTV.setText(secretcode)
                dialogView.codevisibleLL.visibility = View.GONE
            }
            dialogView.btn_title_clear.setOnClickListener {
                alert.dismiss()
            }

            dialogView.cancleTV.setOnClickListener {
                alert.dismiss()
            }

            dialogView.okTV.setOnClickListener {
                val code = dialogView.categoryTitleET.text.toString()
                if (code == null || code == "") {
                    Toast.makeText(context, "빈칸은 입력하실 수 없습니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                secretcode = code

                alert.dismiss()
            }
        }

    }

    fun adddongchat() {
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

       /* if (!agreeCB.isChecked) {
            Utils.alert(context, "운영정책 동의를 체크해 주세요.")
            return
        }*/

        var max_count = peoplecountET.text.toString()
        if (max_count.isEmpty()){
            Utils.alert(context, "인원수를 입력해주세요.")
            return
        } else if (max_count.toInt() > 80){
            Utils.alert(context, "인원 최대 제한은 80명입니다.")
            return
        }

      /*  if (region_id == "-1") {
            Utils.alert(context, "지역 선택은 필수 입니다다.")
            return
        }*/
        if (region_ids.size<1) {
            Utils.alert(context, "지역 선택은 필수 입니다다.")
            return
        }

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("title", title)
        params.put("max_count", max_count.toInt())
        params.put("introduce", content)
//        params.put("regions", region_id)
        params.put("regions", region_ids.joinToString(",").replace(" ",""))

        if (profile == null) {
            Utils.alert(context, "프로필사진은 필수 입력입니다.")
            return
        } else {
            params.put("intro", ByteArrayInputStream(Utils.getByteArray(profile)))
        }

        if (background == null) {
            Utils.alert(context, "대문/배경사진은 필수 입력입니다.")
            return
        } else {
            params.put("background", ByteArrayInputStream(Utils.getByteArray(background)))
        }
        params.put("type", "2")
        params.put("division", 0)

        if (secretcode != "") {
            params.put("block_code", secretcode)
        }

        ChattingAction.add_chat(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {

                    var intent = Intent()
                    intent.putExtra("reset", "reset")
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
//                println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
//                println(errorResponse)
            }
        })

    }


    fun gogundlg(){
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dlg_select_chat_region, null)
        builder.setView(dialogView)
        val alert = builder.show()
        region_names.clear()
        region_ids.clear()
        dialogView.dlg_region_LV.adapter = gugunadapter
        dialogView.dlg_region_LV.setOnItemClickListener { parent, view, position, id ->

            val item = gugunList.get(position)
            Log.d("아템",item.toString())
            var issel = Utils.getBoolen(item,"isSelectedOp")
            if (issel){
                gugunList[position].put("isSelectedOp",false)
                var region = item.getJSONObject("Regions")
                var name = Utils.getString(region, "name")
                val id = Utils.getInt(region, "id")
                region_names.remove(name)
                region_ids.remove(id)
            }else{
                if (region_ids.size>2){
                    Toast.makeText(context,"최대선택지역은 3개입니다.",Toast.LENGTH_SHORT).show()
                    return@setOnItemClickListener
                }
                gugunList[position].put("isSelectedOp",true)
                var region = item.getJSONObject("Regions")
                var name = Utils.getString(region, "name")
                val id = Utils.getInt(region, "id")

                region_names.add(name)
                region_ids.add(id)
            }
            gugunadapter.notifyDataSetChanged()


//            region_id = id
//            rightregionTV.text = name

        }
        dialogView.btn_regionOK.setOnClickListener {
            var region = region_names.toString().replace("[","").replace("]","")
            leftregionTV.text = region
            alert.dismiss()
        }
        dialogView.btn_dlg_dismiss.setOnClickListener {
            alert.dismiss()
        }
    }


    fun getBigCity() {

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        RegionAction.api_sido(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val datalist = response!!.getJSONArray("sido")

                if (datalist.length() > 0 && datalist != null) {
                    for (i in 0 until datalist.length()) {
                        bigcitylist.add(datalist.get(i) as JSONObject)
                        bigcitylist[i].put("isSelectedOp", false)
                    }

                    todaycount = response!!.getInt("todayCount")
                    cntTV.setText(todaycount.toString() + "/5")

                    cityadapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
            }

        })

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
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                Toast.makeText(context, "불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun chooseProfile() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, PROFILE)
    }

    private fun chooseBackground() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, BACKGROUND)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                PROFILE -> {
                    if (data != null) {
                        val contentURI = data.data
                        //Log.d("uri", contentURI.toString())
//                        profile = contentURI
                        //content://media/external/images/media/1200
                        try {
                            //갤러리에서 가져온 이미지를 프로필에 세팅
//                            var thumbnail = MediaStore.Images.Media.getBitmap(context.contentResolver, contentURI)

                            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)

                            val cursor = context.contentResolver.query(contentURI, filePathColumn, null, null, null)
                            if (cursor!!.moveToFirst()) {
                                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                                val picturePath = cursor.getString(columnIndex)

                                cursor.close()

                                profile = Utils.getImage(context.contentResolver, picturePath.toString())
//                            val resized = Utils.resizeBitmap(thumbnail, 100)
//                            profile = thumbnail
                                basicIV.visibility = View.GONE
//                            profileIV.setImageURI(contentURI)
                                profileIV.setImageBitmap(profile)
                                profiledtIV.visibility = View.VISIBLE

                            }
//                            val img = ByteArrayInputStream(Utils.getByteArray(thumbnail))

                            //전송하기 위한 전처리
                            //먼저 ImageView에 세팅하고 세팅한 이미지를 기반으로 작업
//                            val bitmap = profileIV.drawable as BitmapDrawable
//                            val img = ByteArrayInputStream(Utils.getByteArray(bitmap.bitmap))

                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(context, "바꾸기실패", Toast.LENGTH_SHORT).show()
                        }

                    }
                }

                BACKGROUND -> {
                    if (data != null) {
                        val contentURI = data.data
                        //Log.d("uri", contentURI.toString())
                        //content://media/external/images/media/1200
//                        background = contentURI
                        backgroundIV.setImageResource(0)
                        try {
                            //갤러리에서 가져온 이미지를 프로필에 세팅
//                            var thumbnail = MediaStore.Images.Media.getBitmap(context.contentResolver, contentURI)
                            var thumbnail = Utils.getImage(context.contentResolver, contentURI.toString())
//                            val resized = Utils.resizeBitmap(thumbnail, 100)
//                            background = thumbnail
//                            backgroundIV.setImageURI(contentURI)
//                            backbasicIV.visibility = View.GONE
//                            backgrounddtIV.visibility = View.VISIBLE

                            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)

                            val cursor = context.contentResolver.query(contentURI, filePathColumn, null, null, null)
                            if (cursor!!.moveToFirst()) {
                                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                                val picturePath = cursor.getString(columnIndex)

                                cursor.close()

                                background = Utils.getImage(context.contentResolver, picturePath.toString())
//                            val resized = Utils.resizeBitmap(thumbnail, 100)
//                            profile = thumbnail
                                backbasicIV.visibility = View.GONE
//                            profileIV.setImageURI(contentURI)
                                backgroundIV.setImageBitmap(background)
                                backgrounddtIV.visibility = View.VISIBLE
                            }

                            //전송하기 위한 전처리
                            //먼저 ImageView에 세팅하고 세팅한 이미지를 기반으로 작업
//                            val bitmap = backgroundIV.drawable as BitmapDrawable
//                            val img = ByteArrayInputStream(Utils.getByteArray(bitmap.bitmap))


                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(context, "바꾸기실패", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        }

    }


    private fun permissionprofile() {

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                chooseProfile()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(context, "권한설정을 해주셔야 합니다.", Toast.LENGTH_SHORT).show()
            }

        }

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

    }

    private fun permissionbackground() {

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                chooseBackground()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(context, "권한설정을 해주셔야 합니다.", Toast.LENGTH_SHORT).show()
            }

        }

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

    }


}
