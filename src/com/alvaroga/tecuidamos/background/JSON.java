package com.alvaroga.tecuidamos.background;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.alvaroga.tecuidamos.Main;
import com.alvaroga.tecuidamos.alarmas.InfoAlarm;
import com.alvaroga.tecuidamos.alarmas.ZonaAlarm;

public class JSON extends AsyncTask<Void, Void, Boolean> {

	private static final long TIMEOUT = 2000;
	Context context;
	private String response = null;
	private String url;
	Toast toast;
	int metodo;

	// Para los Handlers: 0 para Main, 1 para InfoAlarm y 2 para
	// GestorCaidasService, 3 para Zona

	public JSON(Context context, String url, int metodo) {
		this.context = context;
		this.url = url;
		this.metodo = metodo;

	}

	private String getJson(String url) {
		System.out.println("url "+url);

		DefaultHttpClient httpclient = new DefaultHttpClient(
				new BasicHttpParams());
		HttpPost httppost = new HttpPost(url);
		httppost.setHeader("Content-type", "application/json");

		InputStream inputStream = null;
		try {
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			inputStream = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			return (sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (Exception e) {
			}
		}
		return "";
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();

	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		try {
			if (get())
				setResponse(response);

			switch (metodo) {
			case 0:
				Main.myHandler.sendEmptyMessage(0);
				break;
			case 1:
				InfoAlarm.myHandler.sendEmptyMessage(0);
				break;
			case 2:
				GestorCaidasService.myHandler.sendEmptyMessage(0);
				break;
			case 3:
				ZonaAlarm.myHandler.sendEmptyMessage(0);
				break;
			}

		} catch (Exception e) {
		}

	}

	@Override
	protected Boolean doInBackground(Void... params) {
		long in = System.currentTimeMillis();
		do {
			response = getJson(url);
			if (System.currentTimeMillis() - in >= TIMEOUT)
				return false;
		} while (response == null);

		return true;

	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

}
