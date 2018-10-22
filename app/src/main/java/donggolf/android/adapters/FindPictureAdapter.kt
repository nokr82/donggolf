package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.base.Utils
import donggolf.android.models.PictureCategory
import org.json.JSONObject


open class FindPictureAdapter(context: Context, view:Int, data:ArrayList<PictureCategory>) : ArrayAdapter<PictureCategory>(context,view, data){

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<PictureCategory> = data

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

        var data : PictureCategory = getItem(position)

        item.findpicture_item_name.text = data.title

        item.findpicture_item_count.text = data.count.toString()

        return retView
    }

    override fun getItem(position: Int): PictureCategory {

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

        var findpicture_item_image : ImageView
        var findpicture_item_name : TextView
        var findpicture_item_count : TextView


        init {
            findpicture_item_image = v.findViewById<View>(R.id.findpicture_item_image) as ImageView
            findpicture_item_name = v.findViewById<View>(R.id.findpicture_item_name) as TextView
            findpicture_item_count = v.findViewById<View>(R.id.findpicture_item_count) as TextView

        }
    }
}