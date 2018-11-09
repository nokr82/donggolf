package donggolf.android.actions

import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.models.Search

class SearchAction {

    companion object {

        fun list(params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.list("search", params, orderBy, page) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->
                result(success, data, exception)
            }
        }

        fun saveContent(params: Search, result: (success:Boolean, key:String?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.save("search", params) { success: Boolean, key: String?, exception: Exception? ->
                result(success, key, exception)
            }
        }

        fun deleteContent(key: String, result: (success: Boolean) -> Unit) {
            FirebaseFirestoreUtils.delete("search", key) {
                result(it)
            }
        }
    }
}