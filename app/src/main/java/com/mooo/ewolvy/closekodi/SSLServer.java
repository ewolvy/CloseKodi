package com.mooo.ewolvy.closekodi;

import android.os.Environment;
import android.util.Log;

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
}