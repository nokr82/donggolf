package donggolf.android.activities

import android.content.Context
import android.content.Intent
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
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.activity_mod_status_msg.*
import kotlinx.android.synthetic.main.activity_profile_name_modif.*

class ProfileNameModifActivity : RootActivity() {

    private var mAuth: FirebaseAuth? = null
    lateinit var context : Context

    lateinit var imgl : String
    lateinit var imgs : String
    var lastN : Long = 0
    lateinit var nick : String
    lateinit var sex : String
    lateinit var sTag : ArrayList<String>
    lateinit var statusMessage : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_name_modif)

        //nameET.setText("")

        context = this

        finishNameLL.setOnClickListener {
            finish()
        }

        nameET.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 입력되는 텍스트에 변화가 있을 때 호출된다.
            }

            override fun afterTextChanged(count: Editable) {
                // 입력이 끝났을 때 호출된다.

                nickLettersCnt.setText(Integer.toString(nameET.text.toString().length))
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // 입력하기 전에 호출된다.
            }
        })

        btnNickDel.setOnClickListener {
            nameET.setText("")
        }

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()
        val db = FirebaseFirestore.getInstance()

        var uid = PrefUtils.getStringPreference(context, "uid")
        //println("uid====$uid")
        ProfileAction.viewContent(uid) { success: Boolean, data: Map<String, Any>?, exception: Exception? ->
            statusMessage = data!!.get("state_msg") as String

            imgl = data!!.get("imgl") as String
            imgs = data!!.get("imgs") as String
            lastN = data!!.get("last") as Long
            nick = data!!.get("nick") as String
            sex = data!!.get("sex") as String
            sTag = data!!.get("sharpTag") as ArrayList<String>

            nameET.setText(nick)
        }

        nick_ok.setOnClickListener {
            //DB에 저장하고 finish
            nick = nameET.text.toString()
            val item = Users(imgl, imgs, lastN, nick, sex, sTag, statusMessage)

            FirebaseFirestoreUtils.save("users", uid, item) {
                if (it) {
                    var itt = Intent()
                    itt.putExtra("newNick", nick)
                    setResult(RESULT_OK, itt)
                    finish()
                } else {

                }
            }
        }

    }
}
