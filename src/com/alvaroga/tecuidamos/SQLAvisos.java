package com.alvaroga.tecuidamos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.alvaroga.tecuidamos.alarmas.AvisoAlarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

public class SQLAvisos {

	public static final String PREFS_NAME = "Prefs";

	public static final String KEY_ROWID = "_id";

	public static final String KEY_NOMBRE = "nombre";

	public static final String KEY_VECES = "veces";

	public static final String KEY_HORAS = "horas";

	public static final String KEY_STOCK = "stock";

	private static final String DATABASE_data = "SQL";

	private static final String DATABASE_TABLE = "SQL";

	private static final int DATABASE_VERSION = 1;

	/**
	 * El Helper ayuda a establecer una conexión entre el móvil y la base de
	 * datos. Leer SQLiteOpenHelper en caso de necesitar más
	 */
	private DBHelper ourHelper;
	/**
	 * El contexto de la aplicación.
	 */
	private final Context context;
	/**
	 * Instancia de nuestra base de datos.
	 */
	private static SQLiteDatabase ourDatabase;

	/**
	 * Columnas de nuestra base de datos.
	 */
	static final String columnas[] = new String[] { KEY_ROWID, KEY_NOMBRE,
			KEY_VECES, KEY_HORAS, KEY_STOCK };


	private static class DBHelper extends SQLiteOpenHelper {

		/**
		 * @param context
		 */
		public DBHelper(Context context) {
			super(context, DATABASE_data, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database
		 * .sqlite.SQLiteDatabase)
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" + KEY_ROWID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NOMBRE
					+ " TEXT, " + KEY_VECES + " INTEGER, " + KEY_HORAS
					+ " TEXT, " + KEY_STOCK + " INTEGER);"

			);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database
		 * .sqlite.SQLiteDatabase, int, int)
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}

	public SQLAvisos(Context c) {
		context = c;
	}

	public void open() throws SQLException {
		ourHelper = new DBHelper(context);
		ourDatabase = ourHelper.getWritableDatabase();

	}

	public void close() {
		ourHelper.close();
	}

	public void newMedicamento(String nombre, int veces, String fechas,
			int stock,int id, boolean b) throws Exception { //

		ContentValues cv = new ContentValues();
		cv.put(KEY_NOMBRE, nombre);
		cv.put(KEY_VECES, veces);

		cv.put(KEY_HORAS, fechas);
		cv.put(KEY_STOCK, stock);
		ourDatabase.insert(DATABASE_TABLE, null, cv);

		if (b) newAviso(nombre, veces, fechas,id);


	}

	// Este es el metodo que lanza la alarma para el aviso.
	private void newAviso(String nombre, int veces, String fechas,int id) {
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int mins = c.get(Calendar.MINUTE);
		int ahora = hour * 60 + mins;

		String[] fechasA = fechas.split(" ");

		int tiempo = 24 * 60;
		for (int i = 0; i < veces; i++) {
			int horaG = Integer.parseInt(fechasA[i].substring(0, 2));
			int minsG = Integer.parseInt(fechasA[i].substring(3, 5));
			int totalG = horaG * 60 + minsG;
			
			if (totalG == ahora) continue;

			else if (totalG > ahora) {
				int restante = totalG - ahora;
				if (restante < tiempo)
					tiempo = restante;

			} else {
				int restante = 24 * 60 - totalG + ahora;
				if (restante < tiempo)
					tiempo = restante;

			}
		}
		AvisoAlarm alarm = new AvisoAlarm();
		Bundle extras = new Bundle();
		extras.putString("nombre", nombre);
		extras.putInt("id", id);


		alarm.setAlarm(context, tiempo, extras);
	}

	public String[] getLista() {

		List<String> list = new ArrayList<String>();

		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columnas, null, null,
				null, null, null);

		if (cursor != null && cursor.moveToFirst()) {

			do {
				String data = cursor.getString(cursor
						.getColumnIndex(KEY_NOMBRE));
				list.add(data);
			} while (cursor.moveToNext());

		}
		String[] array = new String[list.size()];
		if (list.size() == 0) {
			array = new String[1];
			array[0] = context.getString(R.string.nohayavisos);
		} else {
			for (int i = 0; i < array.length; i++) {
				array[i] = list.get(i);
			}
		}
		cursor.close();
		return array;
	}

	public int getStock(String nombre) {

		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columnas, KEY_NOMBRE
				+ "=" + "'" + nombre + "'", null, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {

			int data = cursor.getInt(cursor.getColumnIndex(KEY_STOCK));
			cursor.close();
			return data;
		}
		cursor.close();
		return 0;
	}

	public String getHoras(String nombre) {

		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columnas, KEY_NOMBRE
				+ "=" + "'" + nombre + "'", null, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {

			String data = cursor.getString(cursor.getColumnIndex(KEY_HORAS));
			cursor.close();
			return data;
		}
		cursor.close();
		return null;
	}

	public int getVeces(String nombre) {

		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columnas, KEY_NOMBRE
				+ "=" + "'" + nombre + "'", null, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {

			int data = cursor.getInt(cursor.getColumnIndex(KEY_VECES));
			cursor.close();
			return data;
		}
		cursor.close();
		return 0;
	}

	public void borraEntrada(String nombre) {
		ourDatabase.delete(DATABASE_TABLE, KEY_NOMBRE + "=" + "'" + nombre
				+ "'", null);

	}

	public void reducirStock(String nombre) {
		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columnas, KEY_NOMBRE
				+ "=" + "'" + nombre + "'", null, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {

			int data = cursor.getInt(cursor.getColumnIndex(KEY_STOCK));
			cursor.close();
			ContentValues cv = new ContentValues();
			cv.put(KEY_STOCK, data - 1);
			ourDatabase.update(DATABASE_TABLE, cv, KEY_NOMBRE + "=" + "'"
					+ nombre + "'", null);

		}
		cursor.close();

	}
	public void setStock(String nombre,int stock) {
		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columnas, KEY_NOMBRE
				+ "=" + "'" + nombre + "'", null, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {

			cursor.close();
			ContentValues cv = new ContentValues();
			cv.put(KEY_STOCK, stock);
			ourDatabase.update(DATABASE_TABLE, cv, KEY_NOMBRE + "=" + "'"
					+ nombre + "'", null);

		}
		cursor.close();

	}

	public void setNextAlarma(String nombre,int id) {
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int mins = c.get(Calendar.MINUTE);
		int ahora = hour * 60 + mins;

		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columnas, KEY_NOMBRE
				+ "=" + "'" + nombre + "'", null, null, null, null);

		int tiempo = 24 * 60;

		if (cursor != null && cursor.moveToFirst()) {

			int veces = cursor.getInt(cursor.getColumnIndex(KEY_VECES));
			String fechas = cursor.getString(cursor.getColumnIndex(KEY_HORAS));

			String[] fechasA = fechas.split(" ");

			for (int i = 0; i < veces; i++) {
				int horaG = Integer.parseInt(fechasA[i].substring(0, 2));
				int minsG = Integer.parseInt(fechasA[i].substring(3, 5));
				int totalG = horaG * 60 + minsG;
				
				if (totalG == ahora) continue;

				else if (totalG > ahora) {
					int restante = totalG - ahora;
					if (restante < tiempo)
						tiempo = restante;

				} else {
					int restante = 24 * 60 - totalG + ahora;
					if (restante < tiempo)
						tiempo = restante;

				}
			}
		}

		AvisoAlarm alarm = new AvisoAlarm();
		Bundle extras = new Bundle();
		extras.putString("nombre", nombre);
		extras.putInt("id", id);

		alarm.setAlarm(context, tiempo, extras);
	}

	public void setAllNextAlarmas() {
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int mins = c.get(Calendar.MINUTE);
		int ahora = hour * 60 + mins;

		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columnas, null, null,
				null, null, null);

		int tiempo = 24 * 60;
		int ultima = getUltimaFila();

		if (cursor != null && cursor.moveToFirst()) {

			for (int i = 0; i < ultima; i++) {

				try {
					String nombre = cursor.getString(cursor
							.getColumnIndex(KEY_NOMBRE));
					int veces = cursor.getInt(cursor.getColumnIndex(KEY_VECES));
					String fechas = cursor.getString(cursor
							.getColumnIndex(KEY_HORAS));

					String[] fechasA = fechas.split(" ");

					for (int j = 0; j < veces; j++) {
						int horaG = Integer
								.parseInt(fechasA[j].substring(0, 2));
						int minsG = Integer
								.parseInt(fechasA[j].substring(3, 5));
						int totalG = horaG * 60 + minsG;

						if (totalG > ahora) {
							int restante = totalG - ahora;
							if (restante < tiempo)
								tiempo = restante;
						} else {
							int restante = 24 * 60 + totalG - ahora;
							if (restante < tiempo)
								tiempo = restante;
						}
					}
					AvisoAlarm alarm = new AvisoAlarm();
					Bundle extras = new Bundle();
					extras.putString("nombre", nombre);
					alarm.setAlarm(context, tiempo, extras);

				} catch (Exception e) {
					continue;
				}
			}

		}
	}

	private int getUltimaFila() {
		Cursor cursor = ourDatabase.query(DATABASE_TABLE, columnas, null, null,
				null, null, null);

		try {
			cursor.moveToLast();
			int ultimo = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));
			cursor.close();
			return ultimo;
		} catch (CursorIndexOutOfBoundsException e) {
			cursor.close();
			return 0;
		} catch (Exception e) {
			cursor.close();
			e.printStackTrace();
			return 0;
		}

	}

}
