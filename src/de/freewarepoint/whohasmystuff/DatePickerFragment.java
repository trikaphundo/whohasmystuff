package de.freewarepoint.whohasmystuff;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    final Calendar calendar;

    public DatePickerFragment(Calendar calendar) {
        this.calendar = calendar;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (getTag().equals("fragment_pick_date")) {
            ((AddObject)getActivity()).updateDate(year, month, day);
        }
        else if (getTag().equals("fragment_pick_return_date")) {
            ((AddObject)getActivity()).updateReturnDate(year, month, day);
        }
    }

}

