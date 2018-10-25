package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.base.Utils
import org.json.JSONObject


open class MarketMainAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

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


        var divisionTV : TextView
        var imageIV : ImageView
        var contentTV : TextView
        var nicknameTV : TextView
        var timeTV: TextView
        var priceTV: TextView
        var moreTV: TextView

        init {
            divisionTV = v.findViewById<View>(R.id.divisionTV) as TextView
            imageIV = v.findViewById<View>(R.id.imageIV) as ImageView
            contentTV = v.findViewById<View>(R.id.contentTV) as TextView
            nicknameTV = v.findViewById<View>(R.id.nicknameTV) as TextView
            timeTV = v.findViewById<View>(R.id.timeTV) as TextView
            priceTV = v.findViewById<View>(R.id.priceTV) as TextView
            moreTV = v.findViewById<View>(R.id.moreTV) as TextView
        }
    }
}