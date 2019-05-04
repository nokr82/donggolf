package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.nostra13.universalimageloader.core.ImageLoader
import donggolf.android.R
import donggolf.android.base.*
import kotlinx.android.synthetic.main.activity_web_picture_detail.*

class WebPictureDetailActivity : RootActivity() {

    private lateinit var context: Context

    var src = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_picture_detail)

        context = this
        src = intent.getStringExtra("src")
        ImageLoader.getInstance().displayImage(src, webIV, Utils.UILoptionsProfile)

        ifinishLL.setOnClickListener {
            finish()
        }

    }
}
