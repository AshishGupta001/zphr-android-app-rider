package com.zphr.user;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.ImageView;

import com.activity.ParentActivity;
import com.general.files.ActUtils;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.MTextView;

public class QuestionAnswerActivity extends ParentActivity {

    MTextView titleTxt, vQuestion, vAnswer, contact_us_btn, textstillneedhelp;
    WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_answer);

        ImageView backImgView = findViewById(R.id.backImgView);
        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        titleTxt = findViewById(R.id.titleTxt);
        vQuestion = findViewById(R.id.vQuestion);
        clearCookies(getApplication());
        webView = findViewById(R.id.webView);

        vAnswer = findViewById(R.id.vAnswer);
        contact_us_btn = findViewById(R.id.contact_us_btn);
        textstillneedhelp = findViewById(R.id.textstillneedhelp);
        if (getIntent().getStringExtra("QUESTION") != null) {
            vQuestion.setText(Html.fromHtml(getIntent().getStringExtra("QUESTION") + ""));
            vAnswer.setVisibility(View.GONE);
            MyApp.executeWV(webView, generalFunc, getIntent().getStringExtra("ANSWER"));
        }
        titleTxt.setText(generalFunc.retrieveLangLBl("FAQ", "LBL_FAQ_TXT"));
        addToClickHandler(backImgView);
        addToClickHandler(contact_us_btn);
        setData();
    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d("Api", "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.d("Api", "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    public void setData() {

        contact_us_btn.setText("" + generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_HEADER_TXT"));
        textstillneedhelp.setText("" + generalFunc.retrieveLangLBl("", "LBL_STILL_NEED_HELP"));

    }

    private Context getActContext() {
        return QuestionAnswerActivity.this;
    }


    public void onClick(View view) {
        Utils.hideKeyboard(getActContext());
        switch (view.getId()) {
            case R.id.backImgView:
                QuestionAnswerActivity.super.onBackPressed();
                break;
            case R.id.contact_us_btn:
                new ActUtils(getActContext()).startAct(ContactUsActivity.class);
                break;
        }
    }

}