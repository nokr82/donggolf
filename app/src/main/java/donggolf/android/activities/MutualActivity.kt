package donggolf.android.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.R
import donggolf.android.adapters.MainAdapter
import donggolf.android.adapters.MainEditAdapter
import donggolf.android.base.RootActivity
import org.json.JSONObject

class MutualActivity : RootActivity() {

    private lateinit var context: Context

    private  var adapterData : ArrayList<Map<String, Any>> = ArrayList<Map<String, Any>>()

    private  lateinit var  adapter : MainAdapter

    val user = HashMap<String, Any>()

    companion object {
        const val TAG = "MutualActivity"
    }

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mutual)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()
        val db = FirebaseFirestore.getInstance()


    }
}

