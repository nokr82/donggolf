package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_notice2.*

class Notice2Activity : RootActivity() {

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice2)

        context = this

        settingLV.setOnClickListener {
            var intent: Intent = Intent(context, SetNoticeActivity::class.java)
            startActivity(intent)
        }

        finishLL.setOnClickListener {
            finish()
        }



    }
}
