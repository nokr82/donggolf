package donggolf.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance();

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
                                Log.d(MainActivity.TAG, document.getId() + " => " + document.getData())
                            }
                        } else {
                            Log.w(MainActivity.TAG, "Error getting documents.", task.exception)
                        }
                    }
                })
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
