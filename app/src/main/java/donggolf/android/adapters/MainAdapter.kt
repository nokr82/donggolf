package donggolf.android.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.base.Config
import donggolf.android.base.Utils
import donggolf.android.models.Text
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList
import android.support.v4.os.HandlerCompat.postDelayed
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import com.kakao.auth.StringSet.file
import android.graphics.Bitmap
import donggolf.android.R
import donggolf.android.base.DownloadFileAsyncTask


open class MainAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<JSONObject> = data

    var text:Text = Text()
    var photo:JSONArray = JSONArray()
    var video:ArrayList<String> = ArrayList<String>()



    override fun getView(position: Int, convertView: View?, parent : ViewGroup?): View {

        lateinit var retView: View

        if (convertView == null) {
            retView = View.inflate(context, view, null)
            item = ViewHolder(retView)
            retView.tag = item
        } else {
            retView = convertView
            item = convertView.tag as ViewHolder
            if (item == null) {
                retView = View.inflate(context, view, null)
                item = ViewHolder(retView)
                retView.tag = item
            }
        }

        var json = data.get(position)
        var Content = json.getJSONObject("Content")
        val member_id = Utils.getString(Content,"member_id")
        val title = Utils.getString(Content,"title")
        val text = Utils.getString(Content,"text")
        val content_id = Utils.getString(Content,"id")
        val favorite_cnt = Utils.getString(Content,"favorite_cnt")
        val look_cnt = Utils.getString(Content,"look_cnt")
        val like_cnt = Utils.getString(Content,"like_cnt")
        val cmt_cnt = Utils.getString(Content,"cmt_cnt")
        var member = json.getJSONObject("Member")
        var nick = Utils.getString(member,"nick")
        var sex = Utils.getString(member,"sex")
        var profile = Utils.getString(member,"profile_img")
        var created = Utils.getString(Content,"created")
        var image_uri = Utils.getString(Content,"image_uri")

        val since = Utils.since(created)

        if (sex == "0"){
            item.main_item_nickname.setTextColor(Color.parseColor("#000000"))
        }else{
            item.main_item_nickname.setTextColor(Color.parseColor("#EF5C34"))
        }

        item.main_item_title.text = title.toString()
        item.main_item_content.text = text.toString()
        item.main_item_view_count.text = look_cnt.toString()
        item.main_item_like_count.text = like_cnt.toString()
        item.main_item_nickname.text = nick.toString()
        item.main_item_comment_count.text = cmt_cnt.toString()
        item.main_item_lately.text = since.toString()

        if (image_uri != null){
            if (image_uri != "") {
                var image_type = Utils.getString(Content,"image_type")
                var image = Config.url + image_uri
                if (image_type == "1") {
                    item.profileIV.visibility = View.VISIBLE
                    item.videoVV.visibility = View.GONE
                    ImageLoader.getInstance().displayImage(image, item.profileIV, Utils.UILoptionsProfile)
                } else {
                    item.videoVV.visibility = View.GONE
                    item.profileIV.visibility = View.VISIBLE

                    val bMap = ThumbnailUtils.createVideoThumbnail(image.toString(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
                    item.profileIV.setImageBitmap(bMap)

                }
            } else {
                item.profileIV.visibility = View.GONE
                item.videoVV.visibility = View.GONE
            }
        } else {
            item.profileIV.visibility = View.GONE
            item.videoVV.visibility = View.GONE
        }

        if (profile != null){
            var image = Config.url + profile
            ImageLoader.getInstance().displayImage(image, item.main_item_image, Utils.UILoptionsUserProfile)
        } else {
            item.main_item_image.setImageResource(donggolf.android.R.drawable.icon_profiles)
        }


        return retView
    }

    override fun getItem(position: Int): JSONObject {

        return data.get(position)
    }

    override fun getItemId(position: Int): Long {

        return position.toLong()
    }

    override fun getCount(): Int {

        return data.count()
    }

    fun removeItem(position: Int){
        data.removeAt(position)
        notifyDataSetChanged()
    }

    class ViewHolder(v: View) {

        var main_item_title : TextView
        var main_item_content : TextView

        var main_item_nickname : TextView
        var main_item_lately : TextView
        var main_item_view_count : TextView
        var main_item_comment_count : TextView
        var main_item_like_count : TextView
        var profileIV: ImageView
        var main_item_image : CircleImageView
        var videoVV: VideoView

        init {

            main_item_title = v.findViewById<View>(R.id.main_item_title) as TextView
            main_item_content = v.findViewById<View>(R.id.main_item_content) as TextView
            main_item_nickname = v.findViewById<View>(R.id.main_item_nickname) as TextView
            main_item_lately = v.findViewById<View>(R.id.main_item_lately) as TextView
            main_item_view_count = v.findViewById<View>(R.id.main_item_view_count) as TextView
            main_item_comment_count = v.findViewById<View>(R.id.main_item_comment_count) as TextView
            main_item_like_count = v.findViewById<View>(R.id.main_item_like_count) as TextView
            profileIV = v.findViewById<View>(R.id.profileIV) as ImageView
            main_item_image = v.findViewById<View>(R.id.main_item_image) as CircleImageView
            videoVV = v.findViewById<View>(R.id.videoVV) as VideoView

        }


    }


}