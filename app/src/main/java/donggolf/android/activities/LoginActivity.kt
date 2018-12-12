package donggolf.android.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
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
import android.provider.SyncStateContract.Helpers.update
import android.content.pm.PackageManager
import com.google.android.gms.common.util.ClientLibraryUtils.getPackageInfo
import android.content.pm.PackageInfo
import android.util.Base64
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.squareup.okhttp.internal.Util
import cz.msebera.android.httpclient.Header
import donggolf.android.actions.MemberAction
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class LoginActivity : RootActivity() {

    private lateinit var context: Context
    private var progressDialog: ProgressDialog? = null
    private lateinit var mAuth: FirebaseAuth

    var autoLogin = false
    var email = ""
    var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        context = this
        progressDialog = ProgressDialog(context)

        //getKeyHash(context)

        mAuth = FirebaseAuth.getInstance()

        //PrefUtils.clear(context)

        autoLogin = PrefUtils.getBooleanPreference(context, "auto")

        println("autoLogin========$autoLogin")
        if (autoLogin){
            //autologinCB.isChecked = true
            if (progressDialog != null) {
                progressDialog!!.setMessage("loading...")
                progressDialog!!.show()
            }
            login()

        } else {
            PrefUtils.clear(context)
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

                        autologinCB.isChecked = true

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

        if (PrefUtils.getBooleanPreference(context, "auto") == null){

            email = Utils.getString(emailET)
            password = Utils.getString(passwordET)

        } else if (PrefUtils.getBooleanPreference(context,"auto")){

            email = PrefUtils.getStringPreference(context, "email")
            password = PrefUtils.getStringPreference(context, "pass")

        } else {

            //PrefUtils.clear(context)
            email = Utils.getString(emailET)
            if (email.isEmpty()) {
                Utils.alert(context, "아이디는 필수 입력입니다.")
                return
            }

            password = Utils.getString(passwordET)
            if (password.isEmpty()) {
                Utils.alert(context, "비밀번호는 필수 입력입니다.")
                return
            }
        }

        val params = RequestParams()
        params.put("email", email)
        params.put("passwd", password)

        MemberAction.member_login(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                try {
                    val result = response!!.getString("result")
                    println("LoginActivity :: ${response.toString()}")
                    if (result == "ok") {
                        //Utils.alert(context,"로그인 성공")
                        if (progressDialog != null) {
                            progressDialog!!.dismiss()
                        }
                        val member = response.getJSONObject("member")

                        val member_id = Utils.getInt(member, "id")

                        println("member_id -------$member_id")
                        PrefUtils.setPreference(context, "member_id", member_id)

                        if (autologinCB.isChecked){
                            PrefUtils.setPreference(context, "email", email)
                            PrefUtils.setPreference(context, "pass", password)
                            PrefUtils.setPreference(context, "auto", true)
                        } else {
                            //PrefUtils.setPreference(context, "auto", false)
                        }

                        startActivity(Intent(context, MainActivity::class.java))

                        finish()

                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                //super.onFailure(statusCode, headers, responseString, throwable)
                Utils.alert(context, "로그인에 실패했습니다.")
            }

        })

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
        println("moveRegister()")
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

    fun getKeyHash(context: Context) : String? {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }

        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
