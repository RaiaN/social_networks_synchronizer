package sns;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import com.example.SocialNetworksSynchronizer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SettingsActivity extends Activity {
    HashMap <String, Switch> vkSwitches = new HashMap<String, Switch>();
    HashMap <String, Switch> fbSwitches = new HashMap<String, Switch>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        vkSwitches.put(Requests.VK_BIRTHDATE, (Switch)findViewById(R.id.birthday_switch));
        vkSwitches.put(Requests.VK_COUNTRY, (Switch)findViewById(R.id.address_switch));
        vkSwitches.put(Requests.VK_CONTACTS, (Switch)findViewById(R.id.phone_numbers_switch));
        vkSwitches.put(Requests.VK_SERVICES, (Switch)findViewById(R.id.services_switch));
        vkSwitches.put(Requests.VK_EDUCATION, (Switch)findViewById(R.id.education_switch));

        fbSwitches.put(Requests.FB_BIRTHDATE, (Switch)findViewById(R.id.birthday_switch_fb));
        fbSwitches.put(Requests.FB_EDUCATION, (Switch)findViewById(R.id.education_switch_fb));
        fbSwitches.put(Requests.FB_ADDRESS, (Switch) findViewById(R.id.address_switch_fb));

        restoreSwitches();

        ((Button)findViewById(R.id.apply_settings_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
    }

    private void restoreVkSwitches() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<String> vkFields = new ArrayList<String>(Arrays.asList(Requests.VK_REQUEST_FIELDS.split(",")));
                for( String field: vkFields ) {
                    if( vkSwitches.get(field) != null ) {
                        vkSwitches.get(field).setChecked(true);
                    }
                }
            }
        });
    }

    private void restoreFbSwitches() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<String> fbFields = new ArrayList<String>(Arrays.asList(Requests.FB_REQUEST_FIELDS.split(",")));
                for( String field: fbFields ) {
                    if( fbSwitches.get(field) != null ) {
                        fbSwitches.get(field).setChecked(true);
                    }
                }
            }
        });
    }

    private void restoreSwitches() {
        restoreVkSwitches();
        restoreFbSwitches();
    }

    private void saveVkSettings() {
        List<String> fieldsList = new ArrayList<String>();
        fieldsList.add(Requests.VK_PHOTO);

        for(String field: Requests.VK_ALL) {
            if( vkSwitches.get(field) != null && vkSwitches.get(field).isChecked() ) {
                if( field.compareTo(Requests.VK_COUNTRY) == 0 ) {
                    fieldsList.add(Requests.VK_COUNTRY);
                    fieldsList.add(Requests.VK_CITY);
                } else {
                    fieldsList.add(field);
                }
            }
        }

        String fields = TextUtils.join(",", fieldsList);
        Requests.VK_REQUEST_FIELDS = fields;
    }

    private void saveFbSettings() {
        List<String> fieldsList = new ArrayList<String>();
        fieldsList.add(Requests.FB_NAME);
        fieldsList.add(Requests.FB_PHOTO);

        for(String field: Requests.FB_ALL) {
            if( fbSwitches.get(field)!= null && fbSwitches.get(field).isChecked() ) {
                fieldsList.add(field);
            }
        }

        String fields = TextUtils.join(",", fieldsList);
        Requests.FB_REQUEST_FIELDS = fields;
    }

    private void saveSettings() {
        saveVkSettings();
        saveFbSettings();
        finish();
    }
}
