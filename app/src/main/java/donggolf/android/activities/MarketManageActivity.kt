package donggolf.android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_market_manage.*

class MarketManageActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_manage)

        finishaBT.setOnClickListener {
            finish()
        }
    }
}
