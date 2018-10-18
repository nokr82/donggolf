package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import donggolf.android.R
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : RootActivity() {

    private lateinit var context: Context
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        context = this

        mAuth = FirebaseAuth.getInstance();

        btn_login.setOnClickListener {
            login()
        }

        btn_nomember_login.setOnClickListener {
            nomemberlogin()
        }

        linear_go_findid.setOnClickListener {
            moveaddpost()
        }

        linear_go_register.setOnClickListener {
            moveregister()
        }

        autologinBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("로그인상태를 유지하시겠습니까?\n타인의 개인정보 도용에 주의하시기 바랍니다.")

                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                    })
                    .setNegativeButton("취소",DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                    })
            val alert = builder.create()
            alert.show()


        }

    }

    private fun login() {
        val email = Utils.getString(emailET)
        val password = Utils.getString(passwordET)

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth.getCurrentUser()

                        LoginActivity.setLoginData(context, user)

                        startActivity(Intent(context, MainActivity::class.java))

                        finish()

                    } else {
                        // If sign in fails, display a messa to the user.
                        Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    fun nomemberlogin() {
        val email = emailET.text.toString()
        val password = passwordET.text.toString()


    }

    fun moveaddpost() {
        startActivity(Intent(this, AddPostActivity::class.java))
    }

    fun moveregister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    companion object {
        fun setLoginData(context: Context, user: FirebaseUser?) {

            println(user)

            val uid = user?.uid
            val email = user?.email

            PrefUtils.setPreference(context, "uid", uid)
            PrefUtils.setPreference(context, "email", email)
        }

        fun setInfoData(context: Context, info: Map<String, Any>?) {

            println(info)

            val sex = Utils.getString(info, "sex")
            val phone = Utils.getString(info, "phone")
            val nick = Utils.getString(info, "nick")

            PrefUtils.setPreference(context, "sex", sex)
            PrefUtils.setPreference(context, "phone", phone)
            PrefUtils.setPreference(context, "nick", nick)
        }
    }


}
