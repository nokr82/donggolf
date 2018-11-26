package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.models.MutualFriendData

class DlgRegionAdapter(context: Context, view: Int, data: ArrayList<Map<String,Boolean>>) : ArrayAdapter<Map<String,Boolean>>(context,view, data) {
    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<Map<String,Boolean>> = data

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

        var tmpData : Map<String,Boolean> = data.get(position)

        //item.item_profileImg.setImageResource(tmpData.profileImg)
        item.itemTV.setText(tmpData.keys.toString())
        if (tmpData.getValue(tmpData.keys.toString()) == true){
            item.itemRdo.setImageResource(R.drawable.btn_radio_on)
        }else{
            item.itemRdo.setImageResource(R.drawable.btn_radio_off)
        }


        //item.item_relationIV.setImageResource(R.drawable.icon_second)

        return retView
    }

    override fun getItem(position: Int): Map<String,Boolean> {

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
        var itemRdo : ImageView
        init {
            itemTV = v.findViewById(R.id.itemTV)
            itemRdo = v.findViewById(R.id.itemRdo)
        }
    }
}