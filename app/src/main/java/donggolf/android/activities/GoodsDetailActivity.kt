package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_goods_detail.*
import java.util.ArrayList

class GoodsDetailActivity : RootActivity() {

    private lateinit var context: Context

    var _Images: ArrayList<String> = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_detail)

        context = this

        finishLL.setOnClickListener {
            finish()
        }

        moresellerpostTV.setOnClickListener {
            MoveSellerActivity()
        }

        reportTV.setOnClickListener {
            MoveReportActivity()
        }


    }

    fun MoveSellerActivity(){
        var intent: Intent = Intent(this, SellerActivity::class.java)
        startActivity(intent)
    }

    fun MoveReportActivity(){
        var intent: Intent = Intent(this, ReportActivity::class.java)
        startActivity(intent)
    }
}
