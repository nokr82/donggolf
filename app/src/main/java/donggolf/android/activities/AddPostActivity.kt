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
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.Content
import donggolf.android.models.Info
import donggolf.android.models.PhotoData
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_findid.*
import java.net.URI



class AddPostActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var context: Context

    private val FROM_CAMERA: Int = 100

    private val SELECT_PICTURE: Int = 101

    private val SELECT_VIDEO: Int = 102

    var photoList: ArrayList<PhotoData> = ArrayList<PhotoData>()

    var result: ArrayList<String> = ArrayList<String>()

    private var imagesPaths: MutableList<String>? = null

    private var images: MutableList<Bitmap>? = null

    private var videoPaths: MutableList<String>? = null

    private var videos: MutableList<Bitmap>? = null

    private var LoginMember: Info? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        context = this

        mAuth = FirebaseAuth.getInstance()

        val db = FirebaseFirestore.getInstance()

        // Create a new user with a first and last name



        permission()

        finishaBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("글쓰기를 취소하시겠습니까 ?")

                    .setPositiveButton("유지하고 나가기", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()

                    })
                    .setNegativeButton("삭제하고 나가기", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
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
            addContent()
        }

        val email: String = PrefUtils.getStringPreference(context,"email")

        Log.d("yjs", "email : " + email)


    }

    private fun moveMyPicture(){
        var intent = Intent(context, FindPictureGridActivity::class.java);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    private fun moveMyVideo(){
        var intent = Intent(context, FindVideoActivity::class.java);
        startActivityForResult(intent, SELECT_VIDEO);
    }

    private fun permission(){

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {

            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {

            }

        }

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

    }

    private fun addContent(){

        val title = Utils.getString(titleET)
        if (title.isEmpty()){
            Utils.alert(context, "제목을 입력해주세요.")
            return
        }

        var content = Utils.getString(contentET)
        if (content.isEmpty()){
            Utils.alert(context, "내용을 입력해주세요.")
            return
        }

        val user = mAuth.currentUser

        if (user != null){
            val uid = user.uid

            val now = System.currentTimeMillis()

            val email: String = PrefUtils.getStringPreference(context,"email")
            val nick: String = PrefUtils.getStringPreference(context,"nick")

            Log.d("yjs", "nick : " + nick)

            var params = HashMap<String, Any>()
            params.put("uid", uid)

            val title = Utils.getString(titleET)
            val content = Utils.getString(contentET)

            val item = Content(now,0,0,uid,"이미지 경로 test : " + "성공" + "비디오 경로 test : ",title,content,0,false,
                    0,0,  nick.toString(),false,0,false)

            movefindpictureBT.setImageBitmap(images!!.get(0))

            ContentAction.saveContent(item){success: Boolean, key: String?, exception: Exception? ->

                if (success){

                    finish()

                } else {

                    Log.d("yjs", "excption")

                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                SELECT_PICTURE -> {
                    var item = data?.getStringArrayExtra("images")

                    for (i in 0 .. (item!!.size - 1)) {
                        val str = item[i]

                        imagesPaths?.add(str)

                        println("str : " + str)

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

                    var intent = Intent();

                    Log.d("yjs", "PostResult : " + item?.size.toString()+" : " + item?.get(0).toString())

                    setResult(RESULT_OK,intent);

                }
                SELECT_VIDEO->{
                    var item = data?.getStringArrayExtra("videos")

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

                    var intent = Intent();

                    Log.d("yjs", "PostResult : " + item?.size.toString())

                    setResult(RESULT_OK,intent);
                }
            }
        }
    }

}
