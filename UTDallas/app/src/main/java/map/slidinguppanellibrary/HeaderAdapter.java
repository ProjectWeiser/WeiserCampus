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

import map.MapSearchListItemData;

public class HeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private ArrayList<MapSearchListItemData> mData;

    private LayoutInflater mLayoutInflater;

    private boolean mIsSpaceVisible = true;

    public interface ItemClickListener {
        void onItemClicked(int position);
    }

    private WeakReference<ItemClickListener> mCallbackRef;

    public HeaderAdapter(Context ctx, ArrayList<MapSearchListItemData> data, ItemClickListener listener) {
        mLayoutInflater = LayoutInflater.from(ctx);
        mData = data;
        mCallbackRef = new WeakReference<>(listener);
        this.hideSpace();

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            View v = mLayoutInflater.inflate(R.layout.map_search_list_item, parent, false);
            return new MapSearchListItemNoFilter(v);
        } else if (viewType == TYPE_HEADER) {
            View v = mLayoutInflater.inflate(R.layout.transparent_header_view, parent, false);
            return new HeaderItem(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MapSearchListItemNoFilter) {
            MapSearchListItemData dataItem = getItem(position);
            ((MapSearchListItemNoFilter) holder).mName.setText(dataItem.getName());
            ((MapSearchListItemNoFilter) holder).mTimeWalk.setText(dataItem.getTimeWalk() + " minute walk");
            ((MapSearchListItemNoFilter) holder).mCrowdStatus.setText(dataItem.getCrowdStatus());
            ((MapSearchListItemNoFilter) holder).mPosition = position;
        } else if (holder instanceof HeaderItem) {
            ((HeaderItem) holder).mSpaceView.setVisibility(mIsSpaceVisible ? View.VISIBLE : View.GONE);
            ((HeaderItem) holder).mPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    public MapSearchListItemData getItem(int position) {
        return mData.get(position - 1);
    }

    // Custom class to populate the ListItems
    class MapSearchListItemNoFilter extends HeaderItem {
        private TextView mName;
        private TextView mTimeWalk;
        private TextView mCrowdStatus;

        public MapSearchListItemNoFilter(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.placeNameItem);
            mTimeWalk = (TextView) itemView.findViewById(R.id.walkDistanceItem);
            mCrowdStatus = (TextView) itemView.findViewById(R.id.crowdStatusItem);
        }
    }

    class HeaderItem extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mSpaceView;
        int mPosition;

        public HeaderItem(View itemView) {
            super(itemView);
            mSpaceView = itemView.findViewById(R.id.space);
            itemView.setOnClickListener(this);
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

    public ArrayList<MapSearchListItemData> getData() {
        return mData;
    }
}