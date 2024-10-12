package com.zphr.user;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.activity.ParentActivity;
import com.general.files.GeneralFunctions;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import java.util.HashMap;

public class ContactUsActivity extends ParentActivity {

    MTextView titleTxt;
    MTextView subheaderTxt;
    MTextView detailTxt;
    MTextView floatingLabel1, floatingLabel2;
    MaterialEditText subjectBox;
    MaterialEditText contentBox;
    MButton btn_type2;
    String required_str = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        ImageView backImgView = findViewById(R.id.backImgView);
        addToClickHandler(backImgView);
        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        titleTxt = findViewById(R.id.titleTxt);
        subheaderTxt = findViewById(R.id.subheaderTxt);
        detailTxt = findViewById(R.id.detailTxt);
        floatingLabel1 = findViewById(R.id.floatingLabel1);
        floatingLabel2 = findViewById(R.id.floatingLabel2);

        subjectBox = findViewById(R.id.subjectBox);

        contentBox = findViewById(R.id.contentBox);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();

        setLabels();

        btn_type2.setId(Utils.generateViewId());
        addToClickHandler(btn_type2);
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_HEADER_TXT"));
        subheaderTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_SUBHEADER_TXT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_SEND_QUERY_BTN_TXT"));
        detailTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_DETAIL_TXT"));


        floatingLabel1.setText(generalFunc.retrieveLangLBl("", "LBL_RES_TO_CONTACT"));
        subjectBox.setHint(generalFunc.retrieveLangLBl("", "LBL_ADD_SUBJECT_HINT_CONTACT_TXT"));
        subjectBox.setHideUnderline(true);

        subjectBox.setMetTextColor(Color.parseColor("#757575"));
        contentBox.setMetTextColor(Color.parseColor("#757575"));

        if (generalFunc.isRTLmode()) {
            subjectBox.setPaddings(0, 0, (int) getResources().getDimension(R.dimen._10sdp), 0);
        } else {
            subjectBox.setPaddings((int) getResources().getDimension(R.dimen._10sdp), 0, 0, 0);
        }


        floatingLabel2.setText(generalFunc.retrieveLangLBl("", "LBL_YOUR_QUERY"));
        contentBox.setHint(generalFunc.retrieveLangLBl("", "LBL_YOUR_QUERY"));
        contentBox.setHideUnderline(true);
        if (generalFunc.isRTLmode()) {
            contentBox.setPaddings(0, 0, (int) getResources().getDimension(R.dimen._10sdp), 0);
        } else {
            contentBox.setPaddings((int) getResources().getDimension(R.dimen._10sdp), 0, 0, 0);
        }

        contentBox.setSingleLine(false);
        contentBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        contentBox.setGravity(Gravity.TOP);

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
    }

    public void submitQuery() {
        boolean subjectEntered = Utils.checkText(subjectBox);
        boolean contentEntered = Utils.checkText(contentBox);

        if (!subjectEntered || !contentEntered) {
            ((MTextView) findViewById(R.id.subjectBox_error)).setText(required_str);
            findViewById(R.id.subjectBox_error).setVisibility(!subjectEntered ? View.VISIBLE : View.INVISIBLE);

            ((MTextView) findViewById(R.id.contentBox_error)).setText(required_str);
            findViewById(R.id.contentBox_error).setVisibility(!contentEntered ? View.VISIBLE : View.INVISIBLE);
            return;
        } else {
            findViewById(R.id.subjectBox_error).setVisibility(View.INVISIBLE);
            findViewById(R.id.contentBox_error).setVisibility(View.INVISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "sendContactQuery");
        parameters.put("UserType", Utils.app_type);
        parameters.put("UserId", generalFunc.getMemberId());
        parameters.put("message", Utils.getText(contentBox));
        parameters.put("subject", Utils.getText(subjectBox));

        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {
                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    contentBox.setText("");
                    subjectBox.setText("");
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });
    }

    public Context getActContext() {
        return ContactUsActivity.this;
    }


    public void onClick(View view) {
        int i = view.getId();
        Utils.hideKeyboard(getActContext());
        if (i == R.id.backImgView) {
            ContactUsActivity.super.onBackPressed();
        } else if (i == btn_type2.getId()) {
            submitQuery();
        }
    }


}
