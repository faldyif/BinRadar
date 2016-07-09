package id.ristech.binradar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener {

    private MapView mapView;
    private GoogleMap googleMap;
    private CameraPosition cameraPosition;

    private static final String[] LOCATION_PERMISSION = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int INITIAL_REQUEST = 1337;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST +1;

    private Marker tempMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout

        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    MarkerOptions baru;

    @Override
    public void onMapReady(final GoogleMap map) {
        // Map initialization
        googleMap = map;
        setUpMap();

        // Listen to floating action button (add new marker)
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the added marker before to avoid memory leak
                if(tempMarker != null) {
                    tempMarker.remove();
                    tempMarker = null;
                }

                // Create new marker based on the center location of the camera
                final LatLng center = map.getCameraPosition().target;
                baru = new MarkerOptions().position(center).title("New trash can").snippet("Click here to submit").draggable(true).icon(BitmapDescriptorFactory.fromResource(DRAWABLE_TRASH_ADD));
                tempMarker =  map.addMarker(baru);

                // Animate the added marker
                final Handler handler = new Handler();
                final long start = SystemClock.uptimeMillis();
                Projection proj = map.getProjection();
                Point startPoint = proj.toScreenLocation(center);
                startPoint.offset(0, -100);
                final LatLng startLatLng = proj.fromScreenLocation(startPoint);
                final long duration = 1500;
                final Interpolator interpolator = new BounceInterpolator();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        long elapsed = SystemClock.uptimeMillis() - start;
                        float t = interpolator.getInterpolation((float) elapsed / duration);
                        double lng = t * center.longitude + (1 - t) * startLatLng.longitude;
                        double lat = t * center.latitude + (1 - t) * startLatLng.latitude;
                        tempMarker.setPosition(new LatLng(lat, lng));
                        if (t < 1.0) {
                            // Post again 16ms later.
                            handler.postDelayed(this, 16);
                        }
                    }
                });
            }
        });
        placeMarkers();
    }

    public void setUpMap() {
        // Request for location permission first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(LOCATION_PERMISSION, LOCATION_REQUEST);
        } else {
            googleMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null)
            {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        }
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setTrafficEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-6.121435, 106.77412), 5));
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(tempMarker != null && marker.equals(tempMarker)) {
                    final LatLng pLatLng = tempMarker.getPosition();
                    final String userID = getArguments().getString("uid");
                    AddTrashDialog dialog = AddTrashDialog.newInstance(pLatLng.latitude,pLatLng.longitude,userID);

                    dialog.show(getActivity().getFragmentManager(), "Dialog");
                    tempMarker.remove();
                    tempMarker = null;
                }
            }
        });
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });
    }

    private int DRAWABLE_TRASH_SMALL = R.drawable.trash_small;
    private int DRAWABLE_TRASH_MEDIUM = R.drawable.trash_medium;
    private int DRAWABLE_TRASH_LARGE = R.drawable.trash_large;
    private int DRAWABLE_TRASH_ADD = R.drawable.trash_add;

    private DatabaseReference databaseReference;

    public void placeMarkers() {
        databaseReference = FirebaseDatabase.getInstance().getReference("trashes");
        databaseReference.keepSynced(false);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot trashSnapshot : dataSnapshot.getChildren()) {
                    Trash newTrash = trashSnapshot.getValue(Trash.class);
                    Double latitude = newTrash.getLatitude();
                    Double longitude = newTrash.getLongitude();
                    String description = newTrash.getDescription();
                    Integer type = newTrash.getType();
                    String userID = newTrash.getUserID();
                    Integer pinImage = null;
                    if(type == R.id.trash_small) {
                        pinImage = DRAWABLE_TRASH_SMALL;
                    } else if(type == R.id.trash_medium) {
                        pinImage = DRAWABLE_TRASH_MEDIUM;
                    } else {
                        pinImage = DRAWABLE_TRASH_LARGE;
                    }
                    googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(description).snippet("Click to report false data").icon(BitmapDescriptorFactory.fromResource(pinImage)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPause() {
        mapView.onPause();
        cameraPosition = googleMap.getCameraPosition();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraPosition != null) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            cameraPosition = null;
        }
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        cameraPosition = googleMap.getCameraPosition();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (cameraPosition != null) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            cameraPosition = null;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.hide();
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.show();
    }
}
