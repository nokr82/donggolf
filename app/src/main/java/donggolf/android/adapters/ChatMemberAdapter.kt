package donggolf.android.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import donggolf.android.R
import donggolf.android.models.MutualFriendData
import kotlin.collections.ArrayList

class ChatMemberAdapter(context: Context, view : Int, data: ArrayList<MutualFriendData>) : ArrayAdapter<MutualFriendData>(context,view, data) {

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<MutualFriendData> = data

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

        var tmpData : MutualFriendData = data.get(position)

        //item.item_profileImg.setImageResource(tmpData.profileImg)
        item.item_nickNameTV.setText(tmpData.nickName)
        item.item_authorizationTV.setText(tmpData.condition)

        //item.item_relationIV.setImageResource(R.drawable.icon_second)

        return retView
    }

    override fun getItem(position: Int): MutualFriendData? {

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

        var item_profileImg : ImageView
        var item_nickNameTV : TextView
        var item_relationIV : ImageView
        var item_authorizationTV : TextView

        init {
            item_profileImg = v.findViewById(R.id.item_profileImg)
            item_nickNameTV = v.findViewById(R.id.item_nickNameTV)
            item_relationIV = v.findViewById(R.id.item_relationIV)
            item_authorizationTV = v.findViewById(R.id.item_authorizationTV)
        }
    }
}