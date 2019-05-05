package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_inquire.*
import org.json.JSONObject

class InquireActivity : RootActivity() {

    lateinit var context : Context

    private lateinit var email : String
    private lateinit var phoneNum : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inquire)

        context = this

        phoneNum = PrefUtils.getStringPreference(context,"userPhone")
        if (phoneNum.length > 0) {
            mobNumTV.text = phoneNum.substring(0, 3) + "-" + phoneNum.substring(3, 7) + "-" + phoneNum.substring(7)
        }

        email = PrefUtils.getStringPreference(context, "email")
        ans_user_email.text = email

        finishLL.setOnClickListener {
            finish()
        }

        goToEmailMod.setOnClickListener {
            inquire_emailLL.visibility = View.GONE
            modEmailLL.visibility = View.VISIBLE
        }

        btn_okTV.setOnClickListener {
            if (Utils.isValidEmail(email)) {
                inquire_emailLL.visibility = View.VISIBLE
                modEmailLL.visibility = View.GONE
                email = Utils.getString(rsp_emailET)
                ans_user_email.text = email
            } else {
                Toast.makeText(context,"유효하지 않은 이메일 형식입니다",Toast.LENGTH_SHORT).show()
            }

        }

        btn_inquire.setOnClickListener {

            val params = RequestParams()

            params.put("email", email)
            params.put("phone", phoneNum)
            params.put("content", Utils.getString(inquire_contET))

            MemberAction.inquire(params, object : JsonHttpResponseHandler(){
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    // println(response)
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                    // println(errorResponse)
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                    // println(responseString)
                }
            })

        }

    }
}
