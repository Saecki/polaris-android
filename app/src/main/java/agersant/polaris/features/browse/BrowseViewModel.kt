package agersant.polaris.features.browse

import agersant.polaris.CollectionItem
import agersant.polaris.PolarisApplication
import agersant.polaris.api.ItemsCallback
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BrowseViewModel : ViewModel() {
    private val mItems: MutableLiveData<ArrayList<out CollectionItem>> = MutableLiveData(ArrayList())
    private val mError = MutableLiveData(false)
    private val mFetching = MutableLiveData(false)

    val api = PolarisApplication.getState().api
    val serverAPI = PolarisApplication.getState().serverAPI
    val playbackQueue = PolarisApplication.getState().playbackQueue
    val items: LiveData<ArrayList<out CollectionItem>> = mItems
    val fetching: LiveData<Boolean> = mFetching
    val error: LiveData<Boolean> = mError
    var initialCreation = true

    private val fetchCallback = object : ItemsCallback {
        override fun onSuccess(items: ArrayList<out CollectionItem>) {
            mItems.postValue(items)
            mFetching.postValue(false)
            mError.postValue(false)
        }

        override fun onError() {
            mItems.postValue(ArrayList())
            mFetching.postValue(false)
            mError.postValue(true)
        }
    }

    fun loadPath(path: String) {
        mFetching.value = true
        mError.value = false
        api.browse(path, fetchCallback)
    }

    fun loadRandom() {
        mFetching.value = true
        mError.value = false
        serverAPI.getRandomAlbums(fetchCallback)
    }

    fun loadRecent() {
        mFetching.value = true
        mError.value = false
        serverAPI.getRecentAlbums(fetchCallback)
    }
}
