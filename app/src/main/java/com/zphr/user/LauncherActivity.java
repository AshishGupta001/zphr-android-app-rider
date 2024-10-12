package com.zphr.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;

import com.activity.ParentActivity;
import com.general.call.CommunicationManager;
import com.general.files.ActUtils;
import com.general.files.ConfigureMemberData;
import com.general.files.GeneralFunctions;
import com.general.files.GetHomeScreenData;
import com.general.files.MyApp;
import com.general.files.OpenMainProfile;
import com.general.files.SetGeneralData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.snackbar.Snackbar;
import com.model.ServiceModule;
import com.service.handler.ApiHandler;
import com.service.server.ServerTask;
import com.utils.Logger;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class LauncherActivity extends ParentActivity implements ProviderInstaller.ProviderInstallListener {

    private GenerateAlertBox currentAlertBox;
    private AVLoadingIndicatorView loaderView;
    private static final int ERROR_DIALOG_REQUEST_CODE = 1;
    private long autoLoginStartTime = 0;
    private String response_str_generalConfigData = "", response_str_autologin = "";
    private boolean mRetryProviderInstall;
    private MTextView drawOverMsgTxtView;
    private String LBL_BTN_OK_TXT, LBL_CANCEL_TXT, LBL_RETRY_TXT, LBL_TRY_AGAIN_TXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Logger.d("Test", "::" + getApplicationContext().getPackageName());

        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);
        drawOverMsgTxtView = (MTextView) findViewById(R.id.drawOverMsgTxtView);

        generalFunc.storeData("isInLauncher", "true");
        drawOverMsgTxtView.setText(generalFunc.retrieveLangLBl("Please wait while we are checking app's configuration. This will take few seconds.", "LBL_DRAW_OVER_APP_NOTE"));

        LBL_RETRY_TXT = generalFunc.retrieveLangLBl("Retry", "LBL_RETRY_TXT");
        LBL_CANCEL_TXT = generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT");
        LBL_BTN_OK_TXT = generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT");
        LBL_TRY_AGAIN_TXT = generalFunc.retrieveLangLBl("Please try again.", "LBL_TRY_AGAIN_TXT");

        ProviderInstaller.installIfNeededAsync(this, this);

        if (!generalFunc.getMemberId().equalsIgnoreCase("")) {
            new GetHomeScreenData().getHomescreenData(this, generalFunc, obj_userProfile);
        }
    }

    private void checkConfigurations() {
        drawOverMsgTxtView.setVisibility(View.GONE);
        closeAlert();

        int status = (GoogleApiAvailability.getInstance()).isGooglePlayServicesAvailable(getActContext());
        if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            showErrorOnPlayServiceDialog(generalFunc.retrieveLangLBl("This application requires updated google play service. " +
                    "Please install Or update it from play store", "LBL_UPDATE_PLAY_SERVICE_NOTE"));
            return;
        } else if (status != ConnectionResult.SUCCESS) {
            showErrorOnPlayServiceDialog(generalFunc.retrieveLangLBl("This application requires updated google play service. " +
                    "Please install Or update it from play store", "LBL_UPDATE_PLAY_SERVICE_NOTE"));
            return;
        }
        if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
            showNoInternetDialog();
        }
        continueProcess();
    }

    private void continueProcess() {
        closeAlert();
        showLoader();

        Utils.setAppLocal(getActContext());
        if (generalFunc.isUserLoggedIn() && Utils.checkText(generalFunc.getMemberId())) {
            if (this.response_str_autologin.trim().equalsIgnoreCase("")) {
                CommunicationManager.getInstance().initiateService(generalFunc, generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                new OpenMainProfile(getActContext(), obj_userProfile.toString(), true, generalFunc).startProcess();
                autoLogin();
            } else {
                continueAutoLogin(this.response_str_autologin);
            }
        } else {
            if (this.response_str_generalConfigData.trim().equalsIgnoreCase("")) {
                downloadGeneralData();
            } else {
                continueDownloadGeneralData(this.response_str_generalConfigData);
            }
        }
    }

    private void reStartAppDialog() {
        closeAlert();
        generalFunc.showGeneralMessage("", LBL_TRY_AGAIN_TXT, LBL_BTN_OK_TXT, "", buttonId -> generalFunc.restartApp());
    }

    private void downloadGeneralData() {
        closeAlert();
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "generalConfigData");
        parameters.put("UserType", Utils.app_type);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);
        parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        parameters.put("vCurrency", generalFunc.retrieveValue(Utils.DEFAULT_CURRENCY_VALUE));
        //parameters.putAll(GetFeatureClassList.getAllGeneralClasses());

        ApiHandler.execute(getActContext(), parameters, responseString -> {

            if (isFinishing()) {
                reStartAppDialog();
                return;
            }
            if (responseString != null && !responseString.equals("")) {

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {
                    response_str_generalConfigData = responseString;
                    continueDownloadGeneralData(responseString);
                } else {
                    String isAppUpdate = generalFunc.getJsonValue("isAppUpdate", responseString);
                    if (!isAppUpdate.trim().equals("") && isAppUpdate.equals("true")) {
                        showAppUpdateDialog(generalFunc.retrieveLangLBl("New update is available to download. " +
                                        "Downloading the latest update, you will get latest features, improvements and bug fixes.",
                                generalFunc.getJsonValue(Utils.message_str, responseString)));
                    } else {
                        String setMsg = LBL_TRY_AGAIN_TXT;
                        if (Utils.checkText(generalFunc.getJsonValue(Utils.message_str, responseString))) {
                            setMsg = generalFunc.getJsonValue(Utils.message_str, responseString);
                        }
                        currentAlertBox = generalFunc.showGeneralMessage("", setMsg, LBL_CANCEL_TXT, LBL_RETRY_TXT, buttonId -> {
                            if (buttonId == 1) {
                                continueProcess();
                            }
                        });
                    }
                }
            } else {
                showError();
            }
        });

    }

    private void continueDownloadGeneralData(String responseString) {
        JSONObject responseObj = generalFunc.getJsonObject(responseString);

        Utils.setAppLocal(getActContext());
        closeLoader();

        generalFunc.storeData(Utils.USER_PROFILE_JSON, responseString);
        generalFunc.storeData("TSITE_DB", generalFunc.getJsonValue("TSITE_DB", responseString));
        generalFunc.storeData("GOOGLE_API_REPLACEMENT_URL", generalFunc.getJsonValue("GOOGLE_API_REPLACEMENT_URL", responseString));
        generalFunc.storeData("APP_LAUNCH_IMAGES", generalFunc.getJsonValue("APP_LAUNCH_IMAGES", responseString));
        new SetGeneralData(generalFunc, responseObj);

        if (generalFunc.getJsonValue("SERVER_MAINTENANCE_ENABLE", responseString).equalsIgnoreCase("Yes")) {
            new ActUtils(getActContext()).startAct(MaintenanceActivity.class);
            finish();
            return;
        }
        MyApp.getInstance().updateLangForAllServiceType(generalFunc, responseString);

        if (generalFunc.isDeliverOnlyEnabled()) {
            GeneralFunctions.clearAndResetLanguageLabelsData(MyApp.getInstance().getApplicationContext());

            JSONArray serviceArray = generalFunc.getJsonArray("ServiceCategories", responseString);

            int serviceArrLength = serviceArray.length();
            if (serviceArrLength > 1) {

                ArrayList<HashMap<String, String>> list_item = new ArrayList<>();
                for (int i = 0; i < serviceArrLength; i++) {
                    JSONObject serviceObj = generalFunc.getJsonObject(serviceArray, i);
                    HashMap<String, String> serviceMap = new HashMap<>();
                    serviceMap.put("iServiceId", generalFunc.getJsonValue("iServiceId", serviceObj.toString()));
                    serviceMap.put("vServiceName", generalFunc.getJsonValue("vServiceName", serviceObj.toString()));
                    serviceMap.put("vImage", generalFunc.getJsonValue("vImage", serviceObj.toString()));
                    serviceMap.put("iCompanyId", generalFunc.getJsonValue("STORE_ID", serviceObj.toString()));
                    String eShowTerms = generalFunc.getJsonValueStr("eShowTerms", serviceObj);
                    serviceMap.put("eShowTerms", Utils.checkText(eShowTerms) ? eShowTerms : "");
                    serviceMap.put("vCategory", generalFunc.getJsonValue("vService", serviceObj.toString()));
                    serviceMap.put("ispriceshow", generalFunc.getJsonValue("ispriceshow", serviceObj.toString()));
                    list_item.add(serviceMap);
                }
                ServiceModule.configure();
                Bundle bn = new Bundle();
                bn.putSerializable("servicedata", list_item);

                //new ActUtils(getActContext()).startActWithData(ServiceHomeActivity.class, bn);
                new ActUtils(getActContext()).startActWithData(UberXHomeActivity.class, bn);
            } else {
                //new ActUtils(getActContext()).startAct(FoodDeliveryHomeActivity.class);
                Bundle bn = new Bundle();
                bn.putBoolean("isfoodOnly", true);
                bn.putString("iCompanyId", generalFunc.getJsonValue("STORE_ID", responseString));
                bn.putString("ispriceshow", generalFunc.getJsonValue("ispriceshow", responseString));
                //new ActUtils(getActContext()).startActWithData(ServiceHomeActivity.class, bn);

            }
        } else {
            Bundle bn = new Bundle();
            bn.putBoolean("isAnimated", true);
            new ActUtils(getActContext()).startActWithData(AppLoginActivity.class, bn);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        try {
            ActivityCompat.finishAffinity(LauncherActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoLogin() {
        closeAlert();
        autoLoginStartTime = Calendar.getInstance().getTimeInMillis();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "getDetail");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);
        parameters.put("UserType", Utils.app_type);
        if (obj_userProfile != null) {
            parameters.put("OLD_PROFILE_RESPONSE", obj_userProfile + "");
        }
        if (!generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY).equalsIgnoreCase("")) {
            parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        }

        ApiHandler.execute(getActContext(), parameters, false, true, generalFunc, responseString -> {

//            closeLoader();
//            if (isFinishing()) {
//                return;
//            }

            if (responseString != null && !responseString.equals("")) {

                if (generalFunc.getJsonValue("changeLangCode", responseString).equalsIgnoreCase("Yes")) {
                    new ConfigureMemberData(responseString, generalFunc, getActContext(), false);
                }
                String message = generalFunc.getJsonValue(Utils.message_str, responseString);


                if (message.equals("SESSION_OUT")) {
                    autoLoginStartTime = 0;
                    MyApp.getInstance().notifySessionTimeOut();
                    Utils.runGC();
                    return;
                }

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {

                    generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
                    if (generalFunc.getJsonValue("UPDATE_USER_DATA", responseString).equals("No")) {
                        return;
                    }

                    MyApp.getInstance().openSessionLoaderView();

                    new Handler().postDelayed(() -> {
                        response_str_autologin = responseString;
                        MyApp.getInstance().updateLangForAllServiceType(generalFunc, message);
                        continueAutoLogin(responseString);
                        MyApp.getInstance().closeSessionLoaderView();
                    }, 2000);

                } else {
                    autoLoginStartTime = 0;
                    if (!generalFunc.getJsonValue("isAppUpdate", responseString).trim().equals("")
                            && generalFunc.getJsonValue("isAppUpdate", responseString).equals("true")) {

                        showAppUpdateDialog(generalFunc.retrieveLangLBl("New update is available to download. " +
                                        "Downloading the latest update, you will get latest features, improvements and bug fixes.",
                                generalFunc.getJsonValue(Utils.message_str, responseString)));
                    } else {
                        if (generalFunc.getJsonValue(Utils.message_str, responseString).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_PASSENGER") ||
                                generalFunc.getJsonValue(Utils.message_str, responseString).equalsIgnoreCase("LBL_ACC_DELETE_TXT")) {

                            showContactUs(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                            return;
                        }
                        showError("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                }
            } else {
                autoLoginStartTime = 0;
                showError();
            }
        });

    }

    private void continueAutoLogin(String responseString) {
        String message = generalFunc.getJsonValue(Utils.message_str, responseString);
        if (generalFunc.getJsonValue("SERVER_MAINTENANCE_ENABLE", message).equalsIgnoreCase("Yes")) {
            new ActUtils(getActContext()).startAct(MaintenanceActivity.class);
            finish();
            return;
        }

        generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
        ServerTask.MAPS_API_REPLACEMENT_STRATEGY = generalFunc.getJsonValue("MAPS_API_REPLACEMENT_STRATEGY", message);
        generalFunc.storeData("MAPS_API_REPLACEMENT_STRATEGY", ServerTask.MAPS_API_REPLACEMENT_STRATEGY);
        generalFunc.storeData("CHECK_SYSTEM_STORE_SELECTION", generalFunc.getJsonValue("CHECK_SYSTEM_STORE_SELECTION", message));
        generalFunc.storeData("TSITE_DB", generalFunc.getJsonValue("TSITE_DB", responseString));
        generalFunc.storeData("GOOGLE_API_REPLACEMENT_URL", generalFunc.getJsonValue("GOOGLE_API_REPLACEMENT_URL", responseString));
        generalFunc.storeData(Utils.ENABLE_GOPAY_KEY, generalFunc.getJsonValue(Utils.ENABLE_GOPAY_KEY, message));
        generalFunc.storeData(Utils.DELIVERALL_KEY, generalFunc.getJsonValue("DELIVERALL", message));
        generalFunc.storeData(Utils.ONLYDELIVERALL_KEY, generalFunc.getJsonValue("ONLYDELIVERALL", message));

        CommunicationManager.getInstance().initiateService(generalFunc, generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        if (generalFunc.isDeliverOnlyEnabled()) {
            boolean isEmailBlankAndOptional = generalFunc.isEmailBlankAndOptional(generalFunc, generalFunc.getJsonValue("vEmail", message));
            if (generalFunc.getJsonValue("vPhone", message).equals("") || (generalFunc.getJsonValue("vEmail", message).equals("") && !isEmailBlankAndOptional)) {
                Bundle bn = new Bundle();
                bn.putBoolean("isRestart", true);
                new ActUtils(getActContext()).startActForResult(AccountverificationActivity.class, bn, Utils.SOCIAL_LOGIN_REQ_CODE);
                return;
            }

            String ePhoneVerified = generalFunc.getJsonValue("ePhoneVerified", message);
            String vPhoneCode = generalFunc.getJsonValue("vPhoneCode", message);
            String vPhone = generalFunc.getJsonValue("vPhone", message);
            if (!ePhoneVerified.equals("Yes")) {
                Bundle bn = new Bundle();
                bn.putString("MOBILE", vPhoneCode + vPhone);
                bn.putString("msg", "DO_PHONE_VERIFY");
                bn.putBoolean("isrestart", true);
                bn.putString("isbackshow", "No");
                new ActUtils(getActContext()).startActForResult(VerifyInfoActivity.class, bn, Utils.VERIFY_MOBILE_REQ_CODE);
                return;
            }

            JSONArray serviceArray = generalFunc.getJsonArray("ServiceCategories", message);

            int serviceArrLength = serviceArray != null ? serviceArray.length() : 0;
            if (serviceArrLength > 1 && generalFunc.isAnyDeliverOptionEnabled()) {

                ArrayList<HashMap<String, String>> list_item = new ArrayList<>();

                for (int i = 0; i < serviceArrLength; i++) {
                    JSONObject serviceObj = generalFunc.getJsonObject(serviceArray, i);
                    HashMap<String, String> servicemap = new HashMap<>();
                    servicemap.put("iServiceId", generalFunc.getJsonValue("iServiceId", serviceObj.toString()));
                    servicemap.put("vServiceName", generalFunc.getJsonValue("vServiceName", serviceObj.toString()));
                    servicemap.put("vImage", generalFunc.getJsonValue("vImage", serviceObj.toString()));
                    servicemap.put("iCompanyId", generalFunc.getJsonValue("STORE_ID", serviceObj.toString()));
                    String eShowTerms = generalFunc.getJsonValueStr("eShowTerms", serviceObj);
                    servicemap.put("eShowTerms", Utils.checkText(eShowTerms) ? eShowTerms : "");
                    servicemap.put("vCategory", generalFunc.getJsonValue("vService", serviceObj.toString()));
                    servicemap.put("ispriceshow", generalFunc.getJsonValue("ispriceshow", serviceObj.toString()));
                    list_item.add(servicemap);
                }
                ServiceModule.configure();
                Bundle bn = new Bundle();
                bn.putSerializable("servicedata", list_item);

                // new ActUtils(getActContext()).startActWithData(ServiceHomeActivity.class, bn);
                new ActUtils(getActContext()).startActWithData(UberXHomeActivity.class, bn);
                try {
                    ActivityCompat.finishAffinity(LauncherActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Bundle bn = new Bundle();
                bn.putBoolean("isfoodOnly", true);
                bn.putString("iCompanyId", generalFunc.getJsonValue("STORE_ID", message));
                bn.putString("ispriceshow", generalFunc.getJsonValue("ispriceshow", message));

                //new ActUtils(getActContext()).startActWithData(ServiceHomeActivity.class, bn);
                // new ActUtils(getActContext()).startAct(FoodDeliveryHomeActivity.class);
                try {
                    ActivityCompat.finishAffinity(LauncherActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (Calendar.getInstance().getTimeInMillis() - autoLoginStartTime < 2000) {
                new Handler(Looper.myLooper()).postDelayed(() -> new OpenMainProfile(getActContext(),
                        generalFunc.getJsonValue(Utils.message_str, responseString), true, generalFunc).startProcess(), 2000);
            } else {
                new OpenMainProfile(getActContext(),
                        generalFunc.getJsonValue(Utils.message_str, responseString), true, generalFunc).startProcess();
            }
        }
    }

    private void showLoader() {
        if (loaderView != null) {
            loaderView.setVisibility(View.VISIBLE);
        }
    }

    private void closeLoader() {
        if (loaderView != null) {
            loaderView.setVisibility(View.GONE);
        }
    }

    private void closeAlert() {
        try {
            if (currentAlertBox != null) {
                currentAlertBox.closeAlertBox();
                currentAlertBox = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContactUs(String content) {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage("", content, generalFunc.retrieveLangLBl("Contact Us", "LBL_CONTACT_US_TXT"), LBL_BTN_OK_TXT, buttonId -> {
            if (buttonId == 0) {
                new ActUtils(getActContext()).startAct(ContactUsActivity.class);
                showContactUs(content);
            } else if (buttonId == 1) {
                MyApp.getInstance().logOutFromDevice(true);
            }
        });
    }

    private void showError() {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage("", LBL_TRY_AGAIN_TXT, LBL_CANCEL_TXT, LBL_RETRY_TXT, buttonId -> handleBtnClick(buttonId, "ERROR"));
    }

    private void showError(String title, String contentMsg) {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage(title, contentMsg, LBL_CANCEL_TXT, LBL_RETRY_TXT, buttonId -> handleBtnClick(buttonId, "ERROR"));
    }

    private void showNoInternetDialog() {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"), LBL_CANCEL_TXT, LBL_RETRY_TXT, buttonId -> handleBtnClick(buttonId, "NO_INTERNET"));
    }


    private void showErrorOnPlayServiceDialog(String content) {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage("", content, LBL_RETRY_TXT, generalFunc.retrieveLangLBl("Update", "LBL_UPDATE"), buttonId -> handleBtnClick(buttonId, "NO_PLAY_SERVICE"));
    }

    private void showAppUpdateDialog(String content) {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("New update available", "LBL_NEW_UPDATE_AVAIL"), content, LBL_RETRY_TXT, generalFunc.retrieveLangLBl("Update", "LBL_UPDATE"), buttonId -> handleBtnClick(buttonId, "APP_UPDATE"));
    }

    private Context getActContext() {
        return LauncherActivity.this;
    }

    public void handleBtnClick(int buttonId, String alertType) {
        Logger.d("alertType", alertType);
        Utils.hideKeyboard(getActContext());
        if (buttonId == 0) {
            if (!alertType.equals("NO_PLAY_SERVICE") && !alertType.equals("APP_UPDATE")) {
                finish();
            } else {
                checkConfigurations();
            }
        } else {
            if (alertType.equals("NO_PLAY_SERVICE")) {
                boolean isSuccessfulOpen = new ActUtils(getActContext()).openURL("market://details?id=com.google.android.gms");
                if (!isSuccessfulOpen) {
                    new ActUtils(getActContext()).openURL("http://play.google.com/store/apps/details?id=com.google.android.gms");
                }
                checkConfigurations();
            } else if (alertType.equals("NO_PERMISSION")) {
                generalFunc.openSettings();
            } else if (alertType.equals("APP_UPDATE")) {
                boolean isSuccessfulOpen = new ActUtils(getActContext()).openURL("market://details?id=" + BuildConfig.APPLICATION_ID);
                if (!isSuccessfulOpen) {
                    new ActUtils(getActContext()).openURL("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                }
                checkConfigurations();
            } else if (!alertType.equals("NO_GPS")) {
                checkConfigurations();
            } else {
                new ActUtils(getActContext()).startActForResult(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Utils.REQUEST_CODE_GPS_ON);
            }
        }
    }

    @Override
    public void onResume() {
        if (generalFunc.prefHasKey(Utils.iServiceId_KEY) && generalFunc != null /*&& !generalFunc.isDeliverOnlyEnabled()*/) {
            generalFunc.removeValue(Utils.iServiceId_KEY);
        }
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.REQUEST_CODE_GPS_ON:
            case GeneralFunctions.MY_SETTINGS_REQUEST:
                checkConfigurations();
                break;
            case ERROR_DIALOG_REQUEST_CODE:
                mRetryProviderInstall = true;
                break;
            case Utils.OVERLAY_PERMISSION_REQ_CODE:
                drawOverMsgTxtView.setVisibility(View.GONE);
                if (!generalFunc.canDrawOverlayViews(getActContext())) {
                    drawOverMsgTxtView.setVisibility(View.VISIBLE);
                    new Handler(Looper.myLooper()).postDelayed(this::checkConfigurations, 15000);
                } else {
                    checkConfigurations();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        generalFunc.storeData("isInLauncher", "false");
    }

    @Override
    public void onProviderInstalled() {
        checkConfigurations();
    }

    @Override
    public void onProviderInstallFailed(int errorCode, Intent intent) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            GooglePlayServicesUtil.showErrorDialogFragment(errorCode, this, ERROR_DIALOG_REQUEST_CODE,
                    dialog -> {
                        // The user chose not to take the recovery action
                        onProviderInstallerNotAvailable();
                    });
        } else {
            // Google Play services is not available.
            onProviderInstallerNotAvailable();
        }
    }

    private void onProviderInstallerNotAvailable() {
        // This is reached if the provider cannot be updated for some reason.
        // App should consider all HTTP communication to be vulnerable, and take
        // appropriate action.
        checkConfigurations();
        showMessageWithAction(drawOverMsgTxtView, generalFunc.retrieveLangLBl("provider cannot be updated for some reason.", "LBL_PROVIDER_NOT_AVALIABLE_TXT"));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mRetryProviderInstall) {
            ProviderInstaller.installIfNeededAsync(this, this);
        }
        mRetryProviderInstall = false;
    }

    private void showMessageWithAction(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setDuration(10000);
        snackbar.show();
    }
}