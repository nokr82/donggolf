package donggolf.android.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.adapters.MarketMainAdapter
import donggolf.android.adapters.SellerAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_seller.*
import org.json.JSONObject

class SellerActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  adapter : SellerAdapter

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        context = this

        finishaLL.setOnClickListener {
            finish()
        }

        var dataObj : JSONObject = JSONObject();

        adapterData.add(dataObj)
        adapterData.add(dataObj)
        adapterData.add(dataObj)

        adapter = SellerAdapter(context,R.layout.item_seller,adapterData)

        gridGV.adapter = adapter



    }
}
