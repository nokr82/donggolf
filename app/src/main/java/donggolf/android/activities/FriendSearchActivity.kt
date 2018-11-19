package donggolf.android.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Config
import com.google.android.gms.appinvite.AppInviteInvitation
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_friend_search.*
import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.appinvite.FirebaseAppInvite
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import donggolf.android.base.Config.url
import java.util.logging.Logger


class FriendSearchActivity : RootActivity() {

    val REQUEST_INVITE = 700

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_search)

        val intent:Intent = intent

        var user:HashMap<String,Any> = intent.getSerializableExtra("tUser") as HashMap<String, Any>

        /*FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
                .addOnSuccessListener(this, OnSuccessListener { data ->
                    if (data == null) {
                        return@OnSuccessListener
                    }

                    val deepLink = data.link

                    val invite = FirebaseAppInvite.getInvitation(data)
                    val invitationId = invite.invitationId

                    deepLink?.let {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setPackage(packageName)
                        intent.data = it

                        startActivity(intent)
                    }
                })
                .addOnFailureListener(this) {

                }*/

        finishBT.setOnClickListener {
            finish()
        }

        invFriend.setOnClickListener {

            /*var intent = AppInviteInvitation.IntentBuilder(user.values.toString() + "님이 초대를 보내셨습니다")
                    .setMessage("인싸스포츠 골프 하쉴? 아래 링크를 눌러 골프어플로 함께하세요!")
                    .setDeepLink(Uri.EMPTY)
//                    .setCustomImage(p0)
                    .setCallToActionText("Find Data")
                    .build()

            startActivityForResult(intent, REQUEST_INVITE)*/


        }
    }

    //카카오톡 공유
    fun shareKakao() {
        var intent = Intent()


    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_INVITE){
            if (resultCode === Activity.RESULT_OK) {
                val ids = AppInviteInvitation.getInvitationIds(
                        resultCode, data!!)
                for (id in ids) {
                    Log.d("CSH", "id of sent invitation: $id")
                }
            } else {
                // Failed to send invitations
            }
        }
    }
}
