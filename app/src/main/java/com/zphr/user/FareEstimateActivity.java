package com.zphr.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activity.ParentActivity;
import com.general.files.GeneralFunctions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.service.handler.ApiHandler;
import com.utils.Utils;
import com.view.MTextView;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.util.HashMap;

public class FareEstimateActivity extends ParentActivity implements OnMapReadyCallback {

    MTextView titleTxt;
    ImageView backImgView;
    MTextView searchLocTxt;
    MTextView baseFareHTxt;
    MTextView baseFareVTxt;
    MTextView commisionHTxt;
    MTextView commisionVTxt;
    MTextView distanceTxt;
    MTextView distanceFareTxt;
    MTextView minuteTxt;
    MTextView minuteFareTxt;
    MTextView totalFareHTxt;
    MTextView totalFareVTxt;
    AVLoadingIndicatorView loaderView;
    LinearLayout container;
    String currency_sign = "";
    SupportMapFragment map;
    GoogleMap gMapView;
    ImageView locPinImg;
    String ePaymentMode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_estimate);


        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);
        container = (LinearLayout) findViewById(R.id.container);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        searchLocTxt = (MTextView) findViewById(R.id.searchLocTxt);
        baseFareHTxt = (MTextView) findViewById(R.id.baseFareHTxt);
        baseFareVTxt = (MTextView) findViewById(R.id.baseFareVTxt);
        commisionHTxt = (MTextView) findViewById(R.id.commisionHTxt);
        commisionVTxt = (MTextView) findViewById(R.id.commisionVTxt);
        distanceTxt = (MTextView) findViewById(R.id.distanceTxt);
        distanceFareTxt = (MTextView) findViewById(R.id.distanceFareTxt);
        minuteTxt = (MTextView) findViewById(R.id.minuteTxt);
        minuteFareTxt = (MTextView) findViewById(R.id.minuteFareTxt);
        totalFareHTxt = (MTextView) findViewById(R.id.totalFareHTxt);
        totalFareVTxt = (MTextView) findViewById(R.id.totalFareVTxt);
        locPinImg = (ImageView) findViewById(R.id.locPinImg);

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);
        map.getMapAsync(this);


        currency_sign = generalFunc.getJsonValueStr("CurrencySymbol", obj_userProfile);
        addToClickHandler(backImgView);
        addToClickHandler(searchLocTxt);

        container.setVisibility(View.GONE);
        setLabels();

        locPinImg.setImageResource(R.mipmap.ic_search);
        ePaymentMode = getIntent().getStringExtra("ePaymentMode");
        if (getIntent().getStringExtra("isDestinationAdded").equals("true")) {
            searchLocTxt.setText(getIntent().getStringExtra("DestLocAddress"));
            findRoute(getIntent().getStringExtra("DestLocLatitude"), getIntent().getStringExtra("DestLocLongitude"));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) {
            return;
        }
        this.gMapView = googleMap;
        this.gMapView.getUiSettings().setZoomControlsEnabled(true);

    }

    public Context getActContext() {
        return FareEstimateActivity.this;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FARE_ESTIMATE_TXT"));
        baseFareHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_BASE_FARE_SMALL_TXT"));
        totalFareHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MIN_FARE_TXT"));
        searchLocTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_PLACE_HINT_TXT"));
    }

    public void findRoute(String destLocLatitude, String destLocLongitude) {

        loaderView.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);

        String originLoc = getIntent().getStringExtra("PickUpLatitude") + "," + getIntent().getStringExtra("PickUpLongitude");
        String destLoc = destLocLatitude + "," + destLocLongitude;

        HashMap<String, String> data = new HashMap<>();
        data.put(Utils.GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY, "");
        data.put(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY, "");
        data = generalFunc.retrieveValue(data);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("d_latitude", destLocLatitude + "");
        hashMap.put("d_longitude", destLocLongitude + "");
        hashMap.put("s_latitude", getIntent().getStringExtra("PickUpLatitude"));
        hashMap.put("s_longitude", getIntent().getStringExtra("PickUpLongitude"));
        String parameters = "origin=" + originLoc + "&destination=" + destLoc;
        hashMap.put("parameters", parameters);


    }

    public void estimateFare(final String distance, final String time, final String directionJSON, final LatLng sourceLocation, final LatLng destLocation) {
        loaderView.setVisibility(View.VISIBLE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "estimateFare");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("distance", distance);
        parameters.put("time", time);
        parameters.put("SelectedCar", getIntent().getStringExtra("SelectedCarId"));
        parameters.put("ePaymentMode", ePaymentMode);

        ApiHandler.execute(getActContext(), parameters, responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail == true) {

                    String total_fare = generalFunc.getJsonValueStr("total_fare", responseObj);
                    String iBaseFare = generalFunc.getJsonValueStr("iBaseFare", responseObj);
                    String fPricePerMin = generalFunc.getJsonValueStr("fPricePerMin", responseObj);
                    String fPricePerKM = generalFunc.getJsonValueStr("fPricePerKM", responseObj);
                    String fCommision = generalFunc.getJsonValueStr("fCommision", responseObj);
                    String MinFareDiff = generalFunc.getJsonValueStr("MinFareDiff", responseObj);
                    String Distance = generalFunc.getJsonValueStr("Distance", responseObj);
                    String Time = generalFunc.getJsonValueStr("Time", responseObj);

                    baseFareVTxt.setText(getIntent().getStringExtra("SelectedCabType")
                            + " " + currency_sign + " " + iBaseFare);
                    distanceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DISTANCE_TXT") + "(" + Distance + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + ")");
                    distanceFareTxt.setText(currency_sign + " " + fPricePerKM);
                    minuteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TIME_TXT") + "(" + Time + " " + generalFunc.retrieveLangLBl("", "LBL_MINUTES_TXT") + ")");
                    minuteFareTxt.setText(currency_sign + " " + fPricePerMin);
                    totalFareVTxt.setText(currency_sign + " " + total_fare);

                    if (!MinFareDiff.equals("") && !MinFareDiff.equals("0")) {

                        (findViewById(R.id.minFareRow)).setVisibility(View.VISIBLE);
                        ((MTextView) findViewById(R.id.minFareHTxt)).setText(currency_sign + "" + total_fare + " "
                                + generalFunc.retrieveLangLBl("", "LBL_MINIMUM"));
                        ((MTextView) findViewById(R.id.minFareVTxt)).setText(currency_sign + " " + total_fare);
                    }

                    locPinImg.setImageResource(R.mipmap.ic_loc_pin_indicator);

                    loaderView.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);

                    if (gMapView != null) {

                        gMapView.clear();
                        PolylineOptions lineOptions = generalFunc.getGoogleRouteOptions(directionJSON, Utils.dipToPixels(getActContext(), 5), getActContext().getResources().getColor(R.color.black));

                        if (lineOptions != null) {
                            gMapView.addPolyline(lineOptions);
                        }

                        MarkerOptions markerOptions_sourceLocation = new MarkerOptions();
                        markerOptions_sourceLocation.position(sourceLocation);
                        markerOptions_sourceLocation.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_source_marker))
                                .anchor(0.5f, 0.5f);

                        MarkerOptions markerOptions_destinationLocation = new MarkerOptions();
                        markerOptions_destinationLocation
                                .position(destLocation);
                        markerOptions_destinationLocation
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dest_marker)).anchor(0.5f, 0.5f);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(sourceLocation);
                        builder.include(destLocation);

                        LatLngBounds bounds = builder.build();

                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                                Utils.dipToPixels(getActContext(), 280), Utils.dipToPixels(getActContext(), 280), 50);
                        gMapView.moveCamera(cu);

                        gMapView.addMarker(markerOptions_sourceLocation);
                        gMapView.addMarker(markerOptions_destinationLocation);

                    }


                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                }
            } else {
                generalFunc.showError();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                searchLocTxt.setText(place.getAddress());
                LatLng placeLocation = place.getLatLng();

                findRoute("" + placeLocation.latitude, "" + placeLocation.longitude);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

                generalFunc.showMessage(generalFunc.getCurrentView(FareEstimateActivity.this),
                        status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }
        }
    }


    public void onClick(View view) {
        Utils.hideKeyboard(getActContext());
        switch (view.getId()) {
            case R.id.backImgView:
                FareEstimateActivity.super.onBackPressed();
                break;
            case R.id.searchLocTxt:
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(FareEstimateActivity.this);
                    startActivityForResult(intent, Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    generalFunc.showMessage(generalFunc.getCurrentView(FareEstimateActivity.this),
                            generalFunc.retrieveLangLBl("", "LBL_SERVICE_NOT_AVAIL_TXT"));
                }
                break;

        }
    }

}
