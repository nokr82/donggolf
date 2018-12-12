package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.HttpClient

object PostAction {

    fun add_post(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/add_post.json", params, handler)
    }

    fun load_post(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/load_post.json", params, handler)
    }

}