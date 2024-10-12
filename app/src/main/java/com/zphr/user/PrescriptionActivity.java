package com.zphr.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.ParentActivity;
import com.adapter.files.GalleryImagesRecyclerAdapter;
import com.general.files.ActUtils;
import com.general.files.FileSelector;
import com.general.files.GeneralFunctions;
import com.general.files.UploadProfileImage;
import com.service.handler.ApiHandler;
import com.utils.LoadImage;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.carouselview.CarouselView;
import com.view.carouselview.ViewListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class PrescriptionActivity extends ParentActivity implements GalleryImagesRecyclerAdapter.OnItemClickListener {

    private static final int SELECT_HISTROY_IMAGE = 4;
    ImageView backImgView;
    MTextView titleTxt;
    MTextView noteTxt, noDescTxt;
    RecyclerView imageListRecyclerView;
    MButton btn_type2, btn_type2_confirm;
    GalleryImagesRecyclerAdapter adapter;
    ArrayList<HashMap<String, String>> listData;
    AppCompatImageView noImgView;
    ProgressBar loading_images;
    private AlertDialog alertDialog;
    View carouselContainerView;
    CarouselView carouselView;
    MTextView closeCarouselTxtView;
    private LinearLayout confirmBtnArea, btn2Area;
    String iImageId = "";
    MTextView skipTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);
        backImgView = findViewById(R.id.backImgView);
        titleTxt = findViewById(R.id.titleTxt);
        carouselContainerView = findViewById(R.id.carouselContainerView);
        carouselView = findViewById(R.id.carouselView);
        noteTxt = findViewById(R.id.noteTxt);
        noDescTxt = findViewById(R.id.noDescTxt);
        closeCarouselTxtView = findViewById(R.id.closeCarouselTxtView);
        imageListRecyclerView = findViewById(R.id.imageListRecyclerView);
        confirmBtnArea = findViewById(R.id.confirmBtnArea);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        btn_type2_confirm = ((MaterialRippleLayout) findViewById(R.id.btn_type2_confirm)).getChildView();
        addToClickHandler(btn_type2_confirm);
        LinearLayout bottomarea = findViewById(R.id.bottomarea);
        skipTxt = findViewById(R.id.skipTxt);
        // skipTxt.setPaintFlags(skipTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btn2Area = findViewById(R.id.btn2Area);
        addToClickHandler(skipTxt);
        btn_type2.setId(Utils.generateViewId());
        noImgView = findViewById(R.id.noImgView);
        loading_images = findViewById(R.id.loading_images);
        addToClickHandler(btn_type2);
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_DONE"));
        noteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GALLERY_IMG_NOTE"));
        addToClickHandler(backImgView);
        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        addToClickHandler(closeCarouselTxtView);
        closeCarouselTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_CLOSE_TXT"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "Prescription"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ATTACH_PRESCRIPTION"));
        noDescTxt.setText(generalFunc.retrieveLangLBl("", "LBL_NOPRESCRIPTION"));
        noteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PRESCRIPTION_BODY_TEXT"));
        btn_type2_confirm.setText(generalFunc.retrieveLangLBl("", "LBL_CONFIRM_TXT"));
        skipTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SKIP_TXT"));

        if (getIntent().getBooleanExtra("isOrder", false)) {
            listData = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("imageList");
            adapter = new GalleryImagesRecyclerAdapter(getActContext(), listData, generalFunc, false, false, false);
            GridLayoutManager gridLay = new GridLayoutManager(getActContext(), adapter.getNumOfColumns());
            imageListRecyclerView.setLayoutManager(gridLay);
            imageListRecyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(this);
            noteTxt.setVisibility(View.GONE);
            bottomarea.setVisibility(View.GONE);
            skipTxt.setVisibility(View.GONE);


        } else {
            skipTxt.setVisibility(View.VISIBLE);
            listData = new ArrayList<>();
            adapter = new GalleryImagesRecyclerAdapter(getActContext(), listData, generalFunc, false, true, false);
            GridLayoutManager gridLay = new GridLayoutManager(getActContext(), adapter.getNumOfColumns());
            imageListRecyclerView.setLayoutManager(gridLay);
            imageListRecyclerView.setAdapter(adapter);
            imageListRecyclerView.setClipToPadding(false);
            adapter.setOnItemClickListener(this);
            getImages();
        }

        if (getIntent().getBooleanExtra("isBack", false)) {
            skipTxt.setVisibility(View.GONE);
        }

    }


    public void handleImgUploadResponse(String responseString) {

        if (responseString != null && !responseString.equals("")) {

            if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {
                getImages();
            } else {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
            }
        } else {
            generalFunc.showError();
        }
    }

    private void getImages() {
        loading_images.setVisibility(View.VISIBLE);
        noImgView.setVisibility(View.GONE);
        skipTxt.setVisibility(View.GONE);
        noDescTxt.setVisibility(View.GONE);
        btn2Area.setVisibility(View.GONE);
        confirmBtnArea.setVisibility(View.VISIBLE);
        listData.clear();
        adapter.notifyDataSetChanged();
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "getPrescriptionImages");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iServiceId", generalFunc.getServiceId());
        parameters.put("eSystem", Utils.eSystem_Type);


        ApiHandler.execute(getActContext(), parameters, responseString -> {
            if (responseString != null && !responseString.equalsIgnoreCase("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    listData.clear();

                    JSONArray arr_data = generalFunc.getJsonArray(Utils.message_str, responseString);

                    if (arr_data != null) {
                        HashMap<String, String> mapData1 = new HashMap<>();
                        mapData1.put("add", "add");
                        listData.add(mapData1);

                        for (int i = 0; i < arr_data.length(); i++) {
                            JSONObject obj_tmp = generalFunc.getJsonObject(arr_data, i);

                            HashMap<String, String> mapData = new HashMap<>();
                            Iterator<String> keysItr = obj_tmp.keys();
                            while (keysItr.hasNext()) {
                                String key = keysItr.next();
                                String value = generalFunc.getJsonValueStr(key, obj_tmp);

                                mapData.put(key, value);
                            }
                            mapData.put("isDelete", "Yes");
                            listData.add(mapData);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (listData.size() == 0) {
                        btn2Area.setVisibility(View.VISIBLE);
                        confirmBtnArea.setVisibility(View.GONE);
                        noDescTxt.setVisibility(View.VISIBLE);
                        noImgView.setVisibility(View.VISIBLE);
                        skipTxt.setVisibility(View.VISIBLE);
                    }
                } else {
                    btn2Area.setVisibility(View.VISIBLE);
                    confirmBtnArea.setVisibility(View.GONE);
                    noDescTxt.setVisibility(View.VISIBLE);
                    noImgView.setVisibility(View.VISIBLE);
                    skipTxt.setVisibility(View.VISIBLE);
                }

            } else {
                generalFunc.showError(true);
            }

            loading_images.setVisibility(View.GONE);
        });

    }


    @Override
    public void onItemClickList(View v, int position) {

        if (position == 0 && !getIntent().getBooleanExtra("isOrder", false)) {
            selectImage();
        } else {
            if (getIntent().getBooleanExtra("isOrder", false)) {
                carouselContainerView.setVisibility(View.VISIBLE);
                carouselView.setViewListener(viewListener);
                carouselView.setPageCount(listData.size());
                carouselView.setCurrentItem(position);
            } else {
                carouselContainerView.setVisibility(View.VISIBLE);
                carouselView.setViewListener(viewListener);
                carouselView.setPageCount(listData.size() - 1);
                carouselView.setCurrentItem(position - 1);
            }
        }
    }

    @Override
    public void onLongItemClickList(View v, int position) {

    }

    @Override
    public void onDeleteClick(View v, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.delete_precription, null);
        dialogView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        builder.setView(dialogView);


        //inflateviews
        MTextView messageTxt = dialogView.findViewById(R.id.messageTxt);
        MTextView cancelTxt = dialogView.findViewById(R.id.cancelTxt);
        MTextView submitTxt = dialogView.findViewById(R.id.submitTxt);

        messageTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DELETE_IMG_CONFIRM_PRESCEIPTION_NOTE"));
        cancelTxt.setOnClickListener(view -> {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
        });
        submitTxt.setOnClickListener(view -> {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            configPrescriptionImage(listData.get(position).get("iImageId"), "DELETE", "");
        });
        alertDialog = builder.create();
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.show();
    }


    ViewListener viewListener = position -> {
        ImageView customView = new ImageView(getActContext());

        CarouselView.LayoutParams layParams = new CarouselView.LayoutParams(CarouselView.LayoutParams.MATCH_PARENT, CarouselView.LayoutParams.MATCH_PARENT);
        customView.setLayoutParams(layParams);

        customView.setPadding(Utils.dipToPixels(getActContext(), 15), 0, Utils.dipToPixels(getActContext(), 15), 0);
        customView.setImageResource(R.mipmap.ic_no_icon);

        final HashMap<String, String> item;
        if (getIntent().getBooleanExtra("isOrder", false)) {
            item = listData.get(position);
        } else {
            item = listData.get(position + 1);
        }

        try {
            new LoadImage.builder(LoadImage.bind(Utils.getResizeImgURL(getActContext(), Objects.requireNonNull(item.get("vImage")), ((int) Utils.getScreenPixelWidth(getActContext())) - Utils.dipToPixels(getActContext(), 30), 0, Utils.getScreenPixelHeight(getActContext()) - Utils.dipToPixels(getActContext(), 30))), customView).setErrorImagePath(R.mipmap.ic_no_icon).setPlaceholderImagePath(R.mipmap.ic_no_icon).build();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return customView;
    };

    private Context getActContext() {
        return PrescriptionActivity.this;
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.backImgView) {
            PrescriptionActivity.super.onBackPressed();
        } else if (i == btn_type2.getId()) {
            selectImage();
        } else if (i == btn_type2_confirm.getId()) {

            if (getIntent().getBooleanExtra("isBack", false)) {
                onBackPressed();
                return;
            }
            Bundle bn = new Bundle();
            bn.putBoolean("isFromEditCard", getIntent().getBooleanExtra("isFromEditCard", false));
            bn.putBoolean("isPrescription", true);
            //new ActUtils(getActContext()).startActWithData(CheckOutActivity.class, bn);
            finish();
        } else if (i == closeCarouselTxtView.getId()) {
            if (carouselContainerView.getVisibility() == View.VISIBLE) {
                carouselContainerView.setVisibility(View.GONE);
            }
        } else if (i == skipTxt.getId()) {
            Bundle bn = new Bundle();
            bn.putBoolean("isFromEditCard", getIntent().getBooleanExtra("isFromEditCard", false));
            bn.putBoolean("isPrescription", true);
            //new ActUtils(getActContext()).startActWithData(CheckOutActivity.class, bn);
            finish();
        }
    }


    @Override
    public void onFileSelectorButtonClick(int btn_id) {
        super.onFileSelectorButtonClick(btn_id);
        new ActUtils(getActContext()).startActForResult(PrescriptionHistoryImagesActivity.class, SELECT_HISTROY_IMAGE);
    }

    @Override
    public void onFileSelected(Uri mFileUri, String mFilePath, FileSelector.FileType mFileType) {
        super.onFileSelected(mFileUri, mFilePath, mFileType);
        configPrescriptionImage("", "ADD", mFilePath);
    }

    private void selectImage() {
        getFileSelector().openFileSelection(FileSelector.FileType.Image, true, generalFunc.retrieveLangLBl("Prescriptions Upload by You", "LBL_PRESCRIPTION_UPLOADED_BY_YOU"), R.drawable.ic_medical_prescription, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_HISTROY_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                iImageId = data.getStringExtra("iImageId");
                getPreviouslyUploadedbyYou();
            } else {
                generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("Can't read selected image. Please try again.", "LBL_IMAGE_READ_FAILED"));
            }
        }
    }

    private void getPreviouslyUploadedbyYou() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "PreviouslyUploadedbyYou");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iServiceId", generalFunc.getServiceId());
        parameters.put("eSystem", Utils.eSystem_Type);
        parameters.put("iImageId", iImageId);
        ApiHandler.execute(getActContext(), parameters, responseString -> {
            if (responseString != null && !responseString.equalsIgnoreCase("")) {
                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {
                    getImages();
                } else {
                    generalFunc.showError(true);
                }
            }
        });

    }

    private void configPrescriptionImage(String iImageId, String action_type, String selectedImagePath) {

        ArrayList<String[]> paramsList = new ArrayList<>();
        paramsList.add(generalFunc.generateImageParams("type", "PrescriptionImages"));
        paramsList.add(generalFunc.generateImageParams("iUserId", generalFunc.getMemberId()));
        paramsList.add(generalFunc.generateImageParams("UserType", Utils.app_type));
        paramsList.add(generalFunc.generateImageParams("action_type", action_type));
        paramsList.add(generalFunc.generateImageParams("iImageId", iImageId));
        paramsList.add(generalFunc.generateImageParams("eSystem", Utils.eSystem_Type));
        paramsList.add(generalFunc.generateImageParams("iMemberId", generalFunc.getMemberId()));
        paramsList.add(generalFunc.generateImageParams("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY)));
        paramsList.add(generalFunc.generateImageParams("GeneralUserType", Utils.app_type));
        paramsList.add(generalFunc.generateImageParams("GeneralMemberId", generalFunc.getMemberId()));
        paramsList.add(generalFunc.generateImageParams("iServiceId", generalFunc.getServiceId()));

        new UploadProfileImage(PrescriptionActivity.this, selectedImagePath, Utils.TempProfileImageName, paramsList).execute();
    }
}