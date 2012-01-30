package org.gathani.ditesh.twitterreader;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class AuthActivity extends Activity {

	private Handler handler = new Handler();
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth);

		new Thread(new Runnable() {

			public void run() {

				Account account;
				AccountManager am = AccountManager.get(AuthActivity.this);
				Account[] accounts = am.getAccountsByType("com.twitter.android.auth.login");
				
		        if (accounts.length > 0) account = accounts[0];
		        else account = null;

				am.getAuthToken(account,
						"com.twitter.android.oauth.token",
						null,
						AuthActivity.this,
						new OnTokenAcquired(),
						handler);

				progressDialog.dismiss();
				Intent i = new Intent(AuthActivity.this, TwitterReaderActivity.class);
				startActivity(i);

			}

		}).start();
	}
	
	private class OnError {

	}
	
	private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
	    @Override
	    public void run(AccountManagerFuture<Bundle> result) {
	        // Get the result of the operation from the AccountManagerFuture.
	        Bundle bundle;
			try {
				
				bundle = result.getResult();
		        // The token is a named value in the bundle. The name of the value
		        // is stored in the constant AccountManager.KEY_AUTHTOKEN.
		        String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);

		        Log.d("DEBUG", "token is " + token);
		        
			} catch (OperationCanceledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    

	    }
	}	
}