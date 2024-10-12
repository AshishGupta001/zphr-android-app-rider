package com.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.zphr.user.MainActivity;
import com.zphr.user.R;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import java.util.HashMap;

/**
 * Created by Admin on 11-07-2016.
 */
public class RequestNearestCab implements Runnable, GenerateAlertBox.HandleAlertBtnClick {

    private final Context mContext;
    private final GeneralFunctions generalFunc;
    public Dialog dialogRequestNearestCab;
    private GenerateAlertBox generateAlert;
    private String driverIds, cabRequestedJson;
    private boolean isCancelBtnClick = false;

    public RequestNearestCab(Context mContext, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.generalFunc = generalFunc;
    }

    public void setRequestData(String driverIds, String cabRequestedJson) {
        this.driverIds = driverIds;
        this.cabRequestedJson = cabRequestedJson;
    }

    @Override
    public void run() {
        dialogRequestNearestCab = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
        dialogRequestNearestCab.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogRequestNearestCab.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogRequestNearestCab.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialogRequestNearestCab.setContentView(R.layout.design_request_nearest_cab_dialog);

        MButton btn_type2 = ((MaterialRippleLayout) dialogRequestNearestCab.findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"));


        if (generalFunc.isRTLmode()) {
            dialogRequestNearestCab.findViewById(R.id.retryBtnArea).setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            dialogRequestNearestCab.findViewById(R.id.retryArea).setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        String retryMsg = generalFunc.retrieveLangLBl("Driver is not able to take your request. Please cancel request and try again OR retry.", "LBL_NOTE_NO_DRIVER_REQUEST");

        String headrMsg = "";

        ((MTextView) dialogRequestNearestCab.findViewById(R.id.noDriverNotifyTxt)).setText(retryMsg);
        if (mContext != null) {
            if (mContext instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) mContext;
                if (mainActivity.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                    headrMsg = generalFunc.retrieveLangLBl("Finding A Provider", "LBL_FINDING_PROVIDER_HEADER_TXT");
                    retryMsg = generalFunc.retrieveLangLBl("Driver is not able to take your request. Please cancel request and try again OR retry.", "LBL_NOTE_NO_PROVIDER_REQUEST");
                    ((MTextView) dialogRequestNearestCab.findViewById(R.id.noDriverNotifyTxt)).setText(retryMsg);
                } else if (mainActivity.getCurrentCabGeneralType().equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                    headrMsg = generalFunc.retrieveLangLBl("Finding A Driver", "LBL_FINDING_DRIVER_HEADER_TXT");
                    retryMsg = generalFunc.retrieveLangLBl("Driver is not able to take your request. Please cancel request and try again OR retry.", "LBL_NOTE_NO_DRIVER_REQUEST");
                    ((MTextView) dialogRequestNearestCab.findViewById(R.id.noDriverNotifyTxt)).setText(retryMsg);
                } else {
                    headrMsg = generalFunc.retrieveLangLBl("Finding A Carrier", "LBL_FINDING_CARRIER_HEADER_TXT");
                    retryMsg = generalFunc.retrieveLangLBl("Driver is not able to take your request. Please cancel request and try again OR retry.", "LBL_NOTE_NO_CARRIER_REQUEST");
                    ((MTextView) dialogRequestNearestCab.findViewById(R.id.noDriverNotifyTxt)).setText(retryMsg);
                }
            }
        }

        RadarView mRadarView = dialogRequestNearestCab.findViewById(R.id.radarView);
        mRadarView.setShowCircles(true);
        mRadarView.startAnimation();

        TextView noDriverText = dialogRequestNearestCab.findViewById(R.id.noDriverText);
        TextView headerText = dialogRequestNearestCab.findViewById(R.id.headerText);
        TextView reqNoteText = dialogRequestNearestCab.findViewById(R.id.reqNoteText);
        TextView retryText = dialogRequestNearestCab.findViewById(R.id.retryText);
        TextView cancelText = dialogRequestNearestCab.findViewById(R.id.cancelText);
        TextView reqcancelText = dialogRequestNearestCab.findViewById(R.id.reqcancelText);

        cancelText.setText(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));
        reqcancelText.setText(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));
        retryText.setText(generalFunc.retrieveLangLBl("Retry", "LBL_RETRY_TXT"));
        headerText.setText(generalFunc.retrieveLangLBl("", "LBL_REQUESTING_TXT"));
        reqNoteText.setText(headrMsg);
        noDriverText.setText(retryMsg);

        retryText.setOnClickListener(v -> {
            if (mContext instanceof MainActivity) {
                ((MainActivity) mContext).retryReqBtnPressed(driverIds, cabRequestedJson);
            }
        });
        cancelText.setOnClickListener(v -> {
            isCancelBtnClick = true;
            cancelRequestConfirm();
        });
        reqcancelText.setOnClickListener(v -> {
            isCancelBtnClick = true;
            cancelRequestConfirm();
        });

        ((ProgressBar) dialogRequestNearestCab.findViewById(R.id.mProgressBar)).setIndeterminate(true);
        dialogRequestNearestCab.setCancelable(false);
        dialogRequestNearestCab.setCanceledOnTouchOutside(false);
        try {
            dialogRequestNearestCab.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        (dialogRequestNearestCab.findViewById(R.id.cancelImgView)).setOnClickListener(view -> {
            if (!isCancelBtnClick) {
                isCancelBtnClick = true;
                cancelRequestConfirm();
            }
        });

        if (mContext != null) {
            ((ProgressBar) dialogRequestNearestCab.findViewById(R.id.mProgressBar)).getIndeterminateDrawable().setColorFilter(
                    mContext.getResources().getColor(R.color.appThemeColor_2), android.graphics.PorterDuff.Mode.SRC_IN);
        }


        btn_type2.setOnClickListener(view -> {
            if (mContext instanceof MainActivity) {
                ((MainActivity) mContext).retryReqBtnPressed(driverIds, cabRequestedJson);
            }
        });
    }

    public void setVisibleBottomArea(int visibility, boolean isDeclineFromDriver) {
        // Animation bottomUp = AnimationUtils.loadAnimation(mContext, R.anim.bottom_up);
        LinearLayout layout = dialogRequestNearestCab.findViewById(R.id.retryArea);
        if (isDeclineFromDriver) {
            String declineMsg = generalFunc.retrieveLangLBl("", "LBL_PROVIDER_DECLINED_REQUEST_MSG");
            ((TextView) dialogRequestNearestCab.findViewById(R.id.noDriverText)).setText(declineMsg);
        } else {
            String retryMsg = generalFunc.retrieveLangLBl("", "LBL_NOTE_NO_PROVIDER_REQUEST");
            ((TextView) dialogRequestNearestCab.findViewById(R.id.noDriverText)).setText(retryMsg);
        }
        layout.setVisibility(visibility);

        if (layout.getVisibility() == View.VISIBLE) {
            LinearLayout reqLayout = dialogRequestNearestCab.findViewById(R.id.reqNoteArea);
            reqLayout.setVisibility(View.GONE);
        } else {
            LinearLayout reqLayout = dialogRequestNearestCab.findViewById(R.id.reqNoteArea);
            reqLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setInVisibleBottomArea(int visibility) {
        //  Animation bottomUp = AnimationUtils.loadAnimation(mContext, R.anim.bottom_up);
        LinearLayout layout = dialogRequestNearestCab.findViewById(R.id.retryArea);
        layout.setVisibility(visibility);

        if (layout.getVisibility() == View.VISIBLE) {
            LinearLayout reqLayout = dialogRequestNearestCab.findViewById(R.id.reqNoteArea);
            reqLayout.setVisibility(View.GONE);
        } else {
            LinearLayout reqLayout = dialogRequestNearestCab.findViewById(R.id.reqNoteArea);
            reqLayout.setVisibility(View.VISIBLE);
        }
    }

    public void dismissDialog() {
        if (dialogRequestNearestCab != null && dialogRequestNearestCab.isShowing()) {
            dialogRequestNearestCab.dismiss();
        }
    }

    private void releaseMainTask() {
        if (mContext != null && mContext instanceof MainActivity) {
            ((MainActivity) mContext).releaseScheduleNotificationTask();
        }
    }

    private void cancelRequestConfirm() {
        if (generateAlert != null) {
            generateAlert.closeAlertBox();
            generateAlert = null;
        }
        generateAlert = new GenerateAlertBox(mContext);
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(this);
        generateAlert.setContentMessage("",
                generalFunc.retrieveLangLBl("", "LBL_CONFIRM_REQUEST_CANCEL_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_TRIP_CANCEL_CONFIRM_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));
        generateAlert.showAlertBox();
    }

    @Override
    public void handleBtnClick(int btn_id) {
        if (btn_id == 0) {
            isCancelBtnClick = false;
            if (generateAlert != null) {
                generateAlert.closeAlertBox();
                generateAlert = null;
            }
        } else {
            if (generateAlert != null) {
                generateAlert.closeAlertBox();
                generateAlert = null;
            }
            cancelRequest();
        }
    }

    private void cancelRequest() {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "cancelCabRequest");
        parameters.put("iUserId", generalFunc.getMemberId());

        ApiHandler.execute(mContext, parameters, true, false, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {
                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {
                    manageAfterCancelCabRequest("");
                } else {
                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);

                    if (message.equals("DO_RESTART") || message.equals(Utils.GCM_FAILED_KEY) || message.equals(Utils.APNS_FAILED_KEY) || message.equals("LBL_SERVER_COMM_ERROR")) {
                        manageAfterCancelCabRequest(message);
                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                }
            } else {
                generalFunc.showError();
            }
        });

    }

    private void manageAfterCancelCabRequest(String message) {
        dismissDialog();
        releaseMainTask();

        if (Utils.checkText(message)) {
            generalFunc.restartApp();
        } else {
            if (mContext instanceof MainActivity) {
                MyApp.getInstance().restartWithGetDataApp();
            }
        }

    }
}