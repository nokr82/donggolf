package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.R.id.main_edit_listitem_title
import donggolf.android.actions.PostAction
import donggolf.android.actions.SearchAction
import donggolf.android.base.Utils
import donggolf.android.fragment.FreeFragment
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


open class ProductTypeAdaapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

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
        val category = json.getJSONObject("ProductType")

        var title:String = Utils.getString(category,"title")
        item.item_option_nameTV.text = title

        var isSel = json.getBoolean("isSelectedOp")
        if (isSel){
            item.item_option_checkIV.visibility = View.VISIBLE
        } else {
            item.item_option_checkIV.visibility = View.INVISIBLE
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


        var item_option_nameTV : TextView
        var item_option_checkIV : ImageView

        init {
            item_option_nameTV = v.findViewById<View>(R.id.item_option_nameTV) as TextView
            item_option_checkIV = v.findViewById<View>(R.id.item_option_checkIV) as ImageView
        }
    }
}