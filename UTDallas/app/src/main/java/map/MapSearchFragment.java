package map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
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

import com.example.shounakk.utdallas.Helper;
import com.example.shounakk.utdallas.MainActivity;
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

import map.location.Place;
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
    private Place mSelectedItem, mInitialPlace;

    private ArrayList<Place> mPlaces;

    FilterState state;

    private enum FilterState {
        NONE,
        BUILDING,
        SUBPLACE
    }

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

        state = FilterState.BUILDING;

        // Initialize markers list
        mMarkers = new ArrayList<>();

        mPlaces = new ArrayList<>();

        // Set up listview and set to it can't overscroll
        mListView = (LockableRecyclerView) rootView.findViewById(android.R.id.list);
        mListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);

        mSlidingContainer = (RelativeLayout) rootView.findViewById(R.id.slidingContainer);

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) rootView.findViewById(R.id.slidingLayout);
        mSlidingUpPanelLayout.setEnableDragViewTouchEvents(true);

        // Set the height of the map and panellayout
        int mapHeight = (int) getResources().getDimension(R.dimen.scrollable_padding);
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
                setUnfiltered(true);
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
                        if(mSelectedItemView.getVisibility() == View.GONE)
                            setUnfiltered(false);
                        else
                            setUnfiltered(true);
                    }
                });

                // Set the marker click listener
                mMap.setOnMarkerClickListener(this);

                // Set up the place markers
                setUpPlaces();
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
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(mLocation, Helper.BUILDING_ZOOM)));
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

        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Execute location service call if user has explicitly granted ACCESS_FINE_LOCATION..
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onItemClicked(int position) {
        if(position == 0)
            return;

        int i = 0;

        // If there are items


        // collapse pane
        mSlidingUpPanelLayout.collapsePane();

        // Move camera to marker related to item pressed
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mHeaderAdapter.getItem(position).getLocation(), Helper.BUILDING_ZOOM), 350, null);

        if(mHeaderAdapter.getSelectedPlace() != null)
            setBuildingFilter(mHeaderAdapter.getSelectedPlace().getSubPlaces().get(position - 1));
        else
            setBuildingFilter(mPlaces.get(position - 1));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public void setUpPlaces() {

        // Loop through each item
        for(Place item : mHeaderAdapter.getData()) {
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

            item.setMarker(marker);

            for(Place subPlace : item.getSubPlaces()) {
                marker = mMap.addMarker(new MarkerOptions().position(subPlace.getLocation()).title(subPlace.getNickname()).icon(BitmapDescriptorFactory.fromResource(resId)));
                subPlace.setMarker(marker);
                subPlace.setMarkerVisible(false);
            }
        }
    }

    public ArrayList<Place> setData() {
        // Temp
        String description = "From eateries to meeting spaces, to a range of departments within student affairs, the Student Union is the hub of campus activity. Stop by to eat, have fun with friends, and become a part of the Comet community.";

        mPlaces.add(new Place("Student Union", "The SU", 3, "Nearly Empty", new LatLng(32.986739, -96.748907), true, description, false));
        mPlaces.add(new Place("Eric Jonnson School of Engineering", "ECS", 6, "Very Crowded", new LatLng(32.986314, -96.750435), false, description, false));
        mPlaces.add(new Place("Arts and Technology", "ATEC", 3, "Nearly Empty", new LatLng(32.986090, -96.747614), false, description, false));
        mPlaces.add(new Place("Eugene McDermott Library", "MC", 6, "Lightly Crowded", new LatLng(32.987025, -96.747631), false, description, false));
        mPlaces.add(new Place("Naveen Jindal School of Management", "JSOM", 4, "Moderately Crowded", new LatLng(32.985137, -96.747724), false, description, false));
        mPlaces.add(new Place("The Activity Center", "The AC", 3, "Nearly Empty", new LatLng(32.985277, -96.749809), false, description, false));
        mPlaces.add(new Place("Hoblitzelle Hall", "HH", 6, "Very Crowded", new LatLng(32.986958, -96.751624), false, description, false));

        ArrayList<Place> subPlaces = new ArrayList<>();
        subPlaces.add(new Place("TI Auditorium", "TI", 6, "Very Crowded", new LatLng(32.986104, -96.750481), false, description, true));
        subPlaces.add(new Place("ECS Counselors Office", "ECO", 6, "Very Crowded", new LatLng(32.986455, -96.750646), false, description, true));

        mPlaces.get(1).setSubPlaces(subPlaces);

        subPlaces = new ArrayList<>();
        subPlaces.add(new Place("Main Lobby", "ML", 6, "Very Crowded", new LatLng(32.985942, -96.747791), false, description, true));
        subPlaces.add(new Place("1.102", "TEST", 3, "Lightly Crowded", new LatLng(32.985916, -96.747335), false, description, true));

        mPlaces.get(2).setSubPlaces(subPlaces);

        // TODO: Pull data from firebase sorted by distance

        return mPlaces;
    }

    public void setLeafFiltered(Place item) {

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
        // Loop through each marker
        int i = 1;
       /* for(Marker m : mMarkers) {

            // If the marker is equal to the one clicked
            if(m.equals(marker)) {

                // Filter to the item related to the marker
                setFiltered(mHeaderAdapter.getItem(i));

                // Animate and zoom to the marker
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mHeaderAdapter.getItem(i).getLocation(), Helper.BUILDING_ZOOM), 350, null);
                return true;
            }

            i++;
        }*/


        for(Place place : mPlaces) {
            if(place.getMarker().getId().equals(marker.getId())) {
                setBuildingFilter(place);
                break;
            }
            for(Place subPlace : place.getSubPlaces()) {
                if(subPlace.getMarker().getId().equals(marker.getId())) {
                    setBuildingFilter(subPlace);
                    break;
                }
            }
        }

        return false;
    }

    public void setUnfiltered(boolean animate) {

        mHeaderAdapter.setState(false);
        mHeaderAdapter.setSelectedPlace(null);
        mHeaderAdapter.notifyDataSetChanged();

        if(animate) {
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

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Helper.UTD_CENTER_LATLNG, Helper.BUILDING_ZOOM), 350, null);

        for(Place place : mPlaces) {
            place.setSubPlacesVisible(false);
        }
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

    private void moveToPlace(Place item) {

        // TODO: replace places fragment with new place via passing the place id | Close this fragment
        //Toast.makeText(getContext(), "Moving to Place: " + item.getName(), 2);
    }

    public void setBuildingFilter(final Place place) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLocation()), 350, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        ArrayList<Place> places;
                        if(place.isSubPlace()) {
                            places = new ArrayList<Place>();
                            places.add(place);
                        }
                        else
                            places = mPlaces;

                        for(Place b : places) {
                            if(b == place) {
                                // Filter to item selected
                                if(b.getSubPlaces().isEmpty()) {
                                    Log.d("DEBUG", "Empty");
                                    setLeafFiltered(b);
                                }
                                else {
                                    b.setSubPlacesVisible(true);
                                    mHeaderAdapter.setSelectedPlace(b);
                                    mHeaderAdapter.setState(true);
                                    mHeaderAdapter.notifyDataSetChanged();
                                    mHeaderAdapter.notifyItemChanged(0);
                                    Log.d("DEBUG", "Not empty");
                                }
                            }
                            else
                                b.setSubPlacesVisible(false);
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLocation(), Helper.SUBPLACE_ZOOM));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        }, 500);
    }
}
