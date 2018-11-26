package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_chat_detail.*

class ChatDetailActivity : RootActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        val author = intent.getStringExtra("Author")
        if (author.equals("개설자")) {
            chatListRemoveLL.visibility = View.GONE

        } else if (author.equals("권한자")) {
            chatListRemoveLL.visibility = View.GONE
        } else {
            chatListRemoveLL.visibility = View.VISIBLE
        }

        btn_opMenu.setOnClickListener {

            drawerMenu.openDrawer(chat_right_menu)

        }

        chatCont.setOnItemClickListener { parent, view, position, id ->
            drawerMenu.closeDrawer(chat_right_menu)
        }

        showMoreTV.setOnClickListener {
            val it = Intent(context, ChatMemberActivity::class.java)
            startActivity(it)
        }




        finishaLL.setOnClickListener {
            finish()
        }


    }
}
