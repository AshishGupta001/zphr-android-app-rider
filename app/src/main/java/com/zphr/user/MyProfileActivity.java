package com.zphr.user;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import com.activity.ParentActivity;
import com.fragments.EditProfileFragment;
import com.fragments.ProfileFragment;
import com.general.files.ActUtils;
import com.general.files.FilePath;
import com.general.files.FileSelector;
import com.general.files.GeneralFunctions;
import com.general.files.UploadProfileImage;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import java.util.ArrayList;

public class MyProfileActivity extends ParentActivity {

    public String isDriverAssigned = "";
    public boolean isEdit = false, isMobile = false, isEmail = false;
    private MTextView titleTxt;
    private SelectableRoundedImageView userProfileImgView, editIconImgView;

    ProfileFragment profileFrag;
    EditProfileFragment editProfileFrag;
    private String SITE_TYPE = "", SITE_TYPE_DEMO_MSG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_profile);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        isDriverAssigned = getIntent().getStringExtra("isDriverAssigned");
        isEdit = getIntent().getBooleanExtra("isEdit", false);
        isMobile = getIntent().getBooleanExtra("isMobile", false);
        isEmail = getIntent().getBooleanExtra("isEmail", false);

        titleTxt = findViewById(R.id.titleTxt);
        ImageView backImgView = findViewById(R.id.backImgView);
        userProfileImgView = findViewById(R.id.userProfileImgView);

        editIconImgView = findViewById(R.id.editIconImgView);
        FrameLayout userImgArea = findViewById(R.id.userImgArea);
        addToClickHandler(userImgArea);
        addToClickHandler(backImgView);

        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }

        new CreateRoundedView(getResources().getColor(R.color.white), Utils.dipToPixels(getActContext(), getResources().getDimension(R.dimen._15sdp)), 0,
                Color.parseColor("#00000000"), editIconImgView);

        editIconImgView.setColorFilter(getResources().getColor(R.color.appThemeColor_1));

        userProfileImgView.setImageResource(R.mipmap.ic_no_pic_user);

        String url = CommonUtilities.USER_PHOTO_PATH + generalFunc.getMemberId() + "/" + generalFunc.getJsonValue("vImgName", obj_userProfile);
        generalFunc.checkProfileImage(userProfileImgView, url, R.mipmap.ic_no_pic_user, R.mipmap.ic_no_pic_user);

        String vImgName_str = generalFunc.getJsonValueStr("vImgName", obj_userProfile);

        if (vImgName_str == null || vImgName_str.equals("") || vImgName_str.equals("NONE")) {
            editIconImgView.setImageResource(R.drawable.ic_pic_add);

        }


        SITE_TYPE = generalFunc.getJsonValueStr("SITE_TYPE", obj_userProfile);
        SITE_TYPE_DEMO_MSG = generalFunc.getJsonValueStr("SITE_TYPE_DEMO_MSG", obj_userProfile);


        openEditProfileFragment();
    }

    public void changePageTitle(String title) {
        titleTxt.setText(title);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // perform your desired action here
            // return 'true' to prevent further propagation of the key event
            return true;
        }
        // let the system handle all other key events
        return super.onKeyDown(keyCode, event);
    }

    public Context getActContext() {
        return MyProfileActivity.this;
    }

    private void openEditProfileFragment() {
        if (editProfileFrag != null) {
            editProfileFrag = null;
            Utils.runGC();
        }
        editProfileFrag = new EditProfileFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, editProfileFrag).commit();
    }

    public EditProfileFragment getEditProfileFrag() {
        return this.editProfileFrag;
    }

    public ProfileFragment getProfileFrag() {
        return this.profileFrag;
    }

    public void changeUserProfileJson(String userProfileJson) {
        Bundle bn = new Bundle();
        generalFunc.storeData(Utils.WALLET_ENABLE, generalFunc.getJsonValue("WALLET_ENABLE", userProfileJson));
        generalFunc.storeData(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValue("REFERRAL_SCHEME_ENABLE", userProfileJson));
        new ActUtils(getActContext()).setOkResult(bn);
        generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));
    }

    public View getCurrView() {
        return generalFunc.getCurrentView(MyProfileActivity.this);
    }

    private boolean isValidImageResolution(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);
        int width = options.outWidth;
        int height = options.outHeight;
        return width >= Utils.ImageUpload_MINIMUM_WIDTH && height >= Utils.ImageUpload_MINIMUM_HEIGHT;
    }

    private String[] generateImageParams(String key, String content) {
        String[] tempArr = new String[2];
        tempArr[0] = key;
        tempArr[1] = content;
        return tempArr;
    }

    private void imageUpload(Uri fileUri) {
        if (SITE_TYPE.equalsIgnoreCase("Demo") && generalFunc.getJsonValue("vEmail", generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)).equalsIgnoreCase("Driver@gmail.com")) {
            generalFunc.showGeneralMessage("", SITE_TYPE_DEMO_MSG);
            return;
        }

        if (fileUri == null) {
            generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_ERROR_OCCURED"));
            return;
        }

        ArrayList<String[]> paramsList = new ArrayList<>();
        paramsList.add(generateImageParams("iMemberId", generalFunc.getMemberId()));
        paramsList.add(generateImageParams("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY)));
        paramsList.add(generateImageParams("GeneralUserType", Utils.app_type));
        paramsList.add(generateImageParams("GeneralMemberId", generalFunc.getMemberId()));
        paramsList.add(generateImageParams("type", "uploadImage"));

        String selectedImagePath = FilePath.getPath(getActContext(), fileUri);
        if (isValidImageResolution(selectedImagePath)) {
            new UploadProfileImage(MyProfileActivity.this, selectedImagePath, Utils.TempProfileImageName, paramsList).execute();
        } else {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Please select image which has minimum is 256 * 256 resolution.", "LBL_MIN_RES_IMAGE"));
        }
    }

    public void handleImgUploadResponse(String responseString) {

        if (responseString != null && !responseString.equals("")) {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            if (isDataAvail) {
                generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));
                obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                changeUserProfileJson(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                String vImgName_str = generalFunc.getJsonValueStr("vImage", obj_userProfile);

                if (vImgName_str == null || vImgName_str.equals("") || vImgName_str.equals("NONE")) {
                    editIconImgView.setImageResource(R.drawable.ic_pic_add);
                } else {
                    editIconImgView.setImageResource(R.mipmap.ic_edit);

                }

                String url = CommonUtilities.USER_PHOTO_PATH + generalFunc.getMemberId() + "/" + generalFunc.getJsonValue("vImgName", obj_userProfile);
                generalFunc.checkProfileImage(userProfileImgView, url, R.mipmap.ic_no_pic_user, R.mipmap.ic_no_pic_user);


            } else {
                generalFunc.showGeneralMessage("",
                        generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
            }
        } else {
            generalFunc.showError();
        }
    }


    public void onClick(View view) {
        Utils.hideKeyboard(getActContext());
        switch (view.getId()) {
            case R.id.backImgView:
                MyProfileActivity.super.onBackPressed();
                break;
            case R.id.userImgArea:
                getFileSelector().openFileSelection(FileSelector.FileType.CroppedImage);
                break;
        }
    }


    @Override
    public void onFileSelected(Uri mFileUri, String mFilePath, FileSelector.FileType mFileType) {
        imageUpload(mFileUri);
    }
}