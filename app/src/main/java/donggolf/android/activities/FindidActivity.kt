package donggolf.android.activities

import android.database.Cursor
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_findid.*

class FindidActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findid)

        btn_finish.setOnClickListener {
            finish()
        }
    }


}
