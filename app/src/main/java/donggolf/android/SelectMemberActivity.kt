package donggolf.android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.base.RootActivity

class SelectMemberActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_member)
    }
}
