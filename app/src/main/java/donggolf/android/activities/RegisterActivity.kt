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
    var gender : Int? = null

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


    }

    private fun join() {

        val email = Utils.getString(RegisterEmailET)
        if(email.isEmpty()) {
            Utils.alert(context, "아이디는 필수입력입니다.")
            return
        }

        val password = Utils.getString(RegisterPasswordET)
        val password2 = Utils.getString(RegisterPasswordET2)

        val phone = Utils.getString(RegisterPhoneET)

        val nickname = Utils.getString(RegisterNicknameET)

        if(radio_gender.checkedRadioButtonId == R.id.radio_btn_male){
            gender = 0          //남자
        }else {
            gender = 1          //여자
        }

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
    }
}
