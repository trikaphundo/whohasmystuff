package de.freewarepoint.whohasmystuff;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import de.freewarepoint.whohasmystuff.database.DatabaseHelper;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ListLentObjects extends AbstractListFragment implements AlertDialogFragment.AlertDialogFragmentListener{

    private static final int REQUEST_EXPORT_PERMISSION = 1024;
    private static final int REQUEST_IMPORT_PERMISSION = 1025;
    /**Tag used to identify the DialogFragment for export confirmation.*/
    private static final String TAG_DIALOG_EXPORT = "export_confirmation_dialog";
    /**Tag used to identify the DialogFragment for import confirmation.*/
    private static final String TAG_DIALOG_IMPORT = "import_confirmation_dialog";



	@Override
	protected int getIntentTitle() { return R.string.app_name; }

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

    @Override
    public void onPositiveAction(DialogFragment dialog){
        super.onPositiveAction(dialog);


        String tag = dialog.getTag();

        if(tag == null){
            return;
        }
        switch(tag){
            case TAG_DIALOG_EXPORT:
                exportData();
                break;
            case TAG_DIALOG_IMPORT:
                if (DatabaseHelper.importDatabaseFromXML(mDbHelper)) {
                    fillData();
                }
                else {
                    showImportErrorDialog();
                }
                break;
        }
    }

    @Override
    public void onNegativeAction(DialogFragment dialog){}

    @Override
    public void onNeutralAction(DialogFragment dialog){}

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
            AlertDialogFragment dialog;

            dialog = AlertDialogFragment.newObject(getResources().getString(R.string.sd_card_error_title),
                    getResources().getString(R.string.sd_card_error_not_readable),
                    getResources().getString(android.R.string.ok),
                    null,
                    null,
                    0);
            dialog.setAlertDialogFragmentListener(this);
            dialog.show(getFragmentManager(), null);

            return false;
        }
        else {
            return true;
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            AlertDialogFragment dialog;
            Bundle args;

            args = new Bundle(3);
            args.putString(AlertDialogFragment.DIALOG_TITLE, getResources().getString(R.string.sd_card_error_title));
            args.putString(AlertDialogFragment.DIALOG_MESSAGE, getResources().getString(R.string.sd_card_error_not_writeable));
            args.putString(AlertDialogFragment.DIALOG_POSITIVE_TEXT, getResources().getString(android.R.string.ok));

            dialog = new AlertDialogFragment();
            dialog.setArguments(args);
            dialog.setAlertDialogFragmentListener(this);
            dialog.show(getFragmentManager(), null);

            return false;
        }
        else {
            return true;
        }
    }

    private void askForExportConfirmation() {
        AlertDialogFragment dialog;

        dialog = AlertDialogFragment.newObject(getResources().getString(R.string.database_export_title),
                getResources().getString(R.string.database_export_message),
                getResources().getString(android.R.string.yes),
                getResources().getString(android.R.string.no),
                null,
                android.R.drawable.ic_dialog_alert);
        dialog.setAlertDialogFragmentListener(this);
        dialog.show(getFragmentManager(), TAG_DIALOG_EXPORT);
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
        AlertDialogFragment dialog;

        dialog = AlertDialogFragment.newObject(getResources().getString(R.string.database_import_title),
                getResources().getString(R.string.database_import_message),
                getResources().getString(android.R.string.yes),
                getResources().getString(android.R.string.no),
                null,
                android.R.drawable.ic_dialog_alert);
        dialog.setAlertDialogFragmentListener(this);
        dialog.show(getFragmentManager(), TAG_DIALOG_IMPORT);
    }

    private void showImportErrorDialog() {
        showErrorDialog(getString(R.string.database_import_error));
    }

    private void showExportErrorDialog() {
        showErrorDialog(getString(R.string.database_export_error));
    }

    /**
     * Shows an AlertDialog with the given message
     * @param message
     */
    private void showErrorDialog(String message) {
        AlertDialogFragment dialog;
        Bundle args;


        //set dialog args
        args = new Bundle(4);
        args.putString(AlertDialogFragment.DIALOG_TITLE, getResources().getString(R.string.database_import_title));
        args.putString(AlertDialogFragment.DIALOG_MESSAGE, message);
        args.putString(AlertDialogFragment.DIALOG_POSITIVE_TEXT, getResources().getString(android.R.string.yes));
        args.putInt(AlertDialogFragment.DIALOG_ICON, android.R.drawable.ic_dialog_alert);

        dialog = new AlertDialogFragment();
        dialog.setArguments(args);
        dialog.setAlertDialogFragmentListener(this);
        dialog.show(getFragmentManager(), null);
    }
}
