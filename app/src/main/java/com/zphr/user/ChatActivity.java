package com.zphr.user;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.RecyclerView;

import com.activity.ParentActivity;
import com.adapter.files.ChatMessagesRecycleAdapter;
import com.general.files.GeneralFunctions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.service.handler.ApiHandler;
import com.utils.CommonUtilities;
import com.utils.LoadImage;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.simpleratingbar.SimpleRatingBar;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends ParentActivity {

    Context mContext;


    EditText input;
    MTextView userNameTxt, catTypeText;
    SimpleRatingBar ratingBar;
    SelectableRoundedImageView userImgView;
    public HashMap<String, String> data_trip_ada;
    DatabaseReference dbRef;

    String passengerImgName = "";
    private ChatMessagesRecycleAdapter chatAdapter;
    private ArrayList<HashMap<String, Object>> chatList;

    MTextView tv_booking_no;
    ImageView msgbtn, backImgView;
    LinearLayout mainArea;
    ProgressBar progressBar;
    //set media sound for chat
    static MediaPlayer mp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design_trip_chat_detail_dialog);


        data_trip_ada = new HashMap<>();

        Logger.d("ChatActivity", "::" + getIntent().getStringExtra("iBiddingPostId"));

        data_trip_ada.put("iTripId", getIntent().getStringExtra("iTripId"));
        if (getIntent().getStringExtra("iBiddingPostId") != null && !getIntent().getStringExtra("iBiddingPostId").equalsIgnoreCase("")) {
            data_trip_ada.put("iTripId", getIntent().getStringExtra("iBiddingPostId") + "_" + getIntent().getStringExtra("iDriverId"));
        }


        mContext = ChatActivity.this;

        getDetails();

        initViews();


        passengerImgName = generalFunc.getJsonValueStr("vImgName", obj_userProfile);


        if (getIntent().getStringExtra("iBiddingPostId") != null && !getIntent().getStringExtra("iBiddingPostId").equalsIgnoreCase("")) {
            dbRef = FirebaseDatabase.getInstance().getReference().child(generalFunc.retrieveValue(Utils.APP_GCM_SENDER_ID_KEY) + "-chat").child(data_trip_ada.get("iTripId") + "-Bidding");
        } else {
            dbRef = FirebaseDatabase.getInstance().getReference().child(generalFunc.retrieveValue(Utils.APP_GCM_SENDER_ID_KEY) + "-chat").child(data_trip_ada.get("iTripId") + "-Trip");
        }


        chatList = new ArrayList<>();

        show();

    }

    public static void playNotificationSound() {
        if (mp != null && !mp.isPlaying()) {
            mp.setLooping(false);
            mp.start();
        }
    }

    public void getDetails() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "getMemberTripDetails");
        parameters.put("UserType", Utils.userType);

        if (getIntent().getStringExtra("iBiddingPostId") != null && !getIntent().getStringExtra("iBiddingPostId").equalsIgnoreCase("")) {
            parameters.put("iBiddingPostId", getIntent().getStringExtra("iBiddingPostId"));
            parameters.put("iDriverId", getIntent().getStringExtra("iDriverId"));
        } else {
            parameters.put("iTripId", getIntent().getStringExtra("iTripId"));
        }

        ApiHandler.execute(getActContext(), parameters, responseString -> {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);
            if (isDataAvail == true) {
                mainArea.setVisibility(View.VISIBLE);

                String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                userNameTxt.setText(generalFunc.getJsonValue("vName", message));
                catTypeText.setText(generalFunc.getJsonValue("vServiceName", message));


                new LoadImage.builder(LoadImage.bind(generalFunc.getJsonValue("vImage", message)), userImgView).setErrorImagePath(R.mipmap.ic_no_pic_user).setPlaceholderImagePath(R.mipmap.ic_no_pic_user).build();


                ratingBar.setRating(GeneralFunctions.parseFloatValue(0, generalFunc.getJsonValue("vAvgRating", message)));

                ((MTextView) findViewById(R.id.titleTxt)).setText("#" + generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("vRideNo", message)));
                ((MTextView) findViewById(R.id.chatsubtitleTxt)).setVisibility(View.VISIBLE);
                ((MTextView) findViewById(R.id.chatsubtitleTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(generalFunc.getJsonValue("tTripRequestDate", message), Utils.OriginalDateFormate, CommonUtilities.OriginalDateFormate)));

                data_trip_ada.put("iFromMemberId", generalFunc.getJsonValue("iMemberId", message));
                data_trip_ada.put("FromMemberImageName", generalFunc.getJsonValue("vImage", message));
                data_trip_ada.put("FromMemberName", generalFunc.getJsonValue("vName", message));
                data_trip_ada.put("vBookingNo", generalFunc.getJsonValue("vRideNo", message));
                data_trip_ada.put("vDate", generalFunc.getJsonValue("tTripRequestDate", message));

            }


        });

    }

    private void initViews() {
        input = (EditText) findViewById(R.id.input);
        userNameTxt = (MTextView) findViewById(R.id.userNameTxt);
        ratingBar = (SimpleRatingBar) findViewById(R.id.ratingBar);
        catTypeText = (MTextView) findViewById(R.id.catTypeText);
        msgbtn = (ImageView) findViewById(R.id.msgbtn);
        userImgView = (SelectableRoundedImageView) findViewById(R.id.userImgView);
        mainArea = (LinearLayout) findViewById(R.id.mainArea);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mainArea.setVisibility(View.GONE);
        backImgView = (findViewById(R.id.backImgView));
        catTypeText.setText(getIntent().getStringExtra("DisplayCat"));
        ratingBar.setRating(GeneralFunctions.parseFloatValue(0, getIntent().getStringExtra("vAvgRating")));


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public int verticalScrollOffset = 0;

    public Context getActContext() {
        return ChatActivity.this;
    }

    public void show() {


        msgbtn.setImageResource(R.drawable.ic_chat_send_disable);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    msgbtn.setImageResource(R.drawable.ic_chat_send_disable);
                } else {
                    msgbtn.setImageResource(R.drawable.ic_chat_send);
                }


            }
        });

        input.setHint(generalFunc.retrieveLangLBl("Enter a message", "LBL_ENTER_MESSAGE"));

        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        backImgView.setOnClickListener(v -> {
            Utils.hideKeyboard(ChatActivity.this);
            onBackPressed();
        });

        msgbtn.setOnClickListener(view -> {

            if (Utils.checkText(input) && Utils.getText(input).length() > 0) {
                // Read the input field and push a new instance
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("eUserType", Utils.app_type);
                dataMap.put("Text", input.getText().toString().trim());
                dataMap.put("iTripId", data_trip_ada.get("iTripId"));
                dataMap.put("passengerImageName", passengerImgName);
                dataMap.put("driverImageName", data_trip_ada.get("FromMemberImageName"));
                dataMap.put("passengerId", generalFunc.getMemberId());
                dataMap.put("driverId", data_trip_ada.get("iFromMemberId"));
                dataMap.put("vDate", generalFunc.getCurrentDateHourMin());
                dataMap.put("vTimeZone", generalFunc.getTimezone());


                dbRef.push().setValue(dataMap, (databaseError, databaseReference) -> {

                    if (databaseError != null) {

                    } else {

                        sendTripMessageNotification(input.getText().toString().trim());

                        // Clear the input
                        input.setText("");
                    }
                });

            }


        });

        final RecyclerView chatCategoryRecyclerView = (RecyclerView) findViewById(R.id.chatCategoryRecyclerView);
        chatAdapter = new ChatMessagesRecycleAdapter(mContext, chatList, generalFunc, data_trip_ada);
        chatCategoryRecyclerView.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.getValue() != null && dataSnapshot.getValue() instanceof HashMap) {
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    chatList.add(dataMap);

                    chatAdapter.notifyDataSetChanged();
                    chatCategoryRecyclerView.scrollToPosition(chatList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public void sendTripMessageNotification(String message) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "SendTripMessageNotification");
        parameters.put("UserType", Utils.userType);
        parameters.put("iFromMemberId", generalFunc.getMemberId());
        parameters.put("iToMemberId", data_trip_ada.get("iFromMemberId"));
        parameters.put("tMessage", message);
        if (getIntent().getStringExtra("iBiddingPostId") != null && !getIntent().getStringExtra("iBiddingPostId").equalsIgnoreCase("")) {
            parameters.put("iBiddingPostId", getIntent().getStringExtra("iBiddingPostId"));
            parameters.put("iDriverId", getIntent().getStringExtra("iDriverId"));
        } else {
            parameters.put("iTripId", getIntent().getStringExtra("iTripId"));
        }

        ApiHandler.execute(getActContext(), parameters, responseString -> {
        });
    }

    public void setCurrentTripData(Bundle bn) {

        String iTripId = data_trip_ada != null && data_trip_ada.containsKey("iTripId") ? data_trip_ada.get("iTripId") : "";

        String iTripIdNoti = bn != null ? bn.get("iTripId").toString().trim() : "";
        String iTripIdCurrent = iTripId != null ? iTripId.trim() : "";
        if (Utils.checkText(iTripId) && Utils.checkText(iTripIdCurrent) && iTripIdCurrent.equals(iTripIdNoti)) {
            Intent intent = new Intent(ChatActivity.this, ChatActivity.class);
            intent.putExtras(bn);
            startActivity(intent);
            ChatActivity.this.finish();
        }
    }
}
