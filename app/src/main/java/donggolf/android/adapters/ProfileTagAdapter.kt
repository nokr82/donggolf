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


open class ProfileTagAdapter(context: Context, view:Int, data:ArrayList<String>) : ArrayAdapter<String>(context,view, data){

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<String> = data

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

        //var data : String = getItem(position)
        var tmp = data.get(position)

        item.titleTagTV.text = tmp
        println("ProfileTagAdapter()=====$tmp")

        item.removeIV.setOnClickListener {
            removeItem(position)
        }

        return retView
    }

    override fun getItem(position: Int): String {

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


        var titleTagTV : TextView
        var removeIV : ImageView

        init {
            titleTagTV = v.findViewById<View>(R.id.titleTagTV) as TextView
            removeIV = v.findViewById<View>(R.id.removeIV) as ImageView


        }
    }
}
