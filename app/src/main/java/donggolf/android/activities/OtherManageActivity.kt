package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_other_manage.*
import kotlinx.android.synthetic.main.dlg_account_active.view.*
import kotlinx.android.synthetic.main.dlg_login_set.view.*
import org.json.JSONObject

class OtherManageActivity : RootActivity() {

    private lateinit var context: Context

    var isActive = "a"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_manage)

        context = this

        isActive = PrefUtils.getStringPreference(context,"isActiveAccount")

        setpasswordLL.setOnClickListener {
            var intent = Intent(context, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        setphoneLL.setOnClickListener {
            var intent = Intent(context, ProfilePhoneChangeActivity::class.java)
            startActivity(intent)
        }

        viewdocumentLL.setOnClickListener {
            var intent = Intent(context, ViewDocumentActivity::class.java)
            startActivity(intent)
        }

        deletememberLL.setOnClickListener {
            var intent = Intent(context, WithdrawalActivity::class.java)
            startActivity(intent)
        }

        mutualLL.setOnClickListener {
            var intent = Intent(context, InquireActivity::class.java)
            startActivity(intent)
        }

        accountStatusLL.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_account_active, null)
            builder.setView(dialogView)
            val alert = builder.show()

            if (isActive == "a"){
                dialogView.rdo_activeIV.setImageResource(R.drawable.btn_radio_on)
                dialogView.rdo_inactiveIV.setImageResource(R.drawable.btn_radio_off)
            } else if (isActive == "i") {
                dialogView.rdo_activeIV.setImageResource(R.drawable.btn_radio_off)
                dialogView.rdo_inactiveIV.setImageResource(R.drawable.btn_radio_on)
            }

            var status = ""

            val params = RequestParams()
            params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))
            params.put("type","status")
            params.put("update", status)

            dialogView.dlg_activeLL.setOnClickListener {
                status = "a"
                dialogView.rdo_activeIV.setImageResource(R.drawable.btn_radio_on)
                dialogView.rdo_inactiveIV.setImageResource(R.drawable.btn_radio_off)

                MemberAction.update_info(params, object : JsonHttpResponseHandler(){
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        val result = response!!.getString("result")
                        if (result == "ok"){
                            alert.dismiss()
                        }
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                        println(responseString)
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                        println(errorResponse)
                    }
                })
            }

            dialogView.dlg_inactiveLL.setOnClickListener {
                status = "i"
                dialogView.rdo_activeIV.setImageResource(R.drawable.btn_radio_off)
                dialogView.rdo_inactiveIV.setImageResource(R.drawable.btn_radio_on)

                MemberAction.update_info(params, object : JsonHttpResponseHandler(){
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                        val result = response!!.getString("result")
                        if (result == "ok"){
                            alert.dismiss()
                        }
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                        println(responseString)
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                        println(errorResponse)
                    }
                })
            }




        }

        loginStatusLL.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dlg_login_set, null)
            builder.setView(dialogView)
            val alert = builder.show()
           var auto =   PrefUtils.getBooleanPreference(context,"auto")

            if (auto==false){
                dialogView.rdo_autoIV.setImageResource(R.drawable.btn_radio_off)
                dialogView.rdo_manualIV.setImageResource(R.drawable.btn_radio_on)
            }else{
                dialogView.rdo_autoIV.setImageResource(R.drawable.btn_radio_on)
                dialogView.rdo_manualIV.setImageResource(R.drawable.btn_radio_off)
            }
            dialogView.dlg_autoLL.setOnClickListener {
                PrefUtils.setPreference(context,"auto", true)
                alert.dismiss()
            }
            dialogView.dlg_manualLL.setOnClickListener {
                PrefUtils.setPreference(context,"auto", false)
                var intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                alert.dismiss()
            }

        }

        finishLL.setOnClickListener {
            finish()
        }


    }

    fun updateInformation(params: RequestParams){

    }

}

