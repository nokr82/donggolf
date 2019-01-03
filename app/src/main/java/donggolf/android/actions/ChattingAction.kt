package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.HttpClient

object ChattingAction {

    fun add_chat(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/add_chat.json", params, handler)
    }

}