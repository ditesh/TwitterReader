package org.gathani.ditesh.twitterreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;

	public TweetAdapter(Activity a) {

		activity = a;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());

	}

	public void add(ArrayList<HashMap<String, String>> d) {
		
		for (int i = 0; i < d.size(); i++) 
			data.add(d.get(i));
		
	}
	
	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View vi = convertView;
		
		if (convertView == null) vi = inflater.inflate(R.layout.tweet, null);

		TextView text = (TextView) vi.findViewById(R.id.tweet);
		ImageView image = (ImageView) vi.findViewById(R.id.image);
		
		text.setText(data.get(position).get("tweet"));
		imageLoader.DisplayImage(data.get(position).get("picurl"), image);
		return vi;

	}

}