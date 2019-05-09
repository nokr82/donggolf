package donggolf.android.activities

import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.google.firebase.auth.FirebaseAuth
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.actions.VersionAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class IntroActivity : RootActivity() {

    private lateinit var context: Context
    private var mAuth: FirebaseAuth? = null
    private var progressDialog: ProgressDialog? = null

    private var market_id:String = "-1"
    private var chatting_member_id:String = "-1"
    private var content_id:String = "-1"
    private var friend_id:String = "-1"
    private var room_id:String = "-1"
    private var is_push:Boolean = false

    protected var _splashTime = 2000 // time to display the splash screen in ms
    private val _active = true
    private var splashThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

//        mAuth = FirebaseAuth.getInstance();

//        val user = mAuth!!.getCurrentUser()

        this.context = this
        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

        // clear all notification
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancelAll()

        val buldle = intent.extras
        if (buldle != null) {
            try {
                if(buldle.getString("market_id") != null) {
                    market_id = buldle.getString("market_id")
                }

                if(buldle.getString("chatting_member_id") != null) {
                    chatting_member_id = buldle.getString("chatting_member_id")
                }

                if(buldle.getString("content_id") != null) {
                    content_id = buldle.getString("content_id")
                }

                if(buldle.getString("friend_id") != null) {
                    friend_id = buldle.getString("friend_id")
                }

                if(buldle.getString("room_id") != null) {
                    room_id = buldle.getString("room_id")
                }

                is_push = buldle.getBoolean("FROM_PUSH")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        splashThread = object : Thread() {
            override fun run() {
                try {
                    var waited = 0
                    while (waited < _splashTime && _active) {
                        Thread.sleep(100)
                        waited += 100
                    }
                } catch (e: InterruptedException) {
                    // do nothing
                } finally {
                    // stopIntro()
                    runOnUiThread {
                        version()
                    }

                }
            }
        }
        (splashThread as Thread).start()

//        val autoLogin = PrefUtils.getBooleanPreference(context, "auto")
//        if(autoLogin) {
//            LoginActivity.setLoginData(context, user)
//            startActivity(Intent(this, MainActivity::class.java))
//        } else {
////                FirebaseAuth.getInstance().signOut()
//            startActivity(Intent(this, LoginActivity::class.java))
//        }

    }

    private fun stopIntro() {

        val autoLogin = PrefUtils.getBooleanPreference(context, "auto")

//        val first = PrefUtils.getBooleanPreference(context, "first")

        if (!autoLogin) {
            PrefUtils.clear(context)

            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            intent.setAction((Intent.ACTION_MAIN))
//            intent.addCategory(Intent.CATEGORY_LAUNCHER)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        } else {
            handler.sendEmptyMessage(0)
        }

    }

    internal var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            //versionInfo();
            login()
        }
    }

    private fun login() {

        val params = RequestParams()
        params.put("email", PrefUtils.getStringPreference(context, "email"))
        params.put("passwd", PrefUtils.getStringPreference(context, "pass"))

        MemberAction.member_login(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {

                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {

                        if (progressDialog != null) {
                            progressDialog!!.dismiss()
                        }

                        val member = response.getJSONObject("member")

                        val admin_del_yn = Utils.getString(member,"admin_del_yn")
                        val admin_day_del_yn = Utils.getString(member,"admin_day_del_yn")
                        val admin_day = Utils.getString(member,"admin_day")
                        val del_day = response.getString("del_day")
                        if (admin_del_yn == "Y"){
                            alert("동네골프 접근이 영구차단되었습니다.")
                            return
                        }else if (admin_day_del_yn == "Y"){
                            alert("동네골프 접근이 "+admin_day+"부터 "+del_day+"까지 차단되었습니다.")
                            return
                        }


                        val isActive = Utils.getString(member,"status")

                        if (isActive == "a") {

                            val member_id = Utils.getInt(member, "id")

                            PrefUtils.setPreference(context, "member_id", member_id)
                            PrefUtils.setPreference(context,"nick", Utils.getString(member,"nick"))
                            PrefUtils.setPreference(context, "email", Utils.getString(member, "email"))
                            PrefUtils.setPreference(context, "pass", Utils.getString(member, "passwd"))
                            PrefUtils.setPreference(context, "auto", true)
                            PrefUtils.setPreference(context,"isActiveAccount","a")
                            PrefUtils.setPreference(context,"userPhone", Utils.getString(member,"phone"))
                            PrefUtils.setPreference(context,"region",Utils.getString(member, "region1"))
                            var region = ""
                            if (Utils.getString(member, "region1")!=""){
                                region += Utils.getString(member, "region1")
                            }
                            if (Utils.getString(member, "region2")!=""){
                                region  += ","+Utils.getString(member, "region2")
                            }
                            if (Utils.getString(member, "region3")!=""){
                                region +=  ","+Utils.getString(member, "region3")
                            }
                            PrefUtils.setPreference(context,"region_id",region)

                            var intent = Intent(context, MainActivity::class.java)
                            intent.putExtra("is_push", is_push)
                            intent.putExtra("market_id", market_id.toInt())
                            intent.putExtra("content_id", content_id.toInt())
                            intent.putExtra("friend_id", friend_id.toInt())
                            intent.putExtra("room_id", Utils.getInt(room_id))
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            intent.setAction((Intent.ACTION_MAIN))
//                            intent.addCategory(Intent.CATEGORY_LAUNCHER)
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)

                            finish()
                        } else {
                            var intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            intent.setAction((Intent.ACTION_MAIN))
//                            intent.addCategory(Intent.CATEGORY_LAUNCHER)
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
//                            Toast.makeText(context,"휴면 계정입니다. 문의해주세요.", Toast.LENGTH_SHORT).show()
                        }

                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                //super.onFailure(statusCode, headers, responseString, throwable)
                Utils.alert(context, "로그인에 실패했습니다.")
            }

        })

    }

    override fun onNewIntent(intent: Intent?) {

    }


    private fun version() {
        VersionAction.version(object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                try {
                    val version = response!!.getJSONObject("version")
                    if (!version.isNull("id")) {
                        val serverVersion = version.getString("version")
                        val type = version.getInt("type")
                        if(type == 1) {
                            versionCheck(serverVersion)
                        } else {
                            stopIntro()
                        }
                    } else {
                        stopIntro()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONArray?) {
                super.onSuccess(statusCode, headers, response)
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseString: String?) {}

            private fun error() {}

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                throwable.printStackTrace()
                error()

                // System.out.println(responseString);
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONObject?) {
                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONArray?) {
                throwable.printStackTrace()
                error()
            }

            override fun onStart() {}

            override fun onFinish() {}
        })
    }


    private fun versionCheck(serverVersion:String) {

        var nowVersion = "1.0.0"

        try {
            val i = context.packageManager.getPackageInfo(context.packageName, 0)
            nowVersion = i.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val n = nowVersion.split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val s = serverVersion.split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

        var n1 = 1
        var n2 = 0
        var n3 = 0

        if (n.size > 0) {
            n1 = Integer.parseInt(n[0])
        }

        if (n.size > 1) {
            n2 = Integer.parseInt(n[1])
        }

        if (n.size > 2) {
            n3 = Integer.parseInt(n[2])
        }

        var s1 = 1
        var s2 = 0
        var s3 = 0

        if (s.size > 0) {
            s1 = Integer.parseInt(s[0])
        }

        if (s.size > 1) {
            s2 = Integer.parseInt(s[1])
        }

        if (s.size > 2) {
            s3 = Integer.parseInt(s[2])
        }

        var newVersionExist = false

        if (s1 > n1) {
            newVersionExist = true
        } else if (s1 >= n1 && s2 > n2) {
            newVersionExist = true
        } else if (s1 >= n1 && s2 >= n2 && s3 > n3) {
            newVersionExist = true
        }

        if (newVersionExist) {
            Utils.alert(context, "새로운 버전이 있습니다.\n업데이트 후 사용하세요.") {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("market://details?id=$packageName")
                startActivity(intent)

                finish()
            }
        } else {
            stopIntro()
        }

    }

}