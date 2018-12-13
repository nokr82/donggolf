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
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.auth.ErrorCode
import com.kakao.auth.ISessionCallback
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.kakaotalk.callback.TalkResponseCallback
import com.kakao.kakaotalk.response.KakaoTalkProfile
import com.kakao.kakaotalk.v2.KakaoTalkService
import com.kakao.message.template.LinkObject
import com.kakao.message.template.TextTemplate
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeResponseCallback
import com.kakao.usermgmt.response.model.UserProfile
import com.kakao.util.exception.KakaoException
import com.kakao.util.helper.log.Logger
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.actions.MemberAction
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.dlg_invite_frd.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class FriendSearchActivity : RootActivity() {

    val REQUEST_INVITE = 700
    lateinit var context : Context

    private  var friendData : ArrayList<JSONObject> = ArrayList<JSONObject>()
    private  lateinit var  friendAdapter : FriendAdapter
    private  lateinit var  editadapter : MainEditAdapter
    private  var editadapterData : ArrayList<Map<String, Any>> = ArrayList<Map<String, Any>>()

    lateinit var user : HashMap<String,Any>

    //var getNick : String = ""
    //var getStMsg : String = ""
    //var getImg : String = ""

    //초대
    private var callback: SessionCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_search)

        context = this

        val intent:Intent = intent

        callback = SessionCallback()

        //main list view setting
        friendAdapter = FriendAdapter(context, R.layout.item_friend_search, friendData)
        frdResultLV.adapter = friendAdapter


        btn_txDel.setOnClickListener {
            frdSearchET.setText("")
        }

        frdSearchET.setOnClickListener {
            main_listview_search.visibility = View.VISIBLE
        }

        //엔터키
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

        btn_search_friends.setOnClickListener {

            var which = Utils.getString(frdSearchET)
            if (which.isEmpty()){
                Utils.alert(context, "검색할 키워드를 입력해주세요")
                return@setOnClickListener
            }

            friendSearchWords(which)

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

    }

    //카카오톡 공유
    fun shareKakao() {

        val params = TextTemplate.newBuilder("동네골프",
                LinkObject.newBuilder().setWebUrl("market://details?id=donggolf.android").setMobileWebUrl("market://details?id=donggolf.android").build()) //본체
                .setButtonTitle("동네골프 멤버 되기") //버튼 String
                .build()

        val serverCallbackArgs = HashMap<String, String>()
        serverCallbackArgs.put("user_id", "$user")
        //serverCallbackArgs.put("product_id", "$shared_product_id")


        KakaoLinkService.getInstance().sendDefault(this, params, object : ResponseCallback<KakaoLinkResponse>() {
            override fun onFailure(errorResult: ErrorResult) {
                Logger.e(errorResult.toString())
            }

            override fun onSuccess(result: KakaoLinkResponse) {

            }
        })


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
        val params = RequestParams()
        params.put("keyword", keyWord)

        MemberAction.search_member(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println("친구검색 ::: $response")
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val members = response.getJSONArray("members")
                        for (i in 0 until members.length()) {
                            val member = members[i] as JSONObject
                            friendData.add(member)
                        }
                    }

                }catch (e:JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println(responseString)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONArray?) {
                println(errorResponse)
            }
        })
    }



    inner class SessionCallback : ISessionCallback {

        override fun onSessionOpened() {

            // System.out.println("onSessionOpened : ");

            redirectSignupActivity()
        }

        override fun onSessionOpenFailed(exception: KakaoException?) {

            // System.out.println("exception : " + exception);

            if (exception != null) {
                //                Logger.e(exception);
                if ("CANCELED_OPERATION" == exception.errorType.toString()) {
                    Toast.makeText(dialogContext, "카카오톡 로그인 취소", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(dialogContext, exception.errorType.toString(), Toast.LENGTH_LONG).show()
                }
            }

            //setContentView(R.layout.activity_login);
        }
    }

    abstract inner class KakaoTalkResponseCallback<T> : TalkResponseCallback<T>() {

        override fun onNotKakaoTalkUser() {
            // KakaoToast.makeToast(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
        }

        override fun onFailure(errorResult: ErrorResult?) {
            // KakaoToast.makeToast(getApplicationContext(), "failure : " + errorResult, Toast.LENGTH_LONG).show();
            Toast.makeText(dialogContext, errorResult!!.errorMessage, Toast.LENGTH_LONG).show()

            val result = ErrorCode.valueOf(errorResult.errorCode)
            if (result == ErrorCode.CLIENT_ERROR_CODE) {
                finish()
            } else {
                redirectSignupActivity()
            }
        }

        override fun onSessionClosed(errorResult: ErrorResult) {
            Toast.makeText(dialogContext, errorResult.errorMessage, Toast.LENGTH_LONG).show()
            finish()
        }

        override fun onNotSignedUp() {
            Toast.makeText(dialogContext, "카카오톡 회원이 아닙니다\n가입후 이용해 주시기 바랍니다.", Toast.LENGTH_LONG).show()
            finish()
        }

        override fun onDidStart() {
            // showWaitingDialog();
        }

        override fun onDidEnd() {
            // cancelWaitingDialog();
        }
    }

    protected fun redirectSignupActivity() {
        requestMe()
    }

    protected fun requestMe() { //유저의 정보를 받아오는 함수

        UserManagement.getInstance().requestMe(object : MeResponseCallback() {
            override fun onFailure(errorResult: ErrorResult?) {
                val message = "failed to get user info. msg=" + errorResult!!
                Logger.d(message)
                Toast.makeText(dialogContext, errorResult.errorMessage, Toast.LENGTH_LONG).show()

                val result = ErrorCode.valueOf(errorResult.errorCode)
                if (result == ErrorCode.CLIENT_ERROR_CODE) {
                    finish()
                } else {
                    redirectSignupActivity()
                }
            }

            override fun onSessionClosed(errorResult: ErrorResult) {
                Toast.makeText(dialogContext, errorResult.errorMessage, Toast.LENGTH_LONG).show()
                finish()
                //redirectSignupActivity();
            }

            override fun onNotSignedUp() {
                Toast.makeText(dialogContext, "카카오톡 회원이 아닙니다\n가입후 이용해 주시기 바랍니다.", Toast.LENGTH_LONG).show()
                finish()
            } // 카카오톡 회원이 아닐 시 showSignup(); 호출해야함

            override fun onSuccess(userProfile: UserProfile) {  //성공 시 userProfile 형태로 반환
                val kakao_ID = userProfile.id.toString()
                // final String kakao_name = userProfile.getNickname();
                val kakao_email = userProfile.email
                // String kakao_profile_image_path = userProfile.getProfileImagePath();

                // System.out.println("kakao_name : " + kakao_name);
                // System.out.println("kakao_profile_image_path : " + kakao_profile_image_path);
                // System.out.println(userProfile.getProperties());


                KakaoTalkService.getInstance().requestProfile(object : KakaoTalkResponseCallback<KakaoTalkProfile>() {
                    override fun onSuccess(result: KakaoTalkProfile) {
                        // System.out.println("df : " + result);

                        val kakao_name = result.nickName

                        // System.out.println("kakao_name : " + kakao_name);

                        val kakao_profile_image_path = result.profileImageUrl
                        // new DownloadFilesTask().execute(kakao_profile_image_path);

                    }
                })


                // isMember(null, "3", null, null, null, kakao_ID, kakao_name, kakao_profile_image_path);
            }
        })
    }

    //문자로 초대메시지 보내기
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
