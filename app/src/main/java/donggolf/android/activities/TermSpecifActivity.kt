package donggolf.android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import donggolf.android.R

class TermSpecifActivity : AppCompatActivity() {

    lateinit var btnBack:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term_specif)

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }
}
