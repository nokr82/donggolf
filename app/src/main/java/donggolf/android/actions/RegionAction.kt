package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.HttpClient

object RegionAction {

    fun api_sido(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/regions/api_sido.json", params, handler)
    }

    fun api_gugun(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/regions/api_gugun.json", params, handler)
    }
    fun get_region(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/regions/get_region.json", params, handler)
    }
}