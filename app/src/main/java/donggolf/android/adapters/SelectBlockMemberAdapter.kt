package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.nostra13.universalimageloader.core.ImageLoader
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.R.id.main_edit_listitem_title
import donggolf.android.actions.MarketAction
import donggolf.android.actions.PostAction
import donggolf.android.actions.SearchAction
import donggolf.android.base.Config
import donggolf.android.base.Utils
import donggolf.android.fragment.FreeFragment
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


open class SelectBlockMemberAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

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

        var member = json.getJSONObject("Member")
        var Chatmember = json.getJSONObject("Chatmember")

        var block_yn = Utils.getString(Chatmember,"block_yn")
        var nick:String = Utils.getString(member,"nick")


        item.friend_name.text = nick

        var isSel = json.getBoolean("isSelectedOp")


        if (block_yn == "Y"){
            item.check_add_mem.visibility = View.VISIBLE
            item.check_sel_mem.visibility = View.GONE
        } else {
            item.check_sel_mem.visibility = View.VISIBLE
            item.check_add_mem.visibility = View.GONE
        }

        if (isSel){
            item.check_add_mem.visibility = View.VISIBLE
            item.check_sel_mem.visibility = View.GONE
        } else {
            item.check_sel_mem.visibility = View.VISIBLE
            item.check_add_mem.visibility = View.GONE
        }


        var image = Config.url + Utils.getString(member, "profile_img")
        ImageLoader.getInstance().displayImage(image, item.friends_profile, Utils.UILoptionsUserProfile)

//        item.check_sel_mem.setOnClickListener {
//            isSel = !isSel
//
//            json.put("check", isSel)
//
//            notifyDataSetChanged()
//        }

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

        var check_sel_mem : ImageView
        var check_add_mem : ImageView
        var friends_profile : de.hdodenhof.circleimageview.CircleImageView
        var friend_name : TextView
        var relation : ImageView

        init {
            check_sel_mem = v.findViewById<View>(R.id.check_sel_mem) as ImageView
            check_add_mem = v.findViewById<View>(R.id.check_add_mem) as ImageView
            friends_profile = v.findViewById<View>(R.id.friends_profile) as de.hdodenhof.circleimageview.CircleImageView
            friend_name = v.findViewById<View>(R.id.friend_name) as TextView
            relation = v.findViewById<View>(R.id.relation) as ImageView
        }
    }
}