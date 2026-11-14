package app.arifnurrohim.linkding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class QuickSaveActivity extends Activity {
    private boolean saving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Prefs.isConfigured(this)) {
            toast("Connect a Linkding server first");
            finish();
            return;
        }

        Intent intent = getIntent();
        String url = Urls.extract(intent.getStringExtra(Intent.EXTRA_TEXT));
        if (url == null) {
            toast("No link found in the shared text");
            finish();
            return;
        }

        Bookmark bookmark = new Bookmark();
        bookmark.url = url;
        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (subject != null) bookmark.title = subject.trim();
        bookmark.unread = Prefs.quickUnread(this);
        bookmark.shared = Prefs.quickShared(this);
        bookmark.archived = Prefs.quickArchived(this);

        saving = true;
        Api.from(this).save(bookmark, true, new Api.Callback<Bookmark>() {
            @Override
            public void onResult(Api.Result<Bookmark> result) {
                saving = false;
                if (isFinishing() || isDestroyed()) return;
                if (result.ok) {
                    toast("Saved to " + Prefs.host(QuickSaveActivity.this));
                } else {
                    toast(result.unauthorized() ? "Invalid API token" : result.error);
                }
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (saving) return;
        super.onBackPressed();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
