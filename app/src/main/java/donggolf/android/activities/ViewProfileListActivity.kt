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
import donggolf.android.actions.MemberAction
import donggolf.android.adapters.ProfileSlideViewAdapter
import donggolf.android.base.*
import kotlinx.android.synthetic.main.activity_view_profile_list.*
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.ArrayList

class ViewProfileListActivity : RootActivity() {

    private lateinit var context: Context

    //private lateinit var pagerAdapter: PictureDetailViewAdapter
    private lateinit var pagerAdapter: ProfileSlideViewAdapter

    var profileImagePaths = ArrayList<String>()
    var getImages : ArrayList<Bitmap> = ArrayList()


    //private lateinit var adverAdapter: FullScreenImageAdapter
    //    var adverImagePaths:ArrayList<String> = ArrayList<String>()
    //
    //    var adPosition = 0

    var imgPosition = 0
    var member_id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile_list)

        context = this
        member_id = intent.getIntExtra("viewAlbumUser",0)

        //pagerAdapter = PictureDetailViewAdapter()
        pagerAdapter = ProfileSlideViewAdapter(this, profileImagePaths)

        albumVP.adapter = pagerAdapter

        closeAlbum.setOnClickListener {
            finish()
        }

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        MemberAction.get_member_img_history(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        profileImagePaths.clear()
                        val memberImages = response.getJSONArray("MemberImgs")
                        for (i in 0 until memberImages.length()) {

                            val json = memberImages[i] as JSONObject
                            val memberImg = json.getJSONObject("MemberImg")
                            var image = Config.url + Utils.getString(memberImg,"image_uri")

                            profileImagePaths.add(image)

                            /*val btm = BitmapFactory.decodeFile(image)

                            getImages.add(btm)*/

                        }

                        pagerAdapter.notifyDataSetChanged()

                        albumPageTV.text = "(" + (imgPosition + 1) + "/" + profileImagePaths.size + ")"
                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }

            }
        })



        albumVP.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                imgPosition = position
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {
                albumPageTV.text = "(" + (imgPosition + 1) + "/" + profileImagePaths.size + ")"
            }
        })

        showProfImgAlbumIV.setOnClickListener {
            val intent = Intent(context, ViewAlbumActivity::class.java)
            intent.putExtra("viewAlbumListUserID", member_id)
            startActivity(intent)
        }

    }
}
