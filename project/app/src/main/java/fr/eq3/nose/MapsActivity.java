package fr.eq3.nose;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
//import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import fr.eq3.nose.spots.request_to_db.DatabaseRequest;
import fr.eq3.nose.spots.spot.Spot;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final int MY_LOCATION_REQUEST_CODE = 101;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location myLocation;
//    private LatLngBounds myArea;
    private static final long MIN_TIME = 0;
    private static final float MIN_DISTANCE = 0;
    private ArrayList<Spot> spotList_tmp = new ArrayList<>();
    private ArrayList<CircleOptions> influenceZone_tmp = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        MapFragment map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        map.getMapAsync(this);
    }


    /**
     * Map management
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Enable to track the location
        enableMyLocation();
        LatLng currentPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        //Map type
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //Min Zoom
        mMap.setMinZoomPreference(14.0f);
        //Disable rotation and scrolling
        LatLngBounds myArea = new LatLngBounds(new LatLng(currentPosition.latitude-0.03, currentPosition.longitude-0.03), new LatLng(currentPosition.latitude+0.03, currentPosition.longitude+0.03));
        mMap.setLatLngBoundsForCameraTarget(myArea);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        //Move the camera to the current location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 17.0f));

    }


    /**
     * Listen to the location changes and refresh the Map according to them
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        myLocation=location;
//        mMap.addCircle(new CircleOptions()
//                .center(latLng)
//                .radius(20)
//                .strokeColor(Color.BLUE)
//                .fillColor(0x700787ef));
        refreshMap();
    }

    @Override
    public void onResume(){
        super.onResume();

        if(mMap != null){
            mMap.clear();

            // add markers from database to the map
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        Toast.makeText(MapsActivity.this,
                "Provider disable: " + provider, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        Toast.makeText(MapsActivity.this,
                "Provider enabled: " + provider, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }



    //TODO//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //---------------------------------METHODS CREATED ESPACIALLY FOR THIS PROJECT------------------------------------------------------
    //TODO//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Check for the permissions and initialize myLocation variable with the last known location
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);

        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            myLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }

    /**
     * Avoid the area around the current location to duplicate itself
     * Re-put all the spots and influence areas on the Map
     */
    public void refreshMap(){
        for(Spot saved : spotList_tmp){
            mMap.addMarker(getSpotMarker(saved));
            mMap.addCircle(getSpotInfluenceZone(saved));
        }
    }

    /**
     * Put a spot on the map, at the current location
     * @param view
     */
    public void addSpotOnMap(View view){
        DatabaseRequest dbr = new DatabaseRequest(this);
        Spot spot = dbr.createSpot("Spot", "", new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        mMap.addMarker(getSpotMarker(spot));
        mMap.addCircle(getSpotInfluenceZone(spot));
        spotList_tmp.add(spot);
        influenceZone_tmp.add(getSpotInfluenceZone(spot));
    }

    /**
     * Change the Map type (satellite/Normal/terrain)
     * @param view
     */
    public void setMapType(View view){
        Switch switchType = findViewById(R.id.mapType);
        if(switchType.isChecked()){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
    }

//    public boolean zonesInConflict(Spot centerZone1, Spot centerZone2){
//        double lat, lng;
//        if(centerZone1.getSpotLocation().getLatitude()>centerZone2.getSpotLocation().getLatitude()){
//            lat=centerZone1.getSpotLocation().getLatitude()-centerZone2.getSpotLocation().getLatitude();
//        }else{
//            lat=centerZone2.getSpotLocation().getLatitude()-centerZone1.getSpotLocation().getLatitude();
//        }
//        if(centerZone1.getSpotLocation().getLongitude()>centerZone2.getSpotLocation().getLongitude()){
//            lng=centerZone1.getSpotLocation().getLongitude()-centerZone2.getSpotLocation().getLongitude();
//        }else{
//            lng=centerZone1.getSpotLocation().getLongitude()-centerZone2.getSpotLocation().getLongitude();
//        }
//        if(lat <= centerZone2.getSpotInfluenceZone().getRadius() || lng <= centerZone2.getSpotInfluenceZone().getRadius()){
//            return true;
//        }
//        return false;
//    }

    private MarkerOptions getSpotMarker(Spot spot){
        return new MarkerOptions()
                .position(new LatLng(spot.getLat(), spot.getLong()))
                .title(spot.getName());
    }

    private CircleOptions getSpotInfluenceZone(Spot spot){
        CircleColor circleColor = CircleColor.GREEN;
        return new CircleOptions()
                .center(new LatLng(spot.getLat(), spot.getLong()))
                .radius(radius(spot.getInfluenceLvl()))
                .strokeColor(circleColor.strokeColor)
                .fillColor(circleColor.fillColor);
    }

    /**
     * Get the radius in meters from a spot influence lvl
     * @param lvl the lvl of influence
     * @return the radius in meters (always positive)
     */
    private int radius(int lvl){
        return lvl > 0 ? lvl * 20 : 0;
    }

    private enum CircleColor{

        GREEN(Color.GREEN, 0x7093fc0a);

        private int strokeColor;
        private int fillColor;

        CircleColor(int strokeColor, int fillColor){
            this.fillColor = fillColor;
            this.strokeColor = strokeColor;
        }
    }


}