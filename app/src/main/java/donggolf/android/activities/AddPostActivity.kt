/*
package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
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
import com.kakao.kakaostory.StringSet.likes
import com.kakao.kakaostory.StringSet.writer
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import de.hdodenhof.circleimageview.CircleImageView
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
    private var displaynamePaths: ArrayList<String> = ArrayList<String>()
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

    var pk : String? = null
    var images_path: ArrayList<String> = ArrayList<String>()
    var modi_path: ArrayList<String> = ArrayList<String>()
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        context = this

//        mAuth = FirebaseAuth.getInstance()

        val dbManager = DataBaseHelper(context)

//        val db = FirebaseFirestore.getInstance()
        val dataList: Array<String> = arrayOf("*");

        val one = 1

        val setContent = TmpContent()

        addPicturesLL = findViewById(R.id.addPicturesLL)

//        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        member_id = PrefUtils.getIntPreference(context, "member_id")
        println("member_id ----- $member_id")

        if (setContent != null) {
            titleET.setText(setContent.title)
            contentET.setText(setContent.texts)
        }

//        loadData(dbManager,member_id.toString())

        permission()

        println("displayname " + displaynamePaths.size.toString())

        val category = intent.getIntExtra("category", 0)

        if (category == 2) {
            addpostTV.text = "수정하기"
            val id = intent.getStringExtra("id")
            getPost()
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

                        if (images_path != null && images_path!!.size > 0 ) {
                            for (i in 0 until images_path!!.size){
                                val imagesPath = ImagesPath(0,member_id.toString(),images_path!!.get(i),1)
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
            intent.putExtra("type","post")
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
                    images_path!!.add(tmpImagesPath.get(i).path!!)
                } else if (tmpImagesPath.get(i).type == 2){
                    videoPaths.add(tmpImagesPath.get(i).path!!)
                } else if (tmpImagesPath.get(i).type == 3) {
                    hashtag.add(tmpImagesPath.get(i).path!!)
                }
            }
        }

        if (images_path!!.size > 0 ){
            for (i in 0..(images_path!!.size - 1)) {
                val str = images_path!![i]

                val add_file = Utils.getImage(context.contentResolver, str)

                if (images?.size == 0) {

                    images?.add(add_file)

                } else {
                    try {
                        images?.set(images!!.size, add_file)
                    } catch (e: IndexOutOfBoundsException) {
                        images?.add(add_file)
                    }

                }

                reset(str, i)

            }

            val child = addPicturesLL!!.getChildCount()
            for (i in 0 until child) {

                println("test : $i")

                val v = addPicturesLL!!.getChildAt(i)

                val delIV = v.findViewById(R.id.delIV) as ImageView

            }
        }

        if (hashtag.size > 0 ){
            var tag = ""
            for (i in 0 until hashtag.size) {
                tag += "#" + hashtag.get(i) + "  "
            }
            hashtagsTV.setText(tag)
        }

        tmpContent = tmpcontent
        this.tmpImagesPath = tmpImagesPath

        titleET.setText(tmpcontent.title)
        contentET.setText(tmpcontent.texts)


        getPost()
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


        if (modi_path != null){
            Log.d("수정",modi_path.toString())
            if (modi_path.size != 0){
                for (i in 0..modi_path.size - 1){
                    params.put("files[" + i + "]",  modi_path[i])
                }
            }
        }


        if (images_path != null){
            Log.d("수정",images_path.toString())
            if (images_path.size != 0){
                for (i in 0..images_path.size - 1){
                    var bt: Bitmap = Utils.getImage(context.contentResolver, images_path.get(i))


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

        if (images_path != null){
            Log.d("작성",images_path.toString())
            if (images_path!!.size != 0){
                for (i in 0..images_path!!.size - 1){
                    var bt: Bitmap = Utils.getImage(context.contentResolver, images_path!!.get(i))

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
        var add_file = Utils.getImage(context.contentResolver, str)
        val bitmap = BitmapFactory.decodeFile(str)
        val v = View.inflate(context, R.layout.item_add_image, null)
        val imageIV = v.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
        val delIV = v.findViewById<View>(R.id.delIV) as ImageView
        imageIV.setImageBitmap(add_file)
        delIV.tag = i

        if (imgSeq == 0) {
            addPicturesLL!!.addView(v)
        }

    }

    fun reset2(str: String, i: Int) {
//        val options = BitmapFactory.Options()
//        options.inJustDecodeBounds = true
//        BitmapFactory.decodeFile(str, options)
//        options.inJustDecodeBounds = false
//        options.inSampleSize = 1
//        if (options.outWidth > 96) {
//            val ws = options.outWidth / 96 + 1
//            if (ws > options.inSampleSize) {
//                options.inSampleSize = ws
//            }
//        }
//        if (options.outHeight > 96) {
//            val hs = options.outHeight / 96 + 1
//            if (hs > options.inSampleSize) {
//                options.inSampleSize = hs
//            }
//        }
        var add_file = Utils.getImage(context.contentResolver, str)
        val bitmap = BitmapFactory.decodeFile(str)
        val v = View.inflate(context, R.layout.item_add_image, null)
        val imageIV = v.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
        val delIV = v.findViewById<View>(R.id.delIV) as ImageView
        ImageLoader.getInstance().displayImage(str,imageIV, Utils.UILoptionsUserProfile)
        delIV.tag = i

        if (imgSeq == 0) {
            addPicturesLL!!.addView(v)
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
                                        var path = Config.url + Utils.getString(contentFile, "image_uri")
                                        reset2(path,i)
                                        Log.d("이미지",path)
                                        imagePaths.add(path)
                                        modi_path!!.add(Utils.getString(contentFile, "image"))
                                    }
                                }
                            }

                            if (images_path != null){

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

                        val add_file = Utils.getImage(context.contentResolver, str)

                        if (images?.size == 0) {

                            images?.add(add_file)

                        } else {
                            try {
                                images?.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images?.add(add_file)
                            }

                        }

                        reset(str, i)

                    }

                    val child = addPicturesLL!!.getChildCount()
                    for (i in 0 until child) {

                        val v = addPicturesLL!!.getChildAt(i)

                        val delIV = v.findViewById(R.id.delIV) as ImageView

                    }

                    displaynamePaths.clear()
                    for (i in 0..(name!!.size - 1)) {
                        val str = name[i]

                        if (displaynamePaths != null) {
                            displaynamePaths.add(str)
                        } else {
                            displaynamePaths.add(str)
                        }

                    }

                    var intent = Intent();

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
                        var tag = ""
                        if (hashtag.size > 0){
                            for (i in 0 until hashtag.size){
                                tag += "#"+hashtag.get(i) + " "
                            }
                            hashtagsTV.setText(tag)
                        }

                    }

                }
            }
        }
    }



    fun clickMethod(v: View) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                    addPicturesLL!!.removeAllViews()
                    images!!.clear()
                    val tag = v.tag as Int
                    images_path!!.removeAt(tag)

                    for (k in images_url!!.indices) {
                        val vv = View.inflate(context, R.layout.item_add_image, null)
                        val imageIV = vv.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
                        val delIV = vv.findViewById<View>(R.id.delIV) as ImageView
                        delIV.visibility = View.GONE
                        val del2IV = vv.findViewById<View>(R.id.del2IV) as ImageView
                        del2IV.visibility = View.VISIBLE
                        del2IV.tag = k
//                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        if (imgSeq == 0) {
                            addPicturesLL!!.addView(vv)
                        }
                    }
                    for (j in images_path!!.indices) {

                        val paths = images_path!!.get(j).split("/")
                        val file_name = paths.get(paths.size - 1)
                        val getPk = file_name.split("_")
                        val pathPk = getPk.get(0)
                        val add_file = Utils.getImage(context!!.getContentResolver(), images_path!!.get(j))
                        if (images!!.size == 0) {
                            images!!.add(add_file)
                        } else {
                            try {
                                images!!.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images!!.add(add_file)
                            }
                        }
                        reset2(images_path!!.get(j), j)
                    }
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()
    }

    fun clickMethod2(v: View) {
        println("------------click2")
        val builder = AlertDialog.Builder(context)
        builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                    addPicturesLL!!.removeAllViews()
                    val tag = v.tag as Int
                    images_url!!.removeAt(tag)
                    images_url_remove!!.add(images_id!!.get(tag).toString())
                    images_id!!.removeAt(tag)

                    for (k in images_url!!.indices) {
                        val vv = View.inflate(context, R.layout.item_add_image, null)
                        val imageIV = vv.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
                        val delIV = vv.findViewById<View>(R.id.delIV) as ImageView
                        delIV.visibility = View.GONE
                        val del2IV = vv.findViewById<View>(R.id.del2IV) as ImageView
                        del2IV.visibility = View.VISIBLE
                        del2IV.tag = k
                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        if (imgSeq == 0) {
                            addPicturesLL!!.addView(vv)
                        }
                    }
                    for (j in images_path!!.indices) {

                        val paths = images_path!!.get(j).split("/")
                        val file_name = paths.get(paths.size - 1)
                        val getPk = file_name.split("_")
                        val pathPk = getPk.get(0)
                        val add_file = Utils.getImage(context!!.getContentResolver(), images_path!!.get(j))
                        if (images!!.size == 0) {
                            images!!.add(add_file)
                        } else {
                            try {
                                images!!.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images!!.add(add_file)
                            }
                        }
                        reset(images_path!!.get(j), j)
                    }

                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()

    }
}



*/
package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.PostAction
import donggolf.android.base.*
import donggolf.android.models.*
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.item_addgoods.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
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
    private var displaynamePaths: ArrayList<String> = ArrayList<String>()
    private var videoPaths: ArrayList<String> = ArrayList<String>()
    private var videos: ArrayList<Bitmap>? = ArrayList()
    var hashtag: ArrayList<String> = ArrayList<String>()
    val user = HashMap<String, Any>()
    var userid: String? = null
    var tmpContent: TmpContent = TmpContent()
    var delids = ArrayList<Int>()
    var tmpImagesPath: ArrayList<ImagesPath> = ArrayList<ImagesPath>()

    var member_id = 0

    private var addPicturesLL: LinearLayout? = null

    private val imgSeq = 0

    var pk : String? = null
    var images_path: ArrayList<String> = ArrayList<String>()
    var modi_path: ArrayList<String> = ArrayList<String>()
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null
    var selectedImageViewList:ArrayList<String> = ArrayList<String>()

    var video_image:ArrayList<String> = ArrayList<String>()

    lateinit var videofile:ByteArray

    var MODIFYY = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        context = this

//        mAuth = FirebaseAuth.getInstance()

        val dbManager = DataBaseHelper(context)

//        val db = FirebaseFirestore.getInstance()
        val dataList: Array<String> = arrayOf("*");

        val one = 1

        val setContent = TmpContent()

      /*  intent = getIntent()
        selectedImageViewList = intent.getStringArrayListExtra("image_uri")
        Log.d("선택된이미지",selectedImageViewList.lastIndex.toString())
        for (i in 0 until selectedImageViewList.lastIndex){
            var path = Config.url + selectedImageViewList[i]
            reset2(path,i)

        }*/


        addPicturesLL = findViewById(R.id.addPicturesLL)

        if (intent.getStringArrayListExtra("image_uri") != null){
            selectedImageViewList = intent.getStringArrayListExtra("image_uri")
            println("images---------selectimage$selectedImageViewList")
            if (selectedImageViewList.size > 0){
                for (i in 0 until selectedImageViewList.size){
                    reset2(selectedImageViewList.get(i),i)
                }
            }
        }

//        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        member_id = PrefUtils.getIntPreference(context, "member_id")

        if (setContent != null) {
            titleET.setText(setContent.title)
            contentET.setText(setContent.texts)
        }

        videoVV.setOnPreparedListener { mp -> mp.isLooping = true }
        var mediaController: MediaController = MediaController(this);
        videoVV.setMediaController(mediaController)

        removeIV.setOnClickListener {
            videoVV.visibility = View.GONE
            removeIV.visibility = View.GONE
            videoPaths.clear()
            video_image.clear()
        }

//        loadData(dbManager,member_id.toString())

        permission()

        val category = intent.getIntExtra("category", 0)

        if (category == 2) {
            addpostTV.text = "수정하기"
            val id = intent.getStringExtra("id")
            getPost()
        }

        finishaBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("글쓰기를 취소하시겠습니까 ?")

                    .setPositiveButton("유지하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
//                        val title = Utils.getString(titleET)
//                        val content = Utils.getString(contentET)
//
//                        val tmpContent = TmpContent(0, member_id.toString(), title, content)
//
//                        dbManager.inserttmpcontent(tmpContent)
//
//                        if (images_path != null && images_path!!.size > 0 ) {
//                            for (i in 0 until images_path!!.size){
//                                val imagesPath = ImagesPath(0,member_id.toString(),images_path!!.get(i),1)
//                                println("imagesPath ${imagesPath.path}")
//                                dbManager.insertimagespath(imagesPath)
//                            }
//                        }
//
//                        if (videoPaths != null && videoPaths.size > 0 ) {
//                            for (i in 0 until videoPaths.size){
//                                val videoPath = ImagesPath(0,member_id.toString(),videoPaths.get(i),2)
//                                println("videoPath ${videoPath.path}")
//                                dbManager.insertimagespath(videoPath)
//                            }
//                        }
//
//                        if (hashtag != null && hashtag.size > 0 ) {
//                            for (i in 0 until hashtag.size){
//                                val hastag = ImagesPath(0,member_id.toString(),hashtag.get(i),3)
//                                println("hastag ${hastag.path}")
//                                dbManager.insertimagespath(hastag)
//                            }
//                        }



                        finish()

                    })
                    .setNegativeButton("삭제하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()

//                        loadData(dbManager,member_id.toString())
//
//                        if(tmpContent.id == null){
//                            finish()
//                        }
//
//                        if (tmpImagesPath != null && tmpImagesPath.size > 0 ){
//                            dbManager.deleteImagePaths(member_id.toString())
//                        }
//
//                        if(tmpContent.id != null) {
//                            dbManager.deleteTmpContent(tmpContent.id!!)
//                            finish()
//                        }


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
            intent.putExtra("type","post")
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
                    images_path!!.add(tmpImagesPath.get(i).path!!)
                } else if (tmpImagesPath.get(i).type == 2){
                    videoPaths.add(tmpImagesPath.get(i).path!!)
                } else if (tmpImagesPath.get(i).type == 3) {
                    hashtag.add(tmpImagesPath.get(i).path!!)
                }
            }
        }

        if (images_path!!.size > 0 ){
            for (i in 0..(images_path!!.size - 1)) {
                val str = images_path!![i]

                val add_file = Utils.getImage(context.contentResolver, str)

                if (images?.size == 0) {

                    images?.add(add_file)

                } else {
                    try {
                        images?.set(images!!.size, add_file)
                    } catch (e: IndexOutOfBoundsException) {
                        images?.add(add_file)
                    }

                }

                reset(str, i)

            }

            val child = addPicturesLL!!.getChildCount()
            for (i in 0 until child) {

                println("test : $i")

                val v = addPicturesLL!!.getChildAt(i)

                val delIV = v.findViewById(R.id.delIV) as ImageView

            }
        }

        if (hashtag.size > 0 ){
            var tag = ""
            for (i in 0 until hashtag.size) {
                tag += "#" + hashtag.get(i) + "  "
            }
            hashtagsTV.setText(tag)
        }

        tmpContent = tmpcontent
        this.tmpImagesPath = tmpImagesPath

        titleET.setText(tmpcontent.title)
        contentET.setText(tmpcontent.texts)

        getPost()
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


        if (delids!=null){
            Log.d("삭제",delids.toString())
            if (delids.size != 0){
                for (i in 0..delids.size - 1){
                    params.put("del_ids["+i+"]",  delids[i])
                }
            }
        }

        var seq = 0
        if (addPicturesLL != null){
            for (i in 0 until addPicturesLL!!.childCount) {
                val v = addPicturesLL?.getChildAt(i)
                val imageIV = v?.findViewById<ImageView>(R.id.addedImgIV)
                if (imageIV is ImageView) {
                    val bitmap = imageIV.drawable as BitmapDrawable
                    params.put("files[$seq]", ByteArrayInputStream(Utils.getByteArray(bitmap.bitmap)))
                    seq++
                    println("add-------------$seq")
                }
            }
        }

//        if (images_path != null){
//            Log.d("작성",images_path.toString())
//            if (images_path!!.size != 0){
//                for (i in 0..images_path!!.size - 1){
//
//                    var bt: Bitmap = Utils.getImage(context.contentResolver, images_path!!.get(i))
//
//                    params.put("files[" + i + "]",  ByteArrayInputStream(Utils.getByteArray(bt)))
//                }
//            }
//        }




        if (video_image.size > 0){
            params.put("video_delete", "delete")
         } else {
            if (videoPaths != null){
                if (videoPaths.size != 0){
                    for (i in 0..videoPaths.size - 1){

                        val file = File(videoPaths.get(i))
                        var bytes = file.readBytes()

                        var n: Int
                        val baos = ByteArrayOutputStream()
                        val videoBytes = baos.toByteArray()

                        params.put("videos[" + i + "]",  ByteArrayInputStream(bytes))
                    }
                }
            }
        }

        PostAction.update_post(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    var intent = Intent()
                    intent.putExtra("reset", "reset")
                    intent.putExtra("id", intent.getStringExtra("id"))
                    setResult(RESULT_OK, intent);
                    finish()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
//                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
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

        if (PrefUtils.getStringPreference(context,"region_id") != null) {
            var region_id = PrefUtils.getStringPreference(context, "region_id")
            params.put("region", region_id)
        } else {
            var region_id = 1
            params.put("region", region_id)
        }

        if (PrefUtils.getStringPreference(context,"region_id2") != null) {
            var region_id = PrefUtils.getStringPreference(context, "region_id2")
            params.put("region2", region_id)
        } else {
            var region_id = 1
            params.put("region2", region_id)
        }

        if (hashtag != null){
            for (i in 0 .. hashtag.size - 1){
                params.put("tag[" + i + "]",  hashtag.get(i))
            }
        }


//        var seq = 0
//        if (addPicturesLL != null){
//            println("------------------addpicture")
//            for (i in 0 until addPicturesLL!!.childCount) {
//                val v = addPicturesLL!!.getChildAt(i)
//                val imageIV : SelectableRoundedImageView = v!!.findViewById(R.id.imageIV)
//                if (imageIV is SelectableRoundedImageView) {
//                    val bitmap = imageIV.resolveResource()
//                    params.put("files[$seq]", ByteArrayInputStream(Utils.getByteArray(bitmap.bitmap)))
//                    seq++
//                    println("------seq----$seq")
//                }
//            }
//        }

//        if (images_path != null){
//            Log.d("작성",images_path.toString())
//            if (images_path!!.size != 0){
//                for (i in 0..images_path!!.size - 1){
//
//                    var bt: Bitmap = Utils.getImage(context.contentResolver, images_path!!.get(i))
//
//                    params.put("files[" + i + "]",  ByteArrayInputStream(Utils.getByteArray(bt)))
//                }
//            }
//        }

        var seq = 0
        if (addPicturesLL != null){
            for (i in 0 until addPicturesLL!!.childCount) {
                val v = addPicturesLL?.getChildAt(i)
                val imageIV = v?.findViewById<ImageView>(R.id.addedImgIV)
                if (imageIV is ImageView) {
                    val bitmap = imageIV.drawable as BitmapDrawable
                    params.put("files[$seq]", ByteArrayInputStream(Utils.getByteArray(bitmap.bitmap)))
                    seq++
                    println("add-------------$seq")
                }
            }
        }

        if (videoPaths != null){
            if (videoPaths.size != 0){
                for (i in 0..videoPaths.size - 1){

                    val file = File(videoPaths.get(i))
                    println("----------videoPath ${videoPaths.get(i)}")
                    println("file ---- ${file.length()}")
//                    val size = file.length() as Int
//                    println("size ---- $size")
//                    val bytes = ByteArray(size)
                    var bytes = file.readBytes()
                    println("bytes ---- $bytes")

//                    try {
//                        val buf = BufferedInputStream( FileInputStream(file));
//                        buf.read(bytes, 0, bytes.size);
//                        buf.close();
//                        bytes = Utils.getByteArray(buf)
//                    } catch (e:FileNotFoundException){
//
//                    } catch (e:IOException){
//
//                    }
                    var n: Int
                    val baos = ByteArrayOutputStream()
                    val videoBytes = baos.toByteArray()

//                    params.put("videos[" + i + "]",  ByteArrayInputStream(videofile))
                    params.put("videos[" + i + "]",  ByteArrayInputStream(bytes))
                }
            }
        }

        PostAction.add_post(params,object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                var intent = Intent()
                intent.putExtra("reset","reset")
                setResult(RESULT_OK, intent);
                finish()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
//                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
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
        var add_file = Utils.getImage(context.contentResolver, str)
        val bitmap = BitmapFactory.decodeFile(str)
//        val v = View.inflate(context, R.layout.item_add_image, null)
//        val imageIV = v.findViewById(R.id.imageIV) as SelectableRoundedImageView
//        val delIV = v.findViewById<View>(R.id.delIV) as ImageView

        var v = View.inflate(context, R.layout.item_addgoods, null)
        val imageIV = v.findViewById(R.id.addedImgIV) as ImageView
        val delIV = v.findViewById<View>(R.id.delIV) as ImageView
//        ImageLoader.getInstance().displayImage(str,v.addedImgIV, Utils.UILoptionsUserProfile)
        imageIV.setImageBitmap(add_file)
        delIV.tag = i

        delIV.setOnClickListener {
            addPicturesLL!!.removeView(v)
            if (images_path!=null){
                images_path!!.removeAt(i)
            }

        }

        if (imgSeq == 0) {
            addPicturesLL!!.addView(v)
        }

    }

    fun reset2(str: String, i: Int) {
//        val options = BitmapFactory.Options()
//        options.inJustDecodeBounds = true
//        BitmapFactory.decodeFile(str, options)
//        options.inJustDecodeBounds = false
//        options.inSampleSize = 1
//        if (options.outWidth > 96) {
//            val ws = options.outWidth / 96 + 1
//            if (ws > options.inSampleSize) {
//                options.inSampleSize = ws
//            }
//        }
//        if (options.outHeight > 96) {
//            val hs = options.outHeight / 96 + 1
//            if (hs > options.inSampleSize) {
//                options.inSampleSize = hs
//            }
//        }
        println("------imagespath ---- $str")
        images_path.add(str)
        var add_file = Utils.getImage(context.contentResolver, str)
        val bitmap = BitmapFactory.decodeFile(str)
        var v = View.inflate(context, R.layout.item_addgoods, null)
//        val imageIV = v.findViewById(R.id.addedImgIV) as ImageView
        val delIV = v.findViewById<View>(R.id.delIV) as ImageView
        ImageLoader.getInstance().displayImage(str,v.addedImgIV, Utils.UILoptionsUserProfile)
        delIV.tag = i
        delIV.setOnClickListener {
            addPicturesLL!!.removeView(v)
            delids.add(i)
//            Log.d("아이디값",delids.toString())

//            Toast.makeText(context,delids.toString(),Toast.LENGTH_SHORT).show()
        }
        if (imgSeq == 0) {
            addPicturesLL!!.addView(v)
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
                                    var id = Utils.getInt(contentFile,"id")
                                    if (type == 1) {
                                        var path = Config.url + Utils.getString(contentFile, "image_uri")
                                        reset2(path,id)
                                        println("getimage------$path")
                                        imagePaths.add(path)
//                                        modi_path!!.add(Utils.getString(contentFile, "image"))
                                        modi_path!!.add(Utils.getString(contentFile, "image"))
                                    } else {
                                        val path = Utils.getString(contentFile, "image_uri")
                                        val image = Utils.getString(contentFile, "image")
                                        println("path ----- $path")
                                        removeIV.visibility = View.VISIBLE
                                        videoVV.visibility = View.VISIBLE
                                        val uri = Uri.parse(Config.url + path)
                                        videoVV.start()
                                        videoVV.setVideoURI(uri)
                                        videoVV.setOnPreparedListener { mp -> mp.isLooping = true }
                                        videoPaths.add(path)
                                        video_image.add(image)
                                    }
                                }
                            }

                            if (images_path != null){

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

                        val add_file = Utils.getImage(context.contentResolver, str)

                        if (images?.size == 0) {

                            images?.add(add_file)

                        } else {
                            try {
                                images?.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images?.add(add_file)
                            }

                        }

                        reset(str, i)

                    }

                    val child = addPicturesLL!!.getChildCount()
                    for (i in 0 until child) {

                        val v = addPicturesLL!!.getChildAt(i)

                        val delIV = v.findViewById(R.id.delIV) as ImageView

                    }

                    displaynamePaths.clear()
                    for (i in 0..(name!!.size - 1)) {
                        val str = name[i]

                        if (displaynamePaths != null) {
                            displaynamePaths.add(str)
                        } else {
                            displaynamePaths.add(str)
                        }

                    }

//                    var intent = Intent();

//                    setResult(RESULT_OK, intent);

                }
                SELECT_VIDEO -> {
                    var item = data?.getStringArrayExtra("videos")
                    var name = data?.getStringArrayExtra("displayname")

//                    for (i in item!!.indices) {
//                        val str = item[i]
//
//                        videoPaths?.add(str)
//
//                        val add_file = Utils.getImage(context.contentResolver, str, 10)
//
//                        if (videos?.size == 0) {
//                            videos?.add(add_file)
//                        } else {
//                            try {
//                                videos?.set(videos!!.size, add_file)
//                            } catch (e: IndexOutOfBoundsException) {
//                                videos?.add(add_file)
//                            }
//
//                        }
//
//                    }
//                    for (i in 0 until item.size) {
//                        println("item----${item.get(i)}")
//                    }

                    videoPaths.clear()
                    video_image.clear()
                    for (i in 0..(item!!.size - 1)) {
                        val str = item[i]

                        if (videoPaths != null) {
                            videoPaths.add(str)
                        } else {
                            videoPaths.add(str)
                        }

                    }

                    var intent = Intent();

                    println("path ----- ${videoPaths.get(0)}")
                    videoVV.visibility = View.VISIBLE
                    removeIV.visibility = View.VISIBLE
                    val uri = Uri.parse(videoPaths.get(0))
                    videoVV.start()
                    videoVV.setVideoURI(uri)
                    videoVV.setOnPreparedListener { mp -> mp.isLooping = true }

//                    setResult(RESULT_OK, intent);

                }

                SELECT_HASHTAG -> {

                    if (data?.getStringArrayListExtra("data") != null) {
                        hashtag = data?.getStringArrayListExtra("data") as ArrayList<String>

                        println("tmpcontent : $hashtag")
                        var tag = ""
                        if (hashtag.size > 0){
                            for (i in 0 until hashtag.size){
                                tag += "#"+hashtag.get(i) + " "
                            }
                            hashtagsTV.setText(tag)
                        }

                    }

                }
            }
        }
    }



    fun clickMethod(v: View) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                    addPicturesLL!!.removeAllViews()
                    images!!.clear()
                    val tag = v.tag as Int
                    if (images_path!=null){
                        images_path!!.removeAt(tag)
                    }


                    for (k in images_url!!.indices) {
                        var vv = View.inflate(context, R.layout.item_addgoods, null)
                        val imageIV = vv.findViewById(R.id.addedImgIV) as ImageView
                        val delIV = vv.findViewById<View>(R.id.delIV) as ImageView
                        delIV.visibility = View.GONE
                        val del2IV = vv.findViewById<View>(R.id.del2IV) as ImageView
                        del2IV.visibility = View.VISIBLE
                        del2IV.tag = k
//                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        if (imgSeq == 0) {
                            addPicturesLL!!.addView(vv)
                        }
                    }
                    for (j in images_path!!.indices) {

                        val paths = images_path!!.get(j).split("/")
                        val file_name = paths.get(paths.size - 1)
                        val getPk = file_name.split("_")
                        val pathPk = getPk.get(0)
                        val add_file = Utils.getImage(context!!.getContentResolver(), images_path!!.get(j))
                        if (images!!.size == 0) {
                            images!!.add(add_file)
                        } else {
                            try {
                                images!!.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images!!.add(add_file)
                            }
                        }
                        reset2(images_path!!.get(j), j)
                    }
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()
    }

    fun clickMethod2(v: View) {
        println("------------click2")
        val builder = AlertDialog.Builder(context)
        builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                    addPicturesLL!!.removeAllViews()
                    val tag = v.tag as Int
                    images_url!!.removeAt(tag)
                    images_url_remove!!.add(images_id!!.get(tag).toString())
                    images_id!!.removeAt(tag)

                    for (k in images_url!!.indices) {
                        var vv = View.inflate(context, R.layout.item_addgoods, null)
                        val imageIV = vv.findViewById(R.id.addedImgIV) as ImageView
                        val delIV = vv.findViewById<View>(R.id.delIV) as ImageView
                        delIV.visibility = View.GONE
                        val del2IV = vv.findViewById<View>(R.id.del2IV) as ImageView
                        del2IV.visibility = View.VISIBLE
                        del2IV.tag = k
                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        if (imgSeq == 0) {
                            addPicturesLL!!.addView(vv)
                        }
                    }
                    for (j in images_path!!.indices) {

                        val paths = images_path!!.get(j).split("/")
                        val file_name = paths.get(paths.size - 1)
                        val getPk = file_name.split("_")
                        val pathPk = getPk.get(0)
                        val add_file = Utils.getImage(context!!.getContentResolver(), images_path!!.get(j))
                        if (images!!.size == 0) {
                            images!!.add(add_file)
                        } else {
                            try {
                                images!!.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images!!.add(add_file)
                            }
                        }
                        reset(images_path!!.get(j), j)
                    }

                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()

    }
}








