package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import donggolf.android.R
import donggolf.android.adapters.FindPictureAdapter
import donggolf.android.base.DataBaseHelper
import donggolf.android.base.RootActivity
import donggolf.android.models.PictureCategory
import kotlinx.android.synthetic.main.activity_find_picture.*
import org.json.JSONObject

class FindPictureActivity : RootActivity() {

        private lateinit var context: Context

        private  var adapterData : ArrayList<PictureCategory> = ArrayList<PictureCategory>()

        private  lateinit var  adapter : FindPictureAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_picture)

        context = this


        var intent = getIntent()

        var camera = PictureCategory("카메라",0,1,null)
        var video = PictureCategory("동영상",0,2,null)


        adapterData.add(camera)
        adapterData.add(video)

        adapter = FindPictureAdapter(context,R.layout.findpicture_listview_item,adapterData)

        findpictre_listview.adapter = adapter

        adapter.notifyDataSetChanged()

        findpictre_listview.setOnItemClickListener { parent, view, position, id ->

            var item = adapterData.get(position)

            if (item.category == 1){
                Log.d("yjs", "category :" + item.category)
                MoveFindPictureGridActivity()
            }else if (item.category == 2){
                MoveFindVideoActivity()
            }


        }





        btn_finish.setOnClickListener {
            finish()
        }


    }
    fun MoveFindVideoActivity(){
        startActivity(Intent(this,FindVideoActivity::class.java))
    }

    fun MoveFindPictureGridActivity(){
        startActivity(Intent(this,FindPictureGridActivity::class.java))
    }

}
