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

    fun get_post(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/get_post.json", params, handler)
    }

    fun update_post(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/update_post.json", params, handler)
    }

    fun add_search(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/add_search.json", params, handler)
    }

    fun search_list(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/search_list.json", params, handler)
    }

    fun delete_search(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/delete_search.json", params, handler)
    }

    fun add_report(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/add_report.json", params, handler)
    }

    fun get_my_report(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/get_my_report.json", params, handler)
    }

    fun delete_report(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/delete_report.json", params, handler)
    }

    fun add_favorite_content(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/add_favorite_content.json", params, handler)
    }

    fun my_favorite_content(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/my_favorite_content.json", params, handler)
    }

    fun delete_favorite_content(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/delete_favorite_content.json", params, handler)
    }

    fun add_friend(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/add_friend.json", params, handler)
    }

    fun add_like(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/add_like.json", params, handler)
    }

    fun add_looker(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/add_looker.json", params, handler)
    }

    fun load_region(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/post/load_region.json", params, handler)
    }



}