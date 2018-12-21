package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.widget.ImageView
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.actions.MemberAction
import donggolf.android.actions.ProfileAction
import donggolf.android.adapters.FullScreenImageAdapter
import donggolf.android.adapters.PictureDetailViewAdapter
import donggolf.android.base.*
import kotlinx.android.synthetic.main.activity_picture_detail.*
import kotlinx.android.synthetic.main.activity_view_profile_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import uk.co.senab.photoview.PhotoViewAttacher
import java.io.FileInputStream
import java.lang.reflect.InvocationTargetException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ViewProfileListActivity : RootActivity() {

    private lateinit var context: Context

    private lateinit var pagerAdapter: PictureDetailViewAdapter

    var getImages : ArrayList<Bitmap> = ArrayList()

    var adPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile_list)

        context = this

        pagerAdapter = PictureDetailViewAdapter()

        albumVP.adapter = pagerAdapter

        closeAlbum.setOnClickListener {
            finish()
        }

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        getImages.clear()
                        val memberImages = response.getJSONArray("MemberImgs")
                        for (i in 0 until memberImages.length()) {

                            val json = memberImages[i] as JSONObject
                            val memberImg = json.getJSONObject("MemberImg")
                            var image = Config.url + "/" + Utils.getString(memberImg,"image_uri")

                            val btm = BitmapFactory.decodeFile(image)

                            getImages.add(btm)

                        }
                        pagerAdapter.notifyDataSetChanged()
                        albumPageTV.text = "(" + (adPosition + 1) + "/" + getImages.size + ")"
                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }

            }
        })



        albumVP.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                adPosition = position
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {
                albumPageTV.text = "(" + (adPosition + 1) + "/" + getImages.size + ")"
            }
        })

        showProfImgAlbumIV.setOnClickListener {
            startActivity(Intent(context, ViewAlbumActivity::class.java))
        }

    }
}
