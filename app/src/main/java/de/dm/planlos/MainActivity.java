package de.dm.planlos;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.util.Collection;

import static de.dm.planlos.Nina.LOG_TAG;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("NINa needs to know where you are.");
                builder.setMessage("Please grant location access so that NINa can tell you and your colleagues where you are.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(dialog -> {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                });
                builder.show();
            }
        }

        Log.i(LOG_TAG, "getting beacon manager");
        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        //initBackgroundGedöns();
        beaconManager.bind(this);

        Communicator.getExistingBeacons();
    }

    private void initBackgroundGedöns() {
        Notification.Builder builder = new Notification.Builder(this, "NINa");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Scanning for Beacons");
        Intent intent = new Intent(this, MonitoringActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
        beaconManager.setEnableScheduledScanJobs(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "coarse location permission granted");
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited.");
                    builder.setMessage("Since location access has not been granted, NINa cannot tell you or your colleagues where you are.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener((dialog) -> {
                        //TODO: do we need to do something here?
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i(LOG_TAG, "BeaconService connected --> Adding notifiers ...");
        beaconManager.addRangeNotifier(this::handleNotification);
        startRanging();
    }

    private void startRanging() {
        try {
            Region wildcardRegion = new Region("myMonitoringUniqueId", Identifier.parse("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), null, null);
            beaconManager.startRangingBeaconsInRegion(wildcardRegion);
        } catch (RemoteException e) {
            //TODO: handle this
        }
    }

    private void handleNotification(Collection<Beacon> beacons, Region region) {
        logBeaconInfos(beacons);
        FoundBeacons foundBeacons = new FoundBeacons(beacons);
        try {
            DmBeacon closestBeacon = foundBeacons.getClosestBeacon();
            handleClosestBeacon(closestBeacon);
        } catch (FoundBeacons.NoBeaconFoundException e) {
            Log.w(LOG_TAG, "No Beacon found ...");
        }
    }

    private void handleClosestBeacon(DmBeacon closestBeacon) {
        if (Nina.LOG_BEACONS) {
            Log.i(LOG_TAG, "-----------------------------------------------");
            Log.i(LOG_TAG, String.format("Closest beacon: " + closestBeacon.id));
            Log.i(LOG_TAG, "-----------------------------------------------");
        }
        TextView textView = findViewById(R.id.message);
        String beaconName = Integer.toString(closestBeacon.id);
        if (Nina.BEACONS.containsKey(closestBeacon.id)) {
            beaconName = Nina.BEACONS.get(closestBeacon.id);
        }
        textView.setText(String.format("Closest beacon: %s\nDistance: %s\n\n", beaconName, closestBeacon.distance) + Nina.getAllBeaconsString());
        Communicator.publishPosition(closestBeacon);
    }

    private static void logBeaconInfos(Collection<Beacon> beacons) {
        if (Nina.LOG_BEACONS) {
            Log.i(LOG_TAG, String.format("I see 0 dead people and %s beacon(s)", beacons.size()));
            beacons.forEach(beacon -> {
                Log.i(LOG_TAG, String.format("ADD: %s, NAME %s, DISTANCE %s, TX: %s, Id1: %s",
                        beacon.getBluetoothAddress(),
                        beacon.getBluetoothName(),
                        beacon.getDistance(),
                        beacon.getTxPower(),
                        beacon.getId1().toString()
                ));
                beacon.getIdentifiers().forEach(ident -> {
                    Log.i(LOG_TAG, "Identifier: " + ident.toString());
                });
                beacon.getDataFields().forEach(df -> {
                    Log.i(LOG_TAG, "Data: " + df.toString());
                });
                beacon.getExtraDataFields().forEach(df -> {
                    Log.i(LOG_TAG, "AddData: " + df.toString());
                });
            });
        }
    }
}
