package de.freewarepoint.whohasmystuff;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import de.freewarepoint.whohasmystuff.database.DatabaseHelper;
import de.freewarepoint.whohasmystuff.database.OpenLendDbAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract  class AbstractListFragment extends ListFragment implements AlertDialogFragment.AlertDialogFragmentListener{

    private static final int SUBMENU_EDIT = SubMenu.FIRST;
    private static final int SUBMENU_MARK_AS_RETURNED = SubMenu.FIRST + 1;
    private static final int SUBMENU_LEND_AGAIN = SubMenu.FIRST + 2;
    private static final int SUBMENU_DELETE = SubMenu.FIRST + 3;

    public static final int ACTION_ADD = 1;
    public static final int ACTION_EDIT = 2;

    public static final int RESULT_DELETE = 2;
    public static final int RESULT_RETURNED = 3;

    public static final String LOG_TAG = "WhoHasMyStuff";
    public static final String FIRST_START = "FirstStart";

    /**Tag that identifies the DialogFragment that may be shown on the very first execution ever.*/
    private static final String TAG_DIALOG_FIRST_TIME = "dialog_first_time";

    protected OpenLendDbAdapter mDbHelper;
    private Cursor mLentObjectCursor;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getIntentTitle());

        mDbHelper = OpenLendDbAdapter.getInstance(getActivity());
        mDbHelper.open();

        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean firstStart = preferences.getBoolean(FIRST_START, true);

        // Database may not be empty after upgrade
        boolean emptyDatabase = mDbHelper.fetchAllObjects().getCount() == 0;

        if (firstStart && emptyDatabase) {
            boolean storageAccessAvailable = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    storageAccessAvailable = false;
                }
            }

            if (storageAccessAvailable && DatabaseHelper.existsBackupFile()) {
                AlertDialogFragment dialog;

                dialog = AlertDialogFragment.newObject(null,
                        getResources().getString(R.string.restore_on_first_start),
                        getResources().getString(R.string.restore_on_first_start_yes),
                        getResources().getString(R.string.restore_on_first_start_no),
                        null,
                        0);
                dialog.setAlertDialogFragmentListener(this);
                dialog.show(getFragmentManager(), TAG_DIALOG_FIRST_TIME);
            }
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(FIRST_START, false);
        editor.commit();

        fillData();

        ListView listView = getListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AbstractListFragment.this.launchEditActivity(position, id);
            }
        });

        registerForContextMenu(listView);
        setHasOptionsMenu(optionsMenuAvailable());
    }

    abstract boolean optionsMenuAvailable();

    protected abstract int getIntentTitle();

    private void launchEditActivity(int position, long id) {
        Cursor c = mLentObjectCursor;
        c.moveToPosition(position);
        Bundle extras = new Bundle();
        extras.putInt(AddObject.ACTION_TYPE, getEditAction());
        extras.putLong(OpenLendDbAdapter.KEY_ROWID, id);
        extras.putString(OpenLendDbAdapter.KEY_DESCRIPTION, c.getString(
                c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_DESCRIPTION)));
        extras.putInt(OpenLendDbAdapter.KEY_TYPE, c.getInt(
                c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_TYPE)));
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = df.parse(c.getString(c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_DATE)));
            extras.putLong(OpenLendDbAdapter.KEY_DATE, date.getTime());
            Date modificationDate = df.parse(c.getString(c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_MODIFICATION_DATE)));
            extras.putLong(OpenLendDbAdapter.KEY_MODIFICATION_DATE, modificationDate.getTime());
        } catch (ParseException e) {
            throw new IllegalStateException("Illegal date in database!");
        }

        extras.putString(OpenLendDbAdapter.KEY_PERSON, c.getString(
                c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_PERSON)));
        extras.putString(OpenLendDbAdapter.KEY_PERSON_KEY, c.getString(
                c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_PERSON_KEY)));

        Intent intent = new Intent(getActivity(), AddObject.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtras(extras);
        startActivityForResult(intent, ACTION_EDIT);
    }

    protected abstract int getEditAction();

    protected abstract boolean redirectToDefaultListAfterEdit();

    protected void fillData() {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final Calendar now = new GregorianCalendar();
        final DurationCalculator durationCalculator = new DurationCalculator(getResources());

        SimpleCursorAdapter lentObjects = getLentObjects();

        lentObjects.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == 3) {
                    Calendar lentDate = new GregorianCalendar();
                    try {
                        long time = df.parse(cursor.getString(columnIndex)).getTime();
                        lentDate.setTimeInMillis(time);
                    } catch (ParseException e) {
                        throw new IllegalStateException("Unable to parse date " + cursor.getString(columnIndex));
                    }

                    TextView dateView = (TextView) view.findViewById(R.id.date);
                    dateView.setText(durationCalculator.getTimeDifference(lentDate, now));

                    return true;
                }

                return false;
            }
        });

        setListAdapter(lentObjects);
    }

    private SimpleCursorAdapter getLentObjects() {
        mLentObjectCursor = getDisplayedObjects();
        getActivity().startManagingCursor(mLentObjectCursor);

        String[] from = new String[]{
                OpenLendDbAdapter.KEY_DESCRIPTION,
                OpenLendDbAdapter.KEY_PERSON,
                OpenLendDbAdapter.KEY_DATE,
                OpenLendDbAdapter.KEY_MODIFICATION_DATE
        };

        int[] to = new int[]{
                R.id.toptext,
                R.id.bottomtext,
                R.id.date
        };

        return new SimpleCursorAdapter(getActivity(), R.layout.row, mLentObjectCursor, from, to);
    }

    protected abstract Cursor getDisplayedObjects();

    protected abstract boolean isMarkAsReturnedAvailable();

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, SUBMENU_EDIT, Menu.NONE, R.string.submenu_edit);

        if (isMarkAsReturnedAvailable()) {
            menu.add(Menu.NONE, SUBMENU_MARK_AS_RETURNED, Menu.NONE, R.string.submenu_mark_as_returned);
        }
        else {
            menu.add(Menu.NONE, SUBMENU_LEND_AGAIN, Menu.NONE, R.string.mark_as_lent_button);
        }

        menu.add(Menu.NONE, SUBMENU_DELETE, Menu.NONE, R.string.submenu_delete);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item) {

        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "Bad MenuInfo", e);
            return false;
        }
        int id = (int) getListAdapter().getItemId(info.position);

        if (item.getItemId() == SUBMENU_EDIT || item.getItemId() == SUBMENU_LEND_AGAIN) {
            launchEditActivity(info.position, id);
        } else if (item.getItemId() == SUBMENU_MARK_AS_RETURNED) {
            mDbHelper.markLentObjectAsReturned(id);
            fillData();
        } else if (item.getItemId() == SUBMENU_DELETE) {
            mDbHelper.deleteLentObject(id);
            fillData();
        }

        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            LentObject lentObject = new LentObject(bundle);

            if (bundle.getBoolean(AddObject.ADD_CALENDAR_ENTRY)) {
                final long startTime = bundle.getLong(AddObject.RETURN_DATE);
                final String title = String.format(getString(R.string.expected_return), lentObject.description);
                final String description = String.format(
                        getString(R.string.calendar_description), lentObject.description.trim(), lentObject.personName.trim());

                Intent intent = new Intent(Intent.ACTION_EDIT)
                        .setType("vnd.android.cursor.item/event")
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                        .putExtra(Events.ALL_DAY, true)
                        .putExtra(Events.TITLE, title)
                        .putExtra(Events.DESCRIPTION, description);
                startActivity(intent);
            }

            if (requestCode == ACTION_ADD) {
                lentObject.returned = false;
                lentObject.modificationDate = new Date();
                mDbHelper.createLentObject(lentObject);
            } else if (requestCode == ACTION_EDIT) {
                Long rowId = bundle.getLong(OpenLendDbAdapter.KEY_ROWID);
                mDbHelper.updateLentObject(rowId, lentObject);
                mDbHelper.markReturnedObjectAsLentAgain(rowId);
                if (redirectToDefaultListAfterEdit()) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            }

            fillData();
        }
        else if (resultCode == RESULT_DELETE) {
            Bundle bundle = data.getExtras();
            Long rowId = bundle.getLong(OpenLendDbAdapter.KEY_ROWID);
            mDbHelper.deleteLentObject(rowId);
            fillData();
        }
        else if (resultCode == RESULT_RETURNED) {
            Bundle bundle = data.getExtras();
            Long rowId = bundle.getLong(OpenLendDbAdapter.KEY_ROWID);
            mDbHelper.markLentObjectAsReturned(rowId);
            fillData();
        }
    }

    @Override
    public void onPositiveAction(DialogFragment dialog){
        String tag = dialog.getTag();

        if(tag == null){
            return;
        }
        switch(tag){
            case TAG_DIALOG_FIRST_TIME:
                Log.d(LOG_TAG, "first time dialog +");
                DatabaseHelper.importDatabaseFromXML(mDbHelper);
                fillData();
                break;
        }
    }

    @Override
    public void onNegativeAction(DialogFragment dialog){}

    @Override
    public void onNeutralAction(DialogFragment dialog){}
}
