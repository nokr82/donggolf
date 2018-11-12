package donggolf.android.activities

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.appinvite.AppInviteInvitation
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_friend_search.*

class FriendSearchActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_search)

        val intent:Intent = intent

        var user:HashMap<String,Any> = intent.getSerializableExtra("tUser") as HashMap<String, Any>

        finishBT.setOnClickListener {
            finish()
        }

        invFriend.setOnClickListener {
            //
            var intent = AppInviteInvitation.IntentBuilder((user.values+"님이 초대를 보내셨습니다").toString())
                    .setMessage("인싸스포츠 골프 허쉴? 아래 링크를 눌러 골프어플로 함께하세요!")
                    .setDeepLink(Uri.EMPTY)
                    /*.setCustomImage(p0)
                    .setCallToActionText()*/
                    .build()
            startActivityForResult(intent, 0)
            //
        }
    }
}
