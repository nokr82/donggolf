package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.JoinAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.Content
import kotlinx.android.synthetic.main.activity_add_post.*
import java.net.URI



class AddPostActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        context = this

        mAuth = FirebaseAuth.getInstance()

        permission()

        finishBT.setOnClickListener {
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
            moveMyPicture()
        }

        addcontentBT.setOnClickListener {
            addContent()
        }





    }

    private fun moveMyPicture(){
        startActivity(Intent(this,FindPictureActivity::class.java))
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

            val content = Content(now,0,0,"test","incheon","제목 테스트 입니다.","테스트 입니다.",0,false,
                    0,0, "고발자 닉네임 테스트",false,0,false)

            ContentAction.saveContent(content){success: Boolean, key: String?, exception: Exception? ->

                if (success){

                    finish()

                } else {

                    Log.d("yjs", "excption")

                }
            }

        }
    }

}
