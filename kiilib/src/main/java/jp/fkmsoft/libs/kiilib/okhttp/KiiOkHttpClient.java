package jp.fkmsoft.libs.kiilib.okhttp;

import android.os.Handler;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import jp.fkmsoft.libs.kiilib.client.KiiHTTPClient;
import jp.fkmsoft.libs.kiilib.entities.KiiContext;

/**
 * OkHttp Implementation
 */
public class KiiOkHttpClient implements KiiHTTPClient {

    private final OkHttpClient mClient;
    private final KiiContext mContext;
    private final Handler mHandler;

    public KiiOkHttpClient(OkHttpClient client, KiiContext context, Handler handler) {
        mClient = client;
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void sendJsonRequest(int method, String url, String token,
                                String contentType,
                                Map<String, String> headers, JSONObject body,
                                final ResponseHandler handler) {
        Request.Builder builder = new Request.Builder()
                .url(url);
        switch (method) {
        case Method.GET:
            builder.get();
            break;
        case Method.POST:
            builder.post(RequestBody.create(MediaType.parse(contentType + ";charset=utf-8"), body.toString()));
            break;
        case Method.PUT:
            builder.put(RequestBody.create(MediaType.parse(contentType + ";charset=utf-8"), body.toString()));
            break;
        case Method.DELETE:
            builder.delete();
            break;
        }
        setHeader(builder, headers, token);
        sendRequest(builder.build(), handler);
    }

    @Override
    public void sendPlainTextRequest(int method, String url, String token,
                                     Map<String, String> headers, String body, final ResponseHandler handler) {
        Request.Builder builder = new Request.Builder()
                .url(url);
        switch (method) {
        case Method.GET:
            builder.get();
            break;
        case Method.POST:
            builder.post(RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), body));
            break;
        case Method.PUT:
            builder.put(RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), body));
            break;
        case Method.DELETE:
            builder.delete();
            break;
        }
        setHeader(builder, headers, token);
        sendRequest(builder.build(), handler);
    }

    @Override
    public void sendStreamRequest(int method, String url, String token, String contentType,
                                  Map<String, String> headers, InputStream body,
                                  final ResponseHandler handler) {
        Request.Builder builder = new Request.Builder()
                .url(url);
        switch (method) {
        case Method.GET:
            builder.get();
            break;
        case Method.POST:
            builder.post(new StreamBody(contentType, body));
            break;
        case Method.PUT:
            builder.put(new StreamBody(contentType, body));
            break;
        case Method.DELETE:
            builder.delete();
            break;
        }
        setHeader(builder, headers, token);
        sendRequest(builder.build(), handler);
    }

    // region private

    private void setHeader(Request.Builder builder, Map<String, String> headers, String token) {
        builder.addHeader("x-kii-appid", mContext.getAppId());
        builder.addHeader("x-kii-appkey", mContext.getAppKey());
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        if (token != null) {
            builder.addHeader("authorization", "bearer " + token);
        }
    }

    private void sendRequest(Request request, final ResponseHandler handler) {
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    int status = response.code();
                    String body = response.body().string();

                    JSONObject json = null;
                    if (status != 204) {
                        json = new JSONObject(body);
                    }
                    String etag = response.header("ETag", "").replaceAll("\"", "");
                    deliverResponse(status, json, etag, handler);
                } catch (Exception e) {
                    deliverError(e, handler);
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                deliverError(e, handler);
            }
        });
    }

    private void deliverResponse(final int status, final JSONObject response, final String etag, final ResponseHandler handler) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                handler.onResponse(status, response, etag);
            }
        });
    }

    private void deliverError(final Exception e, final ResponseHandler handler) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                handler.onException(e);
            }
        });
    }

    // endregion
}
