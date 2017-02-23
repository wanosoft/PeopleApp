package com.herprogramacion.peopleapp.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.herprogramacion.peopleapp.R;
import com.herprogramacion.peopleapp.provider.Contrato.Contactos;
import com.herprogramacion.peopleapp.utilidades.UConsultas;
import com.herprogramacion.peopleapp.utilidades.UTiempo;

public class ActividadInsercionContacto extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // Referencias UI
    private EditText campoPrimerNombre;
    private EditText campoPrimerApellido;
    private EditText campoTelefono;
    private EditText campoCorreo;


    // Clave del uri del contacto como extra
    public static final String URI_CONTACTO = "extra.uriContacto";

    private Uri uriContacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_insercion_contacto);

        agregarToolbar();

        // Encontrar Referencias UI
        campoPrimerNombre = (EditText) findViewById(R.id.campo_primer_nombre);
        campoPrimerApellido = (EditText) findViewById(R.id.campo_primer_apellido);
        campoTelefono = (EditText) findViewById(R.id.campo_telefono);
        campoCorreo = (EditText) findViewById(R.id.campo_correo);

        // Determinar si es detalle
        String uri = getIntent().getStringExtra(URI_CONTACTO);
        if (uri != null) {
            setTitle(R.string.titulo_actividad_editar_contacto);
            uriContacto = Uri.parse(uri);
            getSupportLoaderManager().restartLoader(1, null, this);
        }

    }

    private void agregarToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_insercion_contacto, menu);

        // Verificación de visibilidad acción eliminar
        if (uriContacto != null) {
            menu.findItem(R.id.accion_eliminar).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.accion_confirmar:
                insertar();
                break;
            case R.id.accion_eliminar:
                eliminar();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void insertar() {

        // Extraer datos de UI
        String primerNombre = campoPrimerNombre.getText().toString();
        String primerApellido = campoPrimerApellido.getText().toString();
        String telefono = campoTelefono.getText().toString();
        String correo = campoCorreo.getText().toString();

        // Validaciones y pruebas de cordura
        if (!esNombreValido(primerNombre)) {
            TextInputLayout mascaraCampoNombre = (TextInputLayout) findViewById(R.id.mascara_campo_nombre);
            mascaraCampoNombre.setError("Este campo no puede quedar vacío");
        } else {

            ContentValues valores = new ContentValues();

            // Verificación: ¿Es necesario generar un id?
            if (uriContacto == null) {
                valores.put(Contactos.ID_CONTACTO, Contactos.generarIdContacto());
            }
            valores.put(Contactos.PRIMER_NOMBRE, primerNombre);
            valores.put(Contactos.PRIMER_APELLIDO, primerApellido);
            valores.put(Contactos.TELEFONO, telefono);
            valores.put(Contactos.CORREO, correo);
            valores.put(Contactos.VERSION, UTiempo.obtenerTiempo());

            // Iniciar inserción|actualización
            new TareaAnadirContacto(getContentResolver(), valores).execute(uriContacto);

            finish();
        }
    }

    private boolean esNombreValido(String nombre) {
        return !TextUtils.isEmpty(nombre);
    }

    private void eliminar() {
        if (uriContacto != null) {
            // Iniciar eliminación
            new TareaEliminarContacto(getContentResolver()).execute(uriContacto);
            finish();
        }
    }


    private void poblarViews(Cursor data) {
        if (!data.moveToNext()) {
            return;
        }

        // Asignar valores a UI
        campoPrimerNombre.setText(UConsultas.obtenerString(data, Contactos.PRIMER_NOMBRE));
        campoPrimerApellido.setText(UConsultas.obtenerString(data, Contactos.PRIMER_APELLIDO));
        campoTelefono.setText(UConsultas.obtenerString(data, Contactos.TELEFONO));
        campoCorreo.setText(UConsultas.obtenerString(data, Contactos.CORREO));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, uriContacto, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        poblarViews(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    static class TareaAnadirContacto extends AsyncTask<Uri, Void, Void> {
        private final ContentResolver resolver;
        private final ContentValues valores;

        public TareaAnadirContacto(ContentResolver resolver, ContentValues valores) {
            this.resolver = resolver;
            this.valores = valores;
        }

        @Override
        protected Void doInBackground(Uri... args) {
            Uri uri = args[0];
            if (null != uri) {
                /*
                Verificación: Si el contacto que se va a actualizar aún no ha sido sincronizado,
                es decir su columna 'insertado' = 1, entonces la columna 'modificado' no debe ser
                alterada
                 */
                Cursor c = resolver.query(uri, new String[]{Contactos.INSERTADO}, null, null, null);

                if (c != null && c.moveToNext()) {

                    // Verificación de sincronización
                    if (UConsultas.obtenerInt(c, Contactos.INSERTADO) == 0) {
                        valores.put(Contactos.MODIFICADO, 1);
                    }

                    valores.put(Contactos.VERSION, UTiempo.obtenerTiempo());
                    resolver.update(uri, valores, null, null);
                }

            } else {
                resolver.insert(Contactos.URI_CONTENIDO, valores);
            }
            return null;
        }

    }

    static class TareaEliminarContacto extends AsyncTask<Uri, Void, Void> {
        private final ContentResolver resolver;

        public TareaEliminarContacto(ContentResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        protected Void doInBackground(Uri... args) {

            /*
            Verificación: Si el registro no ha sido sincronizado aún, entonces puede eliminarse
            directamente. De lo contrario se marca como 'eliminado' = 1
             */
            Cursor c = resolver.query(args[0], new String[]{Contactos.INSERTADO}
                    , null, null, null);

            int insertado;

            if (c != null && c.moveToNext()) {
                insertado = UConsultas.obtenerInt(c, Contactos.INSERTADO);
            } else {
                return null;
            }

            if (insertado == 1) {
                resolver.delete(args[0], null, null);
            } else if (insertado == 0) {
                ContentValues valores = new ContentValues();
                valores.put(Contactos.ELIMINADO, 1);
                resolver.update(args[0], valores, null, null);
            }

            return null;
        }
    }
}
