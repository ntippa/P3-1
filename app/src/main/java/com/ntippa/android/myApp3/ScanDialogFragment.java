package com.ntippa.android.myApp3;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.widget.DialerFilter;

/**
 * Created by Nalini on 2/26/2016.
 */
public class ScanDialogFragment extends DialogFragment {
    String message;

    public static DialogFragment newInstance(String message)
    {
       DialogFragment myFragment = new ScanDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        myFragment.setArguments(args);
        return myFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        String message = getArguments().getString("message");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
