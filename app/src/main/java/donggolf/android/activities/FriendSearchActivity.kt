package donggolf.android.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.util.Config
import com.google.android.gms.appinvite.AppInviteInvitation
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_friend_search.*
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import com.google.android.gms.appinvite.AppInvite
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.appinvite.FirebaseAppInvite
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import donggolf.android.actions.ProfileAction
import donggolf.android.adapters.FriendAdapter
import donggolf.android.adapters.MainAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.base.Config.url
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_friend_search.view.*
import java.util.logging.Logger


class FriendSearchActivity : RootActivity() {

    val REQUEST_INVITE = 700
    lateinit var context : Context

    private  var friendData : ArrayList<Map<String, Any>> = ArrayList<Map<String, Any>>()
    private  lateinit var  friendAdapter : FriendAdapter
    private  lateinit var  editadapter : MainEditAdapter
    private  var editadapterData : ArrayList<Map<String, Any>> = ArrayList<Map<String, Any>>()

    //var getNick : String = ""
    //var getStMsg : String = ""
    //var getImg : String = ""

    //초대


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_search)

        context = this

        val intent:Intent = intent

        var user:HashMap<String,Any> = intent.getSerializableExtra("tUser") as HashMap<String, Any>

        friendAdapter = FriendAdapter(context, R.layout.item_friend_search, friendData)

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

        frdSearchET.setOnClickListener {

            main_listview_search.visibility = View.VISIBLE
        }

        frdSearchET.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                var searchCond : HashMap<String, String> = HashMap<String,String>()
                var keyWord = v.frdSearchET.text.toString()
                searchCond.put("sharpTag", keyWord)
                ProfileAction.searchFriendsWithTag(searchCond){ success, data, exception ->
                    if (success) {
                        data!!.forEach {
                            if (it != null) {
                                friendData.add(it)
                            }
                            println("getId in FriendSearch -----------> ${data.get(actionId)}")

                        }
                        friendAdapter.notifyDataSetChanged()
                    }
                }
                true
            } else {
                false
            }
        }

        finishBT.setOnClickListener {
            finish()
        }

        invFriend.setOnClickListener {

            /*var intent = AppInviteInvitation.IntentBuilder(user.values.toString() + "님이 초대를 보내셨습니다")
                    .setMessage("인싸스포츠 골프 하쉴? 아래 링크를 눌러 골프어플로 함께하세요!")
                    .setDeepLink(Uri.EMPTY)
                    .setCustomImage(p0)
                    .setCallToActionText("Find Data")
                    .build()

            startActivityForResult(intent, REQUEST_INVITE)*/

        }

        btn_search_friends.setOnClickListener {
            //frdSearchET.text.toString()
            var which = Utils.getString(frdSearchET)
            if (which.isEmpty()){
                Utils.alert(context, "검색할 키워드를 입력해주세요")
                return@setOnClickListener
            }

            var condition : HashMap<String, String> = HashMap<String, String>()
            condition.put("sharpTag", which)

            ProfileAction.searchFriendsWithTag(condition){ success, data, exception ->
                if (success){

                }
            }


        }
    }

    //카카오톡 공유
    fun shareKakao() {
        var intent = Intent()


    }

    fun addFriendSearchWords() {

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
