package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.actions.SearchAction
import donggolf.android.base.Utils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


open class MainEditAdapter(context: Context, view:Int, data:ArrayList<Map<String, Any>>) : ArrayAdapter<Map<String, Any>>(context,view, data){

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<Map<String, Any>> = data

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

        var data : Map<String, Any> = getItem(position)

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
        }

        return retView
    }

    override fun getItem(position: Int): Map<String, Any> {

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
        var main_edit_listitem_delete : ImageView

        init {
            main_edit_listitem_title = v.findViewById<View>(R.id.main_edit_listitem_title) as TextView
            main_edit_listitem_date = v.findViewById<View>(R.id.main_edit_listitem_date) as TextView
            main_edit_listitem_delete = v.findViewById<View>(R.id.main_edit_listitem_delete) as ImageView
        }
    }
}