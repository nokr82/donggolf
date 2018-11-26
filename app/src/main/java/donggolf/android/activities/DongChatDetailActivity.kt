package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_chat_detail.*
import kotlinx.android.synthetic.main.activity_dong_chat_detail.*

class DongChatDetailActivity : RootActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dong_chat_detail)

        context = this

        btn_opDongchatMenu.setOnClickListener {
            dongchat_drawerMenu.openDrawer(dongchat_right_menu)
        }

        //권한 먼데
        //var participant = intent.getStringExtra("participant")
        var participant = "참여자"
        if (participant.equals("관리자")){
            chatSettingLL.visibility = View.VISIBLE
            chatReportLL.visibility = View.GONE
        } else if (participant.equals("참여자")) {
            chatSettingLL.visibility = View.GONE
            chatReportLL.visibility = View.VISIBLE
        }

        publicLL.setOnClickListener {
            radio_public.setImageResource(R.drawable.btn_radio_on)
            radio_secret.setImageResource(R.drawable.btn_radio_off)
        }

        secretLL.setOnClickListener {
            radio_public.setImageResource(R.drawable.btn_radio_off)
            radio_secret.setImageResource(R.drawable.btn_radio_on)
        }
    }
}
