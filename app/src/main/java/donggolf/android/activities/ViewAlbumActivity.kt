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
import org.json.JSONException
import org.json.JSONObject

class ViewAlbumActivity : RootActivity() {

    lateinit var context: Context

    private lateinit var eachViewAdapter : FullScreenImageAdapter
    private var albumList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_album)

        context = this

        eachViewAdapter = FullScreenImageAdapter(context as ViewAlbumActivity, albumList)

        val params = RequestParams()
        params.put("member_id", PrefUtils.getIntPreference(context, "member_id"))

        MemberAction.get_member_info(params, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                try {
                    val result = response!!.getString("result")
                    if (result == "ok") {
                        albumList.clear()
                        val memberImages = response.getJSONArray("MemberImgs")
                        for (i in 0..memberImages.length()-1) {

                            val json = memberImages[i] as JSONObject
                            val memberImg = json.getJSONObject("MemberImg")
                            var image = Config.url + Utils.getString(memberImg,"image_uri")

                            albumList.add(image)

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
