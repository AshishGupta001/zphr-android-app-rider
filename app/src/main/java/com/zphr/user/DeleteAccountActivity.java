package com.zphr.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.activity.ParentActivity;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.util.HashMap;

public class DeleteAccountActivity extends ParentActivity {

    private WebView mWebView;
    private AVLoadingIndicatorView mLoaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        ImageView backImgView = (ImageView) findViewById(R.id.backImgView);
        MTextView titleTxt = (MTextView) findViewById(R.id.titleTxt);

        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        addToClickHandler(backImgView);
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DELETE_ACCOUNT_TXT"));

        mWebView = (WebView) findViewById(R.id.deleteAccountWebView);
        mLoaderView = (AVLoadingIndicatorView) findViewById(R.id.deleteAccountLoaderView);
        mWebView.setVisibility(View.GONE);
        mLoaderView.setVisibility(View.GONE);

        accountDelete();
    }

    private Context getActContext() {
        return DeleteAccountActivity.this;
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        if (view.getId() == R.id.backImgView) {
            DeleteAccountActivity.super.onBackPressed();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void accountDelete() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "AccountDelete");
        parameters.put("iMemberId", generalFunc.getMemberId());

        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.toString().equals("")) {
                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj)) {
                    mWebView.setWebViewClient(new myWebClient());
                    mWebView.getSettings().setJavaScriptEnabled(true);
                    mWebView.loadUrl(generalFunc.getJsonValueStr("Url", responseStringObj));
                    mWebView.setFocusable(true);
                    mWebView.setVisibility(View.VISIBLE);
                    mLoaderView.setVisibility(View.VISIBLE);

                } else {
                    GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                    alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                    alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                    alertBox.setBtnClickList(btn_id -> finish());
                    alertBox.showAlertBox();
                }
            } else {
                generalFunc.showError();
            }
        });
    }

    private class myWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mLoaderView.setVisibility(View.VISIBLE);
            view.setOnTouchListener(null);

            if (url.contains("success=1&account_deleted=Yes")) {
                mWebView.setVisibility(View.GONE);
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_DELETED_SUCCESS_MSG")), "", generalFunc.retrieveLangLBl("", "LBL_OK"), i -> {
                    if (intCheck.isNetworkConnected()) {
                        MyApp.getInstance().logOutFromDevice(true);
                    } else {
                        generalFunc.showMessage(mLoaderView, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                    }
                });
            }

            if (url.contains("success=0")) {
                mWebView.setVisibility(View.GONE);

                String message;
                if (Utils.checkText(url) && url.contains("&message=")) {
                    String msg = GeneralFunctions.substringAfterLast(url, "&message=");
                    message = Utils.checkText(msg) ? msg.replaceAll("%20", " ") : "";
                } else {
                    message = generalFunc.retrieveLangLBl("", "LBL_REQUEST_FAILED_PROCESS");
                }
                generalFunc.showGeneralMessage("", message, "", generalFunc.retrieveLangLBl("", "LBL_OK"), i -> finish());
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            generalFunc.showError();
            mLoaderView.setVisibility(View.GONE);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onPageFinished(WebView view, String url) {
            mLoaderView.setVisibility(View.GONE);

            view.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            });
        }
    }
}