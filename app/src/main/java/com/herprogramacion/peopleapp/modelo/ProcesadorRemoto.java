package com.herprogramacion.peopleapp.modelo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.herprogramacion.peopleapp.provider.Contrato.Contactos;
import com.herprogramacion.peopleapp.utilidades.UConsultas;
import com.herprogramacion.peopleapp.utilidades.UDatos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actua como un transformador desde SQLite a JSON para enviar contactos al servidor
 */
public class ProcesadorRemoto {
    private static final String TAG = ProcesadorRemoto.class.getSimpleName();

    // Campos JSON
    private static final String INSERCIONES = "inserciones";
    private static final String MODIFICACIONES = "modificaciones";
    private static final String ELIMINACIONES = "eliminaciones";

    private Gson gson = new Gson();

    private interface ConsultaContactos {

        // Proyección para consulta de contactos
        String[] PROYECCION = {
                Contactos.ID_CONTACTO,
                Contactos.PRIMER_NOMBRE,
                Contactos.PRIMER_APELLIDO,
                Contactos.TELEFONO,
                Contactos.CORREO,
                Contactos.VERSION
        };
    }


    public String crearPayload(ContentResolver cr) {
        HashMap<String, Object> payload = new HashMap<>();

        List<Map<String, Object>> inserciones = obtenerInserciones(cr);
        List<Map<String, Object>> modificaciones = obtenerModificaciones(cr);
        List<String> eliminaciones = obtenerEliminaciones(cr);

        // Verificación: ¿Hay cambios locales?
        if (inserciones == null && modificaciones == null && eliminaciones == null) {
            return null;
        }

        payload.put(INSERCIONES, inserciones);
        payload.put(MODIFICACIONES, modificaciones);
        payload.put(ELIMINACIONES, eliminaciones);

        return gson.toJson(payload);
    }

    public List<Map<String, Object>> obtenerInserciones(ContentResolver cr) {
        List<Map<String, Object>> ops = new ArrayList<>();

        // Obtener contactos donde 'insertado' = 1
        Cursor c = cr.query(Contactos.URI_CONTENIDO,
                ConsultaContactos.PROYECCION,
                Contactos.INSERTADO + "=?",
                new String[]{"1"}, null);

        // Comprobar si hay trabajo que realizar
        if (c != null && c.getCount() > 0) {

            Log.d(TAG, "Inserciones remotas: " + c.getCount());

            // Procesar inserciones
            while (c.moveToNext()) {
                ops.add(mapearInsercion(c));
            }

            return ops;

        } else {
            return null;
        }

    }

    public List<Map<String, Object>> obtenerModificaciones(ContentResolver cr) {

        List<Map<String, Object>> ops = new ArrayList<>();

        // Obtener contactos donde 'modificado' = 1
        Cursor c = cr.query(Contactos.URI_CONTENIDO,
                ConsultaContactos.PROYECCION,
                Contactos.MODIFICADO + "=?",
                new String[]{"1"}, null);

        // Comprobar si hay trabajo que realizar
        if (c != null && c.getCount() > 0) {

            Log.d(TAG, "Existen " + c.getCount() + " modificaciones de contactos");

            // Procesar operaciones
            while (c.moveToNext()) {
                ops.add(mapearActualizacion(c));
            }

            return ops;

        } else {
            return null;
        }

    }

    public List<String> obtenerEliminaciones(ContentResolver cr) {

        List<String> ops = new ArrayList<>();

        // Obtener contactos donde 'eliminado' = 1
        Cursor c = cr.query(Contactos.URI_CONTENIDO,
                ConsultaContactos.PROYECCION,
                Contactos.ELIMINADO + "=?",
                new String[]{"1"}, null);

        // Comprobar si hay trabajo que realizar
        if (c != null && c.getCount() > 0) {

            Log.d(TAG, "Existen " + c.getCount() + " eliminaciones de contactos");

            // Procesar operaciones
            while (c.moveToNext()) {
                ops.add(UConsultas.obtenerString(c, Contactos.ID_CONTACTO));
            }

            return ops;

        } else {
            return null;
        }

    }


    /**
     * Desmarca los contactos locales que ya han sido sincronizados
     *
     * @param cr content resolver
     */
    public void desmarcarContactos(ContentResolver cr) {
        // Establecer valores de la actualización
        ContentValues valores = new ContentValues();
        valores.put(Contactos.INSERTADO, 0);
        valores.put(Contactos.MODIFICADO, 0);

        String seleccion = Contactos.INSERTADO + " = ? OR " +
                Contactos.MODIFICADO + "= ?";
        String[] argumentos = {"1", "1"};

        // Modificar banderas de insertados y modificados
        cr.update(Contactos.URI_CONTENIDO, valores, seleccion, argumentos);

        seleccion = Contactos.ELIMINADO + "=?";
        // Eliminar definitivamente
        cr.delete(Contactos.URI_CONTENIDO, seleccion, new String[]{"1"});

    }

    private Map<String, Object> mapearInsercion(Cursor c) {
        // Nuevo mapa para reflejarlo en JSON
        Map<String, Object> mapaContacto = new HashMap<String, Object>();

        // Añadir valores de columnas como atributos
        UDatos.agregarStringAMapa(mapaContacto, Contactos.ID_CONTACTO, c);
        UDatos.agregarStringAMapa(mapaContacto, Contactos.PRIMER_NOMBRE, c);
        UDatos.agregarStringAMapa(mapaContacto, Contactos.PRIMER_APELLIDO, c);
        UDatos.agregarStringAMapa(mapaContacto, Contactos.TELEFONO, c);
        UDatos.agregarStringAMapa(mapaContacto, Contactos.CORREO, c);
        UDatos.agregarStringAMapa(mapaContacto, Contactos.VERSION, c);

        return mapaContacto;
    }

    private Map<String, Object> mapearActualizacion(Cursor c) {
        // Nuevo mapa para reflejarlo en JSON
        Map<String, Object> mapaContacto = new HashMap<String, Object>();

        // Añadir valores de columnas como atributos
        UDatos.agregarStringAMapa(mapaContacto, Contactos.ID_CONTACTO, c);
        UDatos.agregarStringAMapa(mapaContacto, Contactos.PRIMER_NOMBRE, c);
        UDatos.agregarStringAMapa(mapaContacto, Contactos.PRIMER_APELLIDO, c);
        UDatos.agregarStringAMapa(mapaContacto, Contactos.TELEFONO, c);
        UDatos.agregarStringAMapa(mapaContacto, Contactos.CORREO, c);
        UDatos.agregarStringAMapa(mapaContacto, Contactos.VERSION, c);

        return mapaContacto;
    }
}
