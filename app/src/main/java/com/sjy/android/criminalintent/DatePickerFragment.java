package com.sjy.android.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {
    public static final String EXTRA_DATE = "com.sjy.android.criminalIntent.date";
    private static final String ARG_DATE = "date";
    private DatePicker mDatePicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Instant date = (Instant) getArguments().getSerializable(ARG_DATE);
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);*/
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneId.systemDefault());
        int year = ldt.getYear();
        int month = ldt.getMonthValue();
        int day = ldt.getDayOfMonth();

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);

        mDatePicker = v.findViewById(R.id.dialog_date_date_picker);
        mDatePicker.init(year, month, day, null);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();

                        Date tempDate = new GregorianCalendar(year, month, day).getTime();
                        Instant date = tempDate.toInstant();
                        sendResult(Activity.RESULT_OK, date);
                    }
                })
                .create();
    }

    public static DatePickerFragment newInstance(Instant date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void sendResult(int resultCode, Instant date) {
        if(getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);

    }
}
