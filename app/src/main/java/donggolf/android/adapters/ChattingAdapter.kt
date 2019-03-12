package donggolf.android.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.activities.PictureDetailActivity
import donggolf.android.activities.ProfileActivity
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

        val id = Utils.getString(chatting,"id")
        val myId = PrefUtils.getIntPreference(context, "member_id")
        val send_member_id = Utils.getString(chatting,"member_id")
        val content = Utils.getString(chatting,"content")
        val content_image = Utils.getString(chatting, "img")
        val member_nick = Utils.getString(member,"nick")
        val sex = Utils.getString(member,"sex")
        var messageCreated = Utils.getString(chatting,"created")
        var split = messageCreated.split(" ")
        var peoplecount = Utils.getInt(chatting,"peoplecount")
        var read_count = Utils.getInt(chatting,"read_count")
        var difference = peoplecount - read_count
        var type = Utils.getString(chatting,"type")

        item.userprofileIV.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("member_id", send_member_id)
            context.startActivity(intent)
        }

        item.userimageIV.setOnClickListener {
            val type = Utils.getString(chatting,"type")
            println("--------typ[e====== $type")
            if (type == "i"){
                val img = Utils.getString(chatting,"img")
                val imglist:ArrayList<String> = ArrayList<String>()
                imglist.add(img)
                var intent = Intent(context, PictureDetailActivity::class.java)
                intent.putExtra("id", id)
                intent.putExtra("adPosition",0)
                intent.putExtra("paths",imglist)
                intent.putExtra("type","chat")
                context.startActivity(intent)
            }
        }

        item.myimageIV.setOnClickListener {
            val type = Utils.getString(chatting,"type")
            println("--------typ[e====== $type")
            if (type == "i"){
                val img = Utils.getString(chatting,"img")
                val imglist:ArrayList<String> = ArrayList<String>()
                imglist.add(img)
                var intent = Intent(context, PictureDetailActivity::class.java)
                intent.putExtra("id", id)
                intent.putExtra("adPosition",0)
                intent.putExtra("paths",imglist)
                intent.putExtra("type","chat")
                context.startActivity(intent)
            }
        }

        if (sex == "0"){
            item.usernickTV.setTextColor(Color.parseColor("#000000"))
        }

        var text_size = Utils.getString(json,"text_size")
        if (text_size == "1"){
            item.usernickTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12f)
            item.usercontentTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12f)
            item.mycontentTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12f)
        } else if (text_size == "2"){
            item.usernickTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f)
            item.usercontentTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f)
            item.mycontentTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f)
        } else if (text_size == "3"){
            item.usernickTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f)
            item.usercontentTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f)
            item.mycontentTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f)
        } else if (text_size == "4"){
            item.usernickTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20f)
            item.usercontentTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20f)
            item.mycontentTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20f)
        }

        if (difference > 0){
            item.readTV.setText(difference.toString())
        } else {
            item.readTV.setText("")
        }

        if (send_member_id.toInt() == myId) {
            item.myLL.visibility = View.VISIBLE
            item.userLL.visibility = View.GONE
            item.mycontentTV.setText(content)

            if (type == "i"){
                item.myimageIV.visibility = View.VISIBLE
                item.mycontentTV.visibility = View.GONE
                var image = Config.url + content_image
                ImageLoader.getInstance().displayImage(image, item.myimageIV, Utils.UILoptionsUserProfile)
            } else if (type == "c"){
                item.myimageIV.visibility = View.GONE
                item.mycontentTV.visibility = View.VISIBLE
            }  else if (type == "d"){
                item.myLL.visibility = View.GONE
                item.userLL.visibility = View.VISIBLE
                item.usernickTV.visibility = View.GONE
                item.userprofileIV.visibility = View.GONE
                item.usercontentTV.visibility = View.VISIBLE
                item.usercontentTV.setText(content)
            }

        } else {
            item.myLL.visibility = View.GONE
            item.userLL.visibility = View.VISIBLE
            item.usernickTV.setText(member_nick)
            item.usercontentTV.setText(content)
            item.userprofileIV.visibility = View.VISIBLE

            if (type == "i"){
                item.userimageIV.visibility = View.VISIBLE
                item.usercontentTV.visibility = View.GONE
                var image = Config.url + content_image
                ImageLoader.getInstance().displayImage(image, item.userimageIV, Utils.UILoptionsUserProfile)
            } else if (type == "c"){
                item.userimageIV.visibility = View.GONE
                item.usercontentTV.visibility = View.VISIBLE
            } else  if (type == "d"){
                item.usernickTV.visibility = View.GONE
                item.userprofileIV.visibility = View.GONE
                item.usercontentTV.visibility = View.VISIBLE
            }

            val today = Utils.todayStr()

            if (split.get(0) == today){
                var timesplit = split.get(1).split(":")
                var noon = "오전"
                if (timesplit.get(0).toInt() >= 12){
                    noon = "오후"
                }
                var time = noon + " " + timesplit.get(0) + ":" + timesplit.get(1)
                if (difference > 0){
                    time += "(" + difference + ")"
                } else {
                    time += ""
                }

                item.userdateTV.setText(time)
            } else {
                var since = Utils.since(messageCreated)
                if (difference > 0){
                    since += "(" + difference + ")"
                } else {
                    since += ""
                }
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
        var userimageIV :ImageView
        var myimageIV :ImageView

        init {
            myLL = v.findViewById<View>(R.id.myLL) as LinearLayout
            userLL = v.findViewById<View>(R.id.userLL) as LinearLayout
            readTV = v.findViewById<View>(R.id.readTV) as TextView
            mycontentTV = v.findViewById<View>(R.id.mycontentTV) as TextView
            userprofileIV = v.findViewById<View>(R.id.userprofileIV) as CircleImageView
            usernickTV = v.findViewById<View>(R.id.usernickTV) as TextView
            usercontentTV = v.findViewById<View>(R.id.usercontentTV) as TextView
            userdateTV = v.findViewById<View>(R.id.userdateTV) as TextView
            userimageIV = v.findViewById<View>(R.id.userimageIV) as ImageView
            myimageIV = v.findViewById<View>(R.id.myimageIV) as ImageView

        }
    }
}
