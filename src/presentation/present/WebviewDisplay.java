package com.outsystems.sumnisdk.present;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.outsystems.sumnisdk.BasePresentation;
import com.outsystems.sumnisdk.utils.ScreenManager;

import com.outsystems.sample.R;
import com.outsystems.sumnisdk.utils.WebviewReadWrite;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ServiceLoader;

/**
 * Created by highsixty on 2018/3/23.
 * mail  gaolulin@sunmi.com
 */

public class WebviewDisplay extends BasePresentation {

    private LinearLayout root;
    private WebView mWebview;
    private String homeUrl;
    private Context context;
    private CallbackContext callbackContext;

    public WebviewDisplay(Context outerContext, Display display, String url, CallbackContext callbackContext) {
        super(outerContext, display);
        homeUrl = url;
        context=outerContext;
        this.callbackContext = callbackContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vice_webview_layout);

        root = findViewById(R.id.root);
        mWebview = findViewById(R.id.wv_display);
        mWebview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebview.getSettings().setAllowFileAccess(true);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setDatabaseEnabled(true);
        mWebview.getSettings().setAppCacheEnabled(true);
        mWebview.getSettings().setAppCachePath(context.getCacheDir().getAbsolutePath());
        mWebview.getSettings().setDomStorageEnabled(true);

        mWebview.addJavascriptInterface(new WebviewReadWrite(context), "injectedObject");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            root.setClipToOutline(true);
            root.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 15);
                }
            });
        }
    }

    public void updateContext(CallbackContext callbackContext){
        this.callbackContext = callbackContext;
    }

    /*public String readData(){

    }*/

    public void update(String data){
    }



    @Override
    public void show() {
        super.show();
        mWebview.loadUrl(homeUrl);

    }

    @Override
    public void onSelect(boolean isShow) {

    }
    @Override
    public void onBackPressed() {
        mWebview.requestFocus();
        if (mWebview.canGoBack()) {
            mWebview.goBack();
        }
    }


    public void loadUrl(String key) {

    }


    public void reLoad() {
        mWebview.reload();
    }
}
