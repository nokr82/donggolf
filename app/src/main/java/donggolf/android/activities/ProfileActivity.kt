package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.*
import com.loopj.android.http.RequestParams
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : RootActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        context = this

        //상대 홈피
        val user_id = intent.getIntExtra("other_member_id",0)

        //프로필 사진
        otherPrfImgIV.setOnClickListener {
            val intent = Intent(context,ViewProfileListActivity::class.java)
            intent.putExtra("viewAlbumUser", user_id)
            startActivity(intent)
        }

    }

    fun get_user_information(){
        val params = RequestParams()
        //params.put("member_id")//상대방 홈페이지로 넘어갈 때 주는 인텐트값을 줌
    }

}
