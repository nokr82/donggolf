package donggolf.android.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.base.Config
import donggolf.android.base.ElapsedTimeTextView
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import donggolf.android.models.MutualFriendData
import org.json.JSONObject
import kotlin.collections.ArrayList

class EventAdapter(context: Context, view : Int, data: ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data) {

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

        var json= data.get(position)

        val finish = Utils.getString(json, "finish")

        if (finish == "Y") {
            item.deadlineTV.setTextColor(Color.parseColor("#707070"))
            item.deadlineTV.text = "마감"
        } else {

            val leftDays = Utils.getInt(json , "leftDays")

            if (leftDays < 1) {

                item.deadlineTV.dest_date_time = Utils.getInt(json, "timer")
                item.deadlineTV.start()

            } else {
                item.deadlineTV.text = "추첨 : ${leftDays}일전"
            }

            item.deadlineTV.setTextColor(Color.parseColor("#EF5C34"))
        }

        val event = json.getJSONObject("Event")

        val title = Utils.getString(event, "title")
        val contents = Utils.getString(event, "contents")

        if (Utils.getString(json, "participation_yn") == "Y") {
            item.stateTV.text = "추첨번호 : " + Utils.getString(json, "number")
        } else {
            item.stateTV.text = "참여하기"
        }

        item.titleTV.text = title
        item.contentsTV.text = contents

        ImageLoader.getInstance().displayImage(Config.url + Utils.getString(event, "image_uri"), item.imageIV, Utils.UILoptions)

        return retView
    }

    override fun getItem(position: Int): JSONObject? {

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
        var contentsTV : TextView
        var deadlineTV : ElapsedTimeTextView
        var stateTV : TextView
        var imageIV : ImageView

        init {
            titleTV = v.findViewById(R.id.titleTV) as TextView
            contentsTV = v.findViewById(R.id.contentsTV) as TextView
            deadlineTV = v.findViewById(R.id.deadlineTV) as ElapsedTimeTextView
            stateTV = v.findViewById(R.id.stateTV) as TextView
            imageIV = v.findViewById(R.id.imageIV) as ImageView
        }
    }
}