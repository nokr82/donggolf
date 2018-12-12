package donggolf.android.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.internal.InternalTokenResult
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.squareup.okhttp.internal.Util
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.MemberAction
import donggolf.android.actions.ProfileAction
import donggolf.android.activities.*
import donggolf.android.adapters.ImageAdapter
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.FirebaseFirestoreUtils.Companion.db
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.Content
import donggolf.android.models.Photo
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_findid.*
import kotlinx.android.synthetic.main.activity_mod_status_msg.*
import kotlinx.android.synthetic.main.activity_profile_manage.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception

class InfoFragment : Fragment(){

    var ctx: Context? = null

    val SELECT_PROFILE = 104
    val SELECT_STATUS = 105
    val MODIFY_NAME = 106
    val MODIFY_TAG = 107
    val REGION_CHANGE = 108

    private var pimgPaths: ArrayList<String> = ArrayList<String>()//이미지 경로
    private var images: ArrayList<ByteArray> = ArrayList()
    private var smimages: ArrayList<ByteArray> = ArrayList()
    private var strPaths: ArrayList<String> = ArrayList<String>()
    private var strPathsL : ArrayList<String> = ArrayList<String>()
    private var strPathsS : ArrayList<String> = ArrayList<String>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)

        val ctx = context
        if (null != ctx) {
            doSomethingWithContext(ctx)
        }


        return inflater.inflate(R.layout.activity_profile_manage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //프로필 세팅
        var member_id = PrefUtils.getIntPreference(context, "member_id")

        //Action 으로 정보 가져오기
        val params = RequestParams()
        params.put("member_id", member_id)

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject) {
                try {
                    println("InfoFrag :: $response")
                    val result = response!!.getString("result")

                    if (result == "ok") {
                        val member = response.getJSONObject("Member")

                        textDate.text = Utils.getString(member,"created")
                        txUserName.text = Utils.getString(member,"nick")

                        var region = ""

                        if (Utils.getString(member,"region1") != null) {
                            region += Utils.getString(member,"region1") + ","
                        }
                        if (Utils.getString(member,"region2") != null) {
                            region += Utils.getString(member,"region2") + ","
                        }
                        if (Utils.getString(member,"region3") != null) {
                            region += Utils.getString(member,"region3")
                        }

                        if (region.substring(region.length-1) == ","){
                            region = region.substring(0, region.length-2)
                        }
                        txUserRegion.text = region


                        var statusMessage = Utils.getString(member,"status_msg")
                        if (statusMessage != null) {
                            infoStatusMsg.text = statusMessage
                        }


                        knowTogether.visibility = View.GONE

                        //해시태그
                        val data = response.getJSONArray("tags")
                        if (data != null) {
                            var string_tag = ""
                            for (i in 0 until data.length()) {
                                var json = data[i] as JSONObject
                                val memberTag = json.getJSONObject("MemberTags")

                                string_tag += "#" + Utils.getString(memberTag, "tag") + " "
                            }
                            hashtagTV.text = string_tag
                        }

                        //프로필 이미지
                        /*val imgdata = response.getJSONArray("")
                        mngTXPhotoCnt.text*/
                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject) {
                println(errorResponse.toString())
            }
        })

        tv_CONSEQUENCES.setOnClickListener {
            var intent = Intent(activity, OtherManageActivity::class.java)
            startActivity(intent)
        }

        addProfImg.setOnClickListener {
            var intent = Intent(activity, SelectProfileImgActivity::class.java)
            //var intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, SELECT_PROFILE)
        }

        imgProfile.setOnClickListener {
            var intent = Intent(activity, ViewProfileListActivity::class.java)
            //intent.putExtra("album", images)
            startActivity(intent)
        }

        messageTV.setOnClickListener {
            var intent = Intent(activity, ModStatusMsgActivity::class.java)
            startActivityForResult(intent, SELECT_STATUS)
        }

        /*myNeighbor.setOnClickListener {
            var intent = Intent(activity, MutualActivity::class.java)
            startActivity(intent)
        }*/

        btnNameModi.setOnClickListener {
            var intent = Intent(activity, ProfileNameModifActivity::class.java)
            startActivityForResult(intent, MODIFY_NAME)
        }

        tv_CONSEQUENCES.setOnClickListener {
            var itt = Intent(activity, OtherManageActivity::class.java)
            startActivity(itt)
        }

        prfhashtagLL.setOnClickListener {
            var intent = Intent(activity, ProfileTagChangeActivity::class.java)
            startActivityForResult(intent, MODIFY_TAG)
        }

        myNeighbor.setOnClickListener {
            var intent = Intent(activity, MutualActivity::class.java)
            startActivity(intent)
        }

        setRegion.setOnClickListener {
            var intent = Intent(activity, AreaRangeActivity::class.java)
            intent.putExtra("region_type", "my_profile")
            startActivityForResult(intent, REGION_CHANGE)
        }

        btn_myPosts.setOnClickListener {
            val goIt = Intent(activity, MyPostMngActivity::class.java)
            startActivity(goIt)
        }

        btn_go_frd_mng.setOnClickListener {
            val goIt = Intent(activity, FriendManageActivity::class.java)
            startActivity(goIt)
        }

    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                SELECT_PROFILE -> {

                    data?.let {
                        try {
                            //var bitmap = uiHelper.decodeUri(this,it.data)
                        }catch (e:Exception) {

                        }
                    }
                    /*var cursor: Cursor? = null
                    val texts: ArrayList<Any> = ArrayList<Any>()

                    var uri = data?.data
                    try {
                        strPaths.add(MediaStore.Images.Media.DISPLAY_NAME)
                        println("image path ========= $strPaths")

                        var bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
                        //var bt: Bitmap = Utils.getImage(context!!.contentResolver, MediaStore.Images.Media.DISPLAY_NAME, 500)

                        var bo = BitmapFactory.Options()
                        bo.inSampleSize = 4
                        var tmpImg = BitmapFactory.decodeFile(context?.contentResolver.toString(), bo)

                        var smallBmImg = Bitmap.createScaledBitmap(tmpImg, 50, 50, true)
                        var resizedimg = Utils.getByteArray(smallBmImg)

                        //bitmap image to byteArray image
                        var bytearray_ = Utils.getByteArray(bitmap)

                        images.add(bytearray_)
                        smimages.add(resizedimg)

                        val nowTime = System.currentTimeMillis()

                        var imgPaths = ArrayList<String>()
                        var imagsPaths = ArrayList<String>()
                        var imgpath = ArrayList<String>()
                        var photo = Photo()

                        for (i in 0..(strPaths.size - 1)) {

                            var image_path = "imgl/" + i + nowTime + ".png"
                            var images_path = "imgs/" + i +nowTime + ".png"

                            strPathsL.add(image_path)
                            strPathsS.add(images_path)
                            imgpath.add(i.toString() + nowTime.toString() + ".png")
                        }

                        photo.type = "photo"
                        photo.file = imgpath

                        texts.add(photo)

                        var uid = PrefUtils.getStringPreference(context, "uid")

                        ProfileAction.viewContent(uid) { success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                            statusMessage = data!!.get("state_msg") as String

                            imgl = data!!.get("imgl") as ArrayList<String>
                            imgs = data!!.get("imgs") as ArrayList<String>
                            lastN = data!!.get("last") as Long
                            nick = data!!.get("nick") as String
                            sex = data!!.get("sex") as String
                            sTag = data!!.get("sharpTag") as ArrayList<String>

                        }
                        imgl = strPathsL
                        imgs = strPathsS

                        //이미지 firebase로 전송
                        //사실 그냥 uri를 putFile해도 됨

                            UploadTask uploadTask;
                            uploadTask = storageRef.putFile(file);

                        //여러 사진이 담긴 array list 를 전송해야하므로
                        val item = Users(imgl, imgs, lastN, nick, sex, sTag, statusMessage)

                        FirebaseFirestoreUtils.save("users", uid, item) {
                            if (it) {
                                FirebaseFirestoreUtils.uploadFile(bytearray_, "imgl/" + imgl) {
                                    if (it) {
                                        FirebaseFirestoreUtils.uploadFile(resizedimg, "imgs/" + imgs) {
                                            if (it) {

                                            }
                                        }
                                    }
                                }
                            } else {

                            }
                        }


                        //이미지 동그랗게
                        imgProfile.background = ShapeDrawable(OvalShape())
                        imgProfile.clipToOutline = true


                    } catch (e:Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            if (cursor != null && !cursor.isClosed) {
                                cursor.close()
                            }
                        } catch (ex: Exception) {
                        }

                    }

                    mngTXPhotoCnt.text = images.size.toString()*/
                }
                SELECT_STATUS -> {
                    getTempUserInformation("status_msg")
                }
                MODIFY_NAME -> {
                    getTempUserInformation("nick")
                }
                MODIFY_TAG -> {
                    getTempUserInformation("tag")

                }
                REGION_CHANGE -> {
                    getTempUserInformation("region")

                }
            }
        }

    }

    fun getTempUserInformation(type : String){

        var sttsMsg = ""
        var newNick = ""
        var newRegion = ArrayList<String>()
        var newRegionStr = ""
        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))

        MemberAction.get_member_info(params, object :JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    println("response : $response")
                    if (result == "ok") {
                        val member = response.getJSONObject("Member")
                        val memberTags = response.getJSONArray("tags")
                        sttsMsg = Utils.getString(member, "status_msg")
                        newNick = Utils.getString(member, "nick")
                        newRegion.clear()
                        var tmprg = Utils.getString(member,"region1")
                        if (tmprg != null){
                            newRegion.add(tmprg)
                            //rg1 = tmprg
                        }
                        tmprg = Utils.getString(member,"region2")
                        if (tmprg != null){
                            newRegion.add(tmprg)
                            //rg2 = tmprg
                        }
                        tmprg = Utils.getString(member,"region3")
                        if (tmprg != null){
                            newRegion.add(tmprg)
                            //rg3 = tmprg
                        }
                        for (i in 0 until newRegion.size){
                            newRegionStr += newRegion[i] + ","
                        }
                        println("newRegionStr $newRegionStr")



                        when(type){
                            "status_msg" -> {
                                infoStatusMsg.text = sttsMsg
                            }
                            "nick" -> {
                                txUserName.text = newNick
                            }
                            "region" -> {
                                txUserRegion.text = newRegionStr.substring(0,newRegionStr.length-2)
                            }
                            "tag" -> {
                                var taglist = ""
                                for (i in 0..memberTags.length()-1){
                                    val data = memberTags[i] as JSONObject
                                    taglist += "#${Utils.getString(data,"tag")} "
                                    println(taglist)
                                }
                                hashtagTV.text = taglist
                            }
                            else -> {

                            }
                        }

                    }

                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

            }
        })

    }

    fun doSomethingWithContext(context: Context) {
        // TODO: Actually do something with the context
        this.ctx = context
    }



}