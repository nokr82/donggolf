package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_withdrawal.*
import org.json.JSONException
import org.json.JSONObject

class WithdrawalActivity : RootActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdrawal)

        context = this

        finishLL.setOnClickListener {
            finish()
        }

        btn_withd_ok.setOnClickListener {

            if (withdrawalCheck.isChecked) {
                val builder = AlertDialog.Builder(context)
                builder
                        .setMessage("정말 탈퇴하시겠습니까?")

                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            secseion()
                            dialog.cancel()
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                        })
                val alert = builder.create()
                alert.show()
            }
            else {
                Utils.alert(context, "위 사항을 읽어보시고 동의하기에 체크해주세요.")
                return@setOnClickListener
            }
        }

    }


    fun secseion(){

        val params = RequestParams()
        params.put("type", "leave")
        params.put("member_id", PrefUtils.getIntPreference(context,"member_id"))

        MemberAction.update_info(params, object : JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        PrefUtils.clear(context)
                        finishAffinity()
                        System.runFinalizersOnExit(true)
                        System.exit(0)
                    }
                }catch (e:JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                // println(errorResponse)
            }
        })
    }


}
