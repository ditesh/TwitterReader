package org.gathani.ditesh.twitterreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ImageLoader {

	FileCache fileCache;
	ExecutorService executorService;
	final int stub_id = R.drawable.stub;
	MemoryCache memoryCache = new MemoryCache();
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	public void DisplayImage(String url, ImageView imageView) {
		
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		
		if (bitmap != null) imageView.setImageBitmap(bitmap);
		else {
			queuePhoto(url, imageView);
			imageView.setImageResource(stub_id);
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		
		File f = fileCache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f);
		
		if (b != null) return b;

		// from web
		try {
			
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f);
			return bitmap;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		
		try {
			
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight, scale = 1;
			
			while (true) {
				
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) break;
				
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
				
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			fis = new FileInputStream(f);
			Bitmap retval = BitmapFactory.decodeStream(fis, null, o2);
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return retval;
			
		} catch (FileNotFoundException e) {
		}
		
		return null;
		
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			
			if (imageViewReused(photoToLoad)) return;
			
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);

			if (imageViewReused(photoToLoad)) return;
			
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);

		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url)) return true;
		return false;
		
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			
			if (imageViewReused(photoToLoad)) return;
			if (bitmap != null) photoToLoad.imageView.setImageBitmap(bitmap);
			else photoToLoad.imageView.setImageResource(stub_id);
			
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

}