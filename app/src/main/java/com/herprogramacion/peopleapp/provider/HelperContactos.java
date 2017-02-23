package com.herprogramacion.peopleapp.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.herprogramacion.peopleapp.provider.Contrato.Contactos;
import com.herprogramacion.peopleapp.utilidades.UTiempo;

/**
 * Clase auxiliar para controlar accesos a la base de datos SQLite
 */
public class HelperContactos extends SQLiteOpenHelper {

    static final int VERSION = 1;

    static final String NOMBRE_BD = "people_app.db";


    interface Tablas {
        String CONTACTO = "contacto";
    }

    public HelperContactos(Context context) {
        super(context, NOMBRE_BD, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + Tablas.CONTACTO + "("
                        + Contactos._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + Contactos.ID_CONTACTO + " TEXT UNIQUE,"
                        + Contactos.PRIMER_NOMBRE + " TEXT NOT NULL,"
                        + Contactos.PRIMER_APELLIDO + " TEXT,"
                        + Contactos.TELEFONO + " TEXT,"
                        + Contactos.CORREO + " TEXT,"
                        + Contactos.VERSION + " DATE DEFAULT CURRENT_TIMESTAMP,"
                        + Contactos.INSERTADO + " INTEGER DEFAULT 1,"
                        + Contactos.MODIFICADO + " INTEGER DEFAULT 0,"
                        + Contactos.ELIMINADO + " INTEGER DEFAULT 0)");

        // Registro ejemplo #1
        ContentValues valores = new ContentValues();
        valores.put(Contactos.ID_CONTACTO, Contactos.generarIdContacto());
        valores.put(Contactos.PRIMER_NOMBRE, "Roberto");
        valores.put(Contactos.PRIMER_APELLIDO, "Gomez");
        valores.put(Contactos.TELEFONO, "4444444");
        valores.put(Contactos.CORREO, "robertico@mail.com");
        valores.put(Contactos.VERSION, UTiempo.obtenerTiempo());

        db.insertOrThrow(Tablas.CONTACTO, null, valores);

        // Registro ejemplo #2
        valores.clear();
        valores.put(Contactos.ID_CONTACTO, Contactos.generarIdContacto());
        valores.put(Contactos.PRIMER_NOMBRE, "Pablo");
        valores.put(Contactos.PRIMER_APELLIDO, "Catatumbo");
        valores.put(Contactos.TELEFONO, "5555555");
        valores.put(Contactos.CORREO, "pablito@mail.com");
        valores.put(Contactos.VERSION, UTiempo.obtenerTiempo());
        db.insertOrThrow(Tablas.CONTACTO, null, valores);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + Tablas.CONTACTO);
        } catch (SQLiteException e) {
            // Manejo de excepciones
        }
        onCreate(db);
    }
}
