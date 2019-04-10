package fr.eq3.nose;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import fr.eq3.nose.spot.items.DatabaseRequest;
import fr.eq3.nose.spot.items.Spot;
import fr.eq3.nose.spot.view.SpotView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final int MY_LOCATION_REQUEST_CODE = 101;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location myLocation;
    private static final long MIN_TIME = 0;
    private static final float MIN_DISTANCE = 0;
    private ArrayList<Spot> spotList_tmp = new ArrayList<>();
    private ArrayList<CircleOptions> influenceZone_tmp = new ArrayList<>();
    //Requete de localisation
    private Intent intentThatCalled;
    private Criteria criteria;
    private double longitude;
    private double latitude;
    private String bestProvider;
    private String voice2text;
    //About the menu
    FloatingActionMenu menuMap;
    FloatingActionButton option_mapTerrain, option_mapNormal, option_mapSatelite, option_addSpot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        map.getMapAsync(this);
     //   intentThatCalled = getIntent();
        initializeMenu();
    }

    private void initializeMenu(){
        menuMap = findViewById(R.id.menuMap);
        option_mapTerrain = findViewById(R.id.option_mapTerrain);
        option_mapNormal = findViewById(R.id.option_mapNormal);
        option_mapSatelite = findViewById(R.id.option_mapSatelite);
        option_addSpot = findViewById(R.id.option_addSpot);

        option_mapTerrain.setOnClickListener(v -> {
            //TODO something when floating action menu first item clicked
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            menuMap.close(true);
        });
        option_mapNormal.setOnClickListener(v -> {
            //TODO something when floating action menu second item clicked
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            menuMap.close(true);

        });
        option_mapSatelite.setOnClickListener(v -> {
            //TODO something when floating action menu third item clicked
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            menuMap.close(true);

        });
        option_addSpot.setOnClickListener(v -> {
            //TODO something when floating action menu third item clicked
            addSpotOnMap();
            menuMap.close(true);
        });
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
        LatLngBounds myArea = new LatLngBounds(new LatLng(currentPosition.latitude - 0.03, currentPosition.longitude - 0.03), new LatLng(currentPosition.latitude + 0.03, currentPosition.longitude + 0.03));
        mMap.setLatLngBoundsForCameraTarget(myArea);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        //Move the camera to the current location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 17.0f));

        mMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(MapsActivity.this, SpotView.class);
            final long id = Long.parseLong(marker.getTitle());
            intent.putExtra(SpotView.SPOT_EXTRA, id);
            startActivity(intent);
            return true;
        });
    }

    /**
     * Listen to the location changes and refresh the Map according to them
     * @param location new location of person
     */
    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        myLocation=location;
        mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(20)
                .strokeColor(Color.BLUE)
                .fillColor(0x700787ef));
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

    private boolean isLocationEnabled(Context context) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
            return false;
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            myLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            return true;
        }
    }

    //https://stackoverflow.com/questions/32290045/error-invoke-virtual-method-double-android-location-location-getlatitude-on
    protected void getLocation() {
        if (isLocationEnabled(MapsActivity.this)) {
            locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Toast.makeText(MapsActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
                searchNearestPlace(voice2text);
            }
            else{
                //This is what you need:
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
            }
        }
        else
        {
            //prompt user to enable location....
            //.................
        }
    }

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
     */
    public void addSpotOnMap(){
        DatabaseRequest dbr = new DatabaseRequest(this);
        Spot spot = dbr.createSpot("Spot", "", new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        mMap.addMarker(getSpotMarker(spot));
        mMap.addCircle(getSpotInfluenceZone(spot));
        spotList_tmp.add(spot);
        influenceZone_tmp.add(getSpotInfluenceZone(spot));
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
                .title(spot.getId() + "");
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