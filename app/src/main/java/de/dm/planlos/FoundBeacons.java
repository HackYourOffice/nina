package de.dm.planlos;

import android.util.Log;
import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static de.dm.planlos.Nina.LOG_TAG;

public class FoundBeacons {
    private final List<DmBeacon> dmBeacons = new ArrayList<>();

    public FoundBeacons(Collection<Beacon> beacons) {
        beacons.forEach(beacon -> {
            try {
                dmBeacons.add(new DmBeacon(beacon));
            } catch (DmBeacon.InvalidBeaconException e) {
                Log.w(LOG_TAG, String.format("Did not add beacon to found beacons: %s", e.getMessage()));
            }
        });
        dmBeacons.sort(Comparator.comparingDouble(beacon -> beacon.distance));
    }

    public DmBeacon getClosestBeacon() throws NoBeaconFoundException {
        if (dmBeacons.size() == 0) {
            throw new NoBeaconFoundException();
        }
        return dmBeacons.get(0);
    }

    public static class NoBeaconFoundException extends Exception {
        private NoBeaconFoundException() {
            super("No DM Beacon found.");
        }
    }
}
