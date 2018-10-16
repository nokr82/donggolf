package donggolf.android.activities

import android.content.Intent
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : RootActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener{
            // if()
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        btn_nomember_login.setOnClickListener{
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        linear_go_findid.setOnClickListener {
            startActivity(Intent(this,AddPostActivity::class.java))
        }

        linear_go_register.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

    }
}
