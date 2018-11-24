package de.dm.planlos;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
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
        AsyncTask.execute(() -> {
            try {
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
        });
    }

    static void getExistingBeacons() {
        AsyncTask.execute(() -> {
            try {
                URL url = new URL("http://10.0.20.87:8080/findBeacons");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int responseCode = connection.getResponseCode();
                Log.i(LOG_TAG, String.format("Response code for findBeacons: %s", responseCode));
            } catch (IOException e) {
                Log.e(LOG_TAG, String.format("Error: %s -- Message: %s", e.getClass().getSimpleName(), e.getMessage()));
                //TODO: handle this
            }
        });
    }
}
