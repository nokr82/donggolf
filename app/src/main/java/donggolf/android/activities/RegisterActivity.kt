package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import donggolf.android.R
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : RootActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var context: Context
    var gender : Int? = null
    var checkd : Boolean ? = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        context = this

        mAuth = FirebaseAuth.getInstance();

        btn_finish.setOnClickListener {
            finish()
        }
        btn_success_register.setOnClickListener {
            join()
        }
        radioboxCheck()
        checkboxCheck()


    }
    fun radioboxCheck(){
        if(radio_btn_male.isChecked == false && radio_btn_female.isChecked == false){
            radio_btn_male.setChecked(true)
        }
    }


    fun checkboxCheck(){
        allcheckd.setOnClickListener {
            if(allcheckd.isChecked == true){
                checkbox1.setChecked(true)
                checkbox2.setChecked(true)
                checkd = true
            }else if(allcheckd.isChecked == false){
                allcheckd.setChecked(false)
                checkbox1.setChecked(false)
                checkbox2.setChecked(false)
                checkd = false
            }
        }

        checkbox1.setOnClickListener {
            if(checkbox1.isChecked == true && checkbox2.isChecked == true){
                allcheckd.setChecked(true)
                checkd == true
            }else {
                allcheckd.setChecked(false)
                checkd == false
            }
        }

        checkbox2.setOnClickListener {
            if(checkbox1.isChecked == true && checkbox2.isChecked == true){
                allcheckd.setChecked(true)
                checkd == true
            }else {
                allcheckd.setChecked(false)
                checkd == false
            }
        }
    }

    fun join() {
        val email = Utils.getString(registeremailET)
        if(email.isEmpty()) {
            Utils.alert(context, "아이디는 필수입력입니다.")
            return
        }

        val password = Utils.getString(registerpasswordET)
        val password2 = Utils.getString(registerpasswordET2)

        if(password.isEmpty()){
            Utils.alert(context, "비밀번호를 입력해주세요.")
            return
        }else if(password2.isEmpty()){
            Utils.alert(context, "비밀번호를 입력해주세요.")
            return
        }else if(password!=password2){
            Utils.alert(context, "비밀번호가 다릅니다.")
            return
        }

        if(password.length >= 2 && password.length <= 7){
            checkd = true
        }else if(password2.length >= 2 && password2.length <= 7){
            checkd = true
        }else {
            checkd = false
        }

        val phone = Utils.getString(registerphoneET)

        if(phone.isEmpty()){
            Utils.alert(context, "핸드폰 번호를 입력해주세요.")
            return
        }

        val nickname = Utils.getString(registernicknameET)

        if(nickname.isEmpty()){
            Utils.alert(context, "닉네임을 입력해주세요.")
            return
        }



        if(radio_gender.checkedRadioButtonId == R.id.radio_btn_male){
            gender = 0          //남자
        }else {
            gender = 1          //여자
        }

        if(allcheckd.isChecked == true){
            mAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                        override fun onComplete(task: Task<AuthResult>) {
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                val user = mAuth.getCurrentUser()


                                println("user : $user")

                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
            finish()
        }else {
                Utils.alert(context, "약관 동의를 해주세요.")
                return
        }




    }
}
