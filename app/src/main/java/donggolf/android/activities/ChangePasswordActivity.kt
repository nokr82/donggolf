package donggolf.android.activities

import android.content.Context
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        context = this

        finishLL.setOnClickListener {
            if (Utils.getString(tempPassET).isEmpty()
                    && Utils.getString(newPass1ET).isEmpty()
                    && Utils.getString(newPass2ET).isEmpty()) {
                finish()
            } else {
                var tpPw = tempPassET.text.toString()

                val params = RequestParams()
                params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

                MemberAction.get_member_info(params, object : JsonHttpResponseHandler(){
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        try {
                            val result = response!!.getString("result")
                            if (result == "ok") {
                                val member = response.getJSONObject("Member")

                                var tempPass = Utils.getString(member,"passwd")
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
                                                        Utils.alert(context,"비밀번호가 성공적으로 변경되었습니다.")
                                                        finish()
                                                    }
                                                } catch (e:JSONException) {
                                                    println("JSON ERROR!!")
                                                }
                                            }

                                            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                                                println()
                                                println(errorResponse)
                                                println()
                                            }
                                        })
                                    } else {
                                        Utils.alert(context,"재설정할 비밀번호가 일치하지 않습니다. 다시 확인해주세요.")
                                    }
                                } else {
                                    Utils.alert(context, "현재 비밀번호가 일치하지 않습니다. 다시 확인해주세요.")
                                }
                            }
                        } catch (e : JSONException) {
                            e.printStackTrace()
                        }
                    }
                })
            }
        }



    }
}
