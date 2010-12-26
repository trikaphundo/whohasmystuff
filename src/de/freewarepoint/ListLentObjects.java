package de.freewarepoint;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ListLentObjects extends ListActivity {

    private static final int ADD_OBJECT = Menu.FIRST;
    private static final String PREFS_NAME = "prefs";

    private static final int SUBMENU_EDIT = SubMenu.FIRST;
    private static final int SUBMENU_DELETE = SubMenu.FIRST + 1;

    private ArrayList<LendedObject> m_lend;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        m_lend = new ArrayList<LendedObject>();

        registerForContextMenu(getListView());

        loadData();
        updateList();
    }

    private void updateList() {
        LendAdapter m_adapter = new LendAdapter(this, R.layout.row, m_lend);
        setListAdapter(m_adapter);
    }

    private void loadData() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int numValues = settings.getInt("numValues", 0);

        DateFormat df = new SimpleDateFormat();

        for (int i = 0; i < numValues; i++) {
            String name = settings.getString(i + ".name", "");
            String type = settings.getString(i + ".type", "");
            String dateString = settings.getString(i + ".date", "");
            Date date;
            try {
                date = df.parse(dateString);
            } catch (ParseException e) {
                date = new Date();
            }
            LendedObject obj = new LendedObject();
            obj.setName(name);
            obj.setType(type);
            obj.setDate(date);
            m_lend.add(obj);
        }

    }

    private void storeData() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.putInt("numValues", m_lend.size());
        for (int i = 0; i < m_lend.size(); i++) {
            editor.putString(i + ".name", m_lend.get(i).getName());
            editor.putString(i + ".type", m_lend.get(i).getType());
            editor.putString(i + ".date", m_lend.get(i).getDate().toString());
        }
        editor.commit();
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
            m_lend.remove(id);
        }
        updateList();
        storeData();

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
            Bundle bundle = data.getExtras().getBundle("return");
            String name = bundle.getString("name");
            String type = bundle.getString("type");
            LendedObject newObj = new LendedObject();
            newObj.setName(name);
            newObj.setType(type);
            newObj.setDate(new Date());
            m_lend.add(newObj);
            storeData();
            updateList();
        }
    }

    private class LendAdapter extends ArrayAdapter<LendedObject> {

        private ArrayList<LendedObject> items;

        public LendAdapter(Context context, int textViewResourceId, ArrayList<LendedObject> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row, null);
            }
            LendedObject o = items.get(position);
            if (o != null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                //TextView dt = (TextView) v.findViewById(R.id.date);
                if (tt != null) {
                    tt.setText("Name: "+o.getName());                            }
                if(bt != null){
                    bt.setText("Type: "+ o.getType());
                }
                /*
                if (dt != null){
                    dt.setText("Created: " + o.getDate());
                }
                */
            }
            return v;
        }
    }
}
