package donggolf.android.activities

import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity

class ViewAlbumActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_album)
    }
}
