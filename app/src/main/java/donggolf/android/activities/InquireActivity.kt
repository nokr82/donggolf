package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import donggolf.android.R
import donggolf.android.actions.InfoAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_inquire.*

class InquireActivity : RootActivity() {

    lateinit var context : Context

    lateinit var email : String
    lateinit var phoneNum : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inquire)

        context = this

        finishLL.setOnClickListener {
            finish()
        }

        goToEmailMod.setOnClickListener {
//            var goit = Intent(context, )
        }

        var uid = PrefUtils.getStringPreference(context, "uid")
        email = PrefUtils.getStringPreference(context, "email")
        if (!email.isEmpty()){
            ans_user_email.setText(email)
        }else return

        InfoAction.getInfo(uid){success, data, exception ->
            if (success){
                phoneNum = data!!.get("phone") as String
                mobNum.setText(phoneNum)
            }else{
                println("앗아아..")
            }
        }


    }
}
