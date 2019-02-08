package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.HttpClient
import donggolf.android.models.Info


class JoinAction {

    companion object {
        fun join(key: String, params: Info, result: (success: Boolean) -> Unit) {
            FirebaseFirestoreUtils.save("infos", key, params) {
                result(it)
            }
        }

        //중복 닉네임?
        fun is_duplicated_nick(params: RequestParams, handler: JsonHttpResponseHandler) {

        }

    }
}