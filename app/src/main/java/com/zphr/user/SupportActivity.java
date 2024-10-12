package com.zphr.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activity.ParentActivity;
import com.general.files.ActUtils;
import com.livechatinc.inappchat.ChatWindowActivity;
import com.utils.Utils;
import com.view.MTextView;

import java.util.HashMap;

public class SupportActivity extends ParentActivity {


    MTextView titleTxt;
    ImageView backImgView;


    LinearLayout aboutusarea, privacyarea, contactarea, helparea, termsCondArea, chatarea;

    MTextView helpHTxt, contactHTxt, privacyHTxt, aboutusHTxt, termsHTxt, livechatHTxt;

    View seperationLine, seperationLine_contact, seperationLine_help, chatlineView;

    boolean islogin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        initView();
        setLabel();
        islogin = getIntent().getBooleanExtra("islogin", false);
        if (islogin) {
            aboutusarea.setVisibility(View.GONE);
            contactarea.setVisibility(View.GONE);
            helparea.setVisibility(View.GONE);
            seperationLine_help.setVisibility(View.GONE);
            seperationLine_contact.setVisibility(View.GONE);
            seperationLine.setVisibility(View.GONE);
            chatarea.setVisibility(View.GONE);
            chatlineView.setVisibility(View.GONE);

        }
    }


    private void initView() {


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        addToClickHandler(backImgView);
        helpHTxt = (MTextView) findViewById(R.id.helpHTxt);
        contactHTxt = (MTextView) findViewById(R.id.contactHTxt);
        privacyHTxt = (MTextView) findViewById(R.id.privacyHTxt);
        aboutusHTxt = (MTextView) findViewById(R.id.aboutusHTxt);
        termsHTxt = (MTextView) findViewById(R.id.termsHTxt);
        livechatHTxt = (MTextView) findViewById(R.id.livechatHTxt);

        aboutusarea = (LinearLayout) findViewById(R.id.aboutusarea);
        privacyarea = (LinearLayout) findViewById(R.id.privacyarea);
        contactarea = (LinearLayout) findViewById(R.id.contactarea);
        helparea = (LinearLayout) findViewById(R.id.helparea);
        termsCondArea = (LinearLayout) findViewById(R.id.termsCondArea);
        chatarea = (LinearLayout) findViewById(R.id.chatarea);

        seperationLine = (View) findViewById(R.id.seperationLine);
        seperationLine_contact = (View) findViewById(R.id.seperationLine_contact);
        seperationLine_help = (View) findViewById(R.id.seperationLine_help);
        chatlineView = (View) findViewById(R.id.chatlineView);
        addToClickHandler(aboutusarea);
        addToClickHandler(privacyarea);
        addToClickHandler(contactarea);
        addToClickHandler(helparea);
        addToClickHandler(termsCondArea);
        addToClickHandler(chatarea);

        if (generalFunc.getJsonValueStr("ENABLE_LIVE_CHAT", obj_userProfile).equalsIgnoreCase("Yes")) {
            chatarea.setVisibility(View.VISIBLE);
            chatlineView.setVisibility(View.VISIBLE);

        }


    }

    public Context getActContext() {
        return SupportActivity.this;
    }


    private void setLabel() {

        helpHTxt.setText(generalFunc.retrieveLangLBl("FAQ", "LBL_FAQ_TXT"));
        contactHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
        privacyHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PRIVACY_POLICY_TEXT"));
        aboutusHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ABOUT_US_TXT"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUPPORT_HEADER_TXT"));
        termsHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TERMS_AND_CONDITION"));
        livechatHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LIVE_CHAT"));

    }


    public void onClick(View view) {
        Utils.hideKeyboard(SupportActivity.this);
        Bundle bn = new Bundle();
        switch (view.getId()) {

            case R.id.backImgView:
                SupportActivity.super.onBackPressed();
                break;
            case R.id.aboutusarea:

                bn.putString("staticpage", "1");
                new ActUtils(getActContext()).startActWithData(StaticPageActivity.class, bn);
                break;
            case R.id.privacyarea:
                bn.putString("staticpage", "33");
                new ActUtils(getActContext()).startActWithData(StaticPageActivity.class, bn);
                break;
            case R.id.contactarea:
                new ActUtils(getActContext()).startAct(ContactUsActivity.class);
                break;
            case R.id.helparea:
                new ActUtils(getActContext()).startAct(HelpActivity.class);
                break;
            case R.id.termsCondArea:
                bn.putString("staticpage", "4");
                new ActUtils(getActContext()).startActWithData(StaticPageActivity.class, bn);
                break;
            case R.id.chatarea:
                startChatActivity();
                break;

        }
    }


    private void startChatActivity() {

        String vName = generalFunc.getJsonValueStr("vName", obj_userProfile);
        String vLastName = generalFunc.getJsonValueStr("vLastName", obj_userProfile);

        String driverName = vName + " " + vLastName;
        String driverEmail = generalFunc.getJsonValueStr("vEmail", obj_userProfile);

        Utils.LIVE_CHAT_LICENCE_NUMBER = generalFunc.getJsonValueStr("LIVE_CHAT_LICENCE_NUMBER", obj_userProfile);
        HashMap<String, String> map = new HashMap<>();
        map.put("FNAME", vName);
        map.put("LNAME", vLastName);
        map.put("EMAIL", driverEmail);
        map.put("USERTYPE", Utils.userType);

        Intent intent = new Intent(this, ChatWindowActivity.class);
        intent.putExtra(ChatWindowActivity.KEY_LICENCE_NUMBER, Utils.LIVE_CHAT_LICENCE_NUMBER);
        intent.putExtra(ChatWindowActivity.KEY_VISITOR_NAME, driverName);
        intent.putExtra(ChatWindowActivity.KEY_VISITOR_EMAIL, driverEmail);
        intent.putExtra(ChatWindowActivity.KEY_GROUP_ID, Utils.userType + "_" + generalFunc.getMemberId());

        intent.putExtra("myParam", map);
        startActivity(intent);
    }

}
