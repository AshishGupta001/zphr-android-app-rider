package com.zphr.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.activity.ParentActivity;
import com.adapter.files.CategoryListItem;
import com.general.files.ActUtils;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.service.handler.ApiHandler;
import com.utils.Logger;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.anim.loader.AVLoadingIndicatorView;
import com.view.pinnedListView.PinnedSectionListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;

public class SearchCategoryActivity extends ParentActivity  {

    View view;
    private PinnedSectionListView service_list;
    private ArrayList<CategoryListItem> categoryitems_list;

    private int hourCnt = 0, regCnt = 0, fixCnt = 0;
    private EditText searchTxtView;
    private boolean isSearch = false;
    private ImageView imageCancel;
    private AVLoadingIndicatorView loaderView;
    private MTextView noResTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_category);

        service_list = (PinnedSectionListView) findViewById(R.id.service_list);
        MTextView titleTxt = (MTextView) findViewById(R.id.titleTxt);
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_SERVICES"));
        noResTxt = (MTextView) findViewById(R.id.noResTxt);

        service_list.setShadowVisible(true);

        service_list.setFastScrollEnabled(false);
        service_list.setFastScrollAlwaysVisible(false);

        categoryitems_list = new ArrayList<>();
        ImageView backImgView = (ImageView) findViewById(R.id.backImgView);
        addToClickHandler(backImgView);
        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        imageCancel = (ImageView) findViewById(R.id.imageCancel);
        addToClickHandler(imageCancel);
        imageCancel.setVisibility(View.GONE);
        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);
        loaderView.setVisibility(View.GONE);

        searchTxtView = (EditText) findViewById(R.id.searchTxtView);
        searchTxtView.setHint(generalFunc.retrieveLangLBl("", "LBL_SEARCH_SERVICES"));
        searchTxtView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    isSearch = false;
                    imageCancel.setVisibility(View.GONE);
                    loaderView.setVisibility(View.GONE);
                    loaderView.setVisibility(View.VISIBLE);
                    getServiceInfo("");
                    Utils.hideKeyboard(getActContext());
                } else {


                    if (s.length() > 2) {
                        isSearch = true;
                        loaderView.setVisibility(View.VISIBLE);
                        imageCancel.setVisibility(View.GONE);
                        service_list.setVisibility(View.GONE);
                        new Handler().postDelayed(() -> {
                            getServiceInfo(searchTxtView.getText().toString().trim());
                        }, 750);
                    }

                }
            }
        });
    }

    private void getHourData() {
        try {
            hourCnt = 0;
            regCnt = 0;
            fixCnt = 0;
            Realm realm = MyApp.getRealmInstance();
        } catch (Exception e) {
            Logger.d("RealmException", "::" + e.toString());
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            imageCancel.setVisibility(View.GONE);
            loaderView.setVisibility(View.VISIBLE);
            service_list.setVisibility(View.GONE);
            getServiceInfo(Utils.getText(searchTxtView));
        }
    }


    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.backImgView) {
            onBackPressed();
        } else if (i == R.id.imageCancel) {
            loaderView.setVisibility(View.GONE);
            searchTxtView.setText("");
            service_list.setVisibility(View.GONE);
            getServiceInfo("");
        }
    }


    private Context getActContext() {
        return SearchCategoryActivity.this;
    }

    private void getServiceInfo(String searchText) {

        final HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "getDriverServiceCategories");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("iDriverId", getIntent().getStringExtra("iDriverId"));
        parameters.put("SelectedCabType", Utils.CabGeneralType_UberX);
        parameters.put("parentId", getIntent().getStringExtra("parentId") != null ? getIntent().getStringExtra("parentId") : "");
        parameters.put("SelectedVehicleTypeId", getIntent().getStringExtra("SelectedVehicleTypeId") != null ? getIntent().getStringExtra("SelectedVehicleTypeId") : "");
        parameters.put("vSelectedLatitude", getIntent().getStringExtra("latitude"));
        parameters.put("vSelectedLongitude", getIntent().getStringExtra("longitude"));
        parameters.put("vSelectedAddress", getIntent().getStringExtra("address"));

        parameters.put("search_keyword", searchText);

        ApiHandler.execute(getActContext(), parameters, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                if (this.isSearch) {
                    loaderView.setVisibility(View.GONE);
                    imageCancel.setVisibility(View.VISIBLE);
                }
                noResTxt.setVisibility(View.GONE);

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {

                    service_list.setVisibility(View.VISIBLE);


                    categoryitems_list.clear();

                    JSONArray mainListArr = generalFunc.getJsonArray(Utils.message_str, responseString);
                    CategoryListItem[] sections;

                    int sectionPosition = 0, listPosition = 0;


                        sections = new CategoryListItem[mainListArr.length()];

                    Realm realm = MyApp.getRealmInstance();

                    for (int i = 0; i < mainListArr.length(); i++) {
                        JSONObject tempJson = generalFunc.getJsonObject(mainListArr, i);

                        String vCategory = generalFunc.getJsonValueStr("vCategory", tempJson);

                        CategoryListItem section = new CategoryListItem(CategoryListItem.SECTION, vCategory);
                        section.sectionPosition = sectionPosition;
                        section.listPosition = listPosition++;
                        section.CountSubItems = GeneralFunctions.parseIntegerValue(0, vCategory);

                        sections[sectionPosition] = section;

                        categoryitems_list.add(section);

                        JSONArray subListArr = generalFunc.getJsonArray("SubCategories", tempJson);

                        for (int j = 0; j < subListArr.length(); j++) {
                            JSONObject subTempJson = generalFunc.getJsonObject(subListArr, j);

                            CategoryListItem categoryListItem = new CategoryListItem(CategoryListItem.ITEM, generalFunc.getJsonValueStr("vCategory", tempJson));
                            categoryListItem.sectionPosition = sectionPosition;
                            categoryListItem.listPosition = listPosition++;
                            categoryListItem.setvTitle(generalFunc.getJsonValueStr("vVehicleType", subTempJson));
                            categoryListItem.setiVehicleCategoryId(generalFunc.getJsonValueStr("iVehicleCategoryId", subTempJson));
                            categoryListItem.setvDesc(generalFunc.getJsonValueStr("vCategoryDesc", subTempJson));
                            categoryListItem.setvShortDesc(generalFunc.getJsonValueStr("vCategoryShortDesc", subTempJson));
                            categoryListItem.seteFareType(generalFunc.getJsonValueStr("eFareType", subTempJson));
                            categoryListItem.setfFixedFare(generalFunc.getJsonValueStr("fFixedFare", subTempJson));
                            categoryListItem.setfPricePerHour(generalFunc.getJsonValueStr("fPricePerHour", subTempJson));
                            categoryListItem.setfMinHour(generalFunc.getJsonValueStr("fMinHour", subTempJson));
                            categoryListItem.setiVehicleTypeId(generalFunc.getJsonValueStr("iVehicleTypeId", subTempJson));
                            //categoryListItem.setAdd(checkSameRecordExist(realm, generalFunc.getJsonValueStr("iVehicleTypeId", subTempJson)));
                            categoryitems_list.add(categoryListItem);
                        }

                        sectionPosition++;
                    }



                } else {
                    loaderView.setVisibility(View.GONE);
                    if (Utils.checkText(searchTxtView)) {
                        noResTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                        noResTxt.setVisibility(View.VISIBLE);
                    }

                }
            } else {
                generalFunc.showError(true);
            }
        });

    }
}