package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import donggolf.android.R
import donggolf.android.adapters.FindPictureGridAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_find_picture_grid.*
import org.json.JSONObject

class FindPictureGridActivity : RootActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    private  lateinit var  adapter : FindPictureGridAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_picture_grid)


        context = this


        var intent = getIntent()


        var dataList:Array<String> = arrayOf("findpicure_name","findpicure_count")


        var dataObj : JSONObject = JSONObject();

        adapterData.add(dataObj)

        adapter = FindPictureGridAdapter(context,R.layout.findpicture_gridview_item,adapterData)

        findpictre_gridview.adapter = adapter


        adapter.notifyDataSetChanged()


        btn_finish.setOnClickListener {
            finish()
        }
    }

    fun setDataList(data: Cursor){

        while (data.moveToNext()){
        }

    }
}
