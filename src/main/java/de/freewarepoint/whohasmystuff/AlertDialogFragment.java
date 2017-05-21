package de.freewarepoint.whohasmystuff;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


/**
 * A <code>DialogFragment</code> tailored to manage <code>AlertDialog</code>s.
 * The object interested in responding to the dialog actions must implement the inner interface
 * {@link AlertDialogFragment.AlertDialogFragmentListener}.
 * The dialog can be slightly customized by passing some information in a Bundle via
 * <code>setArguments(Bundle)</code>, using this class constants as keys; or, equivalently,
 * by means of the class method <code>newObject()</code>.
 *
 * Since DialogFragments must call the method <code>show()</code> in order to show the dialog,
 * and that method requires a tag to identify the fragment, <strong>the listener can identify
 * dialogs by means of their tag</strong>.
 *
 * Example of how to use this class:
 * <pre><code>
 *     ...
 *     Bundle params;
 *     AlertDialogFragment dialog;
 *
 *     //put dialog parameters in the bundle
 *     params = new Bundle();
 *     params.putString(AlertDialogFragment.DIALOG_TITLE, "my cool dialog title");
 *     params.putString(AlertDialogFragment.DIALOG_MESSAGE, "my cool dialog message");
 *     params.putString(AlertDialogFragment.DIALOG_POSITIVE_TEXT, "Ok");
 *     params.putString(AlertDialogFragment.DIALOG_NEGATIVE_TEXT, "Cancel");
 *
 *     //create the dialog and pass the construction parameters
 *     dialog = new AlertDialogFragment();
 *     dialog.setArguments(params);
 *     dialog.setAlertDialogFragmentListener(getActivity());
 *     dialog.show(getFragmentManager(), "fragmentTag");
 *     ...
 * </code></pre>
 *
 * That piece of code would show a dialog with two buttons; which is equivalent to the following code
 *
 * <pre><code>
 *     AlertDialogFragment dialog = AlertDialogFragment.newObject("my cool dialog title",
 *                                      "my cool dialog message", "Ok", "Cancel", null, 0);
 *     dialog.setAlertDialogFragmentListener(getActivity());
 *     dialog.show(getFragmentManager(), "fragmentTag");
 * </code></pre>
 *
 * The related listener (an activity in this case) could look more or less like this:
 *
 * <pre><code>
 *     public class MainActivity extends AppCompatActivity
 *         implements AlertDialogFragment.AlertDialogFragmentListener{
 *         public static final String LOG_TAG = "MainActivity";
 *
 *         ...
 *         @Override
 *         public void onPositiveAction(DialogFragment dialog){
 *             String tag = dialog.getTag();
 *
 *             switch(tag){
 *                 case "desired tag":
 *                   //your code goes here
 *                   break;
 *                 case "another desired tag":
 *                   //your code goes here
 *                   break;
 *                 ...
 *             }
 *         }
 *
 *         @Override
 *         public void onNegativeAction(DialogFragment dialog){
 *             String tag = dialog.getTag();
 *
 *             switch(tag){
 *                 case "desired tag":
 *                   //your code goes here
 *                   break;
 *                 case "another desired tag":
 *                   //your code goes here
 *                   break;
 *                 ...
 *             }
 *         }
 *
 *         @Override
 *         public void onNeutralAction(DialogFragment dialog){
 *             String tag = dialog.getTag();
 *
 *             switch(tag){
 *                 case "desired tag":
 *                   //your code goes here
 *                   break;
 *                 case "another desired tag":
 *                   //your code goes here
 *                   break;
 *                 ...
 *             }
 *         }
 *     }
 * </code></pre>
 *
 * To inform the user about an error we can create a dialog with just one button:
 *
 * <pre><code>
 *     ...
 *     Bundle params;
 *     AlertDialogFragment dialog;
 *
 *     //put dialog parameters in the bundle
 *     params = new Bundle(4);
 *     params.putString(AlertDialogFragment.DIALOG_TITLE, "my error dialog");
 *     params.putString(AlertDialogFragment.DIALOG_MESSAGE, "super dangerous error happened");
 *     params.putString(AlertDialogFragment.DIALOG_POSITIVE_TEXT, "Understood");
 *     params.putInt(AlertDialogFragment.DIALOG_ICON, android.R.drawable.ic_alert_dialog);
 *
 *     //create the dialog and pass the construction parameters
 *     dialog = new AlertDialogFragment();
 *     dialog.setArguments(params);
 *     dialog.setAlertDialogFragmentListener(someObject);
 *     dialog.show(getFragmentManager(), "exampleTag");
 *     ...
 * </code></pre>
 *
 * This is equivalent to:
 *
 * <pre><code>
 *     AlertDialogFragment dialog = AlertDialogFragment.newObject("my error dialog",
 *                                      "super dangerous error happened", "Understood", null,
 *                                      null, android.R.drawable.ic_alert_dialog);
 *     dialog.setAlertDialogFragmentListener(someObject);
 *     dialog.show(getFragmentManager(), "exampleTag");
 * </code></pre>
 * @author trikaphundo (d3vS4n@tutanota.com)
 */
public class AlertDialogFragment extends DialogFragment {
    /**Key to identify the dialog's title in the Bundle argument. The mapped value is a String*/
    public static final String DIALOG_TITLE = "dialog_title";
    /**Key to identify the message of the dialog in the Bundle argument. The mapped value is a String*/
    public static final String DIALOG_MESSAGE = "dialog_message";
    /**Key to identify the text of the dialog's positive action in the Bundle argument. The mapped value is a String*/
    public static final String DIALOG_POSITIVE_TEXT = "dialog_positive_text";
    /**Key to identify the text of the dialog's negative action in the Bundle argument. The mapped value is a String*/
    public static final String DIALOG_NEGATIVE_TEXT = "dialog_negative_text";
    /**Key to identify the text of the dialog's neutral action in the Bundle argument. The mapped value is a String*/
    public static final String DIALOG_NEUTRAL_TEXT = "dialog_neutral_text";
    /**Key to identify in the Bundle argument the icon to use on the dialog. The mapped value is a resource id*/
    public static final String DIALOG_ICON = "dialog_icon";

    /**The observer (listener) for this fragment's dialog actions.*/
    private AlertDialogFragmentListener mListener;


    /**
     * Interface for listening to actions of the AlertDialog held by an AlertDialogFragment.
     * The object interested in an AlertDialog's actions should implement this interface in order to
     * run the appropiate code when the user makes (clicks) an action of the dialog.
     * Since DialogFragments must call the method <code>show()</code> in order to show the dialog,
     * and that method requires a tag to identify the fragment, <strong>the listener can identify
     * dialogs by means of their tag</strong>.
     */
    public interface AlertDialogFragmentListener{
        /**
         * Code to run when the user clicks the positive action of an AlertDialog.
         * @param dialog the dialog which received the click.
         */
        void onPositiveAction(DialogFragment dialog);
        /**
         * Code to run when the user clicks the negative action of an AlertDialog.
         * @param dialog the dialog which received the click.
         */
        void onNegativeAction(DialogFragment dialog);

        /**
         * Code to run when the user clicks the neutral action of an AlertDialog.
         * @param dialog the dialog which received the click
         */
        void onNeutralAction(DialogFragment dialog);
    }

    /**
     * Convenient class method to create objects of this class.
     * This method saves you the creation of the Bundle holding the supplied parameters;
     * This method is equivalent to:
     * <pre><code>
     *     AlertDialogFragment dialog = new AlertDialogFragment();
     *     Bundle params = new Bundle();
     *
     *     params.putString(AlertDialogFragment.DIALOG_TITLE, title);
     *     params.putString(AlertDialogFragment.DIALOG_MESSAGE, message);
     *     params.putString(AlertDialogFragment.DIALOG_POSITIVE_TEXT, positiveText);
     *     params.putString(AlertDialogFragment.DIALOG_NEGATIVE_TEXT, negativeText);
     *     params.putString(AlertDialogFragment.DIALOG_NEUTRAL_TEXT, neutralText);
     *     params.putInt(AlertDialogFragment.DIALOG_ICON, iconId);
     *
     *     dialog.setArguments(params);
     * </code></pre>
     * @param title dialog's title
     * @param message dialog's message
     * @param positiveText text for the positive action of the dialog
     * @param negativeText text for the negative action of the dialog
     * @param neutralText text for the neutral action of the dialog
     * @param iconId drawable resource identifier, 0 if none
     * @return an object of this class with the given parameters set
     */
    public static AlertDialogFragment newObject(String title, String message,
                                                String positiveText, String negativeText,
                                                String neutralText, int iconId) {
        Bundle params;
        AlertDialogFragment dialog;

        params = new Bundle(6);
        params.putString(AlertDialogFragment.DIALOG_TITLE, title);
        params.putString(AlertDialogFragment.DIALOG_MESSAGE, message);
        params.putString(AlertDialogFragment.DIALOG_POSITIVE_TEXT, positiveText);
        params.putString(AlertDialogFragment.DIALOG_NEGATIVE_TEXT, negativeText);
        params.putString(AlertDialogFragment.DIALOG_NEUTRAL_TEXT, neutralText);
        params.putInt(AlertDialogFragment.DIALOG_ICON, iconId);

        dialog = new AlertDialogFragment();
        dialog.setArguments(params);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Bundle dialogParams;
        AlertDialog.Builder builder;
        DialogInterface.OnClickListener onClickListener;


        //retrieve constructor arguments; if none, use default values
        dialogParams = getArguments();
        String title, msg, positiveText, negativeText, neutralText;
        int iconId;

        if(dialogParams != null){
            title = dialogParams.getString(AlertDialogFragment.DIALOG_TITLE);
            msg = dialogParams.getString(AlertDialogFragment.DIALOG_MESSAGE);
            positiveText = dialogParams.getString(AlertDialogFragment.DIALOG_POSITIVE_TEXT);
            negativeText = dialogParams.getString(AlertDialogFragment.DIALOG_NEGATIVE_TEXT);
            neutralText = dialogParams.getString(AlertDialogFragment.DIALOG_NEUTRAL_TEXT);
            iconId = dialogParams.getInt(AlertDialogFragment.DIALOG_ICON);
        }else{ //default values
            title = msg = positiveText = negativeText = neutralText = null;
            iconId = 0;
        }


        //create the listener for the dialog buttons,
        // just a man in the middle to call the listener methods
        onClickListener = (dialog, which) -> {
                if(mListener != null){
                    switch(which){
                        case DialogInterface.BUTTON_POSITIVE:
                            mListener.onPositiveAction(AlertDialogFragment.this);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            mListener.onNegativeAction(AlertDialogFragment.this);
                            break;
                        case DialogInterface.BUTTON_NEUTRAL:
                            mListener.onNeutralAction(AlertDialogFragment.this);
                            break;
                    }
                }
        };


        //create the dialog
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton(positiveText, onClickListener)
                .setNegativeButton(negativeText, onClickListener)
                .setNeutralButton(neutralText, onClickListener);
        if(iconId != 0){ //is valid resource identifier
            builder.setIcon(iconId);
        }

        return builder.create();
    }

    /**
     * Sets the dialog's listener.
     * When the user makes (clicks) an action on the dialog, the listener will be notified.
     * It is <strong>important</strong> to call this method <em>before</em> adding this fragment
     * to a FragmentManager.
     * @param l listener of this fragment's dialog actions
     */
    public void setAlertDialogFragmentListener(AlertDialogFragment.AlertDialogFragmentListener l){
        this.mListener = l;
    }
}
