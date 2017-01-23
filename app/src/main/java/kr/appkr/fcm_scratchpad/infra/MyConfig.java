package kr.appkr.fcm_scratchpad.infra;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.UUID;

/**
 * My Config
 * Created by brownsoo on 2017. 1. 13..
 */

public class MyConfig {

    public static final String PREF_KEY_3RD_URL = "third_party_url";
    public static final String PREF_KEY_SUBSCRIBE = "subscribe";
    public static final String PREF_KEY_EMAIL = "email";
    public static final String PREF_KEY_PW = "password";

    public static String takeTokenJson(Context context, String token) {

        String json = "{" +
                "\"os_enum\":\"ANDROID\", " +
                "\"push_service_enum\":\"FCM\", " +
                "\"device_id\":\"%s\", "+
                "\"push_service_id\":\"%s\", " +
                "\"operator\":\"%s\", " +
                "\"model\":\"%s\", " +
                "\"api_level\":\"%d\"" +
                "}";

        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        String operator = tm.getNetworkOperatorName();
        String model = getDeviceModel();
        int apiLevel = Build.VERSION.SDK_INT;

        return String.format(json, deviceId, token, operator, model, apiLevel);

    }

    private static String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalizeFirstChar(model);
        } else {
            return capitalizeFirstChar(manufacturer) + " " + model;
        }
    }

    private static String capitalizeFirstChar(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}
