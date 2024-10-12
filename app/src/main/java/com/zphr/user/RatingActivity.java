package com.zphr.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.activity.ParentActivity;
import com.general.files.ActUtils;
import com.general.files.CustomHorizontalScrollView;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.OnSwipeTouchListener;
import com.general.files.SetOnTouchList;
import com.general.files.SlideAnimationUtil;
import com.general.files.SuccessDialog;
import com.kyleduo.switchbutton.SwitchButton;
import com.service.handler.ApiHandler;
import com.service.server.ServerTask;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.DividerView;
import com.view.ErrorView;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.anim.loader.AVLoadingIndicatorView;
import com.view.editBox.MaterialEditText;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RatingActivity extends ParentActivity {

    MTextView titleTxt;
    ImageView backImgView;
    DividerView desdashImage;


    ProgressBar loading;
    ErrorView errorView;
    MButton btn_type2;
    MTextView generalCommentTxt;
    CardView generalCommentArea;
    MaterialEditText commentBox;

    int submitBtnId;

    LinearLayout container;

    SimpleRatingBar ratingBar;
    LinearLayout rideRatingArea;

    LinearLayout lineArea;
    LinearLayout safetyRatingArea;
    SimpleRatingBar safetyRatingBar;
    MTextView tripRatingTitleTxt;
    MTextView safetyRatingTitleTxt;
    CustomHorizontalScrollView hScrollView;
    String ShowSafetyRating = "";

    String iTripId_str;
    LinearLayout uberXRatingLayoutArea, mainRatingArea;
    androidx.appcompat.app.AlertDialog giveTipAlertDialog;

    MTextView totalFareTxt;
    MTextView dateVTxt;

    MTextView ratingHeaderTxt;
    float rating = 0;
    boolean isAnimated = false;

    String tipamount = "";
    boolean isCollectTip = false;

    boolean isUfx = false;
    boolean isFirst = false;


    LinearLayout farecontainer;

    MTextView walletNoteTxt;


    AVLoadingIndicatorView loaderView;
    WebView paymentWebview;

    /* Fav Driver variable declaration start */
    LinearLayout favArea;
    SwitchButton favSwitch;
    MTextView favDriverTitleTxt;

    int width;
    int width_D;
    int width_;
    /* Fav Driver variable declaration end */

    MTextView thanksNoteTxt, orderTxt, chargetxt;
    MTextView destAddressHTxt, sourceAddressHTxt;
    private static final int WEBVIEWPAYMENT = 001;
    View mainAddressArea;
    boolean isBid = false;

    private RelativeLayout rlSubmitView;
    private MTextView txtSkip, txtSubmit;
    private boolean isSkipRatingEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        isUfx = getIntent().getBooleanExtra("isUfx", false);
        isBid = getIntent().getBooleanExtra("isBid", false);
        isFirst = getIntent().getBooleanExtra("isFirst", false);


        mainAddressArea = findViewById(R.id.mainAddressArea);
        destAddressHTxt = (MTextView) findViewById(R.id.destAddressHTxt);
        sourceAddressHTxt = (MTextView) findViewById(R.id.sourceAddressHTxt);
        thanksNoteTxt = (MTextView) findViewById(R.id.thanksNoteTxt);
        orderTxt = (MTextView) findViewById(R.id.orderTxt);
        chargetxt = (MTextView) findViewById(R.id.chargetxt);
        desdashImage = (DividerView) findViewById(R.id.dashImage);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        addToClickHandler(backImgView);
        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);

        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        commentBox = (MaterialEditText) findViewById(R.id.commentBox);
        commentBox.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        commentBox.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        commentBox.setOnTouchListener((v, event) -> {
            if (commentBox.hasFocus()) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_SCROLL:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                }
            }
            return false;
        });

        generalCommentTxt = (MTextView) findViewById(R.id.generalCommentTxt);
        generalCommentArea = (CardView) findViewById(R.id.generalCommentArea);
        container = (LinearLayout) findViewById(R.id.container);
        rideRatingArea = (LinearLayout) findViewById(R.id.rideRatingArea);
        ratingBar = (SimpleRatingBar) findViewById(R.id.ratingBar);
        safetyRatingBar = (SimpleRatingBar) findViewById(R.id.safetyRatingBar);

        farecontainer = (LinearLayout) findViewById(R.id.farecontainer);

        walletNoteTxt = (MTextView) findViewById(R.id.walletNoteTxt);


        uberXRatingLayoutArea = (LinearLayout) findViewById(R.id.uberXRatingLayoutArea);
        mainRatingArea = (LinearLayout) findViewById(R.id.mainRatingArea);

        totalFareTxt = (MTextView) findViewById(R.id.totalFareTxt);
        dateVTxt = (MTextView) findViewById(R.id.dateVTxt);
        ratingHeaderTxt = (MTextView) findViewById(R.id.ratingHeaderTxt);

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);
        addToClickHandler(btn_type2);
        if (isUfx || isBid) {
            backImgView.setVisibility(View.VISIBLE);
            desdashImage.setVisibility(View.GONE);
        } else {
            backImgView.setVisibility(View.GONE);
            desdashImage.setVisibility(View.VISIBLE);
        }

        paymentWebview = (WebView) findViewById(R.id.paymentWebview);
        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);

        rlSubmitView = findViewById(R.id.rlSubmitView);
        txtSkip = findViewById(R.id.txtSkip);
        txtSubmit = findViewById(R.id.txtSubmit);
        txtSkip.setText(generalFunc.retrieveLangLBl("", "LBL_SKIP_TXT"));
        txtSubmit.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));
        addToClickHandler(txtSkip);
        addToClickHandler(txtSubmit);


        setLabels();

        getFare();


        commentBox.setFloatingLabel(MaterialEditText.FLOATING_LABEL_NONE);

        commentBox.setHideUnderline(true);

        if (generalFunc.isRTLmode()) {
            commentBox.setPaddings(0, 0, (int) getResources().getDimension(R.dimen._5sdp), 0);
        } else {
            commentBox.setPaddings((int) getResources().getDimension(R.dimen._5sdp), 0, 0, 0);
        }


        commentBox.setSingleLine(false);
        commentBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        commentBox.setGravity(Gravity.TOP);
    }


    /*fav driver feature End*/
    public class myWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            try {

                loaderView.setVisibility(View.VISIBLE);
                view.setOnTouchListener(null);

                if (url.contains("success=1")) {

                    paymentWebview.setVisibility(View.GONE);
                    loaderView.setVisibility(View.GONE);

                    submitRating("No");


                }

                if (url.contains("success=0")) {
                    paymentWebview.setVisibility(View.GONE);

                    String message = null;
                    if (Utils.checkText(url) && url.contains("&message=")) {
                        String msg = generalFunc.substringAfterLast(url, "&message=");
                        message = Utils.checkText(msg) ? msg.replaceAll("%20", " ") : message;
                    } else {
                        message = generalFunc.retrieveLangLBl("", "LBL_REQUEST_FAILED_PROCESS");

                    }

                    generalFunc.showGeneralMessage("", message, "", generalFunc.retrieveLangLBl("", "LBL_OK"), i -> {
                    });

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {

            generalFunc.showError();
            loaderView.setVisibility(View.GONE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loaderView.setVisibility(View.GONE);

            view.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            });

        }
    }

    public Context getActContext() {
        return RatingActivity.this;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RATING"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));
        commentBox.setHint(generalFunc.retrieveLangLBl("", "LBL_USER_FEEDBACK_NOTE"));
        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MYTRIP_Trip_Date"));
        ratingHeaderTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOW_WAS_RIDE"));

        totalFareTxt.setText(generalFunc.retrieveLangLBl("Total Fare", "LBL_Total_Fare"));
        chargetxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHARGES_TXT"));
    }

    public void getFare() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        if (isBid) {

            parameters.put("type", "displayFareBiddingService");
            parameters.put("memberId", generalFunc.getMemberId());
            parameters.put("UserType", Utils.app_type);
            parameters.put("memberType", Utils.app_type);
            if (getIntent().hasExtra("iBiddingPostId")) {
                parameters.put("iBiddingPostId", getIntent().getExtras().getString("iBiddingPostId"));
            }

        } else {
            parameters.put("type", "displayFare");
            parameters.put("iMemberId", generalFunc.getMemberId());
            parameters.put("UserType", Utils.app_type);
            if (isUfx) {
                parameters.put("iTripId", getIntent().getStringExtra("iTripId"));
            }

        }


        ApiHandler.execute(getActContext(), parameters, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                closeLoader();
                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {

                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    String FormattedTripDate = generalFunc.getJsonValue("tTripRequestDateOrig", message);
                    String TotalFare = generalFunc.getJsonValue("TotalFare", message);
                    String FareSubTotal = generalFunc.getJsonValue("FareSubTotal", message);
                    String fDiscount = generalFunc.getJsonValue("fDiscount", message);
                    String vDriverImage = generalFunc.getJsonValue("vDriverImage", message);
                    String CurrencySymbol = generalFunc.getJsonValue("CurrencySymbol", message);
                    String iVehicleTypeId = generalFunc.getJsonValue("iVehicleTypeId", message);
                    String iDriverId = generalFunc.getJsonValue("iDriverId", message);
                    String tEndLat = generalFunc.getJsonValue("tEndLat", message);
                    String tEndLong = generalFunc.getJsonValue("tEndLong", message);
                    String eCancelled = generalFunc.getJsonValue("eCancelled", message);
                    String vCancelReason = generalFunc.getJsonValue("vCancelReason", message);
                    String vCancelComment = generalFunc.getJsonValue("vCancelComment", message);
                    String vCouponCode = generalFunc.getJsonValue("vCouponCode", message);
                    String carImageLogo = generalFunc.getJsonValue("carImageLogo", message);
                    String iTripId = generalFunc.getJsonValue("iTripId", message);
                    String eType = generalFunc.getJsonValue("eType", message);
                    String eFavDriver = generalFunc.getJsonValue("eFavDriver", message);
                    String eFareType = generalFunc.getJsonValue("eFareType", message);
                    String eFly = generalFunc.getJsonValue("eFly", responseString);
                    ShowSafetyRating = generalFunc.getJsonValue("ShowSafetyRating", message);

                    if (eFareType.equalsIgnoreCase(Utils.CabFaretypeRegular)) {
                        desdashImage.setVisibility(View.VISIBLE);
                    }


                    if (isFavDriverEnabled(eType) && !eFly.equalsIgnoreCase("Yes")) {
                        /*fav driver feature Start*/
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        width = displayMetrics.widthPixels;
                        width_D = (int) (width * 0.369);
                        width_ = (int) (width * 0.397);
                        favSwitch = (SwitchButton) findViewById(R.id.favSwitch);

                        favArea = (LinearLayout) findViewById(R.id.favArea);
                        favArea.setVisibility(View.VISIBLE);

                        favDriverTitleTxt = (MTextView) findViewById(R.id.favDriverTitleTxt);
                        if (eType.equals("Deliver") || eType.equals(Utils.eType_Multi_Delivery)) {
                            favDriverTitleTxt.setText(generalFunc.retrieveLangLBl("Save as Favourite Carrier?", "LBL_SAVE_FAVOUTITE_CARRIER"));
                        } else {
                            favDriverTitleTxt.setText(generalFunc.retrieveLangLBl("save as Favorite Driver", "LBL_SAVE_AS_FAV_DRIVER"));
                        }


                        favSwitch.setOnCheckedChangeListener((compoundButton, b) -> {

                            if (b == true) {
                                favSwitch.setThumbColorRes(R.color.Green);
                            } else {
                                favSwitch.setThumbColorRes(android.R.color.holo_red_dark);
                            }


                        });

                        favSwitch.setChecked(eFavDriver.equalsIgnoreCase("Yes"));


                    }

                    if (ShowSafetyRating.equalsIgnoreCase("Yes")) {
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        width = displayMetrics.widthPixels;
                        width_D = (int) (width * 0.349);
                        width_ = (int) (width * 0.397);
                        safetyRatingBar = findViewById(R.id.safetyRatingBar);
                        tripRatingTitleTxt = findViewById(R.id.tripRatingTitleTxt);
                        safetyRatingTitleTxt = findViewById(R.id.safetyRatingTitleTxt);
                        safetyRatingTitleTxt.setText(generalFunc.retrieveLangLBl("Safety Rating", "LBL_SAFETY_RATING_TXT"));
                        tripRatingTitleTxt.setText(generalFunc.retrieveLangLBl("Trip Rating", "LBL_TRIP_RATING"));
                        safetyRatingTitleTxt.setVisibility(View.VISIBLE);
                        tripRatingTitleTxt.setVisibility(View.VISIBLE);

                        hScrollView = (CustomHorizontalScrollView) findViewById(R.id.hScrollView);
                        lineArea = (LinearLayout) findViewById(R.id.lineArea);
                        safetyRatingArea = (LinearLayout) findViewById(R.id.ratingArea);
                        lineArea.setOnTouchListener(new SetOnTouchList());

                        hScrollView.setOnTouchListener(new SetOnTouchList());
                        addToClickHandler(hScrollView);

                        ratingBar.setOnRatingBarChangeListener(new SimpleRatingBar.OnRatingBarChangeListener() {
                            @Override
                            public void onRatingChanged(SimpleRatingBar simpleRatingBar, float rating, boolean fromUser) {

                                if (safetyRatingArea.getVisibility() == View.GONE) {
                                    showSafetyRatingArea(true);

                                }
                            }
                        });
                        hScrollView.setOnTouchListener(new OnSwipeTouchListener(getActContext()) {
                            public void onSwipeTop() {

                            }

                            public void onSwipeRight() {
                                scrollWithanimation();
                            }

                            public void onSwipeLeft() {
                                scrollWithanimation();
                            }

                            public void onSwipeBottom() {

                            }

                        });
                    }


                    String noVal = "";
                    if (eType.equals(Utils.CabGeneralType_UberX)) {
                        noVal = generalFunc.retrieveLangLBl("", "LBL_SERVICES") + " #";
                    } else if (eType.equals("Deliver") || eType.equals(Utils.eType_Multi_Delivery)) {
                        noVal = generalFunc.retrieveLangLBl("", "LBL_DELIVERY") + " #";
                    } else {
                        noVal = generalFunc.retrieveLangLBl("", "LBL_RIDE") + " #";
                    }


                    thanksNoteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_THANKS_TXT"));
                    orderTxt.setText(noVal + "" + generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("vRideNo", message)));


                    iTripId_str = iTripId;


                    if (generalFunc.getJsonValue("eWalletAmtAdjusted", message).equalsIgnoreCase("Yes")) {
                        walletNoteTxt.setVisibility(View.VISIBLE);
                        walletNoteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WALLET_AMT_ADJUSTED") + " " + generalFunc.getJsonValue("fWalletAmountAdjusted", message));
                    }

                    if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_JOB_REQ_DATE"));
                    } else if (eType.equalsIgnoreCase("Deliver")) {
                        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DELIVERY_DATE_TXT"));
                    } else {
                        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TRIP_DATE_TXT"));
                    }


                    JSONArray FareDetailsArrNewObj = null;

                    FareDetailsArrNewObj = generalFunc.getJsonArray("FareDetailsNewArr", message);

                    addFareDetailLayout(FareDetailsArrNewObj);

                    ((MTextView) findViewById(R.id.dateTxt)).setText(/*": "+*/generalFunc.getDateFormatedType(FormattedTripDate, Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext())));
                    ((MTextView) findViewById(R.id.sourceAddressTxt)).setText(generalFunc.getJsonValue("tSaddress", message));
                    if (generalFunc.getJsonValue("tDaddress", message).equals("")) {
                        ((LinearLayout) findViewById(R.id.destarea)).setVisibility(View.GONE);
                        ((ImageView) findViewById(R.id.imagedest)).setVisibility(View.GONE);


                    } else {
                        ((LinearLayout) findViewById(R.id.destarea)).setVisibility(View.VISIBLE);
                        ((ImageView) findViewById(R.id.imagedest)).setVisibility(View.VISIBLE);
                        ((MTextView) findViewById(R.id.destAddressTxt)).setText(generalFunc.getJsonValue("tDaddress", message));
                    }

                    if (Utils.checkText(FareSubTotal)) {
                        ((MTextView) findViewById(R.id.fareTxt)).setText(generalFunc.convertNumberWithRTL(FareSubTotal));
                    } else {
                        ((MTextView) findViewById(R.id.fareTxt)).setText(CurrencySymbol + " " + generalFunc.convertNumberWithRTL(TotalFare));
                    }
                    LinearLayout towTruckdestAddrArea = (LinearLayout) findViewById(R.id.towTruckdestAddrArea);

                    if (eType.equalsIgnoreCase("UberX")) {
                        uberXRatingLayoutArea.setVisibility(View.GONE);
                        mainRatingArea.setVisibility(View.VISIBLE);
                        new CreateRoundedView(Color.parseColor("#54A626"), Utils.dipToPixels(getActContext(), 9), 0, 0, findViewById(R.id.ufxPickArea));
                        ((MTextView) findViewById(R.id.sourceAddressTxt)).setText(generalFunc.getJsonValue("tSaddress", message));

                        if (generalFunc.getJsonValue("APP_DESTINATION_MODE", message).equalsIgnoreCase("Strict") || generalFunc.getJsonValue("APP_DESTINATION_MODE", message).equalsIgnoreCase("NonStrict")) {

                            if (towTruckdestAddrArea.getVisibility() == View.GONE) {
                                towTruckdestAddrArea.setVisibility(View.VISIBLE);
                                ((MTextView) findViewById(R.id.destAddressTxt)).setText(generalFunc.getJsonValue("tDaddress", message));
                            }
                        }
                        setImages("", "", generalFunc.getJsonValue("vLogoVehicleCategoryPath", message), generalFunc.getJsonValue("vLogoVehicleCategory", message));

                    } else {
                        mainRatingArea.setVisibility(View.VISIBLE);
                        uberXRatingLayoutArea.setVisibility(View.GONE);
                        setImages(carImageLogo, iVehicleTypeId, "", "");
                    }

                    ((MTextView) findViewById(R.id.carType)).setText(generalFunc.getJsonValue("vServiceDetailTitle", message));


                    if (eType.equals("Deliver") || eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                        ratingHeaderTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOW_WAS_DELIVERY"));
                        sourceAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SENDER_LOCATION"));
                        destAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RECEIVER_LOCATION"));

                    } else if (eType.equals(Utils.CabGeneralType_UberX)) {
                        ratingHeaderTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOW_WAS_YOUR_BOOKING"));
                        sourceAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT"));

                    } else {
                        ratingHeaderTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOW_WAS_RIDE"));
                        sourceAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PICKUP_LOCATION_TXT"));
                        destAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));
                    }
                    String generalcommVal = "";
                    if (eCancelled.equals("Yes")) {
                        if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                            generalcommVal = generalFunc.retrieveLangLBl("", "LBL_PREFIX_JOB_CANCEL_PROVIDER");
                        } else if (eType.equalsIgnoreCase("Deliver")) {
                            generalcommVal = generalFunc.retrieveLangLBl("", "LBL_PREFIX_DELIVERY_CANCEL_DRIVER");
                        } else {
                            generalcommVal = generalFunc.retrieveLangLBl("", "LBL_PREFIX_TRIP_CANCEL_DRIVER");
                        }
                        generalCommentTxt.setText(generalcommVal
                                + " " + vCancelReason);
                        generalCommentTxt.setVisibility(View.VISIBLE);
                        generalCommentArea.setVisibility(View.VISIBLE);
                    } else {
                        generalCommentTxt.setVisibility(View.GONE);
                        generalCommentArea.setVisibility(View.GONE);
                    }

                    if (generalFunc.getJsonValue("ENABLE_TIP_MODULE", message).equalsIgnoreCase("Yes")) {
                        isCollectTip = true;


                    } else {
                        isCollectTip = false;
                    }


                    String isVideoCall = generalFunc.getJsonValue("isVideoCall", message);

                    container.setVisibility(View.VISIBLE);


                    if (isVideoCall != null && isVideoCall.equalsIgnoreCase("Yes")) {
                        mainAddressArea.setVisibility(View.GONE);


                    }

                    if (isBid) {
                        noVal = generalFunc.retrieveLangLBl("", "LBL_TASK_TXT") + " #";
                        ((MTextView) findViewById(R.id.dateTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(generalFunc.getJsonValue("dBiddingDate", message), Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext()))));

                        String pickupHval = "";
                        pickupHval = generalFunc.retrieveLangLBl("", "LBL_SERVICE_LOCATION");


                        ((MTextView) findViewById(R.id.sourceAddressHTxt)).setText(pickupHval);

                        orderTxt.setText(noVal + "" + generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("vBiddingPostNo", message)));
                        ratingHeaderTxt.setText(generalFunc.retrieveLangLBl("How's your Task? Rate provider", "LBL_BIDDING_RATE_HEADING_DRIVER_TXT"));
                    }

                    if (generalFunc.getJsonValueStr("ENABLE_SKIP_RATING_RIDE", obj_userProfile).equalsIgnoreCase("Yes")) {
                        isSkipRatingEnable = true;
                        txtSkip.setVisibility(View.VISIBLE);
                        txtSubmit.setVisibility(View.VISIBLE);
                        rlSubmitView.setVisibility(View.GONE);


                    } else {
                        txtSkip.setVisibility(View.GONE);
                        txtSubmit.setVisibility(View.GONE);
                        rlSubmitView.setVisibility(View.VISIBLE);
                    }
                } else {
                    generateErrorView();
                }
            } else {
                generateErrorView();
            }
        });

    }

    private void scrollWithanimation() {
        if (!ratingBar.isFocusable()) {
            lineArea.performClick();
        } else {
            showSafetyRatingArea(true);
        }
    }

    private void showSafetyRatingArea(boolean show) {
        if (show) {

            if (generalFunc.isRTLmode()) {
                SlideAnimationUtil.slideInFromLeft(getActContext(), safetyRatingArea);
            } else {
                SlideAnimationUtil.slideOutToRight(getActContext(), safetyRatingArea);
            }

            if (!isAnimated) {
                hScrollView.setScrollingEnabled(true);

                ratingBar.animate().translationXBy(generalFunc.isRTLmode() ? width_ : -width_);
                rideRatingArea.animate().translationXBy(generalFunc.isRTLmode() ? width_ : -width_);

                ratingBar.setIndicator(true);
                ratingBar.setFocusable(false);

                safetyRatingArea.setVisibility(View.VISIBLE);
                safetyRatingArea.animate().translationXBy(generalFunc.isRTLmode() ? width_D : -width_D);

                isAnimated = true;

                hScrollView.setScrollingEnabled(false);
            }

        } else {
//

            if (generalFunc.isRTLmode()) {
                SlideAnimationUtil.slideInFromRight(getActContext(), ratingBar);
                SlideAnimationUtil.slideInFromRight(getActContext(), rideRatingArea);
            } else {
                SlideAnimationUtil.slideOutToLeft(getActContext(), ratingBar);
                SlideAnimationUtil.slideOutToLeft(getActContext(), rideRatingArea);
            }

            if (isAnimated) {
                hScrollView.setScrollingEnabled(true);

                ratingBar.animate().translationXBy(generalFunc.isRTLmode() ? -width_ : width_);
                rideRatingArea.animate().translationXBy(generalFunc.isRTLmode() ? -width_ : width_);

                ratingBar.setIndicator(false);
                ratingBar.setFocusable(true);

                safetyRatingArea.animate().translationXBy(generalFunc.isRTLmode() ? -width_D : width_D);
                safetyRatingArea.setVisibility(View.GONE);

                isAnimated = false;

                hScrollView.setScrollingEnabled(false);
            }


        }
        if (show) {
            addToClickHandler(lineArea);
        } else {
            lineArea.setOnClickListener(null);
        }

    }


    private void addFareDetailLayout(JSONArray jobjArray) {


        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                String data = jobject.names().getString(0);

                addFareDetailRow(data, jobject.get(data).toString(), (jobjArray.length() - 1) == i ? true : false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void setImages(String carImageLogo, String iVehicleTypeId, String vLogoVehicleCategoryPath, String vLogoVehicleCategory) {
        String imageName = "";
        String size = "";
        switch (getResources().getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                imageName = "mdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "80";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                imageName = "mdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "80";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                imageName = "hdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "120";
                break;
            case DisplayMetrics.DENSITY_TV:
                imageName = "hdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "120";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                imageName = "xhdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "160";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                imageName = "xxhdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "240";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                imageName = "xxxhdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "320";
                break;
        }

    }

    private void submitRating(String isRatingSkip) {
        HashMap<String, String> parameters = new HashMap<>();

        if (isBid) {
            parameters.put("type", "submitRatingBiddingService");
            parameters.put("iGeneralUserId", generalFunc.getMemberId());
            parameters.put("iBiddingPostId", getIntent().getStringExtra("iBiddingPostId"));

        } else {
            parameters.put("type", "submitRating");
            parameters.put("tripID", iTripId_str);
        }
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("rating", "" + ratingBar.getRating());
        parameters.put("message", Utils.getText(commentBox));
        parameters.put("UserType", Utils.app_type);
        if (favSwitch != null) {
            parameters.put("eFavDriver", favSwitch.isChecked() ? "Yes" : "No");
        }
        if (ShowSafetyRating.equalsIgnoreCase("Yes")) {
            parameters.put("safetyRating", "" + safetyRatingBar.getRating());
        }


        parameters.put("fAmount", tipamount);
        if (isCollectTip) {
            parameters.put("isCollectTip", "Yes");
        } else {
            parameters.put("isCollectTip", "No");

        }

        if (isSkipRatingEnable) {
            parameters.put("isSkipRating", isRatingSkip);
        }


        ServerTask exeWebServer = ApiHandler.execute(getActContext(), parameters, true, false, generalFunc, responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    btn_type2.setEnabled(true);

                    showBookingAlert(responseString);
                } else {
                    btn_type2.setEnabled(true);
                    if (generalFunc.getJsonValue(Utils.message_str, responseString).equalsIgnoreCase("LBL_REQUIRED_MINIMUM_AMOUT")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)) + " " + generalFunc.getJsonValue("minValue", responseString));

                    } else {


                        if (generalFunc.getJsonValue("isCollectTip", responseString) != null &&
                                generalFunc.getJsonValue("isCollectTip", responseString).equalsIgnoreCase("Yes")) {
                            Bundle bn = new Bundle();
                            bn.putString("url", generalFunc.getJsonValue(Utils.message_str, responseString));
                            bn.putBoolean("handleResponse", true);
                            new ActUtils(getActContext()).startActForResult(PaymentWebviewActivity.class, bn, WEBVIEWPAYMENT);
                        } else {
                            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                        }


                    }
                }
            } else {
                btn_type2.setEnabled(true);
                generalFunc.showError();
            }
        });
        exeWebServer.setCancelAble(false);
    }


    public void showBookingAlert(String responseString) {


        String titleTxt = "";
        String mesasgeTxt = "";
        String eType = generalFunc.getJsonValue("eType", responseString);
        if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            titleTxt = generalFunc.retrieveLangLBl("Booking Successful", "LBL_JOB_FINISHED");
            mesasgeTxt = generalFunc.retrieveLangLBl("", "LBL_JOB_FINISHED_USER_TXT");
        } else if (eType.equalsIgnoreCase("Deliver")) {
            titleTxt = generalFunc.retrieveLangLBl("Booking Successful", "LBL_DELIVERY_SUCCESS_FINISHED");
            mesasgeTxt = generalFunc.retrieveLangLBl("", "LBL_DELIVERY_FINISHED_USER_TXT");
        } else if (isBid) {
            titleTxt = generalFunc.retrieveLangLBl("", "LBL_TASK_COMPLETED_TXT");
            mesasgeTxt = generalFunc.retrieveLangLBl("", "LBL_TASK_FINISHED_TXT");
        } else {
            titleTxt = generalFunc.retrieveLangLBl("Booking Successful", "LBL_SUCCESS_FINISHED");
            mesasgeTxt = generalFunc.retrieveLangLBl("", "LBL_TRIP_FINISHED_USER_TXT");
        }

        if (generalFunc.getJsonValue("isMedicalServiceTrip", responseString).equalsIgnoreCase("Yes")) {
            titleTxt = generalFunc.retrieveLangLBl("", "LBL_SUCCESS_FINISHED");
            mesasgeTxt = generalFunc.retrieveLangLBl("", "LBL_MEDICAL_TRIP_FINISHED_TXT");
        }


        SuccessDialog.showSuccessDialog(getActContext(), titleTxt, mesasgeTxt, generalFunc.retrieveLangLBl("Ok", "LBL_OK_THANKS"), false, () -> {

            if (isFirst) {
                MyApp.getInstance().refreshView(this, responseString);
                return;
            }
            MyApp.getInstance().refreshView(this, responseString);


        });
    }

    public void buildTipCollectMessage(String message, String positiveBtn, String negativeBtn) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.desgin_add_tip_layout, null);
        builder.setView(dialogView);

        final MaterialEditText tipAmountEditBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        tipAmountEditBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        tipAmountEditBox.setVisibility(View.GONE);
        final MTextView giveTipTxtArea = (MTextView) dialogView.findViewById(R.id.giveTipTxtArea);
        final MTextView msgTxt = (MTextView) dialogView.findViewById(R.id.msgTxt);
        msgTxt.setVisibility(View.VISIBLE);
        final MTextView skipTxtArea = (MTextView) dialogView.findViewById(R.id.skipTxtArea);
        final MTextView titileTxt = (MTextView) dialogView.findViewById(R.id.titileTxt);
        titileTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TIP_TITLE_TXT"));
        msgTxt.setText(message);
        giveTipTxtArea.setText(negativeBtn);
        skipTxtArea.setText(positiveBtn);

        skipTxtArea.setOnClickListener(view -> {
            giveTipAlertDialog.dismiss();
            tipamount = 0 + "";

            btn_type2.setEnabled(false);
            submitRating("No");
            isCollectTip = false;
        });

        giveTipTxtArea.setOnClickListener(view -> {
            giveTipAlertDialog.dismiss();
            showTipBox();

        });
        giveTipAlertDialog = builder.create();
        giveTipAlertDialog.setCancelable(true);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(giveTipAlertDialog);
        }
        giveTipAlertDialog.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card));
        giveTipAlertDialog.show();
    }

    public void showTipBox() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.desgin_add_tip_layout, null);
        builder.setView(dialogView);

        final MaterialEditText tipAmountEditBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        tipAmountEditBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final MTextView giveTipTxtArea = (MTextView) dialogView.findViewById(R.id.giveTipTxtArea);
        final MTextView skipTxtArea = (MTextView) dialogView.findViewById(R.id.skipTxtArea);
        final MTextView titileTxt = (MTextView) dialogView.findViewById(R.id.titileTxt);
        titileTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TIP_AMOUNT_ENTER_TITLE"));
        giveTipTxtArea.setText("" + generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        skipTxtArea.setText("" + generalFunc.retrieveLangLBl("", "LBL_SKIP_TXT"));

        skipTxtArea.setOnClickListener(view -> {
            Utils.hideKeyboard(getActContext());
            giveTipAlertDialog.dismiss();
            btn_type2.setEnabled(false);
            submitRating("No");

        });

        giveTipTxtArea.setOnClickListener(view -> {

            final boolean tipAmountEntered = Utils.checkText(tipAmountEditBox) ? true : Utils.setErrorFields(tipAmountEditBox, generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD"));
            if (tipAmountEntered == false) {
                return;
            }
            Utils.hideKeyboard(getActContext());
            giveTipAlertDialog.dismiss();
            collectTip(Utils.getText(tipAmountEditBox));
            btn_type2.setEnabled(false);

            submitRating("No");
        });
        giveTipAlertDialog = builder.create();
        giveTipAlertDialog.setCancelable(true);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(giveTipAlertDialog);
        }
        giveTipAlertDialog.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card));
        giveTipAlertDialog.show();

    }

    private void collectTip(String tipAmount) {


        tipamount = tipAmount;


    }


    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getFare());
    }

    @Override
    public void onBackPressed() {


        if (paymentWebview.getVisibility() == View.VISIBLE) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_CANCEL_PAYMENT_PROCESS"), generalFunc.retrieveLangLBl("", "LBL_NO"), generalFunc.retrieveLangLBl("", "LBL_YES"), buttonId -> {
                if (buttonId == 1) {
                    paymentWebview.setVisibility(View.GONE);
                    paymentWebview.stopLoading();
                    loaderView.setVisibility(View.GONE);
                    btn_type2.setEnabled(true);
                }
            });

            return;
        }


        if (isFirst) {
            MyApp.getInstance().restartWithGetDataApp();
            return;
        }
        if (isUfx || isBid) {
            MyApp.getInstance().restartWithGetDataApp();
        } else {
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void addFareDetailRow(String row_name, String row_value, boolean isLast) {
        View convertView = null;
        if (row_name.equalsIgnoreCase("eDisplaySeperator")) {
            convertView = new View(getActContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dipToPixels(getActContext(), 1));
            params.setMarginStart(Utils.dipToPixels(getActContext(), 10));
            params.setMarginEnd(Utils.dipToPixels(getActContext(), 10));
            convertView.setBackgroundColor(Color.parseColor("#dedede"));
            convertView.setLayoutParams(params);
        } else {
            LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.design_fare_breakdown_row, null);

            convertView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


            MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
            MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

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

        if (convertView != null)
            farecontainer.addView(convertView);
    }


    public void onClick(View view) {
        int i = view.getId();
        Utils.hideKeyboard(getActContext());
        if (i == submitBtnId) {
            if (ratingBar.getRating() < 0.5) {
                generalFunc.showMessage(generalFunc.getCurrentView(RatingActivity.this), generalFunc.retrieveLangLBl("", "LBL_ERROR_RATING_DIALOG_TXT"));
                return;
            }

            if (safetyRatingArea != null && safetyRatingArea.getVisibility() == View.VISIBLE && safetyRatingBar.getRating() < 0.5) {
                generalFunc.showMessage(generalFunc.getCurrentView(RatingActivity.this), generalFunc.retrieveLangLBl("", "LBL_ERROR_SAFETY_RATING_DIALOG_TXT"));
                return;
            }

            if (isCollectTip) {
                buildTipCollectMessage(generalFunc.retrieveLangLBl("", "LBL_TIP_TXT"), generalFunc.retrieveLangLBl("No,Thanks", "LBL_NO_THANKS"),
                        generalFunc.retrieveLangLBl("Give Tip", "LBL_GIVE_TIP_TXT"));
                return;
            } else {
                btn_type2.setEnabled(false);
                submitRating("No");
            }

        } else if (i == backImgView.getId()) {
            onBackPressed();
        } else if (i == txtSubmit.getId()) {
            btn_type2.performClick();
        } else if (i == txtSkip.getId()) {
            submitRating("Yes");
        } else if (i == R.id.lineArea) {
            showSafetyRatingArea(false);
        } else if (i == R.id.hScrollView) {
            if (!ratingBar.isFocusable()) {
                lineArea.performClick();
            }
        }
    }


    public boolean isFavDriverEnabled(String eType) {
        String ENABLE_FAVORITE_DRIVER_MODULE = generalFunc.retrieveValue(Utils.ENABLE_FAVORITE_DRIVER_MODULE_KEY);
        return Utils.checkText(ENABLE_FAVORITE_DRIVER_MODULE) && ENABLE_FAVORITE_DRIVER_MODULE.equalsIgnoreCase("Yes") && (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride) || eType.equalsIgnoreCase(Utils.CabGeneralType_UberX) || eType.equalsIgnoreCase(Utils.CabGeneralType_Deliver) || eType.equalsIgnoreCase("Deliver") || eType.equalsIgnoreCase(Utils.eType_Multi_Delivery));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        {
            if (resultCode == RESULT_OK && requestCode == WEBVIEWPAYMENT) {
                paymentWebview.setVisibility(View.GONE);
                loaderView.setVisibility(View.GONE);
                isCollectTip = false;

                submitRating("No");

            }

        }
    }
}
