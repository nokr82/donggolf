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

        btn_finish.setOnClickListener {
            finish()
        }

        btn_success_register.setOnClickListener {
            join()
        }

        checkRadioboxes()         //라디오 버튼 디폴트 값 주기

        checkAll()

    }

    private fun checkAll() {
        allCheckCB.setOnClickListener {
            //모두동의
            if (allcheckd.isChecked) {
                agreeCB.isChecked = true
                privacyCB.isChecked = true
            } else {
                conditionscheckbox.isChecked = false
                collectcheckbox.isChecked = false
            }
        }

        conditionscheckbox.setOnClickListener {
            //이용약관
            allcheckd.isChecked = conditionscheckbox.isChecked && collectcheckbox.isChecked
        }

        collectcheckbox.setOnClickListener {
            //개인정보
            allcheckd.isChecked = conditionscheckbox.isChecked && collectcheckbox.isChecked
        }
    }

    private fun checkRadioboxes() {

        if (!radio_btn_male.isChecked && !radio_btn_female.isChecked) {       //라디오 버튼 디폴트 값
            radio_btn_male.isChecked = true
        }

    }


    private fun join() {

        val email = Utils.getString(registeremailET)
        if (email.isEmpty()) {                   //아이디 체크
            Utils.alert(context, "아이디는 필수입력입니다.")
            return
        }

        val password = Utils.getString(registerpasswordET)
        if (password.isEmpty()) {         //비밀번호 체크
            Utils.alert(context, "비밀번호를 입력해주세요.")
            return
        }

        val passwordre = Utils.getString(registerpasswordreET)
        if (passwordre.isEmpty()) {         //비밀번호 체크
            Utils.alert(context, "비밀번호를 입력해주세요.")
            return
        }

        if (password.length < 2 || password.length > 7) {         //비밀번호 글자수 체크
            Utils.alert(context, "글자 수를 확인해주세요.")
            return
        }

        if (passwordre.length < 2 || passwordre.length > 7) {       //비밀번호 글자수 체크
            Utils.alert(context, "글자 수를 확인해주세요.")
            return
        }

        if (password != passwordre) {       //비밀번호 같은지 체크
            Utils.alert(context, "비밀번호가 다릅니다.")
            return
        }

        val phone = Utils.getString(registerphoneET)
        if (phone.isEmpty()) {            //핸드폰 체크
            Utils.alert(context, "핸드폰 번호를 입력해주세요.")
            return
        }

        val nickName = Utils.getString(nickNameET)
        if (nickName.isEmpty()) {         //닉네임 체크
            Utils.alert(context, "닉네임을 입력해주세요.")
            return
        }

        if (this.radio_gender.checkedRadioButtonId == R.id.radio_btn_male) {     //라디오 버튼 값주기
            gender = 0          //남자
        } else {
            gender = 1          //여자
        }

        // 모두 동의 체크
        if (!allcheckd.isChecked) {
            Utils.alert(context, "약관 동의를 해주세요.")
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
