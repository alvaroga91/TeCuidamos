package com.alvaroga.tecuidamos;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.alvaroga.tecuidamos.alarmas.CaidaAlarm;
import com.alvaroga.tecuidamos.background.GestorCaidasService;

public class CaidaDialog extends Activity implements OnClickListener {

	CaidaAlarm alarm;
	PowerManager pm;
	KeyguardManager km;
	WakeLock wl;
	KeyguardLock kl;
	Ringtone r;
	Context c;

	// Dialogo que aparece cuando se detecta una caida.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		kl = km.newKeyguardLock("INFO");
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE, "INFO");
		wl.acquire(); // wake up the screen
		kl.disableKeyguard();

		setContentView(R.layout.caidadialog);
		((Button) findViewById(R.id.bCancelarAlarma)).setOnClickListener(this);
		alarm = new CaidaAlarm();
		String direccion = getIntent().getStringExtra("direccion");
		alarm.setAlarm(this, direccion);

		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

		if (alert == null) {
			alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (alert == null) {
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			}
		}
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)/2, 0);
		
		r = RingtoneManager.getRingtone(this, alert);
		r.play();
		c = this;
	}

	@Override
	public void onClick(View v) {
		

		Intent i = new Intent(c, GestorCaidasService.class);
		startService(i);

		alarm.cancelAlarm(this);
		wl.release();
		kl.reenableKeyguard();
		r.stop();
		finish();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Intent i = new Intent(c, GestorCaidasService.class);
		startService(i);

		alarm.cancelAlarm(this);
		if (wl.isHeld())wl.release();
		kl.reenableKeyguard();
		if (r.isPlaying())r.stop();
		finish();
		super.onStop();
		
	}
}
