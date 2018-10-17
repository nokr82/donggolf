package donggolf.android.base

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.activities.MainActivity

class FirebaseFirestoreUtils {

    companion object {

        val db = FirebaseFirestore.getInstance()

        fun get(collectionName: String, params: HashMap<String, Any>, result: () -> Unit) {
            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                Log.d(MainActivity.TAG, document.getId() + " => " + document.getData())
                            }
                        } else {
                            Log.w(MainActivity.TAG, "Error getting documents.", task.exception)
                        }
                    }
        }

        fun get(collectionName: String, key:String, result: (success:Boolean, data:Map<String, Any>?, exception:Exception?) -> Unit) {
            db.collection(collectionName)
                    .document(key)
                    .get()
                    .addOnSuccessListener {
                        result(true, it.data, null)
                    }
                    .addOnFailureListener {
                        result(false, null, it)
                    }
        }

        fun save(collectionName:String, params: Any, result: (success:Boolean, key:String?, exception:Exception?) -> Unit) {
            db.collection(collectionName)
                    .add(params)
                    .addOnSuccessListener {
                        result(true, it.id, null)
                    }
                    .addOnFailureListener {
                        result(false, null, it)
                    }
        }

        fun save(collectionName:String, key:String, params: Any, result: (success:Boolean) -> Unit) {
            db.collection(collectionName)
                    .document(key)
                    .set(params)
                    .addOnSuccessListener {
                        result(true)
                    }
                    .addOnFailureListener {
                        result(false)
                    }
        }

        fun delete(collectionName:String, key:String, result: (success:Boolean) -> Unit) {
            db.collection(collectionName)
                    .document(key)
                    .delete()
                    .addOnSuccessListener {
                        result(true)
                    }
                    .addOnFailureListener {
                        result(false)
                    }
        }
    }

}