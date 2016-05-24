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
import java.util.ArrayList;
import java.util.HashMap;

import im.delight.android.location.SimpleLocation;


public class VentanaProfesional extends VentanaBase
{

    // Acciones que se realizan en esta ventana desde el menú desplegable
    // (sin invocar a otras ventanas hijas)
    public static enum Accion
    {
        OBRAS_ADJUDICADAS,
        OBRAS_COMPLETADAS,
        OBRAS_SEGUIDAS,
        VALORACIONES
    }


    private String[] imagenesDesplegable;
    private String[] opcionesDesplegable;
    private DrawerLayout contenedorPrincipal;
    private ListView contenedorDesplegable;
    private CharSequence tituloVentana;
    private ActionBarDrawerToggle mDrawerToggle;

    private ListView listaItems;

    private boolean hayDatosParaMostrar;  // Indica si la lista de items contiene algún elemento (obras/valoraciones)

    private Menu menuActionBar;

    private Accion accionActual;
    private GestorSesiones gestorSesion;
    private SimpleLocation gestorUbicacion;

    protected ProgressDialog pDialog;
    private VentanaProfesional estaVentana;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_profesional);


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


        // Gestor de sesion de usuario profesional
        GestorSesiones gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PROFESIONAL);

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
        tituloVentana = getResources().getString( R.string.VentanaProfesional_txt_titulo_obrasAdjudicadas );
        setTitle(tituloVentana);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();


        // Imágenes y textos para las opciones del menú desplegable
        imagenesDesplegable = getResources().getStringArray(R.array.opcionesDesplegable_Profesional_iconos);
        opcionesDesplegable = getResources().getStringArray(R.array.opcionesDesplegable_Profesional_textos);

        Adapter_MenuDeslizante miAdapter = new Adapter_MenuDeslizante(estaVentana, GestorSesiones.TipoUsuario.PROFESIONAL, R.layout.elemento_perfil_profesional, R.layout.elemento_desplegable, imagenesDesplegable, opcionesDesplegable);
        contenedorDesplegable.setAdapter(miAdapter);

        // Listener para cuando se pulse en la lista desplegable
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
                            public void onItemClick(AdapterView parent, View v, int pos, long id)
                            {
                                // Si se muestra una lista de las obras (adjudicadas, completadas o en seguimiento) del profesional,
                                // abrir una nueva ventana para mostrar los datos de la obra seleccionada
                                if (accionActual == Accion.OBRAS_ADJUDICADAS || accionActual == Accion.OBRAS_COMPLETADAS || accionActual == Accion.OBRAS_SEGUIDAS) {
                                    String idObra = (String) v.getTag();

                                    if (idObra != null)
                                    {
                                        Intent intent = new Intent(estaVentana, VentanaDatosObra.class);

                                        Bundle info = new Bundle();
                                        info.putString("id_obra", idObra);
                                        info.putSerializable("accion", accionActual);
                                        info.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
                                        intent.putExtras(info);

                                        startActivity(intent);
                                    }
                                }

                                // Si se muestra una lista de las valoraciones del usuario,
                                // abrir una nueva ventana para mostrar los datos de la obra correspondiente
                                else if (accionActual == Accion.VALORACIONES)
                                {
                                    String idObra = (String) v.getTag();

                                    if (idObra != null)
                                    {
                                        Intent intent = new Intent(estaVentana, VentanaDatosObra.class);

                                        Bundle info = new Bundle();
                                        info.putString("id_obra", idObra);
                                        info.putSerializable("accion", Accion.OBRAS_COMPLETADAS);
                                        info.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
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


        // Inicialmente no se muestra ningún dato (obra/valoración) en el listado de la ventana
        hayDatosParaMostrar = false;


        // Guardaremos una referencia al menú de la ventana cuando se invoque el método onCreateOptionsMenu()
        menuActionBar = null;


        // Por defecto, de entrada se muestran las obras adjudicadas (pero no completadas) del profesional
        // (marcamos esa opción en el desplegable y llamamos al servicio que carga los datos)
        contenedorDesplegable.setItemChecked(1, true);

        accionActual = Accion.OBRAS_ADJUDICADAS;
        cargarCabecerasObras(accionActual);
    }


    // Solicita al servidor las cabeceras de las obras del profesional, para después mostrarlas en el ListView del contenedor principal
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    private void cargarCabecerasObras(Accion tipoObras)
    {
        // Gestor de sesión de usuario profesional
        gestorSesion = new GestorSesiones(estaVentana, GestorSesiones.TipoUsuario.PROFESIONAL);

        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(estaVentana, VentanaLogin.class);
            Bundle info = new Bundle();
            info.putSerializable("tipo_usuario",GestorSesiones.TipoUsuario.PROFESIONAL);
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

            // Resto de parámetros de la petición (filtro de obras adjudicadas/completadas/en seguimiento, idioma del cliente y ubicación actual)
            String filtro = "";
            String idioma;
            String ubicacion;

            idioma = Utils.idiomaAplicacion();
            ubicacion = Utils.ubicacionActual(estaVentana,gestorUbicacion);

            switch (tipoObras)
            {
                case OBRAS_ADJUDICADAS: filtro = "adjudicadas";
                                        break;

                case OBRAS_COMPLETADAS: filtro = "completadas";
                                        break;

                case OBRAS_SEGUIDAS:    filtro = "seguidas";
                                        break;

                default:    break;
            }


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_MY_WORKS_PRO);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.MY_WORKS_PRO, estaVentana);

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
                Log.d("VentanaProfesional", "Solicitando info sobre las cabeceras de obras...");
                tareaGetCabecerasObras.execute();
            }
        }
    }



    // Solicita al servidor las valoraciones realizadas por usuarios particulares acerca de este profesional
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    private void cargarValoraciones()
    {
        // Gestor de sesión de usuario particular
        gestorSesion = new GestorSesiones(estaVentana, GestorSesiones.TipoUsuario.PROFESIONAL);

        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(estaVentana, VentanaLogin.class);
            Bundle info = new Bundle();
            info.putSerializable("tipo_usuario",GestorSesiones.TipoUsuario.PROFESIONAL);
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

            // Resto de parámetros de la petición (idioma del cliente y tipo de usuario)
            String idioma = Utils.idiomaAplicacion();
            String tipo_usuario = "profesional";


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_MY_VOTES);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.MY_VOTES, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("idioma",idioma);
                miServicioWeb.addParam("tipo_usuario",tipo_usuario);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaGetCabecerasFavortos = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaProfesional", "Solicitando valoraciones...");
                tareaGetCabecerasFavortos.execute();
            }
        }
    }



    // Solicita al servidor agregar o eliminar un profesional de la lista de seguidores de una obra
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    public void modificarSeguimiento(String id, boolean seguir, int pos)
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

            // Resto de parámetros de la petición (id de la obra a seguir/olvidar)
            String id_obra = id;


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_FOLLOW_WORK);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web correspondiente con los parámetros de entrada + un parámetro local (pos) que no se enviará por red
                ServicioWeb miServicioWeb;

                if ( seguir )
                    miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.FOLLOW_WORK, estaVentana, new Integer(pos));
                else
                    miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.UNFOLLOW_WORK, estaVentana, new Integer(pos));

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("obra",id_obra);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web (sin mensaje de estado)
                TareaSegundoPlano tareaModificarSeguimiento = new TareaSegundoPlano(miClienteWeb,estaVentana);

                // Ejecutar la tarea en un hilo aparte del principal
                Log.d("VentanaProfesional", "Modificando seguimiento ("+ Boolean.toString(seguir) +") para la obra "+ id_obra + "...");
                tareaModificarSeguimiento.execute();
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

        // Posibles errores del servicio MY_WORKS_PRO a los que se desea dar un tratamiento particular
        // ...

        // Posibles errores del servicio MY_VOTES a los que se desea dar un tratamiento particular
        // ...

        // Posibles errores del servicio FOLLOW_WORK a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.FOLLOW_WORK, "ERR_NoEsPremium", false, R.string.msg_SeguirObra_ERR_NoEsPremium, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.FOLLOW_WORK, "ERR_ObraInexistente", false, R.string.msg_SeguirObra_ERR_ObraInexistente, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.FOLLOW_WORK, "ERR_ObraYaCerrada", false, R.string.msg_SeguirObra_ERR_ObraYaCerrada, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.FOLLOW_WORK, "ERR_LimiteSeguidores", false, R.string.msg_SeguirObra_ERR_LimiteSeguidores, idTituloOperacion) );

        // Posibles errores del servicio UNFOLLOW_WORK a los que se desea dar un tratamiento particular
        // ...

        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana, tarea, idTituloOperacion, email, listaErrores, GestorSesiones.TipoUsuario.PROFESIONAL);

        // Si la respuesta se recibió correctamente (OK), discriminar a qué servicio web corresponde
        // (de los posibles que se pueden consultar desde esta ventana) y procesarla por separado
        if ( respuesta_OK  )
        {
            // Servicio web asociado a la tarea, que contiene la respuesta del servidor
            ServicioWeb miServicioWeb = tarea.getCliente().getServicioWeb();

            if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.MY_WORKS_PRO )
            {
                procesarMisObras(miServicioWeb.getRespuesta());
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.MY_VOTES )
            {
                procesarMisVotos(miServicioWeb.getRespuesta());
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.FOLLOW_WORK )
            {
                procesarModificarSeguimiento(miServicioWeb.getRespuesta(), ((Integer) miServicioWeb.getParametroLocal()).intValue(), true);
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.UNFOLLOW_WORK )
            {
                procesarModificarSeguimiento(miServicioWeb.getRespuesta(), ((Integer) miServicioWeb.getParametroLocal()).intValue(), false);
            }
        }
    }


    // Procesamiento de la respuesta OK al servicio MY_WORKS_PRO
    public void procesarMisObras(RespuestaServicioWeb respuesta)
    {
        // Determinar la cantidad de cabeceras de obra que devolvió la consulta
        ArrayList<ContenidoServicioWeb> contenido = respuesta.getContenido();
        int tam = contenido.size();

        // Dependiendo de si se consultó el listado de obras adjudicadas/completadas/en seguimiento, cambiará el título de la ActionBar
        String detalle = respuesta.getDetalle();
        String titulo;

        if ( detalle.equals("OK_ObrasAdjudicadas") )        titulo = getResources().getString( R.string.VentanaProfesional_txt_titulo_obrasAdjudicadas );
        else if ( detalle.equals("OK_ObrasCompletadas") )   titulo = getResources().getString( R.string.VentanaProfesional_txt_titulo_obrasCompletadas);
        else                                                titulo = getResources().getString( R.string.VentanaProfesional_txt_titulo_obrasSeguidas);

        setTitle(titulo + " (" + Integer.toString(tam) + ")");


        // Si no hay datos de obras, mostrar un único elemento con un mensaje usando un ArrayAdapter de Strings
        // (se sobreescribe el método getView del ArrayAdapter genérico para poder especificar un color del texto concreto)
        if (tam == 0)
        {
            String[] titulosObras;
            String msg;

            if ( detalle.equals("OK_ObrasAdjudicadas") )        msg = getResources().getString( R.string.msg_MisObrasPro_OK_ObrasAdjudicadas );
            else if ( detalle.equals("OK_ObrasCompletadas") )   msg = getResources().getString( R.string.msg_MisObrasPro_OK_ObrasCompletadas );
            else                                                msg = getResources().getString( R.string.msg_MisObrasPro_OK_ObrasSeguidas );

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

            if ( detalle.equals("OK_ObrasAdjudicadas") || detalle.equals("OK_ObrasCompletadas") )
                obrasCerradas = true;

            Adapter_CabeceraObra miAdapter = new Adapter_CabeceraObra(estaVentana,R.layout.elemento_cabecera_obra, contenido, obrasCerradas, GestorSesiones.TipoUsuario.PROFESIONAL, true);
            listaItems.setAdapter(miAdapter);

            // Actualizar el menú de la ventana (para incluir las opciones de ordenar los resultados)
            hayDatosParaMostrar = true;
            onCreateOptionsMenu(menuActionBar);
        }
    }


    // Procesamiento de la respuesta OK al servicio MY_VOTES
    public void procesarMisVotos(RespuestaServicioWeb respuesta)
    {
        // Determinar la cantidad de valoraciones que devolvió la consulta
        ArrayList<ContenidoServicioWeb> contenido = respuesta.getContenido();
        int tam = contenido.size();

        // Establecer el título de la ventana
        String titulo = getResources().getString( R.string.VentanaProfesional_txt_titulo_valoraciones );
        setTitle(titulo + " (" + Integer.toString(tam) + ")");

        // Si no hay datos de valoraciones, mostrar un único elemento con un mensaje usando un ArrayAdapter de Strings
        // (se sobreescribe el método getView del ArrayAdapter genérico para poder especificar un color del texto concreto)
        if (tam == 0)
        {
            String[] titulosFavoritos;
            String msg = getResources().getString( R.string.msg_MisValoraciones_OK_ProfesionalTitular );

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

        // Si hay al menos una valoración, mostrar sus datos usando un Adapter personalizado de Info_Valoracion
        else
        {
            // El servicio web devuelve una lista de objetos ContenidoServicioWeb (superclase),
            // pero el adapter para cargar la lista de favoritos necesita una lista de objetos Info_Valoracion (subclase)
            ArrayList<Info_Valoracion> valoraciones = new ArrayList();

            for (int i=0; i<tam; i++)
                valoraciones.add( (Info_Valoracion) contenido.get(i) );

            Adapter_Valoraciones miAdapter = new Adapter_Valoraciones(estaVentana, R.layout.elemento_valoracion, valoraciones, gestorSesion);
            listaItems.setAdapter(miAdapter);

            // Actualizar el menú de la ventana (para incluir las opciones de ordenar los resultados)
            hayDatosParaMostrar = true;
            onCreateOptionsMenu(menuActionBar);
        }
    }


    // Procesamiento de la respuesta OK a los servicios FOLLOW_WORK y UNFOLLOW_WORK
    private void procesarModificarSeguimiento (RespuestaServicioWeb respuesta, int pos, boolean seguir)
    {
        // Modificar el valor de la entrada correspondiente en el Adapter de la lista,
        // refrescar la lista y mostrar confirmación de la operación al usuario
        String msg;
        Info_CabeceraObra infoObra = null;

        try
        {
            infoObra = (Info_CabeceraObra) listaItems.getItemAtPosition(pos);
        }
        catch (ClassCastException ex)
        {
            Log.e("VentanaProfesional","Error al hacer un cast a Info_CabeceraObra desde el adapter (pos: "+ Integer.toString(pos) +")");
            return;
        }

        // Si la petición fue de seguir una obra
        if ( seguir )
        {
            infoObra.setSeguidor(true);
            msg = getResources().getString(R.string.msg_SeguirObra_OK_ObraSeguida);
        }
        // Si la petición fue de olvidar una obra
        else
        {
            infoObra.setSeguidor(false);
            msg = getResources().getString(R.string.msg_OlvidarObra_OK_ObraOlvidada);
        }

        // Actualizar la vista del listado
        ((Adapter_CabeceraObra) listaItems.getAdapter()).notifyDataSetChanged();

        // Mensaje de confirmación al usuario
        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
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
            if ( accionActual == Accion.VALORACIONES )
                getMenuInflater().inflate(R.menu.menu_ventana_profesional_con_resultados_valoraciones, menu);
            else
                getMenuInflater().inflate(R.menu.menu_ventana_profesional_con_resultados_obras, menu);
        }
        else
            getMenuInflater().inflate(R.menu.menu_ventana_profesional_sin_resultados, menu);

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
                    Log.e("VentanaProfesional", "onMenuOpened ", e);
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


    // Comportamiento al pulsar los elementos de la action bar
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

                    if ( accionActual == Accion.OBRAS_ADJUDICADAS )
                        accionItemDesplegable(1);

                    else if ( accionActual == Accion.OBRAS_COMPLETADAS )
                        accionItemDesplegable(2);

                    else if ( accionActual == Accion.OBRAS_SEGUIDAS )
                        accionItemDesplegable(3);

                    else if ( accionActual == Accion.VALORACIONES )
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


                // Opción de ordenar las valoraciones mostradas por solicitante (ascendente)
                case R.id.menu_ordenar_solicitante_asc:

                    if ( listaItems.getAdapter() instanceof Adapter_Valoraciones )
                    {
                        Adapter_Valoraciones a = (Adapter_Valoraciones) listaItems.getAdapter();
                        a.ordenar(Info_Valoracion.Comparadores.Criterio.SOLICITANTE_ASCENDENTE);
                    }

                    return true;


                // Opción de ordenar las valoraciones mostradas por calidad descendente
                case R.id.menu_ordenar_calidad_desc:

                    if ( listaItems.getAdapter() instanceof Adapter_Valoraciones )
                    {
                        Adapter_Valoraciones a = (Adapter_Valoraciones) listaItems.getAdapter();
                        a.ordenar(Info_Valoracion.Comparadores.Criterio.CALIDAD_DESCENDENTE);
                    }

                    return true;


                // Opción de ordenar las valoraciones mostradas por precio descendente
                case R.id.menu_ordenar_precio_desc:

                    if ( listaItems.getAdapter() instanceof Adapter_Valoraciones )
                    {
                        Adapter_Valoraciones a = (Adapter_Valoraciones) listaItems.getAdapter();
                        a.ordenar(Info_Valoracion.Comparadores.Criterio.PRECIO_DESCENDENTE);
                    }

                    return true;


                // Opción de ordenar las valoraciones mostradas por fecha descendente
                case R.id.menu_ordenar_fecha_desc:

                    if ( listaItems.getAdapter() instanceof Adapter_Valoraciones )
                    {
                        Adapter_Valoraciones a = (Adapter_Valoraciones) listaItems.getAdapter();
                        a.ordenar(Info_Valoracion.Comparadores.Criterio.FECHA_DESCENDENTE);
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
        // Si el elemento en cuestión es el ListView que contiene las obras/valoraciones del profesional
        if ( v.getId()==R.id.lista_items)
        {
            // En ese caso, el objeto menuInfo será del subtipo AdapterContextMenuInfo
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            // Título y textos de las opciones del menú a mostrar
            // (diferentes opciones dependiendo de si estamos en el listado de obras adjudicadas/completadas/en seguimiento o valoraciones)
            String tituloMenu = "";
            String[] opcionesMenu = null;

            // Obtener una referencia a los datos del objeto de la lista, a traves de su adapter
            // (según los datos que muestre la lista en ese momento, será un objeto Info_CabeceraObra o Info_Valoracion)
            // Si el listView no contiene elementos (solo un texto indicando que está vacío),
            // entonces al hacer el casting saltará una excepcion que hemos de capturar
            Object datosElementoSeleccionado = listaItems.getAdapter().getItem(info.position);

            try
            {
                if (accionActual == Accion.OBRAS_ADJUDICADAS)
                {
                    tituloMenu = ((Info_CabeceraObra) datosElementoSeleccionado).getTitulo();
                    opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaProfesional_cabeceraObra_adjudicadas);
                }

                if (accionActual == Accion.OBRAS_COMPLETADAS)
                {
                    tituloMenu = ((Info_CabeceraObra) datosElementoSeleccionado).getTitulo();
                    opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaProfesional_cabeceraObra_completadas);
                }

                if (accionActual == Accion.OBRAS_SEGUIDAS)
                {
                    tituloMenu = ((Info_CabeceraObra) datosElementoSeleccionado).getTitulo();

                    boolean obraSeguida = ((Info_CabeceraObra)  datosElementoSeleccionado).esSeguidor();

                    if ( obraSeguida )
                        opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaProfesional_cabeceraObra_seguidas_seguida);
                    else
                        opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaProfesional_cabeceraObra_seguidas_noSeguida);
                }

                if (accionActual == Accion.VALORACIONES)
                {
                    tituloMenu = ((Info_Valoracion) datosElementoSeleccionado).getTitulo_obra();
                    opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaProfesional_valoracion);
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


    // Acción a realizar al seleccionar un elemento del menú contexual
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
        // (según los datos que muestre la lista en ese momento, será un objeto Info_CabeceraObra o Info_CabeceraParticular)
        // Si el listView no contiene elementos (solo un texto indicando que está vacío),
        // entonces al hacer el casting saltará una excepcion que hemos de capturar
        Object datosElementoSeleccionado = listaItems.getItemAtPosition(posLista);

        try
        {
            // Si la acción es sobre un elemento de la lista de obras adjudicadas
            if (accionActual == Accion.OBRAS_ADJUDICADAS)
            {
                final Info_CabeceraObra infoObra = (Info_CabeceraObra) datosElementoSeleccionado;
                String idObra =  infoObra.getId();

                // Opción de ver detalles de la obra
                if (indiceMenu == 0)
                {
                    Intent intent = new Intent(estaVentana, VentanaDatosObra.class);

                    Bundle b = new Bundle();
                    b.putString("id_obra", idObra);
                    b.putSerializable("accion", accionActual);
                    b.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
                    intent.putExtras(b);

                    startActivity(intent);
                }
            }

            // Si la acción es sobre un elemento de la lista de obras completadas
            else if (accionActual == Accion.OBRAS_COMPLETADAS)
            {
                Info_CabeceraObra infoObra = (Info_CabeceraObra) datosElementoSeleccionado;
                String idObra =  infoObra.getId();

                // Opción de ver detalles de la obra
                if (indiceMenu == 0)
                {
                    Intent intent = new Intent(estaVentana, VentanaDatosObra.class);

                    Bundle b = new Bundle();
                    b.putString("id_obra", idObra);
                    b.putSerializable("accion", accionActual);
                    b.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
                    intent.putExtras(b);

                    startActivity(intent);
                }
            }

            // Si la acción es sobre un elemento de la lista de obras en seguimiento
            else if (accionActual == Accion.OBRAS_SEGUIDAS)
            {
                Info_CabeceraObra infoObra = (Info_CabeceraObra) datosElementoSeleccionado;
                String idObra =  infoObra.getId();

                // Opción de ver detalles de la obra
                if (indiceMenu == 0)
                {
                    Intent intent = new Intent(estaVentana, VentanaDatosObra.class);

                    Bundle b = new Bundle();
                    b.putString("id_obra", idObra);
                    b.putSerializable("accion", accionActual);
                    b.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
                    intent.putExtras(b);

                    startActivity(intent);
                }

                // Opción de seguir/dejar de seguir la obra
                else if (indiceMenu == 1)
                {
                    boolean seguir = infoObra.esSeguidor();

                    modificarSeguimiento(idObra, !seguir, posLista);
                }
            }


            // Si la acción es sobre un elemento de la lista de valoraciones recibidas
            else if (accionActual == Accion.VALORACIONES)
            {
                Info_Valoracion infoVal = (Info_Valoracion) datosElementoSeleccionado;
                String idObra =  infoVal.getId_obra();

                // Opción de ver detalles de la obra
                if (indiceMenu == 0)
                {
                    Intent intent = new Intent(estaVentana, VentanaDatosObra.class);

                    Bundle b = new Bundle();
                    b.putString("id_obra", idObra);
                    b.putSerializable("accion", Accion.OBRAS_COMPLETADAS);
                    b.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
                    intent.putExtras(b);

                    startActivity(intent);
                }
            }
        }
        catch (ClassCastException ex)
        {
        }

        return true;
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
            Intent intent = new Intent(estaVentana, VentanaPerfilProfesional.class);
            startActivity(intent);
        }

        // Opción de ver las obras adjudicadas del profesional
        else if ( pos==1 )
        {
            accionActual = Accion.OBRAS_ADJUDICADAS;
            cargarCabecerasObras(accionActual);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);
        }

        // Opción de ver las obras completadas del profesional
        else if ( pos==2 )
        {
            accionActual = Accion.OBRAS_COMPLETADAS;
            cargarCabecerasObras(accionActual);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);
        }

        // Opción de ver las obras en seguimiento del profesional
        else if ( pos==3 )
        {
            accionActual = Accion.OBRAS_SEGUIDAS;
            cargarCabecerasObras(accionActual);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);
        }

        // Opción de consultar la lista de valoraciones del usuario
        else if ( pos==4 )
        {
            accionActual = Accion.VALORACIONES;
            cargarValoraciones();

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);
        }

        // Opción de buscar obras
        else if ( pos==5 )
        {
            marcarOpcionDesplegable(accionActual);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);

            // Pasar a la ventana de busqueda de obras
            Intent intent = new Intent(estaVentana, VentanaBusquedaObra.class);
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
            info.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
            intent.putExtras(info);

            startActivity(intent);
        }

        // Opción de acceso premium
        else if ( pos==7 )
        {
            marcarOpcionDesplegable(accionActual);

            // Cerrar el desplegable
            contenedorPrincipal.closeDrawer(contenedorDesplegable);

            // Pasar a la ventana de acceso premium
            Intent intent = new Intent(estaVentana, VentanaAccesoPremium.class);
            startActivity(intent);
        }

        else
        {
            marcarOpcionDesplegable(accionActual);

            // No se hace ninguna operación, solo registramos el evento en el log
            Log.e("VentanaParticular", "Opción del drawer ("+Integer.toString(pos) + ") no implementada aún");
        }
    }


    // Por defecto, cuando se pulsa una opción del desplegable, queda marcada.
    // Este método fuerza a que quede marcada otra opción distinta
    // (método de clase privado)
    private void marcarOpcionDesplegable(Accion a)
    {
        if ( a==Accion.OBRAS_ADJUDICADAS )
            contenedorDesplegable.setItemChecked(1, true);

        if ( a==Accion.OBRAS_COMPLETADAS )
            contenedorDesplegable.setItemChecked(2, true);

        if ( a==Accion.OBRAS_SEGUIDAS )
            contenedorDesplegable.setItemChecked(3, true);

        if ( a==Accion.VALORACIONES )
            contenedorDesplegable.setItemChecked(4, true);
    }


    // Fija el texto y el color de texto para el título de la Action Bar
    @Override
    public void setTitle(CharSequence titulo)
    {
        String color = Integer.toHexString(getResources().getColor(R.color.texto_actionBar) & 0x00ffffff);
        tituloVentana = titulo;

        getSupportActionBar().setTitle( Html.fromHtml("<font color='" + color + "'>" + tituloVentana + "</font>") );
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
