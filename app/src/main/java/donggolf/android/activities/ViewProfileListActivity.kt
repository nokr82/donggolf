package donggolf.android.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import android.widget.Toast
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
import java.util.*
import kotlin.collections.ArrayList

class ViewProfileListActivity : RootActivity() {

    private lateinit var context: Context

    //private lateinit var pagerAdapter: PictureDetailViewAdapter
    private lateinit var pagerAdapter: ProfileSlideViewAdapter

    var profileImagePaths = ArrayList<String>()
    var getImages : ArrayList<Bitmap> = ArrayList()
    private val selected = LinkedList<String>()


    //private lateinit var adverAdapter: FullScreenImageAdapter
    //    var adverImagePaths:ArrayList<String> = ArrayList<String>()
    //
    //    var adPosition = 0

    var imgPosition = 0
    var member_id = 0

    var PROFILE = 100

    private var progressDialog: ProgressDialog? = null

    internal var resetDataReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                getMyProfile()
            }
        }
    }

    internal var deleteDataReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                getMyProfile()
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile_list)

        context = this
        member_id = intent.getIntExtra("viewAlbumUser",0)

        progressDialog = ProgressDialog(context, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

        //pagerAdapter = PictureDetailViewAdapter()
        pagerAdapter = ProfileSlideViewAdapter(this, profileImagePaths,selected)

        albumVP.adapter = pagerAdapter

        getMyProfile()

        closeAlbum.setOnClickListener {
            finish()
        }

        var filter1 = IntentFilter("RESET_DATA")
        registerReceiver(resetDataReceiver, filter1)

        var filter2 = IntentFilter("DELETE_IMG")
        registerReceiver(deleteDataReceiver, filter2)


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
            intent.putExtra("viewAlbumListUserID", member_id.toInt())
            startActivityForResult(intent,PROFILE)
        }

    }
    fun getMyProfile(){
        val params = RequestParams()
        if (PrefUtils.getIntPreference(context, "member_id")!= null){
//            params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))
        } else {
            Toast.makeText(context,"비회원은 이용하실 수 없습니다..", Toast.LENGTH_SHORT).show()
            return
        }

        params.put("member_id", member_id)


        MemberAction.get_member_img_history(params, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                val result = response!!.getString("result")
                if (result == "ok") {
                    profileImagePaths.clear()
                    imgPosition = 0
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

                    if (profileImagePaths.size > 0) {
                        albumPageTV.text = "(" + (imgPosition + 1) + "/" + profileImagePaths.size + ")"
                    } else {
                        albumPageTV.text = "(" + (imgPosition) + "/" + profileImagePaths.size + ")"
                    }

                    if (member_id != PrefUtils.getIntPreference(context, "member_id")){
                        showProfImgAlbumIV.visibility = View.GONE
                    }
                }

            }

            private fun error() {
                Utils.alert(context, "조회중 장애가 발생하였습니다.")
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }

                // System.out.println(responseString);

                throwable.printStackTrace()
                error()
            }

            override fun onStart() {
                // show dialog
                if (progressDialog != null) {

                    progressDialog!!.show()
                }
            }

            override fun onFinish() {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PROFILE -> {
                    if (data!!.getStringExtra("reset") != null){
                        getMyProfile()
                    }
                }
            }
        }
    }

}
