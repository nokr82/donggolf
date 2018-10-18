package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.JoinAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.Content
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        context = this

        mAuth = FirebaseAuth.getInstance()

        finishBT.setOnClickListener {
            finish()
        }

        movefindpictureBT.setOnClickListener {
            MoveFindPictureActivity()
        }

        addcontentBT.setOnClickListener {
            addContent()
            Log.d("yjs", "BTClick")
        }

    }


    private fun MoveFindPictureActivity(){
        startActivity(Intent(this,FindPictureActivity::class.java))
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
