package com.cdelgado.mimartillocom;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


public class VentanaAcercaDe extends VentanaBase
{
    private VentanaAcercaDe estaVentana;
    private GestorSesiones gestorSesion;
    private GestorSesiones.TipoUsuario tipoUsuario;

    private TextView txtSoporte_email;
    private Button btnSitioWeb;

    private CharSequence tituloVentana;

    private boolean haySesion;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_acerca_de);

        // Referencia al propio objeto activity
        estaVentana = this;


        // Recuperar la información de si el usuario es un particular o un profesional pasada en el intent, si es que se pasó alguna
        // (si no se indica, se considera que no hay una sesión iniciada)
        Bundle info = this.getIntent().getExtras();

        if ( info!=null && (info.getSerializable("tipo_usuario"))!=null )
        {
            tipoUsuario = (GestorSesiones.TipoUsuario) info.getSerializable("tipo_usuario");
            gestorSesion = new GestorSesiones(this, tipoUsuario);

            haySesion = true;
        }
        // Si no se pasó el parámetro "tipo_usuario", entonces no se mostrará la opción de cerrar sesión
        else
            haySesion = false;

        // Referencia a los elementos de la interfaz
        txtSoporte_email = (TextView) findViewById(R.id.txtSoporte_email);
        btnSitioWeb = (Button) findViewById(R.id.btnSitioWeb);

        // Configuración de la Action Bar de esta ventana: título, color de fondo y visibilidad
        tituloVentana = getResources().getString(R.string.VentanaAcercaDe_txt_titulo);
        setTitle(tituloVentana);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();

        // Mostrar el icono de volver hacia atrás en la action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // El texto del email de soporte aparece subrayado
        txtSoporte_email.setPaintFlags(txtSoporte_email.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Comportamiento al pulsar sobre el texto del email de soporte
        txtSoporte_email.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    String destinatario = ((TextView) arg0).getText().toString().trim();
                    String asunto = getResources().getString(R.string.VentanaAcercaDe_txt_asuntoEmail);
                    asunto += " (V:" + getResources().getString(R.string.version_num) + ", B:" + getResources().getString(R.string.version_build) + ", F:" + getResources().getString(R.string.version_fecha) + ")";

                    ArrayList<String> parametrosLlamadaExterna = new ArrayList();
                    parametrosLlamadaExterna.add(destinatario);
                    parametrosLlamadaExterna.add(asunto);

                    Utils.aplicacionExterna(estaVentana, Utils.AppExterna.CORREO, parametrosLlamadaExterna);
                }
            }
        );

        // Al pulsar sobre el botón del sitio web, abrir la aplicación del navegador
        btnSitioWeb.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    String url = getResources().getString(R.string.sitio_web);

                    ArrayList<String> parametrosLlamadaExterna = new ArrayList();
                    parametrosLlamadaExterna.add(url);

                    Utils.aplicacionExterna(estaVentana, Utils.AppExterna.NAVEGADOR, parametrosLlamadaExterna);
                }
            }
        );

    }


    // Procesar la respuesta del servicio web
    // (no se llama a ningún servicio web desde esta ventana)
    @Override
    public void procesarResultado(TareaSegundoPlano tarea)
    {
    }


    // Fija el texto y el color de texto para el título de la Action Bar
    @Override
    public void setTitle(CharSequence titulo)
    {
        String color = Integer.toHexString(getResources().getColor(R.color.texto_actionBar) & 0x00ffffff);
        tituloVentana = titulo;

        getSupportActionBar().setTitle( Html.fromHtml("<font color='" + color + "'>" + tituloVentana + "</font>") );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Solo se mostrará el menú de la ventana (con la opción de cerrar sesión) si hay una sesión abierta
        if ( haySesion)
            getMenuInflater().inflate(R.menu.menu_ventana_acerca_de, menu);

        return true;
    }

    // Opciones del menú de la Action Bar de esta ventana
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // Botón home de la action bar (volver atras)
            case android.R.id.home:
                onBackPressed();
                return true;

            // Opción de cerrar la sesión
            case R.id.menu_cerrar_sesion:

                if ( gestorSesion.hayDatosSesion() )
                {
                    // Eliminar los datos de la sesion
                    gestorSesion.destruirSesion();
                    String msgLogOut = getResources().getString( R.string.general_txt_msgCierreSesion );
                    Utils.mostrarMensaje(estaVentana, msgLogOut, Utils.TipoMensaje.TOAST, null, null);

                    // Ir a la activity de inicio (eliminando el historial de ventanas abiertas anteriormente)
                    Intent intent = new Intent(estaVentana, VentanaInicio.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(intent);
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
