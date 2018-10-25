package donggolf.android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_report.*

class ReportActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        finishaBT.setOnClickListener {
            finish()
        }
    }
}
