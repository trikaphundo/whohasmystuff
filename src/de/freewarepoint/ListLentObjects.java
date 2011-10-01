package de.freewarepoint;

import android.database.Cursor;
import android.view.Menu;
import android.view.MenuInflater;

public class ListLentObjects extends AbstractListIntent {

	@Override
	protected int getIntentTitle() {
		return R.string.app_name;
	}

	@Override
	protected int getEditAction() {
		return AddObject.ACTION_EDIT_LENT;
	}

	@Override
	protected Cursor getDisplayedObjects() {
		return mDbHelper.fetchLentObjects();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

}
