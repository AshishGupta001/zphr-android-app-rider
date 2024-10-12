package com.zphr.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.activity.ParentActivity;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

/**
 * Created by Admin on 03-11-2016.
 */
public class InviteFriendsActivity extends ParentActivity {

    MTextView titleTxt, shareTxtLbl, invitecodeTxt, shareTxt;
    ImageView backImgView;
    String Refreal_code = "";
    private MButton btn_type3;
    String LBL_INVITE_FRIEND_TXT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        init();
    }

    private void init() {
        titleTxt = findViewById(R.id.titleTxt);
        shareTxtLbl = findViewById(R.id.shareTxtLbl);
        invitecodeTxt = findViewById(R.id.invitecodeTxt);
        shareTxt = findViewById(R.id.shareTxt);
        backImgView = findViewById(R.id.backImgView);

        btn_type3 = ((MaterialRippleLayout) findViewById(R.id.btn_type3)).getChildView();
        btn_type3.setId(Utils.generateViewId());

        setLabels();

        addToClickHandler(btn_type3);
        addToClickHandler(backImgView);
        addToClickHandler(invitecodeTxt);
        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
    }

    private void setLabels() {
        LBL_INVITE_FRIEND_TXT = generalFunc.retrieveLangLBl("", "LBL_INVITE_FRIEND_TXT");
        titleTxt.setText(LBL_INVITE_FRIEND_TXT);
        btn_type3.setText(LBL_INVITE_FRIEND_TXT);
        shareTxtLbl.setText(generalFunc.retrieveLangLBl("", "LBL_INVITE_FRIEND_SHARE"));
        shareTxt.setText((generalFunc.getJsonValueStr("INVITE_DESCRIPTION_CONTENT", obj_userProfile)));
        Refreal_code = generalFunc.getJsonValueStr("vRefCode", obj_userProfile);
        invitecodeTxt.setText(Refreal_code.trim());
    }

    private Context getActContext() {
        return InviteFriendsActivity.this;
    }


    public void onClick(View view) {
        Utils.hideKeyboard(getActContext());
        int i = view.getId();
        if (i == R.id.backImgView) {
            InviteFriendsActivity.super.onBackPressed();
        } else if (i == btn_type3.getId()) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, LBL_INVITE_FRIEND_TXT);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, generalFunc.getJsonValueStr("INVITE_SHARE_CONTENT", obj_userProfile));
            startActivity(Intent.createChooser(sharingIntent, generalFunc.retrieveLangLBl("", "LBL_SHARE_USING")));
        } else if (i == invitecodeTxt.getId()) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", Utils.getText(invitecodeTxt));
            clipboard.setPrimaryClip(clip);
            generalFunc.showMessage(backImgView, generalFunc.retrieveLangLBl("Invite code is copied to clipboard", "LBL_INVITE_COPY_CLIPBOARD"));
        }
    }

}