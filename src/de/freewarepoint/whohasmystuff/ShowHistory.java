package de.freewarepoint.whohasmystuff;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

public class ShowHistory extends AbstractListIntent {

	@Override
	protected int getIntentTitle() {
		return R.string.history_title;
	}

	@Override
	protected int getEditAction() {
		return AddObject.ACTION_EDIT_RETURNED;
	}

	@Override
	protected Cursor getDisplayedObjects() {
		return mDbHelper.fetchReturnedObjects();
	}
}
