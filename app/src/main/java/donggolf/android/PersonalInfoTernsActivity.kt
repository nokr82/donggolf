package donggolf.android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class PersonalInfoTernsActivity : AppCompatActivity() {

    lateinit var btnBack: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info_terns)

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }
}
