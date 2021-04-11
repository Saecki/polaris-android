package agersant.polaris.features.browse;

import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.R;
import agersant.polaris.api.API;

import static agersant.polaris.features.browse.BrowseAdapterAlbum.AlbumViewType.DISC_HEADER;
import static agersant.polaris.features.browse.BrowseAdapterAlbum.AlbumViewType.TRACK;


class BrowseAdapterAlbum extends BrowseAdapter {

    private SparseIntArray discSizes; // Key is disc number, value is number of tracks
    private int numDiscHeaders;
    private final API api;
    private final PlaybackQueue playbackQueue;

    @Override
    void setItems(ArrayList<? extends CollectionItem> items) {
        discSizes = new SparseIntArray();
        for (CollectionItem item : items) {
            int discNumber = item.getDiscNumber();
            discSizes.put(discNumber, 1 + discSizes.get(discNumber, 0));
        }
        numDiscHeaders = discSizes.size();
        if (numDiscHeaders == 1) {
            numDiscHeaders = 0;
        }
        super.setItems(items);
    }

    BrowseAdapterAlbum(API api, PlaybackQueue playbackQueue) {
        super();
        this.api = api;
        this.playbackQueue = playbackQueue;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + numDiscHeaders;
    }

    @Override
    public int getItemViewType(int position) {
        int currentDiscStart = 0;
        for (int discIndex = 0; discIndex < numDiscHeaders; discIndex++) {
            int currentDiscSize = discSizes.valueAt(discIndex);
            if (position == currentDiscStart) {
                return DISC_HEADER.ordinal();
            } else if (position < currentDiscStart + currentDiscSize + 1) {
                return TRACK.ordinal();
            }
            currentDiscStart += currentDiscSize + 1;
        }

        return TRACK.ordinal();
    }

    @Override
    public BrowseItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemQueueStatusView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_browse_item_queued, parent, false);
        if (viewType == DISC_HEADER.ordinal()) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_browse_album_disc_header, parent, false);
            return new BrowseItemHolderAlbumDiscHeader(api, playbackQueue, this, itemView, itemQueueStatusView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_browse_album_item, parent, false);
            return new BrowseItemHolderAlbumTrack(api, playbackQueue, this, itemView, itemQueueStatusView);
        }
    }

    @Override
    public void onBindViewHolder(BrowseItemHolder holder, int position) {

        if (holder instanceof BrowseItemHolderAlbumTrack) {

            // Assign track item
            if (numDiscHeaders > 0) {
                int offset = 1;
                int currentDiscStart = 0;
                for (int discIndex = 0; discIndex < numDiscHeaders; discIndex++) {
                    int currentDiscSize = discSizes.valueAt(discIndex);
                    if (position < currentDiscStart + currentDiscSize + 1) {
                        break;
                    }
                    currentDiscStart += currentDiscSize + 1;
                    offset += 1;
                }

                holder.bindItem(items.get(position - offset));
            } else {
                holder.bindItem(items.get(position));
            }
        } else if (holder instanceof BrowseItemHolderAlbumDiscHeader) {

            // Assign disc number
            BrowseItemHolderAlbumDiscHeader header = (BrowseItemHolderAlbumDiscHeader) holder;

            int currentDiscStart = 0;
            for (int discIndex = 0; discIndex < numDiscHeaders; discIndex++) {
                int currentDiscSize = discSizes.valueAt(discIndex);
                if (position == currentDiscStart) {
                    header.setDiscNumber(discSizes.keyAt(discIndex));
                    break;
                }
                currentDiscStart += currentDiscSize + 1;
            }
        }
    }

    enum AlbumViewType {
        DISC_HEADER,
        TRACK,
    }
}
