package com.general.files;

import android.content.Context;
import android.location.Location;

import com.service.handler.ApiHandler;
import com.service.server.ServerTask;
import com.utils.Logger;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class ConfigPassengerTripStatus implements GetLocationUpdates.LocationUpdates, RecurringTask.OnTaskRunCalled {
    Context mContext;

    /**
     * Variable declaration of singleton instance.
     */
    private static ConfigPassengerTripStatus configPassengerInstance = null;

    GeneralFunctions generalFunc;
    InternetConnection intCheck;
    private ServerTask currentExeTask;
    int FETCH_TRIP_STATUS_TIME_INTERVAL = 15;

    public Location userLoc = null;

    private RecurringTask updateUserStatusTask;
    private GetLocationUpdates getLocUpdates;
    private boolean isDispatchThreadLocked = false;
    private boolean isFirstCall = true;
    /**
     * Used to remove redundant messages (trip message or others). Internal purpose only.
     */
    private HashMap<String, String> listOfCurrentTripMsg_Map = new HashMap<>();


    /**
     * Creating Singleton instance. By using this method will create only one instance of this class.
     */
    public static ConfigPassengerTripStatus getInstance() {
        if (configPassengerInstance == null) {
            configPassengerInstance = new ConfigPassengerTripStatus(MyApp.getInstance().getApplicationContext());
        }
        return configPassengerInstance;
    }

    public ConfigPassengerTripStatus(Context mContext) {
        this.mContext = mContext;

        generalFunc = MyApp.getInstance().getAppLevelGeneralFunc();
        intCheck = new InternetConnection(this.mContext);
    }

    /**
     * Used to start driver's status task. This will initialize frequent task to update device's current location to server and trip status messages.
     */
    public void startUserStatusUpdateTask() {

        stopLocationUpdateTask();

        Context mContext = MyApp.getInstance().getApplicationContext();

        if (mContext != null) {


            stopUserStatusUpdateTask();

            getLocUpdates = new GetLocationUpdates(mContext, Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, true, this);
            FETCH_TRIP_STATUS_TIME_INTERVAL = GeneralFunctions.parseIntegerValue(15, GeneralFunctions.retrieveValue(Utils.FETCH_TRIP_STATUS_TIME_INTERVAL_KEY, mContext));

            updateUserStatusTask = new RecurringTask(FETCH_TRIP_STATUS_TIME_INTERVAL * 1000);

            updateUserStatusTask.setTaskRunListener(this);
            updateUserStatusTask.startRepeatingTask();


        }
    }


    public void forceDestroy() {
        stopLocationUpdateTask();
        stopUserStatusUpdateTask();
    }

    /**
     * Used to stop location updates.
     */
    private void stopLocationUpdateTask() {
        if (getLocUpdates != null) {
            getLocUpdates.stopLocationUpdates();
            getLocUpdates = null;
        }
    }


    /**
     * Used to stop user status update task (A frequent task).
     */
    private void stopUserStatusUpdateTask() {
        if (updateUserStatusTask != null) {
            updateUserStatusTask.stopRepeatingTask();
            updateUserStatusTask = null;
        }
    }


    /**
     * This will dispatch updates regarding trip status OR used to update current driver location to server.
     */
    protected void configUserStatus() {
        Context mContext = MyApp.getInstance().getCurrentAct();
        if (mContext == null) {
            return;
        }
        if (generalFunc.getMemberId().equalsIgnoreCase("")) {
            return;
        }

        String iTripId = "";
        String iDriverId = "";
        String DeliverAll = "";

        if (MyApp.getInstance() != null) {
              if (MyApp.getInstance().mainAct != null) {
                if (MyApp.getInstance().mainAct.isDriverAssigned && MyApp.getInstance().mainAct.driverAssignedData == null) {
                    return;
                }
                if (!MyApp.getInstance().mainAct.isDriverAssigned) {
                    iDriverId = MyApp.getInstance().mainAct.getAvailableDriverIds();
                    iTripId = "";
                    DeliverAll = "";
                } else {
                    DeliverAll = "";
                    iTripId = MyApp.getInstance().mainAct.assignedTripId;
                    iDriverId = MyApp.getInstance().mainAct.assignedDriverId;
                }
            }
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "configPassengerTripStatus");
        parameters.put("iTripId", iTripId);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.userType);
        // parameters.put("apptype", getMemberId());


        if (userLoc != null) {

            parameters.put("vLatitude", "" + userLoc.getLatitude());
            parameters.put("vLongitude", "" + userLoc.getLongitude());
        }

        if (Utils.checkText(iTripId)) {
            parameters.put("CurrentDriverIds", "" + iDriverId);
        } else if (MyApp.getInstance().mainAct != null) {
            if (MyApp.getInstance().mainAct.getAvailableDriverIds() != null) {
                parameters.put("CurrentDriverIds", "" + (MyApp.getInstance().mainAct.getAvailableDriverIds()));
            }
        }

        if (this.currentExeTask != null) {
            this.currentExeTask.cancel(true);
            this.currentExeTask = null;
            Utils.runGC();
        }


        String finalITripId = iTripId;
        String finalDeliverAll = DeliverAll;
        this.currentExeTask = ApiHandler.execute(mContext, parameters, responseString -> {
            if (responseString != null && Utils.checkText(responseString)) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    if (isFirstCall) {
                        isFirstCall = false;
                        return;
                    }

                    if (!finalITripId.isEmpty()) {
                        String message_str = generalFunc.getJsonValue(Utils.message_str, responseString);

                        dispatchMsg(message_str);
                    } else {
                        JSONArray message_arr = generalFunc.getJsonArray(Utils.message_str, responseString);
                        if (message_arr != null) {
                            for (int i = 0; i < message_arr.length(); i++) {
                                JSONObject obj_tmp = generalFunc.getJsonObject(message_arr, i);
                                dispatchMsg(obj_tmp.toString());
                            }
                        } else {
                            String message_str = generalFunc.getJsonValue(Utils.message_str, responseString);
                            dispatchMsg(message_str);
                        }
                    }

                    JSONArray currentDrivers = generalFunc.getJsonArray("currentDrivers", responseString);
                    if (currentDrivers != null && currentDrivers.length() > 0) {

                        if (generalFunc.retrieveValue(Utils.PUBSUB_TECHNIQUE).equalsIgnoreCase("None")) {
                            for (int i = 0; i < currentDrivers.length(); i++) {

                                try {
                                    String data_temp = currentDrivers.get(i).toString();
                                    JSONObject obj = new JSONObject();
                                    obj.put("MsgType", Utils.checkText(finalITripId) ? "LocationUpdateOnTrip" : "LocationUpdate");
                                    obj.put("iDriverId", generalFunc.getJsonValue("iDriverId", data_temp));
                                    obj.put("vLatitude", generalFunc.getJsonValue("vLatitude", data_temp));
                                    obj.put("vLongitude", generalFunc.getJsonValue("vLongitude", data_temp));
                                    obj.put("ChannelName", Utils.pubNub_Update_Loc_Channel_Prefix + generalFunc.getJsonValue("iDriverId", data_temp));
                                    obj.put("LocTime", System.currentTimeMillis() + "");
                                    obj.put("eSystem", finalDeliverAll);

                                    dispatchMsg(obj.toString());

                                } catch (Exception e) {

                                }

                            }
                        }

                    }

                } else {
                    String message_str = generalFunc.getJsonValue(Utils.message_str, responseString);

                    if (message_str.equalsIgnoreCase("SESSION_OUT")) {
                        forceDestroy();

                        if (MyApp.getInstance().getCurrentAct() != null && !MyApp.getInstance().getCurrentAct().isFinishing()) {
                            MyApp.getInstance().notifySessionTimeOut();
                        }
                    }
                }
            }
        });


    }

    private void dispatchMsg(String message) {
        if (!isDispatchThreadLocked) {
            isDispatchThreadLocked = true;

            Logger.d("SocketApp", "CalledFrom:ConfigSCConnection:" + message);

            if (listOfCurrentTripMsg_Map.get(message) == null) {
                Logger.d("SocketApp", "1:CalledFrom:ConfigSCConnection");
                listOfCurrentTripMsg_Map.put(message, "Yes");
                (new FireTripStatusMsg()).fireTripMsg(message);
            }

            isDispatchThreadLocked = false;
        }
    }

    @Override
    public void onLocationUpdate(Location location) {

        this.userLoc = location;
    }

    @Override
    public void onTaskRun() {

        configUserStatus();
    }

    public void executeTaskRun() {

        if (generalFunc == null) {
            //callToJobFinish();
            return;
        }

        String LAST_CALLED_MILLI = generalFunc.retrieveValue("CONFIG_DRIVER_TRIP_STATUS_CALLED");
        if (LAST_CALLED_MILLI.equalsIgnoreCase("") || ((System.currentTimeMillis() - GeneralFunctions.parseLongValue(0, LAST_CALLED_MILLI)) >= (FETCH_TRIP_STATUS_TIME_INTERVAL * 1000))) {
            onTaskRun();
        } else {
            //  callToJobFinish();
        }
    }


}
