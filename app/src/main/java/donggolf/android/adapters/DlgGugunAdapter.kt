package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.base.Utils
import donggolf.android.models.MutualFriendData
import org.json.JSONObject

class DlgGugunAdapter(context: Context, view: Int, data: ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data) {
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

        var json = data.get(position)

        var type = json.getJSONObject("Regions")

        var name:String = Utils.getString(type,"name")
        item.itemTV.text = name

        var isSel = json.getBoolean("isSelectedOp")
        if (isSel){
            item.itemRdoon.visibility = View.VISIBLE
            item.itemRdooff.visibility = View.GONE
        } else {
            item.itemRdooff.visibility = View.VISIBLE
            item.itemRdoon.visibility = View.GONE
        }

        //item.item_relationIV.setImageResource(R.drawable.icon_second)

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

        var itemTV : TextView
        var itemRdoon : ImageView
        var itemRdooff : ImageView
        init {
            itemTV = v.findViewById(R.id.itemTV)
            itemRdoon = v.findViewById(R.id.itemRdoon)
            itemRdooff = v.findViewById(R.id.itemRdooff)
        }
    }
}