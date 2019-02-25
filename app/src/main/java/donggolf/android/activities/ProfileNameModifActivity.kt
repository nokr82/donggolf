package donggolf.android.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_profile_name_modif.*
import org.json.JSONException
import org.json.JSONObject

class ProfileNameModifActivity : RootActivity() {

    lateinit var context : Context
    lateinit var nick : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_name_modif)

        //nameET.setText("")

        context = this

        finishNameLL.setOnClickListener {
            finish()
        }

        nameET.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 입력되는 텍스트에 변화가 있을 때 호출된다.
            }

            override fun afterTextChanged(count: Editable) {
                // 입력이 끝났을 때 호출된다.

                nickLettersCnt.setText(Integer.toString(nameET.text.toString().length))
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // 입력하기 전에 호출된다.
            }
        })

        btnNickDel.setOnClickListener {
            nameET.setText("")
        }

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    println("InfoFrag :: $response")

                    if (result == "ok") {
                        val member = response.getJSONObject("Member")

                        var jNick = Utils.getString(member,"nick")
                        nameET.setText(jNick)
                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }
        })

        nick_ok.setOnClickListener {
            //DB에 저장하고 finish
            if (nameET.text.toString().length > 0){
                Toast.makeText(context,"이름 입력은 필수 입력입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            nick = nameET.text.toString()

            //Do table update action
            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
            params.put("update", nick)
            params.put("type", "nick")

            //php구현 아직 안함
            MemberAction.update_info(params, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    try {
                        val result = response!!.getString("result")
                        if (result == "ok") {
                            var intent = Intent()
                            setResult(RESULT_OK,intent)
                            finish()
                        }
                    }catch (e : JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

                }
            })
        }

    }
}
