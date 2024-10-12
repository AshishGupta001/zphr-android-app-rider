package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.general.call.CommunicationManager;
import com.zphr.user.AdditionalChargeActivity;
import com.zphr.user.BookingActivity;
import com.zphr.user.ChatActivity;
import com.zphr.user.ConfirmEmergencyTapActivity;
import com.zphr.user.HistoryDetailActivity;
import com.zphr.user.MainActivity;
import com.zphr.user.PaymentWebviewActivity;
import com.zphr.user.RatingActivity;
import com.zphr.user.UberXHomeActivity;
import com.utils.Logger;
import com.utils.Utils;
import com.view.GenerateAlertBox;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Admin on 20/03/18.
 */

public class FireTripStatusMsg {

    private final String TAGS = FireTripStatusMsg.class.getSimpleName();
    private Context mContext;
    private static String tmp_msg_chk = "", tmp_store_key = "";

    public FireTripStatusMsg() {
    }

    public FireTripStatusMsg(Context mContext) {
        this.mContext = mContext;
    }

    public void fireTripMsg(String message) {

        Logger.d(TAGS, "MsgReceived :: called");
        if (!Utils.checkText(message) || tmp_msg_chk.equals(message)) {
            Logger.d(TAGS, "MsgReceived :: return");
            return;
        }
        tmp_msg_chk = message;

        Logger.e(TAGS, "MsgReceived::" + message);
        String finalMsg = message;

        if (!GeneralFunctions.isJsonObj(finalMsg)) {
            try {
                finalMsg = new JSONTokener(message).nextValue().toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!GeneralFunctions.isJsonObj(finalMsg)) {
                finalMsg = finalMsg.replaceAll("^\"|\"$", "");

                if (!GeneralFunctions.isJsonObj(finalMsg)) {
                    finalMsg = message.replaceAll("\\\\", "");
                    finalMsg = finalMsg.replaceAll("^\"|\"$", "");
                    if (!GeneralFunctions.isJsonObj(finalMsg)) {
                        finalMsg = message.replace("\\\"", "\"").replaceAll("^\"|\"$", "");
                    }
                    finalMsg = finalMsg.replace("\\\\\"", "\\\"");
                }
            }
        }

        if (MyApp.getInstance() == null) {
            if (mContext != null) {
                dispatchNotification(finalMsg);
            }
            return;
        }

        if (MyApp.getInstance().getCurrentAct() != null) {
            mContext = MyApp.getInstance().getCurrentAct();
        }

        if (mContext == null) {
            dispatchNotification(finalMsg);
            return;
        }

        GeneralFunctions generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        JSONObject obj_msg = generalFunc.getJsonObject(finalMsg);
        String tSessionId = generalFunc.getJsonValueStr("tSessionId", obj_msg);

        if (!tSessionId.equals("") && !tSessionId.equals(generalFunc.retrieveValue(Utils.SESSION_ID_KEY))) {
            return;
        }

        if (!GeneralFunctions.isJsonObj(finalMsg)) {
            String passMessage = generalFunc.convertNumberWithRTL(message);
            MyApp.getInstance().getLoclaNotificationObj().dispatchLocalNotification(mContext, passMessage, true);
            generalFunc.showGeneralMessage("", passMessage);
            return;
        }

        boolean isMsgExist = isTripStatusMsgExist(generalFunc, finalMsg, mContext);
        Logger.d(TAGS, "MsgReceived:: MsgExist-> " + isMsgExist);

        if (isMsgExist) {
            return;
        }

        if (msgHandling(generalFunc, obj_msg)) {
            return;
        }

         if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(() -> continueDispatchMsg(generalFunc, obj_msg));
        } else {
            dispatchNotification(finalMsg);
        }
    }

    private void continueDispatchMsg(GeneralFunctions generalFunc, JSONObject obj_msg) {
        boolean isTripEndDialogShown = false;
        String messageStr = generalFunc.getJsonValueStr("Message", obj_msg);

        String vTitle = generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vTitle", obj_msg));
        String eType = generalFunc.getJsonValueStr("eType", obj_msg);

        if (messageStr.equals("")) {

            String msgTypeStr = generalFunc.getJsonValueStr("MsgType", obj_msg);
            //   String messageType_str = generalFunc.getJsonValueStr("MessageType", obj_msg);

            if (msgTypeStr.equalsIgnoreCase("CHAT")) {


                if (!(MyApp.getInstance().getCurrentAct() instanceof ChatActivity)) {
                    MyApp.getInstance().getLoclaNotificationObj().dispatchLocalNotification(mContext, generalFunc.getJsonValueStr("Msg", obj_msg), false);
                    /*Bundle bn = new Bundle();

                    bn.putString("iFromMemberId", generalFunc.getJsonValueStr("iFromMemberId", obj_msg));
                    bn.putString("FromMemberImageName", generalFunc.getJsonValueStr("FromMemberImageName", obj_msg));
                    bn.putString("iTripId", generalFunc.getJsonValueStr("iTripId", obj_msg));
                    bn.putString("FromMemberName", generalFunc.getJsonValueStr("FromMemberName", obj_msg));
                    bn.putString("vBookingNo", generalFunc.getJsonValueStr("vBookingNo", obj_msg));*/

                    Intent chatActInt = new Intent(MyApp.getInstance().getApplicationContext(), ChatActivity.class);
                    if (obj_msg != null) {
                        chatActInt.putExtras(generalFunc.createChatBundle(obj_msg));
                        chatActInt.putExtra("iBiddingPostId", generalFunc.getJsonValueStr("iBiddingPostId", obj_msg));
                        chatActInt.putExtra("iDriverId", generalFunc.getJsonValueStr("iDriverId", obj_msg));
                    }
                    chatActInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    MyApp.getInstance().getApplicationContext().startActivity(chatActInt);
                } else if (MyApp.getInstance() != null && MyApp.getInstance().getCurrentAct() instanceof ChatActivity) {

                       /* Bundle bn = new Bundle();

                        bn.putString("iFromMemberId", generalFunc.getJsonValueStr("iFromMemberId", obj_msg));
                        bn.putString("FromMemberImageName", generalFunc.getJsonValueStr("FromMemberImageName", obj_msg));
                        bn.putString("iTripId", generalFunc.getJsonValueStr("iTripId", obj_msg));
                        bn.putString("FromMemberName", generalFunc.getJsonValueStr("FromMemberName", obj_msg));
                        bn.putString("vBookingNo", generalFunc.getJsonValueStr("vBookingNo", obj_msg));*/

                    //  ((ChatActivity) MyApp.getInstance().getCurrentAct()).setCurrentTripData(generalFunc.createChatBundle(obj_msg));
                    ChatActivity chatActivity = (ChatActivity) MyApp.getInstance().getCurrentAct();
                    chatActivity.playNotificationSound();
                    return;
                }
            } else if (msgTypeStr.equalsIgnoreCase("VOIP")) {

            } else if (!msgTypeStr.equalsIgnoreCase("LocationUpdate") && !msgTypeStr.equalsIgnoreCase("LocationUpdateOnTrip")) {
                LocalNotification.dispatchLocalNotification(mContext, vTitle, true);

                final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                generateAlert.setCancelable(false);
                generateAlert.setBtnClickList(btn_id -> doOperations());
                generateAlert.setContentMessage("", vTitle);
                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                generateAlert.showAlertBox();
            }
        } else {
            if (messageStr.equalsIgnoreCase("GoPayVerifyAmount")) {
                final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                generateAlert.setCancelable(false);
                generateAlert.setContentMessage("", vTitle);
                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                generateAlert.showAlertBox();
                LocalNotification.dispatchLocalNotification(mContext, vTitle, true);
            } else if (generalFunc.getJsonValueStr("CustomNotification", obj_msg) != null && generalFunc.getJsonValueStr("CustomNotification", obj_msg).equalsIgnoreCase("yes") && MyApp.getInstance().isMyAppInBackGround()) {

                MyApp.getInstance().getLoclaNotificationObj().customNotification(MyApp.getInstance().getApplicationContext(), obj_msg.toString());
                if (messageStr.equalsIgnoreCase("TripStarted") || messageStr.equalsIgnoreCase("TripEnd") || messageStr.equalsIgnoreCase("TripCancelledByDriver") || messageStr.equalsIgnoreCase("TripCancelled") || messageStr.equalsIgnoreCase("DestinationAdded")
                        || messageStr.equalsIgnoreCase("VerifyCharges") || messageStr.equalsIgnoreCase("VerifyChargesDeclined") || messageStr.equalsIgnoreCase("CabRequestAccepted") || messageStr.equalsIgnoreCase("DriverArrived")) {
                    if (messageStr.equalsIgnoreCase("TripEnd")) {
                        if (mContext != null) {
                            isTripEndDialogShown = true;
                            generalFunc.showGeneralMessage("", vTitle, "", generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"),
                                    buttonId -> MyApp.getInstance().restartWithGetDataApp());
                        }
                    }
                } /*else if (messageStr.equalsIgnoreCase("OrderDelivered") && !(MyApp.getInstance().getCurrentAct() instanceof TrackOrderActivity)) {
                    final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                    handleOrderDeliverdDialog(generalFunc, obj_msg, vTitle);
                }*/ else {
                    return;
                }
            }

            if (messageStr.equalsIgnoreCase("TripCancelledByDriver") || messageStr.equalsIgnoreCase("TripCancelled") || messageStr.equalsIgnoreCase("DestinationAdded") || messageStr.equalsIgnoreCase("TripEnd")) {

                if (messageStr.equalsIgnoreCase("TripEnd") || messageStr.equalsIgnoreCase("TripCancelledByDriver") || messageStr.equalsIgnoreCase("TripCancelled")) {
                    generalFunc.storeData(Utils.ISWALLETBALNCECHANGE, "Yes");
                }

                if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {

                    String iDriverId = generalFunc.getJsonValueStr("iDriverId", obj_msg);
                    String iTripId = generalFunc.getJsonValueStr("iTripId", obj_msg);
                    String showTripFare = generalFunc.getJsonValueStr("ShowTripFare", obj_msg);

                   /* if (MyApp.getInstance().getCurrentAct() instanceof OnGoingTripDetailsActivity && MyApp.getInstance().onGoingTripDetailsAct != null && generalFunc.getJsonValue("iTripId", MyApp.getInstance().onGoingTripDetailsAct.tripDetailJson).equals(iTripId)) {
                        MyApp.getInstance().onGoingTripDetailsAct.pubNubMsgArrived(obj_msg.toString(), true);
                    } else*/ if (MyApp.getInstance().getCurrentAct() instanceof AdditionalChargeActivity && MyApp.getInstance().additionalChargesAct != null && MyApp.getInstance().additionalChargesAct.tripDetail.get("iTripId").equals(iTripId)) {
                        MyApp.getInstance().additionalChargesAct.pubNubMsgArrived(obj_msg.toString(), true);
                    } else {
                        if (MyApp.getInstance().getCurrentAct() instanceof BookingActivity) {
                            ((BookingActivity) MyApp.getInstance().getCurrentAct()).focusFragment(1);
                        }

                        if (messageStr.equalsIgnoreCase("TripEnd") || showTripFare.equalsIgnoreCase("true")) {
                            showPubnubGeneralMessage(generalFunc, iTripId, vTitle, false, true);
                        } else {

                            if (MyApp.getInstance().getCurrentAct() instanceof ChatActivity || MyApp.getInstance().getCurrentAct() instanceof ConfirmEmergencyTapActivity) {

                                String tripId = "";
                                if (MyApp.getInstance().getCurrentAct() instanceof ChatActivity) {
                                    ChatActivity activity = (ChatActivity) MyApp.getInstance().getCurrentAct();
                                    tripId = activity.data_trip_ada.get("iTripId");
                                } else if (MyApp.getInstance().getCurrentAct() instanceof ConfirmEmergencyTapActivity) {
                                    ConfirmEmergencyTapActivity activity = (ConfirmEmergencyTapActivity) MyApp.getInstance().getCurrentAct();
                                    tripId = activity.iTripId;
                                }

                                if (!tripId.equalsIgnoreCase("") && iTripId.equalsIgnoreCase(tripId)) {
                                    generalFunc.showGeneralMessage("", vTitle, "", generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"),
                                            buttonId -> {
                                                MyApp.getInstance().restartWithGetDataApp();
                                            });
                                } else {
                                    generalFunc.showGeneralMessage("", vTitle);
                                }
                            } else {
                                generalFunc.showGeneralMessage("", vTitle);
                            }
                        }
                    }
                } else if (eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {

                    String iDriverId = generalFunc.getJsonValueStr("iDriverId", obj_msg);
                    String iTripId = generalFunc.getJsonValueStr("iTripId", obj_msg);
                    String showTripFare = generalFunc.getJsonValueStr("ShowTripFare", obj_msg);
                    String Is_Last_Delivery = generalFunc.getJsonValueStr("Is_Last_Delivery", obj_msg);

                   /* if (MyApp.getInstance().getCurrentAct() instanceof OnGoingTripDetailsActivity && MyApp.getInstance().onGoingTripDetailsAct != null && generalFunc.getJsonValue("iTripId", MyApp.getInstance().onGoingTripDetailsAct.tripDetailJson).equals(iTripId)) {
                        MyApp.getInstance().onGoingTripDetailsAct.pubNubMsgArrived(obj_msg.toString(), true);
                    } else {*/
                        if (MyApp.getInstance().getCurrentAct() instanceof BookingActivity) {
                            ((BookingActivity) MyApp.getInstance().getCurrentAct()).focusFragment(1);
                        }

                        if (messageStr.equalsIgnoreCase("TripEnd") || showTripFare.equalsIgnoreCase("true")) {

                            /*Multi Related Condi*/

                            if (Is_Last_Delivery.equalsIgnoreCase("Yes")) {
                                showMultiPubnubGeneralMessage(generalFunc, obj_msg, true);
                            } else {
                                showMultiPubnubGeneralMessage(generalFunc, obj_msg, false);
                            }

                        } else {

                            if (MyApp.getInstance().getCurrentAct() instanceof ChatActivity || MyApp.getInstance().getCurrentAct() instanceof ConfirmEmergencyTapActivity) {
                                String tripId = "";
                                if (MyApp.getInstance().getCurrentAct() instanceof ChatActivity) {
                                    ChatActivity activity = (ChatActivity) MyApp.getInstance().getCurrentAct();
                                    tripId = activity.data_trip_ada.get("iTripId");
                                } else if (MyApp.getInstance().getCurrentAct() instanceof ConfirmEmergencyTapActivity) {
                                    ConfirmEmergencyTapActivity activity = (ConfirmEmergencyTapActivity) MyApp.getInstance().getCurrentAct();
                                    tripId = activity.iTripId;
                                }

                                if (!tripId.equalsIgnoreCase("") && iTripId.equalsIgnoreCase(tripId)) {
                                    generalFunc.showGeneralMessage("", vTitle, "", generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"),
                                            buttonId -> {
                                                MyApp.getInstance().restartWithGetDataApp();

                                            });
                                } else {
                                    generalFunc.showGeneralMessage("", vTitle);
                                }

                            } else {
                                generalFunc.showGeneralMessage("", vTitle);
                            }
                        //}
                    }

                } else if (generalFunc.getJsonValueStr("eSystem", obj_msg).equalsIgnoreCase(Utils.eSystem_Type)) {
                    if (messageStr.equalsIgnoreCase("OrderConfirmByRestaurant") || messageStr.equalsIgnoreCase("OrderDeclineByRestaurant") || messageStr.equalsIgnoreCase("OrderPickedup") ||
                            messageStr.equalsIgnoreCase("OrderDelivered") || messageStr.equalsIgnoreCase("OrderCancelByAdmin")) {

                        if (messageStr.equalsIgnoreCase("OrderCancelByAdmin")) {
                            final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                            generateAlert.setCancelable(false);
                            generateAlert.setBtnClickList(btn_id -> MyApp.getInstance().restartWithGetDataApp());
                            generateAlert.setContentMessage("", vTitle);
                            generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                            generateAlert.showAlertBox();
                        } /*else if (messageStr.equalsIgnoreCase("OrderDelivered") && !(MyApp.getInstance().getCurrentAct() instanceof TrackOrderActivity)) {
                            handleOrderDeliverdDialog(generalFunc, obj_msg, vTitle);
                        }*/ else {

                            generalFunc.showGeneralMessage("", vTitle);
                        }
                    }
                } else {
                    if (!isTripEndDialogShown) {
                        final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                        generateAlert.setCancelable(false);
//                    generateAlert.setSystemAlertWindow(true);
                        generateAlert.setBtnClickList(btn_id -> MyApp.getInstance().restartWithGetDataApp());
                        generateAlert.setContentMessage("", vTitle);
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                        generateAlert.showAlertBox();
                    }
                }
                return;
            } else if (messageStr.equalsIgnoreCase("TripStarted") || messageStr.equalsIgnoreCase("DriverArrived")) {
                generalFunc.showGeneralMessage("", vTitle);
            } else {
                //  String vTitle = generalFunc.getJsonValueStr("vTitle", obj_msg);
                if (messageStr.equalsIgnoreCase("OrderConfirmByRestaurant") || messageStr.equalsIgnoreCase("OrderDeclineByRestaurant") || messageStr.equalsIgnoreCase("OrderPickedup") ||
                        messageStr.equalsIgnoreCase("OrderDelivered") || messageStr.equalsIgnoreCase("OrderCancelByAdmin") || messageStr.equalsIgnoreCase("OrderReviewItems")) {

                    if (generalFunc.getJsonValueStr("CustomNotification", obj_msg) != null && generalFunc.getJsonValueStr("CustomNotification", obj_msg).equalsIgnoreCase("yes") && MyApp.getInstance().isMyAppInBackGround()) {

//                        Intent intent=new Intent(mContext, NotificationService.class);
//                        intent.putExtra("Data",obj_msg.toString());
//                        mContext. startService(intent);

                        MyApp.getInstance().getLoclaNotificationObj().customNotification(mContext, obj_msg.toString());
                        return;
                    } /*else if (messageStr.equalsIgnoreCase("OrderDelivered") && !(MyApp.getInstance().getCurrentAct() instanceof TrackOrderActivity)) {

                        handleOrderDeliverdDialog(generalFunc, obj_msg, vTitle);

                    } *//*else if (messageStr.equalsIgnoreCase("OrderConfirmByRestaurant") && !(MyApp.getInstance().getCurrentAct() instanceof TrackOrderActivity)) {
                        final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                        generateAlert.setCancelable(false);
                        generateAlert.setBtnClickList(btn_id -> {
                            generateAlert.closeAlertBox();
                            if (btn_id == 0) {
                                Bundle bn = new Bundle();
                                bn.putString("iOrderId", generalFunc.getJsonValueStr("iOrderId", obj_msg));
                                bn.putBoolean("isDeliverNotify", false);
                                new ActUtils(mContext).startActWithData(TrackOrderActivity.class, bn);
                            }
                        });
                        generateAlert.setContentMessage("", vTitle);
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_VIEW_DETAILS"));
                        generateAlert.showAlertBox();
                    }*/ else {
                        generalFunc.showGeneralMessage("", vTitle);
                    }
                } else if (messageStr.equalsIgnoreCase("VerifyCharges")) {
                    generalFunc.showGeneralMessage("", vTitle, "", generalFunc.retrieveLangLBl("", "LBL_CONTINUE_BTN"),
                            buttonId -> {
                                if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                                    MyApp.getInstance().restartWithGetDataApp();
                                } else {
                                    String iTripId = generalFunc.getJsonValueStr("iTripId", obj_msg);

                                    if (MyApp.getInstance().additionalChargesAct != null) {
                                        MyApp.getInstance().additionalChargesAct.pubNubMsgArrived(obj_msg.toString(), false);
                                    } /*else if (MyApp.getInstance().onGoingTripDetailsAct != null) {
                                        MyApp.getInstance().onGoingTripDetailsAct.verifyCharges(obj_msg.toString());
                                    } else if (MyApp.getInstance().getCurrentAct() instanceof OnGoingTripsActivity) {
                                        ((OnGoingTripsActivity) MyApp.getInstance().getCurrentAct()).getOngoingUserTrips(iTripId);
                                    } else if (!(MyApp.getInstance().getCurrentAct() instanceof OnGoingTripsActivity)) {
                                        Bundle bn = new Bundle();
                                        bn.putBoolean("isRestart", true);
                                        bn.putString("iTripId", iTripId);
                                        bn.putBoolean("fromNoti", true);
                                        new ActUtils(MyApp.getInstance().getCurrentAct()).startActForResult(BookingActivity.class, bn, Utils.ASSIGN_DRIVER_CODE);
                                    }*/
                                }
                            });
                } else if (messageStr.equalsIgnoreCase("VerifyChargesDeclined")) {
                    generalFunc.showGeneralMessage("", vTitle, "", generalFunc.retrieveLangLBl("", "LBL_CONTINUE_BTN"),
                            buttonId -> {
                                if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                                    MyApp.getInstance().restartWithGetDataApp();
                                }
                            });
                } else if (messageStr.contains("Bidding")) {
                    manageBindViewTypeWise(messageStr, obj_msg);
                }
            }
        }
        if (messageStr.equalsIgnoreCase("OutstandingGenerated")) {
            generalFunc.showGeneralMessage("", generalFunc.getJsonValue("vTitle", obj_msg.toString()), "", generalFunc.retrieveLangLBl("", "LBL_BTN_PAYMENT_TXT"), button_Id -> {
                if (button_Id == 1) {
                    Bundle bn = new Bundle();
                    bn.putString("url", generalFunc.getJsonValue("PAYMENT_URL", obj_msg.toString()));
                    bn.putBoolean("handleResponse", true);
                    bn.putBoolean("isBack", false);
                    bn.putBoolean("isApiCall", true);
                    new ActUtils(mContext).startActWithData(PaymentWebviewActivity.class, bn);
                }
            });
        }

        /*if (MyApp.getInstance().onGoingTripDetailsAct != null) {
            MyApp.getInstance().onGoingTripDetailsAct.pubNubMsgArrived(obj_msg.toString(), false);
        }*/

        /*if (MyApp.getInstance().getCurrentAct() instanceof OrderPlaceConfirmActivity && messageStr.equalsIgnoreCase("CabRequestAccepted")) {
            // for manage restart apps when notification come
        } else*/ if (MyApp.getInstance().mainAct != null && (!eType.equalsIgnoreCase(Utils.CabGeneralType_UberX))) {
            MyApp.getInstance().mainAct.pubNubMsgArrived(obj_msg.toString());
            /*if (MyApp.getInstance().carWashAct != null) {
                MyApp.getInstance().carWashAct.pubNubMsgArrived(obj_msg.toString());
            }*/
        } /*else if (MyApp.getInstance().carWashAct != null && messageStr.equalsIgnoreCase("CabRequestAccepted")) {
            MyApp.getInstance().carWashAct.pubNubMsgArrived(obj_msg.toString());
        }*/ else if (MyApp.getInstance().uberXAct != null && messageStr.equalsIgnoreCase("CabRequestAccepted")) {
            MyApp.getInstance().uberXAct.pubNubMsgArrived(obj_msg.toString());
        } else if (MyApp.getInstance().uberXHomeAct != null && messageStr.equalsIgnoreCase("CabRequestAccepted")) {
            MyApp.getInstance().uberXHomeAct.pubNubMsgArrived(obj_msg.toString());
        } else if (MyApp.getInstance().rideDeliveryActivity != null && messageStr.equalsIgnoreCase("CabRequestAccepted")) {
            MyApp.getInstance().rideDeliveryActivity.pubNubMsgArrived(obj_msg.toString());
        }
//        if (generalFunc.getJsonValueStr("CustomNotification", obj_msg) != null && generalFunc.getJsonValueStr("CustomNotification", obj_msg).equalsIgnoreCase("yes") && MyApp.getInstance().isMyAppInBackGround()) {
//            LocalNotification localNotification = new LocalNotification();
//            localNotification.customNotification(MyApp.getInstance().getApplicationContext(), obj_msg.toString());
//            return;
//        }
    }

    private void manageBindViewTypeWise(String type, JSONObject obj_msg) {
        GenerateAlertBox alertBox = new GenerateAlertBox(MyApp.getInstance().getCurrentAct());
        alertBox.setContentMessage("", MyApp.getInstance().getAppLevelGeneralFunc().convertNumberWithRTL(MyApp.getInstance().getAppLevelGeneralFunc().getJsonValueStr("vTitle", obj_msg)));
        alertBox.setPositiveBtn(MyApp.getInstance().getAppLevelGeneralFunc().retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        alertBox.setBtnClickList(btn_id -> {
            alertBox.closeAlertBox();
            if (btn_id == 1) {

                switch (type) {
                    case "BiddingTaskReoffered":
                    case "BiddingTaskDeclined":
                        /*if (MyApp.getInstance().getCurrentAct() instanceof BiddingTaskActivity) {
                            BiddingTaskActivity biddingTaskActivity = (BiddingTaskActivity) MyApp.getInstance().getCurrentAct();
                            biddingTaskActivity.refreshImgView.performClick();
                        } else {*/
                            /*Intent biddingTaskIntent = new Intent(MyApp.getInstance().getApplicationContext(), BiddingTaskActivity.class);
                            biddingTaskIntent.putExtra("iBiddingPostId", MyApp.getInstance().getAppLevelGeneralFunc().getJsonValueStr("iBiddingPostId", obj_msg));
                            biddingTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MyApp.getInstance().getApplicationContext().startActivity(biddingTaskIntent);*/
                        //}
                        return;
                    case "BiddingTaskArrived":
                    case "BiddingTaskStarted":
                    case "BiddingTaskOngoing":
                       /* if (MyApp.getInstance().getCurrentAct() instanceof OnGoingTripDetailsActivity) {
                            OnGoingTripDetailsActivity onGoingTripDetailsActivity = (OnGoingTripDetailsActivity) MyApp.getInstance().getCurrentAct();
                            onGoingTripDetailsActivity.getTripDeliveryLocations();
                        } else {*/
                            /*Intent biddingTaskIntent = new Intent(MyApp.getInstance().getApplicationContext(), OnGoingTripDetailsActivity.class);
                            biddingTaskIntent.putExtra("iBiddingPostId", MyApp.getInstance().getAppLevelGeneralFunc().getJsonValueStr("iBiddingPostId", obj_msg));
                            biddingTaskIntent.putExtra("eType", "Bidding");
                            biddingTaskIntent.putExtra("isBid", true);
                            biddingTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MyApp.getInstance().getApplicationContext().startActivity(biddingTaskIntent);*/
                        //}
                        return;
                    case "BiddingTaskFinished":
                        Bundle bn = new Bundle();
                        bn.putBoolean("isBid", true);
                        bn.putString("iBiddingPostId", MyApp.getInstance().getAppLevelGeneralFunc().getJsonValueStr("iBiddingPostId", obj_msg));
                        new ActUtils(mContext).startActWithData(RatingActivity.class, bn);
                        return;
                }
                if (MyApp.getInstance().getCurrentAct() instanceof UberXHomeActivity) {
                    UberXHomeActivity mainActivity = (UberXHomeActivity) MyApp.getInstance().getCurrentAct();
                    mainActivity.checkBiddingView(2);
                } /*else if (MyApp.getInstance().getCurrentAct() instanceof BiddingTaskActivity) {
                    BiddingTaskActivity biddingTaskActivity = (BiddingTaskActivity) MyApp.getInstance().getCurrentAct();
                    biddingTaskActivity.refreshImgView.performClick();

                }*/ else {
                    if (MyApp.getInstance().getCurrentAct() instanceof BookingActivity) {
                        BookingActivity bookingsActivity = (BookingActivity) MyApp.getInstance().getCurrentAct();
                        bookingsActivity.setFrag(2);
                    } else {
                        Intent booksActInt = new Intent(MyApp.getInstance().getApplicationContext(), BookingActivity.class);
                        if (obj_msg != null) {
                            booksActInt.putExtras(MyApp.getInstance().getAppLevelGeneralFunc().createChatBundle(obj_msg));
                        }
                        booksActInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        booksActInt.putExtra("viewPos", 2);
                        MyApp.getInstance().getApplicationContext().startActivity(booksActInt);
                    }
                }
            }
        });
        alertBox.showAlertBox();
    }


    private void doOperations() {
//        MyApp.getInstance().restartWithGetDataApp()
    }

    private void dispatchNotification(String message) {
        Context mLocContext = this.mContext;

        if (mLocContext == null && MyApp.getInstance() != null && MyApp.getInstance().getCurrentAct() == null) {
            mLocContext = MyApp.getInstance().getApplicationContext();
        }

        if (mLocContext != null) {
            GeneralFunctions generalFunc = MyApp.getInstance().getGeneralFun(mLocContext);

            if (!GeneralFunctions.isJsonObj(message)) {
                LocalNotification.dispatchLocalNotification(mLocContext, message, true);
                return;
            }

            JSONObject obj_msg = generalFunc.getJsonObject(message);

            if (msgHandling(generalFunc, obj_msg)) {
                return;
            }

            String message_str = generalFunc.getJsonValueStr("Message", obj_msg);

            if (message_str.equals("")) {
                String msgType_str = generalFunc.getJsonValueStr("MsgType", obj_msg);

                switch (msgType_str) {
                    case "CHAT":
                        generalFunc.storeData("OPEN_CHAT", obj_msg.toString());
                        LocalNotification.dispatchLocalNotification(mLocContext, generalFunc.getJsonValueStr("Msg", obj_msg), false);
                        break;

                    default:
                        LocalNotification.dispatchLocalNotification(mLocContext, generalFunc.getJsonValueStr("vTitle", obj_msg), false);
                }

            } else {
                String pass_msg = generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vTitle", obj_msg));
                switch (message_str) {

                    case "TripCancelledByDriver":
                    case "TripCancelled":
                        generalFunc.saveGoOnlineInfo();
                        if (generalFunc.getJsonValueStr("CustomNotification", obj_msg) != null && generalFunc.getJsonValueStr("CustomNotification", obj_msg).equalsIgnoreCase("yes") && MyApp.getInstance().isMyAppInBackGround()) {

                            MyApp.getInstance().getLoclaNotificationObj().customNotification(MyApp.getInstance().getApplicationContext(), obj_msg.toString());
                            return;
                        } else {
                            LocalNotification.dispatchLocalNotification(mLocContext, pass_msg, false);
                        }
                        break;
                    case "DriverArrived":
                    case "DestinationAdded":
                    case "TripStarted":
                    case "TripEnd":
                    case "CabRequestAccepted":
                        if (generalFunc.getJsonValueStr("CustomNotification", obj_msg) != null && generalFunc.getJsonValueStr("CustomNotification", obj_msg).equalsIgnoreCase("yes") && MyApp.getInstance().isMyAppInBackGround()) {

                            MyApp.getInstance().getLoclaNotificationObj().customNotification(MyApp.getInstance().getApplicationContext(), obj_msg.toString());

                        } else {
                            LocalNotification.dispatchLocalNotification(mLocContext, pass_msg, false);
                        }
                        break;
                    case "OrderDelivered":
                    case "OrderPickedup":
                    case "OrderConfirmByRestaurant":
                    case "OrderDeclineByRestaurant":
                    case "OrderCancelByAdmin":
                    case "OrderReviewItems":

                        if (generalFunc.getJsonValueStr("CustomNotification", obj_msg) != null && generalFunc.getJsonValueStr("CustomNotification", obj_msg).equalsIgnoreCase("yes") && MyApp.getInstance().isMyAppInBackGround()) {
                           /* if (message_str.equalsIgnoreCase("OrderDelivered") && !(MyApp.getInstance().getCurrentAct() instanceof TrackOrderActivity)) {
                                handleOrderDeliverdDialog(generalFunc, obj_msg, pass_msg);

                            } else {*/
                                MyApp.getInstance().getLoclaNotificationObj().customNotification(MyApp.getInstance().getApplicationContext(), obj_msg.toString());
                            //}
                            return;
                        } else {
                            /*if (message_str.equalsIgnoreCase("OrderDelivered") && !(MyApp.getInstance().getCurrentAct() instanceof TrackOrderActivity)) {
                                handleOrderDeliverdDialog(generalFunc, obj_msg, pass_msg);
                            } else {*/
                                MyApp.getInstance().getLoclaNotificationObj().customNotification(MyApp.getInstance().getApplicationContext(), obj_msg.toString());
                            //}
                        }
                        break;
                }
            }
        }
    }

    private void handleOrderDeliverdDialog(GeneralFunctions generalFunc, JSONObject obj_msg, String msg) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            Bundle bn = new Bundle();
            bn.putString("iOrderId", generalFunc.getJsonValueStr("iOrderId", obj_msg));
            bn.putBoolean("isDeliverNotify", true);
            //new ActUtils(mContext).startActWithData(TrackOrderActivity.class, bn);
        });
        generateAlert.setContentMessage("", msg);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.showAlertBox();
    }

    private boolean msgHandling(GeneralFunctions generalFunc, JSONObject obj_msg) {
        String MsgType = generalFunc.getJsonValueStr("MsgType", obj_msg);
        if (MsgType != null) {
            //String messageStr = generalFunc.getJsonValueStr("Message", obj_msg);

            switch (MsgType) {
                case "TwilioVideocall":
                    CommunicationManager.getInstance().incomingCommunicate(mContext, generalFunc, null, obj_msg);
                    return true;
            }
        }
        return false;
    }

    private boolean isTripStatusMsgExist(GeneralFunctions generalFunc, String msg, Context mContext) {

        JSONObject obj_tmp = generalFunc.getJsonObject(msg);

        if (obj_tmp != null) {

            String message = generalFunc.getJsonValueStr("Message", obj_tmp);
            String vConfirmationCode = generalFunc.getJsonValueStr("vConfirmationCode", obj_tmp);
            String randomUniqueCode = generalFunc.getJsonValueStr("iamunique", obj_tmp);
            String Message = generalFunc.getJsonValueStr("Message", obj_tmp);

            if (!message.equals("")) {
                String iTripId = "";
                String iBiddingPostId = "";
                if (generalFunc.getJsonValue("eSystem", msg).equalsIgnoreCase(Utils.eSystem_Type)) {
                    iTripId = generalFunc.getJsonValueStr("iOrderId", obj_tmp);
                } else {
                    iTripId = generalFunc.getJsonValueStr("iTripId", obj_tmp);
                }

                if (generalFunc.getJsonValue("iBiddingPostId", obj_tmp) != null && !generalFunc.getJsonValue("iBiddingPostId", obj_tmp).equals("")) {
                    iBiddingPostId = generalFunc.getJsonValueStr("iBiddingPostId", obj_tmp);
                }
                String iTripDeliveryLocationId = generalFunc.getJsonValueStr("iTripDeliveryLocationId", obj_tmp);
                if (!iTripId.equals("")) {
                    String vTitle = generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vTitle", obj_tmp));
                    String time = generalFunc.getJsonValueStr("time", obj_tmp);
                    String key = "";
                    if (generalFunc.getJsonValue("eType", msg).equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                        key = Utils.TRIP_REQ_CODE_PREFIX_KEY + iTripId + "_" + iTripDeliveryLocationId + "_" + message;
                    } else if (Message.equalsIgnoreCase("VerifyCharges") || Message.equalsIgnoreCase("VerifyChargesDeclined")) {
                        key = Utils.TRIP_REQ_CODE_PREFIX_KEY + iTripId + "_" + vConfirmationCode + "_" + randomUniqueCode + "_" + message;
                    } else {
                        key = Utils.TRIP_REQ_CODE_PREFIX_KEY + iTripId + "_" + message;
                    }
                    if (message.equals("DestinationAdded")) {
                        long newMsgTime = GeneralFunctions.parseLongValue(0, time);

                        String destKeyValueStr = GeneralFunctions.retrieveValue(key, mContext);
                        if (!destKeyValueStr.equals("")) {

                            long destKeyValue = GeneralFunctions.parseLongValue(0, destKeyValueStr);
                            if (newMsgTime > destKeyValue) {
                                generalFunc.removeValue(key);
                            } else {
                                return true;
                            }
                        }
                    }
                    if (tmp_store_key.equalsIgnoreCase(key)) {
                        return true;
                    }
                    tmp_store_key = key;
                    String data = generalFunc.retrieveValue(key);

                    if (data.equals("")) {
                        if (!message.equalsIgnoreCase("TripRequestCancel")) {
                            LocalNotification.dispatchLocalNotification(mContext, vTitle, true);
                        }
                        if (message.equalsIgnoreCase("TripRequestCancel")) {
                            generalFunc.storeData(key + "_" + System.currentTimeMillis(), "" + System.currentTimeMillis());
                        } else {
                            if (time.equals("")) {
                                generalFunc.storeData(key, "" + System.currentTimeMillis());
                            } else {
                                generalFunc.storeData(key, "" + time);
                            }
                        }
                        tmp_store_key = "";
                        return false;
                    } else {
                        tmp_store_key = "";
                        return true;
                    }
                } else if (!iBiddingPostId.equalsIgnoreCase("")) {
                    String time = generalFunc.getJsonValueStr("time", obj_tmp);
                    String key = "";
                    key = Utils.TRIP_REQ_CODE_PREFIX_KEY + iBiddingPostId + "_" + message + "" + time;
                    String data = generalFunc.retrieveValue(key);
                    if (data.equals("")) {
                        if (time.equals("")) {
                            generalFunc.storeData(key, "" + System.currentTimeMillis());
                        } else {
                            generalFunc.storeData(key, "" + time);
                        }
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    String msgType = generalFunc.getJsonValueStr("MsgType", obj_tmp);
                    if (msgType != null) {
                        String key, data, tRandomValue = "";
                        switch (msgType) {
                            case "TwilioVideocall":
                                tRandomValue = generalFunc.getJsonValueStr("tRandomCode", obj_tmp);
                                break;
                        }
                        if (Utils.checkText(tRandomValue)) {
                            key = Utils.TRIP_REQ_CODE_PREFIX_KEY + tRandomValue + "_" + msgType;
                            data = generalFunc.retrieveValue(key);
                            generalFunc.storeData(key, "" + System.currentTimeMillis());
                            return !data.equals("");
                        }
                    }
                }
            } else {
                String msgType = generalFunc.getJsonValueStr("MsgType", obj_tmp);
                if (msgType != null) {
                    String key, data, tRandomValue = "";
                    switch (msgType) {
                        case "TripRequestCancel":
                            tRandomValue = generalFunc.getJsonValueStr("iTripId", obj_tmp);
                            break;
                        case "Notification":
                            tRandomValue = generalFunc.getJsonValueStr("tRandomCode", obj_tmp);
                            break;
                    }
                    if (Utils.checkText(tRandomValue)) {
                        key = Utils.TRIP_REQ_CODE_PREFIX_KEY + tRandomValue + "_" + msgType;
                        data = generalFunc.retrieveValue(key);
                        generalFunc.storeData(key, "" + System.currentTimeMillis());
                        return !data.equals("");
                    }
                }
            }
        }
        return false;
    }

    private void showMultiPubnubGeneralMessage(GeneralFunctions generalFunc, final JSONObject msg_Obj, final boolean isMultirate) {
        try {
            String message = generalFunc.getJsonValueStr("vTitle", msg_Obj);

            final GenerateAlertBox generateAlert = new GenerateAlertBox(MyApp.getInstance().getCurrentAct());
            generateAlert.setContentMessage("", message);
            generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));

            if (generalFunc.getJsonValueStr("eType", msg_Obj).equalsIgnoreCase(Utils.eType_Multi_Delivery) && generalFunc.getJsonValueStr("Is_Last_Delivery", msg_Obj).equalsIgnoreCase("Yes")) {
                generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
            }

            generateAlert.setBtnClickList(btn_id -> {
                generateAlert.closeAlertBox();
                if (mContext instanceof MainActivity) {
                    if (((MainActivity) mContext).driverAssignedHeaderFrag != null && ((MainActivity) mContext).driverAssignedHeaderFrag.backImgView != null) {
                        ((MainActivity) mContext).driverAssignedHeaderFrag.backImgView.performClick();
                    }
                }
                if (btn_id == 0) {
                    return;
                } else if (btn_id == 1 && isMultirate) {
                    Bundle bn = new Bundle();
                    bn.putBoolean("isUfx", false);
                    bn.putString("iTripId", generalFunc.getJsonValueStr("iTripId", msg_Obj));

                    if (!Utils.checkText(generalFunc.getJsonValueStr("iTripId", msg_Obj))) {
                        return;
                    }
                    new ActUtils(mContext).startActForResult(HistoryDetailActivity.class, bn, Utils.MULTIDELIVERY_HISTORY_RATE_CODE);
                }
            });
            generateAlert.showAlertBox();
        } catch (Exception e) {
            Logger.d("AlertEx", e.toString());
        }
    }

    private void showPubnubGeneralMessage(GeneralFunctions generalFunc, final String iTripId, final String message, final boolean isrestart, final boolean isufxrate) {
        try {
            final GenerateAlertBox generateAlert = new GenerateAlertBox(MyApp.getInstance().getCurrentAct());
            generateAlert.setContentMessage("", message);
            generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
            generateAlert.setBtnClickList(btn_id -> {
                generateAlert.closeAlertBox();
                if (isrestart) {
                    MyApp.getInstance().restartWithGetDataApp();
                }
                if (isufxrate) {
                    Bundle bn = new Bundle();
                    bn.putBoolean("isUfx", true);
                    bn.putString("iTripId", iTripId);
                    new ActUtils(mContext).startActWithData(RatingActivity.class, bn);
                }
            });
            generateAlert.showAlertBox();
        } catch (Exception e) {
            Logger.d("AlertEx", e.toString());
        }
    }
}