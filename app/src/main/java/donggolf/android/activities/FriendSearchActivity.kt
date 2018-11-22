package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
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
import android.provider.SyncStateContract.Helpers.update
import android.content.pm.PackageManager
import com.google.android.gms.common.util.ClientLibraryUtils.getPackageInfo
import android.content.pm.PackageInfo
import android.nfc.Tag
import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.models.Users
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.Signature


class FriendSearchActivity : RootActivity() {

    val REQUEST_INVITE = 700
    lateinit var context : Context

    private  var friendData : ArrayList<Users> = ArrayList<Users>()
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

        //main list view setting
        friendAdapter = FriendAdapter(context, R.layout.item_friend_search, friendData)
        frdResultLV.adapter = friendAdapter

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

        btn_txDel.setOnClickListener {
            frdSearchET.setText("")
        }

        frdSearchET.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                //var searchCond : HashMap<String, String> = HashMap<String,String>()
                var keyWord = v.frdSearchET.text.toString()

                //println("Search Words : $keyWord in FriendSearchActivity")
                friendSearchWords(keyWord)
                true
            } else {
                false
            }
        }

        finishBT.setOnClickListener {
            finish()
        }

        invFriend.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_invite_frd, null)

            builder.setView(dialogView)
                    .show()
        }

        btn_search_friends.setOnClickListener {
            //frdSearchET.text.toString()
            var which = Utils.getString(frdSearchET)
            if (which.isEmpty()){
                Utils.alert(context, "검색할 키워드를 입력해주세요")
                return@setOnClickListener
            }

            friendSearchWords(which)

        }
    }

    //카카오톡 공유
    fun shareKakao() {
        var intent = Intent()


    }

    fun getKeyHash(context: Context) : String? {
        val packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES.toString())

        if (packageInfo == null){
            return null

            for (signature in packageInfo?.signatures!!){
                try {
                    getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
                    var md : MessageDigest = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                } catch (e : NoSuchAlgorithmException) {
                    Log.w("KEY_HASH", "Unable to get MessageDigest. signature===========================$signature", e)
                }
            }
        }
        return null
    }

    fun addFriendSearchWords() {

    }

    fun friendSearchWords(keyWord : String) {
        val db = FirebaseFirestore.getInstance()

        friendData.clear()
        db.collection("users")
                .whereEqualTo("nick", keyWord)
                //.whereEqualTo("sharpTag", keyWord)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (document in it.getResult()) {
                            var tmpImgl = document.data.get("imgl") as String
                            var tmpImgs = document.data.get("imgs") as String
                            var tmpLast = document.data.get("last") as Long
                            var tmpNick = document.data.get("nick") as String
                            var tmpSex = document.data.get("sex") as String
                            var tmpSharptag = document.data.get("sharpTag") as ArrayList<String>
                            var tmpSttMsg = document.data.get("state_msg") as String

                            var tmp = Users(tmpImgl, tmpImgs, tmpLast, tmpNick, tmpSex, tmpSharptag, tmpSttMsg)
                            friendData.add(tmp)
                        }
                        friendAdapter.notifyDataSetChanged()
                    }
                }

        //닉네임 조건에서 못 찾으면
        if (friendData.isEmpty()){
            db.collection("users")
                    .whereEqualTo("sharpTag", keyWord)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            for (document in it.getResult()) {
                                var tmpImgl = document.data.get("imgl") as String
                                var tmpImgs = document.data.get("imgs") as String
                                var tmpLast = document.data.get("last") as Long
                                var tmpNick = document.data.get("nick") as String
                                var tmpSex = document.data.get("sex") as String
                                var tmpSharptag = document.data.get("sharpTag") as ArrayList<String>
                                var tmpSttMsg = document.data.get("state_msg") as String

                                var tmp = Users(tmpImgl, tmpImgs, tmpLast, tmpNick, tmpSex, tmpSharptag, tmpSttMsg)
                                friendData.add(tmp)
                            }
                            println("friendData size : ${friendData.size}")
                            friendAdapter.notifyDataSetChanged()
                        }
                    }
        }
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
