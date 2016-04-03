package com.example.shounakk.utdallas;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.google.android.gms.maps.model.LatLng;

import map.MapSearchPreviewFragment;


public class PlacesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private SwipeRefreshLayout swipeRefreshLayout;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlacesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlacesFragment newInstance(String param1, String param2) {
        PlacesFragment fragment = new PlacesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PlacesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places, container, false);
        ScrollView scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ScrollListener(scrollView));

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(this);

        // Add MapSearchPreviewFragment to container
        Fragment msfp = MapSearchPreviewFragment.newInstance(Helper.UTD_CENTER_LATLNG, false);
        getFragmentManager().beginTransaction().add(R.id.mapPreviewContainer, msfp).commit();

        return view;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    class ScrollListener implements ViewTreeObserver.OnScrollChangedListener {

        private final int DISTANCE = 64;

        private ScrollView scrollView;
        private float factor;

        public ScrollListener(ScrollView scrollView) {
            this.scrollView = scrollView;
        }

        @Override
        public void onScrollChanged() {
            if (getView() == null) return;

            int syPx = scrollView.getScrollY();
            float sy = Helper.convertPixelsToDp(syPx, getContext());

            if (sy > DISTANCE) {
                sy = DISTANCE;
            }

            if (sy < 0) {
                sy = 0;
            }

            factor = sy / DISTANCE;

            int background = Helper.setColorAlpha(ContextCompat.getColor(getContext(), R.color.mat_grey_800), factor);

            RelativeLayout locationBar = (RelativeLayout) getView().findViewById(R.id.locationBar);

            locationBar.setBackgroundColor(background);
        }
    }

}
