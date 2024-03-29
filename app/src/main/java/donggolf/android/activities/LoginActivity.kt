package donggolf.android.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import donggolf.android.R
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_login.*
import android.content.pm.PackageManager
import android.util.Base64
import android.view.View
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
//    private lateinit var mAuth: FirebaseAuth
    var sidotype = ""
    var goguntype = ""
    var region_id = ""
    var autoLogin = false
    var email = ""
    var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        context = this
        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

//        PrefUtils.setPreference(context, "sidotype", sidotype)
//        PrefUtils.setPreference(context, "goguntype", goguntype)
//        PrefUtils.setPreference(context, "region_id", region_id)

        //getKeyHash(context)

//        mAuth = FirebaseAuth.getInstance()

        //PrefUtils.clear(context)

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

        autologinRL.setOnClickListener {
            if (checkIV.visibility == View.GONE) {
                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("로그인상태를 유지하시겠습니까?\n타인의 개인정보 도용에 주의하시기 바랍니다.")

                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                            checkIV.visibility = View.VISIBLE
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()

                        })
                val alert = builder.create()
                alert.show()
            } else {
                checkIV.visibility = View.GONE
            }
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

    private fun login() {

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

        val params = RequestParams()
        params.put("email", email)
        params.put("passwd", password)

        MemberAction.member_login(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                try {
                    val result = response!!.getString("result")
                    // println("LoginActivity :: ${response.toString()}")
                    if (result == "ok") {
                        //Utils.alert(context,"로그인 성공")
                        if (progressDialog != null) {
                            progressDialog!!.dismiss()
                        }
                        val member = response.getJSONObject("member")

                        val admin_del_yn = Utils.getString(member,"admin_del_yn")
                        val admin_day_del_yn = Utils.getString(member,"admin_day_del_yn")
                        val admin_day = Utils.getString(member,"admin_day")
                        val del_day = response.getString("del_day")
                        if (admin_del_yn == "Y"){
                            alert("동네골프 접근이 영구차단되었습니다.")
                            return
                        }else if (admin_day_del_yn == "Y"){
                            alert("동네골프 접근이 "+admin_day+"부터 "+del_day+"까지 차단되었습니다.")
                            return
                        }
                        val member_id = Utils.getInt(member, "id")

                        // println("member_id -------$member_id")
                        PrefUtils.setPreference(context, "member_id", member_id)
                        PrefUtils.setPreference(context,"nickname", Utils.getString(member,"nick"))

                        val isActive = Utils.getString(member,"status")
                        if (isActive == "a") {

                            if (checkIV.visibility == View.VISIBLE) {
                                PrefUtils.setPreference(context, "email", email)
                                PrefUtils.setPreference(context, "pass", password)
                                PrefUtils.setPreference(context, "auto", true)

                            } else {
                                PrefUtils.setPreference(context, "email", email)
                                PrefUtils.setPreference(context, "pass", password)
                                PrefUtils.setPreference(context, "auto", false)
                                //PrefUtils.setPreference(context, "auto", false)
                            }
                            var region = ""
                            if (Utils.getString(member, "region1")!=""){
                                region += Utils.getString(member, "region1")
                            }
                            if (Utils.getString(member, "region2")!=""){
                                region  += ","+Utils.getString(member, "region2")
                            }
                            if (Utils.getString(member, "region3")!=""){
                                region +=  ","+Utils.getString(member, "region3")
                            }
                            PrefUtils.setPreference(context,"region_id",region)
                            PrefUtils.setPreference(context,"region",Utils.getString(member, "region1"))
                            PrefUtils.setPreference(context,"isActiveAccount","a")
                            PrefUtils.setPreference(context,"userPhone",Utils.getString(member,"phone"))

                            startActivity(Intent(context, MainActivity::class.java))

                            finish()
                        }
                        else {

                            val builder = AlertDialog.Builder(context)
                            builder
                                    .setMessage("휴면계정을 해제 하시겠습니까 ?")

                                    .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->

                                        updateDormancy(member_id)
                                        dialog.cancel()

                                    })
                                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                                        PrefUtils.setPreference(context,"isActiveAccount", "i")
                                        dialog.cancel()
                                    })

                            val alert = builder.create()
                            alert.show()



//                            PrefUtils.setPreference(context,"isActiveAccount", "i")
//                            Toast.makeText(context,"휴면 계정입니다. 문의해주세요.", Toast.LENGTH_SHORT).show()
                        }

                    }else{
                        Toast.makeText(context,"일치하는 회원이 존재하지 않습니다.",Toast.LENGTH_SHORT).show()
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
        startActivity(Intent(context, MainActivity::class.java))
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
        // println("moveRegister()")
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    fun updateDormancy(member_id:Int){
        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("type","status")
        params.put("update", "a")
        MemberAction.update_info(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok"){
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                // println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }
        })
    }



    override fun onDestroy() {
        super.onDestroy()
    }

}
