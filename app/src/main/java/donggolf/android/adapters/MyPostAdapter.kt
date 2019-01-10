package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.base.Utils
import donggolf.android.models.Text
import org.json.JSONArray
import org.json.JSONObject

class MyPostAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data) {
    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<JSONObject> = data

    var text: Text = Text()
    var photo: JSONArray = JSONArray()

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
        var content = json.getJSONObject("Content")

        json.put("cmt_wrt_id", Utils.getInt(content,""))
        var content_id =  Utils.getString(content,"id")
        val member_id = Utils.getString(content,"member_id")
        val title = Utils.getString(content,"title")
        val text = Utils.getString(content,"text")
        val created = Utils.getString(content, "created").substringBefore(" ")

        item.myPostTitleTV.text = title.toString()
        item.myPostContTV.text = text.toString()
        item.writeDateTV.text = created

        var wd = json.getBoolean("willDel")
        if (wd) {
            item.item_btn_del.visibility = View.VISIBLE
            notifyDataSetChanged()
        } else {
            item.item_btn_del.visibility = View.GONE
            notifyDataSetChanged()
        }

        val member = json.getJSONObject("Member")
        val nick = Utils.getString(member, "nick")
        item.myPostItem_nick.text = nick

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

        var myPostTitleTV : TextView
        var myPostContTV : TextView

        var myPostItem_profile : ImageView
        var myPostItem_nick : TextView
        var writeDateTV : TextView
        var mpCommentTV : TextView

        var item_btn_del : ImageView
        var itemMyPostMng_postImg :ImageView

        init {

            myPostTitleTV = v.findViewById<View>(R.id.myPostTitleTV) as TextView
            myPostContTV = v.findViewById<View>(R.id.myPostContTV) as TextView
            myPostItem_profile = v.findViewById<View>(R.id.myPostItem_profile) as ImageView
            myPostItem_nick = v.findViewById<View>(R.id.myPostItem_nick) as TextView
            writeDateTV = v.findViewById<View>(R.id.writeDateTV) as TextView
            mpCommentTV = v.findViewById<View>(R.id.mpCommentTV) as TextView
            item_btn_del = v.findViewById<View>(R.id.item_btn_del) as ImageView//item_btn_del
            itemMyPostMng_postImg = v.findViewById<View>(R.id.itemMyPostMng_postImg) as ImageView

        }


    }
}