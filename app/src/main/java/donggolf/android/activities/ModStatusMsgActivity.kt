package donggolf.android.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.R
import donggolf.android.actions.ProfileAction
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.models.Content
import donggolf.android.models.HashTag
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.activity_mod_status_msg.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile_tag_change.*


class ModStatusMsgActivity : RootActivity() {

    private var mAuth: FirebaseAuth? = null
    lateinit var context :Context

    lateinit var imgl : String
    lateinit var imgs : String
    var lastN : Long = 0
    lateinit var nick : String
    lateinit var sex : String
    lateinit var sTag : ArrayList<String>
    lateinit var statusMessage : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_status_msg)

        context = this

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()
        val db = FirebaseFirestore.getInstance()

        finishaLL.setOnClickListener {
            finish()
        }

        statusMsg.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 입력되는 텍스트에 변화가 있을 때 호출된다.
            }

            override fun afterTextChanged(count: Editable) {
                // 입력이 끝났을 때 호출된다.

                statusLen.setText(Integer.toString(statusMsg.text.toString().length))
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // 입력하기 전에 호출된다.
            }
        })

        statusTxDel.setOnClickListener {
            statusMsg.setText("")
        }
        //pid : PK라 가져와서 Action류의 함수에 첫번째 파라미터로 넣어줌
        //저장된 users 중 해당 유저의 정보 가져오기
        var uid = PrefUtils.getStringPreference(context, "uid")
//        println("uid====$uid")
        ProfileAction.viewContent(uid) { success: Boolean, data: Map<String, Any>?, exception: Exception? ->
            statusMessage = data!!.get("state_msg") as String

            imgl = data!!.get("imgl") as String
            imgs = data!!.get("imgs") as String
            lastN = data!!.get("last") as Long
            nick = data!!.get("nick") as String
            sex = data!!.get("sex") as String
            sTag = data!!.get("sharpTag") as ArrayList<String>

            statusMsg.setText(statusMessage)
        }

        status_Ok.setOnClickListener {
            //DB에 저장하고 finish
            statusMessage = statusMsg.text.toString()
            val item = Users(imgl, imgs, lastN, nick, sex, sTag, statusMessage)

            FirebaseFirestoreUtils.save("users", uid, item) {
                if (it) {
                    finish()
                } else {

                }
            }
        }
    }
}
