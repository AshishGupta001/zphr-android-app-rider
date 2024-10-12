package com.fragments;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zphr.user.MainActivity;
import com.zphr.user.R;
import com.dialogs.OpenListView;
import com.general.call.CommunicationManager;
import com.general.call.MediaDataProvider;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.MyApp;
import com.model.ServiceModule;
import com.service.handler.ApiHandler;
import com.utils.CommonUtilities;
import com.utils.LoadImage;
import com.utils.Logger;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.editBox.MaterialEditText;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverDetailFragment extends BaseFragment implements GetAddressFromLocation.AddressFound, ViewTreeObserver.OnGlobalLayoutListener {

    int PICK_CONTACT = 2121;

    View view;
    MainActivity mainAct;
    GeneralFunctions generalFunc;

    String driverPhoneNum = "";

    DriverDetailFragment driverDetailFragment;

    String userProfileJson;

    String vDeliveryConfirmCode = "";

    FrameLayout contactarea, cancelarea;
    // View contactview;
    SimpleRatingBar ratingBar;
    GetAddressFromLocation getAddressFromLocation;
    HashMap<String, String> tripDataMap;
    public int fragmentWidth = 0;
    public int fragmentHeight = 0;
    AlertDialog dialog_declineOrder;
    boolean isCancelTripWarning = true;
    String vImage = "";
    String vName = "";
    private String recipientNameTxt = "";

    private static final String TAG = "DriverDetailFragment";
    ImageView rlCall, rlMessage, rlCancel, rlShare, confirmationareacode;

    FrameLayout confirmationareacodearea;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.fragment_driver_detail, container, false);

        cancelarea = (FrameLayout) view.findViewById(R.id.cancelarea);
        contactarea = (FrameLayout) view.findViewById(R.id.contactarea);
        //contactview = (View) view.findViewById(R.id.contactview);
        ratingBar = (SimpleRatingBar) view.findViewById(R.id.ratingBar);


        rlCall = (ImageView) view.findViewById(R.id.rlCall);
        rlMessage = (ImageView) view.findViewById(R.id.rlMessage);
        rlCancel = (ImageView) view.findViewById(R.id.rlCancel);
        rlShare = (ImageView) view.findViewById(R.id.rlShare);
        confirmationareacode = (ImageView) view.findViewById(R.id.confirmationareacode);

        rlCall.setBackground(getRoundBG("#3cca59"));
        rlMessage.setBackground(getRoundBG("#027bff"));
        rlCancel.setBackground(getRoundBG("#ffffff"));
        rlShare.setBackground(getRoundBG("#ffa60a"));
        confirmationareacode.setBackground(getRoundBG("#f59842"));
        confirmationareacodearea = (view.findViewById(R.id.confirmationareacodearea));
        addToClickHandler(confirmationareacodearea);
        mainAct = (MainActivity) getActivity();
        userProfileJson = mainAct.obj_userProfile.toString();
        generalFunc = mainAct.generalFunc;


        getAddressFromLocation = new GetAddressFromLocation(getActivity(), generalFunc);
        getAddressFromLocation.setAddressList(this);

        setData();

        addGlobalLayoutListner();

        driverDetailFragment = mainAct.getDriverDetailFragment();

        mainAct.setDriverImgView(((SelectableRoundedImageView) view.findViewById(R.id.driverImgView)));

        if (generalFunc.getJsonValue("vTripStatus", userProfileJson).equals("On Going Trip")) {

            configTripStartView(vDeliveryConfirmCode);

        }
        return view;
    }

    private boolean isMultiDelivery() {
        if (tripDataMap == null) {
            this.tripDataMap = getTripData();
        }

        return tripDataMap.get("eType").equalsIgnoreCase(Utils.eType_Multi_Delivery);
    }

    public HashMap<String, String> getTripData() {

        HashMap<String, String> tripDataMap = (HashMap<String, String>) getArguments().getSerializable("TripData");


        return tripDataMap;
    }

    public void setData() {
        tripDataMap = (HashMap<String, String>) getArguments().getSerializable("TripData");

        ((MTextView) view.findViewById(R.id.driver_car_model)).setText(tripDataMap.get("DriverCarModelName"));

        if (tripDataMap.get("eFly") != null && tripDataMap.get("eFly").equalsIgnoreCase("Yes")) {
            (view.findViewById(R.id.sharearea)).setVisibility(View.GONE);
        }

        vName = tripDataMap.get("DriverName");
        String name = tripDataMap.get("DriverName");
        String carColor = tripDataMap.get("DriverCarColour");
        Log.d(TAG, "DriverName: " + name);

        ((MTextView) view.findViewById(R.id.driver_name)).setText(tripDataMap.get("DriverName"));
        ratingBar.setRating(generalFunc.parseFloatValue(0, tripDataMap.get("DriverRating")));
        ((MTextView) view.findViewById(R.id.driver_car_model)).setText(tripDataMap.get("DriverCarName") + " - " + tripDataMap.get("DriverCarModelName"));
        ((MTextView) view.findViewById(R.id.numberPlate_txt)).setText(tripDataMap.get("DriverCarPlateNum"));
        ((MTextView) view.findViewById(R.id.driver_car_type)).setText(Utils.checkText(carColor) ? carColor : tripDataMap.get("vVehicleType"));
        driverPhoneNum = tripDataMap.get("DriverPhone");
        vDeliveryConfirmCode = tripDataMap.get("vDeliveryConfirmCode");
        String driverImageName = tripDataMap.get("DriverImage");

        if (isMultiDelivery()) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) (mainAct.userLocBtnImgView).getLayoutParams();
            params.bottomMargin = Utils.dipToPixels(mainAct.getActContext(), 220);
        }

        if (isMultiDelivery()) {
            /*Set delivery recipient Detail*/
            recipientNameTxt = tripDataMap.get("recipientNameTxt");

            Logger.d("Api", "recipient Name" + recipientNameTxt);
            if (recipientNameTxt != null && Utils.checkText(recipientNameTxt)) {
                mainAct.setPanelHeight(175 + 30);
                view.findViewById(R.id.recipientNameArea).setVisibility(View.VISIBLE);
                ((MTextView) view.findViewById(R.id.recipientNameTxt)).setText(recipientNameTxt);
            }


            mainAct.setPanelHeight(205);
            mainAct.setUserLocImgBtnMargin(100 + 10);

            if (isMultiDelivery() && recipientNameTxt != null && Utils.checkText(recipientNameTxt) && Utils.checkText(vDeliveryConfirmCode)) {

                mainAct.setPanelHeight(205 + 30);
                mainAct.setUserLocImgBtnMargin(205 + 30 + 10);

            }
        }


        if (generalFunc.getJsonValueStr("eSignVerification", generalFunc.getJsonObject("TripDetails", userProfileJson)).equals("Yes")) {

            configTripStartView(vDeliveryConfirmCode);

        }

        if (driverImageName == null || driverImageName.equals("") || driverImageName.equals("NONE")) {
            ((SelectableRoundedImageView) view.findViewById(R.id.driverImgView)).setImageResource(R.mipmap.ic_no_pic_user);
            vImage = "";
        } else {
            String image_url = CommonUtilities.PROVIDER_PHOTO_PATH + tripDataMap.get("iDriverId") + "/"
                    + tripDataMap.get("DriverImage");
            vImage = image_url;

            new LoadImage.builder(LoadImage.bind(image_url), ((SelectableRoundedImageView) view.findViewById(R.id.driverImgView))).setErrorImagePath(R.mipmap.ic_no_pic_user).setPlaceholderImagePath(R.mipmap.ic_no_pic_user).build();

        }

        //  mainAct.registerForContextMenu(view.findViewById(R.id.contact_btn));
        addToClickHandler((view.findViewById(R.id.contactarea)));
        addToClickHandler((view.findViewById(R.id.sharearea)));
        addToClickHandler((view.findViewById(R.id.cancelarea)));
        addToClickHandler((view.findViewById(R.id.msgarea)));

    }

    public String getDriverPhone() {
        return driverPhoneNum;
    }

    public void configTripStartView(String vDeliveryConfirmCode) {

        cancelarea.setVisibility(View.GONE);
        if (mainAct != null && mainAct.driverAssignedHeaderFrag != null) {
            mainAct.driverAssignedHeaderFrag.otpInfoArea.setVisibility(View.GONE);
        }

        if (!vDeliveryConfirmCode.trim().equals("") && !ServiceModule.isServiceProviderOnly()) {

            mainAct.setUserLocImgBtnMargin(100);
            this.vDeliveryConfirmCode = vDeliveryConfirmCode;
            confirmationareacodearea.setVisibility(View.VISIBLE);
        }


        if (isMultiDelivery() && recipientNameTxt != null && Utils.checkText(recipientNameTxt) && Utils.checkText(vDeliveryConfirmCode)) {
            mainAct.setPanelHeight(205 + 30);
            mainAct.setUserLocImgBtnMargin(205 + 30 + 10);
            confirmationareacodearea.setVisibility(View.VISIBLE);
        }

    }

    public void sendMsg(String phoneNumber) {
        try {

            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", "" + phoneNumber);
            startActivity(smsIntent);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void call(String phoneNumber) {
        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }


    public void cancelTrip(String eConfirmByUser, String iCancelReasonId, String reason) {
        HashMap<String, String> tripDataMap = (HashMap<String, String>) getArguments().getSerializable("TripData");


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "cancelTrip");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iDriverId", tripDataMap.get("iDriverId"));
        parameters.put("iTripId", tripDataMap.get("iTripId"));
        parameters.put("eConfirmByUser", eConfirmByUser);
        parameters.put("iCancelReasonId", iCancelReasonId);
        parameters.put("Reason", reason);

        ApiHandler.execute(mainAct.getActContext(), parameters, true, false, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);
                if (isDataAvail) {

                    GenerateAlertBox generateAlert = new GenerateAlertBox(mainAct.getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id ->  MyApp.getInstance().refreshView(mainAct, responseString));
                    String msg = "";

                    if (tripDataMap.get("eType").equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                        msg = generalFunc.retrieveLangLBl("", "LBL_SUCCESS_TRIP_CANCELED");
                    } else if (tripDataMap.get("eType").equalsIgnoreCase("Deliver") || isMultiDelivery()) {
                        msg = generalFunc.retrieveLangLBl("", "LBL_SUCCESS_DELIVERY_CANCELED");

                    } else {
                        msg = generalFunc.retrieveLangLBl("", "LBL_SUCCESS_TRIP_CANCELED");
                    }
                    generateAlert.setContentMessage("", msg);
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                    generateAlert.showAlertBox();


                } else {

                    if (message.equals("DO_RESTART") || message.equals(Utils.GCM_FAILED_KEY) || message.equals(Utils.APNS_FAILED_KEY) || message.equals("LBL_SERVER_COMM_ERROR")) {

                        MyApp.getInstance().restartWithGetDataApp();
                        return;
                    }


                    if (generalFunc.getJsonValue("isCancelChargePopUpShow", responseString).equalsIgnoreCase("Yes")) {

                        final GenerateAlertBox generateAlert = new GenerateAlertBox(mainAct.getActContext());
                        generateAlert.setCancelable(false);
                        generateAlert.setBtnClickList(btn_id -> {
                            if (btn_id == 0) {
                                generateAlert.closeAlertBox();

                            } else {
                                generateAlert.closeAlertBox();
                                cancelTrip("Yes", iCancelReasonId, reason);

                            }

                        });
                        generateAlert.setContentMessage("", generalFunc.convertNumberWithRTL(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString))));
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
                        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
                        generateAlert.showAlertBox();

                        return;
                    }
                    isCancelTripWarning = false;
                }
            } else {
                generalFunc.showError();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT && data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = mainAct.getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.TYPE}, null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);

                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setType("vnd.android-dir/mms-sms");
                        smsIntent.putExtra("address", "" + number);

                        String link_location = "http://maps.google.com/?q=" + mainAct.userLocation.getLatitude() + "," + mainAct.userLocation.getLongitude();
                        smsIntent.putExtra("sms_body", generalFunc.retrieveLangLBl("", "LBL_SEND_STATUS_CONTENT_TXT") + " " + link_location);
                        startActivity(smsIntent);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());
    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject) {

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        String link_location = "";
        if (generalFunc.getJsonValue("liveTrackingUrl", userProfileJson).equalsIgnoreCase("")) {
            link_location = "http://maps.google.com/?q=" + address.replace(" ", "%20");
        } else {
            link_location = generalFunc.getJsonValue("liveTrackingUrl", userProfileJson);
        }
        //String link_location = "http://maps.google.com/?q=" + address.replace(" ", "%20");


        sharingIntent.putExtra(Intent.EXTRA_TEXT, generalFunc.retrieveLangLBl("", "LBL_SEND_STATUS_CONTENT_TXT") + " " + link_location);
        startActivity(Intent.createChooser(sharingIntent, "Share using"));

    }

    @Override
    public void onResume() {
        super.onResume();
        addGlobalLayoutListner();
    }

    @Override
    public void onGlobalLayout() {
        boolean heightChanged = false;
        if (getView() != null || view != null) {
            if (getView() != null) {

                if (getView().getHeight() != 0 && getView().getHeight() != fragmentHeight) {
                    heightChanged = true;
                }
                fragmentWidth = getView().getWidth();
                fragmentHeight = getView().getHeight();
            } else if (view != null) {

                if (view.getHeight() != 0 && view.getHeight() != fragmentHeight) {
                    heightChanged = true;
                }

                fragmentWidth = view.getWidth();
                fragmentHeight = view.getHeight();
            }

            Logger.e("FragHeight", "is :::" + fragmentHeight + "\n" + "Frag Width is :::" + fragmentWidth);

            if (heightChanged && fragmentWidth != 0 && fragmentHeight != 0) {
                mainAct.setPanelHeight(fragmentHeight);
            }
        }
    }

    private void addGlobalLayoutListner() {
        if (getView() != null) {
            getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
        if (view != null) {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        if (getView() != null) {
            getView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        } else if (view != null) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }

    public void getDeclineReasonsList() {
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "GetCancelReasons");
        parameters.put("iTripId", tripDataMap.get("iTripId"));
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eUserType", Utils.app_type);

        ApiHandler.execute(mainAct.getActContext(), parameters, true, false, generalFunc, responseString -> {

            if (!responseString.equals("")) {

                boolean isDataAvail = generalFunc.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    if (message.equals("DO_RESTART") || message.equals(Utils.GCM_FAILED_KEY) || message.equals(Utils.APNS_FAILED_KEY) || message.equals("LBL_SERVER_COMM_ERROR")) {

                        MyApp.getInstance().restartWithGetDataApp();
                    } else {
                        showDeclineReasonsAlert(responseString);
                    }
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }

            } else {
                generalFunc.showError();
            }

        });
    }


    int selCurrentPosition = -1;

    public void showDeclineReasonsAlert(String responseString) {
        if (dialog_declineOrder != null) {
            if (dialog_declineOrder.isShowing()) {
                dialog_declineOrder.dismiss();
            }
            dialog_declineOrder = null;
        }
        selCurrentPosition = -1;
        String titleDailog = "";
        if (tripDataMap.get("eType").equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_TRIP");
        } else if (tripDataMap.get("eType").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_BOOKING");
        } else {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_DELIVERY");
        }


        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mainAct);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.decline_order_dialog_design, null);
        builder.setView(dialogView);

        MaterialEditText reasonBox = (MaterialEditText) dialogView.findViewById(R.id.inputBox);
        RelativeLayout commentArea = (RelativeLayout) dialogView.findViewById(R.id.commentArea);
        reasonBox.setHideUnderline(true);
        if (generalFunc.isRTLmode()) {
            reasonBox.setPaddings(0, 0, (int) getResources().getDimension(R.dimen._10sdp), 0);
        } else {
            reasonBox.setPaddings((int) getResources().getDimension(R.dimen._10sdp), 0, 0, 0);
        }

        reasonBox.setSingleLine(false);
        reasonBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        reasonBox.setGravity(Gravity.TOP);

        reasonBox.setVisibility(View.GONE);
        commentArea.setVisibility(View.GONE);
        new CreateRoundedView(Color.parseColor("#ffffff"), 5, 1, Color.parseColor("#C5C3C3"), commentArea);

        reasonBox.setBothText("", generalFunc.retrieveLangLBl("", "LBL_ENTER_REASON"));


        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        JSONArray arr_msg = generalFunc.getJsonArray(Utils.message_str, responseString);
        if (arr_msg != null) {

            for (int i = 0; i < arr_msg.length(); i++) {

                JSONObject obj_tmp = generalFunc.getJsonObject(arr_msg, i);
                HashMap<String, String> datamap = new HashMap<>();
                datamap.put("title", generalFunc.getJsonValueStr("vTitle", obj_tmp));
                datamap.put("id", generalFunc.getJsonValueStr("iCancelReasonId", obj_tmp));
                list.add(datamap);
            }

            HashMap<String, String> othermap = new HashMap<>();
            othermap.put("title", generalFunc.retrieveLangLBl("", "LBL_OTHER_TXT"));
            othermap.put("id", "");
            list.add(othermap);

            MTextView cancelTxt = (MTextView) dialogView.findViewById(R.id.cancelTxt);
            MTextView submitTxt = (MTextView) dialogView.findViewById(R.id.submitTxt);
            MTextView subTitleTxt = (MTextView) dialogView.findViewById(R.id.subTitleTxt);
            ImageView cancelImg = (ImageView) dialogView.findViewById(R.id.cancelImg);
            subTitleTxt.setText(titleDailog);

            submitTxt.setText(generalFunc.retrieveLangLBl("", "LBL_YES"));
            cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_NO"));
            MTextView declinereasonBox = (MTextView) dialogView.findViewById(R.id.declinereasonBox);
            declinereasonBox.setText(generalFunc.retrieveLangLBl("Select Reason", "LBL_SELECT_CANCEL_REASON"));
            submitTxt.setClickable(false);
            submitTxt.setTextColor(getResources().getColor(R.color.gray_holo_light));

            submitTxt.setOnClickListener(v -> {


                if (selCurrentPosition == -1) {
                    return;
                }

                if (Utils.checkText(reasonBox) == false && selCurrentPosition == (list.size() - 1)) {
                    reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD"));
                    return;
                }


                cancelTrip("No", list.get(selCurrentPosition).get("id"), reasonBox.getText().toString().trim());

                dialog_declineOrder.dismiss();


            });
            cancelTxt.setOnClickListener(v -> {

                dialog_declineOrder.dismiss();
            });

            cancelImg.setOnClickListener(v -> {

                dialog_declineOrder.dismiss();
            });


            declinereasonBox.setOnClickListener(v -> OpenListView.getInstance(getActivity(), generalFunc.retrieveLangLBl("", "LBL_SELECT_REASON"), list, OpenListView.OpenDirection.CENTER, true, position -> {


                selCurrentPosition = position;
                HashMap<String, String> mapData = list.get(position);
                declinereasonBox.setText(mapData.get("title"));
                if (selCurrentPosition == (list.size() - 1)) {
                    reasonBox.setVisibility(View.VISIBLE);
                    commentArea.setVisibility(View.VISIBLE);
                } else {
                    reasonBox.setVisibility(View.GONE);
                    commentArea.setVisibility(View.GONE);
                }

                submitTxt.setClickable(true);
                submitTxt.setTextColor(getResources().getColor(R.color.white));


            }).show(selCurrentPosition, "title"));


            dialog_declineOrder = builder.create();
            dialog_declineOrder.setCancelable(false);
            dialog_declineOrder.getWindow().setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.all_roundcurve_card));
            if (generalFunc.isRTLmode()) {
                dialog_declineOrder.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
            dialog_declineOrder.show();

        } else {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NO_DATA_AVAIL"));
        }
    }


    public void onClickView(View view) {
        Utils.hideKeyboard(getActivity());
        switch (view.getId()) {
            case R.id.contactarea:
                MediaDataProvider mDataProvider = new MediaDataProvider.Builder()
                        .setCallId(tripDataMap.get("iDriverId"))
                        .setPhoneNumber(tripDataMap.get("DriverPhone"))
                        .setToMemberType(Utils.CALLTODRIVER)
                        .setToMemberName(tripDataMap.get("DriverName"))
                        .setToMemberImage(tripDataMap.get("DriverImage"))
                        .setMedia(tripDataMap.get("eBookingFrom").equalsIgnoreCase("Kiosk") ? CommunicationManager.MEDIA.DEFAULT : CommunicationManager.MEDIA_TYPE)
                        .setTripId(tripDataMap.get("iTripId"))
                        .build();
                CommunicationManager.getInstance().communicate(MyApp.getInstance().getCurrentAct(), mDataProvider, CommunicationManager.TYPE.OTHER);
                break;
            case R.id.sharearea:
                if (mainAct != null && mainAct.driverAssignedHeaderFrag != null && mainAct.driverAssignedHeaderFrag.driverLocation != null) {
                    getAddressFromLocation.setLocation(mainAct.driverAssignedHeaderFrag.driverLocation.latitude, mainAct.driverAssignedHeaderFrag.driverLocation.longitude);
                    getAddressFromLocation.setLoaderEnable(true);
                    getAddressFromLocation.execute();
                }

                break;

            case R.id.cancelarea:
                String msg = "";

                if (tripDataMap.get("eType").equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                    msg = generalFunc.retrieveLangLBl("", "LBL_TRIP_CANCEL_TXT");
                } else {
                    msg = generalFunc.retrieveLangLBl("", "LBL_DELIVERY_CANCEL_TXT");
                }

                isCancelTripWarning = true;
                getDeclineReasonsList();
                break;

            case R.id.msgarea:
                if (tripDataMap.get("eBookingFrom").equalsIgnoreCase("Kiosk")) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("smsto:" + Uri.encode(driverPhoneNum)));
                    startActivity(intent);
                } else {
                    mainAct.chatMsg();
                }
                break;
            case R.id.confirmationareacodearea:
                showCodeDialog();
                break;
        }
    }


    private void showCodeDialog() {

        // vDeliveryConfirmCode
        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Delivery Confirmation Code", "LBL_DELIVERY_CONFIRMATION_CODE_TXT"),
                generalFunc.retrieveLangLBl("", generalFunc.convertNumberWithRTL(vDeliveryConfirmCode)));


    }

    private GradientDrawable getRoundBG(String color) {

        int strokeWidth = 2;
        int strokeColor = Color.parseColor("#CCCACA");
        int fillColor = Color.parseColor(color);
        GradientDrawable gD = new GradientDrawable();
        gD.setColor(fillColor);
        gD.setShape(GradientDrawable.RECTANGLE);
        gD.setCornerRadius(100);
        gD.setStroke(strokeWidth, strokeColor);

        return gD;
    }


}
