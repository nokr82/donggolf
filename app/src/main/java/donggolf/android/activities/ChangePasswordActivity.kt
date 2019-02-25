package donggolf.android.activities

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_change_password.*
import org.json.JSONException
import org.json.JSONObject

class ChangePasswordActivity : RootActivity() {

    lateinit var context: Context

    private var progressDialog: ProgressDialog? = null

    var tempPass = ""

    var tpPw = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        context = this
        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)


        confirmRL.setOnClickListener {
            setPassword()
        }

        finishLL.setOnClickListener {
            finish()
            Utils.hideKeyboard(this)
        }

        clearIV.setOnClickListener {
            tempPassET.setText("")
            Utils.hideKeyboard(this)
        }

        get_member_info()

    }

    fun get_member_info(){
        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        val member = response.getJSONObject("Member")
                        val email = Utils.getString(member,"email")
                        tempPass = Utils.getString(member,"passwd")
                        userEmail.setText(email)
                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }


    fun setPassword(){

        tpPw = tempPassET.text.toString()

        if (tpPw == "" || tpPw == null){
            Utils.alert(context, "현재 비밀번호를 입력해주세요.")
            return
        }

        if (tempPass == tpPw){
            if (Utils.getString(newPass1ET) == Utils.getString(newPass2ET)) {
                val params = RequestParams()
                params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
                params.put("type", "passwd")
                params.put("update", Utils.getString(newPass1ET))

                MemberAction.update_info(params, object : JsonHttpResponseHandler(){
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        try {
                            val result = response!!.getString("result")
                            if (result == "ok") {
                                Toast.makeText(context, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_LONG).show()
//                                                        Utils.alert(context,"비밀번호가 성공적으로 변경되었습니다.")
                                finish()

                                PrefUtils.setPreference(context, "pass", Utils.getString(newPass1ET))

//                                PrefUtils.clear(context)

                                Utils.hideKeyboard(context)
                            }
                        } catch (e:JSONException) {

                        }
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                    }





                })
            } else {
                Utils.alert(context,"재설정할 비밀번호가 일치하지 않습니다. 다시 확인해주세요.")
            }
        } else {
            Utils.alert(context, "현재 비밀번호가 일치하지 않습니다. 다시 확인해주세요.")
        }
    }
}
