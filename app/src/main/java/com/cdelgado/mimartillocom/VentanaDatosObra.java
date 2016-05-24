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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedDrawable;
import com.makeramen.roundedimageview.RoundedImageView;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;


public class VentanaDatosObra extends VentanaBase
{
    private String idObra;
    private Info_Obra datosObra;

    private GestorSesiones.TipoUsuario tipoUsuario;
    private boolean vistaParticular;
    private boolean obraAbierta;    // solo si el usuario que visualiza la obra es particular

    private boolean obraEnSeguimiento;  // solo si el usuario que visualiza la obra es profesional
    private boolean obraCompletada;     // solo si el usuario que visualiza la obra es profesional
    private boolean obraEncontrada;     // solo si el usuario que visualiza la obra es profesional

    private GestorSesiones gestorSesion;

    protected ProgressDialog pDialog;
    private VentanaDatosObra estaVentana;

    private Menu menuActionBar;


    RelativeLayout tabDatosObra;
    TextView txtTituloObra;
    TextView txtFotosObraContador;
    TextView txtDescripcionObra;
    TextView txtClasificacionObra;
    TextView txtFechaCreacionDatos;
    TextView txtFechaRealizacionDatos;
    TextView txtVisitasDatos;
    TextView txtUbicacionDatos;
    ImageButton btnMapaObra;
    FrameLayout frmSinDatosContacto;
    RelativeLayout tblDatosContacto;
    ImageView imgAvatarContactoObra;
    TextView txtNombreContactoObraDatos;
    TextView txtEmailContactoObraDatos;
    TextView txtTelefonoContactoObraDatos;
    LinearLayout layoutFotosObra;
    RoundedImageView imgObra0, imgObra1, imgObra2, imgObra3;

    LinearLayout tabProfesionalesInteresados;
    ListView listaProfesionalesInteresados;

    TextView titulo_tab0;
    TextView titulo_tab1;

    private String coordenadasObra_lat;
    private String coordenadasObra_lon;

    private CharSequence tituloVentana;

    private boolean avatarPorDefecto;   // permite discriminar si imgAvatarContactoObra está mostrando la imagen por defecto o no


    // Estructuras para el manejo de los botones de fotos de obra
    int contadorFotos;
    int maxImagenes;
    ArrayList<RoundedImageView> imagenes;
    boolean[] adjuntadas;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_datos_obra);


        // Referencia al propio objeto activity
        estaVentana = this;


        // Cuadro de progreso (para mostrar durante las consultas por red)
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // Referencia a los elementos de la ventana
        tabDatosObra                 = (RelativeLayout) findViewById(R.id.tabDatosObra);
        txtTituloObra                = (TextView) findViewById(R.id.txtTituloObra);
        txtFotosObraContador         = (TextView) findViewById(R.id.txtFotosObraContador);
        txtDescripcionObra           = (TextView) findViewById(R.id.txtDescripcionObra);
        txtClasificacionObra         = (TextView) findViewById(R.id.txtClasificacionObra);
        //txtNombreActividad           = (TextView) findViewById(R.id.txtNombreActividad);
        //txtNombreCategoria           = (TextView) findViewById(R.id.txtNombreCategoria);
        //txtNombreTipo                = (TextView) findViewById(R.id.txtNombreTipo);
        txtFechaCreacionDatos        = (TextView) findViewById(R.id.txtFechaCreacionDatos);
        txtFechaRealizacionDatos     = (TextView) findViewById(R.id.txtFechaRealizacionDatos);
        txtVisitasDatos              = (TextView) findViewById(R.id.txtVisitasDatos);
        txtUbicacionDatos            = (TextView) findViewById(R.id.txtUbicacionDatos);
        btnMapaObra                  = (ImageButton) findViewById(R.id.btnMapaObra);
        frmSinDatosContacto          = (FrameLayout) findViewById(R.id.frmSinDatosContacto);
        tblDatosContacto             = (RelativeLayout) findViewById(R.id.tblDatosContacto);
        imgAvatarContactoObra        = (ImageView) findViewById(R.id.imgAvatarContactoObra);
        txtNombreContactoObraDatos   = (TextView) findViewById(R.id.txtNombreContactoObraDatos);
        txtEmailContactoObraDatos    = (TextView) findViewById(R.id.txtEmailContactoObraDatos);
        txtTelefonoContactoObraDatos = (TextView) findViewById(R.id.txtTelefonoContactoObraDatos);
        txtFotosObraContador         = (TextView) findViewById(R.id.txtFotosObraContador);
        layoutFotosObra              = (LinearLayout) findViewById(R.id.layoutFotosObra);
        imgObra0                     = (RoundedImageView) findViewById(R.id.imgObra0);
        imgObra1                     = (RoundedImageView) findViewById(R.id.imgObra1);
        imgObra2                     = (RoundedImageView) findViewById(R.id.imgObra2);
        imgObra3                     = (RoundedImageView) findViewById(R.id.imgObra3);


        tabProfesionalesInteresados   = (LinearLayout) findViewById(R.id.tabProfesionalesInteresados);
        listaProfesionalesInteresados = (ListView) findViewById(R.id.listaProfesionalesInteresados);


        // Por defecto, el avatar muestra la imagen de perfil estándar
        avatarPorDefecto = true;


        // Inicializar estructuras de control de las fotos de la obra
        contadorFotos = 0;
        txtFotosObraContador.setText(Integer.toString(contadorFotos));

        imagenes   = new ArrayList();

        // Si en el futuro se añaden más botones, incluirlos aquí
        imagenes.add(imgObra0); imagenes.add(imgObra1); imagenes.add(imgObra2); imagenes.add(imgObra3);
        maxImagenes = imagenes.size();
        adjuntadas = new boolean[maxImagenes];

        // Inicialmente, todos los botones de imagen son invisibles y no contienen una imagen asociada
        for (int i=0; i<maxImagenes; i++)
        {
            adjuntadas[i] = false;
            imagenes.get(i).setVisibility(View.INVISIBLE);
        }

        // El Layout que contiene los botones de imagen de obra también aparecerá oculto mientras no haya fotos que mostrar
        layoutFotosObra.setVisibility(View.GONE);


        // Comportamiento de los botones de imagen de obra al pulsarlos
        for (int i=0; i<maxImagenes; i++)
        {
            final RoundedImageView botonImagen = imagenes.get(i);
            final int pos = i;

            botonImagen.setOnClickListener
            (
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        accionBotonImagen(pos);
                    }
                }
            );
        }



        // Configuración de las pestañas
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("Tab Datos Obra");
        spec.setContent(R.id.tabDatosObra);
        spec.setIndicator(getResources().getString(R.string.VentanaDatosObra_txt_tabDetalles));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Tab Profesionales Interesados");
        spec.setContent(R.id.tabProfesionalesInteresados);
        spec.setIndicator(getResources().getString(R.string.VentanaDatosObra_txt_tabInteresados));
        tabHost.addTab(spec);

        // Color del título de cada pestaña
        titulo_tab0 = (TextView) tabHost.getTabWidget().getChildTabViewAt(0).findViewById(android.R.id.title);
        titulo_tab1 = (TextView) tabHost.getTabWidget().getChildTabViewAt(1).findViewById(android.R.id.title);
        titulo_tab0.setTextColor(this.getResources().getColorStateList(R.color.texto_tabs));
        titulo_tab1.setTextColor(this.getResources().getColorStateList(R.color.texto_tabs));

        // Por defecto, la ventana abre siempre con la pestaña de datos de la obra
        tabHost.setCurrentTab(0);


        // En principio, siempre se muestra el layout de "no hay datos de contacto" y se oculta el layout de "datos de contacto"
        // (posteriormente, al recibir la respuesta del servidor se verá si esto se mantiene o hay que invertirlo)
        frmSinDatosContacto.setVisibility(View.VISIBLE);
        tblDatosContacto.setVisibility(View.GONE);

        // Título para la ventana
        String tituloActionBar;


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

        // Si quien visualiza la obra es un usuario particular
        if ( vistaParticular )
        {
            // Recuperar la información de si antes estábamos en el listado de obras abiertas o cerradas
            // (por defecto, se considera que abiertas)
            obraAbierta = true;
            if ( info!=null && (info.getSerializable("accion"))!=null )
            {
                VentanaParticular.Accion accion = (VentanaParticular.Accion) info.getSerializable("accion");

                if ( accion== VentanaParticular.Accion.OBRAS_CERRADAS )
                    obraAbierta = false;
            }

            // Titulo de la ventana
            if ( obraAbierta )  tituloActionBar = getResources().getString(R.string.VentanaDatosObra_txt_titulo_obraAbierta);
            else                tituloActionBar = getResources().getString(R.string.VentanaDatosObra_txt_titulo_obraCerrada);

            // Gestor de sesion de usuario particular
            gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PARTICULAR);
        }

        // Si quien visualiza la obra es un usuario profesional
        // (posibles estados de la obra: encontrada en una búsqueda, seguida, adjudicada o completada)
        else
        {
            // Recuperar la información de si antes estábamos en el listado de obras en seguimiento, adjudicadas o completadas
            // (si no es ninguna de las anteriores, se da por hecho que la obra procede del resultado de una búsqueda)
            obraEncontrada = true;
            obraEnSeguimiento = false;
            obraCompletada = false;

            if ( info!=null && (info.getSerializable("accion"))!=null )
            {
                VentanaProfesional.Accion accion = (VentanaProfesional.Accion) info.getSerializable("accion");

                if ( accion== VentanaProfesional.Accion.OBRAS_SEGUIDAS)
                {
                    obraEnSeguimiento = true;
                    obraEncontrada = false;
                }

                else if ( accion== VentanaProfesional.Accion.OBRAS_ADJUDICADAS)
                {
                    obraEncontrada = false;
                }

                else if ( accion== VentanaProfesional.Accion.OBRAS_COMPLETADAS)
                {
                    obraCompletada = true;
                    obraEncontrada = false;
                }
            }

            // Titulo de la ventana
            if ( obraEncontrada )           tituloActionBar = getResources().getString( R.string.VentanaDatosObra_txt_titulo_obraEncontrada);
            else if ( obraEnSeguimiento )   tituloActionBar = getResources().getString( R.string.VentanaDatosObra_txt_titulo_obraEnSeguimiento);
            else if ( obraCompletada )      tituloActionBar = getResources().getString( R.string.VentanaDatosObra_txt_titulo_obraCompletada);
            else                            tituloActionBar = getResources().getString( R.string.VentanaDatosObra_txt_titulo_obraAdjudicada);

            // Gestor de sesion de usuario profesional
            gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PROFESIONAL);

            // Para los profesionales no se muestra la pestaña de seguidores de la obra
            tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.GONE);
        }


        // Configuración de la Action Bar de esta ventana: título, color de fondo y visibilidad
        setTitle( tituloActionBar );
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();

        // Mostrar el icono de volver hacia atrás en la action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Inicialmente, el layout con los datos de la obra está oculto (hasta que se carguen los datos del servidor)
        tabDatosObra.setVisibility(View.GONE);


        // Inicialmente el objeto que contendrá los datos de la obra es null
        datosObra = null;

        // Recuperar la información del id de obra pasada en el intent, si es que se pasó alguna
        // y llamar al servicio web que devuelva información sobre esa obra
        idObra = null;
        if ( info!=null && (idObra=info.getString("id_obra"))!=null )
        {
            solicitarDatosObra();
        }



        // Comportamiento al pulsar la imagen del perfil
        imgAvatarContactoObra.setOnClickListener
        (
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Si el profesional tiene un avatar personalizado, mostrarlo en grande en una nueva ventana
                    if (!avatarPorDefecto) {
                        Bitmap bitmap = ((BitmapDrawable) imgAvatarContactoObra.getDrawable()).getBitmap();
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


        // Comportamiento del botón de Mostrar mapa al pulsarlo
        // (abrir una ventana de la aplicación del sistema asociada para visualizar mapas de Google)
        btnMapaObra.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ArrayList<String> parametrosLlamada = new ArrayList();
                    parametrosLlamada.add(coordenadasObra_lat);
                    parametrosLlamada.add(coordenadasObra_lon);
                    parametrosLlamada.add(txtTituloObra.getText().toString());

                    Utils.aplicacionExterna(estaVentana, Utils.AppExterna.MAPA_COORDENADAS, parametrosLlamada);
                }
            }
        );


        // Comportamiento al pulsar en la ListView de las cabeceras de los profesionales que siguen esa obra
        // Abrir una nueva ventana para mostrar los datos del profesional (si el elemento pulsado representa un profesional)
        listaProfesionalesInteresados.setOnItemClickListener
        (
            new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView parent, View v, int pos, long id)
                {
                    String idProfesional = (String) v.getTag();

                    if (idProfesional != null)
                    {
                        Intent intent = new Intent(estaVentana, VentanaDatosProfesional.class);

                        Bundle info = new Bundle();
                        info.putString("id_profesional", idProfesional);
                        info.putSerializable("tipo_usuario", tipoUsuario);
                        intent.putExtras(info);

                        startActivity(intent);
                    }
                }
            }
        );


        // Al mantener presionado sobre el ListView, se invocará al método onCreateContextMenu de la ventana
        // para mostrar el menú contextual correspondiente
        registerForContextMenu(listaProfesionalesInteresados);


        // Comportamiento al pulsar sobre los TextView de los datos de contacto del solicitante de la obra
        // (se abrirá la aplicación externa correspondiente)
        // ----------------------------------------------------------------------------------------------------------------------------------

        // Al pulsar sobre la dirección de email, abrir la aplicación de correo
        // (solo si es un valor distinto a "n/d")
        txtEmailContactoObraDatos.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    String destinatario = ((TextView) arg0).getText().toString().trim();
                    String asunto = getResources().getString(R.string.VentanaDatosObra_txt_asuntoEmail);

                    if ( ! destinatario.equals("n/d") )
                    {
                        ArrayList<String> parametrosLlamadaExterna = new ArrayList();
                        parametrosLlamadaExterna.add(destinatario);
                        parametrosLlamadaExterna.add(asunto);

                        Utils.aplicacionExterna(estaVentana, Utils.AppExterna.CORREO, parametrosLlamadaExterna);
                    }
                }
            }
        );

        // Al pulsar sobre el teléfono, abrir la aplicación de llamadas
        // (solo si es un valor distinto a "n/d")
        txtTelefonoContactoObraDatos.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View arg0)
                {
                    String telefono = txtTelefonoContactoObraDatos.getText().toString().trim();

                    if ( ! telefono.equals("n/d") )
                    {
                        ArrayList<String> parametrosLlamadaExterna = new ArrayList();
                        parametrosLlamadaExterna.add(telefono);

                        Utils.aplicacionExterna(estaVentana, Utils.AppExterna.LLAMADA, parametrosLlamadaExterna);
                    }
                }
            }
        );

    }



    // Acción que se ejecuta al pulsar uno de los botones de imagen de obra
    private void accionBotonImagen(int pos)
    {
        final RoundedImageView botonImagen = imagenes.get(pos);

        Bitmap bitmap = ((RoundedDrawable)botonImagen.getDrawable()).getSourceBitmap();
        String msg = getResources().getString(R.string.general_txt_imagenObra);

        gestorSesion.setDatosTemporales( Utils.codifica_jpeg_base64(bitmap) );

        Intent intent = new Intent(estaVentana,VentanaMostrarImagen.class);
        Bundle info = new Bundle();
        info.putString("mensaje",msg);
        intent.putExtras(info);

        startActivity(intent);
    }



    // Solicita al servidor los datos de la obra correspondiente, para después mostrarlos en la ventana
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    private void solicitarDatosObra()
    {
        // Si no existen credenciales almacenadas, ir a la ventana de inicio de sesión
        if ( !gestorSesion.hayDatosSesion() )
        {
            Intent intent = new Intent(VentanaDatosObra.this, VentanaLogin.class);

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

            String id_obra = idObra;


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_WORK_INFO);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.WORK_INFO, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("idioma",idioma);
                miServicioWeb.addParam("tipo_usuario",tipo_usuario);
                miServicioWeb.addParam("obra",id_obra);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaGetInfoObras = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaDatosObra", "Solicitando info sobre obra " + idObra + "...");
                tareaGetInfoObras.execute();
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
            Intent intent = new Intent(estaVentana, VentanaLogin.class);

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
                Log.d("VentanaDatosObra", accion +" en favoritos al profesional "+ id_profesional + "...");
                tareaModificarFavoritos.execute();
            }
        }
    }


    // Muestra el diálogo de cierre de obra
    private void dialogo_cerrarObra()
    {
        // Si el objeto que almacena los datos de la obra aún está a null (no se han cargado los datos desde el servidor),
        // entonces no se hace nada
        if ( datosObra == null )
            return;


        // Cuadro de dialogo para escoger a qué profesional adjudicar la obra
        Dialogo_SeleccionSpinner dialogo_CerrarObra;

        // Título del cuádro de diálogo
        String tituloDialogo = getResources().getString( R.string.dialogo_cerrarObra_txt_titulo );

        // Opción por defecto para el spinner del cuadro de diálogo
        String opcionDefecto = getResources().getString( R.string.dialogo_cerrarObra_txt_opcionDefecto );


        // Lista con las posibles opciones (los profesionales que siguen esta obra), inicialmente vacía
        ArrayList<Info_ElementoSpinner> opcionesDialogo = new ArrayList();

        for (int i=0; i<datosObra.getSeguidores().size(); i++)
        {
            Info_CabeceraProfesional seguidor = datosObra.getSeguidores().get(i);

            String id_pro = seguidor.getId();
            String nombre_pro = seguidor.getNombre();

            opcionesDialogo.add( new Info_ElementoSpinner(id_pro,nombre_pro) );
        }


        // Comportamiento de los botones del cuadro de diálogo (Aceptar y Cancelar) al pulsarlos
        Dialogo_SeleccionSpinner.Comportamiento comportamientoDialogo = new Dialogo_SeleccionSpinner.Comportamiento()
        {
            public void elementoSeleccionado(int pos, long id, String item)
            {
                String id_adjudicatario = Long.toString(id);
                cerrarObra(idObra, id_adjudicatario);
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


    // Muestra el diálogo de valorar obra
    private void dialogo_valorarObra()
    {
        // Si el objeto que almacena los datos de la obra aún está a null (no se han cargado los datos desde el servidor),
        // entonces no se hace nada
        if ( datosObra == null )
            return;


        // Cuadro de dialogo para valorar la obra (declaración)
        Dialogo_ValorarObra dialogo_valorarObra;

        // Título del cuádro de diálogo
        String tituloDialogo = getResources().getString( R.string.dialogo_valorarObra_txt_titulo );

        // Id del adjudicatario de la obra
        String idAdjudicatario = datosObra.getAdjudicatario();

        // Si la obra no tiene adjudicatario, no se hace nada
        if ( idAdjudicatario.equals("n/d") )
            return;

        // Nombre del adjudicatario, si existe
        String adjudicatario = "";
        boolean buscar = true;

        for (int i=0; buscar && i<datosObra.getSeguidores().size(); i++)
        {
            Info_CabeceraProfesional seguidor = datosObra.getSeguidores().get(i);

            if ( seguidor.getId().equals(idAdjudicatario) )
            {
                adjudicatario = seguidor.getNombre();
                buscar = false;
            }
        }

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
        // (se mostrará el nombre del adjudicatario de la obra a valorar, como dato informativo al usuario)
        Dialogo_ValorarObra dialogo = new Dialogo_ValorarObra(estaVentana, tituloDialogo, idObra, idAdjudicatario, adjudicatario, comportamientoDialogo);
        dialogo.show();
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
                Log.d("VentanaDatosObra", "Solicitando el cierre de la obra...");
                tareaCerrarObra.execute();
            }
        }
    }



    // Envía una petición de valoración de obra al servidor
    // (si el coste es la cadena vacía, no se enviará este parámetro al servidor)
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
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
                Log.d("VentanaDatosObra", "Registrando valoración de la obra...");
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
                Log.d("VentanaDatosObra", "Solicitando eliminación de la obra...");
                tareaEliminarObra.execute();
            }
        }
    }



    // Solicita al servidor agregar o eliminar un profesional de la lista de seguidores de una obra
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    public void modificarSeguimiento(boolean seguir)
    {
        // Esta acción solo puede ser realizada por un usuario profesional
        if ( tipoUsuario != GestorSesiones.TipoUsuario.PROFESIONAL )
            return;

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
            String id_obra = this.idObra;


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
                // Construir el servicio web correspondiente con los parámetros de entrada
                ServicioWeb miServicioWeb;

                // Dependiendo de si la acción es seguir o no seguir la obra, se invocará a un servicio web o a otro
                if ( seguir )
                    miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.FOLLOW_WORK, estaVentana);
                else
                    miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.UNFOLLOW_WORK, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("obra",id_obra);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaModificarSeguimiento = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea en un hilo aparte del principal
                Log.d("VentanaDatosObra", "Modificando seguimiento ("+ Boolean.toString(seguir) +") para la obra "+ id_obra + "...");
                tareaModificarSeguimiento.execute();
            }
        }

    }



    // Procesar la respuesta de la tarea en segundo plano
    // (según el servicio web que se hubiera invocado)
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

        // Posibles errores del servicio WORK_INFO a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.WORK_INFO, "ERR_ObraInexistente", true, R.string.msg_InfoObra_ERR_ObraInexistente, idTituloOperacion) );

        // Posibles errores del servicio MODIFY_FAVORITES a los que se desea dar un tratamiento particular

        // Posibles errores del servicio CLOSE_WORK a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.CLOSE_WORK, "ERR_ObraInexistente", true, R.string.msg_CerrarObra_ERR_ObraInexistente, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.CLOSE_WORK, "ERR_ObraYaCerrada", false, R.string.msg_CerrarObra_ERR_ObraYaCerrada, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.CLOSE_WORK, "ERR_UsuarioNoAutorizado", false, R.string.msg_CerrarObra_ERR_UsuarioNoAutorizado) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.CLOSE_WORK, "ERR_AdjudicatarioInexistente", false, R.string.msg_CerrarObra_ERR_AdjudicatarioInexistente, idTituloOperacion) );

        // Posibles errores del servicio VOTE_WORK a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.VOTE_WORK, "ERR_OperacionDenegada", true, R.string.msg_ValorarObra_ERR_OperacionDenegada, idTituloOperacion) );

        // Posibles errores del servicio FOLLOW_WORK a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.FOLLOW_WORK, "ERR_NoEsPremium", false, R.string.msg_SeguirObra_ERR_NoEsPremium, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.FOLLOW_WORK, "ERR_ObraInexistente", true, R.string.msg_SeguirObra_ERR_ObraInexistente, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.FOLLOW_WORK, "ERR_ObraYaCerrada", false, R.string.msg_SeguirObra_ERR_ObraYaCerrada, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.FOLLOW_WORK, "ERR_LimiteSeguidores", false, R.string.msg_SeguirObra_ERR_LimiteSeguidores, idTituloOperacion) );

        // Posibles errores del servicio UNFOLLOW_WORK a los que se desea dar un tratamiento particular

        // Posibles errores del servicio REMOVE_WORK a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.REMOVE_WORK, "ERR_OperacionNoAutorizada", false, R.string.msg_EliminarObra_ERR_OperacionNoAutorizada, idTituloOperacion) );
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.REMOVE_WORK, "ERR_OperacionCancelada", false, R.string.msg_EliminarObra_ERR_OperacionCancelada, idTituloOperacion) );


        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana, tarea, idTituloOperacion, email, listaErrores, tipoUsuario);

        // Si la respuesta se recibió correctamente (OK), discriminar a qué servicio web corresponde
        // (de los posibles que se pueden consultar desde esta ventana) y procesarla por separado
        if ( respuesta_OK )
        {
            // Servicio web asociado a la tarea, que contiene la respuesta del servidor
            ServicioWeb miServicioWeb = tarea.getCliente().getServicioWeb();

            if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.WORK_INFO )
            {
                procesarWorkInfo( miServicioWeb.getRespuesta() );
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.MODIFY_FAVORITES )
            {
                procesarModificarFavoritos( miServicioWeb.getRespuesta() , ((Integer)miServicioWeb.getParametroLocal()).intValue() );
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.CLOSE_WORK )
            {
                procesarCierreObra( miServicioWeb.getRespuesta() );
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.VOTE_WORK )
            {
                procesarValoracionObra( miServicioWeb.getRespuesta() );
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.REMOVE_WORK )
            {
                procesarBorradoObra(miServicioWeb.getRespuesta());
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.FOLLOW_WORK )
            {
                procesarModificarSeguimiento( miServicioWeb.getRespuesta(), true);
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.UNFOLLOW_WORK )
            {
                procesarModificarSeguimiento( miServicioWeb.getRespuesta(), false);
            }
        }

    }



    // Procesamiento de la respuesta OK al servicio WORK_INFO
    private void procesarWorkInfo (RespuestaServicioWeb respuesta)
    {
        // Determina si se muestran los Views con los datos de contacto o no
        // (solo para el propio usuario que solicita la obra y para los profesionales que ya la siguen)
        // (NOTA: esto solo afecta a la parte visual, independientemente de si la respuesta del servidor contiene esos datos o no)
        boolean mostrarDatosContacto = false;

        if ( respuesta.getDetalle().equals("OK_ParticularSolicitante") || respuesta.getDetalle().equals("OK_ProfesionalSeguidor") )
            mostrarDatosContacto = true;

        // Referencia al objeto que contiene los datos de la obra
        datosObra = (Info_Obra) (respuesta.getContenido().get(0));


        // Datos de la obra --> pestaña TabDatosObra
        txtTituloObra.setText( datosObra.getTitulo() );
        txtFotosObraContador.setText("0");
        //btnFotosObra.
        String clasificacion = datosObra.getActividad() +" > "+ datosObra.getCategoria() +" > "+ datosObra.getTipo();
        txtClasificacionObra.setText( clasificacion );
        txtDescripcionObra.setText( "\""+ datosObra.getDetalle() +"\"" );
        //txtNombreActividad.setText( obra.getActividad() );
        //txtNombreCategoria.setText( obra.getCategoria() );
        //txtNombreTipo.setText( obra.getTipo() );
        txtFechaCreacionDatos.setText( datosObra.getFechaSolicitud() );
        txtFechaRealizacionDatos.setText( datosObra.getFechaRealizacion() );
        txtVisitasDatos.setText( Integer.toString(datosObra.getVisitas()) );

        // Cargar los datos de contacto recibidos y mostrar su layout correspondiente (si procede)
        if ( mostrarDatosContacto )
        {
            frmSinDatosContacto.setVisibility(View.GONE);
            tblDatosContacto.setVisibility(View.VISIBLE);

            if ( ! datosObra.getAvatar().equals("default_profile_pic") )
            {
                imgAvatarContactoObra.setImageBitmap(Utils.descodifica_imagen_base64(datosObra.getAvatar()));
                avatarPorDefecto = false;
            }

            txtNombreContactoObraDatos.setText(datosObra.getNombreContacto());
            txtEmailContactoObraDatos.setText(datosObra.getEmailContacto());
            txtTelefonoContactoObraDatos.setText(datosObra.getTelefContacto());

            // Si no se especificó alguno de los datos de contacto, mostrarlo en color negro.
            // En otro caso, mostrarlo en color azul (por defecto) y subrayado
            if ( datosObra.getEmailContacto().equals("n/d") )   txtEmailContactoObraDatos.setTextColor( getResources().getColor(R.color.texto_general) );
            else                                                txtEmailContactoObraDatos.setPaintFlags(txtEmailContactoObraDatos.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            if ( datosObra.getTelefContacto().equals("n/d") )   txtTelefonoContactoObraDatos.setTextColor( getResources().getColor(R.color.texto_general) );
            else                                                txtTelefonoContactoObraDatos.setPaintFlags(txtTelefonoContactoObraDatos.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        // Guardar la población y las coordenadas de la obra
        coordenadasObra_lat = Double.toString(datosObra.getLatitud());
        coordenadasObra_lon = Double.toString( datosObra.getLongitud() );

        // Determinar si la ubicación fue indicada en base a la poblacion o en base a las coordenadas concretas
        // (si está basada en la población, se antepondrá el texto "Cerca de..." a la dirección en texto)
        String texto_ubicacion = Utils.obtenerDireccion(datosObra.getLatitud(), datosObra.getLongitud(), estaVentana);

        if ( ! datosObra.getPoblacion().equals("0") )
            texto_ubicacion = getResources().getString(R.string.VentanaDatosObra_txt_cercaDe) +" "+ texto_ubicacion;

        txtUbicacionDatos.setText( texto_ubicacion );



        // Mostrar las imágenes de la obra
        // (si por un casual se reciben mas imágenes que botones de imagen hay en la ventana, las imagenes de más no se mostrarán)
        ArrayList<String> imagenes_base64 = datosObra.getImagenes();

        int numImagenes = (imagenes_base64.size()>maxImagenes)?maxImagenes:imagenes_base64.size();

        for (int i=0; i<numImagenes; i++)
        {
            Bitmap imagen = Utils.descodifica_imagen_base64(imagenes_base64.get(i));

            imagenes.get(i).setImageBitmap(imagen);
            imagenes.get(i).setScaleType(ImageView.ScaleType.CENTER_CROP);
            imagenes.get(i).setVisibility(View.VISIBLE);

            contadorFotos++;
        }

        txtFotosObraContador.setText(Integer.toString(contadorFotos));

        // Si hay alguna foto que mostrar, hacer visible el layout que contiene los botones de las fotos
        if ( contadorFotos > 0 )
            layoutFotosObra.setVisibility(View.VISIBLE);



        // Datos de los profesionales que siguen la obra --> pestaña tabProfesionalesInteresados
        ArrayList<Info_CabeceraProfesional> interesados = datosObra.getSeguidores();
        int tam = interesados.size();

        // Actualizar el título de la pestaña, para incluir el número de seguidores
        String txt = getResources().getString(R.string.VentanaDatosObra_txt_tabInteresados) +" ("+ Integer.toString(tam) +")";
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        ((TextView) tabHost.getTabWidget().getChildTabViewAt(1).findViewById(android.R.id.title)).setText(txt);

        // Si no hay elementos en la lista, mostrar un único elemento con un mensaje usando un ArrayAdapter de Strings
        if (tam < 1)
        {
            String[] titulosProfesionales = new String[1];

            titulosProfesionales[0] = getResources().getString( R.string.VentanaDatosObra_txt_sinSeguidores );

            ArrayAdapter miAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titulosProfesionales)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTextColor( estaVentana.getResources().getColor(R.color.texto_tabs) );

                    return textView;
                }
            };

            listaProfesionalesInteresados.setAdapter(miAdapter);

            // Deshabilitar el click sobre el ListView
            listaProfesionalesInteresados.setSelector(android.R.color.transparent);
        }

        // Si hay al menos una cabecera de profesional, mostrar sus datos usando un Adapter personalizado de Info_CabeceraProfesional
        else
        {
            String adjudicatario = datosObra.getAdjudicatario();

            Adapter_CabeceraProfesional miAdapter = new Adapter_CabeceraProfesional(estaVentana, R.layout.elemento_cabecera_profesional, interesados, adjudicatario);
            listaProfesionalesInteresados.setAdapter(miAdapter);
        }


        // Eliminar del menu de la action bar la opción de valorar la obra, en caso de que la obra no necesite valoración
        // (bien porque es una obra abierta, bien porque no se adjudicó a nadie al cerrarla, o bien porque ya se hizo una valoración de la misma)
        if ( ! datosObra.faltaValoracion() )
            menuActionBar.removeItem(R.id.menu_valorar_obra);


        // Una vez que se han cargado todos los datos de la obra, mostrar el layout de la ventana con los datos de la obra
        tabDatosObra.setVisibility(View.VISIBLE);
    }



    // Procesamiento de la respuesta OK al servicio MODIFY_FAVORITES
    private void procesarModificarFavoritos (RespuestaServicioWeb respuesta, int pos)
    {
        Info_CabeceraProfesional infoPro = null;

        try
        {
            infoPro = (Info_CabeceraProfesional) listaProfesionalesInteresados.getItemAtPosition(pos);
        }
        catch (ClassCastException ex)
        {
            Log.e("BuscarFavoritos","Error al hacer un cast a Info_CabeceraProfesional desde el adapter (pos: "+ Integer.toString(pos) +")");
            return;
        }

        // Si la petición fue de agregar un profesional a favoritos
        if ( respuesta.getDetalle().equals("OK_FavoritoAgregado") )
        {
            infoPro.setFavorito(true);
            ((Adapter_CabeceraProfesional) listaProfesionalesInteresados.getAdapter()).notifyDataSetChanged();

            String msg = getResources().getString(R.string.msg_AgregarFavorito_OK_FavoritoAgregado);
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
        }
        // Si la petición fue de eliminar un profesional de favoritos
        else if ( respuesta.getDetalle().equals("OK_FavoritoEliminado") )
        {
            infoPro.setFavorito(false);
            ((Adapter_CabeceraProfesional) listaProfesionalesInteresados.getAdapter()).notifyDataSetChanged();

            String msg = getResources().getString(R.string.msg_EliminarFavorito_OK_FavoritoEliminado);
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
        }
    }



    // Procesamiento de la respuesta OK al servicio CLOSE_WORK
    private void procesarCierreObra(RespuestaServicioWeb respuesta)
    {
        // Mostrar confirmación al usuario
        String msg = getResources().getString(R.string.msg_CerrarObra_OK_ObraCerrada);
        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);

        // Cambiar el título de la ventana (obra abierta -> cerrada)
        String tituloActionBar = getResources().getString(R.string.VentanaDatosObra_txt_titulo_obraCerrada);
        setTitle(tituloActionBar);

        // Eliminar del menu de la action bar las opciones para obras abiertas, y reemplazarlas por las opciones para obras cerradas
        menuActionBar.removeItem(R.id.menu_cerrar_obra);
        menuActionBar.removeItem(R.id.menu_eliminar_obra);
        menuActionBar.removeItem(R.id.menu_cerrar_sesion);

        obraAbierta = false;
        onCreateOptionsMenu(menuActionBar);


        // Volver a mostrar la pestaña de datos de la obra (si no se estaba mostrando ya)
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setCurrentTab(0);

        // Volver a cargar los datos de la obra (ahora cerrada) en la ventana
        solicitarDatosObra();
    }



    // Procesamiento de la respuesta OK al servicio VOTE_WORK
    private void procesarValoracionObra(RespuestaServicioWeb respuesta)
    {
        // Mostrar confirmación al usuario
        String msg = getResources().getString(R.string.msg_ValorarObra_OK_ValoracionRealizada);
        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);

        // Eliminar del menu de la action bar la opción de valorar la obra
        menuActionBar.removeItem( R.id.menu_valorar_obra );
    }



    // Procesamiento de la respuesta OK a los servicios FOLLOW_WORK y UNFOLLOW_WORK
    private void procesarModificarSeguimiento (RespuestaServicioWeb respuesta, boolean seguir)
    {
        // Si la petición fue de seguir una obra
        if ( seguir )
        {
            String tituloActionBar = getResources().getString( R.string.VentanaDatosObra_txt_titulo_obraEnSeguimiento);
            setTitle( tituloActionBar );

            obraEncontrada = false;
            obraEnSeguimiento = true;
            onCreateOptionsMenu(menuActionBar);

            String msg = getResources().getString(R.string.msg_SeguirObra_OK_ObraSeguida);
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
        }

        // Si la petición fue de olvidar una obra
        else
        {
            String tituloActionBar = getResources().getString( R.string.VentanaDatosObra_txt_titulo_obraEncontrada);
            setTitle(tituloActionBar);

            obraEncontrada = true;
            obraEnSeguimiento = false;
            onCreateOptionsMenu(menuActionBar);

            String msg = getResources().getString(R.string.msg_OlvidarObra_OK_ObraOlvidada);
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
        }
    }



    // Procesamiento de la respuesta OK al servicio REMOVE_WORK
    private void procesarBorradoObra(RespuestaServicioWeb respuesta)
    {
        // Mostrar confirmación al usuario
        String msg = getResources().getString(R.string.msg_EliminarObra_OK_ObraEliminada);
        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);

        // Cerrar la ventana con los datos de la obra
        finish();
    }



    // Método invocado para mostrar el menú contextual de algún elemento de la página
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        // Si el elemento en cuestión es el ListView que contiene los profesionales interesados en la obra
        if ( v.getId()==R.id.listaProfesionalesInteresados)
        {
            // En ese caso, el objeto menuInfo será del subtipo AdapterContextMenuInfo
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            // Título y textos de las opciones del menú a mostrar
            String tituloMenu = "";
            String[] opcionesMenu = null;

            // Obtener una referencia a los datos del objeto de la lista, a traves de su adapter
            // (será un objeto Info_CabeceraProfesional)
            // Si el listView no contiene elementos (solo un texto indicando que está vacío),
            // entonces al hacer el casting saltará una excepcion que hemos de capturar
            Object datosElementoSeleccionado = listaProfesionalesInteresados.getAdapter().getItem(info.position);

            try
            {
                tituloMenu = ((Info_CabeceraProfesional) datosElementoSeleccionado).getNombre();

                boolean esFavorito = ((Info_CabeceraProfesional) datosElementoSeleccionado).esFavorito();

                if (esFavorito)
                    opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaDatosObra_cabeceraProfesional_esFavorito);
                else
                    opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaDatosObra_cabeceraProfesional_noEsFavorito);
            }
            catch (ClassCastException ex)
            {
                opcionesMenu = null;
            }


            if (opcionesMenu != null)
            {
                menu.setHeaderTitle(tituloMenu);

                // Cargar cada una de las opciones en el menú
                for (int i = 0; i < opcionesMenu.length; i++)
                    menu.add(Menu.NONE,i,i,opcionesMenu[i]);
            }
        }

    }



    // Accion a realizar al seleccionar un elemento del menú contexual
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
        // (según los datos que muestre la lista en ese momento, será un objeto Info_CabeceraProfesional)
        // Si el listView no contiene elementos (solo un texto indicando que está vacío),
        // entonces al hacer el casting saltará una excepcion que hemos de capturar
        Object datosElementoSeleccionado = listaProfesionalesInteresados.getItemAtPosition(posLista);

        try
        {
            Info_CabeceraProfesional infoPro = (Info_CabeceraProfesional) datosElementoSeleccionado;
            String idPro =  infoPro.getId();

            // Opción de ver detalles del profesional
            if (indiceMenu == 0)
            {
                Intent intent = new Intent(estaVentana, VentanaDatosProfesional.class);

                Bundle b = new Bundle();
                b.putString("id_profesional", idPro);
                b.putSerializable("tipo_usuario", tipoUsuario);
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
        catch (ClassCastException ex)
        {
        }

        return true;
    }



    // Fija el texto y el color de texto para el título de la Action Bar
    @Override
    public void setTitle(CharSequence titulo)
    {
        String color = Integer.toHexString(getResources().getColor(R.color.texto_actionBar) & 0x00ffffff);
        tituloVentana = titulo;

        getSupportActionBar().setTitle(Html.fromHtml("<font color='" + color + "'>" + tituloVentana + "</font>"));
    }


    // Configura el menú de la barra superior de la ventana
    // (será diferente dependiendo del usuario que la visualiza y del estado de la obra)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if ( menuActionBar == null )
            menuActionBar = menu;

        // Limpiar el menú desplegable de la ventana (si lo había)
        menu.clear();


        int opciones_menu_actionBar;

        // Si el usuario que visualiza la obra es un particular
        if (vistaParticular)
        {
            if (obraAbierta)
                opciones_menu_actionBar = R.menu.menu_ventana_datos_obra_particular_abierta;
            else
                opciones_menu_actionBar = R.menu.menu_ventana_datos_obra_particular_cerrada;
        }

        // Si el usuario que visualiza la obra es un profesional
        else
        {
            // Menú de obra encontrada en la ventana de búsqueda de obras
            if ( obraEncontrada )
                opciones_menu_actionBar = R.menu.menu_ventana_datos_obra_profesional_encontrada;

            // Menú de obra que ya está siendo seguida por el profesional
            else if ( obraEnSeguimiento )
                opciones_menu_actionBar = R.menu.menu_ventana_datos_obra_profesional_seguida;

            // Menú de obra adjudicada o completada
            else
                opciones_menu_actionBar = R.menu.menu_ventana_datos_obra_profesional_adjudicada_completada;
        }

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


    // Comportamiento al pulsar los elementos de la action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        String titulo, msg;

        switch ( item.getItemId() )
        {
            // Botón home de la action bar (volver atras)
            case android.R.id.home:

                onBackPressed();
                return true;


            // Opción de cerrar la obra (solo usuarios particulares)
            case R.id.menu_cerrar_obra:

                dialogo_cerrarObra();
                return true;


            // Opción de borrar la obra (solo usuarios particulares)
            case R.id.menu_eliminar_obra:

                titulo = getResources().getString(R.string.dialogo_eliminarObra_txt_titulo);
                msg = getResources().getString(R.string.dialogo_eliminarObra_txt_pregunta);
                Utils.dialogo_EliminarObra(estaVentana, titulo, msg, Utils.CategoriaDialogo.ADVERTENCIA, idObra);
                return true;


            // Opción de valorar la obra (solo usuarios particulares)
            case R.id.menu_valorar_obra:

                dialogo_valorarObra();
                return true;


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


            // Opción de seguir la obra (solo usuarios profesionales)
            case R.id.menu_seguir_obra:

                modificarSeguimiento(true);

                return true;


            // Opción de dejar de seguir la obra (solo usuarios profesionales)
            case R.id.menu_ignorar_obra:

                modificarSeguimiento(false);

                return true;


            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
