package donggolf.android.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import donggolf.android.adapters.AreaRangeAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_area_range.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class AreaRangeActivity : RootActivity() {

    private lateinit var context: Context

    private  lateinit var  adapter : AreaRangeAdapter

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_range)

        context = this

        finishBT.setOnClickListener {
            finish()
        }

        var dataObj: JSONObject = JSONObject();

        adapterData.add(dataObj)
        adapterData.add(dataObj)
        adapterData.add(dataObj)

        adapter = AreaRangeAdapter(context, R.layout.item_area_range, adapterData)

        listLV.adapter = adapter



    }
}
