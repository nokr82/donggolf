package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import donggolf.android.R
import donggolf.android.adapters.MarketMainAdapter
import donggolf.android.adapters.SellerAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_seller.*
import kotlinx.android.synthetic.main.dlg_comment_menu.view.*
import org.json.JSONObject

class SellerActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  adapter : SellerAdapter

    private  var adapterData = ArrayList<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        context = this

        finishaLL.setOnClickListener {
            finish()
        }

        adapter = SellerAdapter(context,R.layout.item_seller,adapterData)

        gridGV.adapter = adapter

        //분류전체
        sel_formTV.setOnClickListener {

        }

        //브랜드전체
        sel_brandTV.setOnClickListener {

        }

        //종류 전체
        sel_typeTV.setOnClickListener {

        }

        //판매상태 전체
        sel_statusTV.setOnClickListener {

        }

    }


}
