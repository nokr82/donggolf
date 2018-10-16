package donggolf.android.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        btn_finish.setOnClickListener {
            finish()
        }
        btn_go_findpictureactivity.setOnClickListener {
            startActivity(Intent(this,FindPictureActivity::class.java))
        }
    }
}
