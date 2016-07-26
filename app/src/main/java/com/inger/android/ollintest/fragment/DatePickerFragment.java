package com.inger.android.ollintest.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import com.inger.android.ollintest.R;

import java.util.Calendar;

/**
 * Created by netzahdzc on 7/23/16.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
//        return new DatePickerDialog(getActivity(), (EditSessionActivity)getActivity(), year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        ((TextView) getActivity().findViewById(R.id.date_field)).setTextColor(getResources().getColor(R.color.black));
        ((TextView) getActivity().findViewById(R.id.date_field)).setText(day+"/" + (month+1) + "/" + year);
    }
}
