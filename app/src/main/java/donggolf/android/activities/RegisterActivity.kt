package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : RootActivity() {

    //private lateinit var mAuth: FirebaseAuth

    private lateinit var context: Context

    private var gender: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        context = this

        //mAuth = FirebaseAuth.getInstance()

        finishBT.setOnClickListener {
            finish()
        }

        btn_success_register.setOnClickListener {
            join()
        }

        // 라디오 버튼 디폴트 값 주기
        checkRadioboxes()

        checkAll()

    }

    private fun checkAll() {

        // 모두동의
        allCheckCB.setOnClickListener {
            if (allCheckCB.isChecked) {
                agreeCB.isChecked = true
                privacyCB.isChecked = true
            } else {
                agreeCB.isChecked = false
                privacyCB.isChecked = false
            }
        }

        // 이용약관
        agreeCB.setOnClickListener {
            allCheckCB.isChecked = agreeCB.isChecked && privacyCB.isChecked
        }

        // 개인정보
        privacyCB.setOnClickListener {
            allCheckCB.isChecked = agreeCB.isChecked && privacyCB.isChecked
        }

    }


    private fun checkRadioboxes() {

        // 라디오 버튼 디폴트 값
        if (!maleRB.isChecked && !femaleRB.isChecked) {
            maleRB.isChecked = true
        }

    }


    private fun join() {

        // 아이디 체크
        val email = Utils.getString(emailET)
        if (email.isEmpty()) {
            Utils.alert(context, "아이디는 필수 입력입니다.")
            return
        } else {
            val tmpparam = RequestParams()
            tmpparam.put("member_id", email)

            MemberAction.is_duplicated_id(tmpparam, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    try {
                        val result = response!!.getString("result")
                        if (result == "overlap") {
                            emailET.setText("")
                            Utils.alert(context, response!!.getString("result"))
                        }
                    } catch (e : JSONException) {
                        e.printStackTrace()
                    }
                }
            })

        }

        // 이메일 형식 체크
        if(!Utils.isValidEmail(email)){
            Utils.alert(context, "정확한 이메일을 입력해주세요..")
            return
        }

        // 비밀번호 체크
        val password = Utils.getString(passwordET)
        if (password.isEmpty()) {
            Utils.alert(context, "비밀번호를 입력해주세요.")
            return
        }

        // 비밀번호 체크
        val passwordre = Utils.getString(repasswordET)
        if (passwordre.isEmpty()) {
            Utils.alert(context, "비밀번호를 입력해주세요.")
            return
        }

        // 비밀번호 글자수 체크
        if (password.length < 2 || password.length > 7) {
            Utils.alert(context, "글자 수가 2 ~ 7 글자 인지 확인해주세요.")
            return
        }

        // 비밀번호 글자수 체크
        if (passwordre.length < 2 || passwordre.length > 7) {
            Utils.alert(context, "글자 수가 2 ~ 7 글자 인지 확인해주세요.")
            return
        }

        // 비밀번호 같은지 체크
        if (password != passwordre) {
            Utils.alert(context, "비밀번호가 다릅니다.")
            return
        }

        // 핸드폰 체크
        val phone = Utils.getString(phoneET)
        if (phone.isEmpty()) {
            Utils.alert(context, "핸드폰 번호를 입력해주세요.")
            return
        }

        // 닉네임 체크
        val nickName = Utils.getString(nickNameET)
        if (nickName.isEmpty()) {
            Utils.alert(context, "닉네임을 입력해주세요.")
            return
        }

        // 라디오 버튼 값주기
        gender = if (this.radio_gender.checkedRadioButtonId == R.id.maleRB) {

            // 남자
            0
        } else {

            // 여자
            1
        }

        // 모두 동의 체크
        if (!allCheckCB.isChecked) {
            Utils.alert(context, "문서/앱 권한 전체동의를 체크해 주세요.")
            return
        }

        val params = RequestParams()

        params.put("email", email)
        params.put("passwd", password)
        params.put("phone", phone)
        params.put("sex", gender)
        params.put("nick", nickName)

        MemberAction.join_member(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                Utils.alert(context, response!!.getString("message"))
                finish()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                Utils.alert(context, "서버에 접속 중 문제가 발생했습니다.\n재시도해주십시오.")
            }

        })

        /* mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = mAuth.currentUser
                    if(user != null) {
                        val uid = user.uid

                        val info = Info(phone = phone, nick = nickName, sex = (if (gender == 0) "M" else "F"), agree = true)
                        JoinAction.join(uid, info) {

                            println("it : $it")

                            if(it) {
                                InfoAction.getInfo(uid) { success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                                    if(success) {
                                        println("data : $data")

                                        var newbie = user?.uid
                                        //println("newbie uid============================$newbie 가입 성공")

                                        var imgl = ""
                                        var imgs = ""
                                        var lastN = 0L
                                        var nick = nickName
                                        var sex = (if (gender == 0) "M" else "F")
                                        var sTag:ArrayList<String> = ArrayList<String>()
                                        sTag.add("")
                                        var statusMessage = ""

                                        val item = Users(imgl, imgs, lastN, nick, sex, sTag, statusMessage)

                                        FirebaseFirestoreUtils.save("users", uid, item) {
                                            if (it) {
                                                println("성공적으로 가입되었습니다")
                                                finish()
                                            } else {

                                            }
                                        }

                                        LoginActivity.setInfoData(context, data)


                                        finish()

                                    } else {

                                    }
                                }
                            } else {

                            }

                        }
                    }
                }
                .addOnFailureListener {
                    println("fa : $it")
                    it.printStackTrace()
                }*/

    }
}
