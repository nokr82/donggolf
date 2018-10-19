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
import android.util.Log
import donggolf.android.actions.ContentAction
import donggolf.android.adapters.FindPictureAdapter
import donggolf.android.adapters.MainDeatilAdapter
import donggolf.android.base.RootActivity
import donggolf.android.models.Content
import kotlinx.android.synthetic.main.activity_find_picture.*
import org.json.JSONObject
import java.util.*


class MainDetailActivity : RootActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    private  lateinit var  adapter : MainDeatilAdapter


    private val ONE_SECOND : Long = 1000L

    private val ONE_MINUTE : Long = ONE_SECOND * 60L

    private val ONE_HOUR : Long= ONE_MINUTE*60L

    private val GMT_OFFSET_HOUR = (TimeZone.getDefault().rawOffset / ONE_HOUR)

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
            MoveFindPictureActivity()
        }

        if (intent.hasExtra("owner")){
            val owner = intent.getStringExtra("owner")
            val params = HashMap<String, Any>()
            params.put("owner",owner)

            ContentAction.getContent(params){ success: Boolean, data: ArrayList<Map<String, Any>?>?, exception: Exception? ->
                if (success){
                    if(data != null){
                        if(data.size != 0){
                            val time : Long = data[0]!!["createAt"] as Long
                            val second = time / ONE_SECOND % 60L
                            val minute = time / ONE_MINUTE % 60L
                            val hour = ((time / ONE_HOUR % 24L) + GMT_OFFSET_HOUR) % 24

                            titleTV.text = data[0]!!["title"].toString()
                            dateTV.text = hour.toString() + "시 " + minute.toString() + "분 " + second.toString() + "초"
                            viewTV.text = data[0]!!["looker"].toString()
                            nickNameTV.text = data[0]!!["owner"].toString()
                            //상태메시지
                            textTV.text = data[0]!!["texts"].toString()
                        }
                    }

                } else {

                }
            }
        }

    }

    fun MoveFindPictureActivity(){
        startActivity(Intent(this,FindPictureActivity::class.java))
    }


}
