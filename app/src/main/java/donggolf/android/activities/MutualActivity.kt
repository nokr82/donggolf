package donggolf.android.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.google.firebase.auth.FirebaseAuth
import donggolf.android.R
import donggolf.android.adapters.MainAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.base.RootActivity
import org.json.JSONObject

class MutualActivity : RootActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<Map<String, Any>> = ArrayList<Map<String, Any>>()

    private  lateinit var  adapter : MainAdapter

    private  lateinit var  editadapter : MainEditAdapter

    private  var editadapterData : ArrayList<JSONObject> = ArrayList<JSONObject>()

    private val SELECT_PICTURE: Int = 101

    val user = HashMap<String, Any>()

    companion object {
        const val TAG = "MainActivity"
    }

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mutual)




    }
}

open class MutFriendAdapter(context: Context, view: Int, data: ArrayList<Map<String, Any>>) : ArrayAdapter<Map<String, Any>>(context, view, data){

}
