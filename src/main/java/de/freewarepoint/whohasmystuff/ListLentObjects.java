package de.freewarepoint.whohasmystuff;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import de.freewarepoint.whohasmystuff.database.DatabaseHelper;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ListLentObjects extends AbstractListFragment {

    private static final int REQUEST_EXPORT_PERMISSION = 1024;
    private static final int REQUEST_IMPORT_PERMISSION = 1025;

	@Override
	protected int getIntentTitle() {
		return R.string.app_name;
	}

	@Override
	protected int getEditAction() {
		return AddObject.ACTION_EDIT_LENT;
	}

    @Override
    protected boolean redirectToDefaultListAfterEdit() {
        return false;
    }

    @Override
	protected Cursor getDisplayedObjects() {
		return mDbHelper.fetchLentObjects();
	}

    @Override
    protected boolean isMarkAsReturnedAvailable() {
        return true;
    }

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.addButton:
                i = new Intent(getActivity(), AddObject.class);
                i.putExtra(AddObject.ACTION_TYPE, AddObject.ACTION_ADD);
                startActivityForResult(i, ACTION_ADD);
                break;
            case R.id.historyButton:
                Fragment newFragment = new ShowHistory();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.mainActivity, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.exportButton:
                exportAfterPermissionsCheck();
                break;
            case R.id.importButton:
                importAfterPermissionsCheck();
                break;
        }
        return true;
    }

    private void exportAfterPermissionsCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_EXPORT_PERMISSION);
                return;
            }
        }

        if (isExternalStorageWritable()) {
            if (DatabaseHelper.existsBackupFile()) {
                askForExportConfirmation();
            }
            else {
                exportData();
            }
        }
    }

    private void importAfterPermissionsCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_IMPORT_PERMISSION);
                return;
            }
        }

        if (isExternalStorageReadable()) {
            askForImportConfirmation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        final boolean success = grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED;

        switch (requestCode) {
            case REQUEST_EXPORT_PERMISSION:
                if (success) {
                    exportAfterPermissionsCheck();
                }
                break;
            case REQUEST_IMPORT_PERMISSION:
                if (success) {
                    importAfterPermissionsCheck();
                }
                break;
        }
    }

    boolean optionsMenuAvailable() {
        return true;
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(getString(R.string.sd_card_error_title));
            alertDialog.setMessage(getString(R.string.sd_card_error_not_readable));
            alertDialog.show();
            return false;
        }
        else {
            return true;
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(getString(R.string.sd_card_error_title));
            alertDialog.setMessage(getString(R.string.sd_card_error_not_writeable));
            alertDialog.show();
            return false;
        }
        else {
            return true;
        }
    }

    private void askForExportConfirmation() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setTitle(getString(R.string.database_export_title));
        dialog.setMessage(getString(R.string.database_export_message));
        dialog.setPositiveButton(android.R.string.yes, (dialog1, whichButton) -> exportData());
        dialog.setNegativeButton(android.R.string.no, null);
        dialog.show();
    }

    private void exportData() {
        if (DatabaseHelper.exportDatabaseToXML(mDbHelper)) {
            Toast.makeText(getActivity(), R.string.database_export_success, Toast.LENGTH_LONG).show();
        }
        else {
            showExportErrorDialog();
        }
    }

    private void askForImportConfirmation() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setTitle(getString(R.string.database_import_title));
        dialog.setMessage(getString(R.string.database_import_message));
        dialog.setPositiveButton(android.R.string.yes, (dialog1, whichButton) -> {
            if (DatabaseHelper.importDatabaseFromXML(mDbHelper)) {
                fillData();
            }
            else {
                showImportErrorDialog();
            }
        });
        dialog.setNegativeButton(android.R.string.no, null);
        dialog.show();
    }

    private void showImportErrorDialog() {
        showErrorDialog(getString(R.string.database_import_error));
    }

    private void showExportErrorDialog() {
        showErrorDialog(getString(R.string.database_export_error));
    }

    private void showErrorDialog(String message) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setTitle(getString(R.string.database_import_title));
        dialog.setMessage(message);
        dialog.setPositiveButton(android.R.string.yes, null);
        dialog.show();
    }


}
