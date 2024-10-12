package com.general.files;

import android.content.Context;

import com.service.handler.ApiHandler;
import com.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;

public class GetHomeScreenData {

    private Context context;
    private GeneralFunctions generalFunctions;
    private JSONObject obj_userProfile;

    public void getHomescreenData(Context context, GeneralFunctions generalFunctions, JSONObject obj_userProfile) {
        this.context = context;
        this.generalFunctions = generalFunctions;
        this.obj_userProfile = obj_userProfile;
        homeScreenApiCall();

    }

    private void homeScreenApiCall() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "getServiceCategories");
        parameters.put("userId", generalFunctions.getMemberId());
        parameters.put("parentId", generalFunctions.getJsonValueStr(Utils.UBERX_PARENT_CAT_ID, obj_userProfile));
        parameters.put("vLatitude", "");
        parameters.put("vLongitude", "");
        parameters.put("eForVideoConsultation", "");

        ApiHandler.execute(context, parameters, false, false, generalFunctions, responseString -> generalFunctions.storeData("SERVICE_HOME_DATA", responseString));
    }
}