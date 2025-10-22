package com.curso.ejemplo04basesdedatos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class AdminSQLiteOpenHelper extends  SQLiteOpenHelper {
    private static final String DATABASE_NAME = "agenda.db";
    private static final int DATABASE_VERSION = 1;

    // Sentencia SQL para crear la tabla de contactos
    private static final String CREATE_TABLE_CONTACTOS =
            "CREATE TABLE contactos (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT, apellido TEXT, telefono TEXT, email TEXT)";

    public AdminSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTACTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS contactos");
        onCreate(db);
    }

}
