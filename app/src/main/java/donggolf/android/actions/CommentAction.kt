package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.HttpClient

object CommentAction {

    fun comment_at_content(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/comment/comments_content.json", params, handler)
    }
}