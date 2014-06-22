package com.alvaroga.tecuidamos;

import java.math.BigDecimal;
import java.math.MathContext;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alvaroga.tecuidamos.alarmas.InfoAlarm;
import com.alvaroga.tecuidamos.alarmas.ZonaAlarm;
import com.alvaroga.tecuidamos.background.DownloadImage;
import com.alvaroga.tecuidamos.background.GestorCaidasService;
import com.alvaroga.tecuidamos.background.JSON;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

public class Main extends Activity implements OnClickListener,
		ConnectionCallbacks, OnConnectionFailedListener {

	public static final String PREFS = "Prefs";

	public static LocationClient locationClient;
	static Context context;
	static JSON json;
	static DownloadImage di;
	static String jsonResponse = null;
	static Drawable jsonIcon = null;
	static String iconUrl = null;

	static Toast toast;
	MediaPlayer mp;
	static SharedPreferences settings;
	static SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((Button) findViewById(R.id.bAvisos)).setOnClickListener(this);
		((Button) findViewById(R.id.bSalimos)).setOnClickListener(this);
		((Button) findViewById(R.id.bConfig)).setOnClickListener(this);
		((Button) findViewById(R.id.bDetener)).setOnClickListener(this);

		settings = getSharedPreferences(PREFS, Context.MODE_MULTI_PROCESS);
		editor = settings.edit();

		locationClient = new LocationClient(this, this, this);
		context = this;

		if (settings.getBoolean("firstTime", true))
			showFirstDialog();

		if (settings.getBoolean("zonaOn", true)) {
			if (InfoAlarm.locationStart == null
					&& ZonaAlarm.locationStart == null) {
				ZonaAlarm alarm = new ZonaAlarm();
				alarm.setAlarm(context);
			}
		}
		if (settings.getBoolean("caidasCasaOn", true)) {
			Intent i = new Intent(context, GestorCaidasService.class);
			context.startService(i);
					
		}

	}

	private void showFirstDialog() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Nota");
		alert.setMessage("Te recomendamos que configures la aplicación a tus necesidades desde el menú de Configuración para activar (o desactivar) los servicios que quieras.");
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		alert.show();

	}

	@Override
	protected void onStart() {
		super.onStart();
		locationClient.connect();
	}

	@Override
	protected void onStop() {
		locationClient.disconnect();
		super.onStop();

	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.bAvisos) {
			Intent i = new Intent(this, Avisos.class);
			startActivity(i);
		} else if (v.getId() == R.id.bSalimos) {
			toast = Toast.makeText(this,
					"Obteniendo información del tiempo...", Toast.LENGTH_LONG);
			toast.show();
			getResponse();
		}  else if (v.getId() == R.id.bConfig) {
			Intent i = new Intent(this, Config.class);
			startActivity(i);
		} else if (v.getId() == R.id.bDetener) {
			showDetenerServiciosDialog();
		}
	}

	private void showDetenerServiciosDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		final View view = LayoutInflater.from(this).inflate(
				R.layout.cancelarservicios, null);
		alert.setView(view);

		Button bCancelarCaidas = (Button) view
				.findViewById(R.id.bCancelarCaidas);
		Button bCancelarZona = (Button) view.findViewById(R.id.bCancelarZona);
		Button bCancelarLoc = (Button) view.findViewById(R.id.bCancelarLoc);

		alert.setPositiveButton("Cerrar",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}

				});

		class Listener implements OnClickListener {

			@Override
			public void onClick(View v) {
				Toast t = null;
				if (v.getId() == R.id.bCancelarCaidas) {
					Intent i = new Intent(context, GestorCaidasService.class);
					getApplicationContext();
					if (!stopService(i))
						t = Toast.makeText(context,
								"Error al detener el servicio de caídas. ¿Está ya desactivado?",
								Toast.LENGTH_SHORT);
					else
						t = Toast.makeText(context,
								"Servicio de detección de caídas detenido",
								Toast.LENGTH_SHORT);
				} else if (v.getId() == R.id.bCancelarZona) {
					ZonaAlarm alarm = new ZonaAlarm();
					alarm.cancelAlarm(context);
					t = Toast.makeText(context,
							"Servicio de 'Zona Segura' detenido",
							Toast.LENGTH_SHORT);

				} else if (v.getId() == R.id.bCancelarLoc) {
					InfoAlarm alarm = new InfoAlarm();
					alarm.cancelAlarm(context);
					t = Toast.makeText(context,
							"Servicio de localización detenido",
							Toast.LENGTH_SHORT);

				}
				t.show();
			}

		}

		Listener l = new Listener();

		bCancelarCaidas.setOnClickListener(l);
		bCancelarZona.setOnClickListener(l);
		bCancelarLoc.setOnClickListener(l);

		alert.show();
	}

	private void getResponse() {
		Location location = locationClient.getLastLocation();

		BigDecimal lat = new BigDecimal(location.getLatitude(),
				MathContext.DECIMAL64);
		BigDecimal lon = new BigDecimal(location.getLongitude(),
				MathContext.DECIMAL64);

		json = new JSON(this,
				"http://api.openweathermap.org/data/2.5/weather?lat=" + lat
						+ "&lon=" + lon, 0);

		json.execute();

	}

	private static void showDialogSalir() {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		View view = LayoutInflater.from(context).inflate(R.layout.dialogtiempo,
				null);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, int which) {

				if (settings.getBoolean("caidasOn", true)) {
					Intent i = new Intent(context, GestorCaidasService.class);
					context.startService(i);
				}
				if (settings.getBoolean("localizacionOn", true)) {
					InfoAlarm alarm = new InfoAlarm();
					alarm.setAlarm(context);
				}
				ZonaAlarm alarm = new ZonaAlarm();
				alarm.cancelAlarm(context);

			}
		});

		if (jsonResponse != null && iconUrl != null) {
			try {

				JSONObject jObj = new JSONObject(jsonResponse);

				// String nombre = jObj.getString("name");
				alert.setTitle("Posición actual");

				JSONObject main = jObj.getJSONObject("main");

				int temp = main.getInt("temp") - 273;
				int humedad = (int) (main.getDouble("humidity"));

				JSONObject wind = jObj.getJSONObject("wind");
				String viento = wind.getDouble("speed") + " km/h";
				int vientoInt = (int) wind.getDouble("speed");

				JSONArray weather = jObj.getJSONArray("weather");
				JSONObject weatherObj = weather.getJSONObject(0);

				iconUrl = weatherObj.getString("icon");

				ImageView iv = (ImageView) view.findViewById(R.id.ivTiempo);
				iv.setImageDrawable(jsonIcon);
				TextView tvTiempo = (TextView) view
						.findViewById(R.id.tvTiempoTemp);
				tvTiempo.setText("Temp: " + temp + "°\nLluvia: " + humedad
						+ "%\nViento: " + viento);

				alert.setView(view);

				Dialog dialog = alert.create();
				dialog.setCanceledOnTouchOutside(false);
				if (iconUrl != null && jsonResponse != null)
					dialog.show();
				else {
					Toast.makeText(context,
							"Error obteniendo la información del tiempo",
							Toast.LENGTH_LONG).show();
				}

				if (settings.getBoolean("caidasOn", true)) {
					Intent i = new Intent(context, GestorCaidasService.class);
					Bundle b = new Bundle();
					
					context.startService(i);
				}
				if (settings.getBoolean("localizacionOn", true)) {
					InfoAlarm alarm = new InfoAlarm();
					alarm.setAlarm(context);
				}

				iconUrl = null;
				jsonResponse = null;

				if (toast != null)
					toast.cancel();

				// setMailLocation();
				String s = "Hace ";
				if (temp >= (settings.getInt("muchoCalor", 30)))
					s += "mucho calor, ";
				else if (temp >= (settings.getInt("calor", 24)))
					s += "algo de calor, ";
				else if (temp <= (settings.getInt("muchoCalor", 16)))
					s += "algo de frío, ";
				else if (temp <= (settings.getInt("muchoCalor", 7)))
					s += "mucho frío, ";
				else
					s += "buena temperatura, ";

				if (humedad >= (settings.getInt("lluvia", 70)))
					s += "seguramente llueva ";
				else
					s += "no es probable que llueva ";

				if (vientoInt >= 120)
					s += "y hay vientos huracanados.";
				else if (vientoInt >= 71)
					s += "y hay vientos muy fuertes.";
				else if (vientoInt >= 41)
					s += "y hay vientos fuertes.";
				else if (vientoInt >= 21)
					s += "y hay vientos moderados.";
				else
					s += "y hay poco viento.";
				TextView tvInfoTiempo = (TextView) view
						.findViewById(R.id.tvInfoTiempo);
				tvInfoTiempo.setText(s);

			} catch (Exception e) {
				Toast.makeText(context, "Error al obtener la fecha",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		} else {
			Toast.makeText(context, "Error al obtener la fecha",
					Toast.LENGTH_SHORT).show();
		}
	}

	public static void getImageFromURL(String json) {

		JSONObject jObj;
		try {
			jObj = new JSONObject(json);
			JSONArray weather = jObj.getJSONArray("weather");
			JSONObject weatherObj = weather.getJSONObject(0);
			iconUrl = "http://openweathermap.org/img/w/"
					+ weatherObj.getString("icon") + ".png";
		} catch (Exception e) {
			e.printStackTrace();
		}

		di = new DownloadImage(context, iconUrl);
		di.execute();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	// Esto se lanzara cuando se haya obtenido la respuesta y la imagen
	public static Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// Cuando se ha obtenido respuesta del JSON...
			case 0:
				jsonResponse = json.getResponse();
				getImageFromURL(jsonResponse);
				break;

			// Cuando se ha descargado la imagen...
			case 1:
				jsonIcon = di.getD();
				showDialogSalir();

				break;
			default:
				break;
			}
		}
	};

	public void getStandardAlarms() {
		RingtoneManager manager = new RingtoneManager(this);
		manager.setType(RingtoneManager.TYPE_ALARM
				| RingtoneManager.TYPE_RINGTONE);

		Cursor cursor = manager.getCursor();

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
			System.out.println("Title " + title);
		}

	}

}
