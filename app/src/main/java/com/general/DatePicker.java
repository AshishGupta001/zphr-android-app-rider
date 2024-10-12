package com.general;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.datepicker.date.DatePickerDialog;
import com.general.files.GeneralFunctions;
import com.zphr.user.R;
import com.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePicker {

    public static void show(@NonNull Context mContext, @NonNull GeneralFunctions generalFunc, @Nullable Calendar minDate, @Nullable Calendar maxDate, @Nullable String mSelectedDate, @Nullable Calendar[] highlightedDays, @Nullable OnDateSelectionListener listener) {

        Calendar mSelectedCal = Calendar.getInstance();
        if (Utils.checkText(mSelectedDate)) {
            String[] dateParts = mSelectedDate.split("-");
            int selYear = Integer.parseInt(dateParts[0]);
            int selMonth = Integer.parseInt(dateParts[1]);
            int selDay = Integer.parseInt(dateParts[2]);

            mSelectedCal.set(selYear, selMonth, selDay);
        } else {
            mSelectedCal.set(mSelectedCal.get(Calendar.YEAR), (mSelectedCal.get(Calendar.MONTH) + 1), mSelectedCal.get(Calendar.DAY_OF_MONTH));
        }

        DatePickerDialog dpd = DatePickerDialog.newInstance((view, year, monthOfYear, dayOfMonth) -> {
            if (listener != null) {
                listener.onDateSelected(year, monthOfYear, dayOfMonth);
            }
        }, mSelectedCal.get(Calendar.YEAR), mSelectedCal.get(Calendar.MONTH), mSelectedCal.get(Calendar.DAY_OF_MONTH));

        initialize(dpd, mContext, generalFunc, minDate, maxDate, highlightedDays);
        dpd.setTitle(generalFunc.retrieveLangLBl("", "LBL_SELECT_A_DATE_TXT"));

        dpd.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "DatePickerDialog");
    }

    public static void showRange(@NonNull Context mContext, @NonNull GeneralFunctions generalFunc, @Nullable Calendar minDate, @Nullable Calendar maxDate, @Nullable String mSelectedDate, @Nullable Calendar[] highlightedDays, @Nullable OnRangeSetListener listener) {

        Calendar mSelectedCal = Calendar.getInstance();
        if (Utils.checkText(mSelectedDate)) {
            String[] dateParts = mSelectedDate.split("-");
            int selYear = Integer.parseInt(dateParts[0]);
            int selMonth = Integer.parseInt(dateParts[1]);
            int selDay = Integer.parseInt(dateParts[2]);

            mSelectedCal.set(selYear, selMonth, selDay);
        } else {
            mSelectedCal.set(mSelectedCal.get(Calendar.YEAR), (mSelectedCal.get(Calendar.MONTH) + 1), mSelectedCal.get(Calendar.DAY_OF_MONTH));
        }

        DatePickerDialog dpd = DatePickerDialog.newInstance((datePickerDialog, hashMap, hashMap1) -> {
            if (listener != null) {
                listener.OnRangeSetSelected(
                        hashMap.get(DatePickerDialog.CalendarData.YEAR.value),
                        hashMap.get(DatePickerDialog.CalendarData.MONTH.value),
                        hashMap.get(DatePickerDialog.CalendarData.DAY_OF_MONTH.value),
                        hashMap1.get(DatePickerDialog.CalendarData.YEAR.value),
                        hashMap1.get(DatePickerDialog.CalendarData.MONTH.value),
                        hashMap1.get(DatePickerDialog.CalendarData.DAY_OF_MONTH.value));
            }
        }, mSelectedCal.get(Calendar.YEAR), mSelectedCal.get(Calendar.MONTH), mSelectedCal.get(Calendar.DAY_OF_MONTH));

        initialize(dpd, mContext, generalFunc, minDate, maxDate, highlightedDays);
        dpd.setTitle(generalFunc.retrieveLangLBl("", "LBL_SELECT_A_DATE_TXT"));

        dpd.setDateRangeSelector(true, generalFunc.retrieveLangLBl("", "LBL_START_DATE_TXT"), generalFunc.retrieveLangLBl("", "LBL_END_DATE_TXT"));

        dpd.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "DatePickerDialog");
    }

    private static void initialize(DatePickerDialog dpd, Context mContext, GeneralFunctions generalFunc, @Nullable Calendar minDate, @Nullable Calendar maxDate, @Nullable Calendar[] highlightedDays) {
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.setThemeDark(false);
        dpd.setScrollOrientation(DatePickerDialog.ScrollOrientation.HORIZONTAL);

        dpd.setAccentColor(mContext.getResources().getColor(R.color.appThemeColor_1));
        dpd.setLocale(new Locale(generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY)));
        dpd.setOkText(generalFunc.retrieveLangLBl("", "LBL_OK"));
        dpd.setCancelText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
        dpd.setRTLMode(generalFunc.isRTLmode());

        if (highlightedDays != null && highlightedDays.length > 0) {
            dpd.setHighlightedDots(true);
            dpd.setHighlightedDays(highlightedDays);
        }

        if (minDate != null) {
            dpd.setMinDate(minDate);
        }
        if (maxDate != null) {
            dpd.setMaxDate(maxDate);
        }
    }

    public static Calendar checkNextPreviousDay(Date mDate, Calendar registrationDate, int nextPrevious) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);

        if (nextPrevious == -1) {
            if (registrationDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                    && registrationDate.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                    && registrationDate.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)) {
                return null;
            }
        } else if (nextPrevious == 1) {
            if (Calendar.getInstance().get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                    && Calendar.getInstance().get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                    && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)) {
                return null;
            }
        }
        return cal;
    }

    public interface OnDateSelectionListener {
        void onDateSelected(int year, int monthOfYear, int dayOfMonth);
    }

    public interface OnRangeSetListener {
        void OnRangeSetSelected(int sYear, int sMonthOfYear, int sDayOfMonth, int eYear, int eMonthOfYear, int eDayOfMonth);
    }
}