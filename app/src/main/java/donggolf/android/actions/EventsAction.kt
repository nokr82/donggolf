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

    fun detail(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/events/detail.json", params, handler)
    }

    fun participation(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/events/participation.json", params, handler)
    }

    fun event_members(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/events/event_members.json", params, handler)
    }

}