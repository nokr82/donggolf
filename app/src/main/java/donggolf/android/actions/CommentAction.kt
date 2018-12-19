package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.HttpClient

object CommentAction {

    fun comment_at_content(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/comment/add_content_comment.json", params, handler)
    }

    fun get_content_comment_list(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/comment/get_content_comment.json", params, handler)
    }

    fun delete_content_comment(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/comment/delete_comment.json", params, handler)
    }
}