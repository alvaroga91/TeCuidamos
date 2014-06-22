package com.alvaroga.tecuidamos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.alvaroga.tecuidamos.alarmas.ZonaAlarm;
import com.alvaroga.tecuidamos.background.GestorCaidasService;

public class Autostart extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		SQLAvisos db = new SQLAvisos(context);
		db.open();
		db.setAllNextAlarmas();
		db.close();

		SharedPreferences settings = context.getSharedPreferences("Prefs",
				Context.MODE_MULTI_PROCESS);
		if (settings.getBoolean("zonaOn", true)) {

			ZonaAlarm alarm = new ZonaAlarm();
			alarm.setAlarm(context);

		}
		if (settings.getBoolean("caidasCasaOn", true)) {
			Intent i = new Intent(context, GestorCaidasService.class);
			context.startService(i);
		}
	}

}
