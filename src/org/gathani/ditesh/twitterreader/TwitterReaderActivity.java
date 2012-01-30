package org.gathani.ditesh.twitterreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class TwitterReaderActivity extends ListActivity {

	private int page = 1;
	private TweetAdapter ta;
	private ProgressDialog progressDialog;
	private boolean loadingMore = false;

	private Runnable r = new Runnable() {

		private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();;

		private Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				ta.add(list);
				ta.notifyDataSetChanged();
				loadingMore = false;
				progressDialog.dismiss();

			}
		};

		public void run() {

			if (loadingMore) return;

			Log.d("DEBUG", "Loading up API");
			loadingMore = true;
			
			try {

				HashMap<String, String> map;
				list.clear();

				URL twitter = new URL("https://api.twitter.com/1/statuses/user_timeline.json?include_entities=true&include_rts=true&screen_name=ditesh&count=20&page=" + page);
				URLConnection tc = twitter.openConnection();
				InputStreamReader isr = new InputStreamReader(tc.getInputStream());
				
				BufferedReader in = new BufferedReader(isr);
				String line;

				while ((line = in.readLine()) != null) {

					JSONArray ja = new JSONArray(line);

					for (int i = 0; i < ja.length(); i++) {

						JSONObject jo = (JSONObject) ja.get(i);
						map = new HashMap<String, String>();

						map.put("picurl", jo.getJSONObject("user").getString("profile_image_url_https"));
						map.put("tweet", jo.getString("text"));
						list.add(map);

					}
				}

				Log.d("DEBUG", "Got some results: " + list.size());
				page += 1;
				isr.close();
				handler.sendEmptyMessage(0);

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	private Thread t;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		StrictMode.enableDefaults();
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tweet_list);

		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Getting tweets ...");
		progressDialog.setCancelable(false);
		progressDialog.show();
		
		t = new Thread(r);
		t.start();
		
		ta = new TweetAdapter(this);
		setListAdapter(ta);

		this.getListView().setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				int lastInScreen = firstVisibleItem + visibleItemCount;

				Log.d("DEBUG", firstVisibleItem + " " +visibleItemCount + " " + totalItemCount);

				if ((lastInScreen == totalItemCount) && !(loadingMore)) {

					if (t.isAlive()) t.stop();

					t = new Thread(r);
					t.start();

				}
			}
		});
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}