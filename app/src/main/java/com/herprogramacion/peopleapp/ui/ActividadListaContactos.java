package com.herprogramacion.peopleapp.ui;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.herprogramacion.peopleapp.R;
import com.herprogramacion.peopleapp.provider.Contrato;
import com.herprogramacion.peopleapp.provider.Contrato.Contactos;
import com.herprogramacion.peopleapp.utilidades.UCuentas;
import com.herprogramacion.peopleapp.utilidades.UPreferencias;
import com.herprogramacion.peopleapp.utilidades.UWeb;

public class ActividadListaContactos extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, AdaptadorContactos.OnItemClickListener {

    private static final String TAG = ActividadListaContactos.class.getSimpleName();

    // Referencias UI
    private RecyclerView reciclador;
    private LinearLayoutManager layoutManager;
    private AdaptadorContactos adaptador;

    private BroadcastReceiver receptorSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_lista_contactos);

        // Preparar elementos UI
        prepararToolbar();
        prepararFab();
        prepararLista();


        getSupportLoaderManager().restartLoader(1, null, this);

        // Reemplaza con tu clave
        UPreferencias.guardarClaveApi(this, "60d5b4e60cb6a70898f0cd17174e9edd");

        // Crear receptor de mensajes de sincronización
        receptorSync = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                mostrarProgreso(false);
                String mensaje = intent.getStringExtra("extra.mensaje");
                Snackbar.make(findViewById(R.id.coordinador),
                        mensaje, Snackbar.LENGTH_LONG).show();
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registrar receptor
        IntentFilter filtroSync = new IntentFilter(Intent.ACTION_SYNC);
        LocalBroadcastManager.getInstance(this).registerReceiver(receptorSync, filtroSync);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Desregistrar receptor
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receptorSync);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_contactos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.accion_sync) {
            if (UWeb.hayConexion(this)) {
                mostrarProgreso(true);
                sincronizar();
            } else {
                Snackbar.make(findViewById(R.id.coordinador),
                        "No hay conexion disponible. La sincronización queda pendiente",
                        Snackbar.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void mostrarProgreso(boolean mostrar) {
        findViewById(R.id.barra).setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void prepararToolbar() {
        // Agregar toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.titulo_actividad_actividad_contactos);
    }

    private void prepararFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDetalles(null);
            }
        });
    }

    private void prepararLista() {
        reciclador = (RecyclerView) findViewById(R.id.reciclador);
        layoutManager = new LinearLayoutManager(this);
        adaptador = new AdaptadorContactos(this);

        reciclador.setLayoutManager(layoutManager);
        reciclador.setAdapter(adaptador);
    }

    private void sincronizar() {
        // Verificación para evitar iniciar más de una sync a la vez
        Account cuentaActiva = UCuentas.obtenerCuentaActiva(this);
        if (ContentResolver.isSyncActive(cuentaActiva, Contrato.AUTORIDAD)) {
            Log.d(TAG, "Ignorando sincronización ya que existe una en proceso.");
            return;
        }

        Log.d(TAG, "Solicitando sincronización manual");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(cuentaActiva, Contrato.AUTORIDAD, bundle);
    }


    void mostrarDetalles(Uri uri) {
        Intent intent = new Intent(this, ActividadInsercionContacto.class);
        if (null != uri) {
            intent.putExtra(ActividadInsercionContacto.URI_CONTACTO, uri.toString());
        }
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                Contactos.URI_CONTENIDO,
                null, Contactos.ELIMINADO + "=?", new String[]{"0"}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adaptador.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adaptador.swapCursor(null);
    }

    @Override
    public void onClick(AdaptadorContactos.ViewHolder holder, String idContacto) {
        mostrarDetalles(Contactos.construirUriContacto(idContacto));
    }
}
