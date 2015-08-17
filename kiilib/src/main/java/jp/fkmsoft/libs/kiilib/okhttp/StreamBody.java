package jp.fkmsoft.libs.kiilib.okhttp;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okio.BufferedSink;

/**
 * Implementation for stream request
 */
class StreamBody extends RequestBody {
    private MediaType mType;
    private InputStream mIn;

    public StreamBody(String contentType, InputStream in) {
        mType = MediaType.parse(contentType);
        mIn = in;
    }

    @Override
    public MediaType contentType() {
        return mType;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        OutputStream out = null;
        try {
            out = sink.outputStream();
            byte[] buffer = new byte[8192];
            int count;
            while (( count = mIn.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        } finally {
            if (mIn != null) { mIn.close(); }
            if (out != null) { out.close(); }
        }
    }
}
