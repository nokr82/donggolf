package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_view_album.*
import org.json.JSONException
import org.json.JSONObject
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.PopupMenu
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import donggolf.android.adapters.ViewAlbumAdapter
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.dlg_ans_profile_del.view.*
import kotlinx.android.synthetic.main.item_custom_gallery_folder.*
import java.io.ByteArrayInputStream
import java.util.*

class ViewAlbumActivity : RootActivity() , AdapterView.OnItemClickListener{

    lateinit var context: Context

    private lateinit var eachViewAdapter : ViewAlbumAdapter
    private var albumList = ArrayList<JSONObject>()//Path Array

    var tmp_member_id = 0
    var login_id = 0

    var clickedItmeCnt = 0
    var selectedImageList = ArrayList<String>()
    var selImgViewPositions = ArrayList<Int>()

    var p_type = -1


    var SELECT_PICTURE = 100

    var images_path: ArrayList<String> = ArrayList<String>()

    private val selected = LinkedList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_album)

        context = this
        tmp_member_id = intent.getIntExtra("viewAlbumListUserID",0)
        login_id = PrefUtils.getIntPreference(context,"member_id")

        selectGV.setOnItemClickListener(this)
        eachViewAdapter = ViewAlbumAdapter(context, R.layout.findpicture_gridview_item, albumList,selected)
        selectGV.adapter = eachViewAdapter

        getProfilImageList()

        //잡다한 뷰 세팅
        finishBT.setOnClickListener { finish() }

        albumMenuIV.setOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View) {
                //Creating the instance of PopupMenu
                val popup = PopupMenu(context, albumMenuIV)
                //Inflating the Popup using xml file
                popup.menuInflater.inflate(R.menu.album_menu, popup.menu)

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_img_del -> {

                            val builder = AlertDialog.Builder(context)
                            val dialogView = layoutInflater.inflate(R.layout.dlg_ans_profile_del, null)
                            builder.setView(dialogView)
                            val alert = builder.show()

//                            dialogView.dlg_selected_cntTV.text = selImgViewPositions.size.toString()
                            dialogView.dlg_selected_cntTV.text = selected.size.toString()

                            dialogView.dlg_yesTV.setOnClickListener {
                                removeImages()
                                alert.dismiss()
                            }

                            dialogView.dlg_noTV.setOnClickListener {

                                alert.dismiss()
                            }

                            dialogView.dlg_closeIV.setOnClickListener {
                                alert.dismiss()
                            }

                            return@setOnMenuItemClickListener true
                        }
                        R.id.menu_add_img -> {
                            if (login_id == tmp_member_id) {
//                                val galleryIntent = Intent(Intent.ACTION_PICK,
//                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//
//                                startActivityForResult(galleryIntent, GALLERY)

                                permissionimage(1)

                            } else {

                            }
                            return@setOnMenuItemClickListener true
                        }
                        R.id.menu_posting -> {
                            p_type = 1
                            if (selected.size > 1) {
                                Toast.makeText(context, "[ 프로필 사진변경은 1장만 선택하여 주세요", Toast.LENGTH_SHORT).show()
                                return@setOnMenuItemClickListener false
                            }

                            change_profile()

                            return@setOnMenuItemClickListener true
                        }
                        else -> return@setOnMenuItemClickListener false
                    }
                }

                popup.show()//showing popup menu
            }
        })



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
                    }

                    if (images_path != null){
                        add_img_in_album()
                    }

                }
            }
        }
    }

    fun getProfilImageList(){
        val params = RequestParams()
        params.put("member_id", tmp_member_id)

        MemberAction.get_member_img_history(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        albumList.clear()
                        val memberImages = response.getJSONArray("MemberImgs")
                        totalImgCntTV.text = "(${memberImages.length()})"
                        for (i in 0 until memberImages.length()) {

                            val json = memberImages[i] as JSONObject
                            if (tmp_member_id == login_id) {
                                json.put("editMode", true)
                            } else {
                                json.put("editMode", false)
                            }
                            json.put("select_album_img_cnt", 0)
                            json.put("duplicateClicks", false)

                            albumList.add(json)

                        }
                        eachViewAdapter.notifyDataSetChanged()
                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
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


    fun change_profile(){

        var profile_img =""
        for (strPo in selected) {
            val item = albumList.get(strPo.toInt())
            val MemberImg = item.getJSONObject("MemberImg")
            profile_img = Utils.getString(MemberImg,"image_uri")
        }

        //이미지 전송
        val params = RequestParams()
        params.put("profile_img", profile_img)
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        MemberAction.m_update_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    Toast.makeText(context,"프로필사진이 변경되었습니다.",Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                if (errorResponse != null)
                    println(errorResponse.getString("message"))
            }
        })
    }


    fun removeImages(){

        var idx = 0
        var idxn = 0

        for (strPo in selected) {
            val item = albumList.get(strPo.toInt())
            val MemberImg = item.getJSONObject("MemberImg")
            val image_id = Utils.getString(MemberImg,"id")
            selectedImageList.add(image_id)
            println("---------$image_id")
        }

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
        params.put("deleteImgs",selectedImageList)
        clickedItmeCnt = 0
        MemberAction.delete_profile_imgs(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {

                    println(response)
                    println(selImgViewPositions)
                    /*for (i in 0 until selImgViewPositions.size) {
                        //eachViewAdapter.removeItem(selImgViewPositions[i])
                        albumList.removeAt(selImgViewPositions[i])
                    }*/
                    selImgViewPositions.clear()
                    selImgViewPositions.sort()
                    selected.clear()
                    getProfilImageList()
                    var intent = Intent()
                    intent.action = "DELETE_IMG"
                    sendBroadcast(intent)

                    eachViewAdapter.notifyDataSetChanged()

                }catch (e:JSONException){
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
    }

    fun add_img_in_album(){
        val params = RequestParams()
        params.put("member_id", tmp_member_id)

        if (images_path != null){
            if (images_path!!.size != 0){
                for (i in 0..images_path!!.size - 1){
                    var bt: Bitmap = Utils.getImage(context.contentResolver, images_path!!.get(i))

                    params.put("files[" + i + "]",  ByteArrayInputStream(Utils.getByteArray(bt)))
                }
            }
        }

        MemberAction.add_img_in_album(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        images_path.clear()
                        getProfilImageList()
                        var intent = Intent()
                        intent.action = "RESET_DATA"
                        sendBroadcast(intent)
                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
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

    private fun permissionimage(type:Int) {

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                if (type ==1){
                    moveMyPicture()
                }else{
                    moveMyProfile()
                }

            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(context,"권한설정을 해주셔야 합니다.",Toast.LENGTH_SHORT).show()
            }

        }

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

    }

    fun moveMyPicture(){
        var intent = Intent(context, FindPictureActivity::class.java);
        intent.putExtra("image","image")
        startActivityForResult(intent, SELECT_PICTURE);
    }
    fun moveMyProfile(){
        var intent = Intent(context, FindPictureActivity::class.java);
        intent.putExtra("image","profile")
        startActivityForResult(intent, SELECT_PICTURE)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {


        val strPo = position.toString()

        if (p_type !=1){
            if (selected.contains(strPo)) {
                selected.remove(strPo)

                val adapter = selectGV.getAdapter()
                if (adapter != null) {
                    val f = adapter as ViewAlbumAdapter
                    (f as BaseAdapter).notifyDataSetChanged()
                }

            } else {
                if (selected.size > 9) {
                    Toast.makeText(context, "사진은 10개까지 등록가능합니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                selected.add(strPo)

                val adapter = selectGV.getAdapter()
                if (adapter != null) {
                    val f = adapter as ViewAlbumAdapter
                    (f as BaseAdapter).notifyDataSetChanged()
                }
            }
        }else{
            if (selected.contains(strPo)) {
                selected.remove(strPo)

                val adapter = selectGV.getAdapter()
                if (adapter != null) {
                    val f = adapter as ViewAlbumAdapter
                    (f as BaseAdapter).notifyDataSetChanged()
                }

            } else {


                selected.add(strPo)

                val adapter = selectGV.getAdapter()
                if (adapter != null) {
                    val f = adapter as ViewAlbumAdapter
                    (f as BaseAdapter).notifyDataSetChanged()
                }
            }
        }

    }
}
