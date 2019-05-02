package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.FirebaseFirestoreUtils
import donggolf.android.base.HttpClient
import donggolf.android.models.Info


object EventsAction {

    fun index(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/events/index.json", params, handler)
    }

}