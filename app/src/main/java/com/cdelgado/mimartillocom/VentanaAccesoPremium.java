package com.cdelgado.mimartillocom;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class VentanaAccesoPremium extends VentanaBase
{
    private VentanaAccesoPremium estaVentana;
    private GestorSesiones gestorSesion;
    private GestorSesiones.TipoUsuario tipoUsuario;

    private boolean accesoPremium;

    private TextView txtEstado;
    private Button btnAccesoPremium;

    private CharSequence tituloVentana;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_acceso_premium);

        // Referencia al propio objeto activity
        estaVentana = this;

        // Gestor de sesión del usuario (profesional)
        gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PROFESIONAL);

        // Referencia a los elementos de la interfaz
        txtEstado = (TextView) findViewById(R.id.txtEstado);
        btnAccesoPremium = (Button) findViewById(R.id.btnAccesoPremium);

        // Configuración de la Action Bar de esta ventana: título, color de fondo y visibilidad
        tituloVentana = getResources().getString(R.string.VentanaAccesoPremium_txt_titulo);
        setTitle(tituloVentana);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();

        // Mostrar el icono de volver hacia atrás en la action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Por defecto, asignamos a accesoPremium el valor false y deshabilitamos el boton para modificar el acceso Premium
        // (hasta que el servidor indique cuál es el estado actual del usuario)
        accesoPremium = false;
        btnAccesoPremium.setEnabled(false);


        // Solicitamos al servidor informacion acerca del acceso Premium del usuario
        peticion_accesoPremium("consultar");


        // Comportamiento al pulsar sobre el botón para modificar el acceso Premium
        btnAccesoPremium.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    String accion;

                    if ( accesoPremium )    accion = "desactivar";
                    else                    accion = "activar";

                    peticion_accesoPremium(accion);
                }
            }
        );

    }


    // Llamar al servicio web que consulta/modifica el estado actual del acceso Premium del profesional
    // (NOTA: este método solo lanza la petición por red, el procesamiento de la respuesta recibida se realiza en procesarResultado)
    // (método de clase privado)
    private void peticion_accesoPremium(String accionArealizar)
    {
        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(estaVentana, VentanaLogin.class);

            Bundle info = new Bundle();
            info.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
            intent.putExtras(info);

            finish();
            startActivity(intent);
        }

        // Si hay credenciales almacenadas, se enviarán junto a la petición
        else
        {
            // Obtener credenciales almacenadas (si las hay)
            HashMap<String, String> datosSesion = gestorSesion.getDatosSesion();
            String id_usuario = datosSesion.get(gestorSesion.KEY_USUARIO);
            String id_sesion = datosSesion.get(gestorSesion.KEY_SESION);

            // Resto de parámetros de la petición (acción a realizar)
            String accion = accionArealizar;


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_PREMIUM_ACCESS);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada a enviar por red
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.PREMIUM_ACCESS, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("accion",accion);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                // (esta tarea no bloqueará el thread principal ni mostrará mensajes mientras dura la operación)
                TareaSegundoPlano tareaModificarAccesoPremium = new TareaSegundoPlano(miClienteWeb,estaVentana);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaAccesoPremium", "Intentando " + accion + "...");
                tareaModificarAccesoPremium.execute();
            }
        }
    }


    // Procesar la respuesta del servicio web
    @Override
    public void procesarResultado(TareaSegundoPlano tarea)
    {
        // Título del cuadro de diálogo de error (por si hubo errores en la tarea)
        // y email del usuario (por si hubo problemas con la sesión y hay que volver a la ventana de Login)
        int idTituloOperacion = tarea.getIdTituloOperacion();
        String email = gestorSesion.getDatosSesion().get(gestorSesion.KEY_EMAIL);


        // Lista de posibles respuestas de error que puede recibir esta ventana y el tratamiento que debe dárse a cada una
        // (sin incluir las respuestas de sesion expirada, sesión inválida o usuario deshabilitado, que ya se tratan por defecto)
        ArrayList<ErrorServicioWeb> listaErrores = new ArrayList();

        // Posibles errores del servicio PREMIUM_ACCESS a los que se desea dar un tratamiento particular
        // ...


        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana, tarea, idTituloOperacion, email, listaErrores, tipoUsuario);

        // Si la respuesta se recibió correctamente (OK), discriminar a qué servicio web corresponde
        // (de los posibles que se pueden consultar desde esta ventana) y procesarla por separado
        if ( respuesta_OK  )
        {
            // Servicio web asociado a la tarea, que contiene la respuesta del servidor
            ServicioWeb miServicioWeb = tarea.getCliente().getServicioWeb();

            if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.PREMIUM_ACCESS )
            {
                procesarPremiumAccess( miServicioWeb.getRespuesta() );
            }
        }
    }


    // Procesamiento de la respuesta OK al servicio PREMIUM_ACCESS
    public void procesarPremiumAccess(RespuestaServicioWeb respuesta)
    {
        // Determinar a qué acción corresponde la respuesta
        String detalle = respuesta.getDetalle();
        ArrayList<ContenidoServicioWeb> contenido = respuesta.getContenido();


        if ( detalle.equals("OK_Consultado") )
        {
            // Obtener el estado actual del acceso premium del profesional,
            // y adecuar el texto de estado y del botón a dicha respuesta
            Info_AccesoPremium acceso = (Info_AccesoPremium) contenido.get(0);

            actualizarInfoVentana( acceso.esPremium() );
        }

        else if ( detalle.equals("OK_Activado") )
        {
            actualizarInfoVentana( true );

            String tit = getResources().getString(R.string.VentanaAccesoPremium_txt_titulo);
            String msg = getResources().getString(R.string.msg_AccesoPremium_OK_Activado);
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.EXITO, tit);
        }

        else if ( detalle.equals("OK_Desactivado") )
        {
            actualizarInfoVentana( false );

            String tit = getResources().getString(R.string.VentanaAccesoPremium_txt_titulo);
            String msg = getResources().getString(R.string.msg_AccesoPremium_OK_Desactivado);
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, tit);
        }

        else if ( detalle.equals("OK_PromocionFinalizada") )
        {
            String tit = getResources().getString(R.string.VentanaAccesoPremium_txt_titulo);
            String msg = getResources().getString(R.string.msg_AccesoPremium_OK_PromocionFinalizada);
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ERROR, tit);
        }

    }


    // Actualiza la información mostrada en la ventana de acuerdo al parametro indicado
    private void actualizarInfoVentana(boolean accesoPremium_activado)
    {
        if ( accesoPremium_activado )
        {
            accesoPremium = true;

            txtEstado.setText( getResources().getString(R.string.VentanaAccesoPremium_txt_estadoActivado) );
            txtEstado.setTextColor(getResources().getColor(R.color.verde));

            btnAccesoPremium.setText( getResources().getString(R.string.VentanaAccesoPremium_txt_btnDesactivar) );
            btnAccesoPremium.setEnabled(true);
        }

        else
        {
            accesoPremium = false;

            txtEstado.setText( getResources().getString(R.string.VentanaAccesoPremium_txt_estadoDesactivado) );
            txtEstado.setTextColor(getResources().getColor(R.color.rojo));

            btnAccesoPremium.setText(getResources().getString(R.string.VentanaAccesoPremium_txt_btnActivar));
            btnAccesoPremium.setEnabled(true);
        }
    }


    // Fija el texto y el color de texto para el título de la Action Bar
    @Override
    public void setTitle(CharSequence titulo)
    {
        String color = Integer.toHexString(getResources().getColor(R.color.texto_actionBar) & 0x00ffffff);
        tituloVentana = titulo;

        getSupportActionBar().setTitle(Html.fromHtml("<font color='" + color + "'>" + tituloVentana + "</font>"));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
