package de.dm.planlos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Nina {
    public static final String LOG_TAG = "NINa";
    public static final String DM_BEACON_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    public static final boolean LOG_BEACONS = false;
    public static ConcurrentHashMap<Integer, String> BEACONS = new ConcurrentHashMap<>();

    public static String getAllBeaconsString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, String> beacon : BEACONS.entrySet()) {
            sb.append(String.format("%s (%s)\n", beacon.getValue(), beacon.getKey()));
        }
        return sb.toString();
    }
}
