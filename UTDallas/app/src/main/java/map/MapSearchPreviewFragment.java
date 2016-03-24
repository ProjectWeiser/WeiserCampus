package map;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shounakk.utdallas.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Preview of the current location in Google Maps
 */
public class MapSearchPreviewFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String ARG_LOCATION = "arg.location";
    private static final String ARG_HERE = "arg.here";

    // Location of Place and the marker
    private LatLng mLocation;
    private Marker mLocationMarker;

    // Map Fragment and supporting objects
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // Is the user at this place?
    private boolean mIsHere;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_search_preview, container, false);
    }

    public static MapSearchPreviewFragment newInstance(LatLng location, boolean here) {
        MapSearchPreviewFragment f = new MapSearchPreviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_LOCATION, location);
        args.putBoolean(ARG_HERE, here);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set location and whether the user is here
        mLocation = getArguments().getParcelable(ARG_LOCATION);
        mIsHere = getArguments().getBoolean(ARG_HERE);

        // Create the map and add to the fragment container
        mMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapPreviewInsideContainer, mMapFragment, "map");
        fragmentTransaction.commit();

        // Create the client to handle the Google API
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Set up the map
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = mMapFragment.getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                try {
                    // Remove marker from current location
                    mMap.setMyLocationEnabled(false);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                // Turn off undesired options
                mMap.getUiSettings().setCompassEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setAllGesturesEnabled(false);

                // Move map and zoom to the location of the place
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(mLocation, 18.0f)));

                // Marker resource id
                int resId = 0;

                // Set marker resource id
                if(mIsHere)
                    resId = R.drawable.ic_action_location_2;
                else
                    resId = R.drawable.ic_action_location;

                // Add the marker to the map
                mLocationMarker = mMap.addMarker(new MarkerOptions().position(mLocation).icon(BitmapDescriptorFactory.fromResource(resId)));

                // Set map click listener
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        vibrateAndOpenMap();
                    }
                });

                // Set marker click listener
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        vibrateAndOpenMap();
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // In case Google Play services has since become available.
        setUpMapIfNeeded();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(1);
    }

    public void vibrateAndOpenMap() {
        // Vibrate
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);

        // Open MapSearchFragment
        map.MapSearchFragment fragment = map.MapSearchFragment.newInstance(mLocation);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        ft.add(R.id.mapMainContainer, fragment).addToBackStack(null).commit();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
