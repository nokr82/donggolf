package donggolf.android.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_profile_phone_change.*
import org.json.JSONException
import org.json.JSONObject

class ProfilePhoneChangeActivity : RootActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_phone_change)

        context = this

        finishLL.setOnClickListener {
            finish()
        }

        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val member = response.getJSONObject("Member")

                        var phoneNum = Utils.getString(member,"phone")
                        phoneNumET.setText(phoneNum)
                    }
                } catch (e:JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                println(errorResponse)
            }
        })

        changePhoneOK.setOnClickListener {
            val params = RequestParams()
            params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
            params.put("type", "phone")
            params.put("update", Utils.getString(phoneNumET))

            MemberAction.update_info(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    try {
                        val result = response!!.getString("result")
                        if (result == "ok") {
                            Utils.alert(context,"정보가 성공적으로 변경되었습니다.")
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                    println(errorResponse)
                }

            })
        }

    }
}
