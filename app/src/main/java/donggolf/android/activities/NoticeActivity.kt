package donggolf.android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_notice.*

class NoticeActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        finishaBT.setOnClickListener {
            finish()
        }
    }
}
