package com.outsystems.sumnisdk.utils;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WebviewReadWrite {
    private Context context;

    public WebviewReadWrite(Context context){
        this.context=context;

    }

    @JavascriptInterface
    public void saveDataJs(String jsonStr){
        SharePreferenceUtil.setParam(context,"webviewData",jsonStr);
    }

    @JavascriptInterface
    public String readDataJs(){
        return SharePreferenceUtil.getParam(context,"webviewData");
    }
}
