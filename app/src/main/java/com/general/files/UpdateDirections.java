package com.general.files;

import android.content.Context;
import android.location.Location;

import com.zphr.user.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.service.handler.AppService;
import com.service.model.DataProvider;
import com.utils.Logger;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Admin on 02-08-2017.
 */

//public class UpdateDirections implements GetLocationUpdates.LocationUpdates, RecurringTask.OnTaskRunCalled {
public class UpdateDirections implements RecurringTask.OnTaskRunCalled {

    public GoogleMap googleMap;
    public Location destinationLocation;
    public Context mcontext;
    public Location userLocation;

    GeneralFunctions generalFunctions;

    String serverKey;
    Polyline route_polyLine;

    RecurringTask updateFreqTask;

    String gMapLngCode = "en";
    String userProfileJson = "";
    String eUnit = "KMs";
    int DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = 3;

    public UpdateDirections(Context mcontext, GoogleMap googleMap, Location userLocation, Location destinationLocation) {
        this.googleMap = googleMap;
        this.destinationLocation = destinationLocation;
        this.mcontext = mcontext;
        this.userLocation = userLocation;

        generalFunctions = MyApp.getInstance().getGeneralFun(mcontext);

        serverKey = generalFunctions.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY);

        gMapLngCode = generalFunctions.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY);

        userProfileJson = generalFunctions.retrieveValue(Utils.USER_PROFILE_JSON);
        eUnit = generalFunctions.getJsonValue("eUnit", userProfileJson);
        DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = generalFunctions.parseIntegerValue(3, generalFunctions.getJsonValue("DRIVER_ARRIVED_MIN_TIME_PER_MINUTE", userProfileJson));
    }

    public void scheduleDirectionUpdate() {


        String DESTINATION_UPDATE_TIME_INTERVAL = generalFunctions.retrieveValue("DESTINATION_UPDATE_TIME_INTERVAL");
        updateFreqTask = new RecurringTask((int) (generalFunctions.parseDoubleValue(2, DESTINATION_UPDATE_TIME_INTERVAL) * 60 * 1000));
        updateFreqTask.setTaskRunListener(this);
        updateFreqTask.startRepeatingTask();

    }

    public void releaseTask() {
        Logger.d("Task", ":: releaseTask called");
        if (updateFreqTask != null) {
            updateFreqTask.stopRepeatingTask();
            updateFreqTask = null;
        }

        // Utils.runGC();
    }


    public void updateDirections() {

        if (userLocation == null || destinationLocation == null) {
            return;
        }


        if (userProfileJson != null && !generalFunctions.getJsonValue("ENABLE_DIRECTION_SOURCE_DESTINATION_USER_APP", userProfileJson).equalsIgnoreCase("Yes")) {


            double distance = Utils.CalculationByLocation(userLocation.getLatitude(), userLocation.getLongitude(), destinationLocation.getLatitude(), destinationLocation.getLongitude(), "");

            if (userProfileJson != null && !generalFunctions.getJsonValue("eUnit", userProfileJson).equalsIgnoreCase("KMs")) {
                distance = distance * 0.621371;
            } else {
                distance = distance * 0.99999969062399994;
            }

            distance = generalFunctions.round(distance, 2);
            int time = ((int) (distance * DRIVER_ARRIVED_MIN_TIME_PER_MINUTE));

            if (time < 1) {
                time = 1;
            }

            return;
        }


        String originLoc = userLocation.getLatitude() + "," + userLocation.getLongitude();
        String destLoc = destinationLocation.getLatitude() + "," + destinationLocation.getLongitude();

        // String directionURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey + "&language=" + gMapLngCode + "&sensor=true";

        HashMap<String, String> hashMap = new HashMap<>();


        String trip_data = generalFunctions.getJsonValue("TripDetails", userProfileJson);

        String eTollSkipped = generalFunctions.getJsonValue("eTollSkipped", trip_data);
        boolean istollSkip = false;
        if (eTollSkipped == "Yes") {
            istollSkip = true;
        }

        AppService.getInstance().executeService(mcontext, new DataProvider.DataProviderBuilder(userLocation.getLatitude() + "", userLocation.getLongitude() + "").setDestLatitude(destinationLocation.getLatitude() + "").setDestLongitude(destinationLocation.getLongitude() + "").setTollAccess(istollSkip).setWayPoints(new JSONArray()).build(), AppService.Service.DIRECTION, data -> {
            if (data.get("RESPONSE_TYPE") != null && data.get("RESPONSE_TYPE").toString().equalsIgnoreCase("FAIL")) {
                generalFunctions.showGeneralMessage("", generalFunctions.retrieveLangLBl("", "LBL_DEST_ROUTE_NOT_FOUND"));
                return;
            }
            String responseString = data.get("RESPONSE_DATA").toString();

            if (responseString != null && !responseString.equalsIgnoreCase("") && data.get("DISTANCE") == null) {

                JSONArray obj_routes = generalFunctions.getJsonArray("routes", responseString);
                if (obj_routes != null && obj_routes.length() > 0) {
                    JSONObject obj_legs = generalFunctions.getJsonObject(generalFunctions.getJsonArray("legs", generalFunctions.getJsonObject(obj_routes, 0).toString()), 0);


                    String distance = "" + generalFunctions.getJsonValue("value",
                            generalFunctions.getJsonValue("distance", obj_legs.toString()).toString());
                    String time = "" + generalFunctions.getJsonValue("value",
                            generalFunctions.getJsonValue("duration", obj_legs.toString()).toString());

                    double distance_final = generalFunctions.parseDoubleValue(0.0, distance);


                    if (userProfileJson != null && !generalFunctions.getJsonValue("eUnit", userProfileJson).equalsIgnoreCase("KMs")) {
                        distance_final = distance_final * 0.000621371;
                    } else {
                        distance_final = distance_final * 0.00099999969062399994;
                    }

                    distance_final = generalFunctions.round(distance_final, 2);

                }


                if (googleMap != null) {

                    PolylineOptions lineOptions = generalFunctions.getGoogleRouteOptions(responseString, Utils.dipToPixels(mcontext, 5), mcontext.getResources().getColor(R.color.black));

                    if (lineOptions != null) {
                        if (route_polyLine != null) {
                            route_polyLine.remove();
                        }
                        route_polyLine = googleMap.addPolyline(lineOptions);

                    }
                }


            } else {

                Logger.d("RESULTDATA", "::" + data);

                double distance_final = generalFunctions.parseDoubleValue(0.0, data.get("DISTANCE").toString());

                if (userProfileJson != null && !generalFunctions.getJsonValue("eUnit", userProfileJson).equalsIgnoreCase("KMs")) {
                    distance_final = distance_final * 0.000621371;
                } else {
                    distance_final = distance_final * 0.00099999969062399994;
                }
                distance_final = generalFunctions.round(distance_final, 2);

                String time = data.get("DURATION").toString();


                String timeToreach = "1";

                int duration = (int) Math.round((generalFunctions.parseDoubleValue(0.0,
                        time) / 60));


                timeToreach = getTimeTxt(duration);


                if (googleMap != null) {
                    Logger.d("RESULTDATA", "::routes" +  data.get("ROUTES"));
                    HashMap<String, Object> data_dict = new HashMap<>();
                    data_dict.put("routes", data.get("ROUTES"));


                    PolylineOptions lineOptions = generalFunctions.getGoogleRouteOptions(data_dict.toString(), Utils.dipToPixels(mcontext, 5), mcontext.getResources().getColor(R.color.black));

                    if (lineOptions != null) {
                        if (route_polyLine != null) {
                            route_polyLine.remove();
                        }
                        route_polyLine = googleMap.addPolyline(lineOptions);

                    }
                }

            }


        });

    }


    public String getTimeTxt(int duration) {

        if (duration < 1) {
            duration = 1;
        }
        String durationTxt = "";
        String timeToreach = duration == 0 ? "--" : "" + duration;

        timeToreach = duration >= 60 ? formatHoursAndMinutes(duration) : timeToreach;


        durationTxt = (duration < 60 ? generalFunctions.retrieveLangLBl("", "LBL_MINS_SMALL") : generalFunctions.retrieveLangLBl("", "LBL_HOUR_TXT"));

        durationTxt = duration == 1 ? generalFunctions.retrieveLangLBl("", "LBL_MIN_SMALL") : durationTxt;
        durationTxt = duration > 120 ? generalFunctions.retrieveLangLBl("", "LBL_HOURS_TXT") : durationTxt;

        Logger.d("durationTxt", "::" + durationTxt);
        return timeToreach + " " + durationTxt;
    }

    public static String formatHoursAndMinutes(int totalMinutes) {
        String minutes = Integer.toString(totalMinutes % 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;
        return (totalMinutes / 60) + ":" + minutes;
    }

    @Override
    public void onTaskRun() {
        Utils.runGC();
        updateDirections();
    }

    public void changeUserLocation(Location location) {
        if (location != null) {
            this.userLocation = location;
        }
    }


}
