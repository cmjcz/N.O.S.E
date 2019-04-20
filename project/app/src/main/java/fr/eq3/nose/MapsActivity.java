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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.eq3.nose.spot.items.DatabaseRequest;
import fr.eq3.nose.spot.items.Spot;
import fr.eq3.nose.spot.spot_activity.SpotActivity;
import fr.eq3.nose.spot.spot_creator_activity.SpotCreatorActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final int MY_LOCATION_REQUEST_CODE = 101;
    private static final int CREATE_SPOT_REQUEST = 64;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location myLocation;
    private static final long MIN_TIME = 0;
    private static final float MIN_DISTANCE = 0;
    private Set<Spot> spot_cache = new HashSet<>();
    private TextView error;
    //Requete de localisation
    private Criteria criteria;
    private String bestProvider;
    //About the menu
    FloatingActionMenu menuMap;
    FloatingActionButton option_mapTerrain, option_mapNormal, option_mapSatelite, option_addSpot;

    /**
     * Method called at the creation of the app
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        error = findViewById(R.id.error);
        error.setVisibility(View.INVISIBLE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        map.getMapAsync(this);
        initializeMenu();
    }

    /**
     * Method which creates and initializes the menu
     */
    private void initializeMenu(){
        menuMap = findViewById(R.id.menuMap);
        option_mapTerrain = findViewById(R.id.option_mapTerrain);
        option_mapNormal = findViewById(R.id.option_mapNormal);
        option_mapSatelite = findViewById(R.id.option_mapSatelite);
        option_addSpot = findViewById(R.id.option_addSpot);

        option_mapTerrain.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            menuMap.close(true);
        });
        option_mapNormal.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            menuMap.close(true);
        });
        option_mapSatelite.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            menuMap.close(true);
        });
        option_addSpot.setOnClickListener(v -> {
            /**
             * Launch the spotCreator activity to get information about the spot
             */
            Intent intent = new Intent(MapsActivity.this, SpotCreatorActivity.class);
            intent.putExtra(SpotCreatorActivity.KEY_POS, new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
            startActivityForResult(intent, CREATE_SPOT_REQUEST);
        });
    }

    /**
     * Method which creates the general default parameters for the map
     */
    private void updateMaps(){
        double formerLat = myLocation.getLatitude();
        double formerLong = myLocation.getLongitude();
        locationManager.requestLocationUpdates(bestProvider, MIN_TIME, MIN_DISTANCE, this);
        if(locationManager.getLastKnownLocation(bestProvider)!=null){
            myLocation = locationManager.getLastKnownLocation(bestProvider);
            error.setVisibility(View.INVISIBLE);
        }
        LatLng currentPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        refreshSpotsCache();
        //Min Zoom
        mMap.setMinZoomPreference(14.0f);
        //Disable rotation and scrolling
        LatLngBounds myArea = new LatLngBounds(new LatLng(currentPosition.latitude - 0.03, currentPosition.longitude - 0.03), new LatLng(currentPosition.latitude + 0.03, currentPosition.longitude + 0.03));
        mMap.setLatLngBoundsForCameraTarget(myArea);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        //Move the camera to the current location
        if((formerLat==0 && formerLong==0)){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 17.0f));
        }
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
        if(isGpsActivated()){
            getLocation();
        }else{
            if(!isLocationEnabled(MapsActivity.this)){
                enableLocation();
            }
            mMap.setMyLocationEnabled(true);
            Location temporary = new Location(LocationManager.NETWORK_PROVIDER);
            temporary.setLatitude(0);
            temporary.setLongitude(0);
            myLocation = temporary;
        }
        LatLng currentPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        updateMaps();
        //Map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 17.0f));
        mMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(MapsActivity.this, SpotActivity.class);
            final long id = Long.parseLong(marker.getTitle());
            intent.putExtra(SpotActivity.SPOT_EXTRA, id);
            startActivity(intent);
            return true;
        });
        mMap.setOnMyLocationChangeListener(relocate->updateMaps());
        mMap.setOnMapClickListener(click->menuMap.close(true));
        mMap.setOnCameraMoveStartedListener(change->menuMap.close(true));
        if(!isGpsActivated()){
            error.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Listen to the location changes and refresh the Map according to them
     * @param location new location of person
     */
    @Override
    public void onLocationChanged(Location location) {
        if(myLocation.getLongitude()==0 && myLocation.getLatitude()==0){
            myLocation=location;
            updateMaps();
        }else{
            myLocation=location;
        }
        refreshSpotsCache();
    }

    @Override
    public void onResume(){
        super.onResume();

        if(mMap != null){
            mMap.clear();
        }
        refreshMap();
    }

    /**
     * If the provider is disabled by the user
     * @param provider
     */
    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        Toast.makeText(MapsActivity.this,
                "Signal GPS perdu", Toast.LENGTH_LONG)
                .show();
        error.setVisibility(View.VISIBLE);
    }

    /**
     * If the provider is enabled by the user
     * @param provider
     */
    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        Toast.makeText(MapsActivity.this,
                "GPS trouvé", Toast.LENGTH_LONG)
                .show();
        error.setVisibility(View.INVISIBLE);
        if(!isLocationEnabled(MapsActivity.this)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
        }
        getLocation();
        updateMaps();
        LatLng currentPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 17.0f));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }



    //TODO//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //---------------------------------METHODS CREATED ESPACIALLY FOR THIS PROJECT------------------------------------------------------
    //TODO//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * To know if the GPS has been activated
     * @return true/false
     */
    public boolean isGpsActivated(){
        if(bestProvider==null || bestProvider.equals(LocationManager.PASSIVE_PROVIDER)){
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);//GPS_PROVIDER?
    }


    /**
     * The statement is true if ACCESS_FINE_LOCATION has been granted
     * @param context
     * @return
     */
    private boolean isLocationEnabled(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Find the location of the user (no permission here)
     */
    private void enableLocation(){
        locationManager.requestLocationUpdates(bestProvider, MIN_TIME, MIN_DISTANCE, this);
        myLocation = locationManager.getLastKnownLocation(bestProvider);
        if (myLocation == null) {
            getLocation();
        }
    }

    /**
     * Find the current location of the user or ask for permissions
     */
    protected void getLocation() {
            if (isLocationEnabled(MapsActivity.this)) {
                mMap.setMyLocationEnabled(true);
                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                criteria = new Criteria();
                bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));
                myLocation = locationManager.getLastKnownLocation(bestProvider);
                if (myLocation != null) {
                    Toast.makeText(MapsActivity.this, "LOCALISATION : OK", Toast.LENGTH_SHORT).show();

                } else {
                    enableLocation();
                }
            } else {
                //Demande d'activation du service de localisation
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
                getLocation();
            }
    }



    /**
     * Avoid the area around the current location to duplicate itself
     * Re-put all the spots and influence areas on the Map
     */
    public void refreshMap(){
        for(Spot saved : spot_cache){
            mMap.addMarker(getSpotMarker(saved));
            mMap.addCircle(getSpotInfluenceZone(saved));
        }
    }

    private void refreshSpotsCache(){
        DatabaseRequest dbr = new DatabaseRequest(this);
        List<Integer> spots = dbr.getSpotsBetween(myLocation.getLatitude(), myLocation.getLongitude(), 100);
        for(int id : spots) {
            Spot s = dbr.getSpot(id);
            if(!spot_cache.contains(s)){
                spot_cache.add(s);
                addSpotOnMap(s);
            }
        }
    }

    /**I/art: Rejecting re-init on previously-failed class java.lang.Class<fr.eq3.nose.-$$Lambda$MapsActivity$GXJAi3W9Z41rCnmXELYVVzl0h_4>
     * Put a spot on the map, at the current location
     */
    public void addSpotOnMap(Spot spot){
        spot_cache.add(spot);
        mMap.addMarker(getSpotMarker(spot));
        mMap.addCircle(getSpotInfluenceZone(spot));
    }

    /**
     * Return the marker of a spot
     * @param spot
     * @return
     */
    private MarkerOptions getSpotMarker(Spot spot){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(spot.getLat(), spot.getLong()))
                .title(spot.getId() + "");
        return  markerOptions;
    }

    /**
     * Return the influence zone of a spot
     * @param spot
     * @return
     */
    private CircleOptions getSpotInfluenceZone(Spot spot){
        CircleColor circleColor = CircleColor.GREEN;
        return new CircleOptions()
                .center(new LatLng(spot.getLat(), spot.getLong()))
                .radius(radius(spot.getInfluenceLvl()))
                .strokeColor(circleColor.strokeColor)
                .fillColor(circleColor.fillColor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CREATE_SPOT_REQUEST){
            if(resultCode == RESULT_OK){
                if(data != null){
                    long spotid = data.getLongExtra(SpotActivity.SPOT_EXTRA, -1);
                    Spot spot = new DatabaseRequest(this).getSpot(spotid);
                    spot_cache.add(spot);
                    addSpotOnMap(spot);
                }
            }
            menuMap.close(true);
        }
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