package donggolf.android.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.StringUtils
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_profile_phone_change.*
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class ProfilePhoneChangeActivity : RootActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_phone_change)

        context = this

        finishLL.setOnClickListener {
            finish()
            Utils.hideKeyboard(this)
        }

        clearIV.setOnClickListener {
            phoneNumET.setText("")
            Utils.hideKeyboard(this)
        }

        get_member_info()

        changePhoneOK.setOnClickListener {
            setPhoneNumber()
        }

    }

    fun setPhoneNumber(){
        val phonenum = phoneNumET.text.toString()
        if (phonenum == "" || phonenum == null){
            Toast.makeText(context, "빈칸은 입력하실 수 없습니다.", Toast.LENGTH_LONG).show()
            return
        }

        var regex = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$"

        if(!Pattern.matches(regex,phonenum)){
            Toast.makeText(this,"올바른 핸드폰 번호가 아닙니다.",Toast.LENGTH_SHORT).show();
            return;
        } else if (phonenum.length != 11){
            Toast.makeText(this,"올바른 핸드폰 번호가 아닙니다.",Toast.LENGTH_SHORT).show();
            return;
        }

        val params = RequestParams()
        params.put("member_id",PrefUtils.getIntPreference(context,"member_id"))
        params.put("type", "phone")
        params.put("update", phonenum)

        MemberAction.update_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        Toast.makeText(context, "정보가 성공적으로 변경되었습니다.", Toast.LENGTH_LONG).show()
                        finish()
                        Utils.hideKeyboard(context)
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

    fun get_member_info(){
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
    }
}
