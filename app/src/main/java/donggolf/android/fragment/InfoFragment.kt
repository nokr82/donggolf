package donggolf.android.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_mod_status_msg.*
import kotlinx.android.synthetic.main.activity_profile_manage.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
import java.lang.Exception

class InfoFragment : Fragment(){
    lateinit var myContext: Context
    private var progressDialog: ProgressDialog? = null


    val SELECT_PROFILE = 104
    val SELECT_STATUS = 105
    val MODIFY_NAME = 106
    val MODIFY_TAG = 107
    val REGION_CHANGE = 108

    private val GALLERY = 1
    internal var reloadReciver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                member_info()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)
        this.myContext = container!!.context
        progressDialog = ProgressDialog(myContext)
        if (null != myContext) {
            doSomethingWithContext(myContext)
        }


        return inflater.inflate(R.layout.activity_profile_manage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var filter1 = IntentFilter("REGION_CHANGE")
        myContext.registerReceiver(reloadReciver, filter1)

        var filter2 = IntentFilter("DELETE_IMG")
        myContext.registerReceiver(reloadReciver, filter2)


        member_info()

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(myContext))

        mychatFL.setOnClickListener {
            var intent = Intent()
            intent.action = "MY_CHATTING"
            myContext!!.sendBroadcast(intent)
        }

        //메뉴버튼
        tv_CONSEQUENCES.setOnClickListener {
            var intent = Intent(activity, OtherManageActivity::class.java)
            startActivity(intent)
        }

        addProfImg.setOnClickListener {
            choosePhotoFromGallary()
        }

        imgProfile.setOnClickListener {
            var intent = Intent(activity, ViewProfileListActivity::class.java)
            intent.putExtra("viewAlbumUser", PrefUtils.getIntPreference(context, "member_id"))
            startActivity(intent)
        }

        messageTV.setOnClickListener {
            var intent = Intent(activity, ModStatusMsgActivity::class.java)
            startActivityForResult(intent, SELECT_STATUS)
        }

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
            var intent = Intent(activity, AreaMyRangeActivity::class.java)
            intent.putExtra("region_type", "my_profile")
            startActivityForResult(intent, REGION_CHANGE)
        }

        btn_myPosts.setOnClickListener {
            val goIt = Intent(activity, MyPostMngActivity::class.java)
            startActivity(goIt)
        }

        btn_go_frd_mng.setOnClickListener {
            val intent = Intent(activity, FriendManageActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

fun member_info(){
    val params = RequestParams()
    params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

    MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject) {
            try {
                println("InfoFrag :: $response")
                val result = response.getString("result")

                if (result == "ok") {

                    val member = response.getJSONObject("Member")

                    val friendCount = response.getString("friendCount")
                    val contentCount = response.getString("contentCount")
                    val chatCount = response.getString("chatCount")

                    if (chatCount==null){
                        chatcountTV.setText("0")
                    }else{
                        chatcountTV.setText(chatCount)

                    }

                    postcountTV.setText(contentCount)
                    friendCountTV.setText(friendCount)

                    textDate.text = Utils.getString(member,"created").substringBefore(" ")
                    txUserName.text = Utils.getString(member,"nick")

                    //지역
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

                    /*       if (region.substring(region.length-1) == ","){
                               region = region.substring(0, region.length-2)
                           }*/
                    txUserRegion.text = region

                    //상메
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
                    mngTXPhotoCnt.text = imgData.length().toString()

                    //val tmpProfileImage = imgData.getJSONObject(0)
                    val img_uri = Utils.getString(member,"profile_img")//small_uri
                    val image = Config.url + img_uri

                    ImageLoader.getInstance().displayImage(image, imgProfile, Utils.UILoptionsProfile)

                }
            } catch (e : JSONException) {
                e.printStackTrace()
            }
        }

        override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject) {
            println(errorResponse.toString())
        }
    })

}

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                SELECT_PROFILE -> {
                    member_info()
                }
                SELECT_STATUS -> {
                    member_info()
                }
                MODIFY_NAME -> {
                    member_info()
                }
                MODIFY_TAG -> {
                    member_info()

                }
                REGION_CHANGE -> {
                    member_info()
                }
                GALLERY -> {
                    if (data != null)
                    {
                        val contentURI = data.data
                        Log.d("uri",contentURI.toString())
                        //content://media/external/images/media/1200

                        try
                        {
                            //갤러리에서 가져온 이미지를 프로필에 세팅
                            var thumbnail = MediaStore.Images.Media.getBitmap(myContext!!.contentResolver, contentURI)
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
                                    member_info()

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
                            Toast.makeText(myContext, "바꾸기실패", Toast.LENGTH_SHORT).show()
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
                                txUserRegion.text = newRegionStr.substring(0,newRegionStr.length-1)

                            }
                            "tag" -> {
                                var taglist = ""
                                for (i in 0..memberTags.length()-1){
                                    val data = memberTags[i] as JSONObject
                                    taglist += "#"+Utils.getString(data,"tag")
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
                                val inputStream = myContext!!.contentResolver.openInputStream(uri)
                                val btm = BitmapFactory.decodeStream(inputStream)
                                val resized = Utils.resizeBitmap(btm, 100)
                                imgProfile.setImageBitmap(resized)

                                //이미지 동그랗게
//                                imgProfile.background = ShapeDrawable(OvalShape())
//                                imgProfile.scaleType = ImageView.ScaleType.CENTER_CROP
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
        this.myContext = context
    }



}