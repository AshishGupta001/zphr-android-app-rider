package com.zphr.user;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.activity.ParentActivity;
import com.general.files.MyApp;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;

import java.util.HashMap;

public class StaticPageActivity extends ParentActivity {

    public String static_page_id = "1";
    public String staticData = "";
    public String vTitle = "";
    MTextView titleTxt;
    ProgressBar loading;
    ErrorView errorView;
    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_page);

        ImageView backImgView = findViewById(R.id.backImgView);
        backImgView.setOnClickListener(v -> onBackPressed());
        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        titleTxt = findViewById(R.id.titleTxt);
        loading = findViewById(R.id.loading);
        errorView = findViewById(R.id.errorView);
        container = findViewById(R.id.container);

        static_page_id = getIntent().getStringExtra("staticpage");
        staticData = getIntent().getStringExtra("staticData");
        vTitle = getIntent().getStringExtra("vTitle");
        setLabels();

        if (!Utils.checkText(staticData)) {
            loadAboutUsData();
        }
    }


    public void setLabels() {
        if (Utils.checkText(staticData)) {
            titleTxt.setText(vTitle);
            loadAboutUsDetail(staticData);
        } else {
            if (static_page_id.equalsIgnoreCase("1")) {
                titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ABOUT_US_HEADER_TXT"));

            } else if (static_page_id.equalsIgnoreCase("33")) {
                titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PRIVACY_POLICY_TEXT"));

            } else if (static_page_id.equals("4")) {
                titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TERMS_AND_CONDITION"));

            } else {
                titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DETAILS"));
            }
        }
    }

    private void loadAboutUsData() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "staticPage");
        parameters.put("iPageId", static_page_id);
        parameters.put("appType", Utils.app_type);
        parameters.put("iMemberId", generalFunc.getMemberId());
        if (generalFunc.getMemberId().equalsIgnoreCase("")) {
            parameters.put("vLangCode", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        }

        ApiHandler.execute(getActContext(), parameters, responseString -> {
            if (responseString != null && !responseString.equals("")) {
                String tPageDesc = generalFunc.getJsonValue("page_desc", responseString);
                loadAboutUsDetail(tPageDesc);
            } else {
                generateErrorView();
            }
        });

    }

    private void loadAboutUsDetail(String tPageDesc) {
        closeLoader();
        WebView view = new WebView(this);
        container.addView(view);
        MyApp.executeWV(view, generalFunc, tPageDesc);
    }

    private void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    private void generateErrorView() {
        closeLoader();
        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");
        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(this::loadAboutUsData);
    }

    private Context getActContext() {
        return StaticPageActivity.this;
    }
}
