package com.amoryosef613.carfinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkDetector {
	private Context _context;

	public NetworkDetector(Context context) {
		this._context = context;
	}

	/**
	 * Checking for all possible internet providers
	 **/
	public boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) _context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}

	// Function to display simple Alert Dialog
	@SuppressWarnings("deprecation")
	public void showAlertDialog(final Context context, String title, String message, Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();

		// Set Dialog Title
		alertDialog.setTitle(title);
		alertDialog.setCancelable(false);
		// Set Dialog Message
		alertDialog.setMessage(message);

		if (status != null)
			// Set alert dialog icon
			alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

		// Set OK Button

		alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				((Activity)(context)).finish();
			}
		});

		// Show Alert Message
		alertDialog.show();
	}
}
