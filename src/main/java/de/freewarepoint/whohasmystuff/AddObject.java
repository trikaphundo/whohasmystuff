package de.freewarepoint.whohasmystuff;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.*;
import de.freewarepoint.whohasmystuff.database.OpenLendDbAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AddObject extends Activity {

    private Long mRowId;

    private Button mPickDate;
    private Button mPickReturnDate;
    private Button mAddButton;
    private Button mCancelButton;
    private Button mDeleteButton;
    private Button mReturnedButton;
    private AutoCompleteTextView mDescriptionText;
    private AutoCompleteTextView mPersonName;
    private Spinner mTypeSpinner;
    private TextView mModificationDate;

	private String originalName;
	private String originalPersonKey;
	private String selectedPersonKey;

    private int mYear;
    private int mMonth;
    private int mDay;
    
    private int mReturnYear;
    private int mReturnMonth;
    private int mReturnDay;

    private Date selectedDate;

    static final String ADD_CALENDAR_ENTRY = "add_calendar_entry";
    static final String ACTION_TYPE = "action_type";
    static final String RETURN_DATE = "return_date";
    static final int ACTION_ADD = 0;
    static final int ACTION_EDIT_LENT = 1;
	static final int ACTION_EDIT_RETURNED = 2;
    static final int ACTION_SELECT_PERSON = 3;
    static final int REQUEST_READ_CONTACTS = 1024;

    private boolean addCalendarEntry;

	private OpenLendDbAdapter mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_object);
        setTitle(R.string.add_title);

        mDescriptionText = (AutoCompleteTextView) findViewById(R.id.add_description);
        mTypeSpinner = (Spinner) findViewById(R.id.type_spinner);
        mPersonName = (AutoCompleteTextView) findViewById(R.id.personName);
        mAddButton = (Button) findViewById(R.id.add_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
		mDeleteButton = (Button) findViewById(R.id.delete_button);
		mReturnedButton = (Button) findViewById(R.id.returned_button);
        mPickDate = (Button) findViewById(R.id.pickDate);
        mPickReturnDate = (Button) findViewById(R.id.returnDate);
        mModificationDate = (TextView) findViewById(R.id.modification_date_text);

        CheckBox mAddCalendarEntryCheckbox = (CheckBox) findViewById(R.id.add_calendar_checkbox);
        ImageButton selectPerson = (ImageButton) findViewById(R.id.choosePerson);

        mDbHelper = OpenLendDbAdapter.getInstance(this);
        mDbHelper.open();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(adapter);

        Bundle bundle = getIntent().getExtras();

		if (bundle.getInt(ACTION_TYPE) == ACTION_ADD) {
			mReturnedButton.setVisibility(View.GONE);
		}

        if (bundle.containsKey(OpenLendDbAdapter.KEY_ROWID)) {
            initalizeValuesFromBundle(bundle);
            mAddCalendarEntryCheckbox.setVisibility(View.GONE);
        } else {
            selectedDate = new Date();
			mDeleteButton.setVisibility(View.GONE);
            mModificationDate.setVisibility(View.GONE);
        }

        initializeDatePicker(selectedDate);
        Date returnDate = new Date(selectedDate.getTime() + 14 * DateUtils.DAY_IN_MILLIS);
        initializeReturnDatePicker(returnDate);

        selectPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (AddObject.this.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        AddObject.this.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        return;
                    }
                }

                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                AddObject.this.startActivityForResult(intent, ACTION_SELECT_PERSON);
            }
        });

        //android will restore later the state of this checkbox. Which produces a call to
        // the method CompoundButton#setChecked(boolean), which implicitly notifies its observer (listener)
        mAddCalendarEntryCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                addCalendarEntry = isChecked;
                if (isChecked) {
                    mPickReturnDate.setVisibility(View.VISIBLE);
                    AddObject.this.findViewById(R.id.return_date_text).setVisibility(View.VISIBLE);
                } else {
                    mPickReturnDate.setVisibility(View.GONE);
                    AddObject.this.findViewById(R.id.return_date_text).setVisibility(View.GONE);
                }
            }
        });

        mDescriptionText.setAdapter(descriptionsFromOtherItemsAdapter());
        mPersonName.setAdapter(contactNameAdapter());

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle addItemBundle = new Bundle();

                if (mRowId != null) {
                    addItemBundle.putLong(OpenLendDbAdapter.KEY_ROWID, mRowId);
                }

                addItemBundle.putString(OpenLendDbAdapter.KEY_DESCRIPTION, mDescriptionText.getText().toString());
                addItemBundle.putInt(OpenLendDbAdapter.KEY_TYPE, mTypeSpinner.getSelectedItemPosition());

                Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                addItemBundle.putLong(OpenLendDbAdapter.KEY_DATE, c.getTime().getTime());

                addItemBundle.putString(OpenLendDbAdapter.KEY_PERSON, mPersonName.getText().toString());

                if (mPersonName.getText().toString().equals(originalName) && selectedPersonKey == null) {
                    addItemBundle.putString(OpenLendDbAdapter.KEY_PERSON_KEY, originalPersonKey);
                } else {
                    addItemBundle.putString(OpenLendDbAdapter.KEY_PERSON_KEY, selectedPersonKey);
                }

                if (addCalendarEntry) {
                    addItemBundle.putBoolean(ADD_CALENDAR_ENTRY, true);
                    c.set(mReturnYear, mReturnMonth, mReturnDay);
                    addItemBundle.putLong(RETURN_DATE, c.getTime().getTime());
                } else {
                    addItemBundle.putBoolean(ADD_CALENDAR_ENTRY, false);
                }

                Intent mIntent = new Intent();
                mIntent.putExtras(addItemBundle);
                AddObject.this.setResult(RESULT_OK, mIntent);
                AddObject.this.finish();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddObject.this.setResult(RESULT_CANCELED);
                AddObject.this.finish();
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent();
                mIntent.putExtra(OpenLendDbAdapter.KEY_ROWID, mRowId);
                AddObject.this.setResult(ListLentObjects.RESULT_DELETE, mIntent);
                AddObject.this.finish();
            }
        });

        mReturnedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent();
                mIntent.putExtra(OpenLendDbAdapter.KEY_ROWID, mRowId);
                AddObject.this.setResult(ListLentObjects.RESULT_RETURNED, mIntent);
                AddObject.this.finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, ACTION_SELECT_PERSON);
                }
            }
        }

    }

    private void initalizeValuesFromBundle(Bundle bundle) {
        int actionType = bundle.getInt(ACTION_TYPE);
        if (actionType == ACTION_EDIT_LENT || actionType == ACTION_EDIT_RETURNED) {
            setTitle(R.string.edit_title);
            mCancelButton.setVisibility(View.GONE);
        }

        if (actionType == ACTION_EDIT_LENT) {
            mDeleteButton.setVisibility(View.GONE);
            mAddButton.setText(R.string.edit_button);
        }
        else if (actionType == ACTION_EDIT_RETURNED) {
            mReturnedButton.setVisibility(View.GONE);
            mAddButton.setText(R.string.mark_as_lent_button);
        }

        mRowId = bundle.getLong(OpenLendDbAdapter.KEY_ROWID);

        mDescriptionText.setText(bundle.getString(OpenLendDbAdapter.KEY_DESCRIPTION));
        mTypeSpinner.setSelection(bundle.getInt(OpenLendDbAdapter.KEY_TYPE));
        mPersonName.setText(bundle.getString(OpenLendDbAdapter.KEY_PERSON));
        originalName = bundle.getString(OpenLendDbAdapter.KEY_PERSON);
        originalPersonKey = bundle.getString(OpenLendDbAdapter.KEY_PERSON_KEY);
        selectedDate = new Date(bundle.getLong(OpenLendDbAdapter.KEY_DATE));
        final DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        Date modificationDate = new Date(bundle.getLong(OpenLendDbAdapter.KEY_MODIFICATION_DATE));
        mModificationDate.setText(getString(R.string.last_modified) + ": " + df.format(modificationDate));
    }

    private ArrayAdapter<String> descriptionsFromOtherItemsAdapter() {
        List<String> descriptions = new ArrayList<>();

        Cursor returnedItems = mDbHelper.fetchAllObjects();
        int columnIndex = returnedItems.getColumnIndex(OpenLendDbAdapter.KEY_DESCRIPTION);
        while (returnedItems.moveToNext()) {
            final String description = returnedItems.getString(columnIndex).trim();
            if (!descriptions.contains(description)) {
                descriptions.add(description);
            }
        }
        returnedItems.close();

        Collections.sort(descriptions);

        return new ArrayAdapter<>(getApplicationContext(), R.layout.autocomplete_select, R.id.tv_autocomplete, descriptions);
    }

    private ArrayAdapter<String> contactNameAdapter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                // We do not have permission to read contacts, do not provide autocomplete yet
                return new ArrayAdapter<>(getApplicationContext(), R.layout.autocomplete_select, R.id.tv_autocomplete, Collections.<String>emptyList());
            }
        }

        List<String> names = new ArrayList<>();

        // Add names from address book
        Cursor contactsCursor =
                getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (contactsCursor != null) {
            int columnIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            while (contactsCursor.moveToNext()) {
                final String name = contactsCursor.getString(columnIndex);
                if (name != null && !names.contains(name.trim())) {
                    names.add(name.trim());
                }
            }
            contactsCursor.close();
        }

        // Add names from history and current items
        Cursor allItems = mDbHelper.fetchAllObjects();
        int columnIndex = allItems.getColumnIndex(OpenLendDbAdapter.KEY_PERSON);
        while (allItems.moveToNext()) {
            final String name = allItems.getString(columnIndex).trim();
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        allItems.close();

        Collections.sort(names);

        return new ArrayAdapter<>(getApplicationContext(), R.layout.autocomplete_select, R.id.tv_autocomplete, names);
    }

    @Override
	public void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
	}

    private void initializeDatePicker(final Date date) {
        mPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = AddObject.this.getFragmentManager();
                Bundle bundle = new Bundle();
                bundle.putInt("year", mYear);
                bundle.putInt("month", mMonth);
                bundle.putInt("day", mDay);
                DatePickerFragment pickDateDialog = new DatePickerFragment();
                pickDateDialog.setArguments(bundle);
                pickDateDialog.show(fm, "fragment_pick_date");
            }
        });

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        updateDisplay();
    }

    private void initializeReturnDatePicker(final Date date) {
        mPickReturnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                c.set(mReturnYear, mReturnMonth, mReturnDay);

                FragmentManager fm = AddObject.this.getFragmentManager();
                Bundle bundle = new Bundle();
                bundle.putInt("year", mReturnYear);
                bundle.putInt("month", mReturnMonth);
                bundle.putInt("day", mReturnDay);
                DatePickerFragment pickDateDialog = new DatePickerFragment();
                pickDateDialog.setArguments(bundle);
                pickDateDialog.show(fm, "fragment_pick_return_date");
            }
        });

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        mReturnYear = c.get(Calendar.YEAR);
        mReturnMonth = c.get(Calendar.MONTH);
        mReturnDay = c.get(Calendar.DAY_OF_MONTH);

        updateDisplay();
    }

    private void updateDisplay() {
        final DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        Calendar c = Calendar.getInstance();
        
        c.set(mYear, mMonth, mDay);
        mPickDate.setText(df.format(c.getTime()));
        
        c.set(mReturnYear, mReturnMonth, mReturnDay);
        mPickReturnDate.setText(df.format(c.getTime()));
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

    void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mYear = year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;

        updateDisplay();
    }

    void updateReturnDate(int year, int monthOfYear, int dayOfMonth) {
        mReturnYear = year;
        mReturnMonth = monthOfYear;
        mReturnDay = dayOfMonth;

        updateDisplay();
    }
}
