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

    fun get_mates_list(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/mate/mate_req_list.json", params, handler)
    }

    fun accept_mates(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/mate/accept_mate.json", params, handler)
    }

    fun update_mates_status(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/mate/update_mate.json", params, handler)
    }

    fun getCategoryID(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/mate/category_info.json", params, handler)
    }

    fun rejectMateRequest(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/mate/reject_mate.json", params, handler)
    }

    fun blockMember(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/mate/block_mate.json", params, handler)
    }
}