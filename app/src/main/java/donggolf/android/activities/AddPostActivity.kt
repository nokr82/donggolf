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
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.*
import com.github.irshulx.EditorListener
import com.google.api.AnnotationsProto.http
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kakao.kakaostory.StringSet.content
import com.kakao.kakaostory.StringSet.text
import com.kakao.kakaotalk.StringSet.members
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.PostAction
import donggolf.android.actions.PostAction.up_load_image
import donggolf.android.base.*
import donggolf.android.models.*
import kotlinx.android.synthetic.main.activity_add_post.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
var html = ""


class AddPostActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var editorImageLayout = com.github.irshulx.R.layout.tmpl_image_view
    private lateinit var context: Context
    private val FROM_CAMERA: Int = 100
    private val SELECT_PICTURE: Int = 101
    private val SELECT_VIDEO: Int = 102
    private val SELECT_HASHTAG: Int = 103
    var result: ArrayList<String> = ArrayList<String>()
    private var displaynamePaths: ArrayList<String> = ArrayList<String>()
    private var videoPaths: ArrayList<String> = ArrayList<String>()
    private var videoIds: ArrayList<Int> = ArrayList<Int>()
    private var videos: ArrayList<Bitmap>? = ArrayList()
    var hashtag: ArrayList<String> = ArrayList<String>()
    val user = HashMap<String, Any>()
    var userid: String? = null
    var tmpContent: TmpContent = TmpContent()
    var delids = ArrayList<Int>()
    var tmpImagesPath: ArrayList<ImagesPath> = ArrayList<ImagesPath>()


    var imageUrlToIVs = HashMap<String, ImageView>()

    var member_id = 0

    private var addPicturesLL: LinearLayout? = null

    private val imgSeq = 0
    var content_imglist: ArrayList<String> = ArrayList<String>()
    var pk: String? = null
    var images_path: ArrayList<String> = ArrayList<String>()
    var modi_path: ArrayList<String> = ArrayList<String>()
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null
    var selectedImageViewList: ArrayList<String> = ArrayList<String>()

    var video_image: ArrayList<String> = ArrayList<String>()

    lateinit var videofile: ByteArray

    var cht_yn = "Y"
    var cmt_yn = "Y"

    var MODIFYY = 100
    var temp_yn = ""

    var album_video = false
    var video_id = ""
    var text_ex = ""
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
        //위지위그 사용
        editor.setEditorImageLayout(editorImageLayout)
        editor.setDividerLayout(R.layout.tmpl_divider_layout)
        editor.editorListener = object : EditorListener {
            override fun onRenderMacro(name: String?, props: MutableMap<String, Any>?, index: Int): View? {
                val layout = layoutInflater.inflate(R.layout.activity_add_post, null)
                return layout
            }

            override fun onTextChanged(editText: EditText, text: Editable) {
                text_ex = text.toString()
                Log.d("텍스트", text_ex)
            }

            override fun onUpload(image: Bitmap, uuid: String) {
                up_load_image(uuid, image)

                println("uuid::::::::::::::::::::::::::::::::::${uuid}")

            }
        }
        editor.render()

        if (cht_yn == "Y") {
            chatableIV.visibility = View.VISIBLE
        } else {
            chatableIV.visibility = View.GONE
        }

        if (cmt_yn == "Y") {
            replyableIV.visibility = View.VISIBLE
        } else {
            replyableIV.visibility = View.GONE
        }


        addPicturesLL = findViewById(R.id.addPicturesLL)

        if (intent.getStringArrayListExtra("image_uri") != null) {
            selectedImageViewList = intent.getStringArrayListExtra("image_uri")
            println("images---------selectimage$selectedImageViewList")
            if (selectedImageViewList.size > 0) {
                for (i in 0 until selectedImageViewList.size) {
                    reset2(selectedImageViewList.get(i), i)
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
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("삭제하시겠습니까?")

                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        videoVV.visibility = View.GONE
                        videoLL.visibility = View.GONE
                        removeIV.visibility = View.GONE
                        videoPaths.clear()
                        video_image.clear()
                        dialog.cancel()
                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            val alert = builder.create()
            alert.show()

        }

        loadData(dbManager, member_id.toString())

        permission()

        val category = intent.getIntExtra("category", 0)

        if (category == 2) {
            addpostTV.text = "수정하기"
            val id = intent.getStringExtra("id")
            getPost()
        }

        finishaBT.setOnClickListener {
            if (addpostTV.text.equals("수정하기")) {
                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("수정하기를 취소하시겠습니까 ?")

                        .setPositiveButton("계속 수정하기", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()

                            Utils.hideKeyboard(this)

                        })
                        .setNegativeButton("삭제하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()

//                        loadData(dbManager,member_id.toString())

                            if (tmpContent.id == null) {
                                Log.d("끝","")
                                finish()
                            }

                            if (tmpImagesPath != null && tmpImagesPath.size > 0) {
                                dbManager.deleteImagePaths(member_id.toString())
                            }

                            if (tmpContent.id != null) {
                                dbManager.deleteTmpContent(tmpContent.id!!)
                                Log.d("2끝","")
                                finish()
                            }

                            Utils.hideKeyboard(this)


                        })
                val alert = builder.create()
                alert.show()
            } else {
                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("글쓰기를 취소하시겠습니까 ?")

                        .setPositiveButton("유지하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                            val title = Utils.getString(titleET)
//                            val content = Utils.getString(contentET)
                            val content = editor.getContentAsHTML()

                            if (tmpImagesPath != null && tmpImagesPath.size > 0) {
                                dbManager.deleteImagePaths(member_id.toString())
                            }

                            if (tmpContent.id != null) {
                                dbManager.deleteTmpContent(tmpContent.id!!)
                            }

                            val tmpContent = TmpContent(0, member_id.toString(), title, content)

                            dbManager.inserttmpcontent(tmpContent)

                            if (images_path != null && images_path!!.size > 0) {
                                for (i in 0 until images_path!!.size) {
                                    val imagesPath = ImagesPath(0, member_id.toString(), images_path!!.get(i), 1)
                                    println("imagesPath ${imagesPath.path}")
                                    dbManager.insertimagespath(imagesPath)
                                }
                            }

                            if (videoPaths != null && videoPaths.size > 0) {
                                for (i in 0 until videoPaths.size) {
                                    val videoPath = ImagesPath(0, member_id.toString(), videoPaths.get(i), 2)
                                    println("videoPath ${videoPath.path}")
                                    dbManager.insertimagespath(videoPath)
                                }
                            }

                            if (hashtag != null && hashtag.size > 0) {
                                for (i in 0 until hashtag.size) {
                                    val hastag = ImagesPath(0, member_id.toString(), hashtag.get(i), 3)
                                    println("hastag ${hastag.path}")
                                    dbManager.insertimagespath(hastag)
                                }
                            }
                            Log.d("끝3","")
                            finish()
                            dialog.cancel()

                            Utils.hideKeyboard(this)

                        })
                        .setNegativeButton("삭제하고 나가기", DialogInterface.OnClickListener { dialog, id ->


                            //                            loadData(dbManager,member_id.toString())

                            if (tmpContent.id == null) {
                            }

                            if (tmpImagesPath != null && tmpImagesPath.size > 0) {
                                dbManager.deleteImagePaths(member_id.toString())
                            }

                            if (tmpContent.id != null) {
                                dbManager.deleteTmpContent(tmpContent.id!!)
                            }
                            Log.d("끝4","")
                            finish()
                            dialog.cancel()
                            Utils.hideKeyboard(this)

                        })
                val alert = builder.create()
                alert.show()
            }

        }

        movefindpictureBT.setOnClickListener {
            permissionimage()
        }

        movefindvideoBT.setOnClickListener {
            permissionvideo()
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

                        if (tmpContent.id == null) {
                        }

                        if (tmpImagesPath != null && tmpImagesPath.size > 0) {
                            dbManager.deleteImagePaths(member_id.toString())
                        }

                        if (tmpContent.id != null) {
                            dbManager.deleteTmpContent(tmpContent.id!!)
                        }
                        Utils.hideKeyboard(this)


                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        Log.d("끝5","")
                        finish()
                    })

            val alert = builder.create()
            alert.show()

        }

        hashtagLL.setOnClickListener {
            val id = intent.getStringExtra("id")

            var intent = Intent(context, ContentTagChangeActivity::class.java);
            intent.putExtra("type", "post")
            intent.putExtra("id", id)
            startActivityForResult(intent, SELECT_HASHTAG)
        }
        /*  var cmt_yn = "Y"
              if (chatableRL.isChecked == false){
                  cmt_yn = "N"
              }*/
        replyableRL.setOnClickListener {
            if (replyableIV.visibility == View.GONE) {
                replyableIV.visibility = View.VISIBLE
                cmt_yn = "Y"
            } else {
                replyableIV.visibility = View.GONE
                cmt_yn = "N"
            }
        }
        chatableRL.setOnClickListener {
            if (chatableIV.visibility == View.GONE) {
                chatableIV.visibility = View.VISIBLE
                cht_yn = "Y"
            } else {
                chatableIV.visibility = View.GONE
                cht_yn = "N"
            }
        }


    }

    private fun loadData(dbManager: DataBaseHelper, userid: String) {

        //임시저장 데이터 불러오기
        val query = "SELECT * FROM tmpcontent WHERE owner ='" + userid + "'"

        val imagespathquery = "SELECT * FROM imagespath WHERE owner ='" + userid + "'"

        val tmpcontent = dbManager.selectTmpContent(query)

        var tmpImagesPath = dbManager.selectImagesPath(imagespathquery)

        if (tmpImagesPath != null && tmpImagesPath.size > 0) {
            for (i in 0 until tmpImagesPath.size) {
                if (tmpImagesPath.get(i).type == 1) {
                    images_path!!.add(tmpImagesPath.get(i).path!!)
                } else if (tmpImagesPath.get(i).type == 2) {
                    videoPaths.add(tmpImagesPath.get(i).path!!)
                } else if (tmpImagesPath.get(i).type == 3) {
                    hashtag.add(tmpImagesPath.get(i).path!!)
                } else if (tmpImagesPath.get(i).type == 4) {
                    videoIds.add(tmpImagesPath.get(i).path!!.toInt())
                    album_video = true
                }
            }
        }

//        if (images_path!!.size > 0) {
//            for (i in 0..(images_path!!.size - 1)) {
//                val str = images_path!![i]
//
//                val add_file = Utils.getImage(context.contentResolver, str)
//
//                if (images?.size == 0) {
//
//                    images?.add(add_file)
//
//                } else {
//                    try {
//                        images?.set(images!!.size, add_file)
//                    } catch (e: IndexOutOfBoundsException) {
//                        images?.add(add_file)
//                    }
//
//                }
//
//                reset(str, i)
//
//            }
//
//            val child = addPicturesLL!!.getChildCount()
//            for (i in 0 until child) {
//
//                println("test : $i")
//
//                val v = addPicturesLL!!.getChildAt(i)
//
//                val delIV = v.findViewById(R.id.delIV) as ImageView
//
//            }
//        }

        if (videoPaths.size > 0) {
            videoVV.visibility = View.VISIBLE
            videoLL.visibility = View.VISIBLE
            removeIV.visibility = View.VISIBLE
            val uri = Uri.parse(videoPaths.get(0))
            videoVV.start()
            videoVV.setVideoURI(uri)
            videoVV.setOnPreparedListener { mp -> mp.isLooping = true }
        }

        if (hashtag.size > 0) {
            var tag = ""
            for (i in 0 until hashtag.size) {
                tag += "#" + hashtag.get(i) + "  "
            }
            hashtagsTV.setText(tag)
        }

        tmpContent = tmpcontent
        this.tmpImagesPath = tmpImagesPath
        val text: String = if(tmpcontent.texts == null || tmpcontent.texts!!.isEmpty() || tmpcontent.texts!!.count() < 1) "" else tmpcontent.texts!!

        titleET.setText(tmpcontent.title)
        editor.render(text)
//        contentET.setText(tmpcontent.texts)
//        getPost()
    }

    private fun modify(id: String) {
        val title = Utils.getString(titleET)
        if (title.isEmpty()) {
            Utils.alert(context, "제목을 입력해주세요.")
            return
        }


//        var text = Utils.getString(contentET)
//        if (text.isEmpty()) {
//            Utils.alert(context, "내용을 입력해주세요.")
//            return
//        }

        val params = RequestParams()
        params.put("content_id", id)
        val login_id = PrefUtils.getIntPreference(context, "member_id")
        params.put("member_id", login_id)
        params.put("title", title)

        var html = editor.getContentAsHTML()

        if (text_ex.isEmpty()) {
            Utils.alert(context, "내용을 입력해주세요.")
            return
        }

        params.put("text", html)
        params.put("cht_yn", cht_yn)
        params.put("cmt_yn", cmt_yn)

        if (hashtag != null) {
            for (i in 0..hashtag.size - 1) {
                params.put("tag[" + i + "]", hashtag.get(i))
            }
        }

        if (delids != null) {
            Log.d("삭제", delids.toString())
            if (delids.size != 0) {
                for (i in 0..delids.size - 1) {
                    params.put("del_ids[" + i + "]", delids[i])
                }
            }
        }

//        var seq = 0
//        if (addPicturesLL != null) {
//            for (i in 0 until addPicturesLL!!.childCount) {
//                val v = addPicturesLL?.getChildAt(i)
//                val imageIV = v?.findViewById<ImageView>(R.id.addedImgIV)
//                if (imageIV is ImageView) {
//                    val bitmap = imageIV.drawable as BitmapDrawable
//                    params.put("files[$seq]", ByteArrayInputStream(Utils.getByteArrayFromImageView(imageIV)))
//                    seq++
//                    println("add-------------$seq")
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

        if (video_image.size > 0) {
            params.put("video_delete", "delete")
        } else {
            if (videoPaths != null) {
                if (videoPaths.size != 0) {

                    val options = BitmapFactory.Options()
                    options.inSampleSize = 1

                    for (i in 0..videoPaths.size - 1) {

                        if (album_video) {
                            val curThumb = MediaStore.Video.Thumbnails.getThumbnail(context.contentResolver, videoIds[i].toLong(), MediaStore.Video.Thumbnails.MINI_KIND, options)
                            params.put("video_thumbnail[" + i + "]", ByteArrayInputStream(Utils.getByteArray(curThumb)))
                        } else {
                            val bitmap = Utils.retriveVideoFrameFromVideo(videoPaths[i])
                            params.put("video_thumbnail[" + i + "]", ByteArrayInputStream(Utils.getByteArray(bitmap)))
                        }

                        val file = File(videoPaths.get(i))
                        var bytes = file.readBytes()

                        var n: Int
                        val baos = ByteArrayOutputStream()
                        val videoBytes = baos.toByteArray()

                        params.put("videos[" + i + "]", ByteArrayInputStream(bytes))
                    }
                }
            }
        }

        PostAction.update_post(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    var intent = Intent()
                    intent.putExtra("id", id)
                    intent.putExtra("reset", "reset")
                    setResult(RESULT_OK, intent);
                    Log.d("끝5","")
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

        var text = text_ex
        if (text.isEmpty()) {
            Utils.alert(context, "내용을 입력해주세요.")
            return
        }

        html = editor.getContentAsHTML()
        Log.d("텟스2", html)

        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("title", title)
        params.put("text", html)
        params.put("deleted", "N")
        params.put("cht_yn", cht_yn)
        params.put("cmt_yn", cmt_yn)

        if (PrefUtils.getStringPreference(context, "region_id") != null) {
            var region_id = PrefUtils.getStringPreference(context, "region_id")
            if (region_id == "1001") {
                region_id = "0"
                params.put("region", region_id)
            } else {
                params.put("region", region_id)
            }

        } else {
            var region_id = 0
            params.put("region", region_id)
        }

        if (PrefUtils.getStringPreference(context, "region_id2") != null) {
            var region_id = PrefUtils.getStringPreference(context, "region_id2")
            params.put("region2", region_id)
        } else {
            var region_id = 1
            params.put("region2", region_id)
        }
        Log.d("태그", hashtag.toString())

        if (hashtag != null) {
            for (i in 0..hashtag.size - 1) {
                params.put("tag[" + i + "]", hashtag.get(i))
            }
        }

        if (temp_yn != "") {
            params.put("tmp_yn", temp_yn)
        }


        var seq = 0
        /*if (addPicturesLL != null) {
            for (i in 0 until addPicturesLL!!.childCount) {
                val v = addPicturesLL?.getChildAt(i)
                val imageIV = v?.findViewById<ImageView>(R.id.addedImgIV)
                if (imageIV is ImageView) {
                    val bitmap = imageIV.drawable as BitmapDrawable
                    Log.d("로그", bitmap.toString())
                    params.put("files[$seq]", ByteArrayInputStream(Utils.getByteArray(bitmap.bitmap)))
                    seq++
                    println("add-------------$seq")
                }
            }
        }*/

        if (videoPaths != null) {
            if (videoPaths.size != 0) {

                val options = BitmapFactory.Options()
                options.inSampleSize = 1

                for (i in 0..videoPaths.size - 1) {

                    if (album_video) {
                        val curThumb = MediaStore.Video.Thumbnails.getThumbnail(context.contentResolver, videoIds[i].toLong(), MediaStore.Video.Thumbnails.MINI_KIND, options)
                        params.put("video_thumbnail[" + i + "]", ByteArrayInputStream(Utils.getByteArray(curThumb)))
                    } else {
                        val bitmap = Utils.retriveVideoFrameFromVideo(videoPaths[i])
                        params.put("video_thumbnail[" + i + "]", ByteArrayInputStream(Utils.getByteArray(bitmap)))
                    }

                    val file = File(videoPaths.get(i))
                    println("----------videoPath ${videoPaths.get(i)}")
                    println("file ---- ${file.length()}")

                    var bytes = file.readBytes()
                    println("bytes ---- $bytes")


                    var n: Int
                    val baos = ByteArrayOutputStream()
                    val videoBytes = baos.toByteArray()

                    params.put("videos[" + i + "]", ByteArrayInputStream(bytes))

                }
            }
        }

        PostAction.add_post(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                /*var content_id = response!!.getString("content_id")
                Log.d("아뒤컨",content_id)*/

//                get_content_file(content_id)
                var intent = Intent()
                intent.action = "ADD_POST"
                sendBroadcast(intent)
                finish()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
//                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
            }

        })

        val dbManager = DataBaseHelper(context)

//        loadData(dbManager, member_id.toString())

        if (tmpContent.id == null) {
            Log.d("끝6","")
            finish()
        }

        if (tmpContent.id != null) {
            dbManager.deleteTmpContent(tmpContent.id!!)
            Log.d("끝7","")
            finish()
        }

        if (tmpImagesPath != null && tmpImagesPath.size > 0) {
            dbManager.deleteImagePaths(member_id.toString())
        }

    }
    private fun moveMyPicture() {
//        var intent = Intent(context, FindPictureGridActivity::class.java);
        var intent = Intent(context, FindPictureActivity::class.java);
        var time = System.currentTimeMillis()
//        intent.putExtra("time", System.currentTimeMillis())
        intent.putExtra("image", "image")
        startActivityForResult(intent, SELECT_PICTURE);

        println("SELECT_PICTURE:::::::::::::::::::::::::${time}")

    }

    private fun up_load_image(path:String, image:Bitmap) {

        val params = RequestParams()
        params.put("file", ByteArrayInputStream(Utils.getByteArray(image!!)))



        PostAction.up_load_image(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                print("response:::::::::::::${response}")

                var result = response!!.getString("result")

                if (result == "ok") {
                    var image_uri = response!!.getString("image_uri")

//                    html = html.replace(path, image_uri)


                    editor.onImageUploadComplete(Config.url + image_uri, path)
//                    editor.render(html)

                    println("html::::::::::::::::::::::::::${html}")
                    Log.d("image_uri",image_uri)
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
            }

        })


    }

    private fun moveMyVideo() {
//        var intent = Intent(context, FindVideoActivity::class.java);
        var intent = Intent(context, FindPictureActivity::class.java);
        intent.putExtra("image", "video")
        startActivityForResult(intent, SELECT_VIDEO);
    }

    private fun permission() {

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {

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

    private fun permissionimage() {

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                moveMyPicture()
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

    private fun permissionvideo() {

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                moveMyVideo()
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
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("삭제하시겠습니까?")

                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        addPicturesLL!!.removeView(v)
                        dialog.cancel()
                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            val alert = builder.create()
            alert.show()

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
        var add_file = Utils.noResizeImage(context.contentResolver, str)
        val bitmap = BitmapFactory.decodeFile(str)
        var v = View.inflate(context, R.layout.item_addgoods, null)
        val imageIV = v.findViewById(R.id.addedImgIV) as ImageView
        val delIV = v.findViewById<View>(R.id.delIV) as ImageView

//        ImageLoader.getInstance().displayImage(str,v.addedImgIV, Utils.UILoptionsUserProfile)
        imageUrlToIVs.put(str, imageIV)

        ImageLoader.getInstance().loadImage(str, object : ImageLoadingListener {
            override fun onLoadingCancelled(imageUri: String?, view: View?) {

            }

            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
            }

            override fun onLoadingStarted(imageUri: String?, view: View?) {
            }

            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                val iv = imageUrlToIVs.get(imageUri!!)
                iv!!.setImageBitmap(loadedImage)
                Log.d("높이23", loadedImage!!.height.toString())
            }
        })

        delIV.tag = i
        delIV.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("삭제하시겠습니까?")

                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        addPicturesLL!!.removeView(v)
                        delids.add(i)
                        dialog.cancel()
                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            val alert = builder.create()
            alert.show()


//            Log.d("아이디값",delids.toString())

//            Toast.makeText(context,delids.toString(),Toast.LENGTH_SHORT).show()
        }
        if (imgSeq == 0) {
            addPicturesLL!!.addView(v)
        }

    }

    fun getPost() {
        //게시글 불러오기
        if (intent.getStringExtra("id") != null) {
            val id = intent.getStringExtra("id")
            val login_id = PrefUtils.getIntPreference(context, "member_id")

            var params = RequestParams()
            params.put("id", id)
            params.put("member_id", login_id)

            PostAction.get_post(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    try {
                        val result = response!!.getString("result")
                        if (result == "ok") {

                            val data = response.getJSONObject("Content")

                            val title = Utils.getString(data, "title")
                            val text = Utils.getString(data, "text")
                            val member_id = Utils.getString(data, "member_id")
                            val cht_yn = Utils.getString(data, "cht_yn")
                            val cmt_yn = Utils.getString(data, "cmt_yn")

                            val tags = response.getJSONArray("tags")
                            val imageDatas = response.getJSONArray("ContentImgs")


                            if (tags != null && tags.length() > 0) {
                                var hashtags: String = ""

                                for (i in 0 until tags.length()) {
                                    var json = tags.get(i) as JSONObject
                                    var MemberTags = json.getJSONObject("ContentsTags")
                                    val division = Utils.getString(MemberTags, "division")

                                    if (division == "1") {
                                        val tag = Utils.getString(MemberTags, "tag")
                                        hashtags += "#" + tag + "  "
                                    }
                                }
                                hashtagsTV.text = hashtags
                            }

                            if (imageDatas != null && imageDatas.length() > 0) {
                                var imagePaths: java.util.ArrayList<String> = java.util.ArrayList<String>()

                                for (i in 0 until imageDatas.length()) {
                                    var json = imageDatas.get(i) as JSONObject

                                    var contentFile = json.getJSONObject("contentFile")
                                    var type = Utils.getInt(contentFile, "type")
                                    var id = Utils.getInt(contentFile, "id")
                                    if (type == 1) {
                                        var path = Config.url + Utils.getString(contentFile, "image_uri")

                                        reset2(path, id)
                                        println("getimage------$path")
                                        imagePaths.add(path)
//                                        modi_path!!.add(Utils.getString(contentFile, "image"))
                                        modi_path!!.add(Utils.getString(contentFile, "image"))
                                    } else {
                                        val path = Utils.getString(contentFile, "video_uri")
                                        val image = Utils.getString(contentFile, "image")
                                        println("path ----- $path")
                                        removeIV.visibility = View.VISIBLE
                                        videoVV.visibility = View.VISIBLE
                                        videoLL.visibility = View.VISIBLE
                                        val uri = Uri.parse(Config.url + path)
                                        videoVV.start()
                                        videoVV.setVideoURI(uri)
                                        videoVV.setOnPreparedListener { mp -> mp.isLooping = true }
                                        videoPaths.add(path)
                                        album_video = false
                                        video_image.add(image)
                                    }
                                }
                            }

                            if (images_path != null) {

                            }
                            titleET.setText(title)
                            contentET.setText(text)

                            if (cht_yn == "Y") {
                                chatableIV.visibility = View.VISIBLE
                            } else {
                                chatableIV.visibility = View.GONE
                            }

                            if (cmt_yn == "Y") {
                                replyableIV.visibility = View.VISIBLE
                            } else {
                                replyableIV.visibility = View.GONE
                            }

                            editor.render(text)

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

                    println("resulttime:::::::::::::::::::::::::::${System.currentTimeMillis()}")

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

                        editor.insertImage(add_file)
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
                    var video_ids = data?.getIntegerArrayListExtra("ids")
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
                    for (i in 0..(video_ids!!.size - 1)) {
                        val str = video_ids[i]
                        videoIds.add(str)
                    }

                    album_video = true

                    var intent = Intent();

                    println("path ----- ${videoPaths.get(0)}")
                    videoVV.visibility = View.VISIBLE
                    videoLL.visibility = View.VISIBLE
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
                        if (hashtag.size > 0) {
                            for (i in 0 until hashtag.size) {
                                tag += "#" + hashtag.get(i) + " "
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
                    if (images_path != null) {
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

    override fun onBackPressed() {
        val dbManager = DataBaseHelper(context)
        val dataList: Array<String> = arrayOf("*");
        if (addpostTV.text.equals("수정하기")) {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("수정하기를 취소하시겠습니까 ?")

                    .setPositiveButton("계속 수정하기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()

                        Utils.hideKeyboard(this)

                    })
                    .setNegativeButton("삭제하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()

//                        loadData(dbManager,member_id.toString())

                        if (tmpContent.id == null) {
                            Log.d("끝10","")
                            finish()
                        }

                        if (tmpImagesPath != null && tmpImagesPath.size > 0) {
                            dbManager.deleteImagePaths(member_id.toString())
                        }

                        if (tmpContent.id != null) {
                            dbManager.deleteTmpContent(tmpContent.id!!)
                            Log.d("끝11","")
                            finish()
                        }

                        Utils.hideKeyboard(this)


                    })
            val alert = builder.create()
            alert.show()
        } else {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("글쓰기를 취소하시겠습니까 ?")

                    .setPositiveButton("유지하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        val title = Utils.getString(titleET)
//                        val content = Utils.getString(contentET)
                        val content = editor.getContentAsHTML()

                        if (tmpImagesPath != null && tmpImagesPath.size > 0) {
                            dbManager.deleteImagePaths(member_id.toString())
                        }

                        if (tmpContent.id != null) {
                            dbManager.deleteTmpContent(tmpContent.id!!)
                        }

                        val tmpContent = TmpContent(0, member_id.toString(), title, content)

                        dbManager.inserttmpcontent(tmpContent)

                        if (images_path != null && images_path!!.size > 0) {
                            for (i in 0 until images_path!!.size) {
                                val imagesPath = ImagesPath(0, member_id.toString(), images_path!!.get(i), 1)
                                println("imagesPath ${imagesPath.path}")
                                dbManager.insertimagespath(imagesPath)
                            }
                        }

                        if (videoPaths != null && videoPaths.size > 0) {
                            for (i in 0 until videoPaths.size) {
                                val videoPath = ImagesPath(0, member_id.toString(), videoPaths.get(i), 2)
                                println("videoPath ${videoPath.path}")
                                dbManager.insertimagespath(videoPath)
                            }
                        }

                        if (hashtag != null && hashtag.size > 0) {
                            for (i in 0 until hashtag.size) {
                                val hastag = ImagesPath(0, member_id.toString(), hashtag.get(i), 3)
                                println("hastag ${hastag.path}")
                                dbManager.insertimagespath(hastag)
                            }
                        }

                        if (videoIds != null && videoIds.size > 0) {
                            for (i in 0 until videoIds.size) {
                                val video_ids = ImagesPath(0, member_id.toString(), videoIds[i].toString(), 4)
                                println("videoIds ${video_ids.path}")
                                dbManager.insertimagespath(video_ids)
                            }
                        }

                        temp_yn = "N"
                        Log.d("끝12","")
                        finish()

                        Utils.hideKeyboard(this)

                    })
                    .setNegativeButton("삭제하고 나가기", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()

//                    loadData(dbManager,member_id.toString())

                        if (tmpContent.id == null) {
                        }

                        if (tmpImagesPath != null && tmpImagesPath.size > 0) {
                            dbManager.deleteImagePaths(member_id.toString())
                        }

                        if (tmpContent.id != null) {
                            dbManager.deleteTmpContent(tmpContent.id!!)
                        }

                        finish()
                        Utils.hideKeyboard(this)


                    })
            val alert = builder.create()
            alert.show()
        }


    }

    override fun finish() {
        super.finish()

        println(":::::::::::::::::::::::::finishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinishfinish")
    }

}








