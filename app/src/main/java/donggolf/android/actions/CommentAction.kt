package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.HttpClient

object CommentAction {

    fun get_content_comment_list(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/comment/get_content_comment.json", params, handler)
    }
    fun add_market_comment(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/comment/add_market_comment.json", params, handler)
    }
    fun write_comments(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/comment/write_comments.json", params, handler)
    }
    fun delete_content_comment(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/comment/delete_comment.json", params, handler)
    }

    fun content_commenter_ben(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/comment/block_commenter.json", params, handler)
    }

}