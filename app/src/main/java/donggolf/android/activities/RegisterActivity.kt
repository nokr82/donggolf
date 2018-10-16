package donggolf.android.activities

import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_finish.setOnClickListener {
            finish()
        }
        btn_success_register.setOnClickListener {
            finish()
        }


    }
}
