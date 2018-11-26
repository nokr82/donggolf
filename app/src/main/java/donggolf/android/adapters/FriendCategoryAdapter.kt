package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.models.FriendCategory
import java.util.ArrayList

class FriendCategoryAdapter(context: Context, view:Int, data: ArrayList<FriendCategory>) : BaseAdapter() {

    private lateinit var item: FriendCategoryAdapter.ViewHolder
    var view:Int = view
    var data:ArrayList<FriendCategory> = data
    val context = context

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

        var tmpData = data.get(position)

        item.cateTitleTV.setText(tmpData.cateTitle)
        item.item_category_count.setText(tmpData.memberCount)
        var listFirst = true
        var alarmTxt = ""
        if (tmpData.newPost){
            alarmTxt += "새 글"
            listFirst = false
        }
        if (tmpData.newChat){
            if (listFirst){
                alarmTxt += "채팅"
                listFirst = false
            }else{
                alarmTxt += "/채팅"
            }
        }
        if (tmpData.newLogin){
            if (listFirst){
                alarmTxt += "로그인"
                listFirst = false
            }else{
                alarmTxt += "/로그인"
            }

        }
        if (tmpData.newFrdPub){
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

    override fun getItem(position: Int): Any {
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

        init {

            cateTitleTV = v.findViewById(R.id.cateTitleTV)
            alarmListTV = v.findViewById(R.id.alarmListTV)
            item_category_count = v.findViewById(R.id.item_category_count)
            btn_category_del = v.findViewById(R.id.btn_category_del)

        }


    }

}