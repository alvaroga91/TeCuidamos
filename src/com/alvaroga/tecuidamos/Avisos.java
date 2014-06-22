package com.alvaroga.tecuidamos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alvaroga.tecuidamos.alarmas.AvisoAlarm;

public class Avisos extends Activity implements OnClickListener {

	Context context;
	ArrayAdapter<String> modeAdapter;
	int numLista = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.avisos);
		context = this;
		((Button) findViewById(R.id.bNuevoAviso)).setOnClickListener(this);

		showLista();

	}

	private void showLista() {
		ListView lv = (ListView) findViewById(R.id.lvAvisos);

		final SQLAvisos db = new SQLAvisos(this);
		db.open();
		final String lista[] = db.getLista();
		db.close();
		numLista = lista.length;
		if (lista[0].matches("No hay avisos"))
			numLista = 0;
		modeAdapter = new ArrayAdapter<String>(this, R.layout.entradas, lista);

		lv.setAdapter(modeAdapter);

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					final int pos, long idItem) {

				if (lista[0] != SQLAvisos.NULL)
					showInfoDialog(lista, pos);

			}

		});
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bNuevoAviso) {
			nuevoAvisoDialog();
		}
	}

	private void nuevoAvisoDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final View view = LayoutInflater.from(this).inflate(
				R.layout.dialognuevoaviso, null);
		builder.setTitle("Inserta los datos");
		builder.setView(view);

		final Context context = this;

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				EditText etNombre = (EditText) view.findViewById(R.id.etNombre);
				EditText etVeces = (EditText) view.findViewById(R.id.etVeces);
				EditText etStock = (EditText) view.findViewById(R.id.etStock);

				final String nombre = etNombre.getText().toString();
				final String vecesStr = etVeces.getText().toString();
				final String stockStr = etStock.getText().toString();

				try {

					if (Integer.parseInt(vecesStr) > 5
							|| Integer.parseInt(vecesStr) < 1)
						throw new Exception(
								"Solo se permite de 1 a 5 veces por día");
					if (Integer.parseInt(stockStr) < 1)
						throw new Exception(
								"El stock tiene que ser mayor que 0");

					final View viewVeces = createHorasView(Integer
							.parseInt(vecesStr));

					AlertDialog.Builder builder2 = new AlertDialog.Builder(
							context);
					builder2.setTitle("Inserta las horas");
					builder2.setView(viewVeces);

					builder2.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

									try {

										int veces = Integer.parseInt(vecesStr);
										String horas = "";
										for (int i = 0; i < veces; i++) {
											EditText et = (EditText) viewVeces
													.findViewById(100 + i);
											String hora = et.getText()
													.toString().trim();

											if (hora.length() != 5)
												throw new Exception(
														"Formato de hora incorrecto.");
											int horaI = Integer.parseInt(hora
													.substring(0, 2));
											int minI = Integer.parseInt(hora
													.substring(3, 5));

											if (horaI < 0 || horaI > 23)
												throw new Exception(
														"Hora no válida");
											if (minI < 0 || minI > 59)
												throw new Exception(
														"Minutos no válidos");

											horas += hora + " ";

										}
										SharedPreferences settings = context
												.getSharedPreferences(
														"Prefs",
														Context.MODE_MULTI_PROCESS);

										int id = numLista;
										String[] horasA = horas.split(" ");
										if (horasA.length != veces)
											throw new Exception();
										SQLAvisos db = new SQLAvisos(context);
										db.open();
										db.newMedicamento(nombre, veces, horas,
												Integer.parseInt(stockStr), id,
												settings.getBoolean("avisosOn",
														true));
										db.close();
										refreshList();

									} catch (SQLException e) {
										Toast.makeText(context,
												"Error al guardar entrada",
												Toast.LENGTH_SHORT).show();
									} catch (Exception e) {
										e.printStackTrace();
										Toast.makeText(context,
												"Error al formatear las horas",
												Toast.LENGTH_SHORT).show();
										nuevoAvisoDialog();
									}

								}
							});

					builder2.setNegativeButton("Cancelar",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

								}
							});

					AlertDialog dialog2 = builder2.create();
					dialog2.setCanceledOnTouchOutside(false);
					dialog2.show();

				} catch (NumberFormatException e) {
					e.printStackTrace();
					Toast.makeText(context, "Error al obtener los datos",
							Toast.LENGTH_SHORT).show();
					nuevoAvisoDialog();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT)
							.show();
					nuevoAvisoDialog();
				}

			}

			private View createHorasView(int veces) {
				LinearLayout ll = new LinearLayout(context);
				LayoutParams llParams = new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				ll.setOrientation(LinearLayout.VERTICAL);
				ll.setLayoutParams(llParams);
				
				for (int i = 0; i < veces; i++) {
					LinearLayout llVeces = new LinearLayout(context);
					LayoutParams llTvParams = new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);
					llVeces.setOrientation(LinearLayout.HORIZONTAL);

					llVeces.setLayoutParams(llTvParams);

					TextView tv = new TextView(context);
					tv.setText("Hora " + (i + 1));
					EditText et = new EditText(context);

					et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
					if (i == 0) {
						et.requestFocus();
						et.setHint("Por ejemplo, 15:30");
					}
					et.setId(i + 100);
					et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							5) });
					tv.setLayoutParams(new LinearLayout.LayoutParams(0,
							LayoutParams.WRAP_CONTENT, 1f));
					et.setLayoutParams(new LinearLayout.LayoutParams(0,
							LayoutParams.WRAP_CONTENT, 3f));

					llVeces.addView(tv);
					llVeces.addView(et);

					ll.addView(llVeces);

				}
				return ll;
			}

		});
		builder.setNegativeButton("Cancelar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	private void showBorrarEntradaDialog(final int pos, final String[] lista) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle("Borrar entrada");
		builder.setMessage("¿Estás seguro que deseas borrar esta entrada? No se harán futuros avisos");
		builder.setPositiveButton("Borrar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						SQLAvisos db = new SQLAvisos(context);
						db.open();
						db.borraEntrada(lista[pos]);
						db.close();
						refreshList();

						AvisoAlarm alarm = new AvisoAlarm();
						Bundle extras = new Bundle();
						extras.putString("nombre", lista[pos]);
						extras.putInt("id", pos);

						alarm.cancelAlarm(context, extras);
					};
				});
		builder.setNegativeButton("Cancelar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					};
				});
		builder.show();
	}

	private void showActualizarStockDialog(final int pos, final String[] lista) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final View view = LayoutInflater.from(this).inflate(
				R.layout.stockdialog, null);
		builder.setView(view);
		builder.setTitle("Dosis a añadir");
		builder.setPositiveButton("Cambiar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						EditText et = (EditText) view
								.findViewById(R.id.etAddStock);
						int etStock = Integer.parseInt(et.getText().toString());

						if (etStock < 0)
							etStock = 0;

						SQLAvisos db = new SQLAvisos(context);
						db.open();
						int stock = db.getStock(lista[pos]);
						db.setStock(lista[pos], stock + etStock);
						db.close();
						refreshList();
					};
				});
		builder.setNegativeButton("Cancelar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					};
				});
		builder.show();

	};

	private void showInfoDialog(final String[] lista, final int pos) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		String nombre = lista[pos];
		builder.setTitle(nombre);
		SQLAvisos db = new SQLAvisos(context);
		db.open();
		builder.setMessage("Veces al día: " + db.getVeces(nombre) + "\n"
				+ "Horas: " + db.getHoras(nombre) + "\n" + "Stock restante: "
				+ db.getStock(nombre));
		db.close();
		builder.setPositiveButton("Pastillas restantes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						showActualizarStockDialog(pos, lista);

					}

				});
		builder.setNegativeButton("Borrar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						showBorrarEntradaDialog(pos, lista);
					};
				});
		builder.setNeutralButton("Cancelar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		builder.show();
	}

	private void refreshList() {
		showLista();
		modeAdapter.notifyDataSetChanged();
	}
}
