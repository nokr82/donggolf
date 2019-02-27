package donggolf.android.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.nostra13.universalimageloader.core.ImageLoader
import donggolf.android.R
import donggolf.android.activities.PictureDetailActivity
import donggolf.android.base.Config
import donggolf.android.base.Utils
import org.json.JSONObject


open class GoodsComAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

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

        var data = data.get(position)
        //val member_info = data.getJSONObject("Member")
        val comment = data.getJSONObject("MarketComment")

        var comment_id = Utils.getInt(comment,"id")
        data.put("market_id", comment_id)

        item.main_detail_comment_conditionTV.text = Utils.getString(comment,"comment")
        data.put("comment_content", Utils.getString(comment,"comment"))
        item.main_detail_comment_nicknameTV.text = Utils.getString(comment,"nick")
        item.main_detail_comment_dateTV.text = Utils.getString(comment,"created")
        item.main_detail_comment_typeIV.visibility = View.GONE

        var typeAdt = Utils.getString(comment,"type")

 /*       if (typeAdt == "r"){
            item.main_detail_comment_typeIV.visibility = View.VISIBLE
            item.main_detail_comment_typeIV.setImageResource(R.drawable.icon_comment1)

        } else if (typeAdt == "c") {
            item.main_detail_comment_typeIV.visibility = View.VISIBLE
            item.main_detail_comment_typeIV.setImageResource(R.drawable.icon_comment2)

        } else {
            item.main_detail_comment_typeIV.visibility = View.GONE

        }*/


        var comment_memberID = Utils.getInt(comment,"member_id")
        data.put("cmt_wrt_id", comment_memberID)

        var isBlocked = Utils.getString(comment,"block_yn")

        /*var cbyn = data.getString("changedBlockYN")
        if (cbyn == "Y"){
            isBlocked = data.getString("block_yn")
        }*/
        var p_comments_id = Utils.getInt(comment,"p_comments_id")
        var op_comments_id = Utils.getInt(comment,"op_comments_id")
        if (p_comments_id != -1){
            if (op_comments_id != -1) {
                item.main_detail_comment_typeIV.visibility = View.VISIBLE
                item.main_detail_comment_typeIV.setImageResource(R.drawable.icon_comment2)

            }else{
                item.main_detail_comment_typeIV.visibility = View.VISIBLE
                item.main_detail_comment_typeIV.setImageResource(R.drawable.icon_comment1)
            }
        } else {
            item.main_detail_comment_typeIV.visibility = View.GONE
        }

        val freind = Utils.getString(comment,"freind")

        println("-=------freind : $freind")
        if (freind == "0"){
            item.main_detail_comment_relationIV.setImageResource(R.drawable.icon_second)
        } else {
            item.main_detail_comment_relationIV.setImageResource(R.drawable.icon_first)
        }

        if (isBlocked == "Y") {
            item.main_detail_comment_relationIV.setImageResource(R.drawable.icon_block)
            notifyDataSetChanged()
        } else {
            item.main_detail_comment_relationIV.setImageResource(R.drawable.icon_second)
            notifyDataSetChanged()
        }
        val Member = data.getJSONObject("Member")
        val profile_img = Utils.getString(Member,"profile_img")
        if (profile_img != null){
            var image = Config.url + profile_img
            ImageLoader.getInstance().displayImage(image, item.main_detail_comment_profileIV, Utils.UILoptionsUserProfile)
        }

        var gender = Utils.getString(Member,"sex")
        if (gender == "0"){
            item.main_detail_comment_nicknameTV.setTextColor(Color.parseColor("#000000"))
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

        var main_detail_comment_typeIV : ImageView
        var main_detail_comment_profileIV : ImageView
        var main_detail_comment_nicknameTV : TextView
        var main_detail_comment_relationIV : ImageView
        var main_detail_comment_conditionTV : TextView
        var main_detail_comment_dateTV : TextView
        var main_detailLV : LinearLayout


        init {
            main_detail_comment_typeIV = v.findViewById(R.id.main_detail_comment_typeIV)
            main_detail_comment_profileIV = v.findViewById(R.id.main_detail_comment_profileIV)
            main_detail_comment_nicknameTV = v.findViewById(R.id.main_detail_comment_nicknameTV)
            main_detail_comment_relationIV = v.findViewById(R.id.main_detail_comment_relationIV)
            main_detail_comment_conditionTV = v.findViewById(R.id.main_detail_comment_conditionTV)
            main_detail_comment_dateTV = v.findViewById(R.id.main_detail_comment_dateTV)
            main_detailLV = v.findViewById(R.id.main_detailLV)

        }
    }
}

