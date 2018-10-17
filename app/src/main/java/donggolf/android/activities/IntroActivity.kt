package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import donggolf.android.R
import donggolf.android.base.RootActivity



class IntroActivity : RootActivity() {

    private lateinit var context: Context
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        context = this

        mAuth = FirebaseAuth.getInstance();

        val user = mAuth!!.getCurrentUser()

        println("user : ${user?.displayName}")
        println("user : ${user?.email}")
        println("user : ${user?.metadata}")
        println("user : ${user?.uid}")

        if(user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            LoginActivity.setLoginData(context, user)

            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}