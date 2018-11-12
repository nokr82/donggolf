package donggolf.android.actions

import donggolf.android.base.FirebaseFirestoreUtils

class NationalAction {

    companion object {

        fun list(params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.list("settings", "national", params, orderBy, page) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->
                result(success, data, exception)
            }
        }

        fun nationallist(key: String, result: (success:Boolean, data:Map<String, Any>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.get("settings", key) { success:Boolean, data:Map<String, Any>?, exception:Exception? ->
                result(success, data, exception)
            }
        }

    }
}