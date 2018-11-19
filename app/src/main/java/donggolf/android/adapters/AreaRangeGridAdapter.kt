package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import donggolf.android.R
import donggolf.android.models.National
import donggolf.android.models.NationalGrid
import org.json.JSONObject


open class AreaRangeGridAdapter(context: Context, view:Int, data:ArrayList<NationalGrid>) : ArrayAdapter<NationalGrid>(context,view, data){

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<NationalGrid> = data

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

        var data : NationalGrid = getItem(position)

        item.titleTV.setText(data.title)

        item.checkCB.isChecked = true



        return retView
    }

    override fun getItem(position: Int): NationalGrid {

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


        var titleTV : TextView
        var checkCB : CheckBox

        init {
            titleTV = v.findViewById<View>(R.id.titleTV) as TextView
            checkCB = v.findViewById<View>(R.id.checkCB) as CheckBox
        }
    }
}