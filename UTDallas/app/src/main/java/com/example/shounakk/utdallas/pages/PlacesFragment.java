package com.example.shounakk.utdallas.pages;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.example.shounakk.utdallas.Helper;
import com.example.shounakk.utdallas.R;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import map.MapSearchPreviewFragment;


public class PlacesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;

    public PlacesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places, container, false);
        ScrollView scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ScrollListener(scrollView));

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(this);

        Fragment msfp = MapSearchPreviewFragment.newInstance(Helper.UTD_CENTER_LATLNG, false);
        getFragmentManager().beginTransaction().add(R.id.mapPreviewContainer, msfp).commit();

        ImageView bgImage = (ImageView) view.findViewById(R.id.bgImage);

        Picasso.with(getActivity())
                .load(R.drawable.background)
                .fit()
                .centerCrop()
                .into(bgImage);

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