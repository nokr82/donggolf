package donggolf.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import donggolf.android.R
import donggolf.android.adapters.FindPictureAdapter
import donggolf.android.adapters.MainAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.base.RootActivity
import kotlinx.android.synthetic.main.activity_find_picture.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : RootActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    private  lateinit var  adapter : MainAdapter

    private  lateinit var  editadapter : MainEditAdapter

    private  var editadapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    companion object {
        const val TAG = "MainActivity"
    }

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance();

        btn_go_addpost.setOnClickListener {
            startActivity(Intent(this,AddPostActivity::class.java))
        }

        click_btn_main_on_relative.setOnClickListener {

        }

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.getCurrentUser()
        updateUI(currentUser)

        val db = FirebaseFirestore.getInstance()

        // Create a new user with a first and last name
        val user = HashMap<String, Any>()
        user.put("first", "Ada")
        user.put("last", "Lovelace")
        user.put("born", 1815)

        db.collection("users")
                .get()
                .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                    override fun onComplete(task: Task<QuerySnapshot>) {
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                Log.d(TAG, document.getId() + " => " + document.getData())
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.exception)
                        }
                    }
                })


        context = this


        var intent = getIntent()



        var dataObj : JSONObject = JSONObject();

        adapterData.add(dataObj)
        adapterData.add(dataObj)
        adapterData.add(dataObj)
        adapterData.add(dataObj)
        adapterData.add(dataObj)
        adapterData.add(dataObj)
        adapterData.add(dataObj)

        adapter = MainAdapter(context,R.layout.main_listview_item,adapterData)

        main_listview.adapter = adapter


        adapter.notifyDataSetChanged()

        editadapterData.add(dataObj)

        editadapter = MainEditAdapter(context,R.layout.main_edit_listview_item,editadapterData)

        main_edit_listview.adapter = editadapter


        main_listview.setOnItemClickListener { parent, view, position, id ->

            startActivity(Intent(this,MainDetailActivity::class.java))
        }

        main_edit_search.setOnClickListener {
            main_listview_search.setVisibility(View.VISIBLE)
//            main_listview.setVisibility(View.GONE)
        }

        main_edit_close.setOnClickListener {
            main_listview_search.setVisibility(View.GONE)
//            main_listview.setVisibility(View.VISIBLE)
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {

        /*
        mAuth!!.signInWithCustomToken(mCustomToken)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCustomToken:success")
                        val user = mAuth!!.getCurrentUser()
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        */
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

}
