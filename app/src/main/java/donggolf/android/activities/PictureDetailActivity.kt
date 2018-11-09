package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.ViewGroup
import android.widget.ImageView
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.adapters.FullScreenImageAdapter
import donggolf.android.adapters.PictureDetailViewAdapter
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.ImageLoader
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_main_detail.*
import kotlinx.android.synthetic.main.activity_picture_detail.*
import org.json.JSONArray
import org.json.JSONObject
import uk.co.senab.photoview.PhotoViewAttacher
import java.text.SimpleDateFormat
import java.util.*

class PictureDetailActivity : RootActivity() {

    private lateinit var context: Context

    private lateinit var pagerAdapter: PictureDetailViewAdapter

    var adverImagePaths: ArrayList<String> = ArrayList<String>()

    var adPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_detail)

        context = this

        pagerAdapter = PictureDetailViewAdapter()

        viewpagerVP.setAdapter(pagerAdapter)

        ifinishLL.setOnClickListener {
            finish()
        }

        if (intent.hasExtra("id")) {
            val id = intent.getStringExtra("id")


            ContentAction.viewContent(id){ success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                if (success){
                    if(data != null){
                        if(data.size != 0){

                            println("data : $data")
                            val time: Long = data["createAt"] as Long

                            val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA)

                            var texts:ArrayList<HashMap<Objects, Objects>> = data.get("texts") as ArrayList<HashMap<Objects, Objects>>

                            for(i in 0.. (texts.size-1)){

                                val text_ = JSONObject(texts.get(i))

                                val type = Utils.getString(text_, "type")

                                if(type == "text") {
                                    val text = text_.get("text")as String
                                } else if (type == "photo") {
                                    val photo = text_.get("file") as JSONArray

                                    pageTV.setText("(" + 1 + "/" + photo.length() + ")")
                                    for(i in 0.. (photo.length() - 1)) {

                                        FirebaseFirestoreUtils.getFileUri("imgl/"+photo[i].toString()) { b: Boolean, s: String?, exception: Exception? ->
                                            if (s != null) {
                                                adverImagePaths.add(s)

                                                val iv = ImageView(context)

                                                val photoViewAttacher = PhotoViewAttacher(iv)
                                                photoViewAttacher.scaleType = ImageView.ScaleType.FIT_XY

                                                com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(s,iv,Utils.UILoptions)

                                                if(i == 0) {
                                                    pagerAdapter.addView(iv,i)
                                                } else {
                                                    pagerAdapter.addView(iv)
                                                }



                                                pagerAdapter.notifyDataSetChanged()
                                            }
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



        viewpagerVP.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                adPosition = position
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {
                pageTV.setText("(" + (adPosition + 1) + "/" + adverImagePaths.size + ")")
            }
        })


    }
}
