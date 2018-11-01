package donggolf.android.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.base.Utils
import donggolf.android.models.Content
import org.json.JSONObject
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*

open class MainAdapter(context: Context, view:Int, data:ArrayList<Map<String, Any>>) : ArrayAdapter<Map<String, Any>>(context,view, data){

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<Map<String, Any>> = data

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

        var data : Map<String, Any> = getItem(position)

        println("data : $data")

        var title:String = data.get("title") as String
        item.main_item_title.text = title

        var texts:String = data.get("texts") as String
        item.main_item_content.text = texts

        var owner:String = data.get("owner").toString()
        item.main_item_nickname.text = owner

        var looker:Long = data.get("looker") as Long
        item.main_item_view_count.text = looker.toString()

        val time: Long = data.get("createAt")as Long

        val now: Long = System.currentTimeMillis()

        val t1: Timestamp = Timestamp(time)

        val t2: Timestamp = Timestamp(now)

        val gap = now-time

        val min: Long = (gap / 1000) / 60

        val sec: Long = (gap / 1000) % 60

        val hour: Long = (gap / 1000) / 60 / 60

        val day: Long = hour / 24



        if(min >= 60){
            if(hour > 24){
                item.main_item_lately.text = day.toString() + " 일 전"
            }else {
                item.main_item_lately.text = hour.toString() + " 시간 전"
            }
        }else {
            item.main_item_lately.text = min.toString() + " 분 전"
        }





        return retView
    }

    override fun getItem(position: Int): Map<String, Any> {

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

        var main_item_title : TextView
        var main_item_content : TextView

        var main_item_nickname : TextView
        var main_item_lately : TextView
        var main_item_view_count : TextView
        var main_item_comment_count : TextView
        var main_item_like_count : TextView

        init {

            main_item_title = v.findViewById<View>(R.id.main_item_title) as TextView
            main_item_content = v.findViewById<View>(R.id.main_item_content) as TextView
            main_item_nickname = v.findViewById<View>(R.id.main_item_nickname) as TextView
            main_item_lately = v.findViewById<View>(R.id.main_item_lately) as TextView
            main_item_view_count = v.findViewById<View>(R.id.main_item_view_count) as TextView
            main_item_comment_count = v.findViewById<View>(R.id.main_item_comment_count) as TextView
            main_item_like_count = v.findViewById<View>(R.id.main_item_like_count) as TextView


        }


    }





}