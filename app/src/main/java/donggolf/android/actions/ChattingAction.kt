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

    fun chatting(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/chatting.json", params, handler)
    }

    fun add_chat_member(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/add_chat_member.json", params, handler)
    }

    fun set_chatting_setting(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/set_chatting_setting.json", params, handler)
    }

    fun set_push(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/set_push.json", params, handler)
    }

    fun delete_chat_member(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/delete_chat_member.json", params, handler)
    }

    fun set_text_size(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/set_text_size.json", params, handler)
    }

    fun set_block(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/set_block.json", params, handler)
    }

    fun delete_chatting_room(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/delete_chatting_room.json", params, handler)
    }

    fun set_notice(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/set_notice.json", params, handler)
    }

    fun get_announcement(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/get_announcement.json", params, handler)
    }

    fun get_chat_member(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/get_chat_member.json", params, handler)
    }

    fun set_dongchat_block(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/chatting/set_dongchat_block.json", params, handler)
    }

}