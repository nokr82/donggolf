package donggolf.android.actions

import donggolf.android.base.FirebaseFirestoreUtils

class InfoAction {

    companion object {

        fun getInfo(key: String, result: (success:Boolean, data:Map<String, Any>?, exception:Exception?) -> Unit) {
            FirebaseFirestoreUtils.get("infos", key) { success: Boolean, data: Map<String, Any>?, exception: Exception? ->
                result(success, data, exception)
            }
        }
    }

}