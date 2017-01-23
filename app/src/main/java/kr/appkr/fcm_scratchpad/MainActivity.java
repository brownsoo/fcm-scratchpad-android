package kr.appkr.fcm_scratchpad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

import kr.appkr.fcm_scratchpad.infra.MyConfig;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ServerUrlDialog.ServerUrlDialogListener,
        SendTokenDialog.SendDialogListener
{

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST = 100;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();


    private SharedPreferences pref;
    private Button subscribeButton;
    private TextView urlTv;
    private TextView receivedTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean subscribed = pref.getBoolean(MyConfig.PREF_KEY_SUBSCRIBE, false);
        subscribeButton = (Button) findViewById(R.id.subscribeButton);
        subscribeButton.setText(subscribed?R.string.unsubscribe_from_news :R.string.subscribe_to_news);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean subscribed = pref.getBoolean(MyConfig.PREF_KEY_SUBSCRIBE, false);
                String msg;
                if (subscribed) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
                    msg = getString(R.string.msg_unsubscribed);
                }
                else {
                    FirebaseMessaging.getInstance().subscribeToTopic("news");
                    msg = getString(R.string.msg_subscribed);
                }

                subscribeButton.setText(subscribed?R.string.subscribe_to_news :R.string.unsubscribe_from_news);
                // Log and toast
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        subscribeButton.setVisibility(View.GONE); // 토픽 수신 버튼 숨김

        urlTv = (TextView)findViewById(R.id.serverUrlTv);
        String url = pref.getString(MyConfig.PREF_KEY_3RD_URL, "");
        urlTv.setText(url);

        receivedTv = (TextView)findViewById(R.id.receivedTv);

        Button editUrlButton = (Button) findViewById(R.id.edit3rdServerUrlButton);
        editUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        Button sendTokenButton = (Button) findViewById(R.id.sendTokenButton);
        sendTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSendDialog();
            }
        });

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, new IntentFilter(MyFirebaseMessagingService.ACTION_PAYLOAD));


        if (getIntent().getExtras() != null) {

            final StringBuilder sb = new StringBuilder();
            sb.append("알림바 터치로 받은 데이터").append("\n");
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                sb.append("   ").append(key).append(": ").append(value).append("\n");
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    receivedTv.setText(sb.toString());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String token = FirebaseInstanceId.getInstance().getToken();
        TextView tokenTv = (TextView)findViewById(R.id.tokenTv);
        tokenTv.setText(token);
        tokenTv.setMovementMethod(new ScrollingMovementMethod());

        if (token != null) {
            Log.d(TAG, "token = " + token);
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_PHONE_STATE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(MyFirebaseMessagingService.KEY_REMOTE_MESSAGE)) {
                RemoteMessage remoteMessage = intent.getParcelableExtra(MyFirebaseMessagingService.KEY_REMOTE_MESSAGE);
                final StringBuilder sb = new StringBuilder();
                sb.append("데이터 메시지 받음 at ").append(remoteMessage.getSentTime()).append("\n")
                    .append("From: ").append(remoteMessage.getFrom()).append("\n")
                    .append("MessageType: ").append(remoteMessage.getMessageType()).append("\n")
                    .append("Data: ").append("\n");

                for (String key : remoteMessage.getData().keySet()) {
                    String value = remoteMessage.getData().get(key);
                    sb.append("   ").append(key).append(": ").append(value).append("\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        receivedTv.setText(sb.toString());
                    }
                });
            }
        }
    };

    private void showEditDialog() {
        DialogFragment dialog = new ServerUrlDialog();
        dialog.show(getSupportFragmentManager(), "ServerUrlDialog");
    }

    @Override
    public void onSavedServerUrlDialog(DialogFragment dialog) {
        String url = pref.getString(MyConfig.PREF_KEY_3RD_URL, "");
        urlTv.setText(url);
    }

    private void showSendDialog() {
        DialogFragment dialog = new SendTokenDialog();
        dialog.show(getSupportFragmentManager(), "SendTokenDialog");
    }



    @Override
    public void onConfirmSendDialog(String email, String pw) {
        sendTokenUrl(email, pw);
    }

    private void sendTokenUrl(final String email, final String pw) {

        final String url = pref.getString(MyConfig.PREF_KEY_3RD_URL, "");
        if (url.length() == 0) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                String token = FirebaseInstanceId.getInstance().getToken();
                String json = MyConfig.takeTokenJson(getApplicationContext(), token);
                RequestBody body = RequestBody.create(JSON, json);

                String credential = Credentials.basic(email, pw);

                Request request = new Request.Builder()
                        .header("Authorization", credential)
                        .url(url)
                        .post(body)
                        .build();


                Log.d(TAG, "url: " + url);
                Log.d(TAG, json);

                try {
                    Response response = client.newCall(request).execute();
                    final String re = response.body().string();
                    if (re != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                receivedTv.setText("Response 데이터:\n" + re);
                            }
                        });
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            receivedTv.setText("오류 데이터:\n"+e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
}
