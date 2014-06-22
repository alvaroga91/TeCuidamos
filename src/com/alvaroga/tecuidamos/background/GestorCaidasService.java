package com.alvaroga.tecuidamos.background;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.alvaroga.tecuidamos.CaidaDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

public class GestorCaidasService extends Service implements
		SensorEventListener, ConnectionCallbacks, OnConnectionFailedListener {

	static final int ESTADO_CAIDA = 1;
	static final int ESTADO_SUELO = 2;

	private static final int UMBRAL_CAIDA = 3;
	private static final int UMBRAL_SUELO = 20;

	private static int VECES_CAIDA;

	static LocationClient locationClient;

	public static SensorManager sensorManager;
	public static SensorEventListener listener;
	static int estado = 0;
	int veces = 0;
	static Context context;

	static JSON json;

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			float[] values = event.values;

			float x = values[0];
			float y = values[1];
			float z = values[2];

			if (veces > VECES_CAIDA)
				estado = ESTADO_CAIDA;

			if (Math.abs(x + y + z) <= UMBRAL_CAIDA) {
				veces++;
			} else
				veces = 0;

			if ((estado == ESTADO_CAIDA) && Math.abs(x + y + z) >= UMBRAL_SUELO) {
				estado = ESTADO_SUELO;
				locationClient.connect();

			}
		}
	}

	private void lanzarAyuda() {

		Location location = locationClient.getLastLocation();
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
				+ lat + "," + lon + "&sensor=true_or_false";

		json = new JSON(this, url, 2);
		json.execute();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		locationClient = new LocationClient(this, this, this);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(com.alvaroga.tecuidamos.R.drawable.ic_launcher)
				.setContentTitle("TeCuidamos")
				.setContentText("Gestor de caídas activado");
		Intent i = new Intent(this, com.alvaroga.tecuidamos.Main.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i,
				PendingIntent.FLAG_ONE_SHOT);

		mBuilder.setContentIntent(pi);

		Notification note = mBuilder.build();

		note.flags |= Notification.FLAG_NO_CLEAR;

		startForeground(1337, note);

		context = this;
		System.out.println("Servicio caidas on");

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor mAccelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);
		listener = this;

		SharedPreferences settings = getSharedPreferences("Prefs",
				Context.MODE_MULTI_PROCESS);
		VECES_CAIDA = 80 - settings.getInt("sensibilidadCaida", 30);

		return Service.START_STICKY;

	}

	static Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// Cuando se ha obtenido respuesta del JSON...
			case 0:
				if (estado == ESTADO_SUELO) {
					String jsonResponse = json.getResponse();
					String direccion = parseJsonDireccion(jsonResponse);
					startActivity(context, direccion);
					System.out.println("dire " + direccion);
					estado = 0;
					endService(context);
					sensorManager.unregisterListener(listener);
					locationClient.disconnect();
				}
				break;

			default:
				break;
			}
		}

		private String parseJsonDireccion(String jsonResponse) {

			try {
				JSONObject jObj = new JSONObject(jsonResponse);

				JSONArray results = jObj.getJSONArray("results");

				JSONObject first = results.getJSONObject(0);

				return (first.getString("formatted_address"));

			} catch (Exception e) {

			}
			return "Localización no disponible";
		}
	};

	private static void endService(Context c) {
		Intent i = new Intent(c, GestorCaidasService.class);
		c.stopService(i);
	}

	private static void startActivity(Context c, String direccion) {
		Intent i = new Intent(c, CaidaDialog.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

		i.putExtra("direccion", direccion);
		c.startActivity(i);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {
		lanzarAyuda();

	}

	@Override
	public void onDisconnected() {

	}

}
