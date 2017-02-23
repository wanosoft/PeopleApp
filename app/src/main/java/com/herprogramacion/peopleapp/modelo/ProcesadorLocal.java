package com.herprogramacion.peopleapp.modelo;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.herprogramacion.peopleapp.provider.Contrato.Contactos;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Elemento que controla la transformación de JSON a POJO y viceversa
 */
public class ProcesadorLocal {

    private static final String TAG = ProcesadorLocal.class.getSimpleName();

    private interface ConsultaContactos {

        // Proyección para consulta de contactos
        String[] PROYECCION = {
                Contactos.ID_CONTACTO,
                Contactos.PRIMER_NOMBRE,
                Contactos.PRIMER_APELLIDO,
                Contactos.TELEFONO,
                Contactos.CORREO,
                Contactos.VERSION,
                Contactos.MODIFICADO
        };

        // Indices de columnas
        int ID_CONTACTO = 0;
        int PRIMER_NOMBRE = 1;
        int PRIMER_APELLIDO = 2;
        int TELEFONO = 3;
        int CORREO = 4;
        int VERSION = 5;
        int MODIFICADO = 6;

    }

    // Mapa para filtrar solo los elementos a sincronizar
    private HashMap<String, Contacto> contactosRemotos = new HashMap<>();

    // Conversor JSON
    private Gson gson = new Gson();

    public ProcesadorLocal() {
    }

    public void procesar(JSONArray arrayJsonContactos) {
        // Añadir elementos convertidos a los contactos remotos
        for (Contacto contactoActual : gson
                .fromJson(arrayJsonContactos.toString(), Contacto[].class)) {
            contactoActual.aplicarSanidad();
            contactosRemotos.put(contactoActual.idContacto, contactoActual);
        }
    }

    public void procesarOperaciones(ArrayList<ContentProviderOperation> ops, ContentResolver resolver) {

        // Consultar contactos locales
        Cursor c = resolver.query(Contactos.URI_CONTENIDO,
                ConsultaContactos.PROYECCION,
                Contactos.INSERTADO + "=?",
                new String[]{"0"}, null);

        if (c != null) {

            while (c.moveToNext()) {

                // Convertir fila del cursor en objeto Contacto
                Contacto filaActual = deCursorAContacto(c);

                // Buscar si el contacto actual se encuentra en el mapa de mapacontactos
                Contacto match = contactosRemotos.get(filaActual.idContacto);

                if (match != null) {
                    // Esta entrada existe, por lo que se remueve del mapeado
                    contactosRemotos.remove(filaActual.idContacto);

                    // Crear uri de este contacto
                    Uri updateUri = Contactos.construirUriContacto(filaActual.idContacto);

                    /*
                    Aquí se aplica la resolución de conflictos de modificaciones de un mismo recurso
                    tanto en el servidro como en la app. Quién tenga la versión más actual, será tomado
                    como preponderante
                     */
                    if (!match.compararCon(filaActual)) {
                        int flag = match.esMasReciente(filaActual);
                        if (flag > 0) {
                            Log.d(TAG, "Programar actualización  del contacto " + updateUri);

                            // Verificación: ¿Existe conflicto de modificación?
                            if (filaActual.modificado == 1) {
                                match.modificado = 0;
                            }
                            ops.add(construirOperacionUpdate(match, updateUri));

                        }

                    }

                } else {
                    /*
                    Se deduce que aquellos elementos que no coincidieron, ya no existen en el servidor,
                    por lo que se eliminarán
                     */
                    Uri deleteUri = Contactos.construirUriContacto(filaActual.idContacto);
                    Log.i(TAG, "Programar Eliminación del contacto " + deleteUri);
                    ops.add(ContentProviderOperation.newDelete(deleteUri).build());
                }
            }
            c.close();
        }

        // Insertar los items resultantes ya que se asume que no existen en el contacto
        for (Contacto contacto : contactosRemotos.values()) {
            Log.d(TAG, "Programar Inserción de un nuevo contacto con ID = " + contacto.idContacto);
            ops.add(construirOperacionInsert(contacto));
        }
    }

    private ContentProviderOperation construirOperacionInsert(Contacto contacto) {
        return ContentProviderOperation.newInsert(Contactos.URI_CONTENIDO)
                .withValue(Contactos.ID_CONTACTO, contacto.idContacto)
                .withValue(Contactos.PRIMER_NOMBRE, contacto.primerNombre)
                .withValue(Contactos.PRIMER_APELLIDO, contacto.primerApellido)
                .withValue(Contactos.TELEFONO, contacto.telefono)
                .withValue(Contactos.CORREO, contacto.correo)
                .withValue(Contactos.VERSION, contacto.version)
                .withValue(Contactos.INSERTADO, 0)
                .build();
    }

    private ContentProviderOperation construirOperacionUpdate(Contacto match, Uri updateUri) {
        return ContentProviderOperation.newUpdate(updateUri)
                .withValue(Contactos.ID_CONTACTO, match.idContacto)
                .withValue(Contactos.PRIMER_NOMBRE, match.primerNombre)
                .withValue(Contactos.PRIMER_APELLIDO, match.primerApellido)
                .withValue(Contactos.TELEFONO, match.telefono)
                .withValue(Contactos.CORREO, match.correo)
                .withValue(Contactos.VERSION, match.version)
                .withValue(Contactos.MODIFICADO, match.modificado)
                .build();
    }

    /**
     * Convierte una fila de un Cursor en un nuevo Contacto
     *
     * @param c cursor
     * @return objeto contacto
     */
    private Contacto deCursorAContacto(Cursor c) {
        return new Contacto(
                c.getString(ConsultaContactos.ID_CONTACTO),
                c.getString(ConsultaContactos.PRIMER_NOMBRE),
                c.getString(ConsultaContactos.PRIMER_APELLIDO),
                c.getString(ConsultaContactos.TELEFONO),
                c.getString(ConsultaContactos.CORREO),
                c.getString(ConsultaContactos.VERSION),
                c.getInt(ConsultaContactos.MODIFICADO)
        );
    }
}
