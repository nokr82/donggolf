package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import donggolf.android.R
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var context: Context

    var gender: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        context = this

        mAuth = FirebaseAuth.getInstance()

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
        if (this.radio_gender.checkedRadioButtonId == R.id.radio_btn_male) {

            // 남자
            gender = 0
        } else {

            // 여자
            gender = 1
        }

        // 모두 동의 체크
        if (!allCheckCB.isChecked) {
            Utils.alert(context, "문서/앱 권한 전체동의를 체크해 주세요.")
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                    override fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = mAuth.currentUser


                            println("user : $user")

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        finish()


    }
}
