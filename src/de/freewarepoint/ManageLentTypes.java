package de.freewarepoint;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

public class ManageLentTypes extends ListActivity {

    private OpenLendDbAdapter mDbHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_lent_types);
        setTitle(R.string.manageTypesTitle);

        mDbHelper = new OpenLendDbAdapter(this);
        mDbHelper.open();

        fillData();

        registerForContextMenu(getListView());
    }

    private void fillData() {
        Cursor mLentObjectCursor = mDbHelper.fetchAllLentTypes();
        startManagingCursor(mLentObjectCursor);

        String[] from = new String[]{
                OpenLendDbAdapter.KEY_TYPE,
        };

        int[] to = new int[]{
                R.id.typename,
        };

        SimpleCursorAdapter lentObjects = new SimpleCursorAdapter(this, R.layout.type_row, mLentObjectCursor, from, to);

        setListAdapter(lentObjects);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manage_lent_types, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // We have only one menu option
            case R.id.addButton:
                askForType();
                break;
        }
        return true;
    }

    private void askForType() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.add_type);

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                mDbHelper.createLentType(value);
                fillData();
            }
        });

        alert.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing
            }
        });

        alert.show();
    }
}