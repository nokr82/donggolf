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
//import com.kakao.usermgmt.StringSet.nickname
import donggolf.android.R
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import donggolf.android.models.Users
import org.json.JSONObject
import java.net.UnknownServiceException
import java.util.*

class FriendAdapter(context: Context, view:Int, data: ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data) {
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

        val json = data.get(position)
        val member = json.getJSONObject("Member")
        var member_id = Utils.getInt(member,"id")
        val friend = Utils.getString(member,"friend")
        val mate_cnt = Utils.getString(member,"mate_cnt")

        val sex = Utils.getString(member,"sex")

        if (member_id == PrefUtils.getIntPreference(context, "member_id")) {
            item.item_relationIV.visibility = View.GONE
        } else {
            item.item_relationIV.visibility = View.VISIBLE
            if(friend == "0") {
//                item.item_relationIV.setImageResource(R.drawable.icon_second)
                if (mate_cnt!="0"){
                    item.item_relationIV.setImageResource(R.drawable.icon_second)
                }else{
                    item.item_relationIV.visibility = View.GONE
                }
            } else {
                item.item_relationIV.setImageResource(R.drawable.icon_first)
            }
        }

        if (sex == "1"){
            item.item_nickNameTV.setTextColor(Color.parseColor("#EF5C34"))
        }else{
            item.item_nickNameTV.setTextColor(Color.parseColor("#000000"))
        }

        item.item_nickNameTV.text = Utils.getString(member, "nick")

        val img_uri = Utils.getString(member,"profile_img")//small_uri
        val image = Config.url + img_uri
        ImageLoader.getInstance().displayImage(image, item.item_profileImg, Utils.UILoptionsProfile)

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

        var item_profileImg : CircleImageView
        var item_nickNameTV : TextView
        var item_relationIV : ImageView
        var item_authorizationTV : TextView

        init {
            item_profileImg = v.findViewById(R.id.item_profileImg) as CircleImageView
            item_nickNameTV = v.findViewById(R.id.item_nickNameTV) as TextView
            item_relationIV = v.findViewById(R.id.item_relationIV) as ImageView
            item_authorizationTV = v.findViewById(R.id.item_authorizationTV) as TextView
        }
    }
}