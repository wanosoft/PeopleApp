package com.herprogramacion.peopleapp.modelo;

import com.herprogramacion.peopleapp.utilidades.UTiempo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * POJO de los contactos
 */
public class Contacto {

    public String idContacto;
    public String primerNombre;
    public String primerApellido;
    public String telefono;
    public String correo;
    public String version;
    public int modificado;

    public Contacto(String idContacto, String primerNombre,
                    String primerApellido, String telefono, String correo, String version, int modificado) {
        this.idContacto = idContacto;
        this.primerNombre = primerNombre;
        this.primerApellido = primerApellido;
        this.telefono = telefono;
        this.correo = correo;
        this.version = version;
        this.modificado = modificado;
    }

    public void aplicarSanidad() {
        idContacto = idContacto == null ? "" : idContacto;
        primerNombre = primerNombre == null ? "" : primerNombre;
        primerApellido = primerApellido == null ? "" : primerApellido;
        telefono = telefono == null ? "" : telefono;
        correo = correo == null ? "" : correo;
        version = version == null ? UTiempo.obtenerTiempo() : version;
        modificado = 0;
    }

    public int esMasReciente(Contacto match) {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date fechaA = formato.parse(version);
            Date fechaB = formato.parse(match.version);

            return fechaA.compareTo(fechaB);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean compararCon(Contacto otro) {
        return idContacto.equals(otro.idContacto) &&
                primerNombre.equals(otro.primerNombre) &&
                primerApellido.equals(otro.primerApellido) &&
                telefono.equals(otro.telefono) &&
                correo.equals(otro.correo);
    }
}
