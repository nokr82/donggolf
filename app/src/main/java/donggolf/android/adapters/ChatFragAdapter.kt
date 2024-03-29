package donggolf.android.adapters

import android.content.Context
import android.graphics.Color
import android.media.Image
import android.util.Log
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
        var room_member_id = Utils.getString(room,"member_id")
        var peoplecount = Utils.getString(room,"peoplecount")
        var profileimg = Utils.getString(room,"profileimg")
        var room_type = Utils.getString(room,"type")
        var max_count = Utils.getString(room,"max_count")

        val title = Utils.getString(room,"title")
        val friend = Utils.getInt(room,"friend")
        val content = Utils.getString(room,"contents")
//        val chatmember = room.getJSONArray("Chatmember")
        val chatmember = json.getJSONArray("Chatmember")
        var nick = ""
        val readdiv = Utils.getString(room,"readdiv")
        val room_created = Utils.getString(room,"room_created")

        if (readdiv == "0"){
            item.readIV.visibility = View.VISIBLE
        } else {
            item.readIV.visibility = View.GONE
        }

        item.addNickLL.removeAllViews()

        for (i in 0 until chatmember.length()){
            var roomitem = chatmember.get(i) as JSONObject
            val push_yn = Utils.getString(roomitem,"push_yn")
            val member = roomitem.getJSONObject("Member")
            val member_id = Utils.getString(member,"id")

            if (member_id.toInt() == PrefUtils.getIntPreference(context,"member_id")){
                if (push_yn == "Y"){
                    item.pushonIV.visibility = View.VISIBLE
                    item.pushoffIV.visibility = View.GONE
                } else {
                    item.pushonIV.visibility = View.GONE
                    item.pushoffIV.visibility = View.VISIBLE
                }
                chatmember.remove(i)
                break
            }
        }

        val count = peoplecount.toInt() - 3

        for (i in 0 until chatmember.length()){
            var roomitem = chatmember.get(i) as JSONObject
            val member = roomitem.getJSONObject("Member")
            val member_nick = Utils.getString(member,"nick")
            val gander = Utils.getString(member,"sex")

            nick = member_nick
            var v = View.inflate(context, R.layout.item_nick, null)
            var fv = View.inflate(context, R.layout.item_nick, null)
            var nickTV = v.findViewById(R.id.nickTV) as TextView
            var female = fv.findViewById(R.id.nickTV) as TextView
            nickTV.setTextColor(Color.parseColor("#000000"))
            female.setTextColor(Color.parseColor("#EF5C34"))

            if (i == 0){
                nickTV.setText(nick)
                if (gander == "1"){
                    female.setText(nick)
                    item.addNickLL.addView(fv)
                } else {
                    item.addNickLL.addView(v)
                }
            } else if (i < 3) {
                nickTV.setText("/"+nick)
                if (gander == "1"){
                    nickTV.setText("/")
                    item.addNickLL.addView(v)
                    female.setText(nick)
                    item.addNickLL.addView(fv)
                } else {
                    item.addNickLL.addView(v)
                }
            } else if (i == 3) {
                nickTV.setText("외 " + count.toString() + "명")
                item.addNickLL.addView(v)
            } else {
                nickTV.setText("")
                female.setText("")
            }

        }

        if (content != null && content.length > 0){
            item.chatcontentTV.setText(content)
        } else if(content == ""){
            item.chatcontentTV.setText("")
            item.readIV.visibility = View.GONE
        }

//        val created = Utils.getString(room,"created")
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
        } else {
            val since = Utils.since(room_created)
            item.timeTV.setText(since)
        }

        if (chatmember.length() == 2){
            if (friend == 0){
                item.nofriendIV.visibility = View.VISIBLE
                item.firstIV.visibility = View.GONE
            } else {
                item.nofriendIV.visibility = View.GONE
                item.firstIV.visibility = View.VISIBLE
            }
        } else {
            item.nofriendIV.visibility = View.GONE
            item.firstIV.visibility = View.GONE
            item.countTV.visibility = View.VISIBLE
            item.countTV.setText("("+peoplecount.toString()+")")
        }

        if (room_member_id.toInt() == PrefUtils.getIntPreference(context,"member_id")){
            item.nofriendIV.visibility = View.GONE
            item.firstIV.visibility = View.GONE
        }

        if (title != null && title.length > 0) {
            item.nickTV.visibility = View.VISIBLE
            item.addNickLL.visibility = View.GONE
//            item.nickTV.setText(title)
            if (title.length > 15){
                var sub = title.substring(0,15)
                item.nickTV.setText(sub + "...")
            } else {
                item.nickTV.setText(title)
            }

        } else {
            item.nickTV.visibility = View.GONE
            item.addNickLL.visibility = View.VISIBLE
//            item.nickTV.setText(nick)
        }

        if (room_type == "1") {
            var image = Config.url + profileimg
            ImageLoader.getInstance().displayImage(image, item.profPhoto, Utils.UILoptionsUserProfile)
            item.countTV.visibility = View.GONE
        } else {
            var image = Config.url + Utils.getString(room, "intro")
            ImageLoader.getInstance().displayImage(image, item.profPhoto, Utils.UILoptionsUserProfile)
            item.nofriendIV.visibility = View.GONE
            item.firstIV.visibility = View.GONE
            item.countTV.visibility = View.VISIBLE
            item.countTV.setText("("+peoplecount.toString()+")")
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
        var firstIV : ImageView
        var timeTV : TextView
        var countTV : TextView
        var chatcontentTV : TextView
        var nofriendIV: ImageView
        var pushoffIV : ImageView
        var pushonIV : ImageView
        var addNickLL: LinearLayout
        var readIV : ImageView

        init {
            profPhoto = v.findViewById<View>(R.id.profPhoto) as CircleImageView
            isOnLine = v.findViewById<View>(R.id.isOnLine) as ImageView
            nickTV = v.findViewById<View>(R.id.nickTV) as TextView
            firstIV = v.findViewById<View>(R.id.firstIV) as ImageView
            timeTV = v.findViewById<View>(R.id.timeTV) as TextView
            countTV = v.findViewById<View>(R.id.countTV) as TextView
            chatcontentTV = v.findViewById<View>(R.id.chatcontentTV) as TextView
            nofriendIV = v.findViewById<View>(R.id.nofriendIV) as ImageView
            pushoffIV = v.findViewById<View>(R.id.pushoffIV) as ImageView
            pushonIV = v.findViewById<View>(R.id.pushonIV) as ImageView
            addNickLL = v.findViewById<View>(R.id.addNickLL) as LinearLayout
            readIV = v.findViewById<View>(R.id.readIV) as ImageView

        }
    }
}
