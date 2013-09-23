import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2LatLngRect;
import com.google.common.geometry.S2RegionCoverer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;

public class ClientWrapper {
    private static final String clientID = "2013-09-10T22:08:32Z 904499d6f84a opt";
    private static final String clientVersion = "1.35.1";
    //Final connection strings for initializing a connection
    public final String baseURL = "https://m-dot-betaspike.appspot.com";
    public ArrayList<Portal> localPortals = new ArrayList<Portal>();
    public ArrayList<Portal> localHackablePortals = new ArrayList<Portal>();
    public Player player;
    //Array list for portals?
    private ArrayList<EnergyGlob> localEnergyGlobs = new ArrayList<EnergyGlob>();
    private ArrayList<EnergyGlob> localEdibleGlobs = new ArrayList<EnergyGlob>();
    private ArrayList<S2CellId> localCellIds = new ArrayList<S2CellId>();
    private S2LatLng currentLocation = new S2LatLng();
    private String authCookie;
    private String nickname;
    private String xsrfToken;
    private long syncTimestamp;

    public ClientWrapper(String authCookie, Player player) {
        this.player = player;
        this.authCookie = authCookie;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // handshake()
    // Performs a secure handshake with the remote google server and collects connection information
    // for use with later messages
    // Side Effects : nickname, xsrfToken, syncTimestamp
    //////////////////////////////////////////////////////////////////////////////////////////////////
    public void handshake() throws Exception {
        Properties prop = new Properties();

        JSONObject jb = new JSONObject();
        jb.put("nemesisSoftwareVersion", clientID);
        jb.put("deviceSoftwareVersion", clientVersion);

        String nemesisDeviceID = jb.toJSONString();

        prop.load(new FileInputStream("config.properties"));
        System.setProperty("https.proxyHost", prop.getProperty("HTTPS_PROXY"));
        System.setProperty("https.proxyPort", prop.getProperty("HTTPS_PROXY_PORT"));
        System.setProperty("http.proxyHost", prop.getProperty("HTTP_PROXY"));
        System.setProperty("http.proxyPort", prop.getProperty("HTTP_PROXY_PORT"));

        DebugHandler.debugln("Handshaking...");

        //Setup the URL connection
        String url = baseURL + "/handshake?json=" + URLEncoder.encode(nemesisDeviceID, "UTF-8");
        URL handshakeURL = new URL(url);
        URLConnection handshakeCon = handshakeURL.openConnection();
        handshakeCon.setRequestProperty("Cookie", authCookie);
        handshakeCon.setRequestProperty("User-Agent", "Nemesis (gzip)");

        //Use a buffered reader to read the json response
        BufferedReader br = new BufferedReader(new InputStreamReader(handshakeCon.getInputStream()));
        String response = br.readLine();
        response = response.substring(9); // This removes the while(1); from the begining of googles response

        br.close();

        //System.out.println(response);
        DebugHandler.debugln(response);

        //Setup the JSON reader
        JSONParser jp = new JSONParser();
        JSONObject obj = (JSONObject) jp.parse(response);
        JSONObject result = (JSONObject) (obj.get("result"));

        //Read Strings from JSON objects
        this.nickname = (String) result.get("nickname");
        this.xsrfToken = (String) result.get("xsrfToken");
        String syncTimestampSTR = (String) ((JSONObject) result.get("initialKnobs")).get("syncTimestamp");
        this.syncTimestamp = Long.parseLong(syncTimestampSTR, 10);

        //Give results to the debug handler
        DebugHandler.debugln("\tNickname: " + nickname);
        DebugHandler.debugln("\tSyncTime: " + syncTimestampSTR);
        DebugHandler.debugln("\tXSRF-Tok: " + xsrfToken);
        DebugHandler.debugln("\tSuccess!");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // getObjectsInCells()
    // Updates the ClientWrapper object with a new set of energyGlobs based on it's current cells
    // by querying the remote server for all objects in the area
    // TODO : Add parsing of more than just energy globs (get portal information)
    // Side Effects : localEnergyGlobs
    //////////////////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    public void getObjectsInCells() throws Exception {

        //Begin setting up JSON Objects
        LinkedList<String> ll = new LinkedList<String>();
        JSONArray cellsAsHex = new JSONArray();
        JSONArray dates = new JSONArray();
        JSONArray energyGlobGuids = new JSONArray();
        JSONObject main = new JSONObject();
        JSONObject params = new JSONObject();
        JSONObject clientBasket = new JSONObject();

        //Add local cells and 0 dates to arrays
        for (int i = 0; i < this.localCellIds.size(); i++) {
            cellsAsHex.add(Long.toHexString(this.localCellIds.get(i).id()));
            dates.add(0);
        }

        //Setup edible globs here
        for (int i = 0; i < this.localEdibleGlobs.size(); i++)
            energyGlobGuids.add(localEdibleGlobs.get(i).name);
        //TODO!

        //Setup the json message structure
        clientBasket.put("clientBlob", null);
        params.put("cellsAsHex", cellsAsHex);
        params.put("dates", dates);
        params.put("clientBasket", clientBasket);
        params.put("cells", null);
        params.put("energyGlobGuids", energyGlobGuids);
        params.put("playerLocation", S2Wrapper.encodeLocation(this.currentLocation));
        params.put("knobSyncTimestamp", this.syncTimestamp);
        main.put("params", params);

        //Setup the json connection
        URL getObjectsURL = new URL(baseURL + "/rpc/gameplay/getObjectsInCells");
        URLConnection getObjectsCon = getObjectsURL.openConnection();
        getObjectsCon.setRequestProperty("Cookie", authCookie);
        getObjectsCon.setRequestProperty("X-XsrfToken", this.xsrfToken);
        getObjectsCon.setDoOutput(true);

        //Setup and use the writer
        OutputStreamWriter out = new OutputStreamWriter(getObjectsCon.getOutputStream());
        out.write(main.toString());  //Write our json object to the connection
        out.close();

        //Setup and use the reader
        BufferedReader br = new BufferedReader(new InputStreamReader(getObjectsCon.getInputStream()));
        String line = br.readLine();
        br.close();

        //System.out.print("\n\n\n" + line + "\n\n\n");

        //Decode the (interesting parts of the) json response
        JSONParser jp = new JSONParser();
        JSONObject obj = (JSONObject) jp.parse(line);
        JSONObject gameBasket = (JSONObject) obj.get("gameBasket");
        JSONArray gameEntities = (JSONArray) gameBasket.get("gameEntities"); //here we grab the portal entities array
        JSONArray responseEnergyGlobGuids = (JSONArray) (gameBasket.get("energyGlobGuids"));

        //Update our localEnergyGlobs array list
        for (int i = 0; i < responseEnergyGlobGuids.size(); i++)
            localEnergyGlobs.add(new EnergyGlob(responseEnergyGlobGuids.get(i).toString()));

        //System.out.println(gameEntities);    
        //Update our localPortals array list
        for (int i = 0; i < gameEntities.size(); i++) {
            JSONArray entity = (JSONArray) gameEntities.get(i);
            String pguid = entity.get(0).toString();
            JSONObject info = (JSONObject) entity.get(2);
            JSONObject portalV2 = (JSONObject) info.get("portalV2");
            if (portalV2 != null) { //then we are a portal, yay!
                String title = (((JSONObject) portalV2.get("descriptiveText")).get("TITLE").toString());
                JSONObject locE6 = (JSONObject) info.get("locationE6");
                long latE6 = Long.parseLong(locE6.get("latE6").toString());
                long lngE6 = Long.parseLong(locE6.get("lngE6").toString());
                S2LatLng portalS2 = S2LatLng.fromE6(latE6, lngE6);
                localPortals.add(new Portal(pguid, title, portalS2));
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // getSurroundingCells()
    // Updates the ClientWrapper object with a new set of cells based on it's current location
    // Side Effects : localCellIds
    //////////////////////////////////////////////////////////////////////////////////////////////////
    private void getSurroundingCells() {
        //Setup the coverer and the distance from
        S2RegionCoverer coverer = new S2RegionCoverer();
        S2LatLng size = S2LatLng.fromDegrees(0.0045, 0.0045);
        coverer.setMinLevel(16);
        coverer.setMaxLevel(16);

        //Create the rectangle
        S2LatLngRect rect = S2LatLngRect.fromCenterSize(this.currentLocation, size);

        //Use the coverer to fill localCellIds
        coverer.getCovering(rect, localCellIds);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // getEdibleGlobs()
    // Updates the edible globs array list to include globs within 10 meters of currentLocation
    // Side Effects : localEdibleGlobs
    //////////////////////////////////////////////////////////////////////////////////////////////////
    private void getEdibleGlobs() {
        for (int i = 0; i < localEnergyGlobs.size(); i++) {
            if (S2Wrapper.GreatEarthDistance(localEnergyGlobs.get(i).s2ll, currentLocation) < 30.0)
                localEdibleGlobs.add(localEnergyGlobs.get(i));
        }
    }

    private void getHackablePortals() {
        for (int i = 0; i < localPortals.size(); i++) {
            if (S2Wrapper.GreatEarthDistance(localPortals.get(i).location, currentLocation) < 30.0)
                localHackablePortals.add(localPortals.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    private void hackPortal(String portalGUID) throws Exception {
        //setup json request
        JSONObject main = new JSONObject();
        JSONObject params = new JSONObject();

        DebugHandler.debugInfo("Acquiring items from portal " + portalGUID);

        params.put("itemGuid", portalGUID);
        params.put("knobSyncTimeStamp", syncTimestamp);
        params.put("playerLocation", S2Wrapper.encodeLocation(currentLocation));
        main.put("params", params);

        //Setup the json connection
        URL getObjectsURL = new URL(baseURL + "/rpc/gameplay/collectItemsFromPortal");
        URLConnection getObjectsCon = getObjectsURL.openConnection();
        getObjectsCon.setRequestProperty("Cookie", authCookie);
        getObjectsCon.setRequestProperty("X-XsrfToken", this.xsrfToken);
        getObjectsCon.setDoOutput(true);

        //Setup and use the writer
        OutputStreamWriter out = new OutputStreamWriter(getObjectsCon.getOutputStream());
        out.write(main.toString());  //Write our json object to the connection
        out.close();

        //Setup and use the reader
        BufferedReader br = new BufferedReader(new InputStreamReader(getObjectsCon.getInputStream()));
        String line = br.readLine();
        br.close();

        DebugHandler.debugln(line);

        DebugHandler.debugInfo("Acquire successful!");
    }

    @SuppressWarnings("unchecked")
    public void getInventory() throws Exception {
        JSONObject main = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("lastQueryTimestamp", 0);
        main.put("params", params);

        //Setup the json connection
        URL getObjectsURL = new URL(baseURL + "/rpc/playerUndecorated/getInventory");
        URLConnection getObjectsCon = getObjectsURL.openConnection();
        getObjectsCon.setRequestProperty("Cookie", authCookie);
        getObjectsCon.setRequestProperty("X-XsrfToken", this.xsrfToken);
        getObjectsCon.setDoOutput(true);

        //Setup and use the writer
        OutputStreamWriter out = new OutputStreamWriter(getObjectsCon.getOutputStream());
        out.write(main.toString());  //Write our json object to the connection
        out.close();

        //Setup and use the reader
        BufferedReader br = new BufferedReader(new InputStreamReader(getObjectsCon.getInputStream()));
        String line = br.readLine();
        br.close();

        JSONParser jp = new JSONParser();
        JSONObject obj = (JSONObject) jp.parse(line);
        JSONObject gameBasket = (JSONObject) obj.get("gameBasket");
        JSONArray inventoryArray = (JSONArray) gameBasket.get("inventory"); //we need to loop through the inventory array to get each item array

        player.inventory.clear();

        for (int i = 0; i < inventoryArray.size(); i++) {
            JSONArray item = (JSONArray) inventoryArray.get(i);
            JSONObject itemInfo = (JSONObject) item.get(2);
            //System.out.println(itemInfo.toString());
            JSONObject resource = (JSONObject) itemInfo.get("resourceWithLevels");
            if (resource != null) {
                String objectString = resource.get("resourceType").toString() + ":" + resource.get("level").toString();

                boolean found = false;
                for (int j = 0; j < player.inventory.size(); j++) {
                    if (player.inventory.get(j).name.equals(objectString)) {
                        found = true;
                        player.inventory.get(j).quantity++;
                    }
                }
                if (!found)
                    player.inventory.add(new IngressItem(objectString));
                //System.out.println(objectString);
            }
        }


        //player.inventory.clear();


        //System.out.print("\n\n\n" + line + "\n\n\n");
        DebugHandler.debugln(line);

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // newLocation(S2LatLng)
    // Updates the ClientWrapper object with a new location. Old location reliant information is
    // cleared and new cells and object requests are called
    // TODO : Automatically check xm and eat available energyglobs
    // Side Effects : curentLocation, localCellIds, localEnergyGlobs
    //////////////////////////////////////////////////////////////////////////////////////////////////
    public void newLocation(Location loc) throws Exception {
        S2LatLng newLocation = S2LatLng.fromDegrees(loc.getLat(), loc.getLng());
        //clear current data?
        clearLocalData();

        //call anything that changes on new location
        currentLocation = newLocation;

        getSurroundingCells();

        getObjectsInCells();

        getEdibleGlobs();

        localPortals.clear();
        localHackablePortals.clear();

        getObjectsInCells();

        getHackablePortals();

        //hackPortal("2bae48e8561f46a5824793341c65003f.11");
        //get objects in cells, then call again to eat?
        //check xm
        //if low then eat
    }

    public void printLocalHackablePortalNames() {
        for (int i = 0; i < localHackablePortals.size(); i++) {
            DebugHandler.debugInfo(localHackablePortals.get(i).title + " : " + localHackablePortals.get(i).guid);
        }
    }

    public void hackSpecificPortal(String guid) throws Exception {
        hackPortal(guid);
    }

    public void hackLocalPortals() throws Exception {
        for (int i = 0; i < localHackablePortals.size(); i++) {
            hackPortal(localHackablePortals.get(i).guid);
            Thread.sleep(1000);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // clearLocalData()
    // Clears data stored in array lists, called when updating location information
    // Side Effects : localCellIds, localEnergyGlobs
    //////////////////////////////////////////////////////////////////////////////////////////////////    
    private void clearLocalData() {
        localEdibleGlobs.clear();
        localEnergyGlobs.clear();
        localCellIds.clear();
    }

    //TODO: Get energy and maintain state

}
