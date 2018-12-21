package donggolf.android.activities

import android.content.Context
import android.os.Bundle
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.adapters.FullScreenImageAdapter
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.RootActivity
import donggolf.android.base.Utils
import kotlinx.android.synthetic.main.activity_view_album.*
import org.json.JSONException
import org.json.JSONObject

class ViewAlbumActivity : RootActivity() {

    lateinit var context: Context

    private lateinit var eachViewAdapter : FullScreenImageAdapter
    private var albumList = ArrayList<JSONObject>()//Path Array

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_album)

        context = this

        //eachViewAdapter = FullScreenImageAdapter(context as ViewAlbumActivity, albumList)

        //entireImageCntTV
        //잡다한 뷰 세팅
        finishBT.setOnClickListener { finish() }
        albumMenuIV.setOnClickListener {
            //메뉴 띄우기
        }

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        MemberAction.get_member_img_history(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        albumList.clear()
                        val memberImages = response.getJSONArray("MemberImgs")
                        for (i in 0 until memberImages.length()) {

                            val json = memberImages[i] as JSONObject

                            albumList.add(json)

                        }
                        eachViewAdapter.notifyDataSetChanged()
                    }
                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }
}
