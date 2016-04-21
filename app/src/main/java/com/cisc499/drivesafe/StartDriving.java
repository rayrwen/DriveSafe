package com.cisc499.drivesafe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class StartDriving extends AppCompatActivity {

    String surrounding_level, os_level, brake_level, steering_level, accel_level, risk_level, pothole_level, slipper_level;
    String warningMessage;
    TextView connection, overspeed, brake, steering, accel, surrounding, risk, pothole, slipper;
    int refresh_timeout = 10000;  // Auto refresh interval
    Handler handler = new Handler();
    AlertDialog.Builder builder;
    AlertDialog dialog;
    int refreshCount = 0;
    boolean warning, existDialog;


    final String DEBUG_TAG = "StartDriving";
    final String link_surrounding = "http://52.90.69.51/torque/surrounding.php";
    final String link_os = "http://52.90.69.51/torque/os_level.php";
    final String link_brake = "http://52.90.69.51/torque/brake_level.php";
    final String link_steering = "http://52.90.69.51/torque/angle_level.php";
    final String link_accel = "http://52.90.69.51/torque/accel_level.php";
    final String link_risk = "http://52.90.69.51/torque/risk_level.php";
    final String link_pothole = "http://52.90.69.51/torque/pothole_level.php";
    final String link_slipper = "http://52.90.69.51/torque/slipper_level.php";

    final String link_report_pothole = "http://52.90.69.51/torque/report_pothole.php";
    final String link_report_slipper = "http://52.90.69.51/torque/report_slipper.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_driving);

        connection = (TextView) findViewById(R.id.connection_status);
        overspeed = (TextView) findViewById(R.id.overspeed);
        brake = (TextView) findViewById(R.id.hard_braking);
        steering = (TextView) findViewById(R.id.aggressive_steering);
        accel = (TextView) findViewById(R.id.sudden_acceleration);
        surrounding = (TextView) findViewById(R.id.aggressive_percent);

        pothole = (TextView) findViewById(R.id.potholes);
        slipper = (TextView) findViewById(R.id.slipperiness);
        risk = (TextView) findViewById(R.id.overall_risk);

    }

    public void autoRefresh(View view) {
        // Start the initial runnable task by posting through the handler
        handler.post(auto);
    }

    public void stopAutoRefresh(View view) {
        connection.setText(R.string.stop_refresh);
        handler.removeCallbacks(auto);
    }

    public void reportPothole(View view) {
        handler.post(potholeReportRunnable);
    }

    public void reportSlipper(View view) {
        handler.post(slipperReportRunnable);
    }

    private Runnable auto = new Runnable() {
        @Override
        public void run() {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new DownloadWebpageTask().execute(link_surrounding, link_os, link_brake, link_steering,
                        link_accel, link_risk, link_pothole, link_slipper);
            } else {
                connection.setText(R.string.no_connection);
            }
            Log.d("Handlers", "Called on main thread");
            // Repeat this the same runnable code block again another refresh_timeout seconds
            handler.postDelayed(auto, refresh_timeout);
        }
    };

    private Runnable potholeReportRunnable = new Runnable() {
        @Override
        public void run() {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new AccessWebpageTask().execute(link_report_pothole);
            } else {
                connection.setText(R.string.no_connection);
            }
            Log.d("Handlers", "Called on main thread");
        }
    };

    private Runnable slipperReportRunnable = new Runnable() {
        @Override
        public void run() {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new AccessWebpageTask().execute(link_report_slipper);
            } else {
                connection.setText(R.string.no_connection);
            }
            Log.d("Handlers", "Called on main thread");
        }
    };

//
//    // When user clicks button, calls AsyncTask.
//    // Before attempting to fetch the URL, makes sure that there is a network connection.
//    public void refresh(View view) {
//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected()) {
//            connection.setText("Connecting...");
//            new DownloadWebpageTask().execute(link_surrounding, link_os, link_brake, link_steering,
//                    link_accel);
//        } else {
//            connection.setText("No connection");
//        }
//    }

    private class AccessWebpageTask extends AsyncTask<String, Void, String[]> {
        String[] results = new String[1];

        @Override
        protected String[] doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                String report_result = downloadUrl(urls[0]);
                results[0] = report_result;
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "Unable to retrieve web page. URL may be invalid.");
            }

            return results;
        }

        protected void onPostExecute(String[] results) {
            // Set connection textview to the corresponding output
            connection.setText(results[0]);
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String[]> {
        String[] results = new String[8];

        @Override
        protected String[] doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                surrounding_level = downloadUrl(urls[0]);
                os_level = downloadUrl(urls[1]);
                brake_level = downloadUrl(urls[2]);
                steering_level = downloadUrl(urls[3]);
                accel_level = downloadUrl(urls[4]);
                risk_level = downloadUrl(urls[5]);
                pothole_level = downloadUrl(urls[6]);
                slipper_level = downloadUrl(urls[7]);

                results[0] = surrounding_level;
                results[1] = os_level;
                results[2] = brake_level;
                results[3] = steering_level;
                results[4] = accel_level;
                results[5] = risk_level;
                results[6] = pothole_level;
                results[7] = slipper_level;

            } catch (IOException e) {
                Log.d(DEBUG_TAG, "Unable to retrieve web page. URL may be invalid.");
            }

            return results;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String[] results) {
            if (existDialog) {
                dialog.dismiss();
            }
            existDialog = false;
            refreshCount++;
            surrounding.setText(results[0]);
            overspeed.setText(results[1]);
            brake.setText(results[2]);
            steering.setText(results[3]);
            accel.setText(results[4]);
            risk.setText(results[5]);
            pothole.setText(results[6]);
            slipper.setText(results[7]);
            warningMessage = "";
            warning = false;

            // Change colour of text
            // Also set warning messages here
            if (results[0].substring(0, 4).equals("HIGH")) {
                surrounding.setTextColor(getResources().getColor(R.color.red));
                warningMessage = warningMessage.concat("Alert: many dangerous drivers around \n");
                warning = true;
            } else if (results[0].substring(0, 3).equals("LOW")) {
                surrounding.setTextColor(getResources().getColor(R.color.green));
            }

            if (results[1].substring(0, 4).equals("HIGH")) {
                overspeed.setTextColor(getResources().getColor(R.color.red));
                warningMessage = warningMessage.concat("Warning: you drive too fast! \n");
                warning = true;
            } else if (results[1].substring(0, 3).equals("LOW")) {
                overspeed.setTextColor(getResources().getColor(R.color.green));
            }

            if (results[2].substring(0, 4).equals("HIGH")) {
                brake.setTextColor(getResources().getColor(R.color.red));
                warningMessage = warningMessage.concat("Warning: you brake too fast! \n");
                warning = true;
            } else if (results[2].substring(0, 3).equals("LOW")) {
                brake.setTextColor(getResources().getColor(R.color.green));
            }

            if (results[3].substring(0, 4).equals("HIGH")) {
                steering.setTextColor(getResources().getColor(R.color.red));
                warningMessage = warningMessage.concat("Warning: you steer too fast! \n");
                warning = true;
            } else if (results[3].substring(0, 3).equals("LOW")) {
                steering.setTextColor(getResources().getColor(R.color.green));
            }

            if (results[4].substring(0, 4).equals("HIGH")) {
                accel.setTextColor(getResources().getColor(R.color.red));
                warningMessage = warningMessage.concat("Warning: you accelerate too fast! \n");
                warning = true;
            } else if (results[4].substring(0, 3).equals("LOW")) {
                accel.setTextColor(getResources().getColor(R.color.green));
            }

            // Special one here
            if (risk.getText().toString().substring(0, 5).equals("RISKY")) {
                risk.setBackgroundResource(R.color.red);
            } else if (risk.getText().toString().substring(0, 4).equals("SAFE")) {
                risk.setBackgroundResource(R.color.green);
            }

            if (results[6].substring(0, 4).equals("HIGH")) {
                pothole.setTextColor(getResources().getColor(R.color.red));
                warningMessage = warningMessage.concat("Alert: many potholes around \n");
                warning = true;
            } else if (results[6].substring(0, 3).equals("LOW")) {
                pothole.setTextColor(getResources().getColor(R.color.green));
            }

            if (results[7].substring(0, 4).equals("HIGH")) {
                slipper.setTextColor(getResources().getColor(R.color.red));
                warningMessage = warningMessage.concat("Alert: slippery road \n");
                warning = true;
            } else if (results[7].substring(0, 3).equals("LOW")) {
                slipper.setTextColor(getResources().getColor(R.color.green));
            }

            if (warning) {
                connection.setText(warningMessage);
                connection.setSelected(true);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            builder = new AlertDialog.Builder(StartDriving.this)
                                    .setTitle("Warning")
                                    .setMessage(warningMessage)
                                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert);
                            dialog = builder.show();
                            existDialog = true;
                        }
                    }
                });
            }
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
//            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    // React to end driving button
    public void viewUserProfile(View view) {
        Intent intent = new Intent(this, UserProfile.class);
        startActivity(intent);
    }
}
