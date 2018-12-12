package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
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
            println("id +====== $id")

        }

        finishaBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("글쓰기를 취소하시겠습니까 ?")

                    .setPositiveButton("유지하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        val title = Utils.getString(titleET)
                        val content = Utils.getString(contentET)

                        if (hashtag != null) {
                            if (hashtag.size != 0) {
                                var tmphashtag = hashtag[0].toString()

                                for (i in 0..hashtag.size - 1) {
                                    tmphashtag += "," + hashtag[i].toString()
                                }

                                val tmpContent = TmpContent(0, member_id.toString(), title, content)

                                println("member_id ======= $member_id")

                                dbManager.inserttmpcontent(tmpContent)

                                finish()
                            }
                        }

                        if (hashtag != null){
                            if(hashtag.size == 0){
                                var tmphashtag = ""

                                val tmpContent = TmpContent(0, member_id.toString(), title, content)

                                println("userid ======= $userid")

                                dbManager.inserttmpcontent(tmpContent)

                                finish()
                            }
                        }

                    })
                    .setNegativeButton("삭제하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()

                        db.collection("users")

                        loadData(dbManager,member_id.toString())

                        if(tmpContent.id == null){
                            finish()
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

                            intent = Intent()
                            intent.action = "UPDATE_POST"
                            sendBroadcast(intent)

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
            startActivityForResult(intent, SELECT_HASHTAG);
        }

    }

    private fun loadData(dbManager: DataBaseHelper , userid: String) {
        val query = "SELECT * FROM tmpcontent WHERE owner ='" + userid + "'"

        val imagespathquery = "SELECT * FROM imagespath WHERE owner ='" + userid + "'"

        val tmpcontent = dbManager.selectTmpContent(query)

//        tmpImagesPath = dbManager.selectImagesPath(imagespathquery)

        println("tmpImagesPath ======== $tmpImagesPath")

        tmpContent = tmpcontent

        titleET.setText(tmpcontent.title)
        contentET.setText(tmpcontent.texts)

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

        var content = Utils.getString(contentET)
        if (content.isEmpty()) {
            Utils.alert(context, "내용을 입력해주세요.")
            return
        }

        val user = mAuth.currentUser

        if (user != null) {

            val title = Utils.getString(titleET)
            val content = Utils.getString(contentET)

            if (displaynamePaths != null) {
                if (displaynamePaths.size != 0) {

                    val title = Utils.getString(titleET)
                    val content = Utils.getString(contentET)

                    val text = Text("text", content)

                    val texts: ArrayList<Any> = ArrayList<Any>()

                    texts.add(text)

                    val bytearray__: ArrayList<ByteArray> = ArrayList<ByteArray>()
                    for (i in 0..displaynamePaths.size - 1) {
                        var bt: Bitmap = Utils.getImage(context.contentResolver, displaynamePaths.get(i), 500)

                        var bytearray_ = Utils.getByteArray(bt)

                        bytearray__.add(bytearray_)
                    }

                    val nowTime = System.currentTimeMillis()

                    var imgPaths = ArrayList<String>()
                    var imagsPaths = ArrayList<String>()
                    var imgpath = ArrayList<String>()
                    var photo = Photo()

                    for (i in 0..(displaynamePaths.size - 1)) {

                        var image_path = "imgl/" + nowTime + ".png"
                        var images_path = "imgs/" + nowTime + ".png"

                        imagesPaths.add(image_path)
                        imgPaths.add(images_path)
                        imgpath.add(nowTime.toString() + ".png")
                    }

                    photo.type = "photo"
                    photo.file = imgpath

                    texts.add(photo)

                }
            }

            if (displaynamePaths.size == 0) {

            }
        }
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
        params.put("look_count",0)
        params.put("heart_count",0)
        params.put("cmt_count",0)

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

                    hashtag = data?.getSerializableExtra("data") as ArrayList<String>

                    println("tmpcontent : $hashtag")

                }
            }
        }
    }
}



