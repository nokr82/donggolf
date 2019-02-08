package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import donggolf.android.R
import donggolf.android.activities.FriendSearchActivity
import donggolf.android.base.Utils
import org.json.JSONObject
import java.util.*

class FriendSearchAdapter (context: Context, view: Int, data: ArrayList<JSONObject>, friendSearchActivity: FriendSearchActivity) : ArrayAdapter<JSONObject>(context,view, data){

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<JSONObject> = data
    var friendSearchActivity = friendSearchActivity

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


        var SearchList = json.getJSONObject("MateSearch")

        var searchid = Utils.getString(SearchList,"id")

        var content:String = Utils.getString(SearchList,"keyword")
        item.main_edit_listitem_title.text = content

//        var created = SimpleDateFormat("MM-dd").parse(Utils.getString(json,"created")).toString()

        //val dateFormat = SimpleDateFormat("MM-dd", Locale.KOREA)
        item.main_edit_listitem_date.text = Utils.getString(SearchList,"created")



        item.main_edit_listitem_delete.setOnClickListener {
            friendSearchActivity.deleteSearchList(searchid)
            removeItem(position)
            notifyDataSetChanged()
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


        var main_edit_listitem_title : TextView
        var main_edit_listitem_date : TextView
        var main_edit_listitem_delete : LinearLayout

        init {
            main_edit_listitem_title = v.findViewById<View>(R.id.main_edit_listitem_title) as TextView
            main_edit_listitem_date = v.findViewById<View>(R.id.main_edit_listitem_date) as TextView
            main_edit_listitem_delete = v.findViewById<View>(R.id.main_edit_listitem_delete) as LinearLayout
        }
    }
}