package com.zphr.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.ParentActivity;
import com.adapter.files.EmergencyContactRecycleAdapter;
import com.countryview.view.CountryPicker;
import com.general.files.GeneralFunctions;
import com.model.ServiceModule;
import com.service.handler.ApiHandler;
import com.utils.LoadImage;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class EmergencyContactActivity extends ParentActivity implements EmergencyContactRecycleAdapter.OnItemClickList {

    //int PICK_CONTACT = 2121;
    MTextView titleTxt;
    MButton btn_type2;
    ProgressBar loading;
    RelativeLayout dataContainer;
    LinearLayout noContactArea, mainArea;
    androidx.appcompat.app.AlertDialog alertDialog;

    RecyclerView emeContactRecyclerView;
    EmergencyContactRecycleAdapter adapter;
    ErrorView errorView;
    ArrayList<HashMap<String, String>> list;

    private CountryPicker countryPicker;
    private String vSImage = "", vCountryCode = "", vPhoneCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        ImageView backImgView = findViewById(R.id.backImgView);
        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }

        mainArea = findViewById(R.id.mainArea);
        titleTxt = findViewById(R.id.titleTxt);
        loading = findViewById(R.id.loading);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        emeContactRecyclerView = findViewById(R.id.emeContactRecyclerView);
        errorView = findViewById(R.id.errorView);
        dataContainer = findViewById(R.id.dataContainer);
        noContactArea = findViewById(R.id.noContactArea);
        noContactArea = findViewById(R.id.noContactArea);

        list = new ArrayList<>();
        adapter = new EmergencyContactRecycleAdapter(getActContext(), list);
        emeContactRecyclerView.setAdapter(adapter);

        setLabels();

        btn_type2.setId(Utils.generateViewId());
        addToClickHandler(btn_type2);
        addToClickHandler(backImgView);

        getContacts();

        adapter.setOnItemClickList(this);
    }

    @Override
    public void onItemClick(int position) {
        buildWarningMessage(list.get(position).get("iEmergencyId"));
    }

    private void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_EMERGENCY_CONTACT"));
        if (ServiceModule.ServiceProvider || ServiceModule.ServiceBid) {
            ((MTextView) findViewById(R.id.emeTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_FOR_SAFETY"));
        } else {
            ((MTextView) findViewById(R.id.emeTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_EMERGENCY_CONTACT_TITLE"));
        }
        ((MTextView) findViewById(R.id.emeSubTitleTxt1)).setText(generalFunc.retrieveLangLBl("", "LBL_EMERGENCY_CONTACT_SUB_TITLE1"));
        ((MTextView) findViewById(R.id.emeSubTitleTxt2)).setText(generalFunc.retrieveLangLBl("", "LBL_EMERGENCY_CONTACT_SUB_TITLE2"));
        ((MTextView) findViewById(R.id.notifyTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_ADD_EMERGENCY_UP_TO_COUNT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_CONTACTS"));
    }

    private void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    // READ_CONTACTS  permission removed and add new popup for add contacts 05/09/2022
    //Start
    //-----------------------------------------------------------------------------


   /* private void checkPermission() {
        String[] permission = new String[]{Manifest.permission.READ_CONTACTS};
        if (PermissionHandler.getInstance().checkAnyPermissions(generalFunc, true, permission, Utils.REQUEST_READ_CONTACTS)) {
            OpenContactList();
        }
    }*/

    /*@Override
    public void onReqPermissionsResult() {
        checkPermission();
    }*/

    /*private void OpenContactList() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }*/

    //--------------------------------------------------------------
    //End

    private void getContacts() {
        mainArea.setBackgroundColor(Color.parseColor("#EBEBEB"));
        (findViewById(R.id.btn_type2)).setVisibility(View.GONE);
        (findViewById(R.id.notifyTxt)).setVisibility(View.GONE);
        dataContainer.setVisibility(View.VISIBLE);
        noContactArea.setVisibility(View.GONE);

        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        if (list.size() > 0) {
            list.clear();
        }

        final HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "loadEmergencyContacts");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.userType);

        ApiHandler.execute(getActContext(), parameters, responseString -> {

            noContactArea.setVisibility(View.GONE);
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.toString().equals("")) {

                closeLoader();

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseObj)) {

                    JSONArray obj_arr = generalFunc.getJsonArray(Utils.message_str, responseObj);

                    for (int i = 0; i < obj_arr.length(); i++) {
                        JSONObject obj_temp = generalFunc.getJsonObject(obj_arr, i);

                        HashMap<String, String> map = new HashMap<>();

                        map.put("ContactName", generalFunc.getJsonValueStr("vName", obj_temp));
                        map.put("ContactPhone", generalFunc.getJsonValueStr("vPhone", obj_temp));
                        map.put("iEmergencyId", generalFunc.getJsonValueStr("iEmergencyId", obj_temp));

                        list.add(map);
                    }

                    adapter.notifyDataSetChanged();

                    if (obj_arr.length() >= 5) {
                        (findViewById(R.id.notifyTxt)).setVisibility(View.GONE);
                        (findViewById(R.id.btn_type2)).setVisibility(View.GONE);
                    } else {
                        (findViewById(R.id.notifyTxt)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.btn_type2)).setVisibility(View.VISIBLE);
                    }

                } else {
                    mainArea.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    noContactArea.setVisibility(View.VISIBLE);
                    dataContainer.setVisibility(View.GONE);

                    (findViewById(R.id.notifyTxt)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.btn_type2)).setVisibility(View.VISIBLE);
                }
            } else {
                generateErrorView();
            }
        });

    }

    private void addContact(String contactName, String contactPhone) {
        final HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "addEmergencyContacts");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vName", contactName);
        parameters.put("Phone", contactPhone);
        parameters.put("UserType", Utils.userType);

        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {
                    getContacts();
                    generalFunc.showMessage(generalFunc.getCurrentView(EmergencyContactActivity.this),
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });
    }

    private void buildWarningMessage(final String iEmergencyId) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            generateAlert.closeAlertBox();
            if (btn_id == 1) {
                deleteContact(iEmergencyId);
            }
        });
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_CONFIRM_MSG_DELETE_EME_CONTACT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
        generateAlert.showAlertBox();
    }

    private void deleteContact(String iEmergencyId) {
        final HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "deleteEmergencyContacts");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iEmergencyId", iEmergencyId);
        parameters.put("UserType", Utils.userType);

        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {
                    getContacts();
                    generalFunc.showMessage(generalFunc.getCurrentView(EmergencyContactActivity.this),
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });
    }

    private void generateErrorView() {
        closeLoader();
        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");
        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(this::getContacts);
    }

    private Context getActContext() {
        return EmergencyContactActivity.this;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && resultCode == RESULT_OK && data != null) {
            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");
            vSImage = data.getStringExtra("vSImage");
        }
        // READ_CONTACTS  permission removed and add new popup for add contacts 05/09/2022
        /*else if (requestCode == PICK_CONTACT && data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;

                try {
                    c = getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.TYPE}, null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                        addContact(name, number);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        } */
    }

    public void AddEmergencyContacts() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.emergency_contaxct_layout, null);

        final String required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");

        final ImageView contactimg = dialogView.findViewById(R.id.img);
        ImageView countryimage = dialogView.findViewById(R.id.countryimage);
        MaterialEditText countryBox = dialogView.findViewById(R.id.countryBox);
        int paddingValStart = (int) getResources().getDimension(R.dimen._35sdp);
        int paddingValEnd = (int) getResources().getDimension(R.dimen._12sdp);
        if (generalFunc.isRTLmode()) {
            countryBox.setPaddings(paddingValEnd, 0, paddingValStart, 0);
        } else {
            countryBox.setPaddings(paddingValStart, 0, paddingValEnd, 0);
        }

        contactimg.setImageResource(R.drawable.ic_contact_card);

        MaterialEditText nameBox = dialogView.findViewById(R.id.nameBox);

        MTextView submitTxt = dialogView.findViewById(R.id.submitTxt);
        MTextView cancelTxt = dialogView.findViewById(R.id.cancelTxt);
        MTextView subTitleTxt = dialogView.findViewById(R.id.subTitleTxt);
        MaterialEditText phoneBox = dialogView.findViewById(R.id.phoneBox);

        subTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_DETAILS_TXT"));
        submitTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBMIT_BUTTON_TXT"));
        cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));

        nameBox.setFloatingLabelText(generalFunc.retrieveLangLBl("", "LBL_FULL_NAME"));
        nameBox.setHint(generalFunc.retrieveLangLBl("", "LBL_FULL_NAME"));
        nameBox.setInputType(InputType.TYPE_CLASS_TEXT);

        phoneBox.setFloatingLabelText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        phoneBox.setHint(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        phoneBox.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        //phoneBox.setHelperText(generalFunc.retrieveLangLBl("", "LBL_SIGN_IN_MOBILE_EMAIL_HELPER"));

        vSImage = generalFunc.retrieveValue(Utils.DefaultCountryImage);
        int imagewidth = (int) getResources().getDimension(R.dimen._30sdp);
        int imageheight = (int) getResources().getDimension(R.dimen._20sdp);
        String imgUrl = Utils.getResizeImgURL(getActContext(), vSImage, imagewidth, imageheight);
        new LoadImage.builder(LoadImage.bind(imgUrl), countryimage).build();


        countryBox.setOnClickListener(v -> {
            if (countryPicker != null) {
                countryPicker = null;
            }
            countryPicker = new CountryPicker.Builder(getActContext()).showingDialCode(true)
                    .setLocale(locale).showingFlag(true)
                    .enablingSearch(true)
                    .setCountrySelectionListener(country ->
                            setData(country.getCode(), country.getDialCode(),
                                    country.getFlagName(), countryimage, countryBox))
                    .build();
            countryPicker.show(getActContext());
        });

        countryBox.setOnTouchListener(new setOnTouchList());

        if (!generalFunc.getJsonValue("vPhoneCode", obj_userProfile).equals("")) {
            vPhoneCode = generalFunc.getJsonValueStr("vPhoneCode", obj_userProfile);
            vCountryCode = generalFunc.getJsonValueStr("vCountry", obj_userProfile);
            countryBox.setText("+" + generalFunc.convertNumberWithRTL(vPhoneCode));
        }

        if (generalFunc.getJsonValue("vSCountryImage", obj_userProfile) != null && !generalFunc.getJsonValueStr("vSCountryImage", obj_userProfile).equalsIgnoreCase("")) {
            vSImage = generalFunc.getJsonValueStr("vSCountryImage", obj_userProfile);
            imgUrl = Utils.getResizeImgURL(getActContext(), vSImage, imagewidth, imageheight);
            new LoadImage.builder(LoadImage.bind(imgUrl), countryimage).build();
        }

        builder.setView(dialogView);

        cancelTxt.setOnClickListener(v -> alertDialog.dismiss());
        submitTxt.setOnClickListener(v -> {

            boolean mobileEntered = Utils.checkText(phoneBox) ? true : Utils.setErrorFields(phoneBox, required_str);
            boolean NameEntered = Utils.checkText(nameBox) ? true : Utils.setErrorFields(nameBox, required_str);

            if (mobileEntered) {
                mobileEntered = phoneBox.length() >= 3 ? true : Utils.setErrorFields(phoneBox, generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO"));
            }

            if (!NameEntered || !mobileEntered) {
                return;
            } else {
                addContact(Utils.getText(nameBox), "+" + vPhoneCode + " " + Utils.getText(phoneBox));
            }

        });

        builder.setView(dialogView);
        alertDialog = builder.create();


        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card_contact));
        alertDialog.show();
    }


    public void setData(String vCountryCode, String vPhoneCode, String vSImage, ImageView countryimage
            , MaterialEditText countryBox) {
        this.vCountryCode = vCountryCode;
        this.vPhoneCode = vPhoneCode;
        this.vSImage = vSImage;

        runOnUiThread(() -> {
            new LoadImage.builder(LoadImage.bind(vSImage), countryimage).build();

            countryBox.setText("+" + generalFunc.convertNumberWithRTL(vPhoneCode));
        });

    }

    public class setOnTouchList implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                view.performClick();
            }
            return true;
        }
    }


    public void onClick(View view) {
        int i = view.getId();
        Utils.hideKeyboard(getActContext());
        if (i == R.id.backImgView) {
            EmergencyContactActivity.super.onBackPressed();

        } else if (i == btn_type2.getId()) {
            //checkPermission();
            AddEmergencyContacts();
        }
    }

}