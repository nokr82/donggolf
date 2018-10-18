package donggolf.android.base

import com.google.firebase.firestore.FirebaseFirestore
import donggolf.android.models.Content

class FirebaseFirestoreUtils {

    companion object {

        val db = FirebaseFirestore.getInstance()

        fun list(collectionName: String, params: Content, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            db.collection(collectionName)
                    .get()
                    .addOnSuccessListener {

                        val data = ArrayList<Map<String, Any>?>()

                        it.documents.forEach {
                            data.add(it.data)
                        }

                        result(true, data, null)
                    }
                    .addOnFailureListener {
                        result(false, null, it)
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