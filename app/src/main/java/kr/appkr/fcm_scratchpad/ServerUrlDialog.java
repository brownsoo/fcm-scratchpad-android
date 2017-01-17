package kr.appkr.fcm_scratchpad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import kr.appkr.fcm_scratchpad.infra.MyConfig;

/**
 * Created by brownsoo on 2017. 1. 13..
 */

public class ServerUrlDialog extends DialogFragment {

    public interface ServerUrlDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }

    ServerUrlDialogListener listener;

    private Context context;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        init(activity);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        try {
            listener = (ServerUrlDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View custom = inflater.inflate(R.layout.dialog_url, null);
        final EditText editText = (EditText)custom.findViewById(R.id.serverUrlEt);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editText.setText(preferences.getString(MyConfig.PREF_KEY_3RD_URL, ""));

        builder.setView(custom)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        String url = editText.getText().toString();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        preferences.edit().putString(MyConfig.PREF_KEY_3RD_URL, url).apply();

                        listener.onDialogPositiveClick(ServerUrlDialog.this);

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ServerUrlDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
