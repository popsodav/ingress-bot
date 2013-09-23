import com.google.common.geometry.*;

public class TransitHandler extends Thread{
    
    private S2LatLng currentLocation;
    private S2LatLng destination; 
    
    private double distance;
    private int secondsToCross;   
    
    //private GUI gui;

    public TransitHandler(Location loc1, Location loc2){
        this.currentLocation = S2LatLng.fromDegrees(loc1.getLat(), loc1.getLng());
        this.destination = S2LatLng.fromDegrees(loc2.getLat(), loc2.getLng());

        this.distance = S2Wrapper.GreatEarthDistance(this.currentLocation, this.destination);
        this.secondsToCross = (int) (distance/5.0); //move at 5m/s
    }
    
    public TransitHandler(S2LatLng currentLocation, S2LatLng destination){
        this.currentLocation = currentLocation;
        this.destination = destination;
        //this.gui = gui;
        
        this.distance = S2Wrapper.GreatEarthDistance(this.currentLocation, this.destination);
        this.secondsToCross = (int) (distance/5.0); //move at 5m/s
    }
    
    public void run(){
    try{
        int sections = (int) (distance / 5.0);
        double startLat = this.currentLocation.latDegrees();
        double startLng = this.currentLocation.lngDegrees();
        double latChange = (this.destination.latDegrees() - this.currentLocation.latDegrees()) / sections;
        double lngChange = (this.destination.lngDegrees() - this.currentLocation.lngDegrees()) / sections;
        for(int i = 0; i < sections; i++){
            //move a bit
            currentLocation = S2LatLng.fromDegrees(startLat + (latChange * i) , startLng + (lngChange * i));
            //spawn an update thread
            //gui.updateLocation(currentLocation);
            //sleep(1000)
            Thread.sleep(1000);
        }
     }catch (Exception e) {
        System.out.println(e.getMessage());
     }
               
        
    }
}
