package agersant.polaris.api.remote;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaDataSource;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import agersant.polaris.CollectionItem;
import agersant.polaris.R;
import agersant.polaris.api.IPolarisAPI;
import agersant.polaris.ui.FetchImageTask;

public class ServerAPI
		implements IPolarisAPI {

	private static ServerAPI instance;
	private RequestQueue requestQueue;
	private SharedPreferences preferences;
	private Auth auth;

	private String serverAddressKey;
	private String usernameKey;
	private String passwordKey;

	private ServerAPI(Context context) {
		this.requestQueue = RequestQueue.getInstance(context);
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.auth = new Auth(this);

		serverAddressKey = context.getString(R.string.pref_key_server_url);
		usernameKey = context.getString(R.string.pref_key_username);
		passwordKey = context.getString(R.string.pref_key_password);
	}

	public static void init(Context context) {
		instance = new ServerAPI(context);
	}

	public static ServerAPI getInstance() {
		return instance;
	}

	String getURL() {
		String address = this.preferences.getString(serverAddressKey, "");
		address = address.replaceAll("/$", "");
		return address + "/api";
	}

	String getUsername() {
		return this.preferences.getString(usernameKey, "");
	}

	String getPassword() {
		return this.preferences.getString(passwordKey, "");
	}

	RequestQueue getRequestQueue() {
		return this.requestQueue;
	}

	private String getMediaURL(String path) {
		String serverAddress = this.getURL();
		return serverAddress + "/serve/" + path;
	}

	@Override
	public MediaDataSource getAudio(CollectionItem item) throws IOException {
		DownloadQueue downloadQueue = DownloadQueue.getInstance();
		return downloadQueue.getAudio(item);
	}

	@Override
	public void getImage(CollectionItem item, ImageView view) {
		FetchImageTask.load(item, view);
	}

	// Can block!
	public URLConnection serve(String path) throws InterruptedException, ExecutionException, TimeoutException, IOException {
		String url = getMediaURL(path);
		return auth.connect(url);
	}

	public void browse(String path, final Response.Listener<ArrayList<CollectionItem>> success, Response.ErrorListener failure) {
		String serverAddress = this.getURL();
		String requestURL = serverAddress + "/browse/" + path;

		Response.Listener<JSONArray> successWrapper = new Response.Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray response) {
				ArrayList<CollectionItem> items = new ArrayList<>(response.length());
				for (int i = 0; i < response.length(); i++) {
					try {
						JSONObject item = response.getJSONObject(i);
						CollectionItem browseItem = CollectionItem.parse(item);
						items.add(browseItem);
					} catch (Exception e) {
					}
				}
				success.onResponse(items);
			}
		};

		this.auth.doJsonArrayRequest(requestURL, successWrapper, failure);
	}

	public void getRandomAlbums(final Response.Listener<ArrayList<CollectionItem>> success, Response.ErrorListener failure) {
		String serverAddress = this.getURL();
		String requestURL = serverAddress + "/random/";

		Response.Listener<JSONArray> successWrapper = new Response.Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray response) {
				ArrayList<CollectionItem> items = new ArrayList<>(response.length());
				for (int i = 0; i < response.length(); i++) {
					try {
						JSONObject item = response.getJSONObject(i);
						CollectionItem browseItem = CollectionItem.parseDirectory(item);
						items.add(browseItem);
					} catch (Exception e) {
					}
				}
				success.onResponse(items);
			}
		};

		this.auth.doJsonArrayRequest(requestURL, successWrapper, failure);
	}

	public void flatten(String path, final Response.Listener<ArrayList<CollectionItem>> success, Response.ErrorListener failure) {
		String serverAddress = this.getURL();
		String requestURL = serverAddress + "/flatten/" + path;

		Response.Listener<JSONArray> successWrapper = new Response.Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray response) {
				ArrayList<CollectionItem> items = new ArrayList<>(response.length());
				for (int i = 0; i < response.length(); i++) {
					try {
						JSONObject item = response.getJSONObject(i);
						CollectionItem browseItem = CollectionItem.parseSong(item);
						items.add(browseItem);
					} catch (Exception e) {
					}
				}
				success.onResponse(items);
			}
		};

		this.auth.doJsonArrayRequest(requestURL, successWrapper, failure);
	}
}
