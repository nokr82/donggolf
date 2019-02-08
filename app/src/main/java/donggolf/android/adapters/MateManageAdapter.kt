package donggolf.android.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import donggolf.android.R
import donggolf.android.base.Utils
import org.json.JSONObject
import java.util.ArrayList

class MateManageAdapter(context: Context, view:Int, data: ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data) {

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<JSONObject> = data

//    var isCheckedMate = false

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

        Log.d("wklwkd",json.toString())
        var check = json.getBoolean("check")//임의로 따로 넣어준 변수값

        val member = json.getJSONObject("MateMember")

        item.mates_nickTV.text = Utils.getString(member,"nick")
        item.mates_status_msgTV.text = Utils.getString(member,"status_msg")

        json.put("mate_id", Utils.getInt(member,"id"))

        if (check) {
            item.mates_checkedIV.visibility = View.VISIBLE
        } else  {
            item.mates_checkedIV.visibility = View.INVISIBLE
        }

        item.mates_checkboxRL.setOnClickListener {

            check = !check

            json.put("check", check)

            notifyDataSetChanged()

//            isCheckedMate = !isCheckedMate

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
        var mates_checkedIV : ImageView //체크박스 체크
        var mates_checkboxRL : RelativeLayout

        init {
            mates_profileIV = v.findViewById(R.id.mates_profileIV)
            isOnlineIV = v.findViewById(R.id.isOnlineIV)
            mates_nickTV = v.findViewById(R.id.mates_nickTV)
            mates_status_msgTV = v.findViewById(R.id.mates_status_msgTV)
            mates_checkedIV = v.findViewById(R.id.mates_checkedIV)
            mates_checkboxRL = v.findViewById(R.id.mates_checkboxRL)
        }
    }
}