package org.gathani.ditesh.twitterreader;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
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
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());

	}

	public void add(ArrayList<HashMap<String, String>> d) {

		for (int i = 0; i < d.size(); i++)
			data.add(d.get(i));

	}

	public int getCount() {
		return data.size();
	}

	public ArrayList<HashMap<String, String>> getData() {
		return data;
	}
	
	public Object getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View vi = convertView;

		if (convertView == null)
			vi = inflater.inflate(R.layout.tweet, null);

		TextView text = (TextView) vi.findViewById(R.id.tweet);
		ImageView image = (ImageView) vi.findViewById(R.id.image);
		
		// I don't believe I'm using the <font> tag. Urgh.
		String content = "<strong><font size='12'>"
				+ data.get(position).get("name")
				+ "</font></strong><br>" + data.get(position).get("tweet");

		if (data.get(position).get("original_screen_name") != null)
			content += "<br><font size='10' color='grey'>Retweeted by "+data.get(position).get("original_screen_name")+"</font>";

		text.setText(Html.fromHtml(content));
		imageLoader.DisplayImage(data.get(position).get("picurl"), image);
		return vi;

	}

}