package com.cisc499.drivesafe;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Intersection extends AppCompatActivity {

    String front_level, left_level, right_level;
    TextView front, left, right;
    Handler handler = new Handler();

    final String DEBUG_TAG = "Intersection";
    final String link_front = "http://52.90.69.51/torque/surrounding_front.php";
    final String link_left = "http://52.90.69.51/torque/surrounding_left.php";
    final String link_right = "http://52.90.69.51/torque/surrounding_right.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intersection);

        front = (TextView) findViewById(R.id.front);
        left = (TextView) findViewById(R.id.left);
        right = (TextView) findViewById(R.id.right);

    }

    public void refresh(View view) {
        handler.post(intersectionUpdate);
    }


    private Runnable intersectionUpdate = new Runnable() {
        @Override
        public void run() {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
//                connection.setText("Connecting...");
                new DownloadWebpageTask().execute(link_front, link_left, link_right);
            } else {
//                connection.setText("No connection");
            }
            Log.d("Handlers", "Called on main thread");
        }
    };


    private class DownloadWebpageTask extends AsyncTask<String, Void, String[]> {
        String[] results = new String[8];

        @Override
        protected String[] doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                front_level = downloadUrl(urls[0]);
                left_level = downloadUrl(urls[1]);
                right_level = downloadUrl(urls[2]);

                results[0] = front_level;
                results[1] = left_level;
                results[2] = right_level;

            } catch (IOException e) {
                Log.d(DEBUG_TAG, "Unable to retrieve web page. URL may be invalid.");
            }

            return results;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String[] results) {
            front.setText(results[0]);
            left.setText(results[1]);
            right.setText(results[2]);

//            connection.setText("Done");
            // Change colour of text
            // Also set warning messages here
            if (results[0].substring(0, 4).equals("HIGH")) {
                front.setTextColor(getResources().getColor(R.color.red));
            }

            if (results[1].substring(0, 4).equals("HIGH")) {
                left.setTextColor(getResources().getColor(R.color.red));
            }

            if (results[2].substring(0, 4).equals("HIGH")) {
                right.setTextColor(getResources().getColor(R.color.red));
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
}
