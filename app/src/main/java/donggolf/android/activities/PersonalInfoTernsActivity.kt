package donggolf.android.activities

import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_personal_info_terns.*

class PersonalInfoTernsActivity : RootActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info_terns)


        finishLL.setOnClickListener {
            finish()
        }
    }
}
