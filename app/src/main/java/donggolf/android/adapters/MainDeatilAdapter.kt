package donggolf.android.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.R
import donggolf.android.activities.ProfileActivity
import donggolf.android.base.FirebaseFirestoreUtils
import org.json.JSONObject



open class MainDeatilAdapter(context: Context, view:Int, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context,view, data){

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

        var data : JSONObject = getItem(position)

        //LinearLayout click listener ; going to go other's mini-homepage
        /*View.OnClickListener{
            println("main_detailLV was clicked")
        }*/


        item.main_detailLV.setOnClickListener {
            //println("main_detailLV was clicked")
            //유저 조회
            /*var condition:HashMap<String, String> = HashMap<String,String>()
            condition.put("nick", item.main_detail_listitem_nickname.text.toString())*/
            var nick= item.main_detail_listitem_nickname.text.toString()
            val db = FirebaseFirestore.getInstance()
            var writer : String = ""

            db.collection("users")
                    .whereEqualTo("nick",nick)
                    .get()
                    .addOnCompleteListener{
                        if (it.isSuccessful) {
                            for (document in it.getResult()) {
                                Log.d("getUserData", "getId : "+document.getId() + " => " + " getData : " +document.getData())
                                writer = document.getId()
                            }
                        }
                    }

            var goit = Intent(context, ProfileActivity::class.java)
            goit.putExtra("writerID", writer)
            it.context.startActivity(goit)
//            println(data.getJSONObject("id"))
        }

        return retView
    }

    fun getUserData(nick:String){

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


        var main_detail_listitem_comment : ImageView
        var main_detail_listitem_profileimage : ImageView
        var main_detail_listitem_nickname : TextView
        var main_detail_listitem_firstimage : ImageView
        var main_detail_listitem_condition : TextView
        var main_detail_listitem_date : TextView
        var main_detailLV : LinearLayout


        init {
            main_detail_listitem_comment = v.findViewById<View>(R.id.main_detail_listitem_comment) as ImageView
            main_detail_listitem_profileimage = v.findViewById<View>(R.id.main_detail_listitem_profileimage) as ImageView
            main_detail_listitem_nickname = v.findViewById<View>(R.id.main_detail_listitem_nickname) as TextView
            main_detail_listitem_firstimage = v.findViewById<View>(R.id.main_detail_listitem_firstimage) as ImageView
            main_detail_listitem_condition = v.findViewById<View>(R.id.main_detail_listitem_condition) as TextView
            main_detail_listitem_date = v.findViewById<View>(R.id.main_detail_listitem_date) as TextView
            main_detailLV = v.findViewById(R.id.main_detailLV) as LinearLayout

        }
    }
}