package com.zphr.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activity.ParentActivity;
import com.general.files.GeneralFunctions;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

public class FareBreakDownActivity extends ParentActivity {

    MTextView titleTxt, fareBreakdownNoteTxt, carTypeTitle;
    LinearLayout fareDetailDisplayArea;
    String selectedcar = "", iUserId = "", distance = "", time = "", PromoCode = "", vVehicleType = "";
    boolean isSkip, isFixFare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_break_down);

        titleTxt = findViewById(R.id.titleTxt);
        fareBreakdownNoteTxt = findViewById(R.id.fareBreakdownNoteTxt);
        carTypeTitle = findViewById(R.id.carTypeTitle);
        ImageView backImgView = findViewById(R.id.backImgView);
        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        backImgView.setOnClickListener(v -> onBackPressed());
        fareDetailDisplayArea = findViewById(R.id.fareDetailDisplayArea);
        selectedcar = getIntent().getStringExtra("SelectedCar");
        iUserId = getIntent().getStringExtra("iUserId");
        distance = getIntent().getStringExtra("distance");
        time = getIntent().getStringExtra("time");
        PromoCode = getIntent().getStringExtra("PromoCode");
        vVehicleType = getIntent().getStringExtra("vVehicleType");
        isFixFare = getIntent().getBooleanExtra("isFixFare", false);
        setLabels();
        isSkip = getIntent().getBooleanExtra("isSkip", false);
        callBreakdownRequest();

    }

    private void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FARE_BREAKDOWN_TXT"));
        carTypeTitle.setText(vVehicleType);
    }

    private Context getActContext() {
        return FareBreakDownActivity.this;
    }

    private void callBreakdownRequest() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "getEstimateFareDetailsArr");
        parameters.put("iUserId", generalFunc.getMemberId());

        parameters.put("distance", distance);
        parameters.put("time", time);
        parameters.put("SelectedCar", selectedcar);
        parameters.put("PromoCode", PromoCode);
        if (!isSkip) {
            parameters.put("isDestinationAdded", "Yes");
            String destLat = getIntent().getStringExtra("destLat");
            if (destLat != null && !destLat.equalsIgnoreCase("")) {
                parameters.put("DestLatitude", destLat);
                parameters.put("DestLongitude", getIntent().getStringExtra("destLong"));
            }
            String picupLat = getIntent().getStringExtra("picupLat");
            if (picupLat != null && !picupLat.equalsIgnoreCase("")) {
                parameters.put("StartLatitude", picupLat);
                parameters.put("EndLongitude", getIntent().getStringExtra("pickUpLong"));
            }
            if (getIntent().hasExtra("eFly")) {
                parameters.put("iFromStationId", getIntent().getStringExtra("iFromStationId"));
                parameters.put("iToStationId", getIntent().getStringExtra("iToStationId"));
                parameters.put("eFly", getIntent().getStringExtra("eFly"));
            }
        } else {
            parameters.put("isDestinationAdded", "No");
        }

        ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseObj)) {

                    if (generalFunc.getJsonValueStr("VEHICLE_TYPE_SHOW_METHOD", obj_userProfile) != null &&
                            generalFunc.getJsonValueStr("VEHICLE_TYPE_SHOW_METHOD", obj_userProfile).equalsIgnoreCase("Vertical")) {
                        if (isFixFare || getIntent().hasExtra("eFly")) {
                            fareBreakdownNoteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GENERAL_NOTE_FLAT_FARE_EST"));
                        } else {
                            fareBreakdownNoteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GENERAL_NOTE_FARE_EST"));
                        }
                    } else {
                        fareBreakdownNoteTxt.setText(generalFunc.getJsonValueStr("tInfoText", responseObj));
                    }
                    addFareDetailLayout(generalFunc.getJsonArray(Utils.message_str, responseObj));
                }
            } else {
                generalFunc.showError();
            }
        });


    }

    private void addFareDetailLayout(JSONArray jobjArray) {

        if (fareDetailDisplayArea.getChildCount() > 0) {
            fareDetailDisplayArea.removeAllViewsInLayout();
        }

        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                String data = Objects.requireNonNull(jobject.names()).getString(0);
                addFareDetailRow(data, jobject.get(data).toString(), (jobjArray.length() - 1) == i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @SuppressLint("InflateParams")
    private void addFareDetailRow(String row_name, String row_value, boolean isLast) {
        View convertView;
        if (row_name.equalsIgnoreCase("eDisplaySeperator")) {
            convertView = new View(getActContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dipToPixels(getActContext(), 1));
            int dip = Utils.dipToPixels(getActContext(), 10);
            params.setMarginStart(dip);
            params.setMarginEnd(dip);
            convertView.setBackgroundColor(Color.parseColor("#dedede"));
            convertView.setLayoutParams(params);
        } else {
            LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.design_fare_breakdown_row, null);

            convertView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            MTextView titleHTxt = convertView.findViewById(R.id.titleHTxt);
            MTextView titleVTxt = convertView.findViewById(R.id.titleVTxt);

            titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
            titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));

            if (isLast) {
                convertView.setMinimumHeight(Utils.dipToPixels(getActContext(), 40));

                titleHTxt.setTextColor(getResources().getColor(R.color.black));
                titleHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Poppins_SemiBold.ttf");
                titleHTxt.setTypeface(face);
                titleVTxt.setTypeface(face);
                titleVTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                titleVTxt.setTextColor(getResources().getColor(R.color.appThemeColor_1));
            }
        }
        fareDetailDisplayArea.addView(convertView);
    }
}