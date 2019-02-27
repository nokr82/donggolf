package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import donggolf.android.R
import donggolf.android.base.Utils
import donggolf.android.models.FriendCategory
import org.json.JSONObject
import java.util.ArrayList

class FriendCategoryAdapter(context: Context, view:Int, data: ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<JSONObject> = data

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        lateinit var retView: View

        if (convertView == null) {
            retView = View.inflate(context, view, null)
            item = FriendCategoryAdapter.ViewHolder(retView)
            retView.tag = item
        } else {
            retView = convertView
            item = convertView.tag as FriendCategoryAdapter.ViewHolder
            if (item == null) {
                retView = View.inflate(context, view, null)
                item = FriendCategoryAdapter.ViewHolder(retView)
                retView.tag = item
            }
        }

        var json = data.get(position)
        val category = json.getJSONObject("MateCategory")

        json.put("title",Utils.getString(category,"category"))

        item.cateTitleTV.text = Utils.getString(category,"category")
        item.item_category_count.setText(Utils.getString(category,"peoplecnt"))

        if (Utils.getString(category,"peoplecnt") == "0"){
            item.category_del_LL.visibility = View.VISIBLE
            item.item_category_count.visibility = View.GONE
        } else {
            item.category_del_LL.visibility = View.GONE
            item.item_category_count.visibility = View.VISIBLE
        }

        var listFirst = true
        var alarmTxt = ""
        if (Utils.getString(category,"new_post") == "y"){
            alarmTxt += "새 글"
            listFirst = false
        }
        if (Utils.getString(category,"chat") == "y"){
            if (listFirst){
                alarmTxt += "채팅"
                listFirst = false
            }else{
                alarmTxt += "/채팅"
            }
            item.pushTV.visibility = View.VISIBLE
        } else {
            item.pushTV.visibility = View.GONE
        }
        if (Utils.getString(category,"login") == "y"){
            if (listFirst){
                alarmTxt += "로그인"
                listFirst = false
            }else{
                alarmTxt += "/로그인"
            }

        }
        if (Utils.getString(category,"open_mate") == "y"){
            if (listFirst){
                alarmTxt += "친구공개"
                listFirst = false
            }else{
                alarmTxt += "/친구공개"
            }
        }
        item.alarmListTV.text = alarmTxt



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

    class ViewHolder(v: View) {

        var cateTitleTV : TextView
        var alarmListTV : TextView
        var item_category_count : TextView
        var btn_category_del : ImageView
        var pushTV : TextView
        var category_del_LL : LinearLayout


        init {

            cateTitleTV = v.findViewById(R.id.cateTitleTV)
            alarmListTV = v.findViewById(R.id.alarmListTV)
            item_category_count = v.findViewById(R.id.item_category_count)
            btn_category_del = v.findViewById(R.id.btn_category_del)
            pushTV = v.findViewById(R.id.pushTV)
            category_del_LL = v.findViewById(R.id.category_del_LL)

        }


    }

}