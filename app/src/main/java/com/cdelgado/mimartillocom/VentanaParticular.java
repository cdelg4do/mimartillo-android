package com.cdelgado.mimartillocom;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.AdapterView;
import android.view.Menu;
import android.view.MenuItem;
import android.content.res.Configuration;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import im.delight.android.location.SimpleLocation;


public class VentanaParticular extends VentanaBase
{
    // Acciones que se realizan en esta ventana
    // (sin invocar a otras ventanas)
    public static enum Accion
    {
        OBRAS_ABIERTAS,
        OBRAS_CERRADAS,
        FAVORITOS,
        BUSCAR_PROFESIONAL,
    }


    private String[] imagenesDesplegable;
    private String[] opcionesDesplegable;
    private DrawerLayout contenedorPrincipal;
    private ListView contenedorDesplegable;
    private CharSequence tituloVentana;
    private ActionBarDrawerToggle mDrawerToggle;

    private ListView listaItems;

    private boolean hayDatosParaMostrar;  // Indica si la lista de items contiene algún elemento (obras/favoritos)

    private Menu menuActionBar;

    private Accion accionActual;
    private GestorSesiones gestorSesion;
    private SimpleLocation gestorUbicacion;

    protected ProgressDialog pDialog;
    private VentanaParticular estaVentana;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_particular);


        // Referencia al propio objeto activity
        estaVentana = this;

        // Cuadro de progreso (para mostrar durante las consultas por red)
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // Referencia a los elementos de la ventana
        listaItems = (ListView) findViewById(R.id.lista_items);
        contenedorPrincipal = (DrawerLayout) findViewById(R.id.drawer_layout);
        contenedorDesplegable = (ListView) findViewById(R.id.menu_desplegable);


        // Recuperar la información pasada en el intent, si es que se pasó alguna
        Bundle info = this.getIntent().getExtras();

        if ( info!=null )
        {
            // Indicar si hubo errores al cargar el avatar del usuario
            if ( info.getBoolean("fallo_avatar") )
            {
                String titulo = getResources().getString( R.string.titulo_Login_OK_FalloAvatar );
                String msg = getResources().getString( R.string.msg_Login_OK_FalloAvatar );

                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }
        }


        // Gestor de sesion de usuario particular
        GestorSesiones gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PARTICULAR);

        // Construir una nueva instancia de SimpleLocation
        gestorUbicacion = new SimpleLocation(this);

        // Si el servicio de ubicación está desactivado,
        // mostrar un diálogo al usuario para que lo active
        if ( !gestorUbicacion.hasLocationEnabled() )
        {
            String titulo = getResources().getString(R.string.general_txt_msgUbicacionDesactivada_titulo);
            String txt = getResources().getString(R.string.general_txt_msgUbicacionDesactivada);

            // Pedir al usuario que active el acceso a la ubicación
            Utils.dialogo_AvisoUbicacion(estaVentana,titulo,txt, Utils.CategoriaDialogo.ADVERTENCIA);
        }

        // Configuración de la Action Bar de esta ventana: título, color de fondo y visibilidad
        ////tituloVentana = "Bienvenid@, " + gestorSesion.getDatosSesion().get(gestorSesion.KEY_NOMBRE)
        tituloVentana = getResources().getString( R.string.VentanaParticular_txt_titulo_obrasAbiertas );
        setTitle(tituloVentana);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();


        // Imágenes y textos para las opciones del menú desplegable
        imagenesDesplegable = getResources().getStringArray(R.array.opcionesDesplegable_Particular_iconos);
        opcionesDesplegable = getResources().getStringArray(R.array.opcionesDesplegable_Particular_textos);

        Adapter_MenuDeslizante miAdapter = new Adapter_MenuDeslizante(estaVentana, GestorSesiones.TipoUsuario.PARTICULAR, R.layout.elemento_perfil_particular, R.layout.elemento_desplegable, imagenesDesplegable, opcionesDesplegable);
        contenedorDesplegable.setAdapter(miAdapter);


        // Listener para cuando se pulse en la lista del cajón desplegable
        contenedorDesplegable.setOnItemClickListener(new DrawerItemClickListener());

        // Comportamiento del desplegable
        mDrawerToggle = new ActionBarDrawerToggle(this,contenedorPrincipal,R.drawable.ic_drawer4,R.string.general_txt_msgAccesibilidad_drawerOpen,R.string.general_txt_msgAccesibilidad_drawerClose)
        {
            // Invocado cuando el desplegable se cierra totalmente
            public void onDrawerClosed(View view)
            {
            }

            // Invocado cuando el desplegable se abre totalmente
            public void onDrawerOpened(View drawerView)
            {
            }
        };

        // Fijar el objeto anterior como Listener del desplegable
        contenedorPrincipal.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Activar el boton "home" en la esquina de la Action Bar
        getSupportActionBar().setHomeButtonEnabled(true);


        // Comportamiento al pulsar en la ListView de la ventana
        // Dependiendo del contenido mostrado en la lista, se ejecutará una acción u otra
        listaItems.setOnItemClickListener
                (
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView parent, View v, int pos, long id) {
                                // Si se muestra una lista de las obras (abiertas o cerradas) del usuario,
                                // abrir una nueva ventana para mostrar los datos de la obra seleccionada
                                if (accionActual == Accion.OBRAS_ABIERTAS || accionActual == Accion.OBRAS_CERRADAS) {
                                    String idObra = (String) v.getTag();

                                    if (idObra != null) {
                                        Intent intent = new Intent(VentanaParticular.this, VentanaDatosObra.class);

                                        Bundle info = new Bundle();
                                        info.putString("id_obra", idObra);
                                        info.putSerializable("accion", accionActual);
                                        info.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PARTICULAR);
                                        intent.putExtras(info);

                                        startActivity(intent);
                                    }
                                }

                                // Si se muestra una lista de los profesionales favoritos del usuario,
                                // abrir una nueva ventana para mostrar los datos del profesional seleccionado
                                else if (accionActual == Accion.FAVORITOS) {
                                    String idProfesional = (String) v.getTag();

                                    if (idProfesional != null) {
                                        Intent intent = new Intent(VentanaParticular.this, VentanaDatosProfesional.class);

                                        Bundle info = new Bundle();
                                        info.putString("id_profesional", idProfesional);
                                        info.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PARTICULAR);
                                        intent.putExtras(info);

                                        startActivity(intent);
                                    }
                                }

                            }
                        }
                );


        // Al mantener presionado sobre el ListView, se invocará al método onCreateContextMenu de la ventana
        // para mostrar el menú contextual correspondiente
        registerForContextMenu(listaItems);


        // Inicialmente no se muestra ningún dato (obra/favorito) en el listado de la ventana
        hayDatosParaMostrar = false;


        // Guardaremos una referencia al menú de la ventana cuando se invoque el método onCreateOptionsMenu()
        menuActionBar = null;


        // Por defecto, de entrada se muestran las obras abiertas del usuario
        // (marcamos esa opción en el desplegable y llamamos al servicio que carga los datos)
        contenedorDesplegable.setItemChecked(1, true);

        accionActual = Accion.OBRAS_ABIERTAS;
        cargarCabecerasObras(false);
    }


    // Solicita al servidor las cabeceras de las obras del usuario, para después mostrarlas en el ListView del contenedor principal
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    private void cargarCabecerasObras(boolean obrasCerradas)
    {
        // Gestor de sesión de usuario particular
        gestorSesion = new GestorSesiones(estaVentana, GestorSesiones.TipoUsuario.PARTICULAR);

        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(estaVentana, VentanaLogin.class);
            Bundle info = new Bundle();
            info.putSerializable("tipo_usuario",GestorSesiones.TipoUsuario.PARTICULAR);
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

            // Resto de parámetros de la petición (filtro de obras abiertas/cerradas e idioma del cliente)
            String filtro, idioma, ubicacion;

            idioma = Utils.idiomaAplicacion();

            if ( obrasCerradas==true )  filtro = "cerradas";
            else                        filtro = "abiertas";

            ubicacion  = Utils.ubicacionActual(estaVentana,gestorUbicacion);


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_MY_WORKS);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.MY_WORKS, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("idioma",idioma);
                miServicioWeb.addParam("filtro",filtro);
                miServicioWeb.addParam("ubicacion",ubicacion);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaGetCabecerasObras = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaParticular", "Solicitando info sobre las cabeceras de obras...");
                tareaGetCabecerasObras.execute();
            }
        }
    }



    // Solicita al servidor las cabeceras de los profesionales favoritos del usuario, para después mostrarlas en el ListView del contenedor principal
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    private void cargarCabecerasFavoritos()
    {
        // Gestor de sesión de usuario particular
        gestorSesion = new GestorSesiones(estaVentana, GestorSesiones.TipoUsuario.PARTICULAR);

        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(estaVentana, VentanaLogin.class);
            Bundle info = new Bundle();
            info.putSerializable("tipo_usuario",GestorSesiones.TipoUsuario.PARTICULAR);
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

            // Resto de parámetros de la petición (ubicación del cliente)
            String ubicacion  = Utils.ubicacionActual(estaVentana,gestorUbicacion);


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_MY_FAVORITES);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.MY_FAVORITES, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("ubicacion",ubicacion);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaGetCabecerasFavortos = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaParticular", "Solicitando info sobre favoritos...");
                tareaGetCabecerasFavortos.execute();
            }
        }
    }



    // Solicita al servidor agregar o eliminar un profesional de la lista de favoritos del usuario
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    public void modificarFavorito(String id, boolean agregar, int pos)
    {
        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(VentanaParticular.this, VentanaLogin.class);

            Bundle info = new Bundle();
            info.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PARTICULAR);
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

            // Resto de parámetros de la petición (tipo de acción e id del profesional a agregar/eliminar)
            String accion = "";
            if ( agregar == true )  accion = "agregar";
            else                    accion = "eliminar";

            String id_profesional = id;


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_MODIFY_FAVORITES);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada + un parámetro local (pos) que no se enviará por red
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.MODIFY_FAVORITES, estaVentana, new Integer(pos));

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("accion",accion);
                miServicioWeb.addParam("profesional",id_profesional);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web (sin mensaje de estado)
                TareaSegundoPlano tareaModificarFavoritos = new TareaSegundoPlano(miClienteWeb,estaVentana);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaParticular", accion +" en favoritos al profesional "+ id_profesional + "...");
                tareaModificarFavoritos.execute();
            }
        }
    }



    // Solicita al servidor los datos básicos de los profesionales seguidores de una obra,
    // para luego cargarlos en el spinner del diálogo de cerrar obra
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    private void cargarProfesionalesInteresados(String id_obra)
    {
        // Gestor de sesión de usuario particular
        gestorSesion = new GestorSesiones(estaVentana, GestorSesiones.TipoUsuario.PARTICULAR);

        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(VentanaParticular.this, VentanaLogin.class);
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

            // Resto de parámetros de la petición (id de la obra para la que se consultan los seguidores)
            String idObra = id_obra;


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_WORK_FOLLOWERS);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada + un parámetro local (idObra) que no se enviará por red
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.WORK_FOLLOWERS, estaVentana, idObra);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("obra",idObra);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web (sin cuadro de diálogo)
                TareaSegundoPlano tareaGetInteresados = new TareaSegundoPlano(miClienteWeb,estaVentana);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaParticular", "Solicitando info sobre los seguidores de la obra...");
                tareaGetInteresados.execute();
            }
        }
    }



    // Envía una petición de cierre de obra al servidor, adjudicándola al profesional correspondiente
    // (si id_adjudicatario es 0, la obra se cerrará sin adjudicar a nadie)
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    private void cerrarObra(String id_obra, String id_profesional)
    {
        // Gestor de sesión de usuario particular
        gestorSesion = new GestorSesiones(estaVentana, GestorSesiones.TipoUsuario.PARTICULAR);

        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(VentanaParticular.this, VentanaLogin.class);
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

            // Resto de parámetros de la petición (id de la obra a cerrar e id del profesional al que se adjudica, o "0" si no se adjudica a nadie)
            String idObra = id_obra;

            String idAdjudicatario;
            if ( id_profesional.equals("0") )
                idAdjudicatario = "00";
            else
                idAdjudicatario = id_profesional;


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_CLOSE_WORK);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.CLOSE_WORK, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("obra",idObra);
                miServicioWeb.addParam("adjudicatario",idAdjudicatario);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaCerrarObra = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaParticular", "Solicitando el cierre de la obra...");
                tareaCerrarObra.execute();
            }
        }
    }



    // Envía una petición de valoración de obra al servidor
    // (si el coste es la cadena vacía, no se enviará este parámetro al servidor)
    // (NOTA: este método solo lanza la petición por red, el procsamiento de la respuesta recibida se realiza en procesarResultado)
    private void valorarObra(String id_obra, String id_adjudicatario, String coste, float valorCalidad, float valorPrecio, String coment)
    {
        // Gestor de sesión de usuario particular
        gestorSesion = new GestorSesiones(estaVentana, GestorSesiones.TipoUsuario.PARTICULAR);

        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(estaVentana, VentanaLogin.class);
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

            // Resto de parámetros de la petición (id de la obra a valorar, id del profesional adjudicatario, valoraciones de calidad y precio, comentario y coste (opcional)
            String idObra = id_obra;
            String idAdjudicatario = id_adjudicatario;
            String vCalidad = new Integer( Math.round(valorCalidad) ).toString();
            String vPrecio = new Integer( Math.round(valorPrecio) ).toString();
            String comentario = coment;

            BigDecimal numCoste = null;
            boolean enviarCoste = true;

            // Convertir el coste introducido en número
            // (si el coste introducido no es correcto, mostrar aviso y salir)
            if ( !coste.equals("") )
            {
                numCoste = Utils.parsearCantidadMonetaria(coste, Utils.idiomaAplicacion());

                if ( numCoste == null )
                {
                    String msg = getResources().getString( R.string.general_txt_valorarObra_msgCosteIncorrecto);
                    Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST,null,null);
                    return;
                }
            }
            else
                enviarCoste = false;



            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_VOTE_WORK);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.VOTE_WORK, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("obra",idObra);
                miServicioWeb.addParam("adjudicatario",idAdjudicatario);
                miServicioWeb.addParam("valorcalidad",vCalidad);
                miServicioWeb.addParam("valorprecio",vPrecio);
                miServicioWeb.addParam("comentario",comentario);

                if ( enviarCoste )
                    miServicioWeb.addParam("coste",numCoste.toString());


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaValidarObra = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de valorar la obra (en un hilo aparte del principal)
                Log.d("VentanaParticular", "Registrando valoración de la obra...");
                tareaValidarObra.execute();
            }
        }
    }



    // Envía una petición de eliminación de obra al servidor
    // (este método debe ser público para que sea llamado desde el cuadro de diálogo de confirmación: Utils.dialogo_EliminarObra)
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    public void eliminarObra(String id_obra)
    {
        // Gestor de sesión de usuario particular
        gestorSesion = new GestorSesiones(estaVentana, GestorSesiones.TipoUsuario.PARTICULAR);

        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(estaVentana, VentanaLogin.class);
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

            // Resto de parámetros de la petición (id de la obra a eliminar)
            String idObra = id_obra;


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_REMOVE_WORK);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.REMOVE_WORK, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("obra",idObra);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaEliminarObra = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de eliminar la obra (en un hilo aparte del principal)
                Log.d("VentanaParticular", "Solicitando eliminación de la obra...");
                tareaEliminarObra.execute();
            }
        }
    }



    // Procesar la respuesta del servicio web
    // (se invoca cuando la tarea en segundo plano ha finalizado)
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

        // Posibles errores del servicio MY_WORKS a los que se desea dar un tratamiento particular
        // ...

        // Posibles errores del servicio MY_FAVORITES a los que se desea dar un tratamiento particular
        // ...

        // Posibles errores del servicio MODIFY_FAVORITES a los que se desea dar un tratamiento particular
        // ...

        // Posibles errores del servicio WORK_FOLLOWERS a los que se desea dar un tratamiento particular
        // ...

        // Posibles errores del servicio CLOSE_WORK a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.CLOSE_WORK, "ERR_ObraInexistente", false, R.string.msg_CerrarObra_ERR_ObraInexistente, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.CLOSE_WORK, "ERR_ObraYaCerrada", false, R.string.msg_CerrarObra_ERR_ObraYaCerrada, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.CLOSE_WORK, "ERR_UsuarioNoAutorizado", false, R.string.msg_CerrarObra_ERR_UsuarioNoAutorizado, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.CLOSE_WORK, "ERR_AdjudicatarioInexistente", false, R.string.msg_CerrarObra_ERR_AdjudicatarioInexistente, idTituloOperacion) );

        // Posibles errores del servicio VOTE_WORK a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.VOTE_WORK, "ERR_OperacionDenegada",false, R.string.msg_ValorarObra_ERR_OperacionDenegada, idTituloOperacion) );

        // Posibles errores del servicio REMOVE_WORK a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.REMOVE_WORK, "ERR_ObraInexistente", false, R.string.msg_EliminarObra_ERR_ObraInexistente, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.REMOVE_WORK, "ERR_OperacionNoAutorizada",false, R.string.msg_EliminarObra_ERR_OperacionNoAutorizada, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.REMOVE_WORK, "ERR_OperacionCancelada",false, R.string.msg_EliminarObra_ERR_OperacionCancelada, idTituloOperacion) );


        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana, tarea, idTituloOperacion, email, listaErrores, GestorSesiones.TipoUsuario.PARTICULAR);

        // Si la respuesta se recibió correctamente (OK), discriminar a qué servicio web corresponde
        // (de los posibles que se pueden consultar desde esta ventana) y procesarla por separado
        if ( respuesta_OK  )
        {
            // Servicio web asociado a la tarea, que contiene la respuesta del servidor
            ServicioWeb miServicioWeb = tarea.getCliente().getServicioWeb();

            if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.MY_WORKS )
            {
                procesarMisObras(miServicioWeb.getRespuesta());
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.MY_FAVORITES )
            {
                procesarMisFavoritos(miServicioWeb.getRespuesta());
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.MODIFY_FAVORITES )
            {
                procesarModificarFavoritos( miServicioWeb.getRespuesta() , ((Integer)miServicioWeb.getParametroLocal()).intValue() );
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.WORK_FOLLOWERS)
            {
                procesarSeguidoresObra(miServicioWeb.getRespuesta() , (String)miServicioWeb.getParametroLocal() );
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.CLOSE_WORK)
            {
                procesarCierreObra();
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.VOTE_WORK )
            {
                procesarValoracionObra();
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.REMOVE_WORK )
            {
                procesarBorradoObra();
            }
        }
    }


    // Procesamiento de la respuesta OK al servicio MY_WORKS
    public void procesarMisObras(RespuestaServicioWeb respuesta)
    {
        // Determinar la cantidad de cabeceras de obra que devolvió la consulta
        ArrayList<ContenidoServicioWeb> contenido = respuesta.getContenido();
        int tam = contenido.size();

        // Dependiendo de si se consultó el listado de obras abiertas o cerradas cambiará el título de la ActionBar
        String detalle = respuesta.getDetalle();

        String titulo;
        if ( detalle.equals("OK_ObrasAbiertas") )   titulo = getResources().getString( R.string.VentanaParticular_txt_titulo_obrasAbiertas );
        else                                        titulo = getResources().getString( R.string.VentanaParticular_txt_titulo_obrasCerradas );
        setTitle(titulo + " (" + Integer.toString(tam) + ")");


        // Si no hay datos de obras, mostrar un único elemento con un mensaje usando un ArrayAdapter de Strings
        // (se sobreescribe el método getView del ArrayAdapter genérico para poder especificar un color del texto concreto)
        if (tam == 0)
        {
            String[] titulosObras;
            String msg;

            if (detalle.equals("OK_ObrasAbiertas")) msg = getResources().getString( R.string.msg_MisObras_OK_ObrasAbiertas );
            else                                    msg = getResources().getString( R.string.msg_MisObras_OK_ObrasCerradas );

            titulosObras = new String[1];
            titulosObras[0] = msg;

            ArrayAdapter miAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titulosObras)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTextColor( estaVentana.getResources().getColor(R.color.texto_tabs) );

                    return textView;
                }
            };

            listaItems.setAdapter(miAdapter);

            // Deshabilitar el click sobre el ListView
            listaItems.setSelector(android.R.color.transparent);

            // Actualizar el menú de la ventana (para omitir las opciones de ordenar los resultados)
            hayDatosParaMostrar = false;
            onCreateOptionsMenu(menuActionBar);
        }

        // Si hay al menos una cabecera de obra, mostrar sus datos usando un Adapter personalizado de Info_CabeceraObra
        else
        {
            boolean obrasCerradas = false;

            if ( detalle.equals("OK_ObrasCerradas") )
                obrasCerradas = true;

            Adapter_CabeceraObra miAdapter = new Adapter_CabeceraObra(estaVentana,R.layout.elemento_cabecera_obra, contenido, obrasCerradas, GestorSesiones.TipoUsuario.PARTICULAR, true);
            listaItems.setAdapter(miAdapter);

            // Actualizar el menú de la ventana (para incluir las opciones de ordenar los resultados)
            hayDatosParaMostrar = true;
            onCreateOptionsMenu(menuActionBar);
        }
    }


    // Procesamiento de la respuesta OK al servicio MY_FAVORITES
    public void procesarMisFavoritos(RespuestaServicioWeb respuesta)
    {
        // Determinar la cantidad de cabeceras de favoritos que devolvió la consulta
        ArrayList<ContenidoServicioWeb> contenido = respuesta.getContenido();
        int tam = contenido.size();

        // Establecer el título de la ventana
        String titulo = getResources().getString( R.string.VentanaParticular_txt_titulo_favoritos );
        setTitle(titulo + " (" + Integer.toString(tam) + ")");

        // Si no hay datos de favoritos, mostrar un único elemento con un mensaje usando un ArrayAdapter de Strings
        // (se sobreescribe el método getView del ArrayAdapter genérico para poder especificar un color del texto concreto)
        if (tam == 0)
        {
            String[] titulosFavoritos;
            String msg = getResources().getString( R.string.msg_MisFavoritos_OK_ListadoFavoritos );

            titulosFavoritos = new String[1];
            titulosFavoritos[0] = msg;

            ArrayAdapter miAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titulosFavoritos)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTextColor( estaVentana.getResources().getColor(R.color.texto_tabs) );

                    return textView;
                }
            };

            listaItems.setAdapter(miAdapter);

            // Deshabilitar el click sobre el ListView
            listaItems.setSelector(android.R.color.transparent);

            // Actualizar el menú de la ventana (para omitir las opciones de ordenar los resultados)
            hayDatosParaMostrar = false;
            onCreateOptionsMenu(menuActionBar);
        }

        // Si hay al menos una cabecera de favorito, mostrar sus datos usando un Adapter personalizado de Info_CabeceraProfesional
        else
        {
            // El servicio web devuelve una lista de objetos ContenidoServicioWeb (superclase),
            // pero el adapter para cargar la lista de favoritos necesita una lista de objetos Info_CabeceraProfesional (subclase)
            ArrayList<Info_CabeceraProfesional> favoritos = new ArrayList<>();

            for (int i=0; i<tam; i++)
                favoritos.add( (Info_CabeceraProfesional) contenido.get(i) );

            Adapter_CabeceraProfesional miAdapter = new Adapter_CabeceraProfesional(estaVentana, R.layout.elemento_cabecera_profesional, favoritos);
            listaItems.setAdapter(miAdapter);

            // Actualizar el menú de la ventana (para incluir las opciones de ordenar los resultados)
            hayDatosParaMostrar = true;
            onCreateOptionsMenu(menuActionBar);
        }
    }


    // Procesamiento de la respuesta OK al servicio MODIFY_FAVORITES
    private void procesarModificarFavoritos (RespuestaServicioWeb respuesta, int pos)
    {
        // Modificar el valor de la entrada correspondiente en el Adapter de la lista,
        // refrescar la lista (para que actualice el icono de la estrella) y mostrar confirmación de la operación al usuario
        Info_CabeceraProfesional infoPro = null;

        try
        {
            infoPro = (Info_CabeceraProfesional) listaItems.getItemAtPosition(pos);
        }
        catch (ClassCastException ex)
        {
            Log.e("ModificarFavoritos","Error al hacer un cast a Info_CabeceraProfesional desde el adapter (pos: "+ Integer.toString(pos) +")");
            return;
        }

        // Si la petición fue de agregar un profesional a favoritos
        if ( respuesta.getDetalle().equals("OK_FavoritoAgregado") )
        {
            //img.setImageResource(R.drawable.ic_estrella);
            //img.setTag( R.id.TAG_FAVORITES_STATUS , "favorito");
            infoPro.setFavorito(true);
            ((Adapter_CabeceraProfesional) listaItems.getAdapter()).notifyDataSetChanged();

            String msg = getResources().getString(R.string.msg_AgregarFavorito_OK_FavoritoAgregado);
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
        }
        // Si la petición fue de eliminar un profesional de favoritos
        else
        {
            //img.setImageResource(R.drawable.ic_estrellavacia);
            //img.setTag( R.id.TAG_FAVORITES_STATUS , "noEsFavorito");
            infoPro.setFavorito(false);
            ((Adapter_CabeceraProfesional) listaItems.getAdapter()).notifyDataSetChanged();

            String msg = getResources().getString(R.string.msg_EliminarFavorito_OK_FavoritoEliminado);
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
        }
    }


    // Procesamiento de la respuesta OK al servicio WORK_FOLLOWERS
    // (cargar los datos de los seguidores de la obra en el cuadro de diálogo de cierre de obra, y mostrar dicho cuadro)
    private void procesarSeguidoresObra (RespuestaServicioWeb respuesta, String id_obra)
    {
        // Cuadro de dialogo para escoger a qué profesional adjudicar la obra (declaración)
        Dialogo_SeleccionSpinner dialogo_CerrarObra;

        // Título del cuadro de diálogo
        String tituloDialogo = getResources().getString( R.string.titulo_servicio_CLOSE_WORK );

        // Opción por defecto del cuadro de diálogo
        String opcionDefecto = getResources().getString( R.string.VentanaParticular_txt_NoAdjudicarAnadie);


        // Lista con las posibles opciones (los profesionales que siguen esta obra)
        ArrayList<Info_ElementoSpinner> opcionesDialogo = new ArrayList();

        // El servicio web devuelve una lista de objetos ContenidoServicioWeb (superclase), que serán objetos Info_ProfesionalInteresado (subclase)
        // pero el adapter para cargar el spinner del diálogo de cerrar obra necesita una lista de objetos Info_ElementoSpinner (subclase)
        for (int i=0; i<respuesta.getContenido().size(); i++)
        {
            String idPro, nombrePro;
            ContenidoServicioWeb item = respuesta.getContenido().get(i);

            if ( item instanceof Info_ProfesionalInteresado)
            {
                idPro = ((Info_ProfesionalInteresado)item).getId();
                nombrePro = ((Info_ProfesionalInteresado)item).getNombre();

                opcionesDialogo.add( new Info_ElementoSpinner(idPro,nombrePro) );
            }
        }


        // Comportamiento de los botones del cuadro de diálogo (Aceptar y Cancelar) al pulsarlos
        final String idObra = id_obra;

        Dialogo_SeleccionSpinner.Comportamiento comportamientoDialogo = new Dialogo_SeleccionSpinner.Comportamiento()
        {
            public void elementoSeleccionado(int pos, long id, String item)
            {
                //Utils.mostrarMensaje(estaVentana, "Seleccionada Pos: " + Integer.toString(pos) + ", Key: " + Long.toString(id) + ", Item: " + item, Utils.TipoMensaje.TOAST, null, null);

                String id_adjudicatario = Long.toString(id);
                cerrarObra( idObra , id_adjudicatario );

            }

            public void seleccionCancelada()
            {
                String msg = getResources().getString( R.string.dialogo_cerrarObra_txt_msgOperacionCancelada );
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
            }
        };


        // Crear y mostrar el cuadro de diálogo de cierre de obra, con todos los parámetros anteriores
        Dialogo_SeleccionSpinner dialogo = new Dialogo_SeleccionSpinner(estaVentana, tituloDialogo, opcionesDialogo, opcionDefecto, comportamientoDialogo);
        dialogo.show();
    }



    // Procesamiento de la respuesta OK al servicio CLOSE_WORK
    private void procesarCierreObra()
    {
        String msg = getResources().getString(R.string.msg_CerrarObra_OK_ObraCerrada);
        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);

        marcarOpcionDesplegable(Accion.OBRAS_CERRADAS);
        accionItemDesplegable(2);
    }



    // Procesamiento de la respuesta OK al servicio VOTE_WORK
    private void procesarValoracionObra()
    {
        // Mostrar confirmación al usuario
        String msg = getResources().getString(R.string.msg_ValorarObra_OK_ValoracionRealizada);
        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);

        // Volver a cargar el listado de obras cerradas (para actualizar la vista)
        accionItemDesplegable(2);
    }



    // Procesamiento de la respuesta OK al servicio REMOVE_WORK
    private void procesarBorradoObra()
    {
        // Mostrar confirmación al usuario
        String msg = getResources().getString(R.string.msg_EliminarObra_OK_ObraEliminada);
        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);

        // Volver a cargar el listado de obras abiertas/cerradas (para actualizar la vista)
        if ( accionActual==Accion.OBRAS_ABIERTAS )
            accionItemDesplegable(1);
        else
            accionItemDesplegable(2);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Si es la primera vez que se invoca este método, guardamos una referencia al menú de la ventana
        if ( menuActionBar == null )
            menuActionBar = menu;

        // Antes de Menú desplegable de la ventana
        if (menu != null)
            menu.clear();

        if ( hayDatosParaMostrar )
        {
            if ( accionActual == Accion.FAVORITOS )
                getMenuInflater().inflate(R.menu.menu_ventana_particular_con_resultados_favoritos, menu);
            else
                getMenuInflater().inflate(R.menu.menu_ventana_particular_con_resultados_obras, menu);
        }
        else
            getMenuInflater().inflate(R.menu.menu_ventana_particular_sin_resultados, menu);

        return true;
    }


    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        // Para el menú de opciones de la Action Bar (overflow menu),
        // indicar que muestre los iconos correspondientes junto al texto de cada opción del menú
        if(featureId == Window.FEATURE_ACTION_BAR && menu != null)
        {
            if(menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try
                {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                }
                catch(NoSuchMethodException e)
                {
                    Log.e("VentanaParticular", "onMenuOpened ", e);
                }
                catch(Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        return super.onMenuOpened(featureId, menu);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        // Sincronizar el estado del toggle después de onRestoreInstanceState
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        // Actualizar el toggle cuando hay un cambio en el dispositivo
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Al seleccionar un elemento del desplegable, pasar el evento al toggle,
        // si devuelve true, entonces ha manejado el evento de toque del icono de l app
        if ( mDrawerToggle.onOptionsItemSelected(item) )
            return true;

        // Manejo de otros elementos de la action bar
        // (elmentos del menú overflow de la action bar)
        else
        {
            switch (item.getItemId())
            {
                // Opción de actualizar la ventana
                case R.id.menu_actualizar:

                    if ( accionActual == Accion.OBRAS_ABIERTAS )
                        accionItemDesplegable(1);

                    else if ( accionActual == Accion.OBRAS_CERRADAS )
                        accionItemDesplegable(2);

                    else if ( accionActual == Accion.FAVORITOS )
                        accionItemDesplegable(4);

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


                // Opción de ordenar las obras mostradas por título ascendente
                case R.id.menu_ordenar_titulo_asc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraObra )
                    {
                        Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraObra.Comparadores.Criterio.TITULO_ASCENDENTE);
                    }

                    return true;


                // Opción de ordenar las obras mostradas por tipo (por su id) ascendente
                case R.id.menu_ordenar_tipo_asc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraObra )
                    {
                        Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraObra.Comparadores.Criterio.TIPO_ASCENDENTE);
                    }

                    return true;


                // Opción de ordenar las obras mostradas por antiguedad descendente
                case R.id.menu_ordenar_antiguedad_desc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraObra )
                    {
                        Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraObra.Comparadores.Criterio.ANTIGUEDAD_DESCENDENTE);
                    }

                    return true;


                // Opción de ordenar las obras mostradas por fecha de realización ascendente
                case R.id.menu_ordenar_realizacion_asc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraObra )
                    {
                        Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraObra.Comparadores.Criterio.REALIZACION_ASCENDENTE);
                    }

                    return true;


                // Opción de ordenar las obras mostradas por fecha de realización descendente
                case R.id.menu_ordenar_realizacion_desc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraObra )
                    {
                        Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraObra.Comparadores.Criterio.REALIZACION_DESCENDENTE);
                    }

                    return true;


                // Opción de ordenar las obras mostradas por seguidores descendente
                case R.id.menu_ordenar_visitas_desc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraObra )
                    {
                        Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraObra.Comparadores.Criterio.VISITAS_DESCENDENTE);
                    }

                    return true;


                // Opción de ordenar las obras mostradas por distancia ascendente
                case R.id.menu_ordenar_distancia_asc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraObra )
                    {
                        Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraObra.Comparadores.Criterio.DISTANCIA_ASCENDENTE);
                    }

                    return true;


                // Opción de ordenar los profesionales encontrados por nombre ascendente
                case R.id.menu_ordenar_nombre_asc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraProfesional )
                    {
                        Adapter_CabeceraProfesional a = (Adapter_CabeceraProfesional) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraProfesional.Comparadores.Criterio.NOMBRE_ASCENDENTE);
                    }

                    return true;


                // Opción de ordenar los profesionales encontrados por población ascendente
                case R.id.menu_ordenar_poblacion_asc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraProfesional )
                    {
                        Adapter_CabeceraProfesional a = (Adapter_CabeceraProfesional) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraProfesional.Comparadores.Criterio.POBLACION_ASCENDENTE);
                    }

                    return true;


                // Opción de ordenar los profesionales encontrados por provincia ascendente
                case R.id.menu_ordenar_provincia_asc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraProfesional )
                    {
                        Adapter_CabeceraProfesional a = (Adapter_CabeceraProfesional) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraProfesional.Comparadores.Criterio.PROVINCIA_ASCENDENTE);
                    }

                    return true;


                // Opción de ordenar los profesionales encontrados por distancia ascendente
                case R.id.menu_ordenar_distanciaPro_asc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraProfesional )
                    {
                        Adapter_CabeceraProfesional a = (Adapter_CabeceraProfesional) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraProfesional.Comparadores.Criterio.DISTANCIA_ASCENDENTE);
                    }

                    return true;


                // Opción de ordenar los profesionales encontrados por votos descendente
                case R.id.menu_ordenar_votos_desc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraProfesional )
                    {
                        Adapter_CabeceraProfesional a = (Adapter_CabeceraProfesional) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraProfesional.Comparadores.Criterio.VOTOS_DESCENDENTE);
                    }

                    return true;


                // Opción de ordenar los profesionales encontrados por calidad descendente
                case R.id.menu_ordenar_calidad_desc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraProfesional )
                    {
                        Adapter_CabeceraProfesional a = (Adapter_CabeceraProfesional) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraProfesional.Comparadores.Criterio.CALIDAD_DESCENDENTE);
                    }

                    return true;


                // Opción de ordenar los profesionales encontrados por precio descendente
                case R.id.menu_ordenar_precio_desc:

                    if ( listaItems.getAdapter() instanceof Adapter_CabeceraProfesional )
                    {
                        Adapter_CabeceraProfesional a = (Adapter_CabeceraProfesional) listaItems.getAdapter();
                        a.ordenar(Info_CabeceraProfesional.Comparadores.Criterio.PRECIO_DESCENDENTE);
                    }

                    return true;


                default:
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    // Método invocado para mostrar el menú contextual de algún elemento de la página
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        // Si el elemento en cuestión es el ListView que contiene las obras/profesionales favoritos
        if ( v.getId()==R.id.lista_items)
        {
            // En ese caso, el objeto menuInfo será del subtipo AdapterContextMenuInfo
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            // Título y textos de las opciones del menú a mostrar
            // (diferentes opciones dependiendo de si estamos en el listado de obras abiertas, obras cerradas, o favoritos)
            String tituloMenu = "";
            String[] opcionesMenu = null;

            // Obtener una referencia a los datos del objeto de la lista, a traves de su adapter
            // (según los datos que muestre la lista en ese momento, será un objeto Info_CabeceraObra o Info_CabeceraProfesional)
            // Si el listView no contiene elementos (solo un texto indicando que está vacío),
            // entonces al hacer el casting saltará una excepcion que hemos de capturar
            Object datosElementoSeleccionado = listaItems.getAdapter().getItem(info.position);

            try
            {
                if (accionActual == Accion.OBRAS_ABIERTAS)
                {
                    tituloMenu = ((Info_CabeceraObra) datosElementoSeleccionado).getTitulo();
                    opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaParticular_cabeceraObra_abierta);
                }

                if (accionActual == Accion.OBRAS_CERRADAS)
                {
                    tituloMenu = ((Info_CabeceraObra) datosElementoSeleccionado).getTitulo();

                    // Si la obra cerrada tiene la valoración pendiente, se incluirá la opción de valorar en el menú contextual
                    if ( ((Info_CabeceraObra) datosElementoSeleccionado).sinValoracion() )
                        opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaParticular_cabeceraObra_cerrada_necesitaValoracion);
                    else
                        opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaParticular_cabeceraObra_cerrada_noNecesitaValoracion);
                }

                if (accionActual == Accion.FAVORITOS)
                {
                    tituloMenu = ((Info_CabeceraProfesional) datosElementoSeleccionado).getNombre();

                    boolean esFavorito = ((Info_CabeceraProfesional) datosElementoSeleccionado).esFavorito();

                    if (esFavorito)
                        opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaParticular_cabeceraProfesional_esFavorito);
                    else
                        opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaParticular_cabeceraProfesional_noEsFavorito);
                }
            }
            catch (ClassCastException ex)
            {
                opcionesMenu = null;
            }


            if (opcionesMenu != null)
            {
                menu.setHeaderTitle(tituloMenu);

                // Cargar cada una de las opciones en el menú contextual
                for (int i = 0; i < opcionesMenu.length; i++)
                    menu.add(Menu.NONE,i,i,opcionesMenu[i]);
            }
        }

    }


    // Accion a realizar al seleccionar un elemento del menú contexual
    // (al mantener pulsado sobre el listView de la ventana)
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        // Información sobre la acción (índice del menú escogido y posicion del ListView sobre la que se pulsó)
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int indiceMenu = item.getItemId();
        int posLista = info.position;

        String[] opcionesMenu = null;
        String opcionSeleccionada = "";


        // Obtener una referencia a los datos del objeto de la lista, a traves de su adapter
        // (según los datos que muestre la lista en ese momento, será un objeto Info_CabeceraObra o Info_CabeceraProfesional)
        // Si el listView no contiene elementos (solo un texto indicando que está vacío),
        // entonces al hacer el casting saltará una excepcion que hemos de capturar
        Object datosElementoSeleccionado = listaItems.getItemAtPosition(posLista);

        try
        {
            // Si la acción es sobre un elemento de la lista de obras abiertas
            if (accionActual == Accion.OBRAS_ABIERTAS)
            {
                final Info_CabeceraObra infoObra = (Info_CabeceraObra) datosElementoSeleccionado;
                String idObra =  infoObra.getId();

                // Opción de ver detalles de la obra
                if (indiceMenu == 0)
                {
                    Intent intent = new Intent(VentanaParticular.this, VentanaDatosObra.class);

                    Bundle b = new Bundle();
                    b.putString("id_obra", idObra);
                    b.putSerializable("accion", accionActual);
                    b.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PARTICULAR);
                    intent.putExtras(b);

                    startActivity(intent);
                }

                // Opción de cerrar obra
                else if (indiceMenu == 1)
                {
                    // Si la obra no tiene ningún seguidor, mostrar el cuadro de diálogo de cerrar obra
                    // sin opción a adjudicar a ningún profesional
                    if ( infoObra.getInteresados() == 0 )
                    {
                        // Cuadro de dialogo para escoger a qué profesional adjudicar la obra (declaración)
                        Dialogo_SeleccionSpinner dialogo_CerrarObra;

                        // Título del cuádro de diálogo
                        String tituloDialogo = getResources().getString( R.string.dialogo_cerrarObra_txt_titulo );

                        // Opción por defecto del spinner del cuadro de diálogo
                        String opcionDefecto = getResources().getString( R.string.dialogo_cerrarObra_txt_opcionDefecto );


                        // Lista con las posibles opciones (los profesionales que siguen esta obra)
                        // En este caso será una lista vacía, ya que la obra no tiene seguidores
                        ArrayList<Info_ElementoSpinner> opcionesDialogo = new ArrayList();


                        // Comportamiento de los botones del cuadro de diálogo (Aceptar y Cancelar) al pulsarlos
                        Dialogo_SeleccionSpinner.Comportamiento comportamientoDialogo = new Dialogo_SeleccionSpinner.Comportamiento()
                        {
                            public void elementoSeleccionado(int pos, long id, String item)
                            {
                                //Utils.mostrarMensaje(estaVentana, "Seleccionada Pos: " + Integer.toString(pos) + ", Key: " + Long.toString(id) + ", Item: " + item, Utils.TipoMensaje.TOAST, null, null);

                                String id_adjudicatario = Long.toString(id);
                                cerrarObra( infoObra.getId() , id_adjudicatario );
                            }

                            public void seleccionCancelada()
                            {
                                String msg = getResources().getString( R.string.dialogo_cerrarObra_txt_msgOperacionCancelada );
                                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
                            }
                        };


                        // Crear y mostrar el cuadro de diálogo de cierre de obra, con todos los parámetros anteriores
                        Dialogo_SeleccionSpinner dialogo = new Dialogo_SeleccionSpinner(estaVentana, tituloDialogo, opcionesDialogo, opcionDefecto, comportamientoDialogo);
                        dialogo.show();
                    }

                    // Si hay algún profesional interesado,
                    // solicitar sus datos al servidor para luego mostrarlos en el diálogo de cerrar obra
                    else
                        cargarProfesionalesInteresados( infoObra.getId() );
                }

                // Opción de eliminar obra
                else if (indiceMenu == 2)
                {
                    String titulo = getResources().getString(R.string.dialogo_eliminarObra_txt_titulo);
                    String msg = getResources().getString(R.string.dialogo_eliminarObra_txt_pregunta);
                    Utils.dialogo_EliminarObra(estaVentana, titulo, msg, Utils.CategoriaDialogo.ADVERTENCIA, idObra);
                }
            }

            // Si la acción es sobre un elemento de la lista de obras cerradas
            else if (accionActual == Accion.OBRAS_CERRADAS)
            {
                Info_CabeceraObra infoObra = (Info_CabeceraObra) datosElementoSeleccionado;
                String idObra =  infoObra.getId();

                // Opción de ver detalles de la obra
                if (indiceMenu == 0)
                {
                    Intent intent = new Intent(VentanaParticular.this, VentanaDatosObra.class);

                    Bundle b = new Bundle();
                    b.putString("id_obra", idObra);
                    b.putSerializable("accion", accionActual);
                    b.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PARTICULAR);
                    intent.putExtras(b);

                    startActivity(intent);
                }

                // Opción de eliminar la obra
                if (indiceMenu == 1)
                {
                    String titulo = getResources().getString(R.string.dialogo_eliminarObra_txt_titulo);
                    String msg = getResources().getString(R.string.dialogo_eliminarObra_txt_pregunta);

                    Utils.dialogo_EliminarObra(estaVentana, titulo, msg, Utils.CategoriaDialogo.ADVERTENCIA, idObra);
                }

                // Opción de valorar la obra
                if (indiceMenu == 2)
                {
                    String msg = "";

                    // Si la obra no fue adjudicada a ningún seguidor, mostrar un aviso
                    if ( infoObra.getAdjudicatario().equals("0") )
                    {
                        msg = getResources().getString( R.string.dialogo_valorarObra_txt_msgObraNoAsignada );
                        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
                    }

                    // Si ya se hizo una valoración de la obra con anterioridad (no necesita más), mostrar un aviso
                    else if ( !infoObra.sinValoracion() )
                    {
                        msg = getResources().getString( R.string.dialogo_valorarObra_txt_msgObraYaValorada );
                        Utils.mostrarMensaje(estaVentana,msg, Utils.TipoMensaje.TOAST, null, null);
                    }

                    // Si es posible valorar la obra en cuestión, mostrar el cuadro de diálogo de valorar obra
                    else
                    {
                        String idAdjudicatario = infoObra.getAdjudicatario();
                        String tituloObra = infoObra.getTitulo();

                        dialogo_valorarObra(idObra, idAdjudicatario, tituloObra);
                    }

                }
            }


            // Si la acción es sobre un elemento de la lista de profesionales favoritos
            else if (accionActual == Accion.FAVORITOS)
            {
                Info_CabeceraProfesional infoPro = (Info_CabeceraProfesional) datosElementoSeleccionado;
                String idPro =  infoPro.getId();

                // Opción de ver detalles del profesional
                if (indiceMenu == 0)
                {
                    Intent intent = new Intent(VentanaParticular.this, VentanaDatosProfesional.class);

                    Bundle b = new Bundle();
                    b.putString("id_profesional", idPro);
                    b.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PARTICULAR);
                    intent.putExtras(b);

                    startActivity(intent);
                }

                // Opción de eliminar/añadir favorito
                else if (indiceMenu == 1)
                {
                    boolean esFavorito = infoPro.esFavorito();

                    modificarFavorito(idPro, !esFavorito, posLista);
                }
            }
        }
        catch (ClassCastException ex)
        {
        }

        return true;
    }


    // Muestra el cuadro de diálogo para valorar una obra de la lista
    private void dialogo_valorarObra(final String idObra, String idAdjudicatario, String tituloObra)
    {
        // Cuadro de dialogo para valorar la obra (declaración)
        Dialogo_ValorarObra dialogo_valorarObra;


        // Título del cuádro de diálogo
        String tituloDialogo = getResources().getString(R.string.dialogo_valorarObra_txt_titulo);

        // Si la obra no tiene adjudicatario, no se hace nada
        if ( idAdjudicatario.equals("0") )
            return;


        // Declaración de los métodos que invocará el diálogo al pulsar los botones de Aceptar (si todos los datos son correctos) y Cancelar
        Dialogo_ValorarObra.Comportamiento comportamientoDialogo = new Dialogo_ValorarObra.Comportamiento()
        {
            public void dialogoAceptar(String id_adjudicatario, String coste, float valorCalidad, float valorPrecio, String comentario)
            {
                valorarObra(idObra, id_adjudicatario, coste, valorCalidad, valorPrecio, comentario);
            }

            public void dialogoCancelar()
            {
                String msg = getResources().getString( R.string.dialogo_valorarObra_txt_msgOperacionCancelada );
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
            }
        };


        // Crear y mostrar el cuadro de diálogo de cierre de obra, con todos los parámetros anteriores
        // (se mostrará el título de la obra a valorar, como dato informativo al usuario)
        Dialogo_ValorarObra dialogo = new Dialogo_ValorarObra(estaVentana, tituloDialogo, idObra, idAdjudicatario, comportamientoDialogo, tituloObra);
        dialogo.show();
    }


    // Acciones al seleccionar un elemento de la lista del menu desplegable
    private void accionItemDesplegable(int pos)
    {
        // Opción de editar perfil
        if ( pos==0 )
        {
            marcarOpcionDesplegable(accionActual);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);

            // Pasar a la ventana de editar el perfil
            Intent intent = new Intent(VentanaParticular.this, VentanaPerfilParticular.class);
            startActivity(intent);
        }

        // Opción de ver las obras abiertas del usuario
        else if ( pos==1 )
        {
            accionActual = Accion.OBRAS_ABIERTAS;
            cargarCabecerasObras(false);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);
        }

        // Opción de ver las obras cerradas del usuario
        else if ( pos==2 )
        {
            accionActual = Accion.OBRAS_CERRADAS;
            cargarCabecerasObras(true);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);
        }

        // Opción de crear una nueva obra
        else if ( pos==3 )
        {
            marcarOpcionDesplegable(accionActual);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);

            // Pasar a la ventana de nueva obra
            //accionActual = Accion.NUEVA_OBRA;
            Intent intent = new Intent(VentanaParticular.this, VentanaNuevaObra.class);
            startActivity(intent);
        }

        // Opción de consultar la lista de favoritos del usuario
        else if ( pos==4 )
        {
            accionActual = Accion.FAVORITOS;
            cargarCabecerasFavoritos();

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);
        }

        // Opción de buscar profesionales
        else if ( pos==5 )
        {
            marcarOpcionDesplegable(accionActual);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);

            // Pasar a la ventana de búsqueda de profesionales
            Intent intent = new Intent(VentanaParticular.this, VentanaBusquedaProfesional.class);
            startActivity(intent);
        }

        // Opción de Acerca de...
        else if ( pos==6 )
        {
            marcarOpcionDesplegable(accionActual);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);

            // Pasar a la ventana de Acerca De
            Intent intent = new Intent(estaVentana, VentanaAcercaDe.class);

            Bundle info = new Bundle();
            info.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PARTICULAR);
            intent.putExtras(info);

            startActivity(intent);
        }

        else
        {
            marcarOpcionDesplegable(accionActual);

            // No se hace ninguna operación, solo registramos el evento en el log
            Log.e("VentanaParticular", "Opción del drawer ("+Integer.toString(pos) + ") no implementada aún");
        }
    }


    // Por defecto, cuando se pulsa una opción del desplegable, queda marcada
    // Si queremos que se quede marcada otra diferente, se invoca a este método
    // (método de clase privado)
    private void marcarOpcionDesplegable(Accion a)
    {
        if ( a==Accion.OBRAS_ABIERTAS )
            contenedorDesplegable.setItemChecked(1, true);

        if ( a==Accion.OBRAS_CERRADAS )
            contenedorDesplegable.setItemChecked(2, true);

        if ( a==Accion.BUSCAR_PROFESIONAL )
            contenedorDesplegable.setItemChecked(4, true);
    }


    // Fija el texto y el color de texto para el título de la Action Bar
    @Override
    public void setTitle(CharSequence titulo)
    {
        String color = Integer.toHexString(getResources().getColor(R.color.texto_actionBar) & 0x00ffffff);
        tituloVentana = titulo;

        getSupportActionBar().setTitle( Html.fromHtml("<font color='"+color+"'>"+tituloVentana+"</font>") );
    }


    // Clase ausiliar, para definir un Listener de pulsar en un elemento del desplegable
    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int pos, long id)
        {
            accionItemDesplegable(pos);
        }
    }


    @Override
    public void onPause()
    {
        // Interrumpir las actualizaciones de ubicación cuando la ventana entra en estado onPause
        // (para ahorrar energía)
        gestorUbicacion.endUpdates();

        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        // Hacer que el dispositivo actualice su ubicación al pasar a estado onResume
        gestorUbicacion.beginUpdates();
    }

}