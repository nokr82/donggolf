package donggolf.android.base

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import android.os.Bundle
import android.widget.ListView
import donggolf.android.R


class StatusDialog(context: Context, list: Array<String>) : Dialog(context) {

    lateinit var titleTV : TextView
    lateinit var statusLV : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_status_dialog)

        titleTV = findViewById(R.id.titleTV)
        statusLV = findViewById(R.id.statusLV)


    }

}
/*
class CustomDialog(private val context: Context, private val name: String) : Dialog(context), View.OnClickListener {

    private var dialogListener: MyDialogListener? = null

    private var nameEt: TextInputEditText? = null
    private var emailEt: TextInputEditText? = null

    private var cancelTv: TextView? = null
    private var searchTv: TextView? = null

    fun setDialogListener(dialogListener: MyDialogListener) {
        this.dialogListener = dialogListener
    }

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT)

        nameEt = findViewById(R.id.findPwDialogNameEt) as TextInputEditText
        emailEt = findViewById(R.id.findPwDialogEmailEt) as TextInputEditText

        cancelTv = findViewById(R.id.findPwDialogCancelTv) as TextView
        searchTv = findViewById(R.id.findPwDialogFindTv) as TextView

        cancelTv!!.setOnClickListener(this)
        searchTv!!.setOnClickListener(this)

        if (!TextUtils.isEmpty(name)) {
            nameEt!!.setText(name)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.findPwDialogCancelTv -> cancel()
            R.id.findPwDialogFindTv -> {
                val email = emailEt!!.getText().toString()
                val name = nameEt!!.getText().toString()
                dialogListener!!.onPositiveClicked(email, name)
                dismiss()
            }
        }
    }

    companion object {

        private val LAYOUT = R.layout.dialog_custom
    }

}
*/