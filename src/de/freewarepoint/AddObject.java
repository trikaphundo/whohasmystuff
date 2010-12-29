package de.freewarepoint;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddObject extends Activity {

    private Long mRowId;

    private EditText mTypeText;
    private EditText mDescriptionText;

    private TextView mDateDisplay;
    private int mYear;
    private int mMonth;
    private int mDay;

    static final String ACTION_TYPE = "action_type";
    static final int ACTION_ADD = 0;
    static final int ACTION_EDIT = 1;

    private static final int DATE_DIALOG_ID = 0;

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }
            };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add);
        setTitle(R.string.add_title);

        mTypeText = (EditText) findViewById(R.id.add_type);
        mDescriptionText = (EditText) findViewById(R.id.add_description);
        Button addButton = (Button) findViewById(R.id.add_button);

        Bundle bundle = getIntent().getExtras();

        Date date;

        if (bundle != null) {
            if (bundle.getInt(ACTION_TYPE) == ACTION_EDIT) {
                setTitle(R.string.edit_title);
                addButton.setText(R.string.edit_button);
            }

            mRowId = bundle.getLong(OpenLendDbAdapter.KEY_ROWID);
            mTypeText.setText(bundle.getString(OpenLendDbAdapter.KEY_TYPE));
            mDescriptionText.setText(bundle.getString(OpenLendDbAdapter.KEY_DESCRIPTION));
            date = new Date(bundle.getLong(OpenLendDbAdapter.KEY_DATE));
        } else {
            date = new Date();
        }

        initializeDatePicker(date);

        addButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Bundle bundle = new Bundle();

                if (mRowId != null) {
                    bundle.putLong(OpenLendDbAdapter.KEY_ROWID, mRowId);
                }

                bundle.putString(OpenLendDbAdapter.KEY_TYPE, mTypeText.getText().toString());
                bundle.putString(OpenLendDbAdapter.KEY_DESCRIPTION, mDescriptionText.getText().toString());

                Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                bundle.putLong(OpenLendDbAdapter.KEY_DATE, c.getTime().getTime());

                Intent mIntent = new Intent();
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);
                finish();

            }
        });
    }

    private void initializeDatePicker(Date date) {
        // capture our View elements
        mDateDisplay = (TextView) findViewById(R.id.add_date);
        Button mPickDate = (Button) findViewById(R.id.pickDate);

        // add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        // get the current date
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // display the current date (this method is below)
        updateDisplay();
    }

    private void updateDisplay() {
        Calendar c = Calendar.getInstance();
        c.set(mYear, mMonth, mDay);

        final DateFormat df = android.text.format.DateFormat.getDateFormat(getApplicationContext());

        mDateDisplay.setText(df.format(c.getTime()));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }



}