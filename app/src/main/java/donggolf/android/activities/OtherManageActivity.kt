package donggolf.android.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import donggolf.android.R
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_other_manage.*

class OtherManageActivity : RootActivity() {

    private lateinit var context: Context

    var acc_status_arr = arrayOf("활성","휴면")
    var account_state : Int = -1
    var login_status_arr = arrayOf("자동로그인","수동로그인")
    var login_state = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_manage)

        context = this

        setpasswordLL.setOnClickListener {
            var intent: Intent = Intent(context, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        setphoneLL.setOnClickListener {
            var intent: Intent = Intent(context, ProfilePhoneChangeActivity::class.java)
            startActivity(intent)
        }

        viewdocumentLL.setOnClickListener {
            var intent: Intent = Intent(context, ViewDocumentActivity::class.java)
            startActivity(intent)
        }

        deletememberLL.setOnClickListener {
            var intent: Intent = Intent(context, WithdrawalActivity::class.java)
            startActivity(intent)
        }

        mutualLL.setOnClickListener {
            var intent: Intent = Intent(context, InquireActivity::class.java)
            startActivity(intent)
        }

        accountStatusLL.setOnClickListener {
            var aBuilder = AlertDialog.Builder(this@OtherManageActivity, R.style.MyDialogTheme)
            aBuilder.setTitle(R.string.account_status_title)
                    .setSingleChoiceItems(acc_status_arr, -1, DialogInterface.OnClickListener { dialog, which ->

                        //do something!!


                        dialog.dismiss()
                    })
            val mDialog : AlertDialog = aBuilder.create()
            mDialog.show()
        }

        loginStatusLL.setOnClickListener {
            var aBuilder = AlertDialog.Builder(this@OtherManageActivity, R.style.MyDialogTheme)
            aBuilder.setTitle(R.string.login_status_title)
                    .setSingleChoiceItems(login_status_arr, -1, DialogInterface.OnClickListener { dialog, which ->

                        if (which == 0){
                            PrefUtils.setPreference(context, "auto", true)
                            println("자동로그인모드")
                        }else if (which == 1){
                            PrefUtils.setPreference(context, "auto", false)
                            println("수동로그인모드")
                        }

                        dialog.dismiss()
                    })
            val mDialog : AlertDialog = aBuilder.create()
            mDialog.show()
        }

        finishLL.setOnClickListener {
            finish()
        }


    }

    fun accountStatusDialog() {

    }

}

