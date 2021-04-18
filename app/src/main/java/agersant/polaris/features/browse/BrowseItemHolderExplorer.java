package agersant.polaris.features.browse;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.R;
import agersant.polaris.api.API;


class BrowseItemHolderExplorer extends BrowseItemHolder {

    private final ImageView icon;
    private final TextView text;

    BrowseItemHolderExplorer(API api, PlaybackQueue playbackQueue, BrowseAdapter adapter, View itemView, View itemQueueStatusView) {
        super(api, playbackQueue, adapter, itemView, itemQueueStatusView);
        text = itemView.findViewById(R.id.text);
        icon = itemView.findViewById(R.id.icon);
        itemView.setOnClickListener(this);
    }

    @Override
    void bindItem(CollectionItem item) {
        super.bindItem(item);
        text.setText(item.getName());

        int res;
        if (item.isDirectory()) {
            res = R.drawable.ic_folder_open_black_24dp;
        } else {
            res = R.drawable.ic_audiotrack_black_24dp;
        }
        icon.setImageResource(res);
    }
}
