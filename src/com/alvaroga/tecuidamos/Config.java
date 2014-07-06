package com.alvaroga.tecuidamos;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Config extends ListActivity implements OnClickListener {

	private static final String PREFS = "Prefs";
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	Context context;

	public void onCreate(Bundle lololol) {

		super.onCreate(lololol);
		String[] menu = new String[] { getString(R.string.config1),
				getString(R.string.config2),
				getString(R.string.config3),
				getString(R.string.config4) };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, menu);
		setListAdapter(adapter);

		settings = getSharedPreferences(PREFS, Context.MODE_MULTI_PROCESS);
		editor = settings.edit();
		context = this;

		if (settings.getBoolean("firstTime", true))
			showFirstDialog();

	}

	private void showFirstDialog() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getString(R.string.config5));
		alert.setMessage(getString(R.string.config6));

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				editor.putBoolean("firstTime", false);
				editor.apply();
			}
		});
		alert.show();

	}

	private void showInfoDialog(final int which) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		switch (which) {
		case 0:
			alert.setMessage(getString(R.string.config7));
			break;
		case 1:
			alert.setMessage(getString(R.string.config8));
			break;
		case 2:
			alert.setMessage(getString(R.string.config9));
			break;

		case 3:
			showDialogPorDefecto();
			break;

		}

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

				switch (which) {
				case 0:
					showDialogServicios();
					break;
				case 2:
					showDialogConfigTemp();
					break;
				case 1:
					showDialogConfigEnvios();
					break;
				}
			}
		});
		if (which != 3)
			alert.show();

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		showInfoDialog(position);

	}

	private void showDialogServicios() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		final View view = LayoutInflater.from(this).inflate(
				R.layout.configactivserviciosdialog, null);
		alert.setView(view);

		ToggleButton tbAvisos = (ToggleButton) view.findViewById(R.id.tbAvisos);
		ToggleButton tbZona = (ToggleButton) view.findViewById(R.id.tbZona);
		ToggleButton tbLocalizacion = (ToggleButton) view
				.findViewById(R.id.tbLocalizacion);
		ToggleButton tbCaidas = (ToggleButton) view.findViewById(R.id.tbCaidas);
		ToggleButton tbCaidasCasa = (ToggleButton) view
				.findViewById(R.id.tbCaidasCasa);

		tbAvisos.setOnClickListener(this);
		tbZona.setOnClickListener(this);
		tbLocalizacion.setOnClickListener(this);
		tbCaidas.setOnClickListener(this);
		tbCaidasCasa.setOnClickListener(this);
		
		
		tbAvisos.setChecked(settings.getBoolean("avisosOn", true));
		tbZona.setChecked(settings.getBoolean("zonaOn", true));
		tbLocalizacion.setChecked(settings.getBoolean("localizacionOn", true));
		tbCaidas.setChecked(settings.getBoolean("caidasOn", true));
		tbCaidasCasa.setChecked(settings.getBoolean("caidasCasaOn", true));

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, int which) {

			}
		});

		alert.show();
	}

	private void showDialogPorDefecto() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getString(R.string.config11));
		alert.setMessage(getString(R.string.config10));

		alert.setPositiveButton(getString(R.string.config11),
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, int which) {

						editor.clear();
						editor.apply();
					}
				});
		alert.setNegativeButton(getString(R.string.config12),
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, int which) {

					}
				});

		alert.show();
	}

	private void showDialogConfigEnvios() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		final View view = LayoutInflater.from(this).inflate(
				R.layout.configserviciosdialog, null);
		alert.setView(view);

		Spinner spinner = (Spinner) view.findViewById(R.id.spEnvios);
		String actividades[] = new String[] { "Email", "SMS", "Email+SMS" };

		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
				getBaseContext(), android.R.layout.simple_list_item_1,
				actividades);
		spinner.setAdapter(spinnerAdapter);
		final int[] selected = new int[1];

		spinner.setSelection(settings.getInt("modoEnvio", 0), false);

		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				selected[0] = pos;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				selected[0] = settings.getInt("modoEnvio", 0);
			}

		});

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, int which) {

				try {

					EditText etMail = (EditText) view.findViewById(R.id.etMail);
					EditText etPassword = (EditText) view
							.findViewById(R.id.etPass);
					EditText etTelefono = (EditText) view
							.findViewById(R.id.etTelefonoContacto);
					EditText etZona = (EditText) view.findViewById(R.id.etZona);

					String mailStr = etMail.getText().toString().trim();
					String passStr = etPassword.getText().toString().trim();
					String telefonoStr = etTelefono.getText().toString().trim();
					String zonaStr = etZona.getText().toString().trim();

					if (mailStr.length() > 0)
						editor.putString("mail", mailStr);
					if (passStr.length() > 0)
						editor.putString("pass", passStr);
					if (telefonoStr.length() > 0)
						editor.putInt("telefonoContacto",
								Integer.parseInt(telefonoStr));
					if (zonaStr.length() > 0)
						editor.putInt("zonaActualiza",
								Integer.parseInt(zonaStr));

					EditText etActualiza = (EditText) view
							.findViewById(R.id.etActualiza);
					String actualizaStr = etActualiza.getText().toString();
					if (actualizaStr.length() > 0)
						editor.putInt("posicionActualiza",
								Integer.parseInt(actualizaStr));

					editor.putInt("modoEnvio", selected[0]);

					EditText etTiempo = (EditText) view
							.findViewById(R.id.etTiempoAviso);
					String tiempoStr = etTiempo.getText().toString().trim();
					if (tiempoStr.length() > 0)
						editor.putInt("tiempoAviso",
								Integer.parseInt(tiempoStr));

					EditText etSensibilidad = (EditText) view
							.findViewById(R.id.etSensibilidadCaida);
					String sensStr = etSensibilidad.getText().toString().trim();

					if (sensStr.length() > 0) {
						int sens = Integer.parseInt(sensStr);
						if (sens < 1)
							sens = 1;
						else if (sens > 50)
							sens = 50;

						editor.putInt("sensibilidadCaida", sens);
					}
					EditText etZonaSize = (EditText) view
							.findViewById(R.id.etZonaSize);
					String zonaSizeStr = etZonaSize.getText().toString().trim();

					if (zonaSizeStr.length() > 0) {
						int zona = Integer.parseInt(zonaSizeStr);
						if (zona < 1)
							zona = 1;

						editor.putInt("tamañoZona", zona);
					}

					editor.apply();
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.config13),
							Toast.LENGTH_LONG).show();
				}
			}
		});

		alert.show();
	}

	private void showDialogConfigTemp() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		final View view = LayoutInflater.from(this).inflate(
				R.layout.configtempdialog, null);
		alert.setView(view);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, int which) {

				EditText etMuchoCalor = (EditText) view
						.findViewById(R.id.etMuchoCalor);
				EditText etCalor = (EditText) view.findViewById(R.id.etCalor);
				EditText etFrio = (EditText) view.findViewById(R.id.etFrio);
				EditText etMuchoFrio = (EditText) view
						.findViewById(R.id.etMuchoFrio);
				EditText etLluvia = (EditText) view.findViewById(R.id.etLluvia);

				String muchoCalorStr = etMuchoCalor.getText().toString().trim();
				String calorStr = etCalor.getText().toString().trim();
				String frioStr = etFrio.getText().toString().trim();
				String muchoFrioStr = etMuchoFrio.getText().toString().trim();
				String lluviaStr = etLluvia.getText().toString().trim();

				if (muchoCalorStr.length() > 0)
					editor.putInt("muchoCalor", Integer.parseInt(muchoCalorStr));
				if (calorStr.length() > 0)
					editor.putInt("calor", Integer.parseInt(calorStr));
				if (frioStr.length() > 0)
					editor.putInt("frio", Integer.parseInt(frioStr));
				if (muchoFrioStr.length() > 0)
					editor.putInt("muchoFrio", Integer.parseInt(muchoFrioStr));
				if (lluviaStr.length() > 0)
					editor.putInt("lluvia", Integer.parseInt(lluviaStr));

				editor.apply();
			}
		});

		alert.show();
	}

	@Override
	public void onClick(View v) {
		settings = context.getSharedPreferences("Prefs",
				Context.MODE_MULTI_PROCESS);

		View view = LayoutInflater.from(this).inflate(
				R.layout.configactivserviciosdialog, null);

		boolean avisos = settings.getBoolean("avisosOn", true);
		boolean zona = settings.getBoolean("zonaOn", true);
		boolean loc = settings.getBoolean("localizacionOn", true);
		boolean caidas = settings.getBoolean("caidasOn", true);
		boolean caidasCasa = settings.getBoolean("caidasCasaOn", true);

		ToggleButton tbAvisos = (ToggleButton) view
				.findViewById(R.id.tbAvisos);
		ToggleButton tbZona = (ToggleButton) view
				.findViewById(R.id.tbZona);
		ToggleButton tbLocalizacion = (ToggleButton) view
				.findViewById(R.id.tbLocalizacion);
		ToggleButton tbCaidas = (ToggleButton) view
				.findViewById(R.id.tbCaidas);
		ToggleButton tbCaidasCasa = (ToggleButton) view
				.findViewById(R.id.tbCaidasCasa);
		

		if (v.getId() == R.id.tbAvisos) {

			if (avisos) {
				editor.putBoolean("avisosOn", false);
				tbAvisos.setChecked(false);
			} else {
				editor.putBoolean("avisosOn", true);
				tbAvisos.setChecked(true);

			}
			editor.apply();

		} else if (v.getId() == R.id.tbZona) {

			if (zona) {
				editor.putBoolean("zonaOn", false);
				tbZona.setChecked(false);
			} else {
				editor.putBoolean("zonaOn", true);
				tbZona.setChecked(true);
			}

		} else if (v.getId() == R.id.tbLocalizacion) {

			if (loc) {
				editor.putBoolean("localizacionOn", false);
				tbLocalizacion.setChecked(false);
			}

			else {
				editor.putBoolean("localizacionOn", true);
				tbLocalizacion.setChecked(true);
			}

		} else if (v.getId() == R.id.tbCaidas) {

			if (caidas) {
				editor.putBoolean("caidasOn", false);
				tbCaidas.setChecked(false);
			} else {
				editor.putBoolean("caidasOn", true);
				tbCaidas.setChecked(true);
			}
		} else if (v.getId() == R.id.tbCaidasCasa) {

			if (caidasCasa) {
				editor.putBoolean("caidasCasaOn", false);
				tbCaidasCasa.setChecked(false);
			} else {
				editor.putBoolean("caidasCasaOn", true);
				tbCaidasCasa.setChecked(true);
			}

		}		
		
		editor.apply();
	}

}