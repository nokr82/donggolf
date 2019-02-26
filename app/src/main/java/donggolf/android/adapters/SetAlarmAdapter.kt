package donggolf.android.adapters

import android.content.Context
import android.graphics.Color
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
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import donggolf.android.models.PictureCategory
import org.json.JSONObject


open class SetAlarmAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

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
        val member_id = Utils.getString(member,"id")
        val chatmember = json.getJSONObject("Chatmember")
        val chatmember_id = Utils.getString(chatmember,"member_id")
        val push_yn = Utils.getString(chatmember,"push_yn")
        val type = Utils.getString(room,"type")

        if (friend == 0){
            item.nofriendIV.visibility = View.VISIBLE
            item.friendIV.visibility = View.GONE
        } else {
            item.nofriendIV.visibility = View.GONE
            item.friendIV.visibility = View.VISIBLE
        }

        if (member_id.toInt() == PrefUtils.getIntPreference(context,"member_id")){
            item.friendIV.visibility = View.GONE
            item.nofriendIV.visibility = View.GONE
        }

        if (PrefUtils.getIntPreference(context,"member_id") == chatmember_id.toInt()) {
            if (push_yn == "Y") {
                item.chatpushonIV.visibility = View.VISIBLE
                item.chatpushoffIV.visibility = View.GONE
                item.txOnOff.setText("켜짐")
                item.txOnOff.setTextColor(Color.parseColor("#0EDA2F"))
            } else {
                item.chatpushonIV.visibility = View.GONE
                item.chatpushoffIV.visibility = View.VISIBLE
                item.txOnOff.setText("꺼짐")
                item.txOnOff.setTextColor(Color.parseColor("#000000"))
            }
        }


        if (content != null && content.length > 0){
            item.contentTV.setText(content)
        }


        item.nickTV.setText(title)

        if (type == "2"){
            var image = Config.url + Utils.getString(room, "intro")
            ImageLoader.getInstance().displayImage(image, item.profPhoto, Utils.UILoptionsUserProfile)
        } else {
            var image = Config.url + Utils.getString(member, "profile_img")
            ImageLoader.getInstance().displayImage(image, item.profPhoto, Utils.UILoptionsUserProfile)
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

        var profPhoto : CircleImageView
        var isOnLine : ImageView
        var nickTV : TextView
        var contentTV : TextView
        var txOnOff : TextView
        var friendIV :ImageView
        var nofriendIV: ImageView
        var chatpushoffIV : ImageView
        var chatpushonIV : ImageView

        init {
            profPhoto = v.findViewById<View>(R.id.profPhoto) as CircleImageView
            isOnLine = v.findViewById<View>(R.id.isOnLine) as ImageView
            nickTV = v.findViewById<View>(R.id.nickTV) as TextView
            contentTV = v.findViewById<View>(R.id.contentTV) as TextView
            txOnOff = v.findViewById<View>(R.id.txOnOff) as TextView
            friendIV = v.findViewById<View>(R.id.friendIV) as ImageView
            nofriendIV = v.findViewById<View>(R.id.nofriendIV) as ImageView
            chatpushoffIV = v.findViewById<View>(R.id.chatpushoffIV) as ImageView
            chatpushonIV = v.findViewById<View>(R.id.chatpushonIV) as ImageView


        }
    }
}
