package donggolf.android.actions

import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.models.Content

class ContentAction {

    companion object {

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

        fun updateContent(key: String, params: Content, result: (success: Boolean) -> Unit) {
            FirebaseFirestoreUtils.save("contents", key, params) {
                result(it)
            }
        }

        fun deleteContent(key: String, params: Content, result: (success: Boolean) -> Unit) {
            FirebaseFirestoreUtils.delete("contents", key) {
                result(it)
            }
        }
    }

}