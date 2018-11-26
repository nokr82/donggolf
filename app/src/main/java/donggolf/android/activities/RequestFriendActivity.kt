package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import donggolf.android.R
import kotlinx.android.synthetic.main.activity_request_friend.*

class RequestFriendActivity : AppCompatActivity() {

    lateinit var context : Context

    var checkAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_friend)

        context = this

        frdReq_check_all.setOnClickListener {
            if (checkAll){
                checkAll = true
                checkIcon.visibility = View.VISIBLE
            } else {
                checkAll = false
                checkIcon.visibility = View.GONE
            }
        }

        acceptTV.setOnClickListener {
            val acceptItt = Intent(context, FriendReqSelectCategoryActivity::class.java)
            startActivity(acceptItt)
        }
    }
}
