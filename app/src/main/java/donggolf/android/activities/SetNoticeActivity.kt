package donggolf.android.activities

import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_set_notice.*

class SetNoticeActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_notice)

        finishLV.setOnClickListener {
            finish()
        }


    }
}
