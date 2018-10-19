package donggolf.android.actions

import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.models.Content

class ContentAction {

    companion object {

        fun list(params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.list("contents", params, orderBy, page) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->
                result(success, data, exception)
            }
        }

        fun viewContent(key: String, result: (success:Boolean, data:Map<String, Any>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.get("contents", key) { success:Boolean, data:Map<String, Any>?, exception:Exception? ->
                result(success, data, exception)
            }
        }

        fun saveContent(params: Content, result: (success:Boolean, key:String?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.save("contents", params) { success: Boolean, key: String?, exception: Exception? ->
                result(success, key, exception)
            }
        }

        fun deleteContent(key: String, params: Content, result: (success: Boolean) -> Unit) {
            FirebaseFirestoreUtils.delete("contents", key) {
                result(it)
            }
        }

        fun getContent(params: Map<String, Any>, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.list("contents", params) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->
                result(success, data, exception)

            }
        }

    }

}