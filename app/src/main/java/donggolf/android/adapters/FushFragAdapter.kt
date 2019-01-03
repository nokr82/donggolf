package donggolf.android.adapters

import android.content.Context
import android.media.Image
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.base.Config
import donggolf.android.base.Utils
import donggolf.android.models.PictureCategory
import org.json.JSONObject


open class FushFragAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<JSONObject> = data

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

        var data: JSONObject = getItem(position)
        val alarm = data.getJSONObject("Alarm")
        val sender = data.getJSONObject("Sender")

        item.contentTV.text = Utils.getString(alarm, "contents")

        var profile = Config.url + Utils.getString(sender, "profile_img")
        ImageLoader.getInstance().displayImage(profile, item.profileIV, Utils.UILoptionsUserProfile)

        val type = Utils.getInt(alarm , "type")

        var title = ""

        if(type == 1) {

            val content = data.getJSONObject("Content")

            title = "[우리동네] " + Utils.getString(content, "title")

        } else if (type == 2) {

            val market = data.getJSONObject("Market")

            title = "[중고장터] " + Utils.getString(market, "title")

        } else {
            title = "[친구신청] 새로운 친구 신청입니다."
        }

        item.titleTV.text = title

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

        var profileIV : CircleImageView
        var titleTV : TextView
        var contentTV : TextView
        var timeTV : TextView

        init {
            profileIV = v.findViewById<View>(R.id.profileIV) as CircleImageView
            titleTV = v.findViewById<View>(R.id.titleTV) as TextView
            contentTV = v.findViewById<View>(R.id.contentTV) as TextView
            timeTV = v.findViewById<View>(R.id.timeTV) as TextView
        }
    }
}
