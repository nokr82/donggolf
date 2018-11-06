package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_other_manage.*

class OtherManageActivity : RootActivity() {

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_manage)

        context = this

        setpasswordLL.setOnClickListener {
            var intent: Intent = Intent(context, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        setphoneLL.setOnClickListener {
            var intent: Intent = Intent(context, ProfilePhoneChangeActivity::class.java)
            startActivity(intent)
        }

        viewdocumentLL.setOnClickListener {
            var intent: Intent = Intent(context, ViewDocumentActivity::class.java)
            startActivity(intent)
        }

        deletememberLL.setOnClickListener {
            var intent: Intent = Intent(context, WithdrawalActivity::class.java)
            startActivity(intent)
        }

        mutualLL.setOnClickListener {
            var intent: Intent = Intent(context, InquireActivity::class.java)
            startActivity(intent)
        }

        finishLL.setOnClickListener {
            finish()
        }








    }
}
