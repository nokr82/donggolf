package donggolf.android.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import donggolf.android.R
import donggolf.android.actions.MemberAction
import donggolf.android.actions.PostAction
import donggolf.android.base.Utils
import donggolf.android.models.Content
import donggolf.android.models.Photo
import donggolf.android.models.Text
import donggolf.android.models.Video
import kotlinx.android.synthetic.main.activity_profile_manage.*
import org.json.JSONArray
import org.json.JSONObject
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

open class ReportAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<JSONObject> = data

    var text:Text = Text()
    var photo:JSONArray = JSONArray()
    var video:ArrayList<String> = ArrayList<String>()

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
        var list = json.getJSONObject("MarketReport")
        var member = json.getJSONObject("Member")

        val created = Utils.getString(list,"created")
        val content = Utils.getString(list,"content")
        val nick = Utils.getString(member,"nick")

        var tmpnick = ""

        for (i in 0 until nick.length){
            tmpnick += "*"
        }

        item.nameTV.text = "신고자 " + tmpnick
        item.dateTV.text = created
        item.contentTV.text = content

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

        var nameTV : TextView
        var dateTV : TextView
        var contentTV : TextView

        init {

            nameTV = v.findViewById<View>(R.id.nameTV) as TextView
            dateTV = v.findViewById<View>(R.id.dateTV) as TextView
            contentTV = v.findViewById<View>(R.id.contentTV) as TextView

        }


    }





}