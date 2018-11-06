package donggolf.android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_chat_detail.*

class ChatDetailActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        btn_opMenu.setOnClickListener {

            chat_right_menu.visibility = View.VISIBLE
        }

        finishaLL.setOnClickListener {
            finish()
        }


    }
}
