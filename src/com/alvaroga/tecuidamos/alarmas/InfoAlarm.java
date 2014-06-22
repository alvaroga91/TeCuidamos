package com.alvaroga.tecuidamos.alarmas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.SmsManager;

import com.alvaroga.tecuidamos.Mail;
import com.alvaroga.tecuidamos.background.JSON;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

public class InfoAlarm extends BroadcastReceiver implements
		ConnectionCallbacks, OnConnectionFailedListener {

	static LocationClient locationClient;
	public static Location locationStart;

	static Context context;
	static JSON json;
	SharedPreferences settings;
	Thread th;
	private boolean start;
	PowerManager pm;
	static PowerManager.WakeLock wl;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@SuppressLint("Wakelock")
	@Override
	// Esto es lo que se lanzará cuando se cumpla el tiempo de la alarma.
	public void onReceive(Context context, Intent intent) {

		pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wl.acquire();

		InfoAlarm.context = context;

		settings = context.getSharedPreferences("Prefs",
				Context.MODE_MULTI_PROCESS);
		locationClient = new LocationClient(context, this, this);

		locationClient.connect();
		System.out.println("InfoAlarm");
		ZonaAlarm alarm = new ZonaAlarm();
		alarm.cancelAlarm(context);

	}

	public void setAlarm(Context context) {
		System.out.println("InfoAlarm ON");

		start = true;
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wl.acquire();

		Intent i = new Intent(context, InfoAlarm.class);
		locationClient = new LocationClient(context, this, this);

		locationClient.connect();

		SharedPreferences settings = context.getSharedPreferences("",
				Context.MODE_MULTI_PROCESS);

		int tiempo = settings.getInt("posicionActualiza", 15);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ tiempo * 1000 * 60, tiempo * 1000 * 60, pi);
		wl.release();
	}

	public void cancelAlarm(Context context) {
		pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wl.acquire();
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, InfoAlarm.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

		am.cancel(pi);
		start = true;
		locationClient = null;
		locationStart = null;
	}

	private void checkLocation() {

		Location locationNow = locationClient.getLastLocation();

		BigDecimal lat = new BigDecimal(locationStart.getLatitude(),
				MathContext.DECIMAL64);
		BigDecimal lon = new BigDecimal(locationStart.getLongitude(),
				MathContext.DECIMAL64);

		BigDecimal latNow = new BigDecimal(locationNow.getLatitude(),
				MathContext.DECIMAL64);
		BigDecimal lonNow = new BigDecimal(locationNow.getLongitude(),
				MathContext.DECIMAL64);

		BigDecimal difLat = latNow.subtract(lat).abs();
		BigDecimal difLon = lonNow.subtract(lon).abs();

		System.out.println("diflat " + difLat);
		System.out.println("diflon " + difLon);

		BigDecimal zonaSize = new BigDecimal(
				settings.getInt("tamañoZona", 120), MathContext.DECIMAL64);
		// 1 g = 111.111km
		BigDecimal g = zonaSize.divide(new BigDecimal(111111), 16, RoundingMode.HALF_UP);

		System.out.println("g " + g);

		if ((difLat.compareTo(g) == -1 || difLon.compareTo(g) == -1)) { // Si
																		// hemos
																		// vuelto
																		// a
																		// casa,
																		// quitar
			// alarm y volver a poner la zona.
			try {
				locationClient.disconnect();
			} catch (Exception e) {

			}
			SharedPreferences settings = context.getSharedPreferences("Prefs",
					Context.MODE_MULTI_PROCESS);
			int envio = settings.getInt("modoEnvio", 0);
			String s = "Informamos que su familiar ha regresado a casa.";

			switch (envio) {

			case 0:
				enviarMail(context, s, settings);
				break;
			case 1:
				enviarSms(context, s, settings);
				break;
			case 2:
				enviarMail(context, s, settings);
				enviarSms(context, s, settings);
				break;

			}
			cancelAlarm(context); // Hemos vuelto a casa
			start = true;

			ZonaAlarm alarm = new ZonaAlarm();
			alarm.setAlarm(context);
			wl.release();

		} else { // Seguimos fuera
			locationClient.disconnect();
			String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
					+ latNow + "," + lonNow + "&sensor=true_or_false";
			json = new JSON(context, url, 1);
			json.execute();

		}
	}

	public static Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// Cuando se ha obtenido respuesta del JSON...
			case 0:

				String jsonResponse = json.getResponse();
				String direccion = parseJsonDireccion(jsonResponse);

				SharedPreferences settings = context.getSharedPreferences(
						"Prefs", Context.MODE_MULTI_PROCESS);
				int envio = settings.getInt("modoEnvio", 0);
				String s = "Informamos que su familiar se encuentra en "
						+ direccion + ".";

				switch (envio) {

				case 0:
					enviarMail(context, s, settings);
					break;
				case 1:
					enviarSms(context, s, settings);
					break;
				case 2:
					enviarMail(context, s, settings);
					enviarSms(context, s, settings);
					break;

				}
				wl.release();

			}
		}

		private String parseJsonDireccion(String jsonResponse) {

			try {
				JSONObject jObj = new JSONObject(jsonResponse);
				JSONArray results = jObj.getJSONArray("results");
				JSONObject first = results.getJSONObject(0);
				System.out.println("adrres "
						+ first.getString("formatted_address"));
				return (first.getString("formatted_address"));

			} catch (Exception e) {
				e.printStackTrace();
				return "Localizacion no disponible";

			}
		}
	};

	private static void enviarSms(Context context, String mensaje,
			SharedPreferences settings) {

		String numTelefono = settings.getInt("telefonoContacto", 0) + "";

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(numTelefono, null, mensaje, null, null);
		System.out.println("SMS Enviado");

	}

	private static void enviarMail(Context context, String mensaje,
			SharedPreferences settings) {
		String user = settings.getString("mail", "");
		String pass = settings.getString("pass", "");
		Mail mail = new Mail(user, pass);
		mail.sendMail(user, "Te Cuidamos: Actualización de posición", mensaje);

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		if (locationStart == null && ZonaAlarm.locationStart == null)
			start = true;
		if (start) {
			if (ZonaAlarm.locationStart != null)
				locationStart = ZonaAlarm.locationStart;
			else
				locationStart = locationClient.getLastLocation();

			start = false;
			locationClient.disconnect();
		} else
			checkLocation();
	}

	@Override
	public void onDisconnected() {
	}
}