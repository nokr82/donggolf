package donggolf.android.actions;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import donggolf.android.base.HttpClient;

/**
 * Created by dev1 on 2017-02-17.
 */

public class VersionAction {

    // 버전 확인
    public static void version(JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("type", "android");
        HttpClient.post("/version.json", params, handler);
    }

}
