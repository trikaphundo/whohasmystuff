package de.freewarepoint;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddObject extends Activity {

    private Long mRowId;

    private Button mPickDate;
    private Spinner mTypeSpinner;
    private EditText mDescriptionText;

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

        setContentView(R.layout.add_object);
        setTitle(R.string.add_title);

        mTypeSpinner = (Spinner) findViewById(R.id.type_spinner);
        mDescriptionText = (EditText) findViewById(R.id.add_description);
        Button addButton = (Button) findViewById(R.id.add_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);

        OpenLendDbAdapter mDbHelper = new OpenLendDbAdapter(this);
        mDbHelper.open();
        Cursor mLentObjectCursor = mDbHelper.fetchAllLentTypes();
        startManagingCursor(mLentObjectCursor);

        String[] from = new String[]{
                OpenLendDbAdapter.KEY_TYPE,
        };

        int[] to = new int[]{
                R.id.typename,
        };

        SimpleCursorAdapter lentObjects = new SimpleCursorAdapter(this, R.layout.type_row, mLentObjectCursor, from, to);

        mTypeSpinner.setAdapter(lentObjects);

        Bundle bundle = getIntent().getExtras();

        Date date;

        if (bundle.containsKey(OpenLendDbAdapter.KEY_ROWID)) {
            if (bundle.getInt(ACTION_TYPE) == ACTION_EDIT) {
                setTitle(R.string.edit_title);
                addButton.setText(R.string.edit_button);
            }

            int currentlySelected = -1;

            // Find selected type
            // FIXME Does not work when type was deleted
            for (int i = 0; i < lentObjects.getCount(); i++) {
                Cursor c = (Cursor) lentObjects.getItem(i);
                if (c.getString(c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_TYPE))
                        .equals(bundle.getString(OpenLendDbAdapter.KEY_TYPE))) {
                    currentlySelected = i;
                    break;
                }
            }

            mRowId = bundle.getLong(OpenLendDbAdapter.KEY_ROWID);

            mTypeSpinner.setSelection(currentlySelected);
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

                Cursor cursor = (Cursor) mTypeSpinner.getSelectedItem();
                String type = cursor.getString(cursor.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_TYPE));
                bundle.putString(OpenLendDbAdapter.KEY_TYPE, type);
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

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void initializeDatePicker(Date date) {
        // capture our View elements
        mPickDate = (Button) findViewById(R.id.pickDate);

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

        mPickDate = (Button) findViewById(R.id.pickDate);

        Calendar c = Calendar.getInstance();
        c.set(mYear, mMonth, mDay);

        final DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

        mPickDate.setText(df.format(c.getTime()));
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