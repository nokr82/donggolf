package donggolf.android.fragment

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import donggolf.android.R
import donggolf.android.actions.ContentAction
import donggolf.android.activities.MainActivity
import donggolf.android.activities.MainDetailActivity
import donggolf.android.adapters.MainAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.models.Content
import org.json.JSONObject

class FreeFragment : Fragment(){

    private  var adapterData : ArrayList<Map<String, Any>> = ArrayList<Map<String, Any>>()
    private  lateinit var  adapter : MainAdapter
    private  lateinit var  editadapter : MainEditAdapter

    val user = HashMap<String, Any>()

    private var mAuth: FirebaseAuth? = null

    lateinit var main_edit_listview: ListView
    lateinit var addpostLL : LinearLayout
    lateinit var main_edit_search : EditText
    lateinit var main_listview_search : LinearLayout
    lateinit var main_edit_close : TextView
    lateinit var main_listview:ListView
    lateinit var activity: MainActivity//메인액티비티 위에 드로잉할 권한

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //progressDialog = ProgressDialog(context)

        //여기서 데이터를 불러와야 함
        /*mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()
        //updateUI(currentUser)

        val db = FirebaseFirestore.getInstance()
        // Create a new user with a first and last name

        user.put("first", "Ada")
        user.put("last", "Lovelace")
        user.put("born", 1815)

        adapter = MainAdapter(activity, R.layout.main_listview_item,adapterData)

        main_listview.adapter = adapter

        adapter.notifyDataSetChanged()

        db.collection("users")
                .get()
                .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                    override fun onComplete(task: Task<QuerySnapshot>) {
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                Log.d(MainActivity.TAG, document.getId() + " => " + document.getData())
                            }
                        } else {
                            Log.w(MainActivity.TAG, "Error getting documents.", task.exception)
                        }
                    }
                })

        var dataObj : JSONObject = JSONObject();

        var content = Content()

        ContentAction.list(user,Pair("createAt",null),0) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->

            if(success && data != null) {
                data.forEach {
                    println(it)

                    if (it != null) {
                        adapterData.add(it)
                    }

                }

                adapter.notifyDataSetChanged()

            }

        }
        //adapter.notifyDataSetChanged()
        adapter = MainAdapter(activity,R.layout.main_listview_item,adapterData)

        main_listview.adapter = adapter

        adapter.notifyDataSetChanged()

        main_listview.setOnItemClickListener { parent, view, position, id ->
            val id : String
            id = adapterData.get(position)!!.get("id").toString()
            MoveMainDetailActivity(id)
        }*/

        return LayoutInflater.from(inflater.context).inflate(R.layout.fragment_main,container, false)
    }

    fun MoveMainDetailActivity(id : String){
        var intent: Intent = Intent(activity, MainDetailActivity::class.java)
        intent.putExtra("id",id)
        startActivity(intent)
    }

}