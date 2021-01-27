package com.outsystems.sumnisdk.present;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.outsystems.sumnisdk.BasePresentation;
import com.outsystems.sumnisdk.utils.ScreenManager;

import com.outsystems.sample.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by highsixty on 2018/3/23.
 * mail  gaolulin@sunmi.com
 */

public class TextDisplay extends BasePresentation {

    private LinearLayout root;
    private TextView tvTitle;
    private TextView tv;
    private LinearLayout llPresentChoosePayMode;
    private LinearLayout llPresentInfo;
    private TextView tvPaySuccess;
    private TextView paymodeOne;
    private TextView paymodeTwo;
    private TextView paymodeThree;
    private ImageView ivTitle;
    private ProgressBar presentProgress;


    private LinearLayout llPresentPayFail;
    private TextView presentFailOne;
    private TextView presentFailTwo;
    private TextView presentFailThree;
    public int state;

    public TextDisplay(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ScreenManager.getInstance().isMinScreen()) {
            setContentView(R.layout.vice_text_min_layout);

        }else {
            setContentView(R.layout.vice_text_layout);
        }

        root = findViewById(R.id.root);
        tvTitle = findViewById(R.id.tv_title);
        tv = findViewById(R.id.tv);
        llPresentChoosePayMode = findViewById(R.id.ll_present_choose_pay_mode);
        tvPaySuccess = findViewById(R.id.tv_pay_success);
        paymodeOne = findViewById(R.id.paymode_one);
        paymodeTwo = findViewById(R.id.paymode_two);
        paymodeThree = findViewById(R.id.paymode_three);
        ivTitle = findViewById(R.id.iv_title);
        llPresentInfo = findViewById(R.id.ll_present_info);


        llPresentPayFail = findViewById(R.id.ll_present_pay_fail);
        presentFailOne = findViewById(R.id.present_fail_one);
        presentFailTwo = findViewById(R.id.present_fail_two);
        presentFailThree = findViewById(R.id.present_fail_three);


        paymodeOne.setOnClickListener(this);
        paymodeTwo.setOnClickListener(this);
        paymodeThree.setOnClickListener(this);

        presentFailOne.setOnClickListener(this);
        presentFailTwo.setOnClickListener(this);
        presentFailThree.setOnClickListener(this);


        presentProgress = findViewById(R.id.present_progress);


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


    public void update(JSONObject jsonObject, final int state) throws JSONException {
        this.state = state;
        llPresentPayFail.setVisibility(View.GONE);
        presentProgress.setVisibility(View.GONE);

        if (jsonObject.has("textViewPayment1") && jsonObject.has("textViewPayment2") && jsonObject.has("textViewPayment3")){
            paymodeOne.setText(jsonObject.getString("textViewPayment1"));
            paymodeTwo.setText(jsonObject.getString("textViewPayment2"));
            paymodeThree.setText(jsonObject.getString("textViewPayment3"));
        }
        if (jsonObject.has("textViewFail1") && jsonObject.has("textViewFail2") && jsonObject.has("textViewFail3")){
            presentFailOne.setText(jsonObject.getString("textViewFail1"));
            presentFailOne.setText(jsonObject.getString("textViewFail2"));
            presentFailThree.setText(jsonObject.getString("textViewFail3"));
        }

        //iv_title
        switch (state) {
            case 0:
                llPresentInfo.setVisibility(View.VISIBLE);
                tvPaySuccess.setVisibility(View.GONE);
                llPresentChoosePayMode.setVisibility(View.VISIBLE);
                root.setBackgroundResource(R.drawable.present_bg_text1);
                ivTitle.setImageResource(R.drawable.present_pay_iv1);

                setSelect(0);
                tvTitle.setText(jsonObject.getString("title"));//strings[0].replace(":", "")
                tv.setText(jsonObject.getString("textView1"));//zoomString(unit + strings[1]));
                tv.setTextSize(ScreenManager.getInstance().isMinScreen()?136:68);
                break;
            case 1:
                tvPaySuccess.setVisibility(View.VISIBLE);
                llPresentChoosePayMode.setVisibility(View.GONE);
                root.setBackgroundResource(R.drawable.present_bg_text2);
                ivTitle.setImageResource(R.drawable.present_pay_iv2);


                tvTitle.setText(jsonObject.getString("title"));//strings[0].replace(":", ""));
                tvPaySuccess.setText(jsonObject.getString("textView2"));

                tv.setText(jsonObject.getString("textView1"));//zoomString(unit + strings[1]));
                tv.setTextSize(ScreenManager.getInstance().isMinScreen()?136:68);
                playAnim();

                break;
            case 2:

                llPresentInfo.setVisibility(View.GONE);
                root.setBackgroundResource(R.drawable.present_bg_text3);
                ivTitle.setImageResource(R.drawable.present_pay_iv3);
                tvTitle.setText(jsonObject.getString("title"));

                tv.setText(jsonObject.getString("textView1"));//tip);
                tv.setTextSize(ScreenManager.getInstance().isMinScreen()?90:45);
                break;
            case 3:
                tvTitle.setText(jsonObject.getString("title"));
                tv.setText(jsonObject.getString("textView1"));//zoomString(tip));

                presentProgress.setVisibility(View.VISIBLE);
                tvPaySuccess.setVisibility(View.VISIBLE);
                llPresentChoosePayMode.setVisibility(View.GONE);
                llPresentPayFail.setVisibility(View.GONE);

                root.setBackgroundResource(R.drawable.present_bg_text1);
                ivTitle.setImageResource(R.drawable.present_pay_iv1);

                tvPaySuccess.setText(jsonObject.getString("textView1"));
                break;
            case 4:
                tvTitle.setText(jsonObject.getString("title"));
                tv.setText(jsonObject.getString("textView1"));//zoomString(tip));

                llPresentInfo.setVisibility(View.VISIBLE);
                presentProgress.setVisibility(View.GONE);
                tvPaySuccess.setVisibility(View.GONE);
                llPresentChoosePayMode.setVisibility(View.GONE);
                llPresentPayFail.setVisibility(View.VISIBLE);

                setSelect(0);

                root.setBackgroundResource(R.drawable.present_bg_text4);
                ivTitle.setImageResource(R.drawable.present_pay_iv4);
                break;
        }

    }


    private SpannableString zoomString(String strings){
        SpannableString ss = new SpannableString(strings);
        ss.setSpan(new RelativeSizeSpan(0.65f), 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE); // set size
        return  ss;
    }

    void playAnim(){
        AnimationDrawable animationDrawable = (AnimationDrawable) ivTitle.getDrawable();
        animationDrawable.start();
    }


    public void setSelect(int index) {
        paymodeOne.setSelected(index == 0 ? true : false);
        paymodeTwo.setSelected(index == 1 ? true : false);
        paymodeThree.setSelected(index == 2 ? true : false);

    }

    @Override
    public void show() {
        super.show();
        int payMode = 0;//(int) SharePreferenceUtil.getParam(getContext(), PayDialog.PAY_MODE_KEY, 7);
        switch (payMode) {
            case 0:
                paymodeOne.setVisibility(View.GONE);
                paymodeTwo.setVisibility(View.GONE);
                paymodeThree.setVisibility(View.VISIBLE);
                presentFailTwo.setVisibility(View.GONE);
                break;
            case 1:
                paymodeOne.setVisibility(View.GONE);
                paymodeTwo.setVisibility(View.VISIBLE);
                paymodeThree.setVisibility(View.VISIBLE);
                presentFailTwo.setVisibility(View.VISIBLE);

                break;

            case 2:
                paymodeOne.setVisibility(View.VISIBLE);
                paymodeTwo.setVisibility(View.VISIBLE);
                paymodeThree.setVisibility(View.VISIBLE);
                presentFailTwo.setVisibility(View.VISIBLE);

                break;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.paymode_one:
                setSelect(0);
                break;
            case R.id.paymode_two:
                setSelect(1);
                break;
            case R.id.paymode_three:
                setSelect(2);
                break;
            case R.id.present_fail_one:

                break;
            case R.id.present_fail_two:
                break;
            case R.id.present_fail_three:
                break;


        }

    }

    @Override
    public void onSelect(boolean isShow) {

    }
}
