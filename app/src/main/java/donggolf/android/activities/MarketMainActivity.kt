package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import donggolf.android.R
import donggolf.android.adapters.MarketMainAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_market_main.*
import org.json.JSONObject

class MarketMainActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  adapter : MarketMainAdapter

    private  var adapterData = ArrayList<JSONObject>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_main)

        context = this


        finishmLL.setOnClickListener {
            finish()
        }

        init_menu()
        entireClassificationTV.setTextColor(Color.parseColor("#0EDA2F"))

        //목록 가져와서 array에 추가
        var dataObj = JSONObject()

        adapterData.add(dataObj)
        adapterData.add(dataObj)
        adapterData.add(dataObj)

        //set adapter
        adapter = MarketMainAdapter(context,R.layout.item_market_main,adapterData)

        maingridGV.adapter = adapter

        maingridGV.setOnItemClickListener { parent, view, position, id ->
            MoveGoodsDetailActivity()
        }

        addgoodsTV.setOnClickListener {
            MoveAddGoodsAcitity()
        }


    }

    fun MoveGoodsDetailActivity(){
        var intent = Intent(this, GoodsDetailActivity::class.java)
        startActivity(intent)
    }

    fun MoveAddGoodsAcitity(){
        var intent = Intent(this, AddGoodsActivity::class.java)
        startActivity(intent)
    }

    fun init_menu(){
        entireClassificationTV.setTextColor(Color.parseColor("#000000"))
        entireBrandTV.setTextColor(Color.parseColor("#000000"))
        entireTypeTV.setTextColor(Color.parseColor("#000000"))
    }
}
