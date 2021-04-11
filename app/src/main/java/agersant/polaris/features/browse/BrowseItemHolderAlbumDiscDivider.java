package agersant.polaris.features.browse;


import android.view.View;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.api.API;


class BrowseItemHolderAlbumDiscDivider extends BrowseItemHolder {

    BrowseItemHolderAlbumDiscDivider(API api, PlaybackQueue playbackQueue, BrowseAdapter adapter, View itemView, View itemQueueStatusView) {
        super(api, playbackQueue, adapter, itemView, itemQueueStatusView);
    }

    @Override
    public void onClick(View view) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSwiped(View view) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindItem(CollectionItem item) {
        throw new UnsupportedOperationException();
    }
}
