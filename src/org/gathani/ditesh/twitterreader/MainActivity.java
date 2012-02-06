package org.gathani.ditesh.twitterreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.Paging;
import twitter4j.TwitterException;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	private int page = 1;
	private TweetAdapter ta;
	private Singleton appState;
	private boolean lock = false;

	private ProgressDialog progressDialog;
	private static final int NEW_TWEET_ID = Menu.FIRST;
	private static final int SETTINGS_ID = Menu.FIRST + 1;
	private static final int REPLY_ID = Menu.FIRST;
	private static final int RETWEET_ID = Menu.FIRST + 1;
	private static final int RT_ID = Menu.FIRST + 2;
	private static final int FAVOURITE_ID = Menu.FIRST + 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Log.d("debug", "on create");

		appState = (Singleton) getApplication();
		ta = new TweetAdapter(this);
		setListAdapter(ta);

		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Getting tweets ...");
		progressDialog.setCancelable(false);

		if (appState.data == null || appState.data.size() == 0) {

			lock = true;
			progressDialog.show();
			new TwitterConnect().execute();

		}

		ListView lv = getListView();
		lv.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				if (lock == true)
					return;

				int lastInScreen = firstVisibleItem + visibleItemCount;

				// When we're at the bottom of the screen
				if (lastInScreen == totalItemCount) {
					new TwitterConnect().execute();
				}
			}
		});

		registerForContextMenu(lv);

	}

	// Lifecycle methods
	@Override
	protected void onPause() {

		super.onPause();
		Log.d("debug", "on pause");

	}

	// TODO on resume, needs to go back to the same point in the list
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("debug", "on resume");
	}

	// Lifecycle methods
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, NEW_TWEET_ID, 0, R.string.menu_new_tweet);
		menu.add(0, SETTINGS_ID, 1, R.string.menu_settings);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent i = new Intent(this, NewTweetActivity.class);

		switch (item.getItemId()) {
		case NEW_TWEET_ID:

			startActivity(i);
			return true;

		case SETTINGS_ID:
			// Intent i = new Intent(this, NoteEdit.class);
			// startActivityForResult(i, ACTIVITY_CREATE);
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, REPLY_ID, 0, R.string.menu_reply);
		menu.add(0, RETWEET_ID, 1, R.string.menu_retweet);
		menu.add(0, RT_ID, 2, R.string.menu_rt);
		menu.add(0, FAVOURITE_ID, 3, R.string.menu_favourite);
	}

	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		HashMap<String, String> data = (HashMap<String, String>) ta
				.getItem((int) info.id);

		Intent i = new Intent(this, NewTweetActivity.class);
		i.putExtra("id", Long.parseLong(data.get("id")));

		switch (item.getItemId()) {
		case REPLY_ID:
			i.putExtra("tweet", "@" + data.get("screen_name") + " ");
			startActivity(i);
			return true;

		case RETWEET_ID:

			progressDialog = new ProgressDialog(this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Retweeting ...");
			progressDialog.setCancelable(false);
			progressDialog.show();

			new TwitterRT().execute(data.get("id"));
			return true;

		case RT_ID:
			i.putExtra("tweet",
					"RT @" + data.get("screen_name") + " " + data.get("tweet"));
			startActivity(i);

			return true;

		}

		return super.onContextItemSelected(item);

	}

	private class TwitterConnect extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... v) {

			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

			try {

				list.clear();
				HashMap<String, String> map;
				Paging paging = new Paging(page);

				List<twitter4j.Status> statuses = appState.mTwitter
						.getFriendsTimeline(paging);

				for (twitter4j.Status status : statuses) {

					map = new HashMap<String, String>();

					twitter4j.Status retweetedStatus = status
							.getRetweetedStatus();

					if (retweetedStatus != null) {
						map.put("original_screen_name", status.getUser()
								.getName());
						status = retweetedStatus;
					}

					map.put("id", "" + status.getId());

					map.put("picurl", status.getUser().getProfileImageURL()
							.toString());
					map.put("tweet", status.getText());
					map.put("name", status.getUser().getName());
					map.put("screen_name", status.getUser().getScreenName());
					list.add(map);

				}

				page += 1;
				ta.add(list);

			} catch (TwitterException e) {

				// Something went wrong
				// This also handles the scenario where appState was destroyed
				e.printStackTrace();
				Intent i = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(i);

			}

			return null;

		}

		@Override
		protected void onPostExecute(Void v) {

			lock = false;
			ta.notifyDataSetChanged();
			progressDialog.dismiss();
			appState.data = ta.getData();

		}
	};

	private class TwitterRT extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... s) {

			try {

				appState.mTwitter.retweetStatus(Long.parseLong(s[0]));

			} catch (TwitterException e) {

				Toast toast = Toast.makeText(getApplicationContext(),
						"Cannot retweet", Toast.LENGTH_SHORT);
				toast.show();

			}

			return null;

		}

		@Override
		protected void onPostExecute(Void v) {

			ta.notifyDataSetChanged();
			progressDialog.dismiss();

		}
	};
};