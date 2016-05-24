package com.cdelgado.mimartillocom;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;


public class VentanaInicio extends VentanaBase
{
    private GestorSesiones gestorSesion;

    // Referencia a los controles de la interfaz de esta activity
    private ImageView imgAyuda;
    private Button btnParticulares;
    private Button btnProfesionales;
    private Button btnProblemasConexion;

    protected ProgressDialog pDialog;
    private VentanaInicio estaVentana;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_inicio);


        // Referencia al propio objeto activity
        estaVentana = this;

        // Cuadro de progreso (para mostrar durante las consultas por red)
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Obtener la referencia a los controles al crear el objeto
        imgAyuda = (ImageView)findViewById(R.id.imgAyuda);
        btnParticulares = (Button)findViewById(R.id.btnAccesoParticulares);
        btnProfesionales = (Button)findViewById(R.id.btnAccesoProfesionales);
        btnProblemasConexion = (Button)findViewById(R.id.btnProblemasConexion);


        // Datos pasados en el intent
        Bundle info = this.getIntent().getExtras();

        // Recuperar la información de si debemos redirigir a la ventana de Login
        // (por defecto, no)
        boolean redirigir = false;

        // Solo iremos a la ventana de Login si ir_a_ventana_login es true y si se indicó el tipo de usuario
        if  ( info!=null && info.getBoolean("ir_a_ventana_login") && info.getSerializable("tipo_usuario")!=null )
            redirigir = true;

        // Si hay que ir a la ventana de Login, recuperamos el resto de parámetros para invocar dicha ventana
        if ( redirigir )
        {
            GestorSesiones.TipoUsuario tipoUsuario = (GestorSesiones.TipoUsuario) info.getSerializable("tipo_usuario");
            String msg = info.getString("motivo");
            String email = info.getString("emailSugerido");

            Intent intent = new Intent(estaVentana, VentanaLogin.class);

            Bundle bundle = new Bundle();
            bundle.putSerializable("tipo_usuario", tipoUsuario);
            if ( msg != null )      bundle.putString("motivo", msg);
            if ( email != null )    bundle.putString("emailSugerido", email);

            intent.putExtras(info);
            startActivity(intent);
        }


        // Comportamiento del botón de ayuda al pulsarlo
        imgAyuda.setOnClickListener
        (
            new android.view.View.OnClickListener()
            {
                public void onClick(View v)
                {
                    // Mostrar la ventana de Acerca De...
                    Intent intent = new Intent(estaVentana, VentanaAcercaDe.class);
                    startActivity(intent);
                }
            }
        );


        // Comportamiento del botón de Acceso Particulares al pulsarlo
        btnParticulares.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Gestor de sesión de usuario particular
                    gestorSesion = new GestorSesiones(estaVentana, GestorSesiones.TipoUsuario.PARTICULAR);

                    // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
                    if ( !gestorSesion.hayDatosSesion() )
                    {
                        Intent intent = new Intent(VentanaInicio.this, VentanaLogin.class);

                        Bundle b = new Bundle();
                        b.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PARTICULAR);
                        intent.putExtras(b);

                        startActivity(intent);
                    }

                    // Si hay credenciales almacenadas, intentar validarlas
                    else
                    {
                        // Obtener credenciales almacenadas (si las hay)
                        HashMap<String, String> datosSesion = gestorSesion.getDatosSesion();
                        String id_usuario = datosSesion.get(gestorSesion.KEY_USUARIO);
                        String id_sesion = datosSesion.get(gestorSesion.KEY_SESION);


                        // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                        if ( !Utils.dispositivoConectado(cm) )
                        {
                            String titulo = getResources().getString( R.string.VentanaInicio_txt_msgSinConexion_titulo);
                            String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
                        }

                        // Si el dispositivo está conectado a la red,
                        // conectar con el servidor para intentar iniciar sesión con esas credenciales.
                        else
                        {
                            // Tipo de sesión que queremos iniciar
                            String tipo_usuario = "particular";

                            // Construir el servicio web con los parámetros de entrada (guardar como parámetro local el tipo de sesión)
                            ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.CHECK_SESSION, estaVentana, tipo_usuario);

                            miServicioWeb.addParam("usuario",id_usuario);
                            miServicioWeb.addParam("sesion",id_sesion);
                            miServicioWeb.addParam("tipo_usuario",tipo_usuario);


                            // Construir el cliente para la consulta Http(s)
                            ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                            // Crear una tarea asíncrona para conectar con el servicio web
                            TareaSegundoPlano tareaValidarSesion = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                            // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                            Log.d("VentanaInicio","Validando sesión en segundo plano...");
                            tareaValidarSesion.execute();
                        }
                    }

                }
            }
        );


        // Comportamiento del botón de Acceso Profesionales al pulsarlo
        btnProfesionales.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Gestor de sesión de usuario profesional
                    gestorSesion = new GestorSesiones(estaVentana, GestorSesiones.TipoUsuario.PROFESIONAL);

                    // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
                    if ( !gestorSesion.hayDatosSesion() )
                    {
                        Intent intent = new Intent(VentanaInicio.this, VentanaLogin.class);

                        Bundle b = new Bundle();
                        b.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
                        intent.putExtras(b);

                        startActivity(intent);
                    }

                    // Si hay credenciales almacenadas, intentar validarlas
                    else
                    {
                        // Obtener credenciales almacenadas (si las hay)
                        HashMap<String, String> datosSesion = gestorSesion.getDatosSesion();
                        String id_usuario = datosSesion.get(gestorSesion.KEY_USUARIO);
                        String id_sesion = datosSesion.get(gestorSesion.KEY_SESION);


                        // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                        if ( !Utils.dispositivoConectado(cm) )
                        {
                            String titulo = getResources().getString( R.string.VentanaInicio_txt_msgSinConexion_titulo);
                            String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
                        }

                        // Si el dispositivo está conectado a la red,
                        // conectar con el servidor para intentar iniciar sesión con esas credenciales.
                        else
                        {
                            // Tipo de sesión que queremos iniciar
                            String tipo_usuario = "profesional";

                            // Construir el servicio web con los parámetros de entrada (guardar como parámetro local el tipo de sesión)
                            ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.CHECK_SESSION, estaVentana, tipo_usuario);

                            miServicioWeb.addParam("usuario",id_usuario);
                            miServicioWeb.addParam("sesion",id_sesion);
                            miServicioWeb.addParam("tipo_usuario",tipo_usuario);


                            // Construir el cliente para la consulta Http(s)
                            ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                            // Crear una tarea asíncrona para conectar con el servicio web
                            TareaSegundoPlano tareaValidarSesion = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                            // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                            Log.d("VentanaInicio","Validando sesión en segundo plano...");
                            tareaValidarSesion.execute();
                        }
                    }
                }
            }
        );


        // Comportamiento del botón de problemas de conexión al pulsarlo
        btnProblemasConexion.setOnClickListener
        (
            new android.view.View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Dialogo_ProtocoloConexion dialogo = new Dialogo_ProtocoloConexion(estaVentana);
                    dialogo.show();
                }
            }
        );

    }


    // Procesar la respuesta del servicio web
    // (se invoca cuando la tarea en segundo plano ha finalizado)
    @Override
    public void procesarResultado(TareaSegundoPlano tarea)
    {
        GestorSesiones.TipoUsuario tipoUsuario;

        // Servicio web asociado a la tarea, que contiene la respuesta del servidor
        ServicioWeb miServicioWeb = tarea.getCliente().getServicioWeb();

        // Recuperar el tipo de usuario del parámetro local almacenado en la petición web
        String tipo_usuario = (String) miServicioWeb.getParametroLocal();

        if (tipo_usuario.equals("particular") )
            tipoUsuario = GestorSesiones.TipoUsuario.PARTICULAR;
        else
            tipoUsuario = GestorSesiones.TipoUsuario.PROFESIONAL;

        // Título del cuadro de diálogo de error (por si hubo errores en la tarea)
        // y email del usuario (por si hubo problemas con la sesión y hay que volver a la ventana de Login)
        int idTituloOperacion = tarea.getIdTituloOperacion();
        String email = gestorSesion.getDatosSesion().get(gestorSesion.KEY_EMAIL);


        // Lista de posibles respuestas de error que puede recibir esta ventana y el tratamiento que debe darse a cada una
        // (sin incluir las respuestas de sesión expirada, sesión inválida o usuario deshabilitado, que ya se tratan por defecto)
        ArrayList<ErrorServicioWeb> listaErrores = new ArrayList();

        // Posibles errores del servicio CHECK_SESSION a los que se desea dar un tratamiento particular
        // (ninguno --> todos las posibles respuestas de error de este servicio ya se tratan por defecto, ya que son relativas a la validez de la sesión almacenada en el dispositivo)

        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana, tarea, idTituloOperacion, email, listaErrores, tipoUsuario);

        // Si la respuesta se recibió correctamente (OK),
        // ir a la ventana principal del usuario correspondiente (particular o profesional)
        if ( respuesta_OK )
        {
            if ( tipoUsuario == GestorSesiones.TipoUsuario.PARTICULAR )
            {
                Intent intent = new Intent(estaVentana, VentanaParticular.class);
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(estaVentana, VentanaProfesional.class);
                startActivity(intent);
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ventana_inicio, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
