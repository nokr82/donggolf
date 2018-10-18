package donggolf.android.actions

import donggolf.android.base.FirebaseFirestoreUtils

class InfoAction {

    companion object {

        fun getInfo(key: String, result: (success:Boolean, data:Map<String, Any>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.get("infos", key) { success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                result(success, data, exception)
            }
        }

        fun findId(key: String, params: Map<String, Any>, result: (success:Boolean, data:Map<String, Any>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.get("infos", params, key) { success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                result(success, data, exception)
            }
        }

        fun list(params: Map<String, Any>, orderBy:Pair<*, *>?, page: Int, result: (success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.list("infos", params, orderBy, page) { success:Boolean, data:ArrayList<Map<String, Any>?>?, exception:Exception? ->
                result(success, data, exception)

            }
        }
    }

}