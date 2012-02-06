package org.gathani.ditesh.twitterreader;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthActivity extends Activity {

	private Singleton appState;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth);

		appState = ((Singleton) getApplication());

		WebView webView = (WebView) findViewById(R.id.twitter_login);
		webView.setVisibility(View.VISIBLE);

		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {

			// This seems to get fired twice, so we control that through
			// seen bool
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if (url != null && url.startsWith(Constants.CALLBACK_URL)) {

					// String oauthToken = "";
					String oauthVerifier = "";

					String[] urlParameters = url.split("\\?")[1].split("&");

					if (urlParameters[0].startsWith("oauth_verifier")) {
						oauthVerifier = urlParameters[0].split("=")[1];
					} else if (urlParameters.length > 1
							&& urlParameters[1].startsWith("oauth_verifier")) {
						oauthVerifier = urlParameters[1].split("=")[1];
					}

					appState.oauthVerifier = oauthVerifier;
					new TwitterConnect().execute();
					return true;

				} else
					return false;
			}
		});

		webView.loadUrl(appState.requestToken.getAuthorizationURL());

	}

	private class TwitterConnect extends AsyncTask<Void, Void, Void> {

		private Boolean authSuccess = true;
		private AccessToken accessToken = null;

		@Override
		protected Void doInBackground(Void... v) {

			try {
				accessToken = appState.mTwitter.getOAuthAccessToken(
						appState.requestToken, appState.oauthVerifier);

				SharedPreferences pref = getSharedPreferences(
						Constants.PREFERENCE_NAME, MODE_PRIVATE);

				SharedPreferences.Editor editor = pref.edit();
				editor.putString("oauth_token", accessToken.getToken());
				editor.putString("oauth_token_secret",
						accessToken.getTokenSecret());
				editor.commit();

			} catch (TwitterException e) {

				// We don't have access to the Twitter account, auth failed
				authSuccess = false;

			}

			return null;

		}

		@Override
		protected void onProgressUpdate(Void... v) {
			// TODO show progress
		}

		@Override
		protected void onPostExecute(Void v) {

			if (authSuccess == true) {

				// Move on to verification
				Intent i = new Intent(AuthActivity.this,
						MainActivity.class);
				AuthActivity.this.startActivity(i);

			} else {

				Intent i = new Intent(AuthActivity.this, LoginActivity.class);
				AuthActivity.this.startActivity(i);

			}
		}
	}
}