package com.alvaroga.tecuidamos.alarmas;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;

import com.alvaroga.tecuidamos.Mail;

public class CaidaAlarm extends BroadcastReceiver {

	private static final String PREFS = "Prefs";
	static Bundle bundle;

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

		System.out.println("In");
		String direccion = intent.getStringExtra("direccion");
		avisarFamilia(context, direccion);

	}

	private void avisarFamilia(Context context, String direccion) {
		SharedPreferences settings = context.getSharedPreferences("Prefs",
				Context.MODE_MULTI_PROCESS);
		int envio = settings.getInt("metodoEnvio", 0);

		switch (envio) {

		case 0:
			enviarMail(context, direccion, settings);
			break;
		case 1:
			enviarSms(context, direccion, settings);
			break;
		case 2:
			enviarMail(context, direccion, settings);
			enviarSms(context, direccion, settings);

		}

	}

	private void enviarSms(Context context, String direccion,
			SharedPreferences settings) {
		String numTelefono = settings.getInt("telefonoContacto", 0) + "";

		String s = "TeCuidamos: Se ha detectado que su familiar se ha caído en "
				+ direccion + ".";

		SmsManager sms = SmsManager.getDefault();
		System.out.println("telefono "+numTelefono);
		if (numTelefono.length()>0) sms.sendTextMessage(numTelefono, null, s, null, null);
		System.out.println("SMS ENVIADO");

	}

	private void enviarMail(Context context, String direccion,
			SharedPreferences settings) {
		String user = settings.getString("mail", "");
		String pass = settings.getString("pass", "");
		Mail mail = new Mail(user, pass);
		mail.sendMail(user, "Te Cuidamos: Detección de caída",
				"Se ha detectado que su familiar se ha caído en  "
						+ direccion+".");

	}

	@SuppressLint("NewApi")
	public void setAlarm(Context context, String direccion) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(context, CaidaAlarm.class);
		i.putExtra("direccion", direccion);

		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

		SharedPreferences settings = context.getSharedPreferences(PREFS,
				Context.MODE_MULTI_PROCESS);

		int tiempo = settings.getInt("tiempoAviso", 60);
		

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
					+ tiempo * 1000, pi);
		} else {
			am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + tiempo
					* 1000, pi);
		}
	}

	public void cancelAlarm(Context context) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(context, CaidaAlarm.class);
		if (bundle != null)
			i.putExtras(bundle);

		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

		am.cancel(pi);

	}
}