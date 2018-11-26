package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_friend_search.*
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import donggolf.android.adapters.FriendAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_friend_search.view.*
import android.content.pm.PackageManager
import com.google.android.gms.common.util.ClientLibraryUtils.getPackageInfo
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.LinkObject
import com.kakao.message.template.TextTemplate
import com.kakao.network.callback.ResponseCallback
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.dlg_invite_frd.view.*
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class FriendSearchActivity : RootActivity() {

    val REQUEST_INVITE = 700
    lateinit var context : Context

    private  var friendData : ArrayList<Users> = ArrayList<Users>()
    private  lateinit var  friendAdapter : FriendAdapter
    private  lateinit var  editadapter : MainEditAdapter
    private  var editadapterData : ArrayList<Map<String, Any>> = ArrayList<Map<String, Any>>()

    lateinit var user : HashMap<String,Any>

    //var getNick : String = ""
    //var getStMsg : String = ""
    //var getImg : String = ""

    //초대


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_search)

        context = this

        val intent:Intent = intent

        user = intent.getSerializableExtra("tUser") as HashMap<String, Any>

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

            Utils.hideKeyboard(it.context)

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_invite_frd, null) //사용자 정의 다이얼로그 xml 붙이기
            builder.setView(dialogView) // custom xml과 alertDialogBuilder를 붙임
            val alert = builder.show() //builder를 끄기 위해서는 alertDialog에 이식해줘야 함

            dialogView.inviteSMS.setOnClickListener {
                //println("SMS 이미지 클릭됨")

                val smsit = Intent(Intent.ACTION_PICK)
                smsit.data = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                startActivityForResult(smsit, REQUEST_INVITE)

                alert.dismiss()
            }

            dialogView.inviteKaKaO.setOnClickListener {

                try {
                    shareKakao()
                } catch (ke : Exception) {
                    ke.printStackTrace()
                }

                alert.dismiss()
            }
        }

        btn_search_friends.setOnClickListener {

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
        //Get Image Url

        val params = TextTemplate.newBuilder("동네골프",
                LinkObject.newBuilder().setWebUrl("market://details?id=donggolf.android").setMobileWebUrl("market://details?id=donggolf.android").build()) //본체
                .setButtonTitle("동네골프 멤버 되기") //버튼 String
                .build()

        val serverCallbackArgs = HashMap<String, String>()
        serverCallbackArgs.put("user_id", "$user")
        //serverCallbackArgs.put("product_id", "$shared_product_id")

        /*KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, ResponseCallback<KakaoLinkResponse> {

        })*/
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
        //최근 검색한 친구 키워드를 데이터리스트에 추가하고 DB에 save
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

                try {
                    val uri = data?.data
                    //여러 연락처 받아오기
                    val cursor = contentResolver.query(uri, null, null, null, null)

                    cursor.moveToFirst()
                    var phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    var phoneNum = cursor.getString(phoneIdx)

                    //println("You got the phone number ::::::::::::::: $phoneNum")
                    val smsMng = SmsManager.getDefault()
                    smsMng.sendTextMessage(phoneNum, "나", "보낼 내용", null, null)

                } catch (e : Exception) {
                    e.printStackTrace()
                }
            } else {
                // Failed to send invitations
            }
        }
    }

}
