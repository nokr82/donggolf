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


open class MainDeatilAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

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

        var data : JSONObject = getItem(position)



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


        var main_detail_listitem_comment : ImageView
        var main_detail_listitem_profileimage : ImageView
        var main_detail_listitem_nickname : TextView
        var main_detail_listitem_firstimage : ImageView
        var main_detail_listitem_condition : TextView
        var main_detail_listitem_date : TextView



        init {
            main_detail_listitem_comment = v.findViewById<View>(R.id.main_detail_listitem_comment) as ImageView
            main_detail_listitem_profileimage = v.findViewById<View>(R.id.main_detail_listitem_profileimage) as ImageView
            main_detail_listitem_nickname = v.findViewById<View>(R.id.main_detail_listitem_nickname) as TextView
            main_detail_listitem_firstimage = v.findViewById<View>(R.id.main_detail_listitem_firstimage) as ImageView
            main_detail_listitem_condition = v.findViewById<View>(R.id.main_detail_listitem_condition) as TextView
            main_detail_listitem_date = v.findViewById<View>(R.id.main_detail_listitem_date) as TextView


        }
    }
}