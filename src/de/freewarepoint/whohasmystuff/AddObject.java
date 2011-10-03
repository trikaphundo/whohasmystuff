package de.freewarepoint.whohasmystuff;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.*;
import de.freewarepoint.whohasmystuff.database.OpenLendDbAdapter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddObject extends Activity {

    private Long mRowId;

    private Button mPickDate;
    private EditText mDescriptionText;
    private EditText mPersonName;

	private String originalName;
	private String originalPersonKey;
	private String selectedPersonKey;

    private int mYear;
    private int mMonth;
    private int mDay;

    static final String ACTION_TYPE = "action_type";
    static final int ACTION_ADD = 0;
    static final int ACTION_EDIT_LENT = 1;
	static final int ACTION_EDIT_RETURNED = 2;
    static final int ACTION_SELECT_PERSON = 3;

    private static final int DATE_DIALOG_ID = 0;

	private OpenLendDbAdapter mDbHelper;

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

        mDescriptionText = (EditText) findViewById(R.id.add_description);
        mPersonName = (EditText) findViewById(R.id.personName);
        Button addButton = (Button) findViewById(R.id.add_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);
		Button deleteButton = (Button) findViewById(R.id.delete_button);
		Button returnedButton = (Button) findViewById(R.id.returned_button);

        mDbHelper = new OpenLendDbAdapter(this);
        mDbHelper.open();

        Bundle bundle = getIntent().getExtras();

        Date date;

		if (bundle.getInt(ACTION_TYPE) == ACTION_ADD) {
			returnedButton.setVisibility(View.GONE);
		}

        if (bundle.containsKey(OpenLendDbAdapter.KEY_ROWID)) {
			int actionType = bundle.getInt(ACTION_TYPE);
            if (actionType == ACTION_EDIT_LENT || actionType == ACTION_EDIT_RETURNED) {
                setTitle(R.string.edit_title);
                addButton.setText(R.string.edit_button);
				cancelButton.setVisibility(View.GONE);
            }

			if (actionType == ACTION_EDIT_LENT) {
				deleteButton.setVisibility(View.GONE);
			}
			else if (actionType == ACTION_EDIT_RETURNED) {
				returnedButton.setVisibility(View.GONE);
			}

            mRowId = bundle.getLong(OpenLendDbAdapter.KEY_ROWID);

            mDescriptionText.setText(bundle.getString(OpenLendDbAdapter.KEY_DESCRIPTION));
            mPersonName.setText(bundle.getString(OpenLendDbAdapter.KEY_PERSON));
			originalName = bundle.getString(OpenLendDbAdapter.KEY_PERSON);
			originalPersonKey = bundle.getString(OpenLendDbAdapter.KEY_PERSON_KEY);
            date = new Date(bundle.getLong(OpenLendDbAdapter.KEY_DATE));
        } else {
            date = new Date();
			deleteButton.setVisibility(View.GONE);
        }

        initializeDatePicker(date);

        ImageButton selectPerson = (ImageButton) findViewById(R.id.choosePerson);

        selectPerson.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, ACTION_SELECT_PERSON);
            }
        });


        addButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Bundle bundle = new Bundle();

                if (mRowId != null) {
                    bundle.putLong(OpenLendDbAdapter.KEY_ROWID, mRowId);
                }

                bundle.putString(OpenLendDbAdapter.KEY_DESCRIPTION, mDescriptionText.getText().toString());

                Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                bundle.putLong(OpenLendDbAdapter.KEY_DATE, c.getTime().getTime());

                bundle.putString(OpenLendDbAdapter.KEY_PERSON, mPersonName.getText().toString());

				if (mPersonName.getText().toString().equals(originalName) && selectedPersonKey == null) {
					bundle.putString(OpenLendDbAdapter.KEY_PERSON_KEY, originalPersonKey);
				}
				else {
					bundle.putString(OpenLendDbAdapter.KEY_PERSON_KEY, selectedPersonKey);
				}

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

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
				Intent mIntent = new Intent();
				mIntent.putExtra(OpenLendDbAdapter.KEY_ROWID, mRowId);
                setResult(ListLentObjects.RESULT_DELETE, mIntent);
                finish();
            }
        });

		returnedButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent mIntent = new Intent();
				mIntent.putExtra(OpenLendDbAdapter.KEY_ROWID, mRowId);
				setResult(ListLentObjects.RESULT_RETURNED, mIntent);
				finish();
			}
		});
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
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

    @Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
			case (ACTION_SELECT_PERSON):
				if (resultCode == Activity.RESULT_OK) {
					Uri contactData = data.getData();
					Cursor c = managedQuery(contactData, null, null, null, null);
					if (c.moveToFirst()) {
						String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
						mPersonName.setText(name);
						selectedPersonKey = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY));
					}
				}
				break;
		}
	}



}