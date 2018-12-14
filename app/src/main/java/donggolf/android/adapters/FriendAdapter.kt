package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
//import com.kakao.usermgmt.StringSet.nickname
import donggolf.android.R
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

        //var json = data.get(position)
        //        var content = json.getJSONObject("Content")
        //
        //        val member_id = Utils.getString(content,"member_id")
        //        val title = Utils.getString(content,"title")
        //        val text = Utils.getString(content,"text")
        //        val created = Utils.getString(content, "created").substringBefore(" ")

        val json = data.get(position)
        val member = json.getJSONObject("Member")

        item.main_detail_listitem_nickname.text = Utils.getString(member, "nick")
        item.main_detail_listitem_condition.text = Utils.getString(member, "status_msg")

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


        var main_detail_listitem_profileimage : ImageView //프사
        var isOnlineIV : ImageView //프사에 붙은 초록불
        var main_detail_listitem_nickname : TextView //닉네임
        var main_detail_listitem_firstimage : ImageView //일촌인지 이촌인지
        var main_detail_listitem_condition : TextView //상메

        init {
            main_detail_listitem_profileimage = v.findViewById<View>(R.id.main_detail_listitem_profileimage) as ImageView
            isOnlineIV = v.findViewById<View>(R.id.isOnlineIV) as ImageView
            main_detail_listitem_nickname = v.findViewById<View>(R.id.main_detail_listitem_nickname) as TextView
            main_detail_listitem_firstimage = v.findViewById<View>(R.id.main_detail_listitem_firstimage) as ImageView
            main_detail_listitem_condition = v.findViewById<View>(R.id.main_detail_listitem_condition) as TextView
        }
    }
}