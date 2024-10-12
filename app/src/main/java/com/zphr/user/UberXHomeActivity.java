package com.zphr.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.activity.ParentActivity;
import com.dialogs.BottomInfoDialog;
import com.facebook.ads.AdSize;
import com.fragments.HomeDaynamicFragment;
import com.fragments.HomeDaynamic_22_Fragment;
import com.fragments.HomeFragment;
import com.fragments.MyBookingFragment;
import com.fragments.MyProfileFragment;
import com.fragments.MyWalletFragment;
import com.general.files.ActUtils;
import com.general.files.MyApp;
import com.general.files.OpenAdvertisementDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.model.ServiceModule;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MTextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;

public class UberXHomeActivity extends ParentActivity {


    public LinearLayout historyArea, walletArea, profileArea, homeArea, rduTopArea;
    MTextView historyTxt, walletTxt, profileTxt, homeTxt;
    ImageView home_img, bookingImg, walletImg, profileImg;


    public HomeFragment homeFragment;
    public HomeDaynamicFragment homeDaynamicFragment;
    public HomeDaynamic_22_Fragment homeDaynamic_22_fragment;
    public MyBookingFragment myBookingFragment;
    MyProfileFragment myProfileFragment;
    MyWalletFragment myWalletFragment;


    int bottomBtnpos = 1;

    int appThemeColor1, appThemeGetColor1, deSelectedGetColor, deSelectedColor, dayanamicSelColor;

    public String address;
    public String latitude = "0.0", longitude = "0.0";
    private static final int WEBVIEWPAYMENT = 001;
    public String ENABLE_LOCATION_WISE_BANNER = "No";
    private LinearLayout google_banner_container, banner_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uber_xhome2);
        if (generalFunc.getJsonValueStr("ENABLE_FACEBOOK_ADS", obj_userProfile).equalsIgnoreCase("Yes")) {
            facebooksAdds();
        }
        if (generalFunc.getJsonValueStr("ENABLE_GOOGLE_ADS", obj_userProfile).equalsIgnoreCase("Yes")) {
            googleAdds();
        }
        ENABLE_LOCATION_WISE_BANNER = generalFunc.getJsonValueStr("ENABLE_LOCATION_WISE_BANNER", obj_userProfile);
        int color = R.color.appThemeColor_1;
        appThemeColor1 = getActContext().getResources().getColor(color);
        dayanamicSelColor = getActContext().getResources().getColor(color);
        appThemeGetColor1 = ContextCompat.getColor(getActContext(), color);
        int deSelectColor = R.color.homedeSelectColor;
        deSelectedColor = getActContext().getResources().getColor(deSelectColor);
        deSelectedGetColor = ContextCompat.getColor(getActContext(), deSelectColor);
        historyTxt = findViewById(R.id.historyTxt);
        walletTxt = findViewById(R.id.walletTxt);
        profileTxt = findViewById(R.id.profileTxt);
        homeTxt = findViewById(R.id.homeTxt);
        home_img = findViewById(R.id.home_img);
        bookingImg = findViewById(R.id.bookingImg);
        walletImg = findViewById(R.id.walletImg);
        profileImg = findViewById(R.id.profileImg);
        rduTopArea = findViewById(R.id.rduTopArea);
        historyArea = findViewById(R.id.historyArea);
        walletArea = findViewById(R.id.walletArea);
        profileArea = findViewById(R.id.profileArea);
        homeArea = findViewById(R.id.homeArea);


        // below lines comment :for deliver all Home design changes
//        if (generalFunc.getMemberId().equalsIgnoreCase("")) {
//            historyArea.setVisibility(View.GONE);
//            walletArea.setVisibility(View.GONE);
//            profileArea.setVisibility(View.GONE);
//        }
        addToClickHandler(historyArea);
        addToClickHandler(walletArea);
        addToClickHandler(profileArea);
        addToClickHandler(homeArea);


        setLabel();
        manageBottomMenu(homeTxt);
        openHomeFragment();

        String advertise_banner_data = generalFunc.getJsonValueStr("advertise_banner_data", obj_userProfile);
        if (advertise_banner_data != null && !advertise_banner_data.equalsIgnoreCase("")) {

            String image_url = generalFunc.getJsonValue("image_url", advertise_banner_data);
            if (image_url != null && !image_url.equalsIgnoreCase("")) {
                HashMap<String, String> map = new HashMap<>();
                map.put("image_url", image_url);
                map.put("tRedirectUrl", generalFunc.getJsonValue("tRedirectUrl", advertise_banner_data));
                map.put("vImageWidth", generalFunc.getJsonValue("vImageWidth", advertise_banner_data));
                map.put("vImageHeight", generalFunc.getJsonValue("vImageHeight", advertise_banner_data));
                new OpenAdvertisementDialog(getActContext(), map, generalFunc);
            }
        }
        MyApp.getInstance().showOutsatandingdilaog(bookingImg);

        if (generalFunc.retrieveValue("isSmartLoginEnable").equalsIgnoreCase("Yes") &&
                !generalFunc.retrieveValue("isFirstTimeSmartLoginView").equalsIgnoreCase("Yes") && !generalFunc.getMemberId().equals("")) {

            BottomInfoDialog bottomInfoDialog = new BottomInfoDialog(getActContext(), generalFunc);
            bottomInfoDialog.setListener(() -> {
                generalFunc.storeData("isFirstTimeSmartLoginView", "Yes");
                manageHomeFrag();
            });
            bottomInfoDialog.showPreferenceDialog(generalFunc.retrieveLangLBl("", "LBL_QUICK_LOGIN"), generalFunc.retrieveLangLBl("", "LBL_QUICK_LOGIN_NOTE_TXT"),
                    R.raw.biometric, false, generalFunc.retrieveLangLBl("", "LBL_OK"), "", true);
        }

        String OPEN_CHAT = generalFunc.retrieveValue("OPEN_CHAT");
        if (Utils.checkText(OPEN_CHAT)) {
            JSONObject OPEN_CHAT_DATA_OBJ = generalFunc.getJsonObject(OPEN_CHAT);
            generalFunc.removeValue("OPEN_CHAT");
            if (OPEN_CHAT_DATA_OBJ != null) {
                new ActUtils(getActContext()).startActWithData(ChatActivity.class, generalFunc.createChatBundle(OPEN_CHAT_DATA_OBJ));
            }
        }

    }

    private void manageHomeFrag() {
        generalFunc.isLocationPermissionGranted(true);
        if (homeFragment != null && isHomeFrg) {
            homeFragment.initializeLocationCheckDone();
        } else if (homeDaynamicFragment != null && isHomeFrg) {
            homeDaynamicFragment.initializeLocationCheckDone();
        } else if (homeDaynamic_22_fragment != null && isHomeFrg) {
            homeDaynamic_22_fragment.initializeLocationCheckDone();
        }
    }

    public void checkBiddingView(int pos) {
        if (isBookingfrg) {
            myBookingFragment.setFrag(pos);
        } else {
            Bundle bn = new Bundle();
            bn.putInt("viewPos", pos);
            new ActUtils(getActContext()).startActWithData(BookingActivity.class, bn);
        }

    }

    private void googleAdds() {
        AdView mAdView;
        google_banner_container = findViewById(R.id.google_banner_container);
        google_banner_container.setVisibility(View.VISIBLE);
        //manage Google ads
        MobileAds.initialize(getActContext());
        mAdView = new AdView(getActContext());
        mAdView.setAdSize(com.google.android.gms.ads.AdSize.FULL_BANNER);
        mAdView.setAdUnitId(generalFunc.getJsonValueStr("GOOGLE_ADMOB_ID", obj_userProfile));
        AdRequest adRequest = new AdRequest.Builder().build();
        google_banner_container.addView(mAdView);
        mAdView.loadAd(adRequest);
    }

    private void facebooksAdds() {
        banner_container = findViewById(R.id.banner_container);
        banner_container.setVisibility(View.VISIBLE);
        com.facebook.ads.AdView adView;
        adView = new com.facebook.ads.AdView(this, "IMG_16_9_APP_INSTALL#" + generalFunc.getJsonValueStr("FACEBOOK_PLACEMENT_ID", obj_userProfile), AdSize.BANNER_HEIGHT_50);
        // Add the ad view to your activity layout
        banner_container.addView(adView);

// Request an ad
        adView.loadAd();
    }

    private void manageView(boolean isHome) {
        if (google_banner_container != null) {
            google_banner_container.setVisibility(isHome ? View.VISIBLE : View.GONE);
        }
        if (banner_container != null) {
            banner_container.setVisibility(isHome ? View.VISIBLE : View.GONE);
        }
    }


    public void pubNubMsgArrived(String message) {

        String driverMsg = generalFunc.getJsonValue("Message", message);
        String eType = generalFunc.getJsonValue("eType", message);

        if (driverMsg.equals("CabRequestAccepted")) {
            String eSystem = generalFunc.getJsonValueStr("eSystem", obj_userProfile);
            if (eSystem != null && eSystem.equalsIgnoreCase("DeliverAll")) {
                generalFunc.showGeneralMessage("", generalFunc.getJsonValue("vTitle", message));
                return;
            }


            if (eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                return;
            } else if (app_type != null && app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {

                if (!eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                    MyApp.getInstance().restartWithGetDataApp();
                    return;
                }
            }

            if (generalFunc.isJSONkeyAvail("iCabBookingId", message) && !generalFunc.getJsonValue("iCabBookingId", message).trim().equals("")) {
                MyApp.getInstance().restartWithGetDataApp();
            } else {
                if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX) || eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                    return;
                } else {
                    MyApp.getInstance().restartWithGetDataApp();
                }
            }

        }
    }


    public void openHomeFragment() {
        manageView(true);

        if (generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP_22", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP_22", obj_userProfile).equalsIgnoreCase("Yes")) {
            if (homeDaynamic_22_fragment == null) {
                homeDaynamic_22_fragment = new HomeDaynamic_22_Fragment();
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("uberXAddress", address);
                bundle.putString("uberXlat", latitude);
                bundle.putString("uberXlong", longitude);
                homeDaynamic_22_fragment.setArguments(bundle);
            }
            openPageFrag(1, homeDaynamic_22_fragment);

        } else if (generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile).equalsIgnoreCase("Yes")) {
            if (homeDaynamicFragment == null) {
                homeDaynamicFragment = new HomeDaynamicFragment();
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("uberXAddress", address);
                bundle.putString("uberXlat", latitude);
                bundle.putString("uberXlong", longitude);
                homeDaynamicFragment.setArguments(bundle);
            }
            openPageFrag(1, homeDaynamicFragment);

        } else {

            if (homeFragment == null) {
                homeFragment = new HomeFragment();
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("uberXAddress", address);
                bundle.putString("uberXlat", latitude);
                bundle.putString("uberXlong", longitude);
                homeFragment.setArguments(bundle);
            }
            openPageFrag(1, homeFragment);
            bottomBtnpos = 1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Logger.d("Onresume", ":: called");

        if (myProfileFragment != null && isProfilefrg) {
            myProfileFragment.onResume();
        }

        if (myWalletFragment != null && isWalletfrg) {

            myWalletFragment.onResume();
        }


        Log.d("uberxac", "onResume: ");

        if (myBookingFragment != null && isBookingfrg) {

            myBookingFragment.onResume();
        }
    }

    public void openProfileFragment() {
        manageView(false);
        if (myProfileFragment == null) {
            myProfileFragment = new MyProfileFragment();
        }
        openPageFrag(4, myProfileFragment);
        bottomBtnpos = 4;

    }

    public void openWalletFragment() {
        manageView(false);

        if (generalFunc.getMemberId().equals("")) {
            openProfileFragment();
            return;

        }
        if (myWalletFragment == null) {
            myWalletFragment = new MyWalletFragment();
        }
        openPageFrag(3, myWalletFragment);

        bottomBtnpos = 3;


    }


    public void openPageFrag(int position, Fragment fragToOpen) {
        int leftAnim = bottomBtnpos > position ? R.anim.slide_from_left : R.anim.slide_from_right;
        int rightAnim = bottomBtnpos > position ? R.anim.slide_to_right : R.anim.slide_to_left;

        try {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(leftAnim,
                    rightAnim)
                    .replace(R.id.fragContainerone, fragToOpen).commit();


        } catch (Exception e) {
            Logger.e("ExceptionFrag", "::" + e.toString());
        }

    }


    public void openHistoryFragment() {
        manageView(false);
        if (generalFunc.getMemberId().equals("")) {
            openProfileFragment();
            return;

        }

        if (myBookingFragment == null) {
            myBookingFragment = new MyBookingFragment();
        }

        openPageFrag(2, myBookingFragment);
        bottomBtnpos = 2;
    }

    public void setLabel() {
        profileTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HEADER_RDU_PROFILE"));
        homeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_BOTTOM_MENU"));
        walletTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HEADER_RDU_WALLET"));
        if (ServiceModule.isDeliverAllOnly()) {
            historyTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MY_ORDERS"));
        } else {
            historyTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HEADER_RDU_BOOKINGS"));
        }

    }

    public void manageBottomMenu(MTextView selTextView) {
        //manage Select deselect Bottom Menu
        if (selTextView.getId() == homeTxt.getId()) {

            homeTxt.setTextColor(appThemeColor1);
            home_img.setColorFilter(appThemeGetColor1, android.graphics.PorterDuff.Mode.SRC_IN);
            if (generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile).equalsIgnoreCase("Yes")) {
                homeTxt.setTextColor(dayanamicSelColor);
                home_img.setColorFilter(dayanamicSelColor, android.graphics.PorterDuff.Mode.SRC_IN);

            }
        } else {
            homeTxt.setTextColor(deSelectedColor);
            home_img.setColorFilter(deSelectedGetColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (selTextView.getId() == historyTxt.getId()) {
            historyTxt.setTextColor(appThemeColor1);
            bookingImg.setColorFilter(appThemeGetColor1, android.graphics.PorterDuff.Mode.SRC_IN);
            if (generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile).equalsIgnoreCase("Yes")) {
                historyTxt.setTextColor(dayanamicSelColor);
                bookingImg.setColorFilter(dayanamicSelColor, android.graphics.PorterDuff.Mode.SRC_IN);

            }
        } else {
            historyTxt.setTextColor(deSelectedColor);
            bookingImg.setColorFilter(deSelectedGetColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (selTextView.getId() == walletTxt.getId()) {
            walletTxt.setTextColor(appThemeColor1);
            walletImg.setColorFilter(appThemeGetColor1, android.graphics.PorterDuff.Mode.SRC_IN);
            if (generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile).equalsIgnoreCase("Yes")) {
                walletTxt.setTextColor(dayanamicSelColor);
                walletImg.setColorFilter(dayanamicSelColor, android.graphics.PorterDuff.Mode.SRC_IN);

            }
        } else {
            walletTxt.setTextColor(deSelectedColor);
            walletImg.setColorFilter(deSelectedGetColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (selTextView.getId() == profileTxt.getId()) {
            profileTxt.setTextColor(appThemeColor1);
            profileImg.setColorFilter(appThemeGetColor1, android.graphics.PorterDuff.Mode.SRC_IN);
            if (generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile).equalsIgnoreCase("Yes")) {
                profileTxt.setTextColor(dayanamicSelColor);
                profileImg.setColorFilter(dayanamicSelColor, android.graphics.PorterDuff.Mode.SRC_IN);

            }
        } else {
            profileTxt.setTextColor(deSelectedColor);
            profileImg.setColorFilter(deSelectedGetColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }

    }

    public boolean isHomeFrg = true;
    boolean isWalletfrg = false, isProfilefrg = false, isBookingfrg = false;


    public void onClick(View view) {
        int i = view.getId();
        Bundle bn = new Bundle();
        isHomeFrg = false;
        isWalletfrg = false;
        isProfilefrg = false;
        isBookingfrg = false;

        if (i == walletArea.getId()) {
            getWindow().setStatusBarColor(appThemeColor1);
            isWalletfrg = true;
            manageBottomMenu(walletTxt);
            openWalletFragment();

        } else if (i == profileArea.getId()) {
            getWindow().setStatusBarColor(appThemeColor1);
            isProfilefrg = true;
            manageBottomMenu(profileTxt);
            openProfileFragment();
        } else if (i == historyArea.getId()) {
            getWindow().setStatusBarColor(appThemeColor1);
            isBookingfrg = true;
            manageBottomMenu(historyTxt);
            openHistoryFragment();

        } else if (i == homeArea.getId()) {
            isHomeFrg = true;
            manageBottomMenu(homeTxt);
            if (generalFunc.prefHasKey(Utils.isMultiTrackRunning) && generalFunc.retrieveValue(Utils.isMultiTrackRunning).equalsIgnoreCase("Yes")) {
                MyApp.getInstance().restartWithGetDataApp();
            } else {
                openHomeFragment();
            }
        }
    }


    @Override
    public void onBackPressed() {

        if (generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP_22", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP_22", obj_userProfile).equalsIgnoreCase("Yes")) {
            if (homeDaynamic_22_fragment != null) {
                if (homeDaynamic_22_fragment.isLoading) {
                    return;
                }
                if (homeDaynamic_22_fragment.CAT_TYPE_MODE.equals("1") && generalFunc.getJsonValueStr(Utils.UBERX_PARENT_CAT_ID, obj_userProfile).equalsIgnoreCase("0")) {
                    homeDaynamic_22_fragment.multiServiceSelect.clear();
                    homeDaynamic_22_fragment.manageToolBarAddressView(false);
                    homeDaynamic_22_fragment.uberXHomeActivity.rduTopArea.setVisibility(View.VISIBLE);
                    homeDaynamic_22_fragment.configCategoryView();
                    return;
                }
            }

        } else if (generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", obj_userProfile).equalsIgnoreCase("Yes")) {

            if (homeDaynamicFragment != null) {
                if (homeDaynamicFragment.isLoading) {
                    return;
                }
                if (homeDaynamicFragment.CAT_TYPE_MODE.equals("1") && generalFunc.getJsonValueStr(Utils.UBERX_PARENT_CAT_ID, obj_userProfile).equalsIgnoreCase("0")) {
                    homeDaynamicFragment.multiServiceSelect.clear();
                    homeDaynamicFragment.backImgView.setVisibility(View.GONE);
                    homeDaynamicFragment.manageToolBarAddressView(false);
                    homeDaynamicFragment.uberXHomeActivity.rduTopArea.setVisibility(View.VISIBLE);
                    homeDaynamicFragment.configCategoryView();
                    return;
                }
            }
        } else {
            if (homeFragment != null) {
                if (homeFragment.isLoading) {
                    return;
                }
                if (homeFragment.CAT_TYPE_MODE.equals("1") && generalFunc.getJsonValueStr(Utils.UBERX_PARENT_CAT_ID, obj_userProfile).equalsIgnoreCase("0")) {
                    homeFragment.multiServiceSelect.clear();
                    homeFragment.backImgView.setVisibility(View.GONE);
                    homeFragment.manageToolBarAddressView(false);
                    homeFragment.uberXHomeActivity.rduTopArea.setVisibility(View.VISIBLE);
                    homeFragment.MainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    homeFragment.MainTopArea.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    homeFragment.bannerArea.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    homeFragment.selectServiceTxt.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    homeFragment.configCategoryView();
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (homeFragment != null && isHomeFrg) {
            homeFragment.onActivityResult(requestCode, resultCode, data);
        } else if (homeDaynamic_22_fragment != null && isHomeFrg) {
            homeDaynamic_22_fragment.onActivityResult(requestCode, resultCode, data);
        } else if (homeDaynamicFragment != null && isHomeFrg) {
            homeDaynamicFragment.onActivityResult(requestCode, resultCode, data);
        } else if (myWalletFragment != null && isWalletfrg) {
            myWalletFragment.onActivityResult(requestCode, resultCode, data);
        } else if (myProfileFragment != null && isProfilefrg) {
            myProfileFragment.onActivityResult(requestCode, resultCode, data);
        } else if (myBookingFragment != null && isBookingfrg) {
            myBookingFragment.onActivityResult(requestCode, resultCode, data);
        }

    }

    public Context getActContext() {
        return UberXHomeActivity.this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (homeFragment != null && isHomeFrg) {
            homeFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (homeDaynamicFragment != null && isHomeFrg) {
            homeDaynamicFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (homeDaynamic_22_fragment != null && isHomeFrg) {
            homeDaynamic_22_fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
