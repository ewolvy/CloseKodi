package com.mooo.ewolvy.closekodi;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import ar.com.daidalos.afiledialog.FileChooserDialog;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    static final int PERMISSION_SHOW_FILE_DIALOG = 1;
    static final String PREFERENCES = "MY_PREFERENCES";

    String mCertificateFile;
    TextView certificateText;
    EditText mServer, mPort, mUsername, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.edit_certificate_button).setOnClickListener(this);

        mServer = (EditText) findViewById(R.id.edit_server);
        mPort = (EditText) findViewById(R.id.edit_port);
        mUsername = (EditText) findViewById(R.id.edit_username);
        mPassword = (EditText) findViewById(R.id.edit_password);
        certificateText = (TextView) findViewById(R.id.edit_certificate_text);

        SharedPreferences preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        mServer.setText(preferences.getString(getString(R.string.preference_server), "https://"));
        mPort.setText(preferences.getString(getString(R.string.preference_port), "0"));
        mUsername.setText(preferences.getString(getString(R.string.preference_username), ""));
        mPassword.setText(preferences.getString(getString(R.string.preference_password), ""));

        mCertificateFile = preferences.getString(getString(R.string.preference_certificate), "");
        certificateText.setText(mCertificateFile);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.edit_certificate_button) {
            // Check if have permission to read files.
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                // We don't have permission. Ask for it.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_SHOW_FILE_DIALOG);
            } else {
                // We have permission. Show dialog to choose file.
                showFileDialog(this);
            }
        }
    }

    public void showFileDialog(final Context context) {
        String fullPath, path;
        FileChooserDialog dialog = new FileChooserDialog(context);
        fullPath = mCertificateFile;
        if (fullPath.equals(Environment.getExternalStorageState())) {
            path = fullPath;
        } else {
            File file = new File(fullPath);
            path = file.getParent();
        }
        dialog.loadFolder(path);
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {

            public void onFileSelected(Dialog source, File file) {
                source.hide();
                Toast toast = Toast.makeText(source.getContext(),
                        source.getContext().getString(R.string.settings_selected_file) + file.getAbsoluteFile(),
                        Toast.LENGTH_LONG);
                toast.show();
                mCertificateFile = file.getAbsolutePath();
                certificateText.setText(mCertificateFile);
            }

            public void onFileSelected(Dialog source, File folder, String name) {
                source.hide();
                Toast toast = Toast.makeText(source.getContext(),
                        "File created: " + folder.getName() + "/" + name,
                        Toast.LENGTH_LONG);
                toast.show();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mServer.getText().toString().equals("https://") ||
                Integer.parseInt(mPort.getText().toString()) == 0 ||
                mUsername.getText().toString().equals("") ||
                mPassword.getText().toString().equals("") ||
                mCertificateFile.equals("")){

        } else {
            SharedPreferences preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(getString(R.string.preference_server), mServer.getText().toString());
            editor.putInt(getString(R.string.preference_port), Integer.parseInt(mPort.getText().toString()));
            editor.putString(getString(R.string.preference_username), mUsername.getText().toString());
            editor.putString(getString(R.string.preference_password), mPassword.getText().toString());
            editor.putString(getString(R.string.preference_certificate), mCertificateFile);

            editor.apply();
            super.onBackPressed();
        }
    }
}
