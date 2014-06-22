package com.alvaroga.tecuidamos.alarmas;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Toast;

import com.alvaroga.tecuidamos.AvisoDialog;
import com.alvaroga.tecuidamos.SQLAvisos;

public class AvisoAlarm extends BroadcastReceiver { 

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

		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		@SuppressWarnings("deprecation")
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"");
		wl.acquire();
		Bundle extras = intent.getExtras();
		String nombre = extras.getString("nombre");
		int id = extras.getInt("id");
		SQLAvisos db = new SQLAvisos(context);
		db.open();
		db.reducirStock(nombre);
		db.setNextAlarma(nombre,id);
		if (db.getStock(nombre)<=10) Toast.makeText(context, "Quedan "+ db.getStock(nombre)+" dosis", Toast.LENGTH_LONG).show(); 
		db.close();
		
		
		

		Intent i = new Intent(context,AvisoDialog.class);
		i.putExtras(extras);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		context.startActivity(i);
		
		//Toast.makeText(context, nombre + " alarma", Toast.LENGTH_LONG).show();

	}

	public void setAlarm(Context context, int tiempo, Bundle extras) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(context, AvisoAlarm.class);
		i.putExtras(extras);
		
		int id = extras.getInt("id");

		PendingIntent pi = PendingIntent.getBroadcast(context, id, i, 0);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
					+ tiempo * 1000 * 60, pi);
		} else {
			am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + tiempo
					* 1000 * 60, pi);
		}
	}

	public void cancelAlarm(Context context, Bundle extras) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		
		Intent i = new Intent(context, AvisoAlarm.class);
		if (extras != null)
			i.putExtras(extras);
		int id = extras.getInt("id");

		PendingIntent pi = PendingIntent.getBroadcast(context, id, i, 0);

		am.cancel(pi);
		
	}
}