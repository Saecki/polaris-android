package agersant.polaris.api;

import java.util.List;

import agersant.polaris.CollectionItem;


public interface ItemsCallback {

    void onSuccess(List<CollectionItem> items);

    void onError();

}
