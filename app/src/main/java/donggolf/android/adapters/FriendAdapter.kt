package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
//import com.kakao.usermgmt.StringSet.nickname
import donggolf.android.R
import donggolf.android.models.Users
import java.net.UnknownServiceException
import java.util.*

class FriendAdapter(context: Context, view:Int, data: ArrayList<Users>) : ArrayAdapter<Users>(context,view, data) {
    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<Users> = data

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

        item.main_detail_listitem_nickname.setText(data.get(position).nick)
        item.main_detail_listitem_condition.setText(data.get(position).state_msg)
        /*if (data.get(position).get("relation").toString().equals("mate1")){
            item.main_detail_listitem_firstimage.setImageResource(R.drawable.icon_first)
        }else{
            item.main_detail_listitem_firstimage.setImageResource(R.drawable.icon_second)
        }*/

        /*var data : Map<String, Any> = getItem(position)

        var title:String = data.get("content") as String
        item.main_edit_listitem_title.text = title

        var date:Long = data.get("date") as Long
        val dateFormat: SimpleDateFormat = SimpleDateFormat("MM-dd", Locale.KOREA)
        val currentTime: String = dateFormat.format(Date(date))
        item.main_edit_listitem_date.text = currentTime

        item.main_edit_listitem_delete.setOnClickListener {

            var id:String = data.get("id") as String
            SearchAction.deleteContent(id){
                if(it){
                    removeItem(position)
                }
            }
        }*/

        return retView
    }

    override fun getItem(position: Int): Users {

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