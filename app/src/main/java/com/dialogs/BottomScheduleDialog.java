package com.dialogs;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.datepicker.DatePickerTimeline;
import com.datepicker.OnDateSelectedListener;
import com.general.files.GeneralFunctions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.utils.Utils;
import com.view.MTextView;
import com.zphr.user.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BottomScheduleDialog {
    BottomSheetDialog hotoUseDialog;
    GeneralFunctions generalFunc;
    Context actContext;
    public JSONObject obj_userProfile;

    Calendar tripDateTime = Calendar.getInstance();
    OndateSelectionListener onDateSelectedListener;

    public BottomScheduleDialog(Context actContext, OndateSelectionListener onDateSelectedListener) {
        this.actContext = actContext;
        generalFunc = new GeneralFunctions(actContext);
        this.onDateSelectedListener = onDateSelectedListener;
        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
    }

    public BottomScheduleDialog() {
        super();
    }

    public void showPreferenceDialog(String title, String posBtn, String negBtn, String id,
                                     Boolean isReselectDate, Calendar cal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(actContext);

        View dialogView = View.inflate(actContext, R.layout.design_bottom_schedule, null);
        builder.setView(dialogView);

        MTextView titleTxt = dialogView.findViewById(R.id.titleTxt);
        MTextView selectedDate = dialogView.findViewById(R.id.selectedDate);
        MTextView selectedTime = dialogView.findViewById(R.id.selectedTime);
        RecyclerView calendar_recycler_view = dialogView.findViewById(R.id.calendar_recycler_view);
        DatePickerTimeline datePickerTimeline = dialogView.findViewById(R.id.dateTimeline);
        MTextView skipTxtArea = dialogView.findViewById(R.id.skipTxtArea);
        MTextView okTxt = dialogView.findViewById(R.id.okTxt);
        MTextView hourselectotTxt = dialogView.findViewById(R.id.hourselectotTxt);
        MTextView minselectotTxt = dialogView.findViewById(R.id.minselectotTxt);
        MTextView AmpmselectotTxt = dialogView.findViewById(R.id.AmpmselectotTxt);

        Calendar currentCal = Calendar.getInstance(Locale.getDefault());
        int minMinute;
        minMinute = (Integer.parseInt(generalFunc.getJsonValueStr("MINIMUM_HOURS_LATER_BOOKING", obj_userProfile))) + 1;
        currentCal.add(Calendar.MINUTE, minMinute);


        SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd", Locale.ENGLISH);
        SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat hourSdf = new SimpleDateFormat("hh");
        SimpleDateFormat minSdf = new SimpleDateFormat("mm");
        SimpleDateFormat ampmsdf = new SimpleDateFormat("a");


        if (!title.equalsIgnoreCase("")) {
            titleTxt.setText(title);
            titleTxt.setVisibility(View.VISIBLE);
        } else {
            titleTxt.setVisibility(View.GONE);
        }

        if (!posBtn.equals("")) {
            okTxt.setText(posBtn);
            okTxt.setVisibility(View.VISIBLE);
        } else {
            okTxt.setVisibility(View.GONE);
        }

        if (!negBtn.equals("")) {
            skipTxtArea.setText(negBtn);
            skipTxtArea.setVisibility(View.VISIBLE);
        } else {
            skipTxtArea.setVisibility(View.INVISIBLE);
        }

        datePickerTimeline.setInitialDate(currentCal.get(Calendar.YEAR), currentCal.get(Calendar.MONTH), currentCal.get(Calendar.DAY_OF_MONTH));

        if (isReselectDate) {
            tripDateTime = cal;
            selectedDate.setText(sdf.format(tripDateTime.getTime()));
            selectedTime.setText(timeSdf.format(tripDateTime.getTime()));
            hourselectotTxt.setText(hourSdf.format(tripDateTime.getTime()));
            minselectotTxt.setText(minSdf.format(tripDateTime.getTime()));
            AmpmselectotTxt.setText(ampmsdf.format(tripDateTime.getTime()));

            datePickerTimeline.setActiveDateWithScroll(tripDateTime);
        } else {
            tripDateTime = currentCal;
            selectedDate.setText(sdf.format(currentCal.getTime()));
            selectedTime.setText(timeSdf.format(currentCal.getTime()));
            hourselectotTxt.setText(hourSdf.format(currentCal.getTime()));
            minselectotTxt.setText(minSdf.format(currentCal.getTime()));
            AmpmselectotTxt.setText(ampmsdf.format(currentCal.getTime()));
        }

        OnDateSelectedListener OnDateSelectedListener = new OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day, int dayOfWeek, int weekOfMonth) {

                tripDateTime.set(Calendar.YEAR, year);
                tripDateTime.set(Calendar.MONTH, month);
                tripDateTime.set(Calendar.DAY_OF_MONTH, day);
                tripDateTime.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                tripDateTime.set(Calendar.WEEK_OF_MONTH, weekOfMonth);

                selectedDate.setText(sdf.format(tripDateTime.getTime()));
                Log.d("DateSelected", "Date:" + tripDateTime.getTime());

            }

            @Override
            public void onDisabledDateSelected(int year, int month, int day, int dayOfWeek, boolean isDisabled) {
                // Do Something
            }
        };

        datePickerTimeline.setOnDateSelectedListener(OnDateSelectedListener);
        View.OnClickListener timePickerListener = v -> {

            int hour = tripDateTime.get(Calendar.HOUR_OF_DAY);
            int minute = tripDateTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(actContext, R.style.calender_dialog_theme, (timePicker, selectedHour, selectedMinute) -> {

                tripDateTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                tripDateTime.set(Calendar.MINUTE, selectedMinute);
                tripDateTime.set(Calendar.SECOND, 0);
                tripDateTime.set(Calendar.MILLISECOND, 0);

                selectedTime.setText(timeSdf.format(tripDateTime.getTime()));
                hourselectotTxt.setText(hourSdf.format(tripDateTime.getTime()));
                minselectotTxt.setText(minSdf.format(tripDateTime.getTime()));
                AmpmselectotTxt.setText(ampmsdf.format(tripDateTime.getTime()));

            }, hour, minute, false);
            mTimePicker.show();
        };


        hourselectotTxt.setOnClickListener(timePickerListener);
        minselectotTxt.setOnClickListener(timePickerListener);
        AmpmselectotTxt.setOnClickListener(timePickerListener);


        okTxt.setOnClickListener(view -> {


            int minHours = Integer.parseInt(generalFunc.getJsonValueStr("MINIMUM_HOURS_LATER_BOOKING", obj_userProfile));
                int maxHours = Integer.parseInt(generalFunc.getJsonValueStr("MAXIMUM_HOURS_LATER_BOOKING", obj_userProfile));

                if (!Utils.isValidTimeSelect(tripDateTime.getTime(), TimeUnit.MINUTES.toMillis(minHours)) || Utils.isValidTimeSelect(tripDateTime.getTime(), TimeUnit.MINUTES.toMillis(maxHours))) {
                    generalFunc.showGeneralMessage("", generalFunc.getJsonValueStr("LBL_INVALID_PICKUP_MAX_NOTE_MSG", obj_userProfile));
                    return;
                }

                onDateSelectedListener.onScheduleSelection(Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", tripDateTime.getTime()), tripDateTime.getTime(), id);


            hotoUseDialog.dismiss();

        });

        skipTxtArea.setOnClickListener(view -> hotoUseDialog.dismiss());

        hotoUseDialog = new BottomSheetDialog(actContext);
        hotoUseDialog.setContentView(dialogView);
        View bottomSheetView = hotoUseDialog.getWindow().getDecorView().findViewById(R.id.design_bottom_sheet);
        bottomSheetView.setBackgroundColor(actContext.getResources().getColor(android.R.color.transparent));
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) dialogView.getParent());
        mBehavior.setPeekHeight(Utils.dpToPx(600, actContext));

        hotoUseDialog.setCancelable(false);
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(hotoUseDialog);
        }
        Animation a = AnimationUtils.loadAnimation(actContext, R.anim.bottom_up);
        a.reset();
        bottomSheetView.clearAnimation();
        bottomSheetView.startAnimation(a);
        hotoUseDialog.show();

    }

    public interface OndateSelectionListener {
        void onScheduleSelection(String selDateTime, Date date, String iCabBookingId);

    }

}
