package com.outsystems.sumnisdk;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import org.json.JSONObject;


/**
 * Created by zhicheng.liu on 2018/4/2
 * address :liuzhicheng@sunmi.com
 * description :
 */

public abstract class BasePresentation extends Presentation implements View.OnClickListener {
    private static final String TAG = "BasePresentation";
    public boolean isShow;
    int index;
    public View.OnClickListener onClickListener;
    public BasePresentationHelper helper = BasePresentationHelper.getInstance();

    public BasePresentation(Context outerContext, Display display) {
        super(outerContext, display);
        index = helper.add(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setOnClickListener(@Nullable View.OnClickListener l) {
        onClickListener = l;
    }


    @Override
    public void hide() {
        super.hide();
        helper.hide(this);
        isShow = false;
        Log.i(TAG, "hide");
    }

    @Override
    public void show() {
//        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        super.show();
        Log.i(TAG, "show");
        helper.show(index);
        isShow = true;
    }

    @Override
    public void dismiss() {
        isShow = false;
        Log.i(TAG, "dismiss");
        super.dismiss();
    }


    @Override
    public void onClick(View v) {
        if(onClickListener!=null){
            onClickListener.onClick(v);
        }
    }

    public abstract  void onSelect(boolean isShow);
}
