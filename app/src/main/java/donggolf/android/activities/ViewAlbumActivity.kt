package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import android.widget.PopupMenu
import android.widget.Toast
import donggolf.android.adapters.ViewAlbumAdapter
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.dlg_ans_profile_del.view.*
import kotlinx.android.synthetic.main.findpicture_gridview_item.view.*
import java.io.ByteArrayInputStream
import java.io.IOException

class ViewAlbumActivity : RootActivity() {

    lateinit var context: Context

    private lateinit var eachViewAdapter : ViewAlbumAdapter
    private var albumList = ArrayList<JSONObject>()//Path Array

    val GALLERY = 2
    var tmp_member_id = 0
    var login_id = 0

    var clickedItmeCnt = 0
    var selectedImageList = ArrayList<String>()
    var selectedImageViewList = ArrayList<String>()
    var selImgViewPositions = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_album)

        context = this
        tmp_member_id = intent.getIntExtra("viewAlbumListUserID",0)
        login_id = PrefUtils.getIntPreference(context,"member_id")

        eachViewAdapter = ViewAlbumAdapter(context, R.layout.findpicture_gridview_item, albumList)
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

                            dialogView.dlg_selected_cntTV.text = selImgViewPositions.size.toString()

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
                                val galleryIntent = Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                                startActivityForResult(galleryIntent, GALLERY)

                            } else {

                            }
                            return@setOnMenuItemClickListener true
                        }
                        R.id.menu_posting -> {

                            return@setOnMenuItemClickListener true
                        }
                        else -> return@setOnMenuItemClickListener false
                    }
                }

                popup.show()//showing popup menu
            }
        })

        selectGV.setOnItemClickListener { parent, view, position, id ->

            //var is_duplicateClicks= albumList[position].getBoolean("duplicateClicks")
            if (clickedItmeCnt <= albumList.size) {

                var dc_tf = albumList[position].getBoolean("duplicateClicks")

                println("temp position item : $dc_tf")
                if (dc_tf){
                    albumList[position].put("duplicateClicks", !dc_tf)
                    println("removed item : ${selImgViewPositions-1}")
                    selImgViewPositions.removeAt(selImgViewPositions.size - position - 1)
                    clickedItmeCnt--
                } else {
                    albumList[position].put("duplicateClicks", !dc_tf)
                    clickedItmeCnt++
                    albumList[position].put("select_album_img_cnt", clickedItmeCnt)
                    selectedImageList.add(albumList[position].getString("image_id"))
                    selectedImageViewList.add(albumList[position].getString("image_uri"))
                    Log.d("이미지배열",selectedImageViewList.toString())
                    selImgViewPositions.add(position)
                }

                println(selImgViewPositions)

                eachViewAdapter.notifyDataSetChanged()
            }

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && resultCode == GALLERY) {
            if (data != null)
            {
                val contentURI = data.data
                Log.d("갤러리",contentURI.toString())
                //content://media/external/images/media/1200

                try
                {
                    //갤러리에서 가져온 이미지를 프로필에 세팅
                    var thumbnail = MediaStore.Images.Media.getBitmap(context!!.contentResolver, contentURI)
                    val resized = Utils.resizeBitmap(thumbnail, 100)
//                            imgProfile.setImageBitmap(resized)

                    //전송하기 위한 전처리
                    //먼저 ImageView에 세팅하고 세팅한 이미지를 기반으로 작업
                    val bitmap = resized
                    val img = ByteArrayInputStream(Utils.getByteArray(bitmap))

                    //이미지 전송
                    val params = RequestParams()
                    params.put("files", img)
                    params.put("type", "image")
                    params.put("member_id",PrefUtils.getIntPreference(context, "member_id"))

                    MemberAction.update_info(params, object : JsonHttpResponseHandler() {
                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                            //getTempUserInformation("image")
                            getProfilImageList()
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
                catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "실패", Toast.LENGTH_SHORT).show()
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

    fun removeImages(){
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

}
