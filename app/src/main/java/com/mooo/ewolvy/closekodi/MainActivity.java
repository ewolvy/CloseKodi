package com.mooo.ewolvy.closekodi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    static final String PREFERENCES = "MY_PREFERENCES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeKodi();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(MainActivity.this,
                        SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void closeKodi(){
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        String server = preferences.getString(getString(R.string.preference_server), "");
        int port = preferences.getInt(getString(R.string.preference_port), 0);
        String username = preferences.getString(getString(R.string.preference_username), "");
        String password = preferences.getString(getString(R.string.preference_password), "");
        String certificate = preferences.getString(getString(R.string.preference_certificate), "");

        if (server.equals("") ||
                port == 0 ||
                username.equals("") ||
                password.equals("") ||
                certificate.equals("")){
            Toast.makeText(this, R.string.settings_not_set, Toast.LENGTH_LONG).show();
        } else {
            SSLServer myServer = new SSLServer(server,
                    port,
                    username,
                    password,
                    certificate);
            myServer.execute(this);
        }
    }
}
