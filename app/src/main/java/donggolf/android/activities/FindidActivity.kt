package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.InfoAction
import donggolf.android.actions.MemberAction
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.Info
import kotlinx.android.synthetic.main.activity_findid.*
import kotlinx.android.synthetic.main.activity_main_detail.*
import org.json.JSONObject

class FindidActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var context: Context

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
            findid()
        }



    }

    fun findid() {

        val category = intent.getIntExtra("find",0)

        val phone = Utils.getString(phoneET)
        if (phone.isEmpty()) {
            Utils.alert(context, "전화번호를 입력하세요.")
            return
        }

        if(category == 1){
            val params = RequestParams()
            params.put("phone", phone)

            MemberAction.find_id(params, object :JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        //println("아이디 찾기 :: ${response.toString()}")
                        val member = response.getJSONObject("member")
                        var email = Utils.getString(member, "email")
                        //println("찾은 ID :: $member_id")

                        if (!email.isEmpty()) {
                            guideTV.text = "전송된 코드를 입력해주세요"
                            phoneET.setHint("코드 입력")
                            findBT.setOnClickListener {
                                //JoinController forget_pwd.json
                            }
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

        }else {

            val params = RequestParams()
            params.put("phone", phone)

            MemberAction.find_id(params, object :JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        //println("아이디 찾기 :: ${response.toString()}")
                        val member = response.getJSONObject("member")
                        var email = Utils.getString(member, "email")
                        //println("찾은 ID :: $member_id")

                        if (!email.isEmpty()) {

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
        }



    }
}

