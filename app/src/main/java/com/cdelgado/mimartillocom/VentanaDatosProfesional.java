package com.cdelgado.mimartillocom;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;


public class VentanaDatosProfesional extends VentanaBase
{
    private String idProfesional;
    private GestorSesiones.TipoUsuario tipoUsuario;
    private boolean vistaParticular;

    private GestorSesiones gestorSesion;

    protected ProgressDialog pDialog;
    private VentanaDatosProfesional estaVentana;

    private Menu menuActionBar;


    LinearLayout tabDatosProfesional;

    ImageView imgProAvatar;
    TextView txtProNombre;
    TableLayout layoutProRatings;
    TextView txtProCalidadDatos;
    RatingBar rtnProCalidad;
    TextView txtProPrecioDatos;
    RatingBar rtnProPrecio;
    TextView txtProDescripcion;

    TableRow tbrProDireccion;
    TableRow tbrProPoblacion;
    TableRow tbrProTelefono1;
    TableRow tbrProTelefono2;
    TableRow tbrProEmail;
    TableRow tbrProWeb;
    TextView txtProDireccion;
    TextView txtProDireccionDatos;
    TextView txtProPoblacionDatos;
    TextView txtProTelefono1Datos;
    TextView txtProTelefono2Datos;
    TextView txtProEmailDatos;
    TextView txtProWebDatos;

    ToggleButton btnProEspecializacion;
    LinearLayout layoutProInternoEspecializacion;
    TextView txtProEspecialidades;
    LinearLayout listaEspecialidades;

    TextView titulo_tab0;
    TextView titulo_tab1;

    LinearLayout tabValoraciones;
    ListView listaValoraciones;

    private CharSequence tituloVentana;

    private boolean avatarPorDefecto;   // Permite discriminar si imgProAvatar está mostrando la imagen por defecto o no

    private boolean esFavorito; // Indica si el profesional mostrado esta en la lista de favoritos (solo usuarios particulares)


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_datos_profesional);


        // Referencia al propio objeto activity
        estaVentana = this;

        // Cuadro de progreso (para mostrar durante las consultas por red)
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // Referencia a los elementos de la ventana
        tabDatosProfesional = (LinearLayout) findViewById(R.id.tabDatosProfesional);

        imgProAvatar       = (ImageView) findViewById(R.id.imgProAvatar);
        txtProNombre       = (TextView) findViewById(R.id.txtProNombre);
        layoutProRatings   = (TableLayout) findViewById(R.id.layoutProRatings);
        txtProCalidadDatos = (TextView) findViewById(R.id.txtProCalidadDatos);
        rtnProCalidad      = (RatingBar) findViewById(R.id.rtnProCalidad);
        txtProPrecioDatos  = (TextView) findViewById(R.id.txtProPrecioDatos);
        rtnProPrecio       = (RatingBar) findViewById(R.id.rtnProPrecio);
        txtProDescripcion  = (TextView) findViewById(R.id.txtProDescripcion);

        tbrProDireccion = (TableRow) findViewById(R.id.tbrProDireccion);
        tbrProPoblacion = (TableRow) findViewById(R.id.tbrProPoblacion);
        tbrProTelefono1 = (TableRow) findViewById(R.id.tbrProTelefono1);
        tbrProTelefono2 = (TableRow) findViewById(R.id.tbrProTelefono2);
        tbrProEmail     = (TableRow) findViewById(R.id.tbrProEmail);
        tbrProWeb       = (TableRow) findViewById(R.id.tbrProWeb);

        txtProDireccion = (TextView) findViewById(R.id.txtProDireccion);
        txtProDireccionDatos = (TextView) findViewById(R.id.txtProDireccionDatos);
        txtProPoblacionDatos = (TextView) findViewById(R.id.txtProPoblacionDatos);
        txtProTelefono1Datos = (TextView) findViewById(R.id.txtProTelefono1Datos);
        txtProTelefono2Datos = (TextView) findViewById(R.id.txtProTelefono2Datos);
        txtProEmailDatos     = (TextView) findViewById(R.id.txtProEmailDatos);
        txtProWebDatos       = (TextView) findViewById(R.id.txtProWebDatos);

        btnProEspecializacion = (ToggleButton) findViewById(R.id.btnProEspecializacion);
        layoutProInternoEspecializacion = (LinearLayout) findViewById(R.id.layoutProInternoEspecializacion);
        txtProEspecialidades = (TextView) findViewById(R.id.txtProEspecialidades);
        listaEspecialidades    = (LinearLayout) findViewById(R.id.layoutEspecialidades);

        tabValoraciones = (LinearLayout) findViewById(R.id.tabValoraciones);
        listaValoraciones = (ListView) findViewById(R.id.listaValoraciones);


        // Por defecto, el avatar muestra la imagen de perfil estándar
        avatarPorDefecto = true;



        // Configuración de las pestañas
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("Tab Datos Profesional");
        spec.setContent(R.id.tabDatosProfesional);
        spec.setIndicator(getResources().getString(R.string.VentanaDatosProfesional_txt_tabDetalles));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Tab Valoraciones");
        spec.setContent(R.id.tabValoraciones);
        spec.setIndicator(getResources().getString(R.string.VentanaDatosProfesional_txt_tabValoraciones));
        tabHost.addTab(spec);

        // Color del título de cada pestaña
        titulo_tab0 = (TextView) tabHost.getTabWidget().getChildTabViewAt(0).findViewById(android.R.id.title);
        titulo_tab1 = (TextView) tabHost.getTabWidget().getChildTabViewAt(1).findViewById(android.R.id.title);
        titulo_tab0.setTextColor(this.getResources().getColorStateList(R.color.texto_tabs));
        titulo_tab1.setTextColor(this.getResources().getColorStateList(R.color.texto_tabs));

        // Por defecto, la ventana abre siempre con la pestaña de datos del profesional
        tabHost.setCurrentTab(0);



        // Datos pasados en el intent
        Bundle info = this.getIntent().getExtras();

        // Recuperar la información de si el usuario es un particular o un profesional pasada en el intent, si es que se pasó alguna
        // (por defecto, se considera que es un particular)
        vistaParticular = true;
        if ( info!=null && (info.getSerializable("tipo_usuario"))!=null )
        {
            tipoUsuario = (GestorSesiones.TipoUsuario) info.getSerializable("tipo_usuario");

            if ( tipoUsuario == GestorSesiones.TipoUsuario.PROFESIONAL )
                vistaParticular = false;
        }


        // Gestor de sesion de usuario particular/profesional
        if ( vistaParticular )
            gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PARTICULAR);
        else
            gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PROFESIONAL);


        // Configuración de la Action Bar de esta ventana: título, color de fondo y visibilidad
        String tituloActionBar;
        tituloActionBar = getResources().getString(R.string.VentanaDatosProfesional_txt_titulo);

        setTitle( tituloActionBar );
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();

        // Mostrar el icono de volver hacia atrás en la action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Inicialmente, el layout con los datos del pprofesional está oculto (hasta que se carguen los datos del servidor)
        tabDatosProfesional.setVisibility(View.GONE);

        // Asimismo, el layout con las especialidades del profesional también está oculto (hasta que se pulse el botón de mostrar)
        layoutProInternoEspecializacion.setVisibility(View.GONE);


        // Recuperar la información del id del profesional pasada en el intent, si es que se pasó alguna
        // y llamar al servicio web que devuelva información sobre ese profesional
        idProfesional = null;
        if ( info!=null && (idProfesional=info.getString("id_profesional"))!=null )
        {
            solicitarDatosProfesional();
        }



        // Comportamiento al pulsar la imagen del perfil
        imgProAvatar.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Si el profesional tiene un avatar personalizado, mostrarlo en grande en una nueva ventana
                    if ( ! avatarPorDefecto )
                    {
                        Bitmap bitmap = ((BitmapDrawable)imgProAvatar.getDrawable()).getBitmap();
                        String msg = getResources().getString(R.string.general_txt_imagenPerfil);

                        gestorSesion.setDatosTemporales( Utils.codifica_jpeg_base64(bitmap) );

                        Intent intent = new Intent(estaVentana,VentanaMostrarImagen.class);
                        Bundle info = new Bundle();
                        info.putString("mensaje",msg);
                        intent.putExtras(info);

                        startActivity(intent);
                    }
                }
            }
        );


        // Comportamiento del botón de Mostrar/Ocultar las especialidades del profesional
        btnProEspecializacion.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    boolean on = ((ToggleButton) v).isChecked();

                    if (on)
                        layoutProInternoEspecializacion.setVisibility(View.VISIBLE);
                    else
                        layoutProInternoEspecializacion.setVisibility(View.GONE);
                }
            }
        );


        // Comportamiento al pulsar en la ListView de las valoraciones del profesional
        // (abrir una ventana con los datos de la obra correspondiente)
        listaValoraciones.setOnItemClickListener
        (
            new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView parent, View v, int pos, long id)
                {
                    String idObra = (String) v.getTag();

                    if (idObra != null) {
                        Intent intent = new Intent(VentanaDatosProfesional.this, VentanaDatosObra.class);

                        Bundle info = new Bundle();
                        info.putString("id_obra", idObra);
                        info.putSerializable("accion", VentanaParticular.Accion.OBRAS_CERRADAS);    // Todas las obras que aparecen
                                                                                                    // en las valoraciones ya están cerradas
                        info.putSerializable("tipo_usuario", tipoUsuario);
                        intent.putExtras(info);

                        startActivity(intent);
                    }
                }
            }
        );


        // Comportamiento al pulsar sobre los TextView de los datos de contacto del profesional
        // (se abrirá la aplicación externa correspondiente)
        // ----------------------------------------------------------------------------------------------------------------------------------

        // Al pulsar sobre la dirección, abrir la aplicación de mapas
        txtProDireccionDatos.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    String direccion = txtProDireccionDatos.getText().toString().trim() +", "+ txtProPoblacionDatos.getText().toString().trim();
                    String etiqueta  = txtProNombre.getText().toString().trim();

                    ArrayList<String> parametrosLlamadaExterna = new ArrayList();
                    parametrosLlamadaExterna.add(direccion);
                    parametrosLlamadaExterna.add(etiqueta);

                    Utils.aplicacionExterna(estaVentana, Utils.AppExterna.MAPA_DIRECCION, parametrosLlamadaExterna);
                }
            }
        );

        // Al pulsar sobre el teléfono #1, abrir la aplicación de llamadas
        txtProTelefono1Datos.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    String telefono = txtProTelefono1Datos.getText().toString().trim();

                    ArrayList<String> parametrosLlamadaExterna = new ArrayList();
                    parametrosLlamadaExterna.add(telefono);

                    Utils.aplicacionExterna(estaVentana, Utils.AppExterna.LLAMADA, parametrosLlamadaExterna);
                }
            }
        );

        // Al pulsar sobre el teléfono #2, abrir la aplicación de llamadas
        txtProTelefono2Datos.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    String telefono = txtProTelefono2Datos.getText().toString().trim();

                    ArrayList<String> parametrosLlamadaExterna = new ArrayList();
                    parametrosLlamadaExterna.add(telefono);

                    Utils.aplicacionExterna(estaVentana, Utils.AppExterna.LLAMADA, parametrosLlamadaExterna);
                }
            }
        );

        // Al pulsar sobre la dirección de email, abrir la aplicación de correo
        txtProEmailDatos.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    String destinatario = ((TextView) arg0).getText().toString().trim();
                    String asunto = getResources().getString(R.string.VentanaDatosProfesional_txt_asuntoEmail);

                    ArrayList<String> parametrosLlamadaExterna = new ArrayList();
                    parametrosLlamadaExterna.add(destinatario);
                    parametrosLlamadaExterna.add(asunto);

                    Utils.aplicacionExterna(estaVentana, Utils.AppExterna.CORREO, parametrosLlamadaExterna);
                }
            }
        );

        // Al pulsar sobre la dirección web, abrir la aplicación del navegador
        txtProWebDatos.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    String url = ((TextView) arg0).getText().toString().trim();

                    ArrayList<String> parametrosLlamadaExterna = new ArrayList();
                    parametrosLlamadaExterna.add(url);

                    Utils.aplicacionExterna(estaVentana, Utils.AppExterna.NAVEGADOR, parametrosLlamadaExterna);
                }
            }
        );

    }



    // Solicita al servidor los datos del profesional correspondiente, para después mostrarlos en la ventana
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    private void solicitarDatosProfesional()
    {
        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(VentanaDatosProfesional.this, VentanaLogin.class);

            Bundle info = new Bundle();
            info.putSerializable("tipo_usuario", tipoUsuario);
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

            // Idioma del cliente
            String idioma = Utils.idiomaAplicacion();

            // Resto de parámetros de la petición (tipo de usuario e id de la obra a consultar)
            String tipo_usuario;
            if ( vistaParticular )  tipo_usuario = "particular";
            else                    tipo_usuario = "profesional";

            String id_profesional = idProfesional;


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_PROFESSIONAL_INFO);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.PROFESSIONAL_INFO, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("idioma",idioma);
                miServicioWeb.addParam("tipo_usuario",tipo_usuario);
                miServicioWeb.addParam("profesional",id_profesional);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaGetInfoProfesional = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaDatosProfesional", "Solicitando info sobre profesional " + idProfesional + "...");
                tareaGetInfoProfesional.execute();
            }
        }
    }


    // Solicita al servidor agregar o eliminar el profesional en la lista de favoritos del usuario
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    public void modificarFavorito(boolean agregar)
    {
        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            // Redirigimos a la Ventana de Login a traves de la ventana de Inicio, cerrando todas las demás
            // (de este modo, garantizamos que la ventana de inicio siempre queda en el fondo de la pila
            // independientemente de cuántas ventanas hubiéramos abierto)
            Intent intent = new Intent(estaVentana, VentanaInicio.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Bundle info = new Bundle();
            info.putBoolean("ir_a_ventana_login", true);
            info.putSerializable("tipo_usuario", tipoUsuario);

            intent.putExtras(info);

            finish();
            startActivity(intent);
        }

        // Si hay credenciales almacenadas, se enviarán junto a la petición
        else
        {
            // Obtener credenciales almacenadas
            HashMap<String, String> datosSesion = gestorSesion.getDatosSesion();
            String id_usuario = datosSesion.get(gestorSesion.KEY_USUARIO);
            String id_sesion = datosSesion.get(gestorSesion.KEY_SESION);

            // Resto de parámetros de la petición (tipo de acción e id del profesional a agregar/eliminar)
            String accion = "";
            if ( agregar == true )  accion = "agregar";
            else                    accion = "eliminar";

            String id_profesional = idProfesional;


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
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.MODIFY_FAVORITES, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("accion",accion);
                miServicioWeb.addParam("profesional",id_profesional);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaModificarFavoritos = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaDatosProfesional", accion +" en favoritos al profesional "+ id_profesional + "...");
                tareaModificarFavoritos.execute();
            }
        }
    }


    // Procesar la respuesta del servicio web
    // (se invoca cuando una tarea en segundo plano ha finalizado)
    @Override
    public void procesarResultado(TareaSegundoPlano tarea)
    {
        // Título del cuadro de diálogo de error (por si hubo errores en la tarea)
        // y email del usuario (por si hubo problemas con la sesión y hay que volver a la ventana de Login)
        int idTituloOperacion = tarea.getIdTituloOperacion();
        String email = gestorSesion.getDatosSesion().get(gestorSesion.KEY_EMAIL);


        // Lista de posibles respuestas de error que puede recibir esta ventana y el tratamiento que debe darse a cada una
        // (sin incluir las respuestas de sesión expirada, sesión inválida o usuario deshabilitado, que ya se tratan por defecto)
        ArrayList<ErrorServicioWeb> listaErrores = new ArrayList();

        // Posibles errores del servicio PROFESSIONAL_INFO a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.PROFESSIONAL_INFO, "ERR_ProfesionalInexistente", true, R.string.msg_InfoProfesional_ERR_ProfesionalInexistente, idTituloOperacion) );

        // Posibles errores del servicio MODIFY_FAVORITES a los que se desea dar un tratamiento particular


        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana, tarea, idTituloOperacion, email, listaErrores, tipoUsuario);

        // Si la respuesta se recibió correctamente (OK), discriminar a qué servicio web corresponde
        // (de los posibles que se pueden consultar desde esta ventana) y procesarla por separado
        if ( respuesta_OK )
        {
            // Servicio web asociado a la tarea, que contiene la respuesta del servidor
            ServicioWeb miServicioWeb = tarea.getCliente().getServicioWeb();

            if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.PROFESSIONAL_INFO )
            {
                procesarProfessionalInfo( miServicioWeb.getRespuesta() );
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.MODIFY_FAVORITES )
            {
                procesarModificarFavoritos( miServicioWeb.getRespuesta() );
            }
        }

    }


    // Procesamiento de la respuesta OK al servicio PROFESSIONAL_INFO
    private void procesarProfessionalInfo (RespuestaServicioWeb respuesta)
    {
        // Referencia al objeto que contiene los datos de la obra
        Info_Profesional profesional = (Info_Profesional) (respuesta.getContenido().get(0));

        // Cuántos votos ha recibido este profesional
        int votos = profesional.getValoraciones().size();


        // Datos del profesional --> pestaña TabDatosProfesional
        txtProNombre.setText(profesional.getNombre());

        if ( ! profesional.getAvatar().equals("default_profile_pic") )
        {
            imgProAvatar.setImageBitmap(Utils.descodifica_imagen_base64(profesional.getAvatar()));
            avatarPorDefecto = false;
        }

        // Si el usuario tiene algún voto, mostramos sus ratings
        // Si no, ocultamos la tabla de ratings
        if (votos > 0)
        {
            //txtProCalidadDatos.setText(Double.toString(profesional.getMedia_calidad()));
            txtProCalidadDatos.setText( Utils.formatearReal(profesional.getMedia_calidad(),Utils.idiomaAplicacion()) );
            rtnProCalidad.setRating((float) profesional.getMedia_calidad());
            //txtProPrecioDatos.setText(Double.toString(profesional.getMedia_precio()));
            txtProPrecioDatos.setText( Utils.formatearReal(profesional.getMedia_precio(),Utils.idiomaAplicacion()) );
            rtnProPrecio.setRating((float) profesional.getMedia_precio());
        }
        else
        {
            layoutProRatings.setVisibility(View.GONE);
        }


        boolean sinDatosContacto = true;

        // Si no hay definida una descripción, ocultar el texto correspondiente
        // En caso contrario, mostrarla
        if (profesional.getDescripcion().equals("n/d"))
            txtProDescripcion.setVisibility(View.GONE);
        else
        {
            txtProDescripcion.setText(profesional.getDescripcion());
            sinDatosContacto = false;
        }

        // Si no hay definida una dirección, ocultar el layout correspondiente
        // En caso contrario, mostrarla
        if (profesional.getDireccion().equals("n/d"))
            tbrProDireccion.setVisibility(View.GONE);
        else
        {
            txtProDireccionDatos.setText(profesional.getDireccion());
            txtProDireccionDatos.setPaintFlags(txtProDireccionDatos.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            sinDatosContacto = false;
        }

        // Si no hay definida una población, ocultar el layout correspondiente
        // En caso contrario, mostrarla
        if (profesional.getId_poblacion().equals("0"))
            tbrProPoblacion.setVisibility(View.GONE);
        else
        {
            txtProPoblacionDatos.setText(profesional.getPoblacion() + " (" + profesional.getProvincia() + ")");
            sinDatosContacto = false;
        }

        // Si se definió algún teléfono en el campo telefono1 o telefono2, mostrarlo
        // Si alguno (o los dos campos) están vacíos, se ocultan
        ArrayList<String> listaTelefonos = new ArrayList<>();
        if (!profesional.getTelefono1().equals("n/d"))
            listaTelefonos.add(profesional.getTelefono1());
        if (!profesional.getTelefono2().equals("n/d"))
            listaTelefonos.add(profesional.getTelefono2());

        if (listaTelefonos.size() == 0)
        {
            tbrProTelefono1.setVisibility(View.GONE);
            tbrProTelefono2.setVisibility(View.GONE);
        }
        else if (listaTelefonos.size() == 1)
        {
            txtProTelefono1Datos.setText(listaTelefonos.get(0));
            txtProTelefono1Datos.setPaintFlags(txtProTelefono1Datos.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tbrProTelefono2.setVisibility(View.GONE);
            sinDatosContacto = false;
        }
        else
        {
            txtProTelefono1Datos.setText(listaTelefonos.get(0));
            txtProTelefono2Datos.setText(listaTelefonos.get(1));
            txtProTelefono1Datos.setPaintFlags(txtProTelefono1Datos.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            txtProTelefono2Datos.setPaintFlags(txtProTelefono2Datos.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            sinDatosContacto = false;
        }

        // Si no hay definido un email, ocultar el layout correspondiente
        // En caso contrario, mostrarlo
        if (profesional.getEmail_contacto().equals("n/d"))
            tbrProEmail.setVisibility(View.GONE);
        else
        {
            txtProEmailDatos.setText( profesional.getEmail_contacto() );
            txtProEmailDatos.setPaintFlags(txtProEmailDatos.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            sinDatosContacto = false;
        }

        // Si no hay definido una dirección web, ocultar el layout correspondiente
        // En caso contrario, mostrarla
        if ( profesional.getWeb().equals("n/d") )
            tbrProWeb.setVisibility(View.GONE);
        else
        {
            txtProWebDatos.setText(profesional.getWeb());
            txtProWebDatos.setPaintFlags(txtProWebDatos.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            sinDatosContacto = false;
        }

        // Si no se definió nungún dato de contacto, mostrar un mensaje
        if ( sinDatosContacto )
        {
            String msg = getResources().getString( R.string.VentanaDatosProfesional_txt_sinDatosContacto );
            txtProDireccion.setText(msg);

            txtProDireccionDatos.setVisibility(View.GONE);
            tbrProDireccion.setVisibility(View.VISIBLE);
        }


        // Especialización del profesional, si es que indicó alguna
        Log.d("VentanaDatosProfesional","Especialidades recibidas: "+profesional.getEspecialidades() );

        if ( Utils.contarEspecialidades(profesional.getActividades())==0 )
        {
            String msg = getResources().getString( R.string.VentanaDatosProfesional_txt_sinEspecialidades );
            txtProEspecialidades.setText(msg);
            listaEspecialidades.setVisibility(View.GONE);
        }
        else
        {
            txtProEspecialidades.setVisibility(View.GONE);
            Utils.construirTablaEspecialidades(estaVentana, listaEspecialidades, profesional.getActividades(),false);
            listaEspecialidades.setVisibility(View.VISIBLE);
        }



        // Valoraciones de los usuarios sobre este profesional --> pestaña tabProfesionalesInteresados
        ArrayList<Info_Valoracion> valoraciones = profesional.getValoraciones();

        // Actualizar el título de la pestaña, para incluir el número de valoraciones
        String txt = getResources().getString(R.string.VentanaDatosProfesional_txt_tabValoraciones) + " ("+ Integer.toString(votos) +")";
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        ((TextView) tabHost.getTabWidget().getChildTabViewAt(1).findViewById(android.R.id.title)).setText(txt);

        // Si no hay elementos en la lista, mostrar un único elemento con un mensaje usando un ArrayAdapter de Strings
        // (se sobreescribe el método getView del ArrayAdapter genérico para indicar un color del texto específico);
        if (votos < 1)
        {
            String[] titulosValoraciones = new String[1];

            String msg = getResources().getString( R.string.general_txt_profesionalSinValoraciones);
            titulosValoraciones[0] = msg;

            ArrayAdapter miAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titulosValoraciones)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTextColor( estaVentana.getResources().getColor(R.color.texto_tabs) );

                    return textView;
                }
            };

            listaValoraciones.setAdapter(miAdapter);

            // Deshabilitar el click sobre el ListView
            listaValoraciones.setSelector(android.R.color.transparent);
        }

        // Si hay al menos una valoración para el profesional, mostrarla usando un Adapter personalizado de Info_Valoracion
        else
        {
            Adapter_Valoraciones miAdapter = new Adapter_Valoraciones(estaVentana, R.layout.elemento_valoracion, valoraciones, gestorSesion);
            listaValoraciones.setAdapter(miAdapter);
        }

        // Una vez que se han cargado todos los datos de la obra, mostrar el layout de la ventana (el ScrollView)
        tabDatosProfesional.setVisibility(View.VISIBLE);


        // Por último, mostrar el menú de ventana correspondiente
        // (varía si el usuario es particular o no, y si el profesional está en su lista de favoritos o no)
        esFavorito = profesional.esFavorito();
        onCreateOptionsMenu(menuActionBar);
    }


    // Procesamiento de la respuesta OK al servicio MODIFY_FAVORITES
    // (actualizar el menú de la ventana y mostrar mensaje de confirmación al usuario)
    private void procesarModificarFavoritos (RespuestaServicioWeb respuesta)
    {
        String msg = "";

        // Si la petición fue de agregar el profesional a favoritos
        if ( respuesta.getDetalle().equals("OK_FavoritoAgregado") )
        {
            esFavorito = true;
            msg = getResources().getString(R.string.msg_AgregarFavorito_OK_FavoritoAgregado);
        }
        // Si la petición fue de eliminar el profesional de favoritos
        else if ( respuesta.getDetalle().equals("OK_FavoritoEliminado") )
        {
            esFavorito = false;
            msg = getResources().getString(R.string.msg_EliminarFavorito_OK_FavoritoEliminado);
        }

        onCreateOptionsMenu(menuActionBar);

        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Guardamos una referencia al objeto menú de la vetnana, si no la teníamos antes
        if ( menuActionBar == null )
            menuActionBar = menu;

        // Limpiar el menú desplegable de la ventana (si lo había)
        menu.clear();


        int opciones_menu_actionBar;

        // Si el usuario que visualiza la obra es un particular
        if (vistaParticular)
        {
            if (esFavorito)
                opciones_menu_actionBar = R.menu.menu_ventana_datos_profesional_vista_particular_favorito;
            else
                opciones_menu_actionBar = R.menu.menu_ventana_datos_profesional_vista_particular_no_favorito;
        }

        // Si el usuario que visualiza la obra es un profesional
        else
            opciones_menu_actionBar = R.menu.menu_ventana_datos_profesional_vista_profesional;


        getMenuInflater().inflate(opciones_menu_actionBar, menuActionBar);

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
                    Log.e("VentanaDatosProfesional", "onMenuOpened ", e);
                }
                catch(Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        return super.onMenuOpened(featureId, menu);
    }


    // Comportamiento al pulsar los elementos de la action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch ( item.getItemId() )
        {
            // Botón home de la action bar (volver atras)
            case android.R.id.home:
                onBackPressed();
                break;

            // Opción de cerrar la sesión (usuarios particulares y profesionales)
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


            // Opción de añadir el profesional a favoritos (solo usuarios particulares)
            case R.id.menu_agregar_favorito:

                modificarFavorito(true);

                return true;


            // Opción de eliminar al profesional de favoritos (solo usuarios particulares)
            case R.id.menu_eliminar_favorito:

                modificarFavorito(false);

                return true;


            default:
                break;
        }

        return true;

/*
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
*/
    }

}
