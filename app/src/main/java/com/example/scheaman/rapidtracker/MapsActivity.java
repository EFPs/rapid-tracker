package com.example.scheaman.rapidtracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST = 1;

    private GoogleMap mMap;
    private FirebaseUser user;
    private boolean locationReady = false;
    private float myLat;
    private float myLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        } else {
            Log.d("Print onCrete","HEY");

            requestLocationUpdates();
        }
    }

//    private void makeSureThatDBExist(String path){
//        DatabaseReference mDatabase;
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//        Log.d("Print user path", "inMakeSure User Path ARE "+path);
//        mDatabase.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.d("Print database exist", "IN");
//                if(!dataSnapshot.exists()){
//                    Log.d("Print database exist", "NO");
//
//                    requestLocationUpdates();
//                }else{
//                    Log.d("Print database exist", "YES");
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d("Print database Cancel", "Canceled");
//
//            }
//        });
//    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        user =  FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        final String userPath = "location" + "/" + user.getUid();
        final String otherUsers = "location/";

        //If user is new and doesn't have database yet, create it.
//        makeSureThatDBExist(userPath);
//        while (!locationReady){
//            makeSureThatDBExist(userPath);
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//        final String path = "location" + "/" + "123";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(userPath);
        DatabaseReference otherRef = FirebaseDatabase.getInstance().getReference(otherUsers);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    Log.d("Print inmap", "NO");
                    requestLocationUpdates();
                }else{
                    Log.d("Print inmap", "YES");
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    GenericTypeIndicator<HashMap<String,Object>> t = new GenericTypeIndicator<HashMap<String,Object>>() {};
                    HashMap<String,Object> location = dataSnapshot.getValue(t);
                    Log.d("Retrieved", "Value is: " + location);


                    Log.d("Print", "Value is: " + location.get("latitude").getClass() + " "+ location.get("longitude") );
                    // Add a marker and move the camera
                    Double lat = (Double)location.get("latitude");
                    Double lng = (Double)location.get("longitude");
                    String name = (String)location.get("name");
                    LatLng current = new LatLng(lat, lng);

                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(current).title(name));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(current));

                }


            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Ignore
            }


        });

        otherRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String,HashMap<String,Object>>> t = new GenericTypeIndicator<HashMap<String,HashMap<String,Object>>>() {};
                HashMap<String,HashMap<String,Object>> location = dataSnapshot.getValue(t);
                for(Object i : location.keySet()){
                    if(!i.toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        Double lat = Double.parseDouble(location.get(i).get("latitude").toString());
                        Double lng = Double.parseDouble(location.get(i).get("longitude").toString());
                        LatLng current = new LatLng(lat, lng);
                        mMap.addMarker(new MarkerOptions()
                                .position(current)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                .title(i.toString()));

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Ignore
            }
        });
    }



    private void requestLocationUpdates() {
        user =  FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        final String path = "location" + "/" + user.getUid();
        Log.i("Print inUpdate", "inUpdate User Path ARE "+path);
//        final String path = "location" + "/" + "123";
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        Log.i("Print inUpdate", "Permission "+permission +" "+PackageManager.PERMISSION_GRANTED);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            Log.i("print","Permission Granted");
            // Request location updates and when an update is
            // received, store the location in Firebase
            LocationRequest request = new LocationRequest();
            request.setInterval(10000);
            request.setFastestInterval(5000);
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            Log.i("Print","Client request location update");
            locationReady = true;
//            client.requestLocationUpdates(request, new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//
//                }
//
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras) {
//
//                }
//            }, null);
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Log.i("Print","onLocation Result");
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    Location location = locationResult.getLastLocation();
                    HashMap name = new HashMap() ;
                    name.put("name",user.getEmail());
                    if (location != null) {
                        Log.d("Print Location", "location update " + location);
                        ref.setValue(location);
                        ref.updateChildren(name);
                    }else{
                        Log.e("Print Location", "location is null " + location);
                    }
                }


            }, null);
        }else{
            Log.d("Print inUpdate","Permission fucked"+ permission);
        }
    }

}
