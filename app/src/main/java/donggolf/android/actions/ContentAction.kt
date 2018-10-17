package donggolf.android.actions

import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.models.Content

class ContentAction {

    companion object {

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