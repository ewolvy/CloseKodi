package com.mooo.ewolvy.closekodi;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

class SSLServer {
    // Constants
    static private final String LOG_TAG = "SSLServer";

    // Variables
    private String address;
    private int port;
    private String username;
    private String password;
    private String certificate;

    // Constructor
    SSLServer(String address, int port, String username, String password, String certificate) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
        this.certificate = certificate;
    }

    private static HttpsURLConnection setUpHttpsConnection(String urlString,
                                                           String fileName) {
        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            /*
            // My CRT file that I put in the assets folder
            // I got this file by following these steps:
            // * Go to https://littlesvr.ca using Firefox
            // * Click the padlock/More/Security/View Certificate/Details/Export
            // * Saved the file as littlesvr.crt (type X.509 Certificate (PEM))
            // The MainActivity.context is declared as:
            // public static Context context;
            // And initialized in MainActivity.onCreate() as:
            // MainActivity.context = getApplicationContext();
            InputStream caInput = context.getResources().openRawResource(R.raw.ewolvy);
            Certificate ca = cf.generateCertificate(caInput);
            // System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            */

            // Check file availability
            String state = Environment.getExternalStorageState(new File(fileName));
            if (Objects.equals(state, Environment.MEDIA_MOUNTED) ||
                    Objects.equals(state, Environment.MEDIA_MOUNTED_READ_ONLY)){
                // Use certificate from file
                FileInputStream fis = new FileInputStream(fileName);
                BufferedInputStream bis = new BufferedInputStream(fis);

                if (bis.available() <= 0){
                    return null;
                } else {
                    Certificate ca = cf.generateCertificate(bis);

                    // Create a KeyStore containing our trusted CAs
                    String keyStoreType = KeyStore.getDefaultType();
                    KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", ca);

                    // Create a TrustManager that trusts the CAs in our KeyStore
                    String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                    tmf.init(keyStore);

                    // Create an SSLContext that uses our TrustManager
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, tmf.getTrustManagers(), null);

                    // Tell the URLConnection to use a SocketFactory from our SSLContext
                    URL url = new URL(urlString);
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

                    return urlConnection;
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Failed to establish SSL connection to server: " + ex.toString());
            return null;
        }
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    void execute(Context c){
        doConnection connection = new doConnection();
        connection.execute(c);
    }

    private class doConnection extends AsyncTask<Context, Void, String> {
        String fullAddress;
        Context currentContext;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fullAddress = address;
            fullAddress = fullAddress + ":";
            fullAddress = fullAddress + port;
            fullAddress = fullAddress + "/closeKodi/";
        }

        @Override
        protected String doInBackground(Context... contexts) {
            currentContext = contexts[0];
            HttpsURLConnection urlConnection = setUpHttpsConnection(fullAddress, certificate);
            String jsonResponse = "";
            try {
                if (urlConnection != null){
                    urlConnection.setReadTimeout(10000 /* milliseconds */);
                    urlConnection.setConnectTimeout(15000 /* milliseconds */);
                    urlConnection.setRequestMethod("GET");
                    String userCredentials = username;
                    userCredentials = userCredentials + ":";
                    userCredentials = userCredentials + password;
                    String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes(), 0);
                    urlConnection.setRequestProperty ("Authorization", basicAuth);
                    urlConnection.connect();
                    if (urlConnection.getResponseCode() == 200) {
                        InputStream inputStream = urlConnection.getInputStream();
                        try{
                            jsonResponse = readFromStream(inputStream);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "IO Exception: " + e.toString());
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null){
                Toast toast = Toast.makeText(currentContext, result, Toast.LENGTH_SHORT);
                toast.show();
            }else{
                Toast toast = Toast.makeText(currentContext, currentContext.getString(R.string.connection_error), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}