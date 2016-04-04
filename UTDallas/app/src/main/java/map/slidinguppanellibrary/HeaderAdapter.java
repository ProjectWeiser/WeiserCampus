package map.slidinguppanellibrary;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shounakk.utdallas.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import map.location.Place;

public class HeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private ArrayList<Place> mPlaces;
    public Place selectedPlace;

    private LayoutInflater mLayoutInflater;

    private enum FilteredState {
        FILTERED,
        UNFILTERED
    }

    private FilteredState state = FilteredState.UNFILTERED;

    private boolean mIsSpaceVisible;

    public void setState(boolean filtered) {
        if(filtered)
            state = FilteredState.FILTERED;
        else
            state = FilteredState.UNFILTERED;
    }

    public void setSelectedPlace(Place place) {
        selectedPlace = place;
    }

    public Place getSelectedPlace() {
        return selectedPlace;
    }

    public interface ItemClickListener {
        void onItemClicked(int position);
    }

    private WeakReference<ItemClickListener> mCallbackRef;

    public HeaderAdapter(Context ctx, ArrayList<Place> data, ItemClickListener listener) {
        mLayoutInflater = LayoutInflater.from(ctx);
        mPlaces = data;
        mCallbackRef = new WeakReference<>(listener);
        this.hideSpace();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            View v = mLayoutInflater.inflate(R.layout.map_search_list_item, parent, false);
            return new MapSearchListItem(v);
        } else if (viewType == TYPE_HEADER) {
            View v;
            if(selectedPlace != null) {
                v = mLayoutInflater.inflate(R.layout.filtered_header_view, parent, false);
            }
            else {
                v = mLayoutInflater.inflate(R.layout.unfiltered_header_view, parent, false);
            }
            return new HeaderItem(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MapSearchListItem) {
            Place dataItem = getItem(position);

            ((MapSearchListItem) holder).mName.setText(dataItem.getName());
            ((MapSearchListItem) holder).mTimeWalk.setText(dataItem.getTimeWalk() + " minute walk");
            ((MapSearchListItem) holder).mCrowdStatus.setText(dataItem.getCrowdStatus());
            ((MapSearchListItem) holder).mPosition = position;
        } else if (holder instanceof HeaderItem) {
//            ((HeaderItem) holder).mSpaceView.setVisibility(mIsSpaceVisible ? View.VISIBLE : View.GONE);
            if(selectedPlace != null) {
                //((HeaderItem) holder).mTitle.setText(selectedPlace.getName());
                //((HeaderItem) holder).mDescription.setText(selectedPlace.getDescription());
            }

            ((HeaderItem) holder).mPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        if(selectedPlace == null)
            return mPlaces.size() + 1;
        else
            return selectedPlace.getSubPlaces().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    public Place getItem(int position) {
        if(selectedPlace == null)
            return mPlaces.get(position - 1);
        else
            return selectedPlace.getSubPlaces().get(position - 1);
    }

    // Custom class to populate the ListItems
    class MapSearchListItem extends HeaderItem {
        private TextView mName;
        private TextView mTimeWalk;
        private TextView mCrowdStatus;

        public MapSearchListItem(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.placeNameItem);
            mTimeWalk = (TextView) itemView.findViewById(R.id.walkDistanceItem);
            mCrowdStatus = (TextView) itemView.findViewById(R.id.crowdStatusItem);
        }
    }

    class HeaderItem extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mSpaceView;
        TextView mTitle;
        TextView mDescription;
        int mPosition;

        public HeaderItem(View itemView) {
            super(itemView);
           // mSpaceView = itemView.findViewById(R.id.space);
            setUpViews();
            itemView.setOnClickListener(this);
        }

        public void setUpViews() {
            mTitle = (TextView) itemView.findViewById(R.id.filteredTitleList);
            mDescription = (TextView) itemView.findViewById(R.id.filteredBodyList);
        }

        @Override
        public void onClick(View v) {
            ItemClickListener callback = mCallbackRef != null ? mCallbackRef.get() : null;
            if (callback != null) {
                callback.onItemClicked(mPosition);
            }

        }
    }

    public void hideSpace() {
        mIsSpaceVisible = false;
        notifyItemChanged(0);
    }

    public void showSpace() {
        mIsSpaceVisible = true;
        notifyItemChanged(0);
    }

    public ArrayList<Place> getData() {
        return mPlaces;
    }
}