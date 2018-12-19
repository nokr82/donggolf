package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.joooonho.SelectableRoundedImageView
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.MemberAction
import donggolf.android.actions.PostAction
import donggolf.android.base.*
import donggolf.android.models.*
import kotlinx.android.synthetic.main.activity_add_post.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Member
import java.util.*
import kotlin.collections.ArrayList


class AddPostActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var context: Context
    private val FROM_CAMERA: Int = 100
    private val SELECT_PICTURE: Int = 101
    private val SELECT_VIDEO: Int = 102
    private val SELECT_HASHTAG: Int = 103

    var result: ArrayList<String> = ArrayList<String>()
    private var imagesPaths: ArrayList<String> = ArrayList<String>()
    private var displaynamePaths: ArrayList<String> = ArrayList<String>()
    private var images: ArrayList<Bitmap> = ArrayList()
    private var videoPaths: ArrayList<String> = ArrayList<String>()
    private var videos: ArrayList<Bitmap>? = ArrayList()
    var hashtag: ArrayList<String> = ArrayList<String>()
    val user = HashMap<String, Any>()
    var userid: String? = null
    var tmpContent: TmpContent = TmpContent()

    var tmpImagesPath: ArrayList<ImagesPath> = ArrayList<ImagesPath>()

    var member_id = 0

    private var addPicturesLL: LinearLayout? = null

    private val imgSeq = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        context = this

        mAuth = FirebaseAuth.getInstance()

        val dbManager = DataBaseHelper(context)

        val db = FirebaseFirestore.getInstance()
        val dataList: Array<String> = arrayOf("*");

        val one = 1

        val setContent = TmpContent()

        addPicturesLL = findViewById(R.id.addPicturesLL)

        member_id = PrefUtils.getIntPreference(context, "member_id")
        println("member_id ----- $member_id")

        if (setContent != null) {
            titleET.setText(setContent.title)
            contentET.setText(setContent.texts)
        }

        loadData(dbManager,member_id.toString())

        permission()

        println("displayname " + displaynamePaths.size.toString())

        val category = intent.getIntExtra("category", 0)

        if (category == 2) {
            addpostTV.text = "수정하기"
            val id = intent.getStringExtra("id")
        }

        finishaBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("글쓰기를 취소하시겠습니까 ?")

                    .setPositiveButton("유지하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        val title = Utils.getString(titleET)
                        val content = Utils.getString(contentET)

                        val tmpContent = TmpContent(0, member_id.toString(), title, content)

                        dbManager.inserttmpcontent(tmpContent)

                        if (imagesPaths != null && imagesPaths.size > 0 ) {
                            for (i in 0 until imagesPaths.size){
                                val imagesPath = ImagesPath(0,member_id.toString(),imagesPaths.get(i),1)
                                println("imagesPath ${imagesPath.path}")
                                dbManager.insertimagespath(imagesPath)
                            }
                        }

                        if (videoPaths != null && videoPaths.size > 0 ) {
                            for (i in 0 until videoPaths.size){
                                val videoPath = ImagesPath(0,member_id.toString(),videoPaths.get(i),2)
                                println("videoPath ${videoPath.path}")
                                dbManager.insertimagespath(videoPath)
                            }
                        }

                        if (hashtag != null && hashtag.size > 0 ) {
                            for (i in 0 until hashtag.size){
                                val hastag = ImagesPath(0,member_id.toString(),hashtag.get(i),3)
                                println("hastag ${hastag.path}")
                                dbManager.insertimagespath(hastag)
                            }
                        }

                        finish()

                    })
                    .setNegativeButton("삭제하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()

                        loadData(dbManager,member_id.toString())

                        if(tmpContent.id == null){
                            finish()
                        }

                        if (tmpImagesPath != null && tmpImagesPath.size > 0 ){
                            dbManager.deleteImagePaths(member_id.toString())
                        }

                        if(tmpContent.id != null) {
                            dbManager.deleteTmpContent(tmpContent.id!!)
                            finish()
                        }


                    })
            val alert = builder.create()
            alert.show()
        }

        movefindpictureBT.setOnClickListener {
            moveMyPicture()
        }

        movefindvideoBT.setOnClickListener {
            moveMyVideo()
        }

        addcontentBT.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("등록하시겠습니까 ?")

                    .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()


                        if (category == 1) {
                            addContent()
                        } else {
                            val id = intent.getStringExtra("id")
                            modify(id)

//                            intent = Intent()
//                            intent.action = "UPDATE_POST"
//                            sendBroadcast(intent)

                            finish()

                        }

                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        finish()
                    })

            val alert = builder.create()
            alert.show()

        }

        hashtagLL.setOnClickListener {
            var intent = Intent(context, ProfileTagChangeActivity::class.java);
            intent.putExtra("type",1)
            startActivityForResult(intent, SELECT_HASHTAG);
        }

    }

    private fun loadData(dbManager: DataBaseHelper , userid: String) {

        //임시저장 데이터 불러오기
        val query = "SELECT * FROM tmpcontent WHERE owner ='" + userid + "'"

        val imagespathquery = "SELECT * FROM imagespath WHERE owner ='" + userid + "'"

        val tmpcontent = dbManager.selectTmpContent(query)

        var tmpImagesPath = dbManager.selectImagesPath(imagespathquery)

        if (tmpImagesPath != null && tmpImagesPath.size > 0){
            for (i in 0 until tmpImagesPath.size){
                if (tmpImagesPath.get(i).type == 1){
                    imagesPaths.add(tmpImagesPath.get(i).path!!)
                } else if (tmpImagesPath.get(i).type == 2){
                    videoPaths.add(tmpImagesPath.get(i).path!!)
                } else if (tmpImagesPath.get(i).type == 3) {
                    hashtag.add(tmpImagesPath.get(i).path!!)
                }
            }
        }

        tmpContent = tmpcontent
        this.tmpImagesPath = tmpImagesPath

        titleET.setText(tmpcontent.title)
        contentET.setText(tmpcontent.texts)

        getPost()

    }

    private fun moveMyPicture() {
        var intent = Intent(context, FindPictureGridActivity::class.java);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    private fun moveMyVideo() {
        var intent = Intent(context, FindVideoActivity::class.java);
        startActivityForResult(intent, SELECT_VIDEO);
    }

    private fun permission() {

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {

            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {

            }

        }

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

    }

    private fun modify(id: String) {
        val title = Utils.getString(titleET)
        if (title.isEmpty()) {
            Utils.alert(context, "제목을 입력해주세요.")
            return
        }

        var text = Utils.getString(contentET)
        if (text.isEmpty()) {
            Utils.alert(context, "내용을 입력해주세요.")
            return
        }

        val params = RequestParams()
        params.put("content_id",id)
        val login_id = PrefUtils.getIntPreference(context, "member_id")
        params.put("member_id",login_id)
        params.put("title",title)
        params.put("text",text)

        var cht_yn = "Y"
        if (replyableCB.isChecked == false){
            cht_yn = "N"
        }
        params.put("cht_yn",cht_yn)

        var cmt_yn = "Y"
        if (chatableCB.isChecked == false){
            cmt_yn = "N"
        }
        params.put("cmt_yn",cmt_yn)

        if (hashtag != null){
            for (i in 0 .. hashtag.size - 1){
                params.put("tag[" + i + "]",  hashtag.get(i))
            }
        }

        if (displaynamePaths != null){
            if (displaynamePaths.size != 0){
                for (i in 0..displaynamePaths.size - 1){
                    var bt: Bitmap = Utils.getImage(context.contentResolver, displaynamePaths.get(i), 800)

                    params.put("files[" + i + "]",  ByteArrayInputStream(Utils.getByteArray(bt)))
                }
            }
        }

        PostAction.update_post(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                finish()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
            }

        })

    }

    private fun addContent() {

        val title = Utils.getString(titleET)
        if (title.isEmpty()) {
            Utils.alert(context, "제목을 입력해주세요.")
            return
        }

        var text = Utils.getString(contentET)
        if (text.isEmpty()) {
            Utils.alert(context, "내용을 입력해주세요.")
            return
        }

        val params = RequestParams()
        params.put("member_id",member_id)
        params.put("title",title)
        params.put("text",text)
        params.put("deleted","N")

        var cht_yn = "Y"
        if (replyableCB.isChecked == false){
            cht_yn = "N"
        }
        params.put("cht_yn",cht_yn)

        var cmt_yn = "Y"
        if (chatableCB.isChecked == false){
            cmt_yn = "N"
        }
        params.put("cmt_yn",cmt_yn)

        if (hashtag != null){
            for (i in 0 .. hashtag.size - 1){
                params.put("tag[" + i + "]",  hashtag.get(i))
            }
        }

        if (displaynamePaths != null){
            if (displaynamePaths.size != 0){
                for (i in 0..displaynamePaths.size - 1){
                    var bt: Bitmap = Utils.getImage(context.contentResolver, displaynamePaths.get(i), 800)

                    params.put("files[" + i + "]",  ByteArrayInputStream(Utils.getByteArray(bt)))
                }
            }
        }

        if (videoPaths != null){
            if (videoPaths.size != 0){
                for (i in 0..videoPaths.size - 1){


                    val baos = ByteArrayOutputStream()

//                    val fis = FileInputStream(File(videoPaths.get(i)))
//                    val buf = ByteArray(1024)
//                    var n: Int
//                    while ((bytesRead = inputStream.read(b)) != -1)
//                    {
//                        bos.write(b, 0, bytesRead);
//                    }
//
//                    videoBytes = baos.toByteArray()
//
//                    var bt: Bitmap = Utils.getImage(context.contentResolver, videoPaths.get(i), 800)
//
//                    params.put("videos[" + i + "]",  ByteArrayInputStream(Utils.getByteArray(bt)))

                }
            }
        }

        PostAction.add_post(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                finish()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
            }

        })

        val dbManager = DataBaseHelper(context)

        loadData(dbManager,member_id.toString())

        if(tmpContent.id == null){
            finish()
        }

        if(tmpContent.id != null) {
            dbManager.deleteTmpContent(tmpContent.id!!)
            finish()
        }

        if (tmpImagesPath != null && tmpImagesPath.size > 0 ){
            dbManager.deleteImagePaths(member_id.toString())
        }

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

                        imagesPaths.add(str)

                        Log.d("yjs", "Pathi : " + imagesPaths.get(0))


                        val add_file = Utils.getImage(context.contentResolver, str, 10)

                        if (images?.size == 0) {

                            images?.add(add_file)

                        } else {
                            try {
                                images?.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images?.add(add_file)
                            }

                        }

                    }

                    val child = addPicturesLL!!.getChildCount()
                    for (i in 0 until child) {

                        println("test : $i")

                        val v = addPicturesLL!!.getChildAt(i)

                        val delIV = v.findViewById(R.id.delIV) as ImageView

                    }

                    displaynamePaths.clear()
                    for (i in 0..(name!!.size - 1)) {
                        val str = name[i]

                        if (displaynamePaths != null) {
                            displaynamePaths.add(str)

                            Log.d("yjs", "display " + displaynamePaths.get(0))
                            Log.d("yjs", "display " + displaynamePaths.get(0))
                        } else {
                            displaynamePaths.add(str)
                            Log.d("yjs", "display " + displaynamePaths.get(0))
                        }

                    }



                    var intent = Intent();

                    println("display : ======== $displaynamePaths")

                    Log.d("yjs", "PostResult : " + item?.size.toString() + " : " + item?.get(0).toString())

                    setResult(RESULT_OK, intent);

                }
                SELECT_VIDEO -> {
                    var item = data?.getStringArrayExtra("videos")
                    var name = data?.getStringArrayExtra("displayname")

                    for (i in item!!.indices) {
                        val str = item[i]

                        videoPaths?.add(str)

                        val add_file = Utils.getImage(context.contentResolver, str, 10)

                        if (videos?.size == 0) {
                            videos?.add(add_file)
                        } else {
                            try {
                                videos?.set(videos!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                videos?.add(add_file)
                            }

                        }

                    }


                    videoPaths.clear()
                    for (i in 0..(name!!.size - 1)) {
                        val str = name[i]

                        if (videoPaths != null) {
                            videoPaths.add(str)
                        } else {
                            videoPaths.add(str)
                        }

                    }

                    var intent = Intent();

                    Log.d("yjs", "Video : " + item?.size.toString() + "name : " + videoPaths.toString())

                    setResult(RESULT_OK, intent);

                }

                SELECT_HASHTAG -> {

                    if (data?.getStringArrayListExtra("data") != null) {
                        hashtag = data?.getStringArrayListExtra("data") as ArrayList<String>

                        println("tmpcontent : $hashtag")
                    }

                }
            }
        }
    }

    fun getPost(){
        //게시글 불러오기
        if (intent.getStringExtra("id") != null){
            val id = intent.getStringExtra("id")
            val login_id = PrefUtils.getIntPreference(context, "member_id")

            var params = RequestParams()
            params.put("id",id)
            params.put("member_id",login_id)

            PostAction.get_post(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    try {
                        val result = response!!.getString("result")
                        if (result == "ok") {

                            val data = response.getJSONObject("Content")

                            val title = Utils.getString(data,"title")
                            val text = Utils.getString(data,"text")
                            val member_id = Utils.getString(data,"member_id")
                            val cht_yn = Utils.getString(data,"cht_yn")
                            val cmt_yn = Utils.getString(data,"cmt_yn")

                            val tags = response.getJSONArray("tags")
                            val imageDatas = response.getJSONArray("ContentImgs")

                            if (tags != null && tags.length() > 0 ){
                                var hashtags: String = ""

                                for (i in 0 until tags.length()){
                                    var json = tags.get(i) as JSONObject
                                    var MemberTags = json.getJSONObject("MemberTags")
                                    val division = Utils.getString(MemberTags,"division")

                                    if (division == "1"){
                                        val tag = Utils.getString(MemberTags,"tag")
                                        hashtags += "#"+tag + "  "
                                        hashtag.add(tag)
                                    }
                                }

                            }

                            if (hashtag != null){

                            }

                            if (imageDatas != null && imageDatas.length() > 0){
                                var imagePaths: java.util.ArrayList<String> = java.util.ArrayList<String>()

                                for (i in 0 until imageDatas.length()){
                                    var json = imageDatas.get(i) as JSONObject

                                    var contentFile = json.getJSONObject("contentFile")
                                    var type = Utils.getInt(contentFile,"type")
                                    if (type == 1) {
                                        val path = Utils.getString(contentFile, "image_uri")
                                        imagePaths.add(path)
                                        displaynamePaths.add(path)
                                    }
                                }
                            }

                            if (displaynamePaths != null){

                            }
                            titleET.setText(title)
                            contentET.setText(text)

                            if (cht_yn  == "Y"){
                                chatableCB.isChecked = true
                            } else {
                                chatableCB.isChecked = false
                            }

                            if (cmt_yn == "Y"){
                                replyableCB.isChecked = true
                            } else {
                                replyableCB.isChecked = false
                            }

                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                }
            })

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
}



