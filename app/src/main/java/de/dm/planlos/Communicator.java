package de.dm.planlos;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static de.dm.planlos.Nina.LOG_TAG;

public class Communicator {
    static void setPostRequestContent(HttpURLConnection conn,
                                      JSONObject jsonObject) throws IOException {
        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(MainActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }

    static void publishPosition(DmBeacon dmBeacon) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
            try {
                Log.i(LOG_TAG, "publishing position ...");
                URL url = new URL("http://10.0.20.87:8080/publishPosition");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("person", "John Wayne (interessiert's)");
                jsonObject.put("id", dmBeacon.id);
                setPostRequestContent(connection, jsonObject);
                connection.connect();
                int responseCode = connection.getResponseCode();
                Log.i(LOG_TAG, String.format("Publish Position Response code: %s", responseCode));
            } catch (IOException | JSONException e) {
                Log.e(LOG_TAG, String.format("Error: %s -- Message: %s", e.getClass().getSimpleName(), e.getMessage()));
                //TODO: handle this
            }
                return null;
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static void getExistingBeacons() {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean interrupted = false;
                while (!interrupted) {
                    try {
                        Log.i(LOG_TAG, "Finding existing BEACONS...");
                        URL url = new URL("http://10.0.20.87:8080/findBeacons");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        Log.d(LOG_TAG, String.format("Response code for findBeacons: %s", responseCode));
                        if (responseCode == 200) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            br.close();
                            JSONArray json = new JSONArray(sb.toString());
                            Log.i(LOG_TAG, "JSON length: " + json.length());
                            for (int i = 0; i < json.length(); i++) {
                                Integer id = json.getJSONObject(i).getInt("id");
                                String name = json.getJSONObject(i).getString("description");
                                Log.d(LOG_TAG, String.format("Beacon from DB: %s (%s)", name, id));
                                Nina.BEACONS.put(id, name);
                            }
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(LOG_TAG, String.format("Error: %s -- Message: %s", e.getClass().getSimpleName(), e.getMessage()));
                        //TODO: handle this
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
                return null;
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
