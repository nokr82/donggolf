package donggolf.android.base

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import com.google.firebase.storage.FirebaseStorage
import kotlin.collections.ArrayList


class FirebaseFirestoreUtils {

    companion object {

        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

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
            list(collectionName, params, null, -1, result)
        }

        fun list(collectionName: String, params: Map<String, Any>, orderBy:Pair<*, *>?, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            list(collectionName, params, null, -1, result)
        }

        fun list(collectionName: String, params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            list(collectionName, params, orderBy, page, 20, result)
        }

        fun list(collectionName: String, documentsName:String, params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            list(collectionName, documentsName, params, orderBy, page, 20, result)
        }

        fun list(collectionName: String, documentsName:String, params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, limit: Long, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

            // Create a reference to the cities collection
            val ref = db.collection(collectionName)
            ref.document(documentsName)

            var query:Query? = null

            params.keys.forEach {
                val key = it
                val value = params[key]

                println("key in FireBaseFireStoreUtils===================================================================$key")
                println("value in FireBaseFireStoreUtils=================================================================$value")

                query = ref.whereEqualTo(key, value)
            }

            // orderBy
            if(orderBy != null) {
                val key = orderBy.first
                if(key != null) {
                    var direction = orderBy.second as? Query.Direction
                    if(direction == null) {
                        direction = Query.Direction.ASCENDING
                    }

                    query = ref.orderBy(key.toString(), direction)

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

            query!!.get()
                    .addOnSuccessListener {

                        val data = ArrayList<Map<String, Any>?>()

                        it.documents.forEach {
                            val item = it.data
                            if (item != null) {
                                item!!.put("id", it.id)
                                data.add(item)
                            }
                        }

                        result(true, data, null)
                    }
                    .addOnFailureListener {
                        result(false, null, it)
                    }
        }

        fun list(collectionName: String, params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, limit: Long, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

            // Create a reference to the cities collection
            val ref = db.collection(collectionName)
            var query:Query? = null

            params.keys.forEach {
                val key = it
                val value = params[key]

                query = ref.whereEqualTo(key.toString(), value)
            }

            // orderBy
            if(orderBy != null) {
                val key = orderBy.first
                if(key != null) {
                    var direction = orderBy.second as? Query.Direction
                    if(direction == null) {
                        direction = Query.Direction.ASCENDING
                    }

                    query = ref.orderBy(key.toString(), direction)

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

            query!!.get()
                    .addOnSuccessListener {

                        val data = ArrayList<Map<String, Any>?>()

                        it.documents.forEach {
                            val item = it.data
                            if (item != null) {
                                item!!.put("id", it.id)
                                data.add(item)
                            }
                        }

                        result(true, data, null)
                    }
                    .addOnFailureListener {
                        result(false, null, it)
                    }
        }

        //test. later,
        fun slist(collectionName: String, params: String, orderBy:Pair<*, *>?, page: Int, limit: Long, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

            // Create a reference to the cities collection
            val ref = db.collection(collectionName)
            var query:Query? = null

            query = ref.whereEqualTo("title", params)

            // orderBy
            if(orderBy != null) {
                val key = orderBy.first
                if(key != null) {
                    var direction = orderBy.second as? Query.Direction
                    if(direction == null) {
                        direction = Query.Direction.ASCENDING
                    }

                    query = ref.orderBy(key.toString(), direction)

                }

            }

            query!!.get()
                    .addOnSuccessListener {

                        val data = ArrayList<Map<String, Any>?>()

                        it.documents.forEach {
                            val item = it.data
                            if (item != null) {
                                item!!.put("id", it.id)
                                data.add(item)
                            }
                        }

                        result(true, data, null)
                    }
                    .addOnFailureListener {
                        result(false, null, it)
                    }
        }//sList end

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

        fun uploadFile(bytes:ByteArray, path:String, result: (success:Boolean) -> Unit) {
            val ref = storage.reference.child(path)
            ref.putBytes(bytes)
                    .addOnSuccessListener {
                        println(it.storage)
                        result(true)
                    }
                    .addOnFailureListener {
                        result(false)
                    }
        }

        fun getFileUri(path:String, result: (success:Boolean, uri: String?, exception:Exception?) -> Unit) {
            val ref = storage.reference.child(path)
            ref.downloadUrl
                    .addOnSuccessListener {
                        result(true, it.toString(), null)
                    }
                    .addOnFailureListener {
                        result(false, null, it)
                    }
        }

        fun deleteFile(path:String, result: (success:Boolean) -> Unit) {
            val ref = storage.reference.child(path)
            ref.delete()
                    .addOnSuccessListener {
                        result(true)
                    }
                    .addOnFailureListener {
                        result(false)
                    }

        }

        fun updateField(collectionName:String, key:String, field:String, propertyKey:String, propertyValue:Any, result: (success:Boolean) -> Unit) {

            db.runTransaction {
                val docRef = db.collection(collectionName).document(key)
                val doc = it.get(docRef)
                val data = doc.data

                val field = data!![field] as HashMap<String, Any>
                field[propertyKey] = propertyValue
                it.update(docRef, data)

                result(true)
            }

        }

        fun deleteFieldKey(collectionName:String, key:String, field:String, propertyKey:String, result: (success:Boolean) -> Unit) {

            db.runTransaction {
                val docRef = db.collection(collectionName).document(key)
                val doc = it.get(docRef)
                val data = doc.data

                val field = data!![field] as HashMap<String, Any>
                field.remove(propertyKey)
                it.update(docRef, data)

                result(true)
            }

        }

    }


    //임시 : 닉네임으로 user PK 검색 서칭
    fun tempGetOthersPK(collectionName: String, params: String, orderBy:Pair<*, *>?, page: Int, limit: Long, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {

        // Create a reference to the cities collection
        val ref = db.collection(collectionName)
        var query:Query? = null

        query = ref.whereEqualTo("title", params)

        // orderBy
        if(orderBy != null) {
            val key = orderBy.first
            if(key != null) {
                var direction = orderBy.second as? Query.Direction
                if(direction == null) {
                    direction = Query.Direction.ASCENDING
                }

                query = ref.orderBy(key.toString(), direction)

            }

        }

        query!!.get()
                .addOnSuccessListener {

                    val data = ArrayList<Map<String, Any>?>()

                    it.documents.forEach {
                        val item = it.data
                        if (item != null) {
                            item!!.put("id", it.id)
                            data.add(item)
                        }
                    }

                    result(true, data, null)
                }
                .addOnFailureListener {
                    result(false, null, it)
                }
    }

}