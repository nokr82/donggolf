package donggolf.android.actions

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import donggolf.android.base.HttpClient

object MarketAction {

    fun load_category(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/load_category.json", params, handler)
    }

    fun add_market_product(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/add_market_item.json", params, handler)
    }
    fun modify_item_info(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/update_market_item.json", params, handler)
    }

    fun delete_market_item(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/delete_market_item.json", params, handler)
    }//params : product_id, type == del


    fun get_market_product(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/get_second_hand_market_item.json", params, handler)
    }

    fun get_product_detail(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/get_product_detail.json", params, handler)
    }

    fun get_market_report(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/get_market_report.json", params, handler)
    }

    fun add_report(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/add_report.json", params, handler)
    }

    fun get_content_cnt(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/get_content_cnt.json", params, handler)
    }

    fun get_my_market_item(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/get_my_market_item.json", params, handler)
    }

    fun add_market_looker(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/add_market_looker.json", params, handler)
    }

    fun add_keyword(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/add_keyword.json", params, handler)
    }

    fun get_keyword(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/get_keyword.json", params, handler)
    }

    fun delete_keyword(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/delete_keyword.json", params, handler)
    }

    fun add_market_comment(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/add_market_comment.json", params, handler)
    }

    fun get_market_comment(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/get_market_comment.json", params, handler)
    }

    fun delete_market_comment(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/delete_market_comment.json", params, handler)
    }

    fun market_block_commenter(params: RequestParams, handler: JsonHttpResponseHandler) {
        HttpClient.post("/market/market_block_commenter.json", params, handler)
    }

}