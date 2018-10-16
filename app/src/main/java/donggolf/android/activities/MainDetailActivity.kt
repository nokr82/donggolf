package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import donggolf.android.R
import kotlinx.android.synthetic.main.activity_main_detail.*
import android.graphics.Paint.FAKE_BOLD_TEXT_FLAG
import donggolf.android.adapters.FindPictureAdapter
import donggolf.android.adapters.MainDeatilAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_find_picture.*
import org.json.JSONObject


class MainDetailActivity : RootActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    private  lateinit var  adapter : MainDeatilAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_detail)

        detail_add_comment.paintFlags = detail_add_comment.getPaintFlags() or Paint.FAKE_BOLD_TEXT_FLAG

        context = this

        var dataObj : JSONObject = JSONObject();

        adapterData.add(dataObj)


        adapter = MainDeatilAdapter(context,R.layout.main_detail_listview_item,adapterData)

        main_detail_listview.adapter = adapter


        adapter.notifyDataSetChanged()

        main_detail_gofindpicture.setOnClickListener {
            startActivity(Intent(this,FindPictureActivity::class.java))
        }


    }
    fun setDataList(data: Cursor){

        while (data.moveToNext()){
        }

    }
}
