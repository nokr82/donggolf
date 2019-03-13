package donggolf.android.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import donggolf.android.R
import donggolf.android.activities.FriendReqSelectCategoryActivity
import donggolf.android.activities.ProfileActivity
import donggolf.android.activities.RequestFriendActivity
import donggolf.android.base.Config
import donggolf.android.base.Utils
import org.json.JSONObject
import java.util.ArrayList

class MateManageAdapter(context: Context, view:Int, data: ArrayList<JSONObject>,requestFriendActivity: RequestFriendActivity,wait:String) : ArrayAdapter<JSONObject>(context,view, data) {

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<JSONObject> = data
    var requestFriendActivity = requestFriendActivity
    var wait = wait

//    var isCheckedMate = false

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

        val json = data.get(position)

        var check = json.getBoolean("check")//임의로 따로 넣어준 변수값
        var division = json.getString("division")


        val matemember = json.getJSONObject("MateMember")
        var mate_id = Utils.getString(matemember,"id")
        var mateprofile = Utils.getString(matemember,"profile_img")
        var mate_gender = Utils.getString(matemember,"sex")

        val member = json.getJSONObject("Member")
        val member_id = Utils.getString(member,"id")
        val profile = Utils.getString(member,"profile_img")
        val member_gender = Utils.getString(member,"sex")

        json.put("mate_id", Utils.getInt(matemember,"id"))

        if (division == "w"){
            item.mates_checkboxRL.visibility = View.GONE
            item.mates_nickTV.text = Utils.getString(member,"nick")
            if (member_gender == "0"){
                item.mates_nickTV.setTextColor(Color.parseColor("#000000"))
            } else {
                item.mates_nickTV.setTextColor(Color.parseColor("#EF5C34"))
            }
            item.mates_status_msgTV.text = Utils.getString(member,"status_msg")
            if (profile != null){
                var image = Config.url + profile
                ImageLoader.getInstance().displayImage(image, item.mates_profileIV, Utils.UILoptionsUserProfile)
            } else {
                item.mates_profileIV.setImageResource(R.drawable.icon_profiles)
            }
        } else if (division == "s"){
            item.acceptLL.visibility = View.GONE
            item.cancleLL.visibility = View.VISIBLE
            item.mates_checkboxRL.visibility = View.GONE
            item.mates_nickTV.text = Utils.getString(matemember,"nick")
            if (mate_gender == "0"){
                item.mates_nickTV.setTextColor(Color.parseColor("#000000"))
            } else {
                item.mates_nickTV.setTextColor(Color.parseColor("#EF5C34"))
            }
            item.mates_status_msgTV.text = Utils.getString(matemember,"status_msg")
            if (mateprofile != null){
                var image = Config.url + mateprofile
                ImageLoader.getInstance().displayImage(image, item.mates_profileIV, Utils.UILoptionsUserProfile)
            } else {
                item.mates_profileIV.setImageResource(R.drawable.icon_profiles)
            }
        } else {
            item.acceptLL.visibility = View.GONE
            item.mates_checkboxRL.visibility = View.GONE
            item.blockcancleLL.visibility = View.VISIBLE
            item.mates_nickTV.text = Utils.getString(matemember,"nick")
            if (mate_gender == "0"){
                item.mates_nickTV.setTextColor(Color.parseColor("#000000"))
            } else {
                item.mates_nickTV.setTextColor(Color.parseColor("#EF5C34"))
            }
            item.mates_status_msgTV.text = Utils.getString(matemember,"status_msg")
            if (mateprofile != null){
                var image = Config.url + mateprofile
                ImageLoader.getInstance().displayImage(image, item.mates_profileIV, Utils.UILoptionsUserProfile)
            } else {
                item.mates_profileIV.setImageResource(R.drawable.icon_profiles)
            }
        }

        if (check) {
            item.mates_checkedIV.visibility = View.VISIBLE
        } else  {
            item.mates_checkedIV.visibility = View.INVISIBLE
        }

        item.mates_checkboxRL.setOnClickListener {

            check = !check

            json.put("check", check)

            notifyDataSetChanged()

//            isCheckedMate = !isCheckedMate

        }

        item.mates_profileIV.setOnClickListener {
            var intent = Intent(context, ProfileActivity::class.java)
            if (wait == "wait"){
                intent.putExtra("member_id", member_id)
            } else {
                intent.putExtra("member_id", mate_id)
            }
            context.startActivity(intent)
        }

        item.accLL.setOnClickListener {
            val acceptItt = Intent(context, FriendReqSelectCategoryActivity::class.java)
            acceptItt.putExtra("mates_id",member_id.toInt())
            Log.d("2아뒤",member_id.toString())
            context.startActivity(acceptItt)
           /* removeItem(position)
            notifyDataSetChanged()*/
        }

        item.refuseLL.setOnClickListener {
            requestFriendActivity.refuse(member_id)
        }

        item.cancleLL.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("${Utils.getString(matemember,"nick")} 님에게 보낸 친구 요청을 취소하시겠습니까 ?")

                    .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        requestFriendActivity.cancle(Utils.getString(matemember,"id"))
                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })

            val alert = builder.create()
            alert.show()
        }

        item.blockcancleLL.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                    .setMessage("${Utils.getString(matemember,"nick")} 님을 차단해제 하시겠습니까 ?")

                    .setPositiveButton("예", DialogInterface.OnClickListener { dialog, id ->
                        requestFriendActivity.block_cancle(Utils.getString(matemember,"id"))
                        dialog.cancel()
                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })

            val alert = builder.create()
            alert.show()
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

        var mates_profileIV : CircleImageView //프사
        var isOnlineIV : ImageView //프사에 붙은 초록불
        var mates_nickTV : TextView //닉네임
        var mates_status_msgTV : TextView //상메
        var mates_checkedIV : ImageView //체크박스 체크
        var mates_checkboxRL : RelativeLayout

        var acceptLL : LinearLayout
        var acceptTV : TextView
        var refuseTV : TextView
        var cancleTV : TextView
        var blockcancleTV : TextView

        var accLL : LinearLayout
        var refuseLL : LinearLayout
        var cancleLL : LinearLayout
        var blockcancleLL : LinearLayout

        init {
            mates_profileIV = v.findViewById(R.id.mates_profileIV)
            isOnlineIV = v.findViewById(R.id.isOnlineIV)
            mates_nickTV = v.findViewById(R.id.mates_nickTV)
            mates_status_msgTV = v.findViewById(R.id.mates_status_msgTV)
            mates_checkedIV = v.findViewById(R.id.mates_checkedIV)
            mates_checkboxRL = v.findViewById(R.id.mates_checkboxRL)
            acceptLL = v.findViewById(R.id.acceptLL)
            acceptTV = v.findViewById(R.id.acceptTV)
            refuseTV = v.findViewById(R.id.refuseTV)
            cancleTV = v.findViewById(R.id.cancleTV)
            blockcancleTV = v.findViewById(R.id.blockcancleTV)
            accLL = v.findViewById(R.id.accLL)
            refuseLL = v.findViewById(R.id.refuseLL)
            cancleLL = v.findViewById(R.id.cancleLL)
            blockcancleLL = v.findViewById(R.id.blockcancleLL)
        }
    }
}