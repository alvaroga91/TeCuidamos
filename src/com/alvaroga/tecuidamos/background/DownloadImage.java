package com.alvaroga.tecuidamos.background;

import java.io.InputStream;
import java.net.URL;

import com.alvaroga.tecuidamos.Main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

public class DownloadImage extends AsyncTask<Void, Void, Boolean> {

	private static final long TIMEOUT = 5000;

	Context context;
	private String url;
	private Drawable d = null;

	public DownloadImage(Context context, String url) {
		this.context = context;
		this.url = url;

	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		try {
			if (get()){
				setD(d);
				Main.myHandler.sendEmptyMessage(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected Boolean doInBackground(Void... params) {
		long in = System.currentTimeMillis();

		do {
			try {
				System.out.println("Url "+url);
				InputStream is = (InputStream) new URL(url).getContent();
				d = Drawable.createFromStream(is, "src name");
				if (d != null && is !=null)
					return true;
				if (System.currentTimeMillis() - in >= TIMEOUT)
					return false;

			} catch (Exception e) {
				e.printStackTrace();
				if (System.currentTimeMillis() - in >= TIMEOUT)
					return false;

			}
		} while (d == null);
		return true;
	}

	public Drawable getD() {
		return d;
	}

	public void setD(Drawable d) {
		this.d = d;
	}

}
