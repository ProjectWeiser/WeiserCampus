package map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shounakk.utdallas.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import map.slidinguppanellibrary.HeaderAdapter;
import map.slidinguppanellibrary.LockableRecyclerView;
import map.slidinguppanellibrary.SlidingUpPanelLayout;

public class MapSearchFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        SlidingUpPanelLayout.PanelSlideListener, LocationListener, HeaderAdapter.ItemClickListener, OnMapReadyCallback, OnMarkerClickListener {

    private static final String ARG_LOCATION = "arg.location";
    private static final String ARG_INITIAL_PLACE = "arg.initialplace";

    // All Views inside this fragment
    private LockableRecyclerView mListView;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private RelativeLayout mSlidingContainer;
    private FrameLayout mMapContainerFrame;
    private LinearLayout mSelectedItemView;
    private TextView mSelectedClear, mSelectedTitle, mSelectedBody;
    private View mTransparentView;
    private View mWhiteSpaceView;

    // ListView HeaderAdapter
    private HeaderAdapter mHeaderAdapter;

    // Location of the place's position and marker
    private LatLng mLocation;

    // Map Fragment and supporting objects
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // List of markers and markers selected
    private ArrayList<Marker> mMarkers;
    private Marker mMarkerSelected;

    // Location Variables
    private Marker mLocationMarker;
    private boolean mIsNeedLocationUpdate = true;

    // Data for the selected item and initial place
    private MapSearchListItemData mSelectedItem, mInitialPlace;


    public static MapSearchFragment newInstance(LatLng location) {
        MapSearchFragment f = new MapSearchFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_LOCATION, location);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_search, container, false);

        // Initialize markers list
        mMarkers = new ArrayList<>();

        // Set up listview and set to it can't overscroll
        mListView = (LockableRecyclerView) rootView.findViewById(android.R.id.list);
        mListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);

        mSlidingContainer = (RelativeLayout) rootView.findViewById(R.id.slidingContainer);

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) rootView.findViewById(R.id.slidingLayout);
        mSlidingUpPanelLayout.setEnableDragViewTouchEvents(true);

        // Set the height of the map and panellayout
        int mapHeight = getResources().getDimensionPixelSize(R.dimen.map_height);
        mSlidingUpPanelLayout.setPanelHeight(500);
        mSlidingUpPanelLayout.setScrollableView(mListView, mapHeight);

        // Set slide listener of panellayout
        mSlidingUpPanelLayout.setPanelSlideListener(this);

        // transparent view at the top of ListView
        mTransparentView = rootView.findViewById(R.id.transparentView);
        mWhiteSpaceView = rootView.findViewById(R.id.whiteSpaceView);

        // Set transparent view to invisible
        mTransparentView.setVisibility(View.INVISIBLE);

        mMapContainerFrame = (FrameLayout) rootView.findViewById(R.id.mapContainerFrame);

        // Set up selected item view
        mSelectedItemView = (LinearLayout) rootView.findViewById(R.id.selectedItemView);
        mSelectedItemView.setVisibility(View.GONE);
        mSelectedClear = (TextView) rootView.findViewById(R.id.filteredClear);
        mSelectedTitle = (TextView) rootView.findViewById(R.id.filteredTitle);
        mSelectedBody = (TextView) rootView.findViewById(R.id.filteredBody);

        // Clear filter when "Clear" button is pressed
        mSelectedClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnfiltered();
            }
        });

        // Expand the map
        expandMap();

        mSlidingUpPanelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSlidingUpPanelLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mSlidingUpPanelLayout.onPanelDragged(0);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set place location
        mLocation = getArguments().getParcelable(ARG_LOCATION);

        // Create a new map and add it to the container
        mMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapContainer, mMapFragment, "map");
        fragmentTransaction.commit();

        // show white bg if there are not too many items
        mWhiteSpaceView.setVisibility(View.VISIBLE);

        // Set headeradapter and set it on the listview
        mHeaderAdapter = new HeaderAdapter(getActivity(), setData(), this);
        mListView.setItemAnimator(null);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(layoutManager);
        mListView.setAdapter(mHeaderAdapter);

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

                // Move map and zoom to the location of the place
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(mLocation, 16.0f)));

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        // Don't move the map to user's location
                        mIsNeedLocationUpdate = false;

                        moveToLocation(latLng, false);

                        // Set the list to be unfiltered
                        if(mSelectedItemView.getVisibility() == View.VISIBLE)
                            setUnfiltered();
                    }
                });

                // Set the marker click listener
                mMap.setOnMarkerClickListener(this);

                // Set up the place markers
                setUpMarkers();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // In case Google Play services has since become available.
        setUpMapIfNeeded();

        // Fade the locationbar
        animateLocationBar(true);
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

        // Fade in the locationbar
        animateLocationBar(false);
        super.onStop();
    }

    /*private LatLng getLastKnownLocation() {
        return getLastKnownLocation(true);
    }

    private LatLng getLastKnownLocation(boolean isMoveMarker) {
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        String provider = lm.getBestProvider(criteria, true);
        if (provider == null) {
            return null;
        }
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
        }
        Location loc = lm.getLastKnownLocation(provider);
        if (loc != null) {
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            if (isMoveMarker) {
                moveMarker(latLng);
            }
            return latLng;
        }
        return null;
    }*/

    // Move the marker to a given location
    private void moveMarker(LatLng latLng) {
        if (mLocationMarker != null) {
            mLocationMarker.remove();
        }
    }

    /*private void moveToLocation(Location location) {
        if (location == null) {
            return;
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        moveToLocation(latLng);
    }

    private void moveToLocation(LatLng latLng) {
        moveToLocation(latLng, true);
    }*/

    private void moveToLocation(LatLng latLng, final boolean moveCamera) {
        if (latLng == null) {
            return;
        }
        moveMarker(latLng);

        mListView.post(new Runnable() {
            @Override
            public void run() {
                if (mMap != null && moveCamera) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(mLocation, 16f)));
                }
            }
        });
    }

    private void collapseMap() {
        mListView.setScrollingEnabled(true);
    }

    private void expandMap() {
        if (mHeaderAdapter != null) {
            mHeaderAdapter.hideSpace();
        }
        mTransparentView.setVisibility(View.INVISIBLE);
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16f), 1000, null);
        }
        mListView.setScrollingEnabled(false);
    }

    @Override
    public void onPanelSlide(View view, float v) {
    }

    @Override
    public void onPanelCollapsed(View view) {
        expandMap();
    }

    @Override
    public void onPanelExpanded(View view) {
        collapseMap();
    }

    @Override
    public void onPanelAnchored(View view) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(1);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onItemClicked(int position) {
        int i = 0;

        // If there are items
        if(mHeaderAdapter.getItemCount() > 0) {

            // Loop through each item
            for(MapSearchListItemData item : mHeaderAdapter.getData()) {

                // Set each marker related to the respective item to the image of "not here"
                mMarkers.get(i).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location));
                i++;
            }
        }

        // Set the marker related to the item clicked to image of "here"
        mMarkers.get(position - 1).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location_2));

        // collapse pane
        mSlidingUpPanelLayout.collapsePane();

        // Move camera to marker related to item pressed
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mHeaderAdapter.getItem(position).getLocation(), 16f), 350, null);

        // Filter to item selected
        setFiltered(mHeaderAdapter.getItem(position));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public void setUpMarkers() {

        // Loop through each item
        for(MapSearchListItemData item : mHeaderAdapter.getData()) {
            int resId = 0;
            Marker marker;

            // If the item is the place the user is at
            if(item.getIsHere())

                // Set marker to "here" image
                resId = R.drawable.ic_action_location_2;
            else

                // Set marker to "not-here" image
                resId = R.drawable.ic_action_location;

            // Add marker to map
            marker = mMap.addMarker(new MarkerOptions().position(item.getLocation()).title(item.getNickname()).icon(BitmapDescriptorFactory.fromResource(resId)));

            // Add marker to markers list
            if(marker != null)
                mMarkers.add(marker);
        }
    }

    public ArrayList<MapSearchListItemData> setData() {
        // Temp
        String description = "From eateries to meeting spaces, to a range of departments within student affairs, the Student Union is the hub of campus activity. Stop by to eat, have fun with friends, and become a part of the Comet community.";

        ArrayList<MapSearchListItemData> data = new ArrayList<>();
        data.add(new MapSearchListItemData("Student Union", "The SU", 3, "Nearly Empty", new LatLng(32.986739, -96.748907), true, description));
        data.add(new MapSearchListItemData("Eric Jonnson School of Engineering", "ECS", 6, "Very Crowded", new LatLng(32.986314, -96.750435), false, description));
        data.add(new MapSearchListItemData("Arts and Technology", "ATEC", 3, "Nearly Empty", new LatLng(32.986090, -96.747614), false, description));
        data.add(new MapSearchListItemData("Eugene McDermott Library", "MC", 6, "Lightly Crowded", new LatLng(32.987025, -96.747631), false, description));
        data.add(new MapSearchListItemData("Naveen Jindal School of Management", "JSOM", 4, "Moderately Crowded", new LatLng(32.985137, -96.747724), false, description));
        data.add(new MapSearchListItemData("The Activity Center", "The AC", 3, "Nearly Empty", new LatLng(32.985277, -96.749809), false, description));
        data.add(new MapSearchListItemData("Hoblitzelle Hall", "HH", 6, "Very Crowded", new LatLng(32.986958, -96.751624), false, description));

        // TODO: Pull data from firebase sorted by distance

        return data;
    }

    public void setFiltered(MapSearchListItemData item) {

        // Animate the list to the bottom if it's visible
        if(mSlidingContainer.getVisibility() == View.VISIBLE)
            animateDown();
        else {

            // Fade out old data
            Animation fade_out = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
            mSelectedTitle.startAnimation(fade_out);
            mSelectedBody.startAnimation(fade_out);
        }

        // Scroll list to top
        mListView.scrollToPosition(0);

        // Set selectedItemView data
        mSelectedTitle.setText(item.getName());
        mSelectedBody.setText(item.getDescription());

        // Animate fade in of new data
        Animation fade_in = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        mSelectedTitle.startAnimation(fade_in);
        mSelectedBody.startAnimation(fade_in);

        // Set selected item
        mSelectedItem = item;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        // Loop throug each marker
        int i = 1;
        for(Marker m : mMarkers) {

            // If the marker is equal to the one clicked
            if(m.equals(marker)) {

                // Filter to the item related to the marker
                setFiltered(mHeaderAdapter.getItem(i));

                // Animate and zoom to the marker
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mHeaderAdapter.getItem(i).getLocation(), 16f), 350, null);
                return true;
            }

            i++;
        }

        // TODO: Check if the marker clicked is related to the item filtered. If so, move to place

        return false;
    }

    public void setUnfiltered() {

        // Set the slide_down animation to a variable
        Animation down = AnimationUtils.loadAnimation(getContext(),
                R.anim.slide_down);

        // Set an animationlistener to the animation
        down.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                // Switch visibility of selected item and list
                mSlidingContainer.setVisibility(View.VISIBLE);
                mSelectedItemView.setVisibility(View.GONE);

                // Animate the list to slide up
                Animation up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                mSlidingContainer.startAnimation(up);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // Start the animation
        mSelectedItemView.startAnimation(down);
    }

    private void animateDown() {

        // Set the slide_down animation to a variable
        Animation down = AnimationUtils.loadAnimation(getContext(),
                R.anim.slide_down);

        // Set an animationlistener to the animation
        down.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Switch visibility of selected item and list
                mSlidingContainer.setVisibility(View.GONE);
                mSelectedItemView.setVisibility(View.VISIBLE);

                // Animate the list to slide up
                Animation up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                mSelectedItemView.startAnimation(up);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // Start the animation
        mSlidingContainer.startAnimation(down);
    }

    private void animateLocationBar(boolean isVisible) {
        int fadeResourceId = 0;
        float alpha = 0f;

        // Set the alpha and resourceId based on whether the locationbar is currently visible
        if(isVisible) {
            fadeResourceId = R.anim.fade_out;
        }
        else {
            fadeResourceId = R.anim.fade_in;
            alpha = 1f;
        }

        // Animation needs a final alpha
        final float finalAlpha = alpha;

        final RelativeLayout locationBar = (RelativeLayout) getActivity().findViewById(R.id.locationBar);

        // Set the animation resource
        Animation animation = AnimationUtils.loadAnimation(getContext(), fadeResourceId);

        // Create an animationlistener for the animation
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                // After the animation is over, set the alpha of the locationBar to either fully showing or invisible
                locationBar.setAlpha(finalAlpha);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // Start the animation
        locationBar.startAnimation(animation);
    }

    private void moveToPlace(MapSearchListItemData item) {

        // TODO: replace places fragment with new place via passing the place id | Close this fragment
        //Toast.makeText(getContext(), "Moving to Place: " + item.getName(), 2);
    }

}
