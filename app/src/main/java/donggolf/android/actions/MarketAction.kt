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

}