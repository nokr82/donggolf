package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.adapters.AreaRangeAdapter
import donggolf.android.adapters.MarketMainAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_area_range.*
import kotlinx.android.synthetic.main.activity_market_main.*
import org.json.JSONObject

class MarketMainActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  adapter : MarketMainAdapter

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_main)

        context = this

        finishLL.setOnClickListener {
            finish()
        }

        var dataObj : JSONObject = JSONObject();

        adapterData.add(dataObj)
        adapterData.add(dataObj)
        adapterData.add(dataObj)

        adapter = MarketMainAdapter(context,R.layout.item_market_main,adapterData)

        gridGV.adapter = adapter

        gridGV.setOnItemClickListener { parent, view, position, id ->
            MoveGoodsDetailActivity()
        }

        addgoodsTV.setOnClickListener {
            MoveAddGoodsAcitity()
        }


    }

    fun MoveGoodsDetailActivity(){
        var intent: Intent = Intent(this, GoodsDetailActivity::class.java)
        startActivity(intent)
    }

    fun MoveAddGoodsAcitity(){
        var intent: Intent = Intent(this, AddGoodsActivity::class.java)
        startActivity(intent)
    }
}
