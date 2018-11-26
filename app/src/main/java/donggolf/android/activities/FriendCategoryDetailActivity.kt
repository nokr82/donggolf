package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_friend_category_detail.*

class FriendCategoryDetailActivity : RootActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_category_detail)

        context = this

        val getItt = intent.getStringExtra("groupTitle")
        titleFrdCateTV.setText(getItt)

        frdReq_check_all.setOnClickListener {
            checkIcon.visibility = View.VISIBLE
        }

        categoryManagementIV.setOnClickListener {
            val itt = Intent(context, FriendGrpDetailSettingActivity::class.java)
            itt.putExtra("groupTitle", getItt)
            startActivity(itt)
        }
    }
}
