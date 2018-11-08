package donggolf.android.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import donggolf.android.R
import kotlinx.android.synthetic.main.activity_main_detail.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import donggolf.android.actions.ContentAction
import donggolf.android.adapters.MainDeatilAdapter
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import donggolf.android.fragment.DetailFragment1
import donggolf.android.fragment.DetailFragment2
import donggolf.android.fragment.DetailFragment3
import donggolf.android.models.Content
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainDetailActivity : FragmentActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    private  lateinit var  adapter : MainDeatilAdapter

    internal lateinit var pagerAdapter: PagerAdapter

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

        pagerAdapter = PagerAdapter(getSupportFragmentManager())
        pagerVP.adapter = pagerAdapter
        pagerAdapter.notifyDataSetChanged()

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


                                    for(i in 0.. photo.length() - 1) {
                                        FirebaseFirestoreUtils.getFileUri("imgl/"+photo[i].toString()) { b: Boolean, s: String?, exception: Exception? ->
                                            println(" b: Boolean, s: String?, exception: Exception? $b , $s , $exception")

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

    class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(i: Int): Fragment {
            val fragment: Fragment
            val args = Bundle()
            when (i) {
                0 -> {
                    fragment = DetailFragment1()
                    fragment.arguments = args
                    return fragment
                }
                1 -> {
                    fragment = DetailFragment2()
                    fragment.arguments = args
                    return fragment
                }
                else -> {
                    fragment = DetailFragment3()
                    fragment.arguments = args
                    return fragment
                }
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return ""
        }

        override fun destroyItem(viewPager: ViewGroup, position: Int, `object`: Any) {
            //
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

    }


}
