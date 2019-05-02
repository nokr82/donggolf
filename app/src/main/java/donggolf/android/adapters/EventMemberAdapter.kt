package donggolf.android.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.activities.EventMembersActivity
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import donggolf.android.models.MutualFriendData
import org.json.JSONObject
import kotlin.collections.ArrayList

class EventMemberAdapter(context: Context, view : Int, data: ArrayList<JSONObject>, activity: EventMembersActivity) : ArrayAdapter<JSONObject>(context,view, data) {

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<JSONObject> = data
    var activity:EventMembersActivity = activity

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

        var json= data.get(position)

        var eventMember = json.getJSONObject("EventMember")

        val freind = Utils.getString(eventMember, "freind")

        var member = json.getJSONObject("Member")

        var nick = Utils.getString(member,"nick")
        var member_id = Utils.getInt(member,"id")
        var image = Config.url + Utils.getString(member, "profile_img")

        ImageLoader.getInstance().displayImage(image, item.item_profileImg, Utils.UILoptionsUserProfile)
        item.item_nickNameTV.setText(nick)

        var gender = Utils.getString(member, "sex")

        if (gender == "0") {
            item.item_nickNameTV.setTextColor(Color.parseColor("#000000"))
        }

        if (member_id == PrefUtils.getIntPreference(context, "member_id")) {
            item.item_relationIV.visibility = View.GONE
        } else {
            item.item_relationIV.visibility = View.VISIBLE
            if(freind == "0") {
//                item.item_relationIV.setImageResource(R.drawable.icon_second)
                item.item_relationIV.visibility = View.GONE
            } else {
                item.item_relationIV.setImageResource(R.drawable.icon_first)
            }
        }

        item.numberTV.text = "추첨번호 " + Utils.getString(eventMember, "number1") + Utils.getString(eventMember, "number2")

        if (activity.finish == "Y") {
            item.gradeTV.visibility = View.VISIBLE
            item.gradeTV.text = "${Utils.getInt(eventMember, "grade")}등"
        } else {
            item.gradeTV.visibility = View.GONE
        }

        return retView
    }

    override fun getItem(position: Int): JSONObject? {

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

        var item_profileImg : CircleImageView
        var item_nickNameTV : TextView
        var item_relationIV : ImageView
        var numberTV : TextView
        var gradeTV : TextView

        init {
            item_profileImg = v.findViewById(R.id.item_profileImg) as CircleImageView
            item_nickNameTV = v.findViewById(R.id.item_nickNameTV) as TextView
            item_relationIV = v.findViewById(R.id.item_relationIV) as ImageView
            numberTV = v.findViewById(R.id.numberTV) as TextView
            gradeTV = v.findViewById(R.id.gradeTV) as TextView
        }
    }
}