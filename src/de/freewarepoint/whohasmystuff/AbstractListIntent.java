package de.freewarepoint.whohasmystuff;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractListIntent extends ListActivity {

    private static final int SUBMENU_EDIT = SubMenu.FIRST;
    private static final int SUBMENU_DELETE = SubMenu.FIRST + 1;

    public static final int ACTION_ADD = 1;
    public static final int ACTION_EDIT = 2;

	public static final int RESULT_DELETE = 2;
	public static final int RESULT_RETURNED = 3;

    public static final String LOG_TAG = "WhoHasMyStuff";

    protected OpenLendDbAdapter mDbHelper;
    private Cursor mLentObjectCursor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		setTitle(getIntentTitle());

        mDbHelper = new OpenLendDbAdapter(this);
        mDbHelper.open();

        fillData();

        ListView listView = getListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                launchEditActivity(position, id);
            }
        });

        registerForContextMenu(listView);
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
	}

	protected abstract int getIntentTitle();

    private void launchEditActivity(int position, long id) {
        Cursor c = mLentObjectCursor;
        c.moveToPosition(position);
        Bundle extras = new Bundle();
        extras.putInt(AddObject.ACTION_TYPE, getEditAction());
        extras.putLong(OpenLendDbAdapter.KEY_ROWID, id);
        extras.putString(OpenLendDbAdapter.KEY_DESCRIPTION, c.getString(
                c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_DESCRIPTION)));
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = df.parse(c.getString(c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_DATE)));
            extras.putLong(OpenLendDbAdapter.KEY_DATE, date.getTime());
        } catch (ParseException e) {
            throw new IllegalStateException("Illegal date in database!");
        }

        extras.putString(OpenLendDbAdapter.KEY_PERSON, c.getString(
                c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_PERSON)));
		extras.putString(OpenLendDbAdapter.KEY_PERSON_KEY, c.getString(
				c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_PERSON_KEY)));

        Intent intent = new Intent(this, AddObject.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtras(extras);
        startActivityForResult(intent, ACTION_EDIT);
    }

	protected abstract int getEditAction();

    protected void fillData() {
        final DateFormat adf = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		SimpleCursorAdapter lentObjects = getLentObjects();

        lentObjects.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == 2) {
                    Date date;
                    try {
                        date = df.parse(cursor.getString(columnIndex));
                    } catch (ParseException e) {
                        throw new IllegalStateException("Unable to parse date " + cursor.getString(columnIndex));
                    }

                    TextView dateView = (TextView) view.findViewById(R.id.date);
                    dateView.setText(adf.format(date));

                    return true;
                }

                return false;
            }

        });

        setListAdapter(lentObjects);
    }

	private SimpleCursorAdapter getLentObjects() {
		mLentObjectCursor = getDisplayedObjects();
		startManagingCursor(mLentObjectCursor);

		String[] from = new String[]{
				OpenLendDbAdapter.KEY_DESCRIPTION,
				OpenLendDbAdapter.KEY_PERSON,
				OpenLendDbAdapter.KEY_DATE
		};

		int[] to = new int[]{
				R.id.toptext,
				R.id.bottomtext,
				R.id.date
		};

		return new SimpleCursorAdapter(this, R.layout.row, mLentObjectCursor, from, to);
	}

	protected abstract Cursor getDisplayedObjects();

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, SUBMENU_EDIT, 0, R.string.submenu_edit);
        menu.add(0, SUBMENU_DELETE, 0, R.string.submenu_delete);
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

        if (item.getItemId() == SUBMENU_EDIT) {
            launchEditActivity(info.position, id);
        } else if (item.getItemId() == SUBMENU_DELETE) {
            mDbHelper.deleteLentObject(id);
            fillData();
        }

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String name = bundle.getString(OpenLendDbAdapter.KEY_DESCRIPTION);
            long time = bundle.getLong(OpenLendDbAdapter.KEY_DATE);
            String personName = bundle.getString(OpenLendDbAdapter.KEY_PERSON);
			String personKey = bundle.getString(OpenLendDbAdapter.KEY_PERSON_KEY);

            if (requestCode == ACTION_ADD) {
                mDbHelper.createLentObject(name, new Date(time), personName, personKey, false);
            } else if (requestCode == ACTION_EDIT) {
                Long rowId = bundle.getLong(OpenLendDbAdapter.KEY_ROWID);
                mDbHelper.updateLentObject(rowId, name, new Date(time), personName, personKey);
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
}
