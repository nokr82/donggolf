package donggolf.android.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
import com.kakao.kakaostory.StringSet.writer
import com.kakao.kakaotalk.callback.TalkResponseCallback
import com.kakao.kakaotalk.response.KakaoTalkProfile
import com.kakao.kakaotalk.v2.KakaoTalkService
import com.kakao.message.template.*
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeResponseCallback
import com.kakao.usermgmt.response.model.UserProfile
import com.kakao.util.exception.KakaoException
import com.kakao.util.helper.log.Logger
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.squareup.okhttp.internal.Util
import cz.msebera.android.httpclient.Header
import donggolf.android.R.id.frdSearchET
import donggolf.android.R.id.main_listview_search
import donggolf.android.actions.MateAction
import donggolf.android.actions.MemberAction
import donggolf.android.actions.PostAction
import donggolf.android.adapters.FriendSearchAdapter
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.activity_main_detail.*
import kotlinx.android.synthetic.main.dlg_invite_frd.view.*
import kotlinx.android.synthetic.main.dlg_post_menu.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class FriendSearchActivity : RootActivity() {

    val REQUEST_INVITE = 700
    lateinit var context: Context

    private var friendData: ArrayList<JSONObject> = ArrayList<JSONObject>()
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var editadapter: FriendSearchAdapter
    private var editadapterData: ArrayList<JSONObject> = ArrayList()

    lateinit var user: HashMap<String, Any>

    var membercnt = ""
    var sidotype = ""
    var goguntype = ""
    //초대
    private var callback: SessionCallback? = null


    var type = ""

    var member_id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_search)

        context = this

        var intent: Intent = intent

        callback = SessionCallback()


        intent = getIntent()
        membercnt = intent.getStringExtra("membercnt")
        sidotype = PrefUtils.getStringPreference(context, "sidotype")
        goguntype = PrefUtils.getStringPreference(context, "goguntype")
        member_cntTV.text = "골퍼 " + membercnt + "명"
        areaTV.text = sidotype + "/" + goguntype


        //main list view setting
        friendAdapter = FriendAdapter(context, R.layout.item_friend_search, friendData)
        frdResultLV.adapter = friendAdapter

        frdResultLV.setOnItemClickListener{ parent, view, position, id ->

                var json = friendAdapter.getItem(position)
                var member = json.getJSONObject("Member")
                member_id = Utils.getString(member, "id")
                visibleMenu()
                Log.d("멤버디디",member_id)

        }
        editadapter = FriendSearchAdapter(context, R.layout.main_edit_listview_item, editadapterData)
        frd_editLV.adapter = editadapter


        btn_txDel.setOnClickListener {
            frdSearchET.setText("")
        }

        frdSearchET.setOnClickListener {

            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

            MateAction.view_mate_search_history(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    try {
                        println(response)
                        val result = response!!.getString("result")
                        if (result == "ok") {
                            val mateSearchHistories = response.getJSONArray("mateSearchHistories")
                            editadapterData.clear()
                            for (i in 0 until editadapterData.size) {
                                val mateSearch = mateSearchHistories[i] as JSONObject

                                editadapterData.add(mateSearch)
                            }

                            editadapter.notifyDataSetChanged()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            })

            main_listview_search.visibility = View.VISIBLE
            main_edit_close.setOnClickListener {
                main_listview_search.visibility = View.GONE
            }


        }

        //엔터키
        frdSearchET.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //var searchCond : HashMap<String, String> = HashMap<String,String>()
                var keyWord = v.frdSearchET.text.toString()

                //println("Search Words : $keyWord in FriendSearchActivity")
                if (keyWord.startsWith("#")) {
                    keyWord = keyWord.replace("#","")
                    type = "1"
                    friendSearchhash(keyWord)

                } else {
                    type = "2"
                    friendSearchWords(keyWord)
                }
//                friendSearchWords(keyWord)
                true
            } else {
                false
            }
        }

        btn_search_friends.setOnClickListener {

            var which = Utils.getString(frdSearchET)
            if (which.isEmpty()) {
                Utils.alert(context, "검색할 키워드를 입력해주세요")
                return@setOnClickListener
            }
            if (which.startsWith("#")) {
                which = which.replace("#","")
                friendSearchhash(which)

            } else {
                friendSearchWords(which)
            }

        }

        finishBT.setOnClickListener {
            finish()
        }

        invFriend.setOnClickListener {

            Utils.hideKeyboard(it.context)

            try {
                val info = context.packageManager.getPackageInfo("donggolf.android", PackageManager.GET_SIGNATURES)
                for (signature in info.signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
                }

            } catch (e: PackageManager.NameNotFoundException) {

            } catch (e: NoSuchAlgorithmException) {

            }


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
                } catch (ke: Exception) {
                    ke.printStackTrace()
                }

                alert.dismiss()
            }
        }

    }


   fun shareKakao() {
       getKeyHash(context)
       val url = "market://details?id=donggolf.android"
       val imgBuilder = ContentObject.newBuilder("동네골프",
               Config.url + "/data/member/5c1cbbaf-e0b0-4ff7-85fe-67b1ac1f19c8",
               LinkObject.newBuilder().setWebUrl(url).setMobileWebUrl(url).build())
               .setDescrption("동네골프")
               .build()


       val builder = FeedTemplate.newBuilder(imgBuilder)
       builder.addButton(ButtonObject("동네골프 멤버 되기", LinkObject.newBuilder()
               .setWebUrl(url)
               .setMobileWebUrl(url)
               .build()))

       val params = builder.build()

       KakaoLinkService.getInstance().sendDefault(this@FriendSearchActivity, params, object : ResponseCallback<KakaoLinkResponse>() {
           override fun onFailure(errorResult: ErrorResult) {
               Logger.e(errorResult.toString())
           }

           override fun onSuccess(result: KakaoLinkResponse) {

           }
       })

       /*
            final KakaoLink kakaoLink = KakaoLink.getKakaoLink(this);

                KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
                kakaoTalkLinkMessageBuilder.addText("우리 동네 할인 식품 알림 서비스\n\n회원가입 없이 위치 기반으로 우리 동네 할인 식품을 만나보세요!");
//                    kakaoTalkLinkMessageBuilder.addAppButton("식세끼 바로가기");
                kakaoTalkLinkMessageBuilder.addWebButton("식세끼 바로가기", "http://eat-master.co.kr/market/open?id=-1");
//                    kakaoTalkLinkMessageBuilder.addWebLink("http://eat-master.co.kr/market/open?id=-1");
                kakaoTalkLinkMessageBuilder.addImage("http://13.124.13.37/data/ad/recommend_img", 934, 501);
                kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder, context);
                */

   }


    fun visibleMenu(){

        val builder = AlertDialog.Builder(context)
        builder.setMessage("친구신청하시겠습니까 ?").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                    if (member_id != null) {


                        var params = RequestParams()
                        params.put("mate_id", member_id)
                        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
                        params.put("category_id",0)
                        params.put("status","w")
                        PostAction.add_friend(params, object : JsonHttpResponseHandler() {
                            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                                val result = response!!.getString("result")
                                if (result == "yes") {
                                    Toast.makeText(context, "이미 친구신청을 하셨습니다.", Toast.LENGTH_SHORT).show()
                                }else {
                                    Toast.makeText(context, "친구신청을 보냈습니다", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                            }
                        })

                    }

                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()


    }

   fun getKeyHash(context: Context): String? {
        val packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES.toString())

        if (packageInfo == null) {
            return null

            for (signature in packageInfo?.signatures!!) {
                try {
                    getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
                    var md: MessageDigest = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                } catch (e: NoSuchAlgorithmException) {
                    Log.w("KEY_HASH", "Unable to get MessageDigest. signature===========================$signature", e)
                }
            }
        }
        return null
    }

    //최근검색목록키워드 저장
    fun addFriendSearchWords(keywd: String) {
        //최근 검색한 친구 키워드를 데이터리스트에 추가하고 DB에 save
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        params.put("keyword", keywd)

        MateAction.search_mate(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    //println("MateAction Result ::: $response")
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val mateSearchHistories = response.getJSONArray("mateSearchHistories")
                        editadapterData.clear()
                        for (i in 0..editadapterData.size - 1) {
                            val mateSearch = mateSearchHistories.getJSONObject(i)

                            editadapterData.add(mateSearch)
                        }

                        editadapter.notifyDataSetChanged()
                    }
                    main_listview_search.visibility = View.GONE
                    frdSearchET.setText("")
                } catch (e: JSONException) {
                    Log.e("JsonError", "Add mate search word history Action")
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONArray?) {
                println(errorResponse)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                println("친구 최근검색하다 오류난 부분 :: $responseString")
            }
        })
    }

    //키워드로 찾음
    fun friendSearchWords(keyWord: String) {
        val params = RequestParams()
        params.put("keyword", keyWord)
        params.put("goguntype", goguntype)

        MemberAction.search_member(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println("친구검색 ::: $response")
                    val result = response!!.getString("result")
                    friendData.clear()
                    if (result == "ok") {
                        val members = response.getJSONArray("members")
                        if (members.length() == 0) {
                            Toast.makeText(context, "친구를 찾을수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                        for (i in 0 until members.length()) {
                            val member = members[i] as JSONObject
                            val member_o = member.getJSONObject("Member")
                            val member_id = Utils.getInt(member_o,"id")
                            Log.d("멤버다",member.toString())
                            if (member_id!=PrefUtils.getIntPreference(context, "member_id")){
                                friendData.add(member)
                            }

                        }
                    }
                    friendAdapter.notifyDataSetChanged()

                    addFriendSearchWords(keyWord)

                } catch (e: JSONException) {
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


    fun friendSearchhash(keyWord: String) {
        val params = RequestParams()
        params.put("keyword", keyWord)
        params.put("goguntype", goguntype)

        MemberAction.search_member(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    println("친구검색 ::: $response")
                    val result = response!!.getString("result")
                    friendData.clear()
                    if (result == "ok") {
                        val members = response.getJSONArray("members_tag")
                        if (members.length() == 0) {
                            Toast.makeText(context, "친구를 찾을수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                        for (i in 0 until members.length()) {
                            val member = members[i] as JSONObject
                            val member_o = member.getJSONObject("Member")
                            val member_id = Utils.getInt(member_o,"id")
                            Log.d("멤버다",member.toString())
                            if (member_id!=PrefUtils.getIntPreference(context, "member_id")){
                                friendData.add(member)
                            }
                        }
                    }
                    friendAdapter.notifyDataSetChanged()

                    addFriendSearchWords(keyWord)

                } catch (e: JSONException) {
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
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_INVITE) {
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
                    smsMng.sendTextMessage(phoneNum, "010-1234-8765", "보낼 내용", null, null)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                // Failed to send invitations
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Utils.hideKeyboard(context)
    }
}
