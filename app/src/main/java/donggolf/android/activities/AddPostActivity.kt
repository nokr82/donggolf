package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.InfoAction
import donggolf.android.actions.JoinAction
import donggolf.android.adapters.ImageAdapter
import donggolf.android.base.*
import donggolf.android.models.*
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_findid.*
import java.net.URI



class AddPostActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var context: Context

    private val FROM_CAMERA: Int = 100

    private val SELECT_PICTURE: Int = 101

    private val SELECT_VIDEO: Int = 102

    private val SELECT_HASHTAG: Int = 103

    var photoList: ArrayList<PhotoData> = ArrayList<PhotoData>()

    var result: ArrayList<String> = ArrayList<String>()

    private var imagesPaths: ArrayList<String> = ArrayList<String>()

    private var displaynamePaths: ArrayList<String> = ArrayList<String>()

    private var images: ArrayList<Bitmap> = ArrayList()

    private var videoPaths: ArrayList<String> = ArrayList<String>()

    private var videos: ArrayList<Bitmap>? = ArrayList()

    private var LoginMember: Info? = null

    var hashtag: ArrayList<String> = ArrayList<String>()

    val user = HashMap<String, Any>()

    var userid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        context = this

        mAuth = FirebaseAuth.getInstance()

        val db = FirebaseFirestore.getInstance()


        val dataList: Array<String> = arrayOf("*");

        val one = 1

        val setContent = TmpContent()


        if (setContent != null) {
            titleET.setText(setContent.title)
            contentET.setText(setContent.texts)

        }

        // Create a new user with a first and last name

        permission()

        println("displayname " + displaynamePaths.size.toString())

        db.collection("users")
                .get()
                .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                    override fun onComplete(task: Task<QuerySnapshot>) {
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                Log.d("yjs", document.getId() + " => " + document.getData())
                                userid = document.getId()
                            }
                        } else {
                            Log.w("yjs", "Error getting documents.", task.exception)
                        }
                    }
                })

        val category = intent.getIntExtra("category", 0)
        if (category == 2) {
            addpostTV.text = "수정하기"
            val id = intent.getStringExtra("id")
            ContentAction.viewContent(id) { success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                if (success) {
                    if (data != null) {
                        if (data.size != 0) {
                            titleET.setText(data["title"].toString())
                            contentET.setText(data["texts"].toString())
                        }
                    }
                }
            }
        }


        finishaBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("글쓰기를 취소하시겠습니까 ?")

                    .setPositiveButton("유지하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()

                        if (displaynamePaths != null) {
                            if (displaynamePaths.size != 0) {

                                val title = Utils.getString(titleET)
                                val content = Utils.getString(contentET)

                                val nick: String = PrefUtils.getStringPreference(context, "nick")


                                if (hashtag != null) {
                                    if (hashtag.size != 0) {
                                        var tmphashtag = hashtag[0].toString()

                                        for (i in 0..hashtag.size - 1) {
                                            tmphashtag += "," + hashtag[i].toString()
                                        }

                                        val tmpContent = TmpContent(0, userid!!, title, content, tmphashtag, 1)

//                                        dbManager.inserttmpcontent(tmpContent)
                                    }
                                }


                            }
                        }

                        if (displaynamePaths.size == 0) {

                            val title = Utils.getString(titleET)
                            val content = Utils.getString(contentET)

                            val nick: String = PrefUtils.getStringPreference(context, "nick")


                        }


                    })
                    .setNegativeButton("삭제하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        finish()
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

                            finish()
                        } else {
                            val id = intent.getStringExtra("id")
                            modify(id)

                            println("id : ======== $id")

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

        val email: String = PrefUtils.getStringPreference(context, "email")

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

            val text = Text("text", content)

            val texts: ArrayList<Any> = ArrayList<Any>()

            texts.add(text)

            val nick: String = PrefUtils.getStringPreference(context, "nick")


            if (displaynamePaths != null) {
                if (displaynamePaths.size != 0) {

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

                    val sharpTag: ArrayList<String> = ArrayList<String>()

                    val item = Content(nowTime, 0, 0, nick.toString(), null, title, texts, "", false,
                            0, 0, "", false, 0, false, sharpTag)


                    ContentAction.viewContent(id) { success, data, exception ->
                        if (success) {
                            if (data != null) {
                                if (data.size != 0) {
                                    FirebaseFirestoreUtils.save("contents", id, item) {
                                        if (it) {
                                            if (displaynamePaths.size != 0) {

                                                val bytearray__: ArrayList<ByteArray> = ArrayList<ByteArray>()
                                                for (i in 0..displaynamePaths.size - 1) {

                                                    val image_path = photo.file[i]

                                                    var bt: Bitmap = Utils.getImage(context.contentResolver, displaynamePaths.get(i), 500)

                                                    var bytearray_ = Utils.getByteArray(bt)

                                                    val cutImage = Utils.resize(bt, 100)

                                                    val cutBytearray_ = Utils.getByteArray(cutImage)

                                                    FirebaseFirestoreUtils.uploadFile(bytearray_, image_path) {
                                                        if (it) {
                                                            FirebaseFirestoreUtils.uploadFile(cutBytearray_, imgPaths.get(i)) {
                                                                if (it) {

                                                                }
                                                            }


                                                        }
                                                    }


                                                }
                                            }


                                        }
                                    }
                                }
                            }

                        }
                    }

                    if (displaynamePaths.size == 0) {

                        val regionItem = Region(nowTime, "", "")

                        val sharpTag: ArrayList<String> = ArrayList<String>()


                        val content = Utils.getString(contentET)

                        val text = Text("text", content)

                        val texts: ArrayList<Any> = ArrayList<Any>()

                        texts.add(text)

                        ContentAction.viewContent(id) { success, data, exception ->
                            if (success) {
                                if (data != null) {
                                    if (data.size != 0) {
                                        val time: Long = data["createAt"] as Long
                                        val item = Content(nowTime, 0, 0, nick.toString(), regionItem, title, texts, "", false,
                                                0, 0, "", false, 0, false, sharpTag)
                                        FirebaseFirestoreUtils.save("contents", id, item) {
                                            if (it) {
                                                finish()
                                            } else {

                                            }
                                        }
                                    }
                                }
                            }
                        }


                    }
                }

            }
        }
    }

    private fun addContent() {

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
            val uid = user.uid

            val nowTime = System.currentTimeMillis()

            val email: String = PrefUtils.getStringPreference(context, "email")
            val nick: String = PrefUtils.getStringPreference(context, "nick")

            var params = HashMap<String, Any>()
            params.put("uid", uid)

            val title = Utils.getString(titleET)
            val content = Utils.getString(contentET)

            val text = Text("text", content)

            val texts: ArrayList<Any> = ArrayList<Any>()

            texts.add(text)

            if (displaynamePaths != null) {
                if (displaynamePaths.size != 0) {

                    val bytearray__: ArrayList<ByteArray> = ArrayList<ByteArray>()
                    for (i in 0..displaynamePaths.size - 1) {
                        var bt: Bitmap = Utils.getImage(context.contentResolver, displaynamePaths.get(i), 500)

                        var bytearray_ = Utils.getByteArray(bt)

                        bytearray__.add(bytearray_)
                    }


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

                    val regionItem = Region(nowTime, "", "")

                    val sharpTag: ArrayList<String> = ArrayList<String>()

                    val item = Content(nowTime, 0, 0, nick.toString(), regionItem, title, texts, "", false,
                            0, 0, "", false, 0, false, sharpTag)

                    ContentAction.saveContent(item) { success: Boolean, key: String?, exception: Exception? ->
                        if (success) {
                            if (displaynamePaths.size != 0) {

                                val bytearray__: ArrayList<ByteArray> = ArrayList<ByteArray>()
                                for (i in 0..displaynamePaths.size - 1) {

                                    val image_path = photo.file[i]

                                    var bt: Bitmap = Utils.getImage(context.contentResolver, displaynamePaths.get(i), 500)

                                    var bytearray_ = Utils.getByteArray(bt)

                                    val cutImage = Utils.resize(bt, 100)

                                    val cutBytearray_ = Utils.getByteArray(cutImage)

                                    FirebaseFirestoreUtils.uploadFile(bytearray_, "imgl/"+image_path) {
                                        if (it) {
                                            FirebaseFirestoreUtils.uploadFile(cutBytearray_, imgPaths.get(i)) {
                                                if (it) {

                                                }
                                            }


                                        }
                                    }


                                }
                            }
                        } else {

                        }
                    }


                }
            }
            if (displaynamePaths.size == 0) {
                val regionItem = Region(nowTime, "", "")

                val sharpTag: ArrayList<String> = ArrayList<String>()

                val content = Utils.getString(contentET)

                val text = Text("text", content)

                val texts: ArrayList<Any> = ArrayList<Any>()

                texts.add(text)

                val item = Content(nowTime, 0, 0, nick.toString(), regionItem, title, texts, "", false,
                        0, 0, "", false, 0, false, sharpTag)
                ContentAction.saveContent(item) { success: Boolean, key: String?, exception: Exception? ->
                    if (success) {
                        finish()
                    } else {

                    }
                }
            }

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
                    for (i in 0..(name!!.size - 1)) {
                        val str = name[i]

                        if (displaynamePaths != null) {
                            displaynamePaths.clear()
                            displaynamePaths.add(str)


                            Log.d("yjs", "display " + displaynamePaths.get(0))
                        } else {
                            displaynamePaths.add(str)
                            Log.d("yjs", "display " + displaynamePaths.get(0))
                        }

                    }

                    var intent = Intent();

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

                    for (i in 0..(name!!.size - 1)) {
                        val str = name[i]

                        if (displaynamePaths != null) {
                            displaynamePaths.clear()
                            displaynamePaths.add(str)
                        } else {
                            displaynamePaths.add(str)
                        }

                    }

                    var intent = Intent();

                    Log.d("yjs", "PostResult : " + item?.size.toString() + "name : " + displaynamePaths.toString())

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



