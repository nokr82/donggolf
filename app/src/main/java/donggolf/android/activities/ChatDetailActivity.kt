package donggolf.android.activities

import android.os.Bundle
import android.view.View
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_chat_detail.*

class ChatDetailActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        btn_opMenu.setOnClickListener {

            drawerMenu.openDrawer(chat_right_menu)

        }

        chatCont.setOnItemClickListener { parent, view, position, id ->
            drawerMenu.closeDrawer(chat_right_menu)
        }






        finishaLL.setOnClickListener {
            finish()
        }


    }
}
