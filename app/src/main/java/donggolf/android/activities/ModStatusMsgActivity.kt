package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.actions.ProfileAction
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.models.Content
import donggolf.android.models.HashTag
import donggolf.android.models.Users
import kotlinx.android.synthetic.main.activity_mod_status_msg.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile_tag_change.*
import org.json.JSONException
import org.json.JSONObject


class ModStatusMsgActivity : RootActivity() {

    private var mAuth: FirebaseAuth? = null
    lateinit var context :Context

    lateinit var imgl : String
    lateinit var imgs : String
    var lastN : Long = 0
    lateinit var nick : String
    lateinit var sex : String
    lateinit var sTag : ArrayList<String>
    lateinit var statusMessage : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_status_msg)

        context = this

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()
        val db = FirebaseFirestore.getInstance()

        finishaLL.setOnClickListener {
            finish()
        }

        statusMsg.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 입력되는 텍스트에 변화가 있을 때 호출된다.
            }

            override fun afterTextChanged(count: Editable) {
                // 입력이 끝났을 때 호출된다.

                statusLen.setText(Integer.toString(statusMsg.text.toString().length))
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // 입력하기 전에 호출된다.
            }
        })

        statusTxDel.setOnClickListener {
            statusMsg.setText("")
        }

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    println("InfoFrag :: ${response.toString()}")

                    if (result == "ok") {
                        val member = response.getJSONObject("Member")

                        var jStatusMsg = Utils.getString(member,"status_msg")
                        statusMsg.setText(jStatusMsg)
                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {

            }
        })

        status_Ok.setOnClickListener {
            statusMessage = statusMsg.text.toString()
            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
            params.put("update", statusMessage)
            params.put("type", "status_msg")

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
