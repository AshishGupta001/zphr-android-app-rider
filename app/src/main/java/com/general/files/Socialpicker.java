package com.general.files;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zphr.user.MobileStegeActivity;
import com.zphr.user.R;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.utils.Utils;
import com.view.MTextView;

import java.util.Arrays;

public class Socialpicker {

    static BottomSheetDialog optionDailog;
    static GeneralFunctions generalFunc;
    static LinearLayout facebookArea, linkdinArea, googleArea, smartLoginArea, smartLoginDisableArea;
    static ImageView closeImg;
    static Context mContext;
    static InternetConnection intCheck;
    static LoginButton loginButton;
    public static CallbackManager callbackManager;
    static GoogleSignInClient mGoogleApiClient;
    static final int RC_SIGN_IN = 001;

    public static void build(Context mContext, boolean isSamertLogin) {

        Socialpicker.mContext = mContext;
        generalFunc = new GeneralFunctions(mContext);
        optionDailog = new BottomSheetDialog(mContext);
        intCheck = new InternetConnection(mContext);
        View contentView = View.inflate(mContext, R.layout.layout_socialpicker, null);
        if (generalFunc.isRTLmode()) {
            contentView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        optionDailog.setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(400, mContext)));
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) contentView.getParent());
        mBehavior.setPeekHeight(Utils.dpToPx(400, mContext));
        optionDailog.setCancelable(false);
        googleArea = contentView.findViewById(R.id.googleArea);
        linkdinArea = contentView.findViewById(R.id.linkdinArea);
        facebookArea = contentView.findViewById(R.id.facebookArea);
        smartLoginArea = contentView.findViewById(R.id.smartLoginArea);
        smartLoginDisableArea = contentView.findViewById(R.id.smartLoginDisableArea);
        closeImg = contentView.findViewById(R.id.closeImg);
        MTextView titleTxt = contentView.findViewById(R.id.titleTxt);
        MTextView smartLoginHtxt = contentView.findViewById(R.id.smartLoginHtxt);
        MTextView fbHTxt = contentView.findViewById(R.id.fbHTxt);
        MTextView linkedinHtxt = contentView.findViewById(R.id.linkedinHtxt);
        MTextView googleHtxt = contentView.findViewById(R.id.googleHtxt);
        MTextView smartLoginDisableHtxt = contentView.findViewById(R.id.smartLoginDisableHtxt);
        MTextView smartLoginDisableNoteTxt = contentView.findViewById(R.id.smartLoginDisableNoteTxt);
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHOOSE_ACCOUNT"));
        smartLoginHtxt.setText(generalFunc.retrieveLangLBl("", "LBL_SMART_LOGIN_APPLOGIN"));
        fbHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FACEBOOK_TXT"));
        linkedinHtxt.setText(generalFunc.retrieveLangLBl("", "LBL_LINKEDIN_TXT"));
        googleHtxt.setText(generalFunc.retrieveLangLBl("", "LBL_GOOGLE_TXT"));
        smartLoginDisableHtxt.setText(generalFunc.retrieveLangLBl("", "LBL_QUICK_LOGIN"));
        smartLoginDisableNoteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SMART_LOGIN_ACTIVATION_MSG"));
        facebookArea.setOnClickListener(new setOnClickList());
        googleArea.setOnClickListener(new setOnClickList());
        linkdinArea.setOnClickListener(new setOnClickList());
        closeImg.setOnClickListener(new setOnClickList());
        smartLoginArea.setOnClickListener(new setOnClickList());
        closeImg.setOnClickListener(new setOnClickList());

        //fb
        loginButton = new LoginButton(mContext);

        callbackManager = CallbackManager.Factory.create();

        loginButton.setPermissions(Arrays.asList("public_profile", "email"));

        loginButton.registerCallback(callbackManager, new RegisterFbLoginResCallBack(mContext, callbackManager));

        //google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient = GoogleSignIn.getClient(mContext, gso);

        ((View) contentView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        Animation a = AnimationUtils.loadAnimation(mContext, R.anim.bottom_up);
        a.reset();
        contentView.clearAnimation();
        contentView.startAnimation(a);

        if (generalFunc.retrieveValue(Utils.FACEBOOK_LOGIN).equalsIgnoreCase("NO")) {
            facebookArea.setVisibility(View.GONE);
        }
        if (generalFunc.retrieveValue(Utils.GOOGLE_LOGIN).equalsIgnoreCase("NO")) {
            googleArea.setVisibility(View.GONE);
        }
        if (generalFunc.retrieveValue(Utils.LINKDIN_LOGIN).equalsIgnoreCase("NO")) {
            linkdinArea.setVisibility(View.GONE);
        }
        if (generalFunc.retrieveValue("isSmartLoginEnable").equalsIgnoreCase("Yes")) {
            if (generalFunc.retrieveValue("isUserSmartLogin") != null && generalFunc.retrieveValue("isUserSmartLogin").equalsIgnoreCase("yes") && isSamertLogin) {
                smartLoginArea.setVisibility(View.VISIBLE);
                smartLoginDisableArea.setVisibility(View.GONE);
            } else {
                smartLoginArea.setVisibility(View.GONE);
                smartLoginDisableArea.setVisibility(View.VISIBLE);
            }
        } else {
            smartLoginArea.setVisibility(View.GONE);
            smartLoginDisableArea.setVisibility(View.GONE);
        }
        optionDailog.show();
    }

    private static class setOnClickList implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            optionDailog.dismiss();
            if (i == facebookArea.getId()) {
                if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                    generalFunc.showError();
                } else {
                    loginButton.performClick();
                }
            } else if (i == googleArea.getId()) {
                if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                    generalFunc.showError();
                } else {
                    GoogleSignIn.getClient(mContext, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                    Intent signInIntent = mGoogleApiClient.getSignInIntent();
                    MyApp.getInstance().getCurrentAct().startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            } else if (i == linkdinArea.getId()) {
                if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                    generalFunc.showError();
                } else {
                    RegisterLinkedinLoginResCallBack registerLinkedinLoginResCallBack = new RegisterLinkedinLoginResCallBack(mContext);
                    registerLinkedinLoginResCallBack.continueLogin();
                }
            } else if (i == smartLoginArea.getId()) {
                MobileStegeActivity.mSmartLogin.clickEvent();
            } else if (i == closeImg.getId()) {
                optionDailog.dismiss();
            }
        }
    }
}