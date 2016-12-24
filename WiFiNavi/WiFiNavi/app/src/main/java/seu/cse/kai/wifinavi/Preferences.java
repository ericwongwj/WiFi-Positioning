package seu.cse.kai.wifinavi;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


public class Preferences extends AppCompatActivity {

    CheckBox cb_autoUpdate;
    EditText et_updateFrequency;

    public static final String MY_PREFERENCE = "MY_PREFERENCE";
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_UPDATE_FREQUENCY = "PREF_UPDATE_FREQUENCY";
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        cb_autoUpdate = (CheckBox) findViewById(R.id.checkBox_auto_update);
        et_updateFrequency = (EditText) findViewById(R.id.edittext_update_frequency);

        Context context = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        updateUIFromPreferences();

        Button okButton = (Button) findViewById(R.id.button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                Preferences.this.setResult(RESULT_OK);
                finish();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.this.setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preferences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUIFromPreferences() {
        boolean autoUpdate = prefs.getBoolean(PREF_AUTO_UPDATE, true);
        int updateFrequency = prefs.getInt(PREF_UPDATE_FREQUENCY, 10);

        cb_autoUpdate.setChecked(autoUpdate);
        et_updateFrequency.setText(String.valueOf(updateFrequency));
    }

    private void savePreferences() {
        boolean autoUpdate = cb_autoUpdate.isChecked();
        int updateFrequency = Integer.parseInt(et_updateFrequency.getText().toString());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_AUTO_UPDATE, autoUpdate);
        editor.putInt(PREF_UPDATE_FREQUENCY, updateFrequency);
        editor.commit();
    }
}
