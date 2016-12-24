package seu.cse.kai.wifinavi;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Dell on 2015/5/6.
 */
public class APService extends Service {

    private WifiManager wifi;
    private Timer updateTimer;
    public static final String UPDATE = "Update";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoUpdate = prefs.getBoolean(Preferences.PREF_AUTO_UPDATE, true);
        int updateFreq = prefs.getInt(Preferences.PREF_UPDATE_FREQUENCY, 10);

        /*SharedPreferences prefs = getSharedPreferences(Preferences.MY_PREFERENCE, Activity.MODE_PRIVATE);
        boolean autoUpdate = prefs.getBoolean(Preferences.PREF_AUTO_UPDATE, true);
        int updateFreq = prefs.getInt(Preferences.PREF_UPDATE_FREQUENCY, 10);*/

        updateTimer.cancel();
        if (autoUpdate) {
            updateTimer = new Timer("APUpdats");
            updateTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    refreshAPs();
                }
            }, 0, updateFreq * 1000);
        }
        else {
            refreshAPs();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        updateTimer = new Timer("apUpdates");
        // managing wifi
        String service = Context.WIFI_SERVICE;
        wifi = (WifiManager)getSystemService(service);
        if (!wifi.isWifiEnabled())
            if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
                wifi.setWifiEnabled(true);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                List<ScanResult> results = wifi.getScanResults();
                myScanResultHandler(results);
            }
            public void onPause() {
                unregisterReceiver(this);
            }
            public void onStop() {
                unregisterReceiver(this);
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addNewAP(AP _ap) {
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(APContentProvider.KEY_SSID, _ap.SSID);
        values.put(APContentProvider.KEY_RSSI, _ap.RSSI);
        cr.insert(APContentProvider.CONTENT_URI, values);
    }

    private void announceUpdate() {
        Intent intent = new Intent(UPDATE);
        sendBroadcast(intent);
    }

    private boolean refreshAPs() {
        ContentResolver cr = getContentResolver();
        cr.delete(APContentProvider.CONTENT_URI, null, null);
        // scanning for hotspots
        wifi.startScan();
        return true;
    }

    private void myScanResultHandler(List<ScanResult> scanResults) {
        for (ScanResult result : scanResults) {
            addNewAP(new AP(result.SSID, result.level, result.BSSID));
        }
        announceUpdate();
    }

}
