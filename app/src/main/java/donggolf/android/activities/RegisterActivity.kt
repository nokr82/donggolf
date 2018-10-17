package donggolf.android.activities

import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : RootActivity() {

    var gander : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_finish.setOnClickListener {
            finish()
        }
        btn_success_register.setOnClickListener {
            SuccessRegister()
            finish()
        }


    }

    fun SuccessRegister(){

        val email = Utils.getString(RegisteremailET)

        val password = Utils.getString(RegisterPasswordET)
        val password2 = Utils.getString(RegisterPasswordET2)

        val phone = Utils.getString(RegisterPhoneET)

        val nickname = Utils.getString(RegisterNickNameET)

        if(radio_gender.checkedRadioButtonId == R.id.radio_btn_male){
            gander = 0          //남자
        }else {
            gander = 1          //여자
        }

    }
}
