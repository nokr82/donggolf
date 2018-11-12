package donggolf.android.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import donggolf.android.R
import donggolf.android.actions.NationalAction
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import java.util.*
import java.util.Arrays.asList
import kotlin.collections.HashMap
import java.util.Map.Entry;


class IntroActivity : RootActivity() {

    private lateinit var context: Context
    private var mAuth: FirebaseAuth? = null

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        context = this

        progressDialog = ProgressDialog(context)

        mAuth = FirebaseAuth.getInstance();

        val user = mAuth!!.getCurrentUser()

        println("user : ${user?.displayName}")
        println("user : ${user?.email}")
        println("user : ${user?.metadata}")
        println("user : ${user?.uid}")


        if(user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            val autoLogin = PrefUtils.getBooleanPreference(context, "auto_login")
            if(autoLogin) {
                LoginActivity.setLoginData(context, user)
                println("autoLogin =============== intro $autoLogin")
                startActivity(Intent(this, MainActivity::class.java))
            } else {

                FirebaseAuth.getInstance().signOut()

                startActivity(Intent(this, LoginActivity::class.java))
            }


        }
    }



}