package de.freewarepoint;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ListLentObjects extends ListActivity {

    private static final int ADD_OBJECT = Menu.FIRST;

    private static final int SUBMENU_EDIT = SubMenu.FIRST;
    private static final int SUBMENU_DELETE = SubMenu.FIRST + 1;

    private OpenLendDbAdapter mDbHelper;
    private Cursor mLentObjectCursor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mDbHelper = new OpenLendDbAdapter(this);
        mDbHelper.open();

        fillData();

        registerForContextMenu(getListView());
    }

    private void fillData() {
        mLentObjectCursor = mDbHelper.fetchAllLentObjects();
        startManagingCursor(mLentObjectCursor);

        String[] from = new String[]{
                OpenLendDbAdapter.KEY_TYPE,
                OpenLendDbAdapter.KEY_DESCRIPTION,
                OpenLendDbAdapter.KEY_DATE
        };

        int[] to = new int[]{
                R.id.toptext,
                R.id.bottomtext,
                R.id.date
        };

        SimpleCursorAdapter lentObjects = new SimpleCursorAdapter(this, R.layout.row, mLentObjectCursor, from, to);

        final DateFormat adf = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        lentObjects.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == 3) {
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

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

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
            Log.e("Bla", "bad menuInfo", e);
            return false;
        }
        int id = (int) getListAdapter().getItemId(info.position);

        if (item.getItemId() == SUBMENU_DELETE) {
            mDbHelper.deleteLentObject(id);
            fillData();
        }

        return true;
    }

    // This method is called once the menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // We have only one menu option
            case R.id.addButton:
                // Launch Preference activity
                Intent i = new Intent(this, AddObject.class);
                startActivityForResult(i, ADD_OBJECT);
                break;
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_CANCELED){
            throw new IllegalStateException("Error!");
        }
        else {
            Bundle bundle = data.getExtras();
            String name = bundle.getString(OpenLendDbAdapter.KEY_DESCRIPTION);
            String type = bundle.getString(OpenLendDbAdapter.KEY_TYPE);
            mDbHelper.createLentObject(type, name, new Date());
            fillData();
        }
    }
}
