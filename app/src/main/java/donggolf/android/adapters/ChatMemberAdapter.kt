package donggolf.android.adapters

import android.content.Context
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
import donggolf.android.models.MutualFriendData
import org.json.JSONObject
import kotlin.collections.ArrayList

class ChatMemberAdapter(context: Context, view : Int, data: ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data) {

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

        var json= data.get(position)

        var room = json.getJSONObject("Chatroom")
        val founder = Utils.getString(room,"member_id")

        var member = json.getJSONObject("Member")
        var nick:String = Utils.getString(member,"nick")
        var member_id = Utils.getString(member,"id")

        var image = Config.url + Utils.getString(member, "profile_img")
        ImageLoader.getInstance().displayImage(image, item.item_profileImg, Utils.UILoptionsUserProfile)

        item.item_nickNameTV.setText(nick)

        if (founder == member_id){
            item.item_authorizationTV.setText("개설자")
        } else {
            item.item_authorizationTV.setText("")
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
        var item_authorizationTV : TextView

        init {
            item_profileImg = v.findViewById(R.id.item_profileImg) as CircleImageView
            item_nickNameTV = v.findViewById(R.id.item_nickNameTV) as TextView
            item_relationIV = v.findViewById(R.id.item_relationIV) as ImageView
            item_authorizationTV = v.findViewById(R.id.item_authorizationTV) as TextView
        }
    }
}