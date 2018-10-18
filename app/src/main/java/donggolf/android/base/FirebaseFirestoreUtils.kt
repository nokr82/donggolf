package donggolf.android.base

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class FirebaseFirestoreUtils {

    companion object {

        val db = FirebaseFirestore.getInstance()

        fun list(collectionName: String, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

            val params = HashMap<String, Any>()

            list(collectionName, params, null, -1, result)
        }

        fun list(collectionName: String, orderBy:Pair<*, *>?, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

            val params = HashMap<String, Any>()

            list(collectionName, params, null, -1, result)
        }

        fun list(collectionName: String, orderBy:Pair<*, *>?, page: Int, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

            val params = HashMap<String, Any>()

            list(collectionName, params, null, -1, result)
        }

        fun list(collectionName: String, params: Map<String, Any>, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

            val params = HashMap<String, Any>()

            list(collectionName, params, null, -1, result)
        }

        fun list(collectionName: String, params: Map<String, Any>, orderBy:Pair<*, *>?, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

            val params = HashMap<String, Any>()

            list(collectionName, params, null, -1, result)
        }

        fun list(collectionName: String, params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

            val params = HashMap<String, Any>()

            list(collectionName, params, orderBy, page, 20, result)
        }

        fun list(collectionName: String, params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, limit: Long, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

            // Create a reference to the cities collection
            val ref = db.collection(collectionName)

            params.keys.forEach {
                val key = it
                val value = params[key]

                ref.whereEqualTo(key.toString(), value);
            }

            // orderBy
            if(orderBy != null) {
                val key = orderBy.first
                if(key != null) {
                    var direction = orderBy.second as? Query.Direction
                    if(direction == null) {
                        direction = Query.Direction.ASCENDING
                    }

                    ref.orderBy(key.toString(), direction)

                    /*
                    println("key : $key, di : $direction")

                    // paging
                    if(page > 0) {

                        println("(page - 1) * limit : ${(page - 1) * limit}")

                        ref.startAt((page - 1) * limit)
                        ref.limit(limit)
                    }
                    */
                }

            }

            ref.get()
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

        fun get(collectionName: String, params:Map<String, Any>,key:String, result: (success:Boolean, data:Map<String, Any>?, exception:Exception?) -> Unit) {

            val ref = db.collection(collectionName)

            params.keys.forEach {
                val key = it
                val value = params[key]

                println("key : " + key)
                println("value : " + value)
                println("================================================================")

                ref.whereEqualTo(key.toString(), value);
            }


            ref
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