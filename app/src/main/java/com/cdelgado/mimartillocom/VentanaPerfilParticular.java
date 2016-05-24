package com.cdelgado.mimartillocom;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

import org.apache.http.message.BasicNameValuePair;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;



public class VentanaPerfilParticular extends VentanaBase implements AdapterView.OnItemSelectedListener
{
    // Clases de datos a cargar en los spinners de la ventana
    public static enum ClaseParametro
    {
        PROVINCIAS,
        POBLACIONES
    }

    // Secciones en que se divide esta ventana (para modificar la configuración en el servidor)
    // NOTA: no se incluye la sección de "Seguridad de la conexión" ya que es un ajuste local del cliente
    public static enum Secciones
    {
        CABECERA,
        PASSWORD,
        CONTACTO,
        OTROS
    }


    private GestorSesiones gestorSesion;

    protected ProgressDialog pDialog;
    private VentanaPerfilParticular estaVentana;

    private SimpleDateFormat formateadorFecha;
    private DatePickerDialog dialogoFecha;

    boolean usuarioSeleccionoUnaProvincia;

    ImageView imgAvatar;
    ImageButton btnEliminarAvatar;
    TextView txtEmail;

    FrameLayout layoutPasswordCabecera;
    LinearLayout layoutPasswordDatos;
    EditText txtPasswordActual;
    EditText txtPasswordNuevo1;
    EditText txtPasswordNuevo2;
    Button btnPassword;

    FrameLayout layoutSeguridadCabecera;
    LinearLayout layoutSeguridadDatos;
    Switch swSeguridad;
    TextView txtEstadoSeguridad;

    FrameLayout layoutContactoCabecera;
    LinearLayout layoutContactoDatos;
    EditText txtNombre;
    EditText txtTelefono1;
    EditText txtTelefono2;
    Spinner spnProvincia;
    Spinner spnPoblacion;
    Button btnContacto;

    FrameLayout layoutOtrosCabecera;
    LinearLayout layoutOtrosDatos;
    EditText txtNacimiento;
    ImageView imgNacimiento;
    Spinner spnSexo;
    RadioGroup rgrPublicidad;
    RadioButton rbtnPublicidadSi;
    RadioButton rbtnPublicidadNo;
    Spinner spnIdioma;
    Button btnOtros;

    private CharSequence tituloVentana;
    private boolean avatarPorDefecto;

    final int REQ_CODE_ESCOGER_IMAGEN_GALERIA = 2;
    private static final int ASPECTO_X = 1;
    private static final int ASPECTO_Y = 1;
    private static final int ANCHO_PX = 256;
    private static final int ALTO_PX = 256;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_perfil_particular);


        // Referencia al propio objeto activity
        estaVentana = this;

        // Cuadro de progreso (para mostrar durante las consultas por red)
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // Referencia a los elementos de la ventana
        imgAvatar				= (ImageView)findViewById(R.id.perfPart_imgAvatar);
        btnEliminarAvatar       = (ImageButton)findViewById(R.id.perfPart_btnEliminarAvatar);
        txtEmail				= (TextView)findViewById(R.id.perfPart_txtEmail);

        layoutPasswordCabecera	= (FrameLayout)findViewById(R.id.perfPart_layoutPasswordCabecera);
        layoutPasswordDatos	    = (LinearLayout)findViewById(R.id.perfPart_layoutPasswordDatos);
        txtPasswordActual		= (EditText)findViewById(R.id.perfPart_txtPasswordActual);
        txtPasswordNuevo1		= (EditText)findViewById(R.id.perfPart_txtPasswordNuevo1);
        txtPasswordNuevo2		= (EditText)findViewById(R.id.perfPart_txtPasswordNuevo2);
        btnPassword			    = (Button)findViewById(R.id.perfPart_btnPassword);

        layoutSeguridadCabecera = (FrameLayout)findViewById(R.id.perfPart_layoutSeguridadCabecera);
        layoutSeguridadDatos    = (LinearLayout)findViewById(R.id.perfPart_layoutSeguridadDatos);
        swSeguridad             = (Switch)findViewById(R.id.perfPart_swSeguridad);
        txtEstadoSeguridad      = (TextView)findViewById(R.id.perfPart_txtEstadoSeguridad);

        layoutContactoCabecera	= (FrameLayout)findViewById(R.id.perfPart_layoutContactoCabecera);
        layoutContactoDatos	    = (LinearLayout)findViewById(R.id.perfPart_layoutContactoDatos);
        txtNombre				= (EditText)findViewById(R.id.perfPart_txtNombre);
        txtTelefono1			= (EditText)findViewById(R.id.perfPart_txtTelefono1);
        txtTelefono2			= (EditText)findViewById(R.id.perfPart_txtTelefono2);
        spnProvincia			= (Spinner)findViewById(R.id.perfPart_spnProvincia);
        spnPoblacion			= (Spinner)findViewById(R.id.perfPart_spnPoblacion);
        btnContacto			    = (Button)findViewById(R.id.perfPart_btnContacto);

        layoutOtrosCabecera	    = (FrameLayout)findViewById(R.id.perfPart_layoutOtrosCabecera);
        layoutOtrosDatos		= (LinearLayout)findViewById(R.id.perfPart_layoutOtrosDatos);
        txtNacimiento			= (EditText)findViewById(R.id.perfPart_txtNacimiento);
        imgNacimiento           = (ImageView)findViewById(R.id.perfPart_imgNacimiento);
        spnSexo				    = (Spinner)findViewById(R.id.perfPart_spnSexo);
        rgrPublicidad			= (RadioGroup)findViewById(R.id.perfPart_rgrPublicidad);
        rbtnPublicidadSi		= (RadioButton)findViewById(R.id.perfPart_rbtnPublicidadSi);
        rbtnPublicidadNo		= (RadioButton)findViewById(R.id.perfPart_rbtnPublicidadNo);
        spnIdioma               = (Spinner)findViewById(R.id.perfPart_spnIdioma);
        btnOtros				= (Button)findViewById(R.id.perfPart_btnOtros);


        // Valores del spinner de genero
        ArrayList<Info_ElementoSpinner> opcionesGenero = new ArrayList();
        opcionesGenero.add(new Info_ElementoSpinner("1", getResources().getString(R.string.VentanaPerfil_txt_sexoMasculino)));
        opcionesGenero.add(new Info_ElementoSpinner("2", getResources().getString(R.string.VentanaPerfil_txt_sexoFemenino)));
        String opcionPorDefecto = getResources().getString(R.string.VentanaPerfil_txt_sexoSinEspecificar);

        spnSexo.setAdapter( new Adapter_Spinner(estaVentana,R.layout.elemento_spinner,opcionesGenero,opcionPorDefecto) );


        // Spinner de idioma para las notificaciones (los valores disponibles se indican en el propio adapter)
        spnIdioma.setAdapter(new Adapter_SpinnerIdiomas(estaVentana));


        // Inicialmente, se muestra la imagen del avatar por defecto
        avatarPorDefecto = true;
        btnEliminarAvatar.setVisibility(View.INVISIBLE);


        // Por defecto, los layouts de todas las subsecciones aparecen "plegados"
        layoutPasswordDatos.setVisibility(View.GONE);
        layoutSeguridadDatos.setVisibility(View.GONE);
        layoutContactoDatos.setVisibility(View.GONE);
        layoutOtrosDatos.setVisibility(View.GONE);


        // Gestor de sesion de usuario particular
        gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PARTICULAR);


        // Consultar en las preferencias generales del cliente el protocolo de conexión que se usa actualmente
        // y actualizar la interfaz
        if ( gestorSesion.usarConexionSegura() )
        {
            swSeguridad.setChecked(true);
            txtEstadoSeguridad.setText( getResources().getString(R.string.VentanaPerfil_txt_seguridadConexionActivada) );
            txtEstadoSeguridad.setTextColor( getResources().getColor(R.color.verde) );
        }
        else
        {
            swSeguridad.setChecked(false);
            txtEstadoSeguridad.setText( getResources().getString(R.string.VentanaPerfil_txt_seguridadConexionDesactivada) );
            txtEstadoSeguridad.setTextColor( getResources().getColor(R.color.rojo) );
        }

        // Comportamiento del switch del protocolo de conexión al cambiar de estado
        // (registrar el cambio en las preferencias generales de la aplicación y actualizar la interfaz)
        swSeguridad.setOnCheckedChangeListener
        (
            new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean activarSeguridad)
                {
                    gestorSesion.setConexionSegura(activarSeguridad);

                    if (activarSeguridad)
                    {
                        txtEstadoSeguridad.setText( getResources().getString(R.string.VentanaPerfil_txt_seguridadConexionActivada) );
                        txtEstadoSeguridad.setTextColor( getResources().getColor(R.color.verde) );
                    }
                    else
                    {
                        txtEstadoSeguridad.setText( getResources().getString(R.string.VentanaPerfil_txt_seguridadConexionDesactivada) );
                        txtEstadoSeguridad.setTextColor( getResources().getColor(R.color.rojo) );
                    }
                }
            }
        );



        // Configuración de la Action Bar de esta ventana: título, color de fondo y visibilidad
        String tituloActionBar = getResources().getString(R.string.VentanaPerfil_txt_titulo);
        setTitle(tituloActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();

        // Mostrar el icono de volver hacia atrás en la action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Comportamiento de los spinners al escoger un elemento de la lista
        // (requiere que la ventana implemente AdapterView.OnItemSelectedListener)
        spnSexo.setOnItemSelectedListener(this);
        spnProvincia.setOnItemSelectedListener(this);
        spnPoblacion.setOnItemSelectedListener(this);


        // Inicialmente, los spinners de provincias y población están deshabilitados
        // (mientras no se cargue datos en ellos)
        spnProvincia.setEnabled(false);
        spnPoblacion.setEnabled(false);


        // Inicialmente, el flag que indica si en algún momento el usuario seleccionó
        // una provincia distinta a la que está en su perfil está a false
        usuarioSeleccionoUnaProvincia = false;


        // Cargar del servidor los datos actuales del perfil del usuario
        // (incluyendo los datos de los spinners)
        cargarDatosUsuario();


        // Configurar el cuadro de diálogo de elección de fecha (para la fecha de nacimiento)

        // La fecha de nacimiento se mostrará en el formato correspondiente al idioma en que se use la aplicación
        formateadorFecha = Utils.nuevoSimpleDateFormat( Utils.idiomaAplicacion() );

        // Anular la entrada directa en el campo de texto
        txtNacimiento.setInputType(InputType.TYPE_NULL);

        Calendar miCalendario = Calendar.getInstance();

        dialogoFecha = new DatePickerDialog(this,
            new DatePickerDialog.OnDateSetListener()
            {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                {
                    Calendar fecha = Calendar.getInstance();
                    fecha.set(year, monthOfYear, dayOfMonth);
                    txtNacimiento.setText( formateadorFecha.format( fecha.getTime()) );
                }

            }
            ,miCalendario.get(Calendar.YEAR), miCalendario.get(Calendar.MONTH), miCalendario.get(Calendar.DAY_OF_MONTH));


        // Comportamiento de la imagen de la fecha de nacimiento al pulsarla
        // (mostrar el cuadro de diáologo de elección de fecha)
        imgNacimiento.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialogoFecha.show();
                }
            }
        );


        // Comportamiento al pulsar la imagen del perfil
        imgAvatar.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    accion_imagenPerfil();
                }
            }
        );


        // Comportamiento del botón de eliminar imagen del perfil
        btnEliminarAvatar.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    avatarPorDefecto = true;
                    imgAvatar.setImageResource(R.drawable.default_avatar);
                    btnEliminarAvatar.setVisibility(View.INVISIBLE);
                    actualizarPerfil(Secciones.CABECERA);
                }
            }
        );


        // Comportamiento del botón de cambio de password
        btnPassword.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    actualizarPerfil(Secciones.PASSWORD);
                }
            }
        );


        // Comportamiento del botón de actualizar información de contacto
        btnContacto.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    actualizarPerfil(Secciones.CONTACTO);
                }
            }
        );


        // Comportamiento del botón de actualizar otros datos
        btnOtros.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    actualizarPerfil(Secciones.OTROS);
                }
            }
        );

    }



    // Comportamiento al seleccionar un elemento de un spinner
    // Si se selecciona la posición (0) en el spinner de provincias, desactivar el sub-spinner correspondiente
    // Si se selecciona otra posición, cargar los valores correspondientes a esa opción en los sub-spinner
    // (no hay acción cuando se selecciona un elemento de poblaciones, ya que no tiene sub-spinners que dependan de él)
    // (tampoco hay acción si la seleccion de elemento se realiza al cargar los datos del servidor en el spinner,
    // es decir, si usuarioSeleccionoUnaProvincia = false)
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int pos, long id)
    {
        Spinner sp;

        if ( parent instanceof Spinner)
            sp = (Spinner) parent;
        else
            return;


        if ( sp == spnProvincia )
            // Si no se selecciona ninguna provincia, el spinner de poblaciones se desactiva
            if ( id==0 )
                desactivarSpinner(spnPoblacion, getResources().getString( R.string.spinner_txt_sinEspecificar ) );

            // Si se selecciona alguna provincia, cargar sus correspondientes poblaciones
            // (siempre que se trate de una selección de provincia hecha por el usuario)
            else
                if ( usuarioSeleccionoUnaProvincia )
                    cargarSpinner(ClaseParametro.POBLACIONES, Long.toString(id));

                // Si la seleccion de provincia la hizo la aplicación al cargar los datos del servidor, no se hace nada
                // pero la siguiente vez que se invoque el método ya se aplicará la acción de arriba
                else
                    usuarioSeleccionoUnaProvincia = true;
    }


    // Comportamiento al seleccionar ningún elemento de un spinner (no hacer nada)
    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        // TODO Auto-generated method stub
    }





    // Deshabilita el spinner y lo vacia de elementos, solo deja el texto indicado en la posición 0
    // (método de clase privado)
    private void desactivarSpinner(Spinner sp, String txt)
    {
        ArrayList<Info_ElementoSpinner> listaVacia = new ArrayList();

        sp.setAdapter(new Adapter_Spinner(estaVentana, R.layout.elemento_spinner, listaVacia, txt));
        sp.setEnabled(false);
    }


    // Comportamiento de los FrameLayout de cabecera de cada sección al pulsarlos
    // (este método se define en la propiedad onClick de esos layouts)
    public void onClick_LayoutSecciones(View v)
    {
        if (!(v instanceof FrameLayout))
            return;

        FrameLayout layout = (FrameLayout) v;

        if (layout == layoutPasswordCabecera)
        {
            layoutPasswordDatos.setVisibility(View.VISIBLE);
            layoutSeguridadDatos.setVisibility(View.GONE);
            layoutContactoDatos.setVisibility(View.GONE);
            layoutOtrosDatos.setVisibility(View.GONE);
        }

        else if (layout == layoutSeguridadCabecera)
        {
            layoutPasswordDatos.setVisibility(View.GONE);
            layoutSeguridadDatos.setVisibility(View.VISIBLE);
            layoutContactoDatos.setVisibility(View.GONE);
            layoutOtrosDatos.setVisibility(View.GONE);
        }

        else if (layout == layoutContactoCabecera)
        {
            layoutPasswordDatos.setVisibility(View.GONE);
            layoutSeguridadDatos.setVisibility(View.GONE);
            layoutContactoDatos.setVisibility(View.VISIBLE);
            layoutOtrosDatos.setVisibility(View.GONE);
        }

        else if (layout == layoutOtrosCabecera)
        {
            layoutPasswordDatos.setVisibility(View.GONE);
            layoutSeguridadDatos.setVisibility(View.GONE);
            layoutContactoDatos.setVisibility(View.GONE);
            layoutOtrosDatos.setVisibility(View.VISIBLE);

            txtNacimiento.requestFocus();
        }
    }


    // Acción a realizar al pulsar la imágen del perfil
    private void accion_imagenPerfil()
    {
        // Si el ImageView de perfil aún tiene asignada la imagen por defecto, abrir aplicación de galería o similar para escoger otra
        // (se pedirá al usuario que la recorte para ajustarla a la relación de aspecto adecuada)
        // El tratamiento de la imagen devuelta se realizará en el método onActivityResult()
        if ( avatarPorDefecto )
        {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);

            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("outputX", ANCHO_PX);
            intent.putExtra("outputY", ALTO_PX);
            intent.putExtra("aspectX", ASPECTO_X);
            intent.putExtra("aspectY", ASPECTO_Y);
            intent.putExtra("return-data", false);  // esto hará que no se devuelvan los datos de la imagen recortada (por si es demasiado grande)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getTempUri());   // la imagen recortada se almacenará en un fichero en esta uri
            intent.putExtra("output", Utils.getTempUri());

            startActivityForResult(intent, REQ_CODE_ESCOGER_IMAGEN_GALERIA);
        }

        // Si el no, mostrarla en grande en una ventana nueva
        else
        {
            Bitmap bitmap = ((BitmapDrawable)imgAvatar.getDrawable()).getBitmap();
            String msg = getResources().getString(R.string.general_txt_imagenPerfil);

            gestorSesion.setDatosTemporales( Utils.codifica_jpeg_base64(bitmap) );

            Intent intent = new Intent(estaVentana,VentanaMostrarImagen.class);
            Bundle info = new Bundle();
            info.putString("mensaje",msg);
            intent.putExtras(info);

            startActivity(intent);
        }
    }


    // Recoge el resultado devuelto por la aplicación de seleccionar y recortar imágen del dispositivo
    // Si este resultado es correcto, actualiza el ImageView del perfil y solicita al servidor que registre la nueva imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Si se trata de asignar como imagen de perfil una imagen previamente seleccionada
        if (requestCode == REQ_CODE_ESCOGER_IMAGEN_GALERIA)
        {
            // Si no se pudo seleccionar una imagen, mostrar mensaje de aviso y no hacer nada
            if (resultCode != Activity.RESULT_OK)
            {
                String msg = getResources().getString(R.string.VentanaPerfil_txt_msgImagenNoSeleccionada);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
                return;
            }

            // Si se seleccionó una imagen, se encontrará guardada en un fichero temporal
            else
            {
                Uri tempFileUri = Utils.getTempUri();
                Bitmap bitmap = null;

                try
                {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(tempFileUri));
                }
                catch (FileNotFoundException e)
                {
                    Log.e("VentanaPerfilParticular", "Fallo al leer el fichero temporal: " + e.toString());
                }

                // Si no pudo construirse un bitmap con los datos del fichero temporal, mostrar mensaje de error
                if (bitmap == null)
                {
                    Log.e("VentanaPerfilParticular", "Fallo al decodificar la imagen guardada en el fichero temporal");

                    String msg = getResources().getString(R.string.general_txt_msgFalloSeleccionImagen);
                    Utils.mostrarMensaje(this, msg, Utils.TipoMensaje.TOAST, null, null);
                }

                // Si la operación fue correcta, mostrar la imagen escogida como avatar,
                // mostrar el botón de eliminar avatar y actualizar el avatar en el servidor
                else
                {
                    imgAvatar.setImageBitmap(bitmap);
                    avatarPorDefecto = false;
                    btnEliminarAvatar.setVisibility(View.VISIBLE);

                    actualizarPerfil(Secciones.CABECERA);
                }

                // Por último, eliminar el fichero temporal (si existía)
                Utils.removeTempFile();
            }
        }
    }


    // Llamar al servicio web que carga los datos del perfil del usuario
    // (NOTA: este método solo lanza la petición por red, la carga de los datos recibidos se realiza en procesarResultado)
    // (método de clase privado)
    private void cargarDatosUsuario()
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

            // Tipo de acción a realizar (consultar/actualizar)
            String accion = "consultar";


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_USER_PROFILE);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada a enviar por red
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.USER_PROFILE, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("accion",accion);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                // (esta tarea no bloqueará el thread principal ni mostrará mensajes mientras dura la operación)
                TareaSegundoPlano tareaGetDatosUsuario = new TareaSegundoPlano(miClienteWeb,estaVentana);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaPerfilParticular", "Obteniendo datos del perfil...");
                tareaGetDatosUsuario.execute();
            }
        }
    }


    // Misma invocación que llamar al método cargarSpinner con 3 parámetros, siendo el último null
    private void cargarSpinner(ClaseParametro claseParametro, String idSuperclase)
    {
        cargarSpinner(claseParametro, idSuperclase, null);
    }


    // Llamar al servicio web que carga los datos a mostrar en alguno de los spinners de búsqueda
    // (aunque para las clases ACTIVIDADES y PROVINCIAS se ignorará, el valor de id debe indicarse siempre como un String con un entero mayor que 0)
    // En este caso, el último parámetro idClase indica que debe seleccionarse esa opción una vez queden cargados los valores en el spinner.
    // (NOTA: este método solo lanza la petición por red, la carga de los datos recibidos se realiza en procesarResultado)
    // (método de clase privado)
    private void cargarSpinner(ClaseParametro claseParametro, String idSuperclase, String idClase)
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

            // Idioma del cliente
            String idioma = Utils.idiomaAplicacion();

            // Resto de parámetros de la petición (tipo de usuario, clase de parámetro, id de la super clase)
            String tipo_usuario = "particular";

            String clase;
            switch (claseParametro)
            {
                case PROVINCIAS:    clase = "provincias";   break;
                case POBLACIONES:   clase = "poblaciones";  break;
                default:            clase = "NOEXISTE";     break;  // Esto hará que el servicio web devulva un error
            }

            String id_superClase = idSuperclase;


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_SPINNER_VALUES);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada a enviar por red

                // Si el parámetro idClase es null, guardamos como parámetro local solo la clase
                // Si el parámetro idClase NO ES null, guardamos un arrayList<String>
                // cuya posición 0 contiene el clase y cuya posición 1 contiene idClase
                // (esto no tiene efecto en la consulta al servicio web, solo en el post-procesado de la respuesta recibida)
                ServicioWeb miServicioWeb;

                if (idClase == null)
                {
                    miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.SPINNER_VALUES, estaVentana, clase);
                }
                else
                {
                    ArrayList<String> parametroLocal = new ArrayList();
                    parametroLocal.add(clase);
                    parametroLocal.add(idClase);

                    miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.SPINNER_VALUES, estaVentana, parametroLocal);
                }

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("idioma",idioma);
                miServicioWeb.addParam("tipo_usuario",tipo_usuario);
                miServicioWeb.addParam("clase",clase);
                miServicioWeb.addParam("id_super",id_superClase);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                // (esta tarea no bloqueará el thread principal ni mostrará mensajes mientras dura la operación)
                TareaSegundoPlano tareaGetDatosSpinner = new TareaSegundoPlano(miClienteWeb,estaVentana);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaPerfilParticular", "Obteniendo datos de la clase "+ clase +"...");
                tareaGetDatosSpinner.execute();
            }
        }
    }


    // Comprueba si los datos indicados en el formulario son correctos para solicitar una búsqueda al servidor
    // Si lo son, devuelve un String = null
    // Si no son correctos, devuelve un String con la explicación del problema
    // (método de clase interno)
    private String validarDatosPerfil(Secciones seccion)
    {
        String msg = null;

        switch (seccion)
        {
            case CABECERA:

                // La sección de cabecera no tiene ningún dato que validar
                // (cualquiera que sea el estado de la imagen de perfil, es válido)

                break;


            case PASSWORD:

                String pActual = txtPasswordActual.getText().toString();
                String pNuevo1 = txtPasswordNuevo1.getText().toString();
                String pNuevo2 = txtPasswordNuevo2.getText().toString();

                if (pActual.length() == 0)
                    msg = getResources().getString( R.string.VentanaPerfil_txt_msgFormulario1 );

                else if (!pNuevo1.equals(pNuevo2))
                    msg = getResources().getString( R.string.VentanaPerfil_txt_msgFormulario2 );

                else if (pActual.equals(pNuevo1))
                    msg = getResources().getString( R.string.VentanaPerfil_txt_msgFormulario3 );

                else if (!Utils.passwordValida(pNuevo1))
                    msg = getResources().getString( R.string.VentanaPerfil_txt_msgFormulario4 );

                break;


            case CONTACTO:

                String nombre = txtNombre.getText().toString();
                long idProvincia = spnProvincia.getSelectedItemId();
                long idPoblacion = spnPoblacion.getSelectedItemId();

                if (nombre.length() == 0)
                    msg = getResources().getString( R.string.VentanaPerfil_txt_msgFormulario5 );

                else if (idProvincia != 0 && idPoblacion == 0)
                    msg = getResources().getString( R.string.VentanaPerfil_txt_msgFormulario6 );

                break;


            case OTROS:

                boolean error = false;
                String fNac = txtNacimiento.getText().toString();


                if (!Utils.validarFecha(fNac, Utils.idiomaAplicacion()))
                    msg = getResources().getString( R.string.VentanaPerfil_txt_msgFormulario7 );

                else
                {
                    // La fecha de nacimiento debe ser anterior a la actual
                    SimpleDateFormat formateadorFecha = Utils.nuevoSimpleDateFormat(Utils.idiomaAplicacion());

                    Date hoy = new Date();
                    Date fechaIndicada = null;

                    try
                    {
                        fechaIndicada = formateadorFecha.parse(fNac);
                    }
                    catch (ParseException e)
                    {
                        error = true;
                    }

                    if (error)
                        msg = getResources().getString( R.string.VentanaPerfil_txt_msgFormulario7 );

                    else if (fechaIndicada.compareTo(hoy) >= 0)
                        msg = getResources().getString( R.string.VentanaPerfil_txt_msgFormulario8 );
                }

                break;


            default:
                break;
        }

        return msg;
    }


    // Devuelve un ArrayList<BasicNameValuePair> con los parámetros del perfil a actualizar para una petición al servicio web USER_PROFILE
    // Esta función no realiza ninguna comprobación, asumiendo que todos los datos del formulario son correctos (esto debe comprobarse antes)
    // (método de clase interno)
    private ArrayList<BasicNameValuePair> getListaParametros(Secciones seccion)
    {
        ArrayList<BasicNameValuePair> lista = new ArrayList();

        switch (seccion)
        {
            case CABECERA:

                String sec = "cabecera";
                String imagen_base64;

                if ( avatarPorDefecto )
                    imagen_base64 = "default_profile_pic";
                else
                {
                    Bitmap avatar = ((BitmapDrawable)imgAvatar.getDrawable()).getBitmap();
                    imagen_base64 = Utils.codifica_jpeg_base64(avatar);
                }

                lista.add( new BasicNameValuePair("seccion",sec) );
                lista.add( new BasicNameValuePair("avatar",imagen_base64) );

                break;


            case PASSWORD:

                sec = "password";
                String pActual = txtPasswordActual.getText().toString();
                String pNuevo  = txtPasswordNuevo1.getText().toString();

                lista.add( new BasicNameValuePair("seccion",sec) );
                lista.add( new BasicNameValuePair("passwordActual",pActual) );
                lista.add( new BasicNameValuePair("passwordNuevo",pNuevo) );

                break;


            case CONTACTO:

                sec = "contacto";
                String nombre  = txtNombre.getText().toString();
                String tel1    = txtTelefono1.getText().toString();
                String tel2    = txtTelefono2.getText().toString();
                long poblacion = spnPoblacion.getSelectedItemId();

                lista.add( new BasicNameValuePair("seccion",sec) );
                lista.add( new BasicNameValuePair("nombre",nombre) );

                if ( tel1.length()>0 )
                    lista.add( new BasicNameValuePair("tel1",tel1) );

                if ( tel2.length()>0 )
                    lista.add( new BasicNameValuePair("tel2",tel2) );

                if ( poblacion!=0 )
                    lista.add( new BasicNameValuePair("poblacion",Long.toString(poblacion)) );

                break;


            case OTROS:

                sec = "otros";

                // Convertir la fecha de nacimiento a formato de la BBDD
                String nacimiento = txtNacimiento.getText().toString();
                nacimiento = Utils.formatearFechaBBDD(nacimiento, Utils.idiomaAplicacion());


                long idSexo = spnSexo.getSelectedItemId();
                long idIdioma = spnIdioma.getSelectedItemId();
                String publicidad;

                if ( rbtnPublicidadSi.isChecked() )
                    publicidad = "1";
                else
                    publicidad = "00";

                String sexo = "";

                if ( idSexo==0 )        sexo = "n/d";
                else if (idSexo==1 )    sexo = "M";
                else if (idSexo==2 )    sexo = "F";

                // El orden de idiomas del spinner viene dado en el constructor de Adapter_SpinnerIdiomas
                // (por defecto, será inglés)
                String idioma = "en";

                if ( idIdioma==0 )        idioma = "es";
                else if (idIdioma==1 )    idioma = "en";

                lista.add( new BasicNameValuePair("seccion",sec) );
                lista.add( new BasicNameValuePair("nacimiento",nacimiento) );
                lista.add( new BasicNameValuePair("publicidad",publicidad) );
                lista.add( new BasicNameValuePair("sexo",sexo) );
                lista.add( new BasicNameValuePair("idioma",idioma) );

                break;


            default:
                break;
        }

        return lista;
    }


    // Envía los nuevos valores del perfil al servidor, si son correctos, a través de una petición al servicio web USER_PROFILE
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    public void actualizarPerfil(Secciones seccion)
    {
        String res = validarDatosPerfil(seccion);

        // Si los valores introducidos por el usuario no son correctos, mostrar un toast de aviso y no hacer nada más
        if ( res != null )
        {
            Utils.mostrarMensaje(estaVentana, res, Utils.TipoMensaje.TOAST, null, null);
            return;
        }

        // Si los valores introducidos por el usuario son correctos, preparar y enviar la petición de actualización al servidor

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

            // Tipo de acción a realizar (consultar/actualizar)
            String accion = "actualizar";

            // Resto de parámetros de la petición
            // (listado con todos los valores a enviar al servidor)
            ArrayList<BasicNameValuePair> listaParametros = getListaParametros(seccion);


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_USER_PROFILE);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada a enviar por red
                // (incluyendo "acción" también como parámetro local, para poder recuperarlo a posteriori)
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.USER_PROFILE, estaVentana, accion);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion", id_sesion);
                miServicioWeb.addParam("accion", accion);

                miServicioWeb.addParamList(listaParametros);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaActualizarPerfil = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de actualizar el perfil (en un hilo aparte del principal)
                Log.d("VentanaPerfilPers", "Actualizando perfil... : " + listaParametros);
                tareaActualizarPerfil.execute();
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

        // Posibles errores del servicio SPINNER_VALUES a los que se desea dar un tratamiento particular

        // Posibles errores del servicio USER_PROFILE a los que se desea dar un tratamiento particular
        listaErrores.add( new ErrorServicioWeb(ServicioWeb.Tipo.USER_PROFILE, "ERR_PasswordActualIncorrecto", false, R.string.msg_PerfilParticular_ERR_PasswordActualIncorrecto) );


        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana, tarea, idTituloOperacion, email, listaErrores, GestorSesiones.TipoUsuario.PARTICULAR);

        // Si hubo errores en la carga de los datos del perfil, además, cerramos la ventana
        if ( !respuesta_OK && tarea.getCliente().getServicioWeb().getTipo()==ServicioWeb.Tipo.USER_PROFILE )
        {
            String accion = (String) tarea.getCliente().getServicioWeb().getParametroLocal();

            if ( accion.equals("consultar") )
                finish();
        }

        // Si la respuesta se recibió correctamente (OK), discriminar a qué servicio web corresponde
        // (de los posibles que se pueden consultar desde esta ventana) y procesarla por separado
        if ( respuesta_OK )
        {
            // Servicio web asociado a la tarea, que contiene la respuesta del servidor
            ServicioWeb miServicioWeb = tarea.getCliente().getServicioWeb();

            if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.SPINNER_VALUES )
            {
                procesarDatosSpinner(miServicioWeb.getRespuesta(), (Object) miServicioWeb.getParametroLocal());
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.USER_PROFILE )
            {
                procesarPerfilUsuario( miServicioWeb.getRespuesta(), (String) miServicioWeb.getParametroLocal() );
            }
        }
    }


    // Procesamiento de la respuesta OK al servicio SPINNER_VALUES
    // (para cargar los valores de un spinner)
    // El Object parametro puede ser un String indicando la clase del spinner a cargar,
    // o un ArrayList<String> indicando la clase del spinner a cargar (posición 0) y el id de la opción a seleccionar (posición 1)
    private void procesarDatosSpinner(RespuestaServicioWeb respuesta, Object parametro)
    {
        // Cargar los datos recibidos en los spinners de la pantalla
        Adapter_Spinner miAdapter = null;

        // La respuesta devuelve un ArrayList<ContenidoServicioWeb>,
        // pero el adapter para el spinner necesita un ArrayList<Info_ElementoSpinner>
        ArrayList<Info_ElementoSpinner> items = new ArrayList();

        int tam = respuesta.getContenido().size();

        for (int i=0; i<tam; i++)
        {
            Info_ElementoSpinner item = (Info_ElementoSpinner) respuesta.getContenido().get(i);
            items.add(item);
        }


        // Determinar los valores incluidos en el parámetro local (String o ArrayList<String>)
        String clase = "";
        String id = null;

        if ( parametro instanceof String)
            clase = (String) parametro;

        if ( parametro instanceof ArrayList )
        {
            ArrayList<String> temp = (ArrayList<String>) parametro;
            clase = temp.get(0);
            id = temp.get(1);
        }


        // Dependiendo de que clase de datos se trate, seleccionamos un spinner distinto
        // (el texto por defecto también puede ser distino, aunque su clave siempre será 0)
        String opcionDefecto = "";
        Spinner spinner = null;

        if ( clase.equals("provincias") )
        {
            opcionDefecto = getResources().getString( R.string.spinner_txt_sinEspecificar );
            spinner = spnProvincia;
        }

        else if ( clase.equals("poblaciones") )
        {
            opcionDefecto = getResources().getString( R.string.spinner_txt_sinEspecificar );
            spinner = spnPoblacion;
        }

        // Cargar los datos recibidos en el spinner correspondiente y habilitarlo
        if ( spinner != null)
        {
            miAdapter = new Adapter_Spinner(estaVentana,R.layout.elemento_spinner,items,opcionDefecto);
            spinner.setAdapter(miAdapter);
            spinner.setEnabled(true);
        }


        // Si se especificó el valor de id en el parámetro local,
        // seleccionar esa entrada en el spinner que acabamos de rellenar (si existe)
        if ( miAdapter != null && id != null )
        {
            int pos = miAdapter.getPosicionClave(id);

            if (pos < 0)
                Log.e("ProcesarDatosSpinner","No se pudo seleccionar la clave "+ id +" en el spinner de "+ clase);
            else
                spinner.setSelection(pos);
        }

    }


    // Procesamiento de la respuesta OK al servicio USER_PROFILE
    private void procesarPerfilUsuario (RespuestaServicioWeb respuesta, String accion)
    {
        // Si se trataba de una actualización de los datos del perfil --> mostrar la confirmación al usuario

        if ( respuesta.getDetalle().equals("OK_CabeceraActualizada") )
        {
            String msg = getResources().getString( R.string.msg_PerfilParticular_OK_CabeceraActualizada );
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
        }

        else if ( respuesta.getDetalle().equals("OK_PasswordActualizado") )
        {
            String msg = getResources().getString( R.string.msg_PerfilParticular_OK_PasswordActualizado );
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
        }

        else if ( respuesta.getDetalle().equals("OK_ContactoActualizado") )
        {
            String msg = getResources().getString( R.string.msg_PerfilParticular_OK_ContactoActualizado );
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);

            // Actualizar en el gestor de sesiones los datos de la sesión con el nombre enviado (por si hubiera cambiado)
            gestorSesion.actualizarDatos( txtNombre.getText().toString() );
        }

        else if ( respuesta.getDetalle().equals("OK_OtrosActualizado") )
        {
            String msg = getResources().getString( R.string.msg_PerfilParticular_OK_OtrosActualizado );
            Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null,null);
        }


        // Si se trataba de una consulta de los datos del perfil --> cargar los datos recibidos en las secciones correspondientes

        else if ( respuesta.getDetalle().equals("OK_InformacionEnviada") || respuesta.getDetalle().equals("OK_FalloImagen") )
        {
            Info_PerfilUsuario perfil = (Info_PerfilUsuario) respuesta.getContenido().get(0);

            txtEmail.setText( perfil.getEmail() );

            txtNombre.setText( perfil.getNombre() );
            txtTelefono1.setText( perfil.getTel1() );
            txtTelefono2.setText( perfil.getTel2() );

            txtNacimiento.setText( Utils.formatearFechaVisible(perfil.getNacimiento(), Utils.idiomaAplicacion()) );


            if ( perfil.getPublicidad() )   rbtnPublicidadSi.setChecked(true);
            else                            rbtnPublicidadNo.setChecked(true);


            if ( perfil.getSexo().equals("n/d") )       spnSexo.setSelection(0);
            else if ( perfil.getSexo().equals("M") )    spnSexo.setSelection(1);
            else                                        spnSexo.setSelection(2);


            if ( perfil.getIdioma().equals("es") )      spnIdioma.setSelection(0);
            else if ( perfil.getIdioma().equals("en") ) spnIdioma.setSelection(1);
            else                                        spnIdioma.setSelection(1);    // por defecto, inglés


            // Si el perfil no tiene ninguna población indicada, inicialmente solo cargamos los datos de las provincias
            if ( perfil.getPoblacion().equals("0") )
            {
                // Cargar en el spinner de Provincias la lista de provincias, sin seleccionar ninguna
                cargarSpinner( ClaseParametro.PROVINCIAS, "9999");
            }
            // Si el perfil tiene alguna población indicada,
            // cargamos los datos de las provincias y de las poblaciones de la provincia en cuestión
            else
            {
                // Cargar en el spinner de Provincias la lista de provincias, y seleccionar la provincia del usuario
                cargarSpinner(ClaseParametro.PROVINCIAS, "9999", perfil.getProvincia());

                // Cargar en el spinner de Poblaciones la lista de poblaciones de esa provincia, y seleccionar la población del usuario
                cargarSpinner(ClaseParametro.POBLACIONES, perfil.getProvincia(), perfil.getPoblacion());
            }

            // Si el perfil tiene una imagen asociada, la mostramos
            if ( perfil.getAvatar() != null )
            {
                imgAvatar.setImageBitmap( perfil.getAvatar() );
                avatarPorDefecto = false;
                btnEliminarAvatar.setVisibility(View.VISIBLE);
            }


            // Si no fue posble cargar la imagen de avatar del usuario, mostrar un diálogo al usuario para informarle
            if ( respuesta.getDetalle().equals("OK_FalloImagen") )
            {
                String titulo = getResources().getString(R.string.titulo_PerfilParticular_OK_FalloImagen );
                String msg = getResources().getString( R.string.msg_PerfilParticular_OK_FalloImagen);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ERROR, titulo);
            }


            // Actualizar en el gestor de sesiones los datos de la sesión con el nombre y email recibidos
            // (por si hubieran cambiado en algún momento)
            if ( avatarPorDefecto )
                gestorSesion.actualizarDatos( perfil.getNombre(), perfil.getEmail(), "default_profile_pic" );
            else
                gestorSesion.actualizarDatos( perfil.getNombre(), perfil.getEmail(), Utils.codifica_jpeg_base64(perfil.getAvatar()) );
        }

    }


    // Fija el texto y el color de texto para el título de la Action Bar
    @Override
    public void setTitle(CharSequence titulo)
    {
        String color = Integer.toHexString(getResources().getColor(R.color.texto_actionBar) & 0x00ffffff);
        tituloVentana = titulo;

        getSupportActionBar().setTitle(Html.fromHtml("<font color='" + color + "'>" + tituloVentana + "</font>") );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ventana_perfil_particular, menu);
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
                    Log.e("VentanaBusquedaObra", "onMenuOpened ", e);
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

                break;


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

