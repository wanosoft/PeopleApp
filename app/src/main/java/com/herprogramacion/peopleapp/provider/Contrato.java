package com.herprogramacion.peopleapp.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.UUID;

/**
 * Contrato con la estructura de la base de datos y forma de las URIs
 */
public class Contrato {

    interface ColumnasSincronizacion {
        String MODIFICADO = "modificado";
        String ELIMINADO = "eliminado";
        String INSERTADO = "insertado";
    }

    interface ColumnasContacto {
        String ID_CONTACTO = "idContacto"; // Pk
        String PRIMER_NOMBRE = "primerNombre";
        String PRIMER_APELLIDO = "primerApellido";
        String TELEFONO = "telefono";
        String CORREO = "correo";
        String VERSION = "version";
    }


    // Autoridad del Content Provider
    public final static String AUTORIDAD = "com.herprogramacion.peopleapp";

    // Uri base
    public final static Uri URI_CONTENIDO_BASE = Uri.parse("content://" + AUTORIDAD);


    /**
     * Controlador de la tabla "contacto"
     */
    public static class Contactos
            implements BaseColumns, ColumnasContacto, ColumnasSincronizacion {

        public static final Uri URI_CONTENIDO =
                URI_CONTENIDO_BASE.buildUpon().appendPath(RECURSO_CONTACTO).build();

        public final static String MIME_RECURSO =
                "vnd.android.cursor.item/vnd." + AUTORIDAD + "/" + RECURSO_CONTACTO;

        public final static String MIME_COLECCION =
                "vnd.android.cursor.dir/vnd." + AUTORIDAD + "/" + RECURSO_CONTACTO;


        /**
         * Construye una {@link Uri} para el {@link #ID_CONTACTO} solicitado.
         */
        public static Uri construirUriContacto(String idContacto) {
            return URI_CONTENIDO.buildUpon().appendPath(idContacto).build();
        }

        public static String generarIdContacto() {
            return "C-" + UUID.randomUUID();
        }

        public static String obtenerIdContacto(Uri uri) {
            return uri.getLastPathSegment();
        }
    }

    // Recursos
    public final static String RECURSO_CONTACTO = "contactos";

}
