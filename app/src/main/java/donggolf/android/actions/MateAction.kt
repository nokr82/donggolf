package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.HttpClient

object MateAction {

    fun search_mate(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/history_search_mate.json", params, handler)
    }

    fun view_mate_search_history(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/member/view_fs_history.json", params, handler)
    }
}