package donggolf.android.activities

import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.Config
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_personal_info_terns.*

class PersonalInfoTernsActivity : RootActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info_terns)

        if (intent.getStringExtra("rule") != null){
            titleTV.setText("장터사용 원칙")
            val url = Config.url + "/agree/agree6"

            println("-----url : $url")

            personalWV.settings.javaScriptEnabled = true
            personalWV.loadUrl(url)
        }else{
            val url = Config.url + "/agree/agree3"

            personalWV.settings.javaScriptEnabled = true
            personalWV.loadUrl(url)
        }


        finishLL.setOnClickListener {
            finish()
        }
    }

    fun dlgView(error:String){

    }
}
