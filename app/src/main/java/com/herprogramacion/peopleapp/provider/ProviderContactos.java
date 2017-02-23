package com.herprogramacion.peopleapp.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.herprogramacion.peopleapp.provider.Contrato.Contactos;
import com.herprogramacion.peopleapp.provider.HelperContactos.Tablas;

/**
 * {@link ContentProvider} que encapsula el acceso a la base de datos de contactos
 */
public class ProviderContactos extends ContentProvider {

    // Comparador de URIs de contenido
    public static final UriMatcher uriMatcher;

    // Identificadores de tipos
    public static final int CONTACTOS = 100;
    public static final int CONTACTOS_ID = 101;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Contrato.AUTORIDAD, "contactos", CONTACTOS);
        uriMatcher.addURI(Contrato.AUTORIDAD, "contactos/*", CONTACTOS_ID);
    }

    private HelperContactos manejadorBD;
    private ContentResolver resolver;


    @Override
    public boolean onCreate() {
        manejadorBD = new HelperContactos(getContext());
        resolver = getContext().getContentResolver();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CONTACTOS:
                return Contactos.MIME_COLECCION;
            case CONTACTOS_ID:
                return Contactos.MIME_RECURSO;
            default:
                throw new IllegalArgumentException("Tipo desconocido: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Obtener base de datos
        SQLiteDatabase db = manejadorBD.getWritableDatabase();
        // Comparar Uri
        int match = uriMatcher.match(uri);

        Cursor c;

        switch (match) {
            case CONTACTOS:
                // Consultando todos los registros
                c = db.query(Tablas.CONTACTO, projection,
                        selection, selectionArgs,
                        null, null, sortOrder);
                c.setNotificationUri(resolver, Contactos.URI_CONTENIDO);
                break;
            case CONTACTOS_ID:
                // Consultando un solo registro basado en el Id del Uri
                String idContacto = Contactos.obtenerIdContacto(uri);
                c = db.query(Tablas.CONTACTO, projection,
                        Contactos.ID_CONTACTO + "=" + "\'" + idContacto + "\'"
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs, null, null, sortOrder);
                c.setNotificationUri(resolver, uri);
                break;
            default:
                throw new IllegalArgumentException("URI no soportada: " + uri);
        }
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = manejadorBD.getWritableDatabase();

        int match = uriMatcher.match(uri);
        int filasAfectadas;

        switch (match) {
            case CONTACTOS:

                filasAfectadas = db.delete(Tablas.CONTACTO,
                        selection,
                        selectionArgs);

                resolver.notifyChange(uri, null, false);

                break;
            case CONTACTOS_ID:

                String idContacto = Contactos.obtenerIdContacto(uri);

                filasAfectadas = db.delete(Tablas.CONTACTO,
                        Contactos.ID_CONTACTO + "=" + "\'" + idContacto + "\'"
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs);

                resolver.notifyChange(uri, null, false);
                break;
            default:
                throw new IllegalArgumentException("Contacto desconocido: " +
                        uri);
        }
        return filasAfectadas;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Validar la uri
        if (uriMatcher.match(uri) != CONTACTOS) {
            throw new IllegalArgumentException("URI desconocida : " + uri);
        }

        ContentValues contentValues;
        if (values != null) {
            contentValues = new ContentValues(values);
        } else {
            contentValues = new ContentValues();
        }

        // Inserción de nueva fila
        SQLiteDatabase db = manejadorBD.getWritableDatabase();

        long _id = db.insert(Tablas.CONTACTO, null, contentValues);

        if (_id > 0) {

            resolver.notifyChange(uri, null, false);

            String idContacto = contentValues.getAsString(Contactos.ID_CONTACTO);

            return Contactos.construirUriContacto(idContacto);
        }
        throw new SQLException("Falla al insertar fila en : " + uri);
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        SQLiteDatabase db = manejadorBD.getWritableDatabase();

        int filasAfectadas;

        switch (uriMatcher.match(uri)) {
            case CONTACTOS:

                filasAfectadas = db.update(Tablas.CONTACTO, values,
                        selection, selectionArgs);

                resolver.notifyChange(uri, null, false);

                break;
            case CONTACTOS_ID:

                String idContacto = Contactos.obtenerIdContacto(uri);

                filasAfectadas = db.update(Tablas.CONTACTO, values,
                        Contactos.ID_CONTACTO + "=" + "\'" + idContacto + "\'"
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs);

                resolver.notifyChange(uri, null, false);

                break;
            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }



        return filasAfectadas;
    }
}
