package com.zphr.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;

import com.activity.ParentActivity;
import com.countryview.view.CountryPicker;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.OpenMainProfile;
import com.general.files.SetOnTouchList;
import com.general.files.ConfigureMemberData;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import java.util.HashMap;

public class VerifyFbProfileActivity extends ParentActivity {

    MTextView titleTxt;
    ImageView backImgView;


    MaterialEditText fNameBox;
    MaterialEditText lNameBox;
    MaterialEditText emailBox;
    MaterialEditText countryBox;
    MaterialEditText mobileBox;

    MButton btn_type2;

    String vCountryCode = "";
    String vPhoneCode = "";
    boolean isCountrySelected = false;

    String required_str = "";
    String error_email_str = "";
    CountryPicker countryPicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_fb_profile);


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        fNameBox = (MaterialEditText) findViewById(R.id.fNameBox);
        lNameBox = (MaterialEditText) findViewById(R.id.lNameBox);
        emailBox = (MaterialEditText) findViewById(R.id.emailBox);
        countryBox = (MaterialEditText) findViewById(R.id.countryBox);
        mobileBox = (MaterialEditText) findViewById(R.id.mobileBox);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();

        removeInput();
        setLabels();

        mobileBox.setInputType(InputType.TYPE_CLASS_NUMBER);

        fNameBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        lNameBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        emailBox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mobileBox.setImeOptions(EditorInfo.IME_ACTION_DONE);

        countryBox.setShowClearButton(false);
        addToClickHandler(backImgView);
        addToClickHandler(btn_type2);
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_VERIFICATION_PAGE_HEADER"));

        fNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_FIRST_NAME_HEADER_TXT"));
        lNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LAST_NAME_HEADER_TXT"));
        emailBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_LBL_TXT"));
        countryBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_COUNTRY_TXT"));
        mobileBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));

        if (generalFunc.retrieveValue(Utils.MOBILE_VERIFICATION_ENABLE_KEY).equals("Yes")) {
            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_NEXT_TXT"));
        } else {
            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_REGISTER_TXT"));
        }

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");

        fNameBox.setText(getIntent().getStringExtra("FNAME"));
        lNameBox.setText(getIntent().getStringExtra("LNAME"));
        emailBox.setText(getIntent().getStringExtra("EMAIL"));
    }

    public void removeInput() {
        Utils.removeInput(countryBox);

        if (generalFunc.retrieveValue("showCountryList").equalsIgnoreCase("Yes")) {

            countryBox.setOnTouchListener(new SetOnTouchList());
            addToClickHandler(countryBox);
        }
    }

    public void checkData() {
        Utils.hideKeyboard(getActContext());
        boolean fNameEntered = Utils.checkText(fNameBox) ? true : Utils.setErrorFields(fNameBox, required_str);
        boolean lNameEntered = Utils.checkText(lNameBox) ? true : Utils.setErrorFields(lNameBox, required_str);
        boolean isEmailBlankAndOptional = generalFunc.isEmailBlankAndOptional(generalFunc, Utils.getText(emailBox));
        boolean emailEntered = isEmailBlankAndOptional ? true : (Utils.checkText(emailBox) ?
                (generalFunc.isEmailValid(Utils.getText(emailBox)) ? true : Utils.setErrorFields(emailBox, error_email_str))
                : Utils.setErrorFields(emailBox, required_str));
        boolean mobileEntered = Utils.checkText(mobileBox) ? true : Utils.setErrorFields(mobileBox, required_str);
        boolean countryEntered = isCountrySelected ? true : Utils.setErrorFields(countryBox, required_str);

        if (fNameEntered == false || lNameEntered == false || emailEntered == false || mobileEntered == false
                || countryEntered == false) {
            return;
        }

        if (generalFunc.retrieveValue(Utils.MOBILE_VERIFICATION_ENABLE_KEY).equals("Yes")) {
            checkUserExist();
        } else {
            registerUser();
        }

    }

    public void registerUser() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "signup");
        parameters.put("vFirstName", Utils.getText(fNameBox));
        parameters.put("vLastName", Utils.getText(lNameBox));
        parameters.put("vEmail", Utils.getText(emailBox));
        parameters.put("vPhone", Utils.getText(mobileBox));
        parameters.put("PhoneCode", vPhoneCode);
        parameters.put("CountryCode", vCountryCode);
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("vFbId", getIntent().getStringExtra("FBID"));

        ApiHandler.execute(getActContext(), parameters, true, true, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    new ConfigureMemberData(responseString, generalFunc, getActContext(), true);
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));
                    new OpenMainProfile(getActContext(),
                            generalFunc.getJsonValue(Utils.message_str, responseString), false, generalFunc).startProcess();
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });

    }

    public void checkUserExist() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "isUserExist");
        parameters.put("Email", Utils.getText(emailBox));
        parameters.put("Phone", Utils.getText(mobileBox));

        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    notifyVerifyMobile();
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });
    }

    public void notifyVerifyMobile() {
        Bundle bn = new Bundle();
        bn.putString("msg", "DO_PHONE_VERIFY");
        bn.putString("MOBILE", vPhoneCode + Utils.getText(mobileBox));
        generalFunc.verifyMobile(bn, null, VerifyInfoActivity.class);
    }

    public Context getActContext() {
        return VerifyFbProfileActivity.this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.VERIFY_MOBILE_REQ_CODE && resultCode == RESULT_OK) {
            String MSG_TYPE = data == null ? "" : (data.getStringExtra("MSG_TYPE") == null ? "" : data.getStringExtra("MSG_TYPE"));
            if (!MSG_TYPE.equals("EDIT_PROFILE")) {
                registerUser();
            }
//            registerUser();
        } else if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && resultCode == RESULT_OK && data != null) {
            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");
            isCountrySelected = true;

            countryBox.setText("+" + vPhoneCode);
        }
    }

    public void setData(String vCountryCode, String vPhoneCode, String vSImage) {
        this.vCountryCode = vCountryCode;
        this.vPhoneCode = vPhoneCode;
        isCountrySelected = true;


        GeneralFunctions generalFunctions = new GeneralFunctions(MyApp.getInstance().getCurrentAct());
        countryBox.setText("+" + generalFunctions.convertNumberWithRTL(vPhoneCode));
    }


    public void onClick(View view) {
        int i = view.getId();
        Utils.hideKeyboard(getActContext());
        if (i == btn_type2.getId()) {
            checkData();
        } else if (i == R.id.backImgView) {
            VerifyFbProfileActivity.super.onBackPressed();
        } else if (i == R.id.countryBox) {
            if (countryPicker == null) {
                countryPicker = new CountryPicker.Builder(getActContext()).showingDialCode(true)
                        .setLocale(locale).showingFlag(true)
                        .enablingSearch(true)
                        .setCountrySelectionListener(country -> setData(country.getCode(), country.getDialCode(), country.getFlagName()))
                        .build();
            }
            countryPicker.show(getActContext());
        }


    }


}
