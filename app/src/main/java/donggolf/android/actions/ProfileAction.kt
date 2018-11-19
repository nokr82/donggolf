package donggolf.android.actions

import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.models.Search

class ProfileAction {
    companion object {

        fun getProfile(params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.list("users", params, orderBy, page) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->
                result(success, data, exception)
            }
        }

        fun viewContent(key: String, result: (success:Boolean, data:Map<String, Any>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.get("users", key) { success:Boolean, data:Map<String, Any>?, exception:Exception? ->
                result(success, data, exception)
            }
        }

        //page = 20,
        fun searchFriendsWithTag(search:Map<String, Any>, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.list("users", search){ success, data, exception ->
                result(success, data, exception)
            }
        }

        fun modifyProfile(params: String, result: (success:Boolean, key:String?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.save("users", params) { success: Boolean, key: String?, exception: Exception? ->
                result(success, key, exception)
            }
        }

        //탈퇴
        fun deleteUser(key: String, result: (success: Boolean) -> Unit) {
            FirebaseFirestoreUtils.delete("users", key) {
                result(it)
            }
        }
    }
}