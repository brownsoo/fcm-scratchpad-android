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
 * 기기 등록을 위해 사용자 정보 확인
 * Created by brownsoo on 2017. 1. 13..
 */

public class SendTokenDialog extends DialogFragment {

    public interface SendDialogListener {
        void onConfirmSendDialog(String email, String pw);
    }

    SendDialogListener listener;

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
            listener = (SendDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement SendDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View custom = inflater.inflate(R.layout.dialog_send, null);
        final EditText emailEt = (EditText)custom.findViewById(R.id.emailEt);
        final EditText pwEt = (EditText)custom.findViewById(R.id.pwEt);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        emailEt.setText(preferences.getString(MyConfig.PREF_KEY_EMAIL, ""));
        pwEt.setText(preferences.getString(MyConfig.PREF_KEY_PW, ""));

        builder.setView(custom)
                // Add action buttons
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        String email = emailEt.getText().toString();
                        String pw = pwEt.getText().toString();

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        preferences.edit().putString(MyConfig.PREF_KEY_EMAIL, email).apply();
                        preferences.edit().putString(MyConfig.PREF_KEY_PW, pw).apply();

                        listener.onConfirmSendDialog(email, pw);

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SendTokenDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
