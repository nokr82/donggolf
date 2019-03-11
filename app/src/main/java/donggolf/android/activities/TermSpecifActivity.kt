package donggolf.android.activities

import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.Config
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_term_specif.*

class TermSpecifActivity : RootActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term_specif)

        var type = 2


        val url = Config.url + "/agree/agree2"

        specWV.settings.javaScriptEnabled = true
        specWV.loadUrl(url)

        finishLL.setOnClickListener  {
            finish()
        }

    }
}
