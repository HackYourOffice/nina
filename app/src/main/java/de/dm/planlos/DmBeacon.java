package de.dm.planlos;

import android.util.Log;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import static de.dm.planlos.Nina.LOG_TAG;

public class DmBeacon {
    public final int id;
    public final double distance;

    public DmBeacon(Beacon beacon) throws InvalidBeaconException {
        validate(beacon);
        id = calculateId(beacon);
        distance = beacon.getDistance();
    }

    private int calculateId(Beacon beacon) {
        byte[] majorBytes = getMajor(beacon).toByteArray();
        byte[] minorBytes = getMinor(beacon).toByteArray();
        return (toInt(majorBytes) << 16) + toInt(minorBytes);
    }

    private void validate(Beacon beacon) throws InvalidBeaconException {
        if (getBeaconId(beacon) == null) {
            throw new InvalidBeaconException(String.format("Beacon %s does not broadcast a UID", beacon.getBluetoothAddress()));
        }
        if (!isDmBeacon(beacon)) {
            throw new InvalidBeaconException(String.format("Beacon %s is not a DM beacon.", beacon.getBluetoothAddress()));
        }
        if (getMajor(beacon) == null) {
            throw new InvalidBeaconException(String.format("Beacon %s Major identifier is null.", beacon.getBluetoothAddress()));
        }
        if (getMinor(beacon) == null) {
            throw new InvalidBeaconException(String.format("Beacon %s Minor identifier is null", beacon.getBluetoothAddress()));
        }
    }

    private static int toInt(byte[] ba) {
        int value = 0;
        for (byte by : ba) {
            value = value << 8;
            value += Byte.toUnsignedInt(by);
        }
        return value;
    }

    public static boolean isDmBeacon(Beacon beacon) {
        Log.i(LOG_TAG, "IDEN: " + getBeaconId(beacon));
        return Nina.DM_BEACON_ID.equals(getBeaconId(beacon).toString());
    }

    private static Identifier getMinor(Beacon beacon) {
        return beacon.getIdentifier(2);
    }

    private static Identifier getMajor(Beacon beacon) {
        return beacon.getIdentifier(1);
    }

    private static Identifier getBeaconId(Beacon beacon) {
        return beacon.getIdentifier(0);
    }

    public static final class InvalidBeaconException extends Exception {
        private InvalidBeaconException(String message) {
            super(message);
        }
    }
}
