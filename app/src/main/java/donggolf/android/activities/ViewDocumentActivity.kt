package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_view_document.*

class ViewDocumentActivity : RootActivity() {

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_document)

        context = this

        finishLL.setOnClickListener {
            finish()
        }


        operLL.setOnClickListener {
            val intent: Intent = Intent(context, OperatingActivity::class.java)
            startActivity(intent)
        }

        termspecifLL.setOnClickListener {
            var intent: Intent = Intent(context, TermSpecifActivity::class.java)
            startActivity(intent)
        }

        personalLL.setOnClickListener {
            var intent: Intent = Intent(context, PersonalInfoTernsActivity::class.java)
            startActivity(intent)
        }

    }
}
