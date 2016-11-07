package cse5236.pinpoint;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsActivity";

    RelativeLayout mapContainer;
    RelativeLayout newPostLayout;

    EditText newPostContent;
    Button newPostSubmit;

    TextView viewPostTitle;
    EditText newMessageContent;
    Button newMessageSubmit;

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LatLng mCoordinates;
    Geocoder geocoder;
    List<Address> addresses;

    FloatingActionButton fab;

    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    ValueEventListener threadListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        mapContainer = (RelativeLayout) findViewById(R.id.mapContainer);
        newPostLayout = (RelativeLayout) findViewById(R.id.newPostLayout);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        geocoder = new Geocoder(this, Locale.getDefault());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        threadListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Changed: " + dataSnapshot.getChildrenCount());
                updateMarkers(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("threadIds").addValueEventListener(threadListener);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCoordinates = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCoordinates, 11));
                newThread();
            }
        });
    }

    public void newThread() {

        fab.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View inflatedLayout = inflater.inflate(R.layout.fragment_new_post, mapContainer, false);
        mapContainer.addView(inflatedLayout);

        newPostContent = (EditText) findViewById(R.id.newPostContent);
        newPostSubmit = (Button) findViewById(R.id.newPostSubmit);
        newPostSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked Send Button: " + newPostContent.getText());
                writeNewThread(newPostContent.getText().toString());
                View newPostView = findViewById(R.id.newPostLayout);
                mapContainer.removeView(newPostView);
                fab.setVisibility(View.VISIBLE);
            }
        });
    }

    public void updateMarkers(DataSnapshot threadIds) {
        mGoogleMap.clear();

        Iterable<DataSnapshot> ids = threadIds.getChildren();
        for (DataSnapshot id : ids) {
            Log.d(TAG, id.toString());
            LatLng threadLatLng = new LatLng((double) id.child("lat").getValue(), (double) id.child("lng").getValue());
            String name = id.getKey();
//            try {
//                addresses = geocoder.getFromLocation(threadLatLng.latitude, threadLatLng.longitude, 1);
//                name = addresses.get(0).getAddressLine(0);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            mGoogleMap.addMarker(new MarkerOptions().position(threadLatLng).title(name));
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        fab.setVisibility(View.GONE);

        LatLng pos = marker.getPosition();
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 11));

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View inflatedLayout = inflater.inflate(R.layout.fragment_view_thread, mapContainer, false);
        mapContainer.addView(inflatedLayout);

        viewPostTitle = (TextView) findViewById(R.id.viewPostTitle);
        newMessageContent = (EditText) findViewById(R.id.newMessageContent);
        newMessageSubmit = (Button) findViewById(R.id.newMessageSubmit);

        String id = marker.getTitle();
        try {
            addresses = geocoder.getFromLocation(pos.latitude, pos.longitude, 1);
            String name = addresses.get(0).getAddressLine(0);
            viewPostTitle.setText(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DatabaseReference messagesRoot = mDatabase.child("threads").child(id).child("messages").getRef();


//        Log.d(TAG, thread.toString());
        return true;
    }

    public void writeNewThread(String text) {
        // Create new Thread child
        DatabaseReference threadRoot = mDatabase.child("threads").push();
        Long timestamp = System.currentTimeMillis()/1000;

        // Save Thread attributes
        String rootRef = threadRoot.toString().substring(threadRoot.getParent().toString().length()+1);
        Thread newThread = new Thread(rootRef, timestamp.toString(), mCoordinates.latitude, mCoordinates.longitude);
        threadRoot.setValue(newThread);

        // Store thread id separately as an index
        ThreadIndex ti = new ThreadIndex(mCoordinates.latitude, mCoordinates.longitude);
        mDatabase.child("threadIds").child(rootRef).setValue(ti);

        // Save first message
        DatabaseReference messagesRoot = threadRoot.child("messages").push();
        String messageId = messagesRoot.toString().substring(messagesRoot.getParent().toString().length()+1);
        Message rootMessage = new Message(messageId, mUser.getUid(), mUser.getDisplayName(), text, timestamp.toString());
        messagesRoot.setValue(rootMessage);
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap=googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mCoordinates = latLng;
                newThread();
            }
        });
        mGoogleMap.setOnMarkerClickListener(this);

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                //(just doing it here for now, note that with this code, no explanation is shown)
                ActivityCompat.requestPermissions(this,
                        new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
