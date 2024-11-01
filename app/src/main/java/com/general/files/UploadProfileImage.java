package com.general.files;

import static com.general.files.GeneralFunctions.rotateBitmap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.NonNull;

import com.zphr.user.BuildConfig;
import com.zphr.user.MyProfileActivity;
import com.zphr.user.PrescriptionActivity;
import com.service.server.AppClient;
import com.service.server.ServerTask;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.ScalingUtilities;
import com.utils.Utils;
import com.view.MyProgressDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Admin on 08-07-2016.
 */
public class UploadProfileImage {

    private final GeneralFunctions generalFunc;
    private final Activity act;
    private MyProgressDialog myPDialog;

    private final String temp_File_Name, selectedImagePath;
    private String responseString = "";
    private final ArrayList<String[]> paramsList;
    private boolean isAudio = false;

    public UploadProfileImage(Activity myProfileAct, String selectedImagePath, String temp_File_Name, ArrayList<String[]> paramsList) {
        this.selectedImagePath = selectedImagePath;
        this.temp_File_Name = temp_File_Name;
        this.paramsList = paramsList;
        this.act = myProfileAct;
        this.generalFunc = MyApp.getInstance().getGeneralFun(act);
    }

    public UploadProfileImage(Activity myProfileAct, String selectedImagePath, String temp_File_Name, ArrayList<String[]> paramsList, boolean isaudio) {
        this.selectedImagePath = selectedImagePath;
        this.temp_File_Name = temp_File_Name;
        this.paramsList = paramsList;
        this.act = myProfileAct;
        this.generalFunc = MyApp.getInstance().getGeneralFun(act);
        this.isAudio = isaudio;
    }

    public void execute() {
        myPDialog = new MyProgressDialog(act, false, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
        try {
            myPDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String filePath = "";
        if (!selectedImagePath.equalsIgnoreCase("")) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                String var5 = null;
                Bitmap var6;

                String path = selectedImagePath;
                int DESIREDWIDTH = Utils.ImageUpload_DESIREDWIDTH;
                int DESIREDHEIGHT = Utils.ImageUpload_DESIREDHEIGHT;

                try {
                    int var7 = Utils.getExifRotation(path);
                    Bitmap var8 = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.CROP);
                    if (var8.getWidth() <= DESIREDWIDTH && var8.getHeight() <= DESIREDHEIGHT) {
                        if (var8.getWidth() > var8.getHeight()) {
                            var6 = ScalingUtilities.createScaledBitmap(var8, var8.getHeight(), var8.getHeight(), ScalingUtilities.ScalingLogic.CROP);
                        } else {
                            var6 = ScalingUtilities.createScaledBitmap(var8, var8.getWidth(), var8.getWidth(), ScalingUtilities.ScalingLogic.CROP);
                        }
                    } else {
                        var6 = ScalingUtilities.createScaledBitmap(var8, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.CROP);
                    }

                    var6 = rotateBitmap(var6, var7);
                    String var9 = act.getExternalCacheDir().toString();
                    File var10 = new File(var9 + "/" + "TempImages");
                    if (!var10.exists()) {
                        var10.mkdir();
                    }

                    File var11 = new File(var10.getAbsolutePath(), temp_File_Name);
                    var5 = var11.getAbsolutePath();
                    FileOutputStream var12;

                    try {
                        var12 = new FileOutputStream(var11);
                        var6.compress(Bitmap.CompressFormat.JPEG, 60, var12);
                        var12.flush();
                        var12.close();
                    } catch (Exception var14) {
                        var14.printStackTrace();
                    }

                    var6.recycle();
                } catch (Throwable var16) {
                    Logger.e("", "" + var16.getMessage());
                }

                filePath = var5 == null ? path : var5;
            } else {
                filePath = generalFunc.decodeFile(selectedImagePath, Utils.ImageUpload_DESIREDWIDTH,
                        Utils.ImageUpload_DESIREDHEIGHT, temp_File_Name);
            }
        }


        if (filePath.equals("")) {
            HashMap<String, String> dataParams = new HashMap<>();
            for (int i = 0; i < paramsList.size(); i++) {
                String[] arrData = paramsList.get(i);

                dataParams.put(arrData[0], arrData[1]);
            }

            Call<Object> call = AppClient.getClient("POST", CommonUtilities.SERVER).getResponse(CommonUtilities.SERVER_WEBSERVICE_PATH, dataParams);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                    if (response.isSuccessful()) {
                        responseString = AppClient.getGSONBuilder().toJson(response.body());
                        fireResponse();
                    } else {
                        responseString = "";
                        fireResponse();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                    Logger.d("DataError", "::" + t.getMessage());
                    responseString = "";
                    fireResponse();
                }
            });
            return;
        }

        File file = new File(filePath);

        MultipartBody.Part filePart = null;
        if (!file.equals("")) {
            if (isAudio) {
                filePart = MultipartBody.Part.createFormData("voiceDirectionFile", temp_File_Name, RequestBody.create(MediaType.parse("multipart/form-data"), file));
            } else {
                filePart = MultipartBody.Part.createFormData("vImage", temp_File_Name, RequestBody.create(MediaType.parse("multipart/form-data"), file));
            }
        }

        HashMap<String, RequestBody> dataParams = new HashMap<>();

        for (int i = 0; i < paramsList.size(); i++) {
            String[] arrData = paramsList.get(i);
            dataParams.put(arrData[0], RequestBody.create(MediaType.parse("text/plain"), arrData[1]));
        }

        if (dataParams != null) {
            dataParams.put("tSessionId", RequestBody.create(MediaType.parse("text/plain"), generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY)));
            dataParams.put("deviceHeight", RequestBody.create(MediaType.parse("text/plain"), Utils.getScreenPixelHeight(act) + ""));
            dataParams.put("deviceWidth", RequestBody.create(MediaType.parse("text/plain"), Utils.getScreenPixelWidth(act) + ""));
            dataParams.put("GeneralUserType", RequestBody.create(MediaType.parse("text/plain"), Utils.app_type));
            dataParams.put("GeneralMemberId", RequestBody.create(MediaType.parse("text/plain"), generalFunc.getMemberId()));
            dataParams.put("GeneralDeviceType", RequestBody.create(MediaType.parse("text/plain"), "" + Utils.deviceType));
            dataParams.put("GeneralAppVersion", RequestBody.create(MediaType.parse("text/plain"), BuildConfig.VERSION_NAME));
            dataParams.put("vTimeZone", RequestBody.create(MediaType.parse("text/plain"), generalFunc.getTimezone()));
            dataParams.put("vUserDeviceCountry", RequestBody.create(MediaType.parse("text/plain"), Utils.getUserDeviceCountryCode(act)));
            dataParams.put("APP_TYPE", RequestBody.create(MediaType.parse("text/plain"), ServerTask.CUSTOM_APP_TYPE));
            dataParams.put("UBERX_PARENT_CAT_ID", RequestBody.create(MediaType.parse("text/plain"), ServerTask.CUSTOM_UBERX_PARENT_CAT_ID));
            dataParams.put("vCurrentTime", RequestBody.create(MediaType.parse("text/plain"), generalFunc.getCurrentGregorianDateHourMin()));
        }

        Call<Object> call = AppClient.getClient("POST", CommonUtilities.SERVER).uploadData(CommonUtilities.SERVER_WEBSERVICE_PATH, filePart, dataParams);
        call.enqueue(new Callback<Object>() {

            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (response.isSuccessful()) {
                    responseString = AppClient.getGSONBuilder().toJson(response.body());
                    fireResponse();
                } else {
                    responseString = "";
                    fireResponse();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                Logger.d("DataError", "::" + t.getMessage());
                responseString = "";
                fireResponse();
            }
        });

    }

    private void fireResponse() {
        try {
            if (myPDialog != null) {
                myPDialog.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (act instanceof MyProfileActivity) {
            ((MyProfileActivity) act).handleImgUploadResponse(responseString);
        } else if (act instanceof PrescriptionActivity) {
            ((PrescriptionActivity) act).handleImgUploadResponse(responseString);
        }
    }
}