package seu.cse.kai.wifinavi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends AppCompatActivity {

    ListView apListView;
    ArrayAdapter<AP> aa;
    ArrayList<AP> aps = new ArrayList<>();

    boolean pause = false;

    static final private int MENU_UPDATE = Menu.FIRST;
    static final private int MENU_SITE_SURVEY = Menu.FIRST + 1;

    AP selectedAP;

    // preferences
    static final private int SHOW_PREFERENCES = 1;
    boolean autoUpdate = true;
    int updateFrequency = 10;

    APReceiver receiver;
    Intent apIntent;

    String targetSSID = "";
    static private String locationTag = "";
    static private boolean flag_site_survey = false;

    public class APReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadAPsFromProvider();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apListView = (ListView)this.findViewById(R.id.apListView);
        apListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedAP = aps.get(position);
                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                ad.setTitle("Setting Target");
                ad.setMessage("\r\n" +
                        "FORGET/CONFIRM A TARGET? \r\n" +
                        "\r\n" +
                        "**** NOTICE  \r\n" +
                        "* Old target will be forgotten. \r\n" +
                        "* Cancel to do nothing. \r\n" +
                        "**** ");
                ad.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        targetSSID = selectedAP.SSID;
                    }
                });
                ad.setNegativeButton("FORGET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        targetSSID = "";
                    }
                });
                ad.setCancelable(true);
                ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // do nothing
                    }
                });
                ad.show();
            }
        });

        int layoutID = android.R.layout.simple_list_item_1;
        aa = new ArrayAdapter<AP>(this, layoutID, aps);
        apListView.setAdapter(aa);

        updateFromPreferences();
        refreshAPs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.add(0, MENU_UPDATE, Menu.NONE, R.string.menu_update);
        menu.add(0, MENU_SITE_SURVEY, Menu.NONE, R.string.menu_site_survey);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case (MENU_UPDATE): {
                refreshAPs();
                return true;
            }
            case (MENU_SITE_SURVEY): {
                final EditText tagEditText = new EditText(this);
                tagEditText.setText(this.locationTag);
                AlertDialog.Builder tagBuilder = new AlertDialog.Builder(this);
                tagBuilder.setTitle("Server");
                tagBuilder.setIcon(android.R.drawable.ic_dialog_info);
                tagBuilder.setMessage("Tips: \r\n" +
                                " 1 - Input the location tag; \r\n" +
                                " 2 - START or STOP"
                );
                tagBuilder.setView(tagEditText);
                tagBuilder.setCancelable(true);
                tagBuilder.setPositiveButton("START", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        locationTag = tagEditText.getText().toString();
                        flag_site_survey = true;
                    }
                });
                tagBuilder.setNegativeButton("STOP", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        locationTag = "";
                        flag_site_survey = false;
                    }
                });
                tagBuilder.show();
                return true;
            }
            case (R.id.action_settings): {
                Intent i = new Intent(this, Preferences.class);
                startActivityForResult(i, SHOW_PREFERENCES);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_PREFERENCES)
            if (resultCode == Activity.RESULT_OK) {
                updateFromPreferences();
                refreshAPs();
            }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        stopService(apIntent);
        super.onPause();
    }

    @Override
    protected void onResume() {
        IntentFilter filter;
        filter = new IntentFilter(APService.UPDATE);
        receiver = new APReceiver();
        registerReceiver(receiver, filter);
        loadAPsFromProvider();
        startService(apIntent);
        super.onResume();
    }

    private void refreshAPs() {
        apIntent = new Intent(this, APService.class);
        startService(apIntent);
    }

    private void updateFromPreferences() {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        autoUpdate = prefs.getBoolean(Preferences.PREF_AUTO_UPDATE, true);
        updateFrequency = prefs.getInt(Preferences.PREF_UPDATE_FREQUENCY, 10);
    }

    private void loadAPsFromProvider() {
        FileOutputStream fos = null;
        if (this.flag_site_survey && this.locationTag != null) {
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                String FILE_NAME = locationTag + ".txt";
                String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
                try {
                    fos  = new FileOutputStream(dir + File.separator + FILE_NAME, true);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String time = sdf.format(new java.util.Date());
                    String to_write = "t = " + time + ";";
                    fos.write(to_write.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        aps.clear();
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(APContentProvider.CONTENT_URI, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                String ssid = c.getString(APContentProvider.SSID_COLUMN);
                int rssi = c.getInt(APContentProvider.RSSI_COLUMN);
                AP ap = new AP(ssid, rssi,"mac");
                aps.add(ap);
                if (this.flag_site_survey) {
                    try {
                        String to_write = ap.SSID + ":" + ap.RSSI + ";";
                        fos.write(to_write.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (isTarget(ap))
                    saveToFile(ap);

            }
            while (c.moveToNext());
        }

        // sort by target & rssi
        Collections.sort(aps, new Comparator<AP>() {
            @Override
            public int compare(AP lhs, AP rhs) {
                if (isTarget(lhs))
                    return -1;
                else if (isTarget(rhs))
                    return 1;
                else
                    return 0 - new Integer(lhs.RSSI).compareTo(new Integer(rhs.RSSI));
            }
        });
        aa.notifyDataSetChanged();

        if (fos != null)
            try {
                fos.write(new String("\r\n").getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private boolean isTarget(AP ap) {
        return (ap.SSID.equals(targetSSID));
    }

    private void saveToFile(AP _ap) {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            String FILE_NAME = _ap.SSID + ".txt";
            String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
            try {
                FileOutputStream fos = new FileOutputStream(dir + File.separator + FILE_NAME, true);
                String to_write = _ap.RSSI + "\r\n";
                fos.write(to_write.getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
