package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.base.Utils
import org.json.JSONObject
import java.util.ArrayList

class MateManageAdapter(context: Context, view:Int, data: ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data) {

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<JSONObject> = data

    var isCheckedMate = false

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

        item.mates_nickTV.text = Utils.getString(member,"nick")
        item.mates_status_msgTV.text = Utils.getString(member,"status_msg")

        if (isCheckedMate) {
            item.mates_checkedIV.visibility = View.VISIBLE
        } else {
            item.mates_checkedIV.visibility = View.INVISIBLE
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

        var mates_profileIV : ImageView //프사
        var isOnlineIV : ImageView //프사에 붙은 초록불
        var mates_nickTV : TextView //닉네임
        var mates_status_msgTV : TextView //상메
        var mates_checkedIV : ImageView //체크박스

        init {
            mates_profileIV = v.findViewById<View>(R.id.mates_profileIV) as ImageView
            isOnlineIV = v.findViewById<View>(R.id.isOnlineIV) as ImageView
            mates_nickTV = v.findViewById<View>(R.id.mates_nickTV) as TextView
            mates_status_msgTV = v.findViewById<View>(R.id.mates_status_msgTV) as TextView
            mates_checkedIV = v.findViewById<View>(R.id.mates_checkedIV) as ImageView
        }
    }
}