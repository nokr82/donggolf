package donggolf.android.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.Config
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_operating.*

class OperatingActivity : RootActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operating)

        context = this

        finishLL.setOnClickListener {
            finish()
            Utils.hideKeyboard(this)
        }


        val url = Config.url + "/agree/agree5"

        operatingWV.settings.javaScriptEnabled = true
        operatingWV.loadUrl(url)
    }
}
