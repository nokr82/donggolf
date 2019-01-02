package donggolf.android.fcm

import android.os.Handler
import android.os.Looper
import android.util.Log

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import cz.msebera.android.httpclient.Header
import donggolf.android.actions.MemberAction
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils

/**
 * Created by dev1 on 2017-12-15.
 */

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {

    override fun onCreate() {
        super.onCreate()
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    override fun onTokenRefresh() {
        // Get updated InstanceID token.

        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "Refreshed token: " + refreshedToken!!)

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        val member_id = PrefUtils.getIntPreference(this, "member_id", -1)
        sendRegistrationToServer(refreshedToken, member_id)

        PrefUtils.setPreference(this, "token", refreshedToken)

    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?, member_id: Int) {
        // TODO: Implement this method to send token to your app server.

        val mainHandler = Handler(Looper.getMainLooper())
        val myRunnable = Runnable {
            //Code that uses AsyncHttpClient in your case ConsultaCaract()
            if (member_id == -1 || null == token || "" == token || token.length < 1) {
                return@Runnable
            }
            updateToken(token, member_id)
        }
        mainHandler.post(myRunnable)

    }

    private fun updateToken(token: String?, member_id: Int) {
        val params = RequestParams()
        params.put("member_id", member_id)
        params.put("token", token)
        params.put("device", Config.device)

        MemberAction.regist_token(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONArray?) {
                super.onSuccess(statusCode, headers, response)
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseString: String?) {}

            private fun error() {

            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header>?,
                responseString: String?,
                throwable: Throwable
            ) {

                val member_id = PrefUtils.getIntPreference(this@MyFirebaseInstanceIDService, "member_id")
//                LoginAction.log(javaClass.toString(), member_id, responseString)

                throwable.printStackTrace()
                error()
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header>?,
                throwable: Throwable,
                errorResponse: JSONObject?
            ) {
                throwable.printStackTrace()
                error()
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header>?,
                throwable: Throwable,
                errorResponse: JSONArray?
            ) {
                throwable.printStackTrace()
                error()
            }

            override fun onStart() {}

            override fun onFinish() {}
        })
    }

    companion object {
        private val TAG = "MyFirebaseIIDService"
    }

}
