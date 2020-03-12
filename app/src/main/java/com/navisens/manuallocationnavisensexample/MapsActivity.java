package com.navisens.manuallocationnavisensexample;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.navisens.motiondnaapi.MotionDna;
import com.navisens.motiondnaapi.MotionDnaApplication;
import com.navisens.motiondnaapi.MotionDnaInterface;

import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MotionDnaInterface, HeadingDialView.DialListener {

    private GoogleMap mMap;
    MotionDnaApplication motionDnaApplication;
    private static final int REQUEST_LOCATION_PERMISSIONS = 1;

    double lastVal = 0.0;
    double lastBearing = 0.0;
    boolean follow = false;

    Button actionButton;
    HeadingDialView headingDialView;
    HeadingArrowView headingArrowView;
    ImageView pinImageView;

    Marker userLocationMarker;
    Bitmap userLocationBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        actionButton = findViewById(R.id.action_button);
        headingDialView = findViewById(R.id.heading_dial);
        headingDialView.addDialListener(this);
        headingArrowView = findViewById(R.id.heading_arrow);
        pinImageView = findViewById(R.id.pin);
        userLocationBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.red_dot);

        actionButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionButton.getText().equals("Manually Set Location")) {
                    actionButton.setText("Set Position");
                    pinImageView.setVisibility(View.VISIBLE);
                    mMap.getUiSettings().setRotateGesturesEnabled(false);
                    userLocationMarker.setVisible(false);
                }
                else if (actionButton.getText().equals("Set Position")) {
                    actionButton.setText("Set Heading");
                    headingArrowView.setVisibility(View.VISIBLE);
                    headingDialView.setVisibility(View.VISIBLE);
                    lastBearing = mMap.getCameraPosition().bearing;
                    mMap.getUiSettings().setRotateGesturesEnabled(true);
                    mMap.getUiSettings().setScrollGesturesEnabled(false);
                }
                else if (actionButton.getText().equals("Set Heading")) {
                    pinImageView.setVisibility(View.INVISIBLE);
                    actionButton.setText("Manually Set Location");
                    headingArrowView.setVisibility(View.INVISIBLE);
                    headingDialView.setVisibility(View.INVISIBLE);
                    mMap.getUiSettings().setScrollGesturesEnabled(true);
                    userLocationMarker.setVisible(true);


                    double heading = mMap.getCameraPosition().bearing;
                    userLocationMarker.setRotation((float)heading);
                    LatLng newLoc = mMap.getCameraPosition().target;
                    motionDnaApplication.setLocationLatitudeLongitudeAndHeadingInDegrees(newLoc.latitude, newLoc.longitude, heading);

                }
                Log.v("APP", "ACTION");
            }
        });
        requestPermissions( MotionDnaApplication.needsRequestingPermissions()
                , REQUEST_LOCATION_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (MotionDnaApplication.checkMotionDnaPermissions(this) == true) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        }else{
            Log.e(this.getClass().getSimpleName(),"===DnaPermissions not granted");
        }

    }


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
        Log.v(getClass().getSimpleName(),"onMapReady");
        mMap = googleMap;
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    follow = false;
                }
            }
        });
        configureAndRunMotionDna();


    }


    // Heading Dial listener callback

    @Override
    public void onDial(double number) {
        Log.v(getClass().getSimpleName(),String.format("BEARING %.3f",-(lastBearing+number)));
        CameraPosition currentCamera = mMap.getCameraPosition();
        CameraUpdate headingUpdate = CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(currentCamera.target).zoom(currentCamera.zoom).bearing((float)-(lastBearing+number)).build());
        mMap.animateCamera(headingUpdate,1,null);
        lastVal = number;
    }

    void configureAndRunMotionDna() {
        motionDnaApplication = new MotionDnaApplication(this);
        motionDnaApplication.runMotionDna("NAVISENS_DEV_KEY_HERE");
        motionDnaApplication.setLocationNavisens();
        motionDnaApplication.setExternalPositioningState(MotionDna.ExternalPositioningState.HIGH_ACCURACY);
        motionDnaApplication.setBinaryFileLoggingEnabled(true);
        motionDnaApplication.startForegroundService();
        Log.v(getClass().getSimpleName(), "SDK: " + MotionDnaApplication.checkSDKVersion());
    }


    @Override
    public void receiveMotionDna(MotionDna motionDna) {
        MotionDna.GlobalLocation globalLocation = motionDna.getLocation().globalLocation;
        final double heading = motionDna.getLocation().heading;
        final LatLng coordinate = new LatLng(globalLocation.latitude, globalLocation.longitude);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (userLocationMarker == null) {
                    userLocationMarker = mMap.addMarker(new MarkerOptions().position(coordinate).icon(BitmapDescriptorFactory.fromBitmap(userLocationBitmap)).flat(true).anchor(0.5f, 0.5f));
                    CameraUpdate centerAndZoom = CameraUpdateFactory.newLatLngZoom(coordinate, 18);
                    mMap.moveCamera(centerAndZoom);
                    follow = true;
                } else {
                    if (follow == true) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(coordinate);
                        mMap.moveCamera(cameraUpdate);
                    }
                }
                userLocationMarker.setPosition(coordinate);
                userLocationMarker.setRotation((float)heading);
            }
        });
    }

    @Override
    public void receiveNetworkData(MotionDna motionDna) {

    }

    @Override
    public void receiveNetworkData(MotionDna.NetworkCode networkCode, Map<String, ?> map) {

    }

    @Override
    public void reportError(MotionDna.ErrorCode errorCode, String s) {

    }

    @Override
    public Context getAppContext() {
        return this.getApplicationContext();
    }

    @Override
    public PackageManager getPkgManager() {
        return getPackageManager();
    }

}