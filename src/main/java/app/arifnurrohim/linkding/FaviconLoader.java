package app.arifnurrohim.linkding;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FaviconLoader {
    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(96);
    private static final ExecutorService POOL = Executors.newFixedThreadPool(2);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private FaviconLoader() {
    }

    public static void clear() {
        CACHE.evictAll();
    }

    public static void load(final ImageView view, String bookmarkUrl) {
        String host = host(bookmarkUrl);
        view.setTag(host);
        if (host == null) {
            view.setImageDrawable(null);
            return;
        }
        Bitmap cached = CACHE.get(host);
        if (cached != null) {
            view.setImageBitmap(cached);
            return;
        }
        view.setImageDrawable(null);
        final String domain = host;
        POOL.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = download(domain);
                if (bitmap != null) CACHE.put(domain, bitmap);
                MAIN.post(new Runnable() {
                    @Override
                    public void run() {
                        if (domain.equals(view.getTag())) {
                            view.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        });
    }

    private static Bitmap download(String domain) {
        HttpURLConnection connection = null;
        try {
            String request = "https://www.google.com/s2/favicons?sz=64&domain="
                    + URLEncoder.encode(domain, "UTF-8");
            connection = (HttpURLConnection) new URL(request).openConnection();
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(6000);
            connection.setRequestProperty("User-Agent", "LinkDing-Android/1.0");
            InputStream stream = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
            return bitmap;
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String host(String url) {
        try {
            String host = Uri.parse(url).getHost();
            if (host == null || host.isEmpty()) return null;
            return host;
        } catch (Exception ignored) {
            return null;
        }
    }
}
