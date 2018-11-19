package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.R
import donggolf.android.actions.InfoAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : RootActivity() {

    private lateinit var context: Context
    private lateinit var mAuth: FirebaseAuth

    var autoLogin = false
    var atEmail : String = ""
    var atPassword : String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)




        val params = HashMap<String, Any>()
        params["standby"]


        val db = FirebaseFirestore.getInstance()

        db.runTransaction {
            val docRef = db.collection("mates").document("5DaIBOw2yOaKjKFvwsVj8uva1XO2")
            val doc = it.get(docRef)
            val data = doc.data

            println(data)

            println("------------------------------")

            val standby = data!!["standby"] as HashMap<String, Any>

            val newData = HashMap<String, Any>()
            newData["block"] = true
            standby["www"] = newData



            it.update(docRef, data)

            println(standby)


        }



        context = this

        mAuth = FirebaseAuth.getInstance()

        autoLogin = PrefUtils.getBooleanPreference(context, "auto", false)
        atEmail = PrefUtils.getStringPreference(context, "email", "")
        atPassword = PrefUtils.getStringPreference(context, "pass", "")
        /*atEmail = "devstories@devstories.com"
        atPassword = "123456"*/
        if (atEmail.isEmpty() || atPassword.isEmpty())
            return

        println("autoLogin========$autoLogin")
        if (autoLogin){
            autologinCB.isChecked = true
        } else {
            autologinCB.isChecked = false
        }

        if(!(atEmail.equals("")) && !(atPassword.equals("")) && autoLogin){
            loginHandler.sendEmptyMessage(0)
        }

        btn_login.setOnClickListener {
            login()
        }

        btn_nomember_login.setOnClickListener {
            nomemberlogin()
        }

        linear_go_findid.setOnClickListener {
            movefindid()
        }

        linear_go_register.setOnClickListener {
            moveregister()
        }

        findpasswordLL.setOnClickListener {
            movefinPassword()
        }

        autologinCB.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("로그인상태를 유지하시겠습니까?\n타인의 개인정보 도용에 주의하시기 바랍니다.")

                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()

                        autoLogin = autologinCB.isChecked

                    })
                    .setNegativeButton("취소",DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                    })
            val alert = builder.create()
            alert.show()


        }

    }

    //login_hdr
    internal var loginHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            login()
        }
    }

    private fun login() {

        var email = Utils.getString(emailET)
        if (autoLogin){
            email = atEmail
        }
        if (email.isEmpty()) {
            Utils.alert(context, "아이디는 필수 입력입니다.")
            return
        }

        var password = Utils.getString(passwordET)
        if (autoLogin){
            password = atPassword
        }
        if (password.isEmpty()) {
            Utils.alert(context, "비밀번호는 필수 입력입니다.")
            return
        }

        /*if (autoLogin){
            email = atEmail
            password = atPassword
        } else {
            email = Utils.getString(emailET)
            password = Utils.getString(passwordET)
        }*/


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth.getCurrentUser()


                        LoginActivity.setLoginData(context, user)

                        InfoAction.getInfo(user!!.uid) { success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                            if(success) {
                                println("data : $data")

                                LoginActivity.setInfoData(context, data)


                                println("autoLogin ==== $autoLogin")

                                if (autoLogin){

                                    PrefUtils.setPreference(context, "email", email)
                                    PrefUtils.setPreference(context, "pass", password)
                                    PrefUtils.setPreference(context, "auto", autoLogin)

                                } else {

                                    PrefUtils.setPreference(context, "email", null)
                                    PrefUtils.setPreference(context, "pass", null)
                                    PrefUtils.setPreference(context, "auto", autoLogin)
                                }

                                startActivity(Intent(context, MainActivity::class.java))

                                finish()

                            } else {

                            }
                        }

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

    fun movefindid() {
        val intent = Intent(this, FindidActivity::class.java)
        intent.putExtra("find",1)
        startActivity(intent)
    }

    fun movefinPassword(){
        val intent = Intent(this, FindidActivity::class.java)
        intent.putExtra("find",2)
        startActivity(intent)
    }

    fun moveregister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    companion object {
        fun setLoginData(context: Context, user: FirebaseUser?) {
            println("loginActivity user : $user")

            val uid = user?.uid
            val email = user?.email

            PrefUtils.setPreference(context, "uid", uid)
            PrefUtils.setPreference(context, "email", email)
        }

        fun setInfoData(context: Context, info: Map<String, Any>?) {

            println("loginActivity info : $info")

            val sex = Utils.getString(info, "sex")
            val phone = Utils.getString(info, "phone")
            val nick = Utils.getString(info, "nick")


            PrefUtils.setPreference(context, "sex", sex)
            PrefUtils.setPreference(context, "phone", phone)
            PrefUtils.setPreference(context, "nick", nick)

        }
    }


}
