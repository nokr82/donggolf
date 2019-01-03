package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.HttpClient

object ChattingAction {

    fun add_chat(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/add_chat.json", params, handler)
    }

    fun load_chatting(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/load_chatting.json", params, handler)
    }

    fun detail_chatting(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/detail_chatting.json", params, handler)
    }

    fun add_chatting(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/add_chatting.json", params, handler)
    }


}