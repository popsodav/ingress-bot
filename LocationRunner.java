import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;


public class LocationRunner {
    private BufferedReader br;
    private ClientWrapper cw;
    private Location currentLocation;
    private HashMap<Integer, Location> locations;
    private Integer lastPortalHash;

    public LocationRunner(FileReader fileReader, ClientWrapper clientWrapper) throws Exception {
        this.br = new BufferedReader(fileReader);
        this.cw = clientWrapper;
        this.locations = new HashMap<Integer, Location>();
    }

    private Location getNearestLocation() {
        Location loc = null;
        Double minDist = null;
        Integer hash = 0;

        for (Map.Entry<Integer, Location> entry : locations.entrySet()) {
            if (entry.equals(currentLocation)) {
                continue;
            }
            Double dist = S2Wrapper.GreatEarthDistance(currentLocation, entry.getValue());
            if (dist > 0.0) {
                if (minDist == null) {
                    minDist = dist;
                } else {
                    if (dist < minDist) {
                        loc = new Location();
                        loc.setLat(entry.getValue().getLat());
                        loc.setLng(entry.getValue().getLng());
                        hash = entry.getKey();
                    }
                }
            }
        }

        lastPortalHash = hash;
        return loc;
    }

    private void updateLocs() throws Exception {
        String curLine;
        Location loc = null;
        DebugHandler.debugInfo("Updating locations...");
        while ((curLine = br.readLine()) != null) {
            if (locations.get(curLine.hashCode()) == null) {
                loc = new Location();
                String[] tmp = curLine.split(",");
                loc.setLat(Double.parseDouble(tmp[0]));
                loc.setLng(Double.parseDouble(tmp[1]));
                locations.put(curLine.hashCode(), loc);
            }
        }
    }

    private Location getStartLocation() {
        Random random = new Random();
        List<Integer> keys = new ArrayList<Integer>(locations.keySet());
        Integer randomKey = keys.get(random.nextInt(keys.size()));
        Location loc = locations.get(randomKey);
        System.out.println("Set start location: " + loc.toString());
        DebugHandler.debugInfo("Set start location: " + loc.toString());
        lastPortalHash = randomKey;
        return loc;
    }

    public void run() throws Exception {

        while (true) {
            if(locations.size() == 0)
            {
                updateLocs();
            }
            if(currentLocation == null)
            {
                currentLocation = getStartLocation();
                continue;
            }

            Location nearLoc = getNearestLocation();
            System.out.println("Moving to location: " + nearLoc.toString());
            DebugHandler.debugInfo("Moving to location: " + nearLoc.toString());

            //get distance in meters
            Double dist = S2Wrapper.GreatEarthDistance(currentLocation, nearLoc);
            TransitHandler th = new TransitHandler(currentLocation, nearLoc);
            th.start();
            int waitTimeSeconds = (int) (dist / 5.0);

            System.out.println("Waiting " + waitTimeSeconds + " seconds to arrive.");
            DebugHandler.debugInfo("Waiting " + waitTimeSeconds + " seconds to arrive.");
            th.join();

            System.out.println("Arrived!");
            DebugHandler.debugInfo("Arrived!");

            locations.remove(lastPortalHash);
            currentLocation = nearLoc;
            cw.newLocation(nearLoc);

            cw.printLocalHackablePortalNames();
            cw.hackLocalPortals();

            cw.getInventory();
            Thread.sleep(100);
        }
    }
}
