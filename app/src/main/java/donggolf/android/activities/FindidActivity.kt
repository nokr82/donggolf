package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.squareup.okhttp.internal.Util
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.actions.MemberAction.find_id
import donggolf.android.base.AlertListener
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_findid.*
import org.json.JSONObject

class FindidActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var context: Context

    var codeType = 0
    var getCode = ""
    var tmpMemId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findid)

        context = this

        mAuth = FirebaseAuth.getInstance()

        val category = intent.getIntExtra("find",0)
        if(category == 1){
            hintTV.text = "아이디 찾기"
        }else {
            hintTV.text = "비밀번호 재발급"
        }

        finishBT.setOnClickListener {
            finish()
        }

        findBT.setOnClickListener {
            if (codeType == 0) {

                findId()

            } else if (codeType == 1) {

                matchingCode(getCode)

            } else if (codeType == 2) {

                setNewPass()

            }
        }


    }

    fun findId() {

        val category = intent.getIntExtra("find",0)

        val phone = Utils.getString(phoneET)
        if (phone.isEmpty()) {
            Utils.alert(context, "전화번호를 입력하세요.")
            return
        }

        if(category == 1){
            val params = RequestParams()
            params.put("phone", phone)

            find_id(params, object :JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        //println("아이디 찾기 :: ${response.toString()}")
                        val member = response.getJSONObject("member")
                        var email = Utils.getString(member, "email")
                        //println("찾은 ID :: $member_id")

                        if (!email.isEmpty()) {
                            idhintTV.text = "아이디 힌트는 "
                            useridTV.text = email.substring(0, 2) + "*****" + "@" + email.substringAfter("@", email)
                            id_hint_endTV.visibility = View.VISIBLE
                            id_hint_endTV.text = "입니다."
                        }

                    } else {

                        idhintTV.text = "가입된 정보가 없습니다."
                        useridTV.text = "\t회원가입"
                        id_hint_endTV.visibility = View.GONE

                        useridTV.setOnClickListener {
                            startActivity(Intent(context, RegisterActivity::class.java))
                        }

                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                }
            })

        }else {//비밀번호 찾기

            val params = RequestParams()
            params.put("phone", phone)

            find_id(params, object :JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        println(response)
                        val member = response.getJSONObject("member")
                        var email = Utils.getString(member, "email")
                        //println("찾은 ID :: $member_id")
                        tmpMemId = Utils.getInt(member, "id")

                        if (!email.isEmpty()) {
                            //해당 폰번으로 가입된 계정이 있는 경우
                            val params = RequestParams()
                            params.put("is_user", "yes")

                            MemberAction.getPassCode(params, object : JsonHttpResponseHandler() {
                                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                                    guideTV.text = "전송된 코드를 입력해주세요."
                                    phoneET.inputType = InputType.TYPE_CLASS_TEXT
                                    phoneET.setText("")
                                    phoneET.hint = "코드 입력"
                                    val result = response!!.getString("result")
                                    println(response)
                                    if (result == "ok") {
                                        //codeType 변경, getCode 세팅
                                        codeType = 1
                                        getCode = response.getString("code")
                                        //폰으로 메세지 전송
                                    }
                                }
                            })

                            /*findBT.setOnClickListener {
                                //JoinController forget_pwd.json
                            }*/
                        } else {
                            //가입자가 아님
                            idhintTV.text = "가입된 정보가 없습니다."
                            useridTV.text = "\t회원가입"
                            id_hint_endTV.visibility = View.GONE

                            useridTV.setOnClickListener {
                                startActivity(Intent(context, RegisterActivity::class.java))
                            }
                        }

                    } else {
                        Log.e("Error", "JSON action error")
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                    println(errorResponse)
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                    println(responseString)
                }
            })
        }



    }

    fun matchingCode(codeStr : String) {
        if (codeStr == Utils.getString(phoneET)){
            guideTV.text = " 새 비밀번호를 입력하세요."
            phoneET.setText("")
            //phoneET.transformationMethod = HideReturnsTransformationMethod.getInstance()
            //phoneET.inputType = InputType.TYPE_MASK_FLAGS
            phoneET.hint = "2~7자리"
            codeType = 2
        } else {
            Utils.alert(context,"입력하신 코드가 일치하지 않습니다.\n다시 확인해주세요")
        }

    }

    fun setNewPass() {

        val params = RequestParams()
        params.put("member_id", tmpMemId)
        params.put("type", "passwd")
        params.put("update", phoneET.text)

        MemberAction.update_info(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val result = response!!.getString("result")
                if (result == "ok") {
                    Utils.alert(context, "비밀번호가 변경되었습니다.\n새로운 비밀번호로 로그인해주세요.")
                    finish()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }

        })

    }



}

