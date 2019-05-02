package donggolf.android.base;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.nio.charset.Charset;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class HttpClient {
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

          System.out.println("GET : " + Config.url + url + "?" + params);
        if (params != null) {
            client.get(Config.url + url, params, responseHandler);
        } else {
            client.get(Config.url + url, responseHandler);
        }
    }

    public static void get2(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        System.out.println("GET2 : " + url + "?" + params);

        if (params != null) {
            client.get(url, params, responseHandler);
        } else {
            client.get(url, responseHandler);
        }
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        //System.out.println("POST : " + Config.url + url + "?" + params);

        client.setTimeout(60 * 1000);

        client.post(Config.url + url, params, responseHandler);
    }

    public static void post2(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        // System.out.println("POST : " + "http://13.124.13.37" + url + "?" + params);

        client.setTimeout(60 * 1000);

        //client.post("http://13.124.13.37" + url, params, responseHandler);
        client.post("http://13.125.241.200" + url, params, responseHandler);
    }

    public static void postJSON(Context context, String url, JSONObject jsonParams, AsyncHttpResponseHandler responseHandler) {

//         System.out.println("POST : " + "http://13.124.13.37" + url + "?" + params);

        client.setTimeout(60 * 1000);

        StringEntity entity = new StringEntity(jsonParams.toString(), Charset.forName("UTF-8"));
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

         System.out.println("params.toString() : " + jsonParams.toString());
         System.out.println("entity : " + entity);

        Header[] headers = new Header[1];
        headers[0] = new BasicHeader("Accept", "application/vnd.tosslab.jandi-v2+json");

        client.post(context, url, headers, entity,"application/json", responseHandler);
    }
}
