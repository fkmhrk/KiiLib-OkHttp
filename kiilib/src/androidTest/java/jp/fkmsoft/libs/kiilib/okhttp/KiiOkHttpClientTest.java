package jp.fkmsoft.libs.kiilib.okhttp;

import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import jp.fkmsoft.libs.kiilib.client.KiiHTTPClient;
import jp.fkmsoft.libs.kiilib.entities.KiiContext;
import jp.fkmsoft.libs.kiilib.entities.test.TestKiiContext;

/**
 * Testcase
 */
public class KiiOkHttpClientTest extends AndroidTestCase {
    private HandlerThread mHandlerThread;
    private KiiContext mKiiContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mHandlerThread = new HandlerThread("Mock UI");
        mHandlerThread.start();

        mKiiContext = new TestKiiContext(Constants.APP_ID, Constants.APP_KEY, KiiContext.SITE_JP);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mHandlerThread.quit();
    }

    public void test_0000_POST() throws Exception {
        Handler handler = new Handler(mHandlerThread.getLooper());

        KiiOkHttpClient client = new KiiOkHttpClient(new OkHttpClient(), mKiiContext, handler);

        Map<String, String> headers = new HashMap<>();
        JSONObject params = new JSONObject();
        params.put("username", "fkmtest");
        params.put("password", "123456");

        final Map<String, Object> result = new HashMap<>();
        final CountDownLatch latch = new CountDownLatch(1);

        client.sendJsonRequest(KiiHTTPClient.Method.POST, "https://api-jp.kii.com/api/oauth2/token",
                null, "application/json", headers, params, new KiiHTTPClient.ResponseHandler() {
                    @Override
                    public void onResponse(int status, JSONObject body, String etag) {
                        result.put("status", status);
                        result.put("body", body);
                        result.put("etag", etag);
                        latch.countDown();
                    }

                    @Override
                    public void onException(Exception e) {
                        result.put("exception", e);
                        latch.countDown();
                    }
                });
        latch.await();
        // assertion
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("body"));
        assertTrue(result.containsKey("etag"));
        JSONObject resp1 = (JSONObject) result.get("body");
        String token = resp1.getString("access_token");
        assertNotNull(token);

        // create object request

        JSONObject objBody = new JSONObject();
        objBody.put("score", 100);

        final CountDownLatch latch2 = new CountDownLatch(1);
        result.clear();

        client.sendJsonRequest(KiiHTTPClient.Method.POST, "https://api-jp.kii.com/api/apps/" + Constants.APP_ID +
                "/users/me/buckets/" + Constants.BUCKET_NAME + "/objects", token, "application/json", headers, objBody, new KiiHTTPClient.ResponseHandler() {
            @Override
            public void onResponse(int status, JSONObject body, String etag) {
                result.put("status", status);
                result.put("body", body);
                result.put("etag", etag);
                latch2.countDown();
            }

            @Override
            public void onException(Exception e) {
                result.put("exception", e);
                latch2.countDown();
            }
        });

        latch2.await();
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("body"));
        assertTrue(result.containsKey("etag"));
        String etag = (String) result.get("etag");
        assertEquals("1", etag);
        objBody = (JSONObject) result.get("body");
        String objectId = objBody.getString("objectID");

        // add object body
        ByteArrayInputStream in = new ByteArrayInputStream("text".getBytes());
        final CountDownLatch latch3 = new CountDownLatch(1);
        result.clear();

        client.sendStreamRequest(KiiHTTPClient.Method.PUT, "https://api-jp.kii.com/api/apps/" + Constants.APP_ID +
                "/users/me/buckets/" + Constants.BUCKET_NAME + "/objects/" + objectId + "/body", token, "text/plain", headers, in, new KiiHTTPClient.ResponseHandler() {
            @Override
            public void onResponse(int status, JSONObject body, String etag) {
                result.put("status", status);
                result.put("body", body);
                result.put("etag", etag);
                latch3.countDown();
            }

            @Override
            public void onException(Exception e) {
                result.put("exception", e);
                latch3.countDown();
            }
        });

        latch3.await();
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("body"));
        assertTrue(result.containsKey("etag"));
        assertEquals(200, (int) result.get("status"));
        objBody = (JSONObject) result.get("body");
        assertTrue(objBody.has("modifiedAt"));

        // delete bucket
        final CountDownLatch latch4 = new CountDownLatch(1);
        result.clear();

        client.sendJsonRequest(KiiHTTPClient.Method.DELETE, "https://api-jp.kii.com/api/apps/" + Constants.APP_ID +
                "/users/me/buckets/" + Constants.BUCKET_NAME, token, null, headers, null, new KiiHTTPClient.ResponseHandler() {
            @Override
            public void onResponse(int status, JSONObject body, String etag) {
                result.put("status", status);
                result.put("body", body);
                result.put("etag", etag);
                latch4.countDown();
            }

            @Override
            public void onException(Exception e) {
                result.put("exception", e);
                latch4.countDown();
            }
        });

        latch4.await();
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("body"));
        assertTrue(result.containsKey("etag"));
    }
}