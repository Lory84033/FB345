package app.friendbook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Friendbook: una WebView a schermo intero.
 * - Mostra il sito incluso nell'app (assets/www) oppure, se compilato in res/values/config.xml, un indirizzo web.
 * - Salva l'accesso (memoria locale della WebView) cosi' al prossimo avvio sei gia' dentro.
 * - Fa funzionare il pulsante "Foto" (selettore immagini) e il tasto Indietro del telefono.
 */
public class MainActivity extends Activity {

    private static final String ASSET_HOME = "file:///android_asset/www/index.html";
    private static final int REQ_PICK_IMAGE = 1001;

    private WebView web;
    private String startUrl = "";
    private ValueCallback<Uri> legacyCallback;     // Android < 5
    private ValueCallback<Uri[]> modernCallback;   // Android 5+

    @SuppressLint({"SetJavaScriptEnabled", "deprecation"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startUrl = getString(R.string.start_url).trim();
        if (!(startUrl.startsWith("http://") || startUrl.startsWith("https://"))) {
            startUrl = "";
        }

        web = new WebView(this);
        web.setBackgroundColor(0xFFE9EAED);
        web.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // necessario per ricordare l'accesso
        s.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT < 19) {
            s.setDatabasePath(getDir("database", MODE_PRIVATE).getPath());
        }
        s.setAllowFileAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);   // il sito incluso nell'app parla con Firebase
        s.setUseWideViewPort(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        CookieManager.getInstance().setAcceptCookie(true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url == null) {
                    return false;
                }
                if (url.startsWith("file:") || (startUrl.length() > 0 && url.startsWith(startUrl))) {
                    return false;
                }
                if (url.startsWith("http:") || url.startsWith("https:")
                        || url.startsWith("mailto:") || url.startsWith("tel:")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (ActivityNotFoundException e) {
                        // nessuna app per aprire il link
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Se l'indirizzo web non si apre (niente rete), usa il sito incluso nell'app.
                if (startUrl.length() > 0 && failingUrl != null && failingUrl.equals(startUrl)) {
                    view.loadUrl(ASSET_HOME);
                }
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            // Android 3.0
            public void openFileChooser(ValueCallback<Uri> callback) {
                legacyCallback = callback;
                pickImage();
            }

            // Android 3.0+
            public void openFileChooser(ValueCallback<Uri> callback, String acceptType) {
                legacyCallback = callback;
                pickImage();
            }

            // Android 4.1+
            public void openFileChooser(ValueCallback<Uri> callback, String acceptType, String capture) {
                legacyCallback = callback;
                pickImage();
            }

            // Android 5.0+
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (modernCallback != null) {
                    modernCallback.onReceiveValue(null);
                }
                modernCallback = callback;
                pickImage();
                return true;
            }
        });

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(startUrl.length() > 0 ? startUrl : ASSET_HOME);
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        try {
            startActivityForResult(Intent.createChooser(intent, "Scegli una foto"), REQ_PICK_IMAGE);
        } catch (ActivityNotFoundException e) {
            deliverImage(null);
        }
    }

    private void deliverImage(Uri uri) {
        if (modernCallback != null) {
            modernCallback.onReceiveValue(uri != null ? new Uri[]{uri} : null);
            modernCallback = null;
        }
        if (legacyCallback != null) {
            legacyCallback.onReceiveValue(uri);
            legacyCallback = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_PICK_IMAGE) {
            Uri uri = (resultCode == RESULT_OK && data != null) ? data.getData() : null;
            deliverImage(uri);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (web != null) {
            web.saveState(outState);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) {
            web.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (web != null) {
            web.onPause();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().flush();
        }
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
