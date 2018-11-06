package donggolf.android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
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
