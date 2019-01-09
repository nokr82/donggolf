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


open class ChatFragAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

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

        var room = json.getJSONObject("Chatroom")

        val title = Utils.getString(room,"title")
        val friend = Utils.getInt(room,"friend")
        val content = Utils.getString(room,"contents")
        val member = json.getJSONObject("Member")
        val chatmember = json.getJSONObject("Chatmember")
        val push_yn = Utils.getString(chatmember,"push_yn")

        if (push_yn == "Y"){
            item.pushonIV.visibility = View.VISIBLE
            item.pushoffIV.visibility = View.GONE
        } else {
            item.pushonIV.visibility = View.GONE
            item.pushoffIV.visibility = View.VISIBLE
        }


        if (content != null && content.length > 0){
            item.chatcontentTV.setText(content)
        } else if(content == ""){
            item.chatcontentTV.setText("")
        }

        val created = Utils.getString(room,"created")
        val today = Utils.todayStr()
        if (created != null && created.length > 0){
            var split = created.split(" ")


            if (split != null && split.size > 0) {

                if (split.get(0) == today){
                    var timesplit = split.get(1).split(":")
                    var noon = "오전"
                    if (timesplit.get(0).toInt() >= 12){
                        noon = "오후"
                    }
                    var time = noon + " " + timesplit.get(0) + ":" + timesplit.get(1)
                    item.timeTV.setText(time)

                } else {
                    val since = Utils.since(created)
                    item.timeTV.setText(since)
                }
            }
        }

        if (friend == 0){
            item.nofriendIV.visibility = View.VISIBLE
            item.firstIV.visibility = View.GONE
        } else {
            item.nofriendIV.visibility = View.GONE
            item.firstIV.visibility = View.VISIBLE
        }

        item.nickTV.setText(title)
        var image = Config.url + Utils.getString(member, "profile_img")
        ImageLoader.getInstance().displayImage(image, item.profPhoto, Utils.UILoptionsUserProfile)

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

        var profPhoto : CircleImageView
        var isOnLine : ImageView
        var nickTV : TextView
        var firstIV : ImageView
        var timeTV : TextView
        var chatcontentTV : TextView
        var nofriendIV: ImageView
        var pushoffIV : ImageView
        var pushonIV : ImageView

        init {
            profPhoto = v.findViewById<View>(R.id.profPhoto) as CircleImageView
            isOnLine = v.findViewById<View>(R.id.isOnLine) as ImageView
            nickTV = v.findViewById<View>(R.id.nickTV) as TextView
            firstIV = v.findViewById<View>(R.id.firstIV) as ImageView
            timeTV = v.findViewById<View>(R.id.timeTV) as TextView
            chatcontentTV = v.findViewById<View>(R.id.chatcontentTV) as TextView
            nofriendIV = v.findViewById<View>(R.id.nofriendIV) as ImageView
            pushoffIV = v.findViewById<View>(R.id.pushoffIV) as ImageView
            pushonIV = v.findViewById<View>(R.id.pushonIV) as ImageView


        }
    }
}
