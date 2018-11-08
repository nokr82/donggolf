package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.MotionEvent
import donggolf.android.R
import kotlinx.android.synthetic.main.activity_main_detail.*
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import donggolf.android.actions.ContentAction
import donggolf.android.adapters.FullScreenImageAdapter
import donggolf.android.adapters.MainDeatilAdapter
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainDetailActivity : RootActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    private  lateinit var  adapter : MainDeatilAdapter

    private lateinit var adverAdapter: FullScreenImageAdapter
    var adverImagePaths:ArrayList<String> = ArrayList<String>()


    var adPosition = 0;

    var PICTURE_DETAIL = 1





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

        if (intent.hasExtra("id")){
            val id = intent.getStringExtra("id")


            ContentAction.viewContent(id){ success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                if (success){
                    if(data != null){
                        if(data.size != 0){

                            println("data : $data")
                            val time: Long = data["createAt"] as Long

                            val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA)

                            val currentTime: String = dateFormat.format(Date(time))

                            titleTV.text = data["title"].toString()
                            dateTV.text = currentTime.toString()
                            viewTV.text = data["looker"].toString()
                            nickNameTV.text = data["owner"].toString()

                            var texts:ArrayList<HashMap<Objects, Objects>> = data.get("texts") as ArrayList<HashMap<Objects, Objects>>

                            for(i in 0.. (texts.size-1)){
//            var text = texts.get(i)
                                val text_ = JSONObject(texts.get(i))
                                println(text_)
                                print( " ============================= ")

                                val type = Utils.getString(text_, "type")

                                if(type == "text") {
                                    val text = text_.get("text")as String
                                    textTV.text = text
                                } else if (type == "photo") {
                                    val photo = text_.get("file") as JSONArray
                                    println("photo : ========== $photo")


                                    for(i in 0.. (photo.length() - 1)) {

                                        FirebaseFirestoreUtils.getFileUri("imgl/"+photo[i].toString()) { b: Boolean, s: String?, exception: Exception? ->
                                            if (s != null) {
                                                adverImagePaths.add(s)

                                                adverAdapter.notifyDataSetChanged()
                                            }
                                            println("Paths ====================== $s")
                                        }
                                    }

                                } else if (type == "video"){
                                    val video = text_.get("file") as JSONArray
                                    println("video : ========= $video")
                                }

                            }

                            println("data : " + data)
//
//                            var bt: Bitmap = Utils.getImage(context.contentResolver, data[0]!!["door_image"].toString(), 100)
//
//                            detailIV.setImageBitmap(bt)

                            //상태메시지


                        }
                    }

                } else {

                }
            }
        }

        adverAdapter = FullScreenImageAdapter(this, adverImagePaths)
        pagerVP.adapter = adverAdapter
        pagerVP.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                adPosition = position
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {
                circleLL.removeAllViews()
                for (i in adverImagePaths.indices) {
                    if (i == adPosition) {
//                        addDot(circleLL, true)
                    } else {
//                        addDot(circleLL, false)
                    }
                }
            }
        })

        pagerVP.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {

                    MotionEvent.ACTION_DOWN ->{

                    }

                    MotionEvent.ACTION_UP -> {

                        if (intent.hasExtra("id")) {
                            val id = intent.getStringExtra("id")
                            var intent = Intent(context, PictureDetailActivity::class.java);
                            intent.putExtra("id", id)
                            startActivityForResult(intent, PICTURE_DETAIL);
                        }

                    }
                }




                return v?.onTouchEvent(event) ?: true
            }
        })

        finishLL.setOnClickListener {
            finish()
        }

        main_detail_gofindpicture.setOnClickListener {
            MoveFindPictureActivity()
        }

        plusBT.setOnClickListener {
            relativ_RL.visibility = View.VISIBLE
        }

        goneRL.setOnClickListener {
            relativ_RL.visibility = View.GONE
        }

        reportTV.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("신고하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->


                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }



        deleteTV.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("정말로 삭제하시겠습니까 ?")

                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                        delete()
                    })
                    .setNegativeButton("취소",DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                    })
            val alert = builder.create()
            alert.show()
        }

        modifyTV.setOnClickListener {
            modify()
        }






    }

    fun MoveFindPictureActivity(){
        startActivity(Intent(this,FindPictureActivity::class.java))
    }

    fun modify(){
        if (intent.hasExtra("id")) {
            val id = intent.getStringExtra("id")
            val intent = Intent(this, AddPostActivity::class.java)
            intent.putExtra("category",2)
            intent.putExtra("id",id)
            startActivity(intent)
        }

    }

    fun delete(){

        val builder = AlertDialog.Builder(context)
        builder
                .setMessage("정말 삭제하시겠습니까 ?")

                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                    if (intent.hasExtra("id")) {
                        val id = intent.getStringExtra("id")

                        ContentAction.deleteContent(id){
                            if(it){
                                println("suc")

                            }else {

                            }

                        }
                    }

                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()
                })
        val alert = builder.create()
        alert.show()

    }





}
