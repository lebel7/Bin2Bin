package com.proper.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import com.proper.Logger.LogHelper;
import com.proper.bin2bin.R;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Lebel on 29/05/2014.
 */
public class UserAuthenticator {
    private SharedPreferences prefs = null;
    private boolean isAuthenticated = false;
    private Context thisContext = null;
    private String deviceIMEI = "";
    private static final String ApplicationID = "Bin2Bin";
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private LogHelper logger = new LogHelper();

    public UserAuthenticator(Context context) {
        thisContext = context;
        prefs = thisContext.getSharedPreferences(context.getString(R.string.preference_credentials), Context.MODE_PRIVATE);
        isAuthenticated = prefs.contains("UserToken");
        TelephonyManager mngr = (TelephonyManager) thisContext.getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = mngr.getDeviceId();
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public UserLoginResponse getCurrentUser() {
        UserLoginResponse response = null;
        ObjectMapper mapper = new ObjectMapper();
        if (isAuthenticated) {
            try {
                response = mapper.readValue(getUserToken().getBytes(), UserLoginResponse.class);
            } catch (IOException e) {
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "UserAuthenticator - getCurrentUser", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.Log(log);
            }
        }
        return response;
    }

    public String getUserToken() {
        String result = "NotAuthenticated";
        if (isAuthenticated) {
            result = prefs.getString("UserToken", "NotAuthenticated");
        }
        return result;
    }

    public void logOffUser() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("UserToken");
        editor.commit();
    }
}
