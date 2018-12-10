package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_dongchat_profile.*

class DongchatProfileActivity : RootActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dongchat_profile)

        context = this

        joinDongChatRL.setOnClickListener {
            val itt = Intent(context, DongChatDetailActivity::class.java)
            startActivity(itt)
        }
    }
}
