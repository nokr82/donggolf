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
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.internal.InternalTokenResult
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.squareup.okhttp.internal.Util
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.MemberAction
import donggolf.android.actions.ProfileAction
import donggolf.android.activities.*
import donggolf.android.adapters.ImageAdapter
import donggolf.android.base.*
import donggolf.android.base.FirebaseFirestoreUtils.Companion.db
import donggolf.android.models.Content
import donggolf.android.models.Photo
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_findid.*
import kotlinx.android.synthetic.main.activity_mod_status_msg.*
import kotlinx.android.synthetic.main.activity_profile_manage.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
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


    private val GALLERY = 1

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
        ////

        /*val defaultOptions = DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(object : FadeInBitmapDisplayer(300){}).build()

        val config = ImageLoaderConfiguration.Builder(
                context)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(object : WeakMemoryCache(){})
                .discCacheSize(100 * 1024 * 1024).build()
*/
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(ctx))


        ////

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

                        textDate.text = Utils.getString(member,"created").substringBefore(" ")
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
                        val data = response.getJSONArray("MemberTags")
                        if (data != null) {
                            var string_tag = ""
                            for (i in 0 until data.length()) {
                                var json = data[i] as JSONObject
                                val memberTag = json.getJSONObject("MemberTag")

                                string_tag += "#" + Utils.getString(memberTag, "tag") + " "
                            }
                            hashtagTV.text = string_tag
                        }

                        //프로필 이미지
                        val imgData = response.getJSONArray("MemberImgs")
                        var txtImgCnt = imgData.length().toString()
                        mngTXPhotoCnt.text = txtImgCnt
                        /*val imgdata = response.getJSONArray("")
                        mngTXPhotoCnt.text*/
                        val images = response.getJSONArray("MemberImgs")
                        val json = images[0] as JSONObject
                        val img_uri = Utils.getString(json,"image_uri")
                        //var image = Config.url + image_uri
                        val image = Config.url + img_uri


                        ImageLoader.getInstance().displayImage(image, imgProfile, Utils.UILoptionsProfile)
                        //이미지 동그랗게
                        imgProfile.background = ShapeDrawable(OvalShape())
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

            choosePhotoFromGallary()

            /*var intent = Intent(activity, SelectProfileImgActivity::class.java)
            //var intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, SELECT_PROFILE)*/
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


    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)

    }



    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                SELECT_PROFILE -> {

                    getTempUserInformation("image")

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
                GALLERY -> {
                    if (data != null)
                    {
                        val contentURI = data!!.data
                        Log.d("uri",contentURI.toString())
                        //content://media/external/images/media/1200

                        try
                        {
                            var profileImg = MediaStore.Images.Media.getBitmap(ctx!!.contentResolver, contentURI)

                            val img = ByteArrayInputStream(Utils.getByteArray(profileImg))
                            //val small_img = Utils.resize(profileImg, 100)

                            val params = RequestParams()

                            params.put("files", img)
                            params.put("type", "image")
                            params.put("member_id",PrefUtils.getIntPreference(context, "member_id"))
                            //params.put("small", small_img)

                            MemberAction.update_info(params, object : JsonHttpResponseHandler() {
                                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                                    //imgProfile.setImageBitmap(thumbnail)
                                    getTempUserInformation("image")
                                }

                                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                                    println(responseString)
                                }
                                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                                    if (errorResponse != null)
                                        println(errorResponse!!.getString("message"))
                                }
                            })







                        }
                        catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(ctx, "바꾸기실패", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        }


    }

    fun getTempUserInformation(type : String) {

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
                        val memberTags = response.getJSONArray("MemberTags")
                        //val memberImgs = response.getJSONArray("MemberImgs")
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
                            "image" -> {
                                /*if (memberImgs.length() > 0) {
                                    val imgOb = memberImgs[0] as JSONObject
                                    val imguri = Utils.getString(imgOb, "image_uri")
                                    *//*val imgpath = Utils.getString(imgOb, "imgpath")
                                newImg = imgpath + imguri*//*
                                    newImg = imguri
                                    val imgUri = Uri.parse(newImg)

                                    imgProfile.setImageURI(imgUri)
                                    imgProfile.background = ShapeDrawable(OvalShape())
                                }*/
                                val images = response.getJSONArray("MemberImgs")
                                val json = images[0] as JSONObject
                                val img_uri = Utils.getString(json,"image_uri")
                                //var image = Config.url + image_uri
                                val image = Config.url + img_uri

                                val uri = Uri.parse(image)
                                val inputStream = ctx!!.contentResolver.openInputStream(uri)
                                val btm = BitmapFactory.decodeStream(inputStream)
                                val resized = Utils.resizeBitmap(btm, 100)
                                imgProfile.setImageBitmap(resized)

                                //ImageLoader.getInstance().displayImage(image, imgProfile, Utils.UILoptionsProfile)
                                //이미지 동그랗게
                                imgProfile.background = ShapeDrawable(OvalShape())
                                imgProfile.scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                        }

                    }

                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
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