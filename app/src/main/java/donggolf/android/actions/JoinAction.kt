package donggolf.android.actions

import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.models.Info


class JoinAction {

    companion object {

        fun join(key: String, params: Info, result: (success: Boolean) -> Unit) {
            FirebaseFirestoreUtils.save("infos", key, params) {
                result(it)
            }
        }


    }

}