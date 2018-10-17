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
            login()
        }

        btn_nomember_login.setOnClickListener{
            nomemberlogin()
        }

        linear_go_findid.setOnClickListener {
            moveaddpost()
        }

        linear_go_register.setOnClickListener {
            moveregister()
        }

    }

    fun login(){
            var email = emailET.text.toString()
            var password = passwordET.text.toString()


            startActivity(Intent(this,MainActivity::class.java))
            finish()
    }
    fun nomemberlogin(){
        var email = emailET.text.toString()
        var password = passwordET.text.toString()


        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    fun moveaddpost(){
        startActivity(Intent(this,AddPostActivity::class.java))
    }
    fun moveregister(){
        startActivity(Intent(this,AddPostActivity::class.java))
    }


}
