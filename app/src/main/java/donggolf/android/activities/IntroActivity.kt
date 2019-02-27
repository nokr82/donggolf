package donggolf.android.activities

import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.actions.NationalAction
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.Arrays.asList
import kotlin.collections.HashMap
import java.util.Map.Entry;


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
                market_id = buldle.getString("market_id")
                chatting_member_id = buldle.getString("chatting_member_id")
                content_id = buldle.getString("content_id")
                friend_id = buldle.getString("friend_id")
                room_id = buldle.getString("room_id")
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
                    stopIntro()
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


}