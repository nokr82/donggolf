package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Bundle
import donggolf.android.R
import kotlinx.android.synthetic.main.activity_main_detail.*
import android.util.Log
import android.view.View
import donggolf.android.actions.ContentAction
import donggolf.android.adapters.MainDeatilAdapter
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainDetailActivity : RootActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    private  lateinit var  adapter : MainDeatilAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_detail)

        detail_add_comment.paintFlags = detail_add_comment.getPaintFlags() or Paint.FAKE_BOLD_TEXT_FLAG

        context = this
        intent = getIntent()

        var dataObj : JSONObject = JSONObject();

        adapterData.add(dataObj)


        adapter = MainDeatilAdapter(context,R.layout.main_detail_listview_item,adapterData)

        main_detail_listview.adapter = adapter

        adapter.notifyDataSetChanged()

        main_detail_gofindpicture.setOnClickListener {
            MoveFindPictureActivity()
        }

        plusBT.setOnClickListener {
            relativ_RL.visibility = View.VISIBLE
        }

        goneRL.setOnClickListener {
            relativ_RL.visibility = View.GONE
        }


        if (intent.hasExtra("id")){
            val id = intent.getStringExtra("id")
            val params = HashMap<String, Any>()
            params.put("id",id)

            ContentAction.getContent(params){ success: Boolean, data: ArrayList<Map<String, Any>?>?, exception: Exception? ->
                if (success){
                    if(data != null){
                        if(data.size != 0){
                            val time: Long = data[0]!!["createAt"] as Long

                            val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA)

                            val currentTime: String = dateFormat.format(Date(time))

                            titleTV.text = data[0]!!["title"].toString()
                            dateTV.text = currentTime.toString()
                            viewTV.text = data[0]!!["looker"].toString()
                            nickNameTV.text = data[0]!!["owner"].toString()

                            println("data : " + data)
//
//                            var bt: Bitmap = Utils.getImage(context.contentResolver, data[0]!!["door_image"].toString(), 100)
//
//                            detailIV.setImageBitmap(bt)

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
