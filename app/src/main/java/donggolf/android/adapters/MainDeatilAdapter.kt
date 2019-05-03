package donggolf.android.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.activities.PictureDetailActivity
import donggolf.android.activities.ProfileActivity
import donggolf.android.base.Config
import donggolf.android.base.PrefUtils
import donggolf.android.base.Utils
import org.json.JSONObject


open class MainDeatilAdapter(context: Context, view: Int, data: ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context, view, data) {

    private lateinit var item: ViewHolder
    var view: Int = view
    var data: ArrayList<JSONObject> = data

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

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

        var data = data.get(position)

        //val member_info = data.getJSONObject("Member")
        val comment = data.getJSONObject("ContentComment")
        var image_uri = Utils.getString(comment, "image_uri")
        if (image_uri != "" && image_uri != null) {
            item.itemimageIV.visibility = View.VISIBLE
            var image = Config.url + image_uri
            ImageLoader.getInstance().displayImage(image, item.itemimageIV, Utils.UILoptionsUserProfile)
        } else {
            item.itemimageIV.visibility = View.GONE
        }


        var comment_id = Utils.getInt(comment, "id")
        data.put("comment_id", comment_id)

        val created = Utils.getString(comment, "created")

        item.main_detail_comment_conditionTV.text = Utils.getString(comment, "comment")
        data.put("comment_content", Utils.getString(comment, "comment"))
        item.main_detail_comment_nicknameTV.text = Utils.getString(comment, "nick")
//        item.main_detail_comment_dateTV.text = created
        item.main_detail_comment_typeIV.visibility = View.GONE
        val today = Utils.todayStr()
        if (created != null && created.length > 0){
            var split = created.split(" ")

            if (split != null && split.size > 0) {

                if (split.get(0) == today){
                    var timesplit = split.get(1).split(":")
                    if (timesplit.get(0).toInt() >= 12){
                    }
                    var time =  timesplit.get(0) + ":" + timesplit.get(1)
                    item.main_detail_comment_dateTV.setText(time)

                } else {
                    val since = Utils.since2(created)
                    item.main_detail_comment_dateTV.setText(since)
                }
            }
        }

        var p_comments_id = Utils.getInt(comment, "p_comments_id")
        var op_comments_id = Utils.getInt(comment, "op_comments_id")



        if (p_comments_id != -1) {
            if (op_comments_id != -1) {
                item.main_detail_comment_typeIV.visibility = View.VISIBLE
                item.main_detail_comment_typeIV.setImageResource(R.drawable.icon_comment2)

            } else {
                item.main_detail_comment_typeIV.visibility = View.VISIBLE
                item.main_detail_comment_typeIV.setImageResource(R.drawable.icon_comment1)
            }
        } else if (op_comments_id != -1) {
            item.main_detail_comment_typeIV.visibility = View.VISIBLE
            item.main_detail_comment_typeIV.setImageResource(R.drawable.icon_comment2)

        } else {
            item.main_detail_comment_typeIV.visibility = View.GONE
        }


        val freind = Utils.getString(comment, "freind")
        val member_id = Utils.getInt(comment, "member_id")
        //println("-=------freind : $freind")

        if (member_id == PrefUtils.getIntPreference(context, "member_id")) {
            item.main_detail_comment_relationIV.visibility = View.GONE
        } else {
            item.main_detail_comment_relationIV.visibility = View.VISIBLE
            if(freind == "0") {
                item.main_detail_comment_relationIV.visibility = View.GONE
            } else {
                item.main_detail_comment_relationIV.setImageResource(R.drawable.icon_first)
            }
        }


        val Member = data.getJSONObject("Member")
        val profile_img = Utils.getString(Member, "profile_img")
        if (profile_img != null) {
            var image = Config.url + profile_img
            ImageLoader.getInstance().displayImage(image, item.main_detail_comment_profileIV, Utils.UILoptionsUserProfile)
        }

        var gender = Utils.getString(Member, "sex")
        if (gender == "0") {
            item.main_detail_comment_nicknameTV.setTextColor(Color.parseColor("#000000"))
        }

        item.itemimageIV.setOnClickListener {
            val imglist: ArrayList<String> = ArrayList<String>()
            imglist.add(image_uri)
            var intent = Intent(context, PictureDetailActivity::class.java)
            intent.putExtra("id", "2")
            intent.putExtra("adPosition", 0)
            intent.putExtra("paths", imglist)
            intent.putExtra("type", "chat")
            context.startActivity(intent)
        }


        item.main_detail_comment_profileIV.setOnClickListener {

            val member_id = Utils.getString(Member, "id")
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("member_id", member_id)
            context.startActivity(intent)
        }
        var comment_memberID = Utils.getInt(comment, "member_id")
        data.put("cmt_wrt_id", comment_memberID)

        var isBlocked = Utils.getString(comment, "block_yn")

        /*var cbyn = data.getString("changedBlockYN")
        if (cbyn == "Y"){
            isBlocked = data.getString("block_yn")
        }*/

        if (isBlocked == "Y") {
            item.main_detail_comment_relationIV.setImageResource(R.drawable.icon_block)
            notifyDataSetChanged()
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

    fun removeItem(position: Int) {
        data.removeAt(position)
        notifyDataSetChanged()
    }

    class ViewHolder(v: View) {

        var main_detail_comment_typeIV: ImageView
        var main_detail_comment_profileIV: CircleImageView
        var main_detail_comment_nicknameTV: TextView
        var main_detail_comment_relationIV: ImageView
        var main_detail_comment_conditionTV: TextView
        var main_detail_comment_dateTV: TextView
        var main_detailLV: LinearLayout
        var itemimageIV: ImageView


        init {
            main_detail_comment_typeIV = v.findViewById(R.id.main_detail_comment_typeIV)
            main_detail_comment_profileIV = v.findViewById(R.id.main_detail_comment_profileIV)
            main_detail_comment_nicknameTV = v.findViewById(R.id.main_detail_comment_nicknameTV)
            main_detail_comment_relationIV = v.findViewById(R.id.main_detail_comment_relationIV)
            main_detail_comment_conditionTV = v.findViewById(R.id.main_detail_comment_conditionTV)
            main_detail_comment_dateTV = v.findViewById(R.id.main_detail_comment_dateTV)
            main_detailLV = v.findViewById(R.id.main_detailLV)
            itemimageIV = v.findViewById(R.id.itemimageIV)

        }
    }
}

