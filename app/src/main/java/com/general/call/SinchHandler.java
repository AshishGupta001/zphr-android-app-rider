package com.general.call;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.zphr.user.R;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.video.VideoScalingType;
import com.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class SinchHandler {

    private static final String TAG = SinchHandler.class.getSimpleName();
    private static SinchHandler instance;
    private GeneralFunctions generalFunc;
    private Context mContext;
    private V3CallListener mListener;

    private final HashMap<String, String> mCallHashMap = new HashMap<>();
    public SinchClient mSinchClient;
    private final String mUserName, SINCH_APP_KEY, SINCH_APP_SECRET_KEY, SINCH_APP_ENVIRONMENT_HOST;
    private ViewGroup localVideo, remoteView;
    Boolean isSwitch = true;

    @Nullable
    private Call mCall = null;

    public static SinchHandler getInstance() {
        if (instance == null) {
            instance = new SinchHandler();
        }
        return instance;
    }

    public void setUIListener(MediaDataProvider dataProvider, V3CallScreen mActivity, V3CallListener V3CallListener) {
        this.mContext = mActivity;
        this.generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        this.mListener = V3CallListener;
        mListener.onUIChanges();
        mListener.onCallProgressing();
        if (dataProvider.isVideoCall) {
            localVideo = mActivity.findViewById(R.id.localVideo);
            remoteView = mActivity.findViewById(R.id.remoteVideo);
            try {

                final VideoController vc = mSinchClient.getVideoController();
                if (vc != null) {
                    mActivity.runOnUiThread(() -> remoteView.addView(vc.getLocalView()));
                }
                if (mCall != null && mCall.getDetails().isVideoOffered()) {
                    mCall.resumeVideo();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public SinchHandler() {
        this.mContext = MyApp.getInstance().getCurrentAct();
        this.generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        this.mUserName = Utils.userType + "_" + generalFunc.getMemberId();

        this.SINCH_APP_KEY = this.generalFunc.retrieveValue(Utils.SINCH_APP_KEY);
        this.SINCH_APP_SECRET_KEY = this.generalFunc.retrieveValue(Utils.SINCH_APP_SECRET_KEY);
        this.SINCH_APP_ENVIRONMENT_HOST = this.generalFunc.retrieveValue(Utils.SINCH_APP_ENVIRONMENT_HOST);
        // this.SINCH_APP_ENVIRONMENT_HOST = "ocra.api.sinch.com";

        mCallHashMap.put("Id", generalFunc.getMemberId());
        mCallHashMap.put("type", Utils.userType);
    }

    public void removeInitiateService() {
        if (mSinchClient != null) {
            mSinchClient.setSupportManagedPush(false);
            mSinchClient.terminateGracefully();
        }
    }

    private void updateVideoViews(final boolean remoteVisible) {
        MyApp.getInstance().getCurrentAct().runOnUiThread(() -> {
            if (remoteView != null && localVideo != null) {

                setSinch(remoteVisible, 0);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) remoteView.getLayoutParams();
                RelativeLayout.LayoutParams localparams = (RelativeLayout.LayoutParams) localVideo.getLayoutParams();
                if (remoteVisible) {
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.height = dpToPx(150);
                    params.width = dpToPx(120);
                    params.topMargin = dpToPx(100);
                    params.rightMargin = dpToPx(20);
                    localparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    remoteView.bringToFront();

                } else {
                    params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    localparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    localparams.height = dpToPx(150);
                    localparams.width = dpToPx(120);
                    localparams.topMargin = dpToPx(100);
                    localparams.rightMargin = dpToPx(20);

                }
                remoteView.setLayoutParams(params);
                localVideo.setLayoutParams(localparams);


                if (isSwitch) isSwitch = false;
                else isSwitch = true;
            }
        });
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void initiateService(String mName, String mImage) {
        mCallHashMap.put("Name", mName);
        mCallHashMap.put("PImage", mImage);
        if (mSinchClient == null) {
            if (SINCH_APP_KEY == null || SINCH_APP_KEY.equalsIgnoreCase("")) {
                return;
            }
            mSinchClient = Sinch.getSinchClientBuilder().context(mContext).userId(mUserName)
                    .applicationKey(this.SINCH_APP_KEY)
                    .environmentHost(this.SINCH_APP_ENVIRONMENT_HOST).build();
            setSinch(false, 2);
            mSinchClient.startListeningOnActiveConnection();
            mSinchClient.getCallClient().setRespectNativeCalls(false);
            mSinchClient.setSupportManagedPush(true);

            setSinch(false, 3);
            mSinchClient.getCallClient().addCallClientListener((callClient, call) -> {
                mCall = call;
                addCallListener(call);
                CommunicationManager.getInstance().incomingCommunicate(mContext, generalFunc, call, null);
            });
            mSinchClient.start();
        }
    }

    public void relayRemotePushNotificationPayload(final Map payload) {
        mSinchClient.relayRemotePushNotificationPayload(payload);
    }

    public void executeAction(Context mContext, CommunicationManager.TYPE communication_type, MediaDataProvider dataProvider) {
        switch (communication_type) {
            case PHONE_CALL:
                mCall = mSinchClient.getCallClient().callPhoneNumber(dataProvider.phoneNumber, mCallHashMap);
                break;
            case CHAT:
                break;
            case VIDEO_CALL:
                mCallHashMap.put("isVideoCall", "Yes");
                mCall = mSinchClient.getCallClient().callUserVideo(dataProvider.toMemberType + "_" + dataProvider.callId, mCallHashMap);
                if (mCall != null) {
                    addCallListener(mCall);
                    mSinchClient.getVideoController().setResizeBehaviour(VideoScalingType.ASPECT_FILL);
                    setSinch(false, 1);
                }
                break;
            case VOIP_CALL:
                mCallHashMap.put("isVideoCall", "No");
                mCall = mSinchClient.getCallClient().callUser(dataProvider.toMemberType + "_" + dataProvider.callId, mCallHashMap);
                if (mCall != null) {
                    addCallListener(mCall);
                    mSinchClient.getAudioController().disableSpeaker();
                }
                break;
        }

    }

    public void answerClicked() {
        if (mCall != null) {
            mCall.answer();
            addCallListener(mCall);
            if (mCall.getDetails().isVideoOffered()) {
                mSinchClient.getVideoController().setResizeBehaviour(VideoScalingType.ASPECT_FILL);
                setSinch(false, 1);
            } else {
                mSinchClient.getAudioController().disableSpeaker();
            }
        }
    }

    // TODO: 12-12-2021 / Viral | AUDIO_CALLING_METHOD = LOCAL | commit this method in side code
    private void setSinch(boolean remoteVisible, int i) {
        /*if (i == 0) {
            mSinchClient.getVideoController().setLocalVideoZOrder(!remoteVisible);
        } else if (i == 1) {
            mSinchClient.getVideoController().setLocalVideoResizeBehaviour(VideoScalingType.ASPECT_FILL);
        } else if (i == 2) {
            ((DefaultSinchClient) mSinchClient).setSupportActiveConnection(true);
        } else if (i == 3) {
            mSinchClient.addSinchClientListener(new SinchClientListener() {
                @Override
                public void onCredentialsRequired(ClientRegistration clientRegistration) {
                    clientRegistration.register(JWT.create(SINCH_APP_KEY, SINCH_APP_SECRET_KEY, mUserName));
                }

                @Override
                public void onUserRegistered() {
                    Logger.d(TAG, "User registered.");
                }

                @Override
                public void onUserRegistrationFailed(SinchError sinchError) {
                    Logger.e(TAG, "User registration failed: " + sinchError.getMessage());
                }

                @Override
                public void onPushTokenRegistered() {
                    Logger.e(TAG, "onPushTokenRegistered() should never been called in the application w/o Managed Push support.");
                }

                @Override
                public void onPushTokenRegistrationFailed(SinchError sinchError) {
                    Logger.e(TAG, "onPushTokenRegistrationFailed() should never been called in the application w/o Managed Push support.");
                }

                @Override
                public void onClientStarted(SinchClient sinchClient) {
                    if (mListener != null) {
                        mListener.onCallEstablished();
                    }
                }

                @Override
                public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
                    Logger.d(TAG, "SinchClient Failed");
                }

                @Override
                public void onLogMessage(int i, String s, String s1) {
                    Logger.d(TAG, "SinchClient started");
                }
            });
        }*/
    }

    // TODO: 12-12-2021 / Viral | AUDIO_CALLING_METHOD = LOCAL | commit this method in side code
    private void addCallListener(Call call) {
        /*call.addCallListener(new CallListener() {
            @Override
            public void onCallProgressing(Call call) {
                mListener.onCallProgressing();
            }

            @Override
            public void onCallEstablished(Call call) {
                if (call.getDetails().isVideoOffered()) {
                    speakerBtnClicked(true);
                }
                mListener.onCallEstablished();
            }

            @Override
            public void onCallEnded(Call call) {
                callEnded();
            }
        });*/
    }

    public void muteBtnClicked(boolean isMute) {
        if (isMute) {
            mSinchClient.getAudioController().mute();
        } else {
            mSinchClient.getAudioController().unmute();
        }
        mListener.onMuteView(isMute);
    }

    public void speakerBtnClicked(boolean isSpeaker) {
        if (isSpeaker) {
            mSinchClient.getAudioController().enableSpeaker();
        } else {
            mSinchClient.getAudioController().disableSpeaker();
        }
        mListener.onSpeakerView(isSpeaker);
    }

    public void switchCameraBtnClicked() {
        mSinchClient.getVideoController().toggleCaptureDevicePosition();
        mListener.onCameraView(null, true);
    }

    public void callEnded() {
        if (CommunicationManager.MEDIA_TYPE == CommunicationManager.MEDIA.SINCH) {
            MyApp.getInstance().getCurrentAct().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }
        mSinchClient.setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_CALL_ENDED"));
        if (mCall != null) {
            mCall.hangup();
        }
        removeVideoViews();
        if (mListener != null) {
            mListener.onCallEnded();
        } else {
            if (MyApp.getInstance().getCurrentAct() instanceof V3CallScreen) {
                V3CallScreen v3CallScreen = (V3CallScreen) MyApp.getInstance().getCurrentAct();
                v3CallScreen.finish();
            }
        }
    }

    public void setEstablishedAfterUI(boolean isMute, boolean isSpeaker, boolean isSpeakerClick, boolean isFront) {
        Activity mActivity = MyApp.getInstance().getCurrentAct();
        mActivity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        if (mCall != null && mCall.getDetails().isVideoOffered()) {
            try {
                final VideoController vc = mSinchClient.getVideoController();
                if (vc != null) {
                    mActivity.runOnUiThread(() -> {
                        remoteView.removeView(vc.getLocalView());
                        localVideo.addView(vc.getLocalView());
                        remoteView.addView(vc.getRemoteView());
                        remoteView.setOnClickListener((View v) -> updateVideoViews(isSwitch));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        new Handler(Looper.myLooper()).postDelayed(() -> {
            muteBtnClicked(isMute);
            if (isSpeakerClick) {
                speakerBtnClicked(isSpeaker);
            } else {
                speakerBtnClicked(mCall != null && mCall.getDetails().isVideoOffered() ? true : isSpeaker);
            }
        }, 2000);
    }


    private void removeVideoViews() {
        try {
            if (mSinchClient == null) {
                return; // early
            }
            final VideoController vc = mSinchClient.getVideoController();
            if (vc != null) {
                MyApp.getInstance().getCurrentAct().runOnUiThread(() -> {
                    if (remoteView != null) {
                        remoteView.removeView(vc.getLocalView());
                        remoteView.removeView(vc.getRemoteView());
                    }
                    if (localVideo != null) {
                        localVideo.removeView(vc.getLocalView());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}