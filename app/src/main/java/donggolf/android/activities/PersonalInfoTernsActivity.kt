package donggolf.android.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_personal_info_terns.*

class PersonalInfoTernsActivity : RootActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info_terns)

        if (intent.getStringExtra("rule") != null){
            titleTV.setText("장터사용 원칙")
        }


        finishLL.setOnClickListener {
            finish()
        }
    }

    fun dlgView(error:String){

    }
}
