package com.general.files;

import android.content.Context;

import androidx.annotation.NonNull;

import com.service.handler.ApiHandler;
import com.utils.Utils;

import java.util.HashMap;

public class GetUserLanguagesForAllServiceType {

    private final GeneralFunctions mGeneralFunc;
    private final Context mContext;
    private final String iServiceId;

    public GetUserLanguagesForAllServiceType(@NonNull Context context, @NonNull GeneralFunctions generalFunc, String iServiceId) {
        this.mContext = context;
        mGeneralFunc = generalFunc;
        this.iServiceId = iServiceId;
        getLanguageLabelServiceWise();
    }

    private void getLanguageLabelServiceWise() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "changelanguagelabel");
        parameters.put("vLang", mGeneralFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        parameters.put("iServicesIDS", iServiceId);
        parameters.put("eSystem", Utils.eSystem_Type);

        ApiHandler.execute(mContext, parameters, false, false, mGeneralFunc, responseString -> {
            // data get Done
            mGeneralFunc.storeData("LanguagesForAllServiceType", responseString);
        });
    }
}