package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import donggolf.android.R
import donggolf.android.adapters.FindPictureAdapter
import donggolf.android.base.DataBaseHelper
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_find_picture.*
import org.json.JSONObject

class FindPictureActivity : RootActivity() {

        private lateinit var context: Context

        private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

        private  lateinit var  adapter : FindPictureAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_picture)

        context = this


        var intent = getIntent()



        var dataObj : JSONObject = JSONObject();

        adapterData.add(dataObj)

        adapter = FindPictureAdapter(context,R.layout.findpicture_listview_item,adapterData)

        findpictre_listview.adapter = adapter


        adapter.notifyDataSetChanged()

        findpictre_listview.setOnItemClickListener { parent, view, position, id ->

            startActivity(Intent(this,FindPictureGridActivity::class.java))

        }




        btn_finish.setOnClickListener {
            finish()
        }


    }


    fun setDataList(data: Cursor){

        while (data.moveToNext()){
        }

    }

}
