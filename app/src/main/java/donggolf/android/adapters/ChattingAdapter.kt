package donggolf.android.adapters

import android.content.Context
import android.media.Image
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import donggolf.android.models.PictureCategory
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


open class ChattingAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

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

        var json = data.get(position)

        val chatting = json.getJSONObject("Chatting")
        val member = json.getJSONObject("Member")

        val myId = PrefUtils.getIntPreference(context, "member_id")
        val send_member_id = Utils.getInt(chatting,"member_id")
        val content = Utils.getString(chatting,"content")
        val content_image = Utils.getString(chatting, "image")
        val member_nick = Utils.getString(member,"nick")
        var messageCreated = Utils.getString(chatting,"created")
        var split = messageCreated.split(" ")
        var peoplecount = Utils.getInt(chatting,"peoplecount")
        var read_count = Utils.getInt(chatting,"read_count")
        var difference = peoplecount - read_count


        if (difference > 0){
            item.readTV.setText(difference.toString())
        } else if (difference == 0){
            item.readTV.setText("")
        }

        if (send_member_id == myId) {
            item.myLL.visibility = View.VISIBLE
            item.userLL.visibility = View.GONE
            item.mycontentTV.setText(content)
        } else {
            item.myLL.visibility = View.GONE
            item.userLL.visibility = View.VISIBLE
            item.usernickTV.setText(member_nick)
            item.usercontentTV.setText(content)

            val today = Utils.todayStr()

            if (split.get(0) == today){
                var timesplit = split.get(1).split(":")
                var noon = "오전"
                if (timesplit.get(0).toInt() >= 12){
                    noon = "오후"
                }
                var time = noon + " " + timesplit.get(0) + ":" + timesplit.get(1)
                item.userdateTV.setText(time)
            } else {
                val since = Utils.since(messageCreated)
                item.userdateTV.setText(since)
            }

            var image = Config.url + Utils.getString(member, "profile_img")
            ImageLoader.getInstance().displayImage(image, item.userprofileIV, Utils.UILoptionsUserProfile)

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

        var myLL : LinearLayout
        var userLL : LinearLayout
        var readTV : TextView
        var mycontentTV : TextView
        var userprofileIV : CircleImageView
        var usernickTV : TextView
        var usercontentTV: TextView
        var userdateTV: TextView

        init {
            myLL = v.findViewById<View>(R.id.myLL) as LinearLayout
            userLL = v.findViewById<View>(R.id.userLL) as LinearLayout
            readTV = v.findViewById<View>(R.id.readTV) as TextView
            mycontentTV = v.findViewById<View>(R.id.mycontentTV) as TextView
            userprofileIV = v.findViewById<View>(R.id.userprofileIV) as CircleImageView
            usernickTV = v.findViewById<View>(R.id.usernickTV) as TextView
            usercontentTV = v.findViewById<View>(R.id.usercontentTV) as TextView
            userdateTV = v.findViewById<View>(R.id.userdateTV) as TextView

        }
    }
}
