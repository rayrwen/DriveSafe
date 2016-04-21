package com.cisc499.drivesafe;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserProfile extends AppCompatActivity {

    String score_accel, score_angle, score_brake, score_overall, score_speed, score_trips;
    TextView accel, angle, brake, overall, speed, trips;

    final String DEBUG_TAG = "StartDriving";
    final String accelLink = "http://52.90.69.51/torque/score_accel.php";
    final String angleLink = "http://52.90.69.51/torque/score_angle.php";
    final String brakeLink = "http://52.90.69.51/torque/score_brake.php";
    final String overallLink = "http://52.90.69.51/torque/score_overall.php";
    final String speedLink = "http://52.90.69.51/torque/score_speed.php";
    final String tripsLink = "http://52.90.69.51/torque/score_trips.php";

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        accel = (TextView) findViewById(R.id.user_acceleration_score);
        brake = (TextView) findViewById(R.id.user_brake_score);
        angle = (TextView) findViewById(R.id.user_steering_score);
        overall = (TextView) findViewById(R.id.user_overall_score);
        speed = (TextView) findViewById(R.id.user_speed_score);
        trips = (TextView) findViewById(R.id.user_trips_made);

    }

    // React to start driving button
    public void startDriving(View view) {
        Intent intent = new Intent(this, StartDriving.class);
        startActivity(intent);
    }

//    // React to home button
//    public void goHome(View view) {
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//    }

    // React to refresh button
    public void refresh(View view) {
        handler.post(update);
    }

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
//                connection.setText("Connecting...");
                new DownloadWebpageTask().execute(accelLink, angleLink, brakeLink, overallLink, speedLink, tripsLink);
            } else {
//                connection.setText("No connection");
            }
            Log.d("Handlers", "Called on main thread");
        }
    };


    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String[]> {
        String[] results = new String[6];

        @Override
        protected String[] doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                score_accel = downloadUrl(urls[0]);
                score_angle = downloadUrl(urls[1]);
                score_brake = downloadUrl(urls[2]);
                score_overall = downloadUrl(urls[3]);
                score_speed = downloadUrl(urls[4]);
                score_trips = downloadUrl(urls[5]);

                results[0] = score_accel;
                results[1] = score_angle;
                results[2] = score_brake;
                results[3] = score_overall;
                results[4] = score_speed;
                results[5] = score_trips;


            } catch (IOException e) {
                Log.d(DEBUG_TAG, "Unable to retrieve web page. URL may be invalid.");
            }

            return results;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String[] results) {
            accel.setText(results[0]);
            angle.setText(results[1]);
            brake.setText(results[2]);
            overall.setText(results[3]);
            speed.setText(results[4]);
            trips.setText(results[5]);

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
}
