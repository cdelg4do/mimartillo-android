package com.cdelgado.mimartillocom;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.makeramen.roundedimageview.RoundedDrawable;
import com.makeramen.roundedimageview.RoundedImageView;

import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import im.delight.android.location.SimpleLocation;


public class VentanaNuevaObra extends VentanaBase implements AdapterView.OnItemSelectedListener
{
    // Clases de datos a cargar en los spinners de la ventana
    public static enum ClaseParametro
    {
        ACTIVIDADES,
        CATEGORIAS,
        TIPOS,
        PROVINCIAS,
        POBLACIONES
    }


    private CharSequence tituloVentana;
    private GestorSesiones gestorSesion;

    private SimpleDateFormat formateadorFecha;
    private DatePickerDialog dialogoFecha;

    boolean usuarioSeleccionoUnaProvincia;

    EditText txt_TituloObraNueva;

    Spinner spn_ActObraNueva;
    Spinner spn_CatObraNueva;
    Spinner spn_TipObraNueva;

    EditText txt_FechaObraNueva;
    ImageView img_FechaObraNueva;

    EditText txt_NombreObraNueva;
    EditText txt_TelefonoObraNueva;
    EditText txt_EmailObraNueva;

    EditText txt_DescripcionObraNueva;

    RadioGroup rgr_UbicacionObraNueva;
    RadioButton rbtn_PoblacionObraNueva;

    LinearLayout layout_PoblacionObraNueva;
    Spinner spn_ProvinciaObraNueva;
    Spinner spn_PoblacionObraNueva;

    RadioButton rbtn_MapaObraNueva;

    RelativeLayout layout_MapaObraNueva;
    TextView txt_MapaObraNueva;
    ImageButton img_MapaObraNueva;

    TextView txt_ContadorFotosObraNueva;
    RoundedImageView imgObra0, imgObra1, imgObra2, imgObra3;
    ImageButton btnBorrar0, btnBorrar1, btnBorrar2, btnBorrar3;

    Button btn_ResetObraNueva;
    Button btn_PublicarObraNueva;


    // Datos por defecto para el formulario
    private String nombre_defecto;
    private String telef_defecto;
    private String email_defecto;

    private long idProvincia_defecto;
    private long idPoblacion_defecto;

    // Poblaciones de la provincia por defecto del usuario
    // (para no tener que cargarlas de nuevo del servidor, en caso de limpiar el formulario)
    private ArrayList<Info_ElementoSpinner> poblaciones_defecto;

    protected ProgressDialog pDialog;
    private VentanaNuevaObra estaVentana;

    private SimpleLocation gestorUbicacion;



    private static final int PLACE_PICKER_REQUEST = 1;
    //private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds( new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090) );


    // Estructuras para el manejo de los botones de fotos de obra
    int contadorFotos;
    int maxImagenes;
    ArrayList<RoundedImageView> imagenes;
    boolean[] adjuntadas;
    ArrayList<ImageButton> borradores;

    private static final int REQ_CODE_ESCOGER_IMAGEN_GALERIA = 2;
    private static final int ASPECTO_X = 4;
    private static final int ASPECTO_Y = 3;
    private static final int ANCHO_PX = 800;
    private static final int ALTO_PX = 600;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_nueva_obra);


        // Referencia al propio objeto activity
        estaVentana = this;

        // Cuadro de progreso (para mostrar durante las consultas por red)
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Referencia a los elementos de la ventana
        txt_TituloObraNueva         = (EditText) findViewById(R.id.txt_TituloObraNueva);
        spn_ActObraNueva            = (Spinner) findViewById(R.id.spn_ActObraNueva);
        spn_CatObraNueva            = (Spinner) findViewById(R.id.spn_CatObraNueva);
        spn_TipObraNueva            = (Spinner) findViewById(R.id.spn_TipObraNueva);
        txt_FechaObraNueva          = (EditText) findViewById(R.id.txt_FechaObraNueva);
        img_FechaObraNueva          = (ImageView) findViewById(R.id.img_FechaObraNueva);
        txt_NombreObraNueva         = (EditText) findViewById(R.id.txt_NombreObraNueva);
        txt_TelefonoObraNueva       = (EditText) findViewById(R.id.txt_TelefonoObraNueva);
        txt_EmailObraNueva          = (EditText) findViewById(R.id.txt_EmailObraNueva);
        txt_DescripcionObraNueva    = (EditText) findViewById(R.id.txt_DescripcionObraNueva);
        rgr_UbicacionObraNueva      = (RadioGroup) findViewById(R.id.rgr_UbicacionObraNueva);
        rbtn_PoblacionObraNueva     = (RadioButton) findViewById(R.id.rbtn_PoblacionObraNueva);
        layout_PoblacionObraNueva   = (LinearLayout) findViewById(R.id.layout_PoblacionObraNueva);
        spn_ProvinciaObraNueva      = (Spinner) findViewById(R.id.spn_ProvinciaObraNueva);
        spn_PoblacionObraNueva      = (Spinner) findViewById(R.id.spn_PoblacionObraNueva);
        rbtn_MapaObraNueva          = (RadioButton) findViewById(R.id.rbtn_MapaObraNueva);
        layout_MapaObraNueva        = (RelativeLayout) findViewById(R.id.layout_MapaObraNueva);
        txt_MapaObraNueva           = (TextView) findViewById(R.id.txt_MapaObraNueva);
        img_MapaObraNueva           = (ImageButton) findViewById(R.id.img_MapaObraNueva);
        txt_ContadorFotosObraNueva  = (TextView) findViewById(R.id.txt_ContadorFotosObraNueva);
        imgObra0                    = (RoundedImageView) findViewById(R.id.imgObra0);
        imgObra1                    = (RoundedImageView) findViewById(R.id.imgObra1);
        imgObra2                    = (RoundedImageView) findViewById(R.id.imgObra2);
        imgObra3                    = (RoundedImageView) findViewById(R.id.imgObra3);
        btnBorrar0                  = (ImageButton) findViewById(R.id.btnBorrar0);
        btnBorrar1                  = (ImageButton) findViewById(R.id.btnBorrar1);
        btnBorrar2                  = (ImageButton) findViewById(R.id.btnBorrar2);
        btnBorrar3                  = (ImageButton) findViewById(R.id.btnBorrar3);
        btn_ResetObraNueva          = (Button) findViewById(R.id.btn_ResetObraNueva);
        btn_PublicarObraNueva       = (Button) findViewById(R.id.btn_PublicarObraNueva);


        // Configuración de la Action Bar de esta ventana: título, color de fondo y visibilidad
        tituloVentana = getResources().getString(R.string.VentanaNuevaObra_txt_titulo);
        setTitle(tituloVentana);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();

        // Mostrar el icono de volver hacia atrás en la action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Comportamiento de los spinners al escoger un elemento de la lista
        // (requiere que la ventana implemente AdapterView.OnItemSelectedListener)
        spn_ActObraNueva.setOnItemSelectedListener(this);
        spn_CatObraNueva.setOnItemSelectedListener(this);
        spn_TipObraNueva.setOnItemSelectedListener(this);
        spn_ProvinciaObraNueva.setOnItemSelectedListener(this);
        spn_PoblacionObraNueva.setOnItemSelectedListener(this);

        // Inicialmente, los spinners están deshabilitados
        // (mientras no se cargue datos en ellos)
        spn_ActObraNueva.setEnabled(false);
        spn_CatObraNueva.setEnabled(false);
        spn_TipObraNueva.setEnabled(false);
        spn_ProvinciaObraNueva.setEnabled(false);
        spn_PoblacionObraNueva.setEnabled(false);

        // Inicialmente, el flag que indica si en algún momento el usuario seleccionó
        // una provincia distinta a la que está en su perfil está a false
        usuarioSeleccionoUnaProvincia = false;


        // Inicialmente, los datos de contacto están vacíos (aunque se cargarán con la llamada siguiente)
        nombre_defecto = "";
        telef_defecto  = "";
        email_defecto  = "";


        // Inicializar estructuras de control de las fotos de obra
        int contadorFotos = 0;

        imagenes   = new ArrayList();
        borradores = new ArrayList();

        // Si en el futuro se añaden más botones, incluirlos aquí
        imagenes.add(imgObra0); imagenes.add(imgObra1); imagenes.add(imgObra2); imagenes.add(imgObra3);
        borradores.add(btnBorrar0); borradores.add(btnBorrar1);borradores.add(btnBorrar2);borradores.add(btnBorrar3);

        maxImagenes = imagenes.size();

        adjuntadas = new boolean[maxImagenes];

        for (int i=0; i<maxImagenes; i++)
        {
            adjuntadas[i] = false;
            imagenes.get(i).setVisibility(View.INVISIBLE);
            borradores.get(i).setVisibility(View.INVISIBLE);
        }

        // Inicialmente solo se muestra el primer botón de añadir imágenes, con el contador a 0
        imgObra0.setVisibility(View.VISIBLE);
        txt_ContadorFotosObraNueva.setText( Integer.toString(contadorFotos) );


        // Comportamiento de los botones de añadir/ver imagen al pulsarlos
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


        // Comportamiento de los botones de borrar imagen al pulsarlos
        for (int i=0; i<maxImagenes; i++)
        {
            final ImageButton botonBorrar = borradores.get(i);
            final int pos = i;

            botonBorrar.setOnClickListener
            (
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        borrarImagen(pos);
                    }
                }
            );
        }



        // Gestor de sesion de usuario particular
        gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PARTICULAR);


        // Cargar del servidor los datos iniciales del formulario
        // (información sobre las actividades disponibles, provincias, datos de contacto por defecto del usuario)
        cargarDatosFormulario();


        // Configurar el cuadro de diálogo de elección de fecha (para la fecha de ejecución)

        // La fecha de realización se mostrará en el formato correspondiente al idioma en que se use la aplicación
        formateadorFecha = Utils.nuevoSimpleDateFormat( Utils.idiomaAplicacion() );

        // Anular la entrada directa en el campo de texto
        txt_FechaObraNueva.setInputType(InputType.TYPE_NULL);

        Calendar miCalendario = Calendar.getInstance();

        dialogoFecha = new DatePickerDialog(this,
            new DatePickerDialog.OnDateSetListener()
            {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                {
                    Calendar fecha = Calendar.getInstance();
                    fecha.set(year, monthOfYear, dayOfMonth);
                    txt_FechaObraNueva.setText( formateadorFecha.format( fecha.getTime()) );
                }

            }
            ,miCalendario.get(Calendar.YEAR), miCalendario.get(Calendar.MONTH), miCalendario.get(Calendar.DAY_OF_MONTH));


        // Comportamiento del icono de calendario al pulsarlo
        // (mostrar el cuadro de diáologo de elección de fecha)
        img_FechaObraNueva.setOnClickListener
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



        // Construir una nueva instancia de SimpleLocation
        // (para localizar la ubicación actual del dispositivo)
        gestorUbicacion = new SimpleLocation(this);

        // Si el servicio de ubicación está desactivado,
        // mostrar un diálogo al usuario para que lo active
        if ( !gestorUbicacion.hasLocationEnabled() )
        {
            String titulo = getResources().getString(R.string.general_txt_msgUbicacionDesactivada_titulo);
            String txt = getResources().getString(R.string.general_txt_msgUbicacionDesactivada);

            // Pedir al usuario que active el acceso a la ubicación
            Utils.dialogo_AvisoUbicacion(estaVentana, titulo, txt, Utils.CategoriaDialogo.ADVERTENCIA);
        }


        // Comportamiento al seleccionar uno de los radioButtons para la ubicación de la obra
        rgr_UbicacionObraNueva.setOnCheckedChangeListener
        (
            new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId)
                {
                    // Si se seleccionó el radioButton de ubicación en el mapa,
                    // ocultar los spinners de provincia y población
                    // y mostrar le selector de mapa
                    if (checkedId == R.id.rbtn_MapaObraNueva)
                    {
                        //rbtn_PoblacionObraNueva.setChecked(false);
                        layout_PoblacionObraNueva.setVisibility(View.GONE);

                        layout_MapaObraNueva.setVisibility(View.VISIBLE);
                    }

                    // Si se seleccíonó el radioButton de población,
                    // mostrar los spinners de provincia y poblacion
                    // y ocultar el selector de mapa
                    else if (checkedId == R.id.rbtn_PoblacionObraNueva)
                    {
                        layout_PoblacionObraNueva.setVisibility(View.VISIBLE);

                        //rbtn_MapaObraNueva.setChecked(false);
                        layout_MapaObraNueva.setVisibility(View.GONE);
                    }
                }
            }
        );


        // Comportamiento de la imágen del mapa al hacer click sobre ella
        img_MapaObraNueva.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LatLngBounds ubicacionAnterior = null;
                double radio = 1000;

                // Si el usuario ya había seleccionado anteriormente una ubicacion,
                // centraremos el mapa en ese punto
                if ( !txt_MapaObraNueva.getText().toString().equals("") )
                {
                    String coordenadas[] = txt_MapaObraNueva.getText().toString().split(",");

                    double lat  = Double.parseDouble( coordenadas[0] );
                    double lon = Double.parseDouble( coordenadas[1] );

                    ubicacionAnterior = Utils.limitesMapa( new LatLng(lat,lon) , radio);
                }

                try
                {
                    int PLACE_PICKER_REQUEST = 1;
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();

                    if ( ubicacionAnterior != null )
                        intentBuilder.setLatLngBounds( ubicacionAnterior );

                    Intent intent = intentBuilder.build(estaVentana);
                    startActivityForResult(intent,PLACE_PICKER_REQUEST);
                }
                catch (GooglePlayServicesRepairableException e)
                {
                    e.printStackTrace();
                }
                catch (GooglePlayServicesNotAvailableException e)
                {
                    e.printStackTrace();
                }
            }
        });


        // Comportamiento del botón de reset al pulsarlo
        btn_ResetObraNueva.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    limpiarFormulario();
                }
            }
        );


        // Comportamiento del botón de publicar obra al pulsarlo
        btn_PublicarObraNueva.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    publicarObra();
                }
            }
        );

    }


    // Acción que se ejecuta al pulsar uno de los botones de imagen de obra
    private void accionBotonImagen(int pos)
    {
        final RoundedImageView botonImagen = imagenes.get(pos);
        final boolean contieneImagen = adjuntadas[pos];

        // Si el boton ya contiene una imagen escogida por el usuario, mostrarla en grande en otra ventana
        if ( contieneImagen )
        {
            Bitmap bitmap = ((RoundedDrawable)botonImagen.getDrawable()).getSourceBitmap();
            String msg = getResources().getString(R.string.general_txt_imagenObra);

            gestorSesion.setDatosTemporales( Utils.codifica_jpeg_base64(bitmap) );

            Intent intent = new Intent(estaVentana,VentanaMostrarImagen.class);
            Bundle info = new Bundle();
            info.putString("mensaje", msg);
            intent.putExtras(info);

            startActivity(intent);
        }

        // Si el botón no contiene ninguna imagen aún, escogerla de la galería
        // (se pedirá al usuario que la recorte para ajustarla a la relación de aspecto adecuada)
        // El tratamiento de la imagen devuelta se realizará en el método onActivityResult()
        else
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
    }



    // Acción que se ejecuta al pulsar uno de los botones de eliminar imagen de obra
    private void borrarImagen(int pos)
    {
        // Si se trata del último botón de imagen que se ha asignado
        if ( pos==contadorFotos-1 )
        {
            // Asignarle el icono de añadir imagen
            imagenes.get(pos).setImageResource(R.drawable.ic_nuevaimagen);

            // Se centra la imagen en el centro del roundedImageView (no se reescala)
            imagenes.get(pos).setScaleType(ImageView.ScaleType.CENTER);

            // Eliminar la marca que indica que tenía una imagen asociada
            adjuntadas[pos] = false;

            // Ocultar el boton de eliminar correspondiente y el botón de añadir imagen siguiente (si lo hay)
            borradores.get(pos).setVisibility(View.INVISIBLE);

            if (pos+1<maxImagenes)
                imagenes.get(pos+1).setVisibility(View.INVISIBLE);
        }

        // Si no es el último botón de imagen, desplazar a la izquierda las posibles imagenes que ya hubiera a su derecha
        else
        {
            for (int i=pos; i<contadorFotos-1; i++)
            {
                int actual    = i;
                int siguiente = i+1;

                if ( !adjuntadas[siguiente] )
                {
                    imagenes.get(actual).setImageResource(R.drawable.ic_nuevaimagen);
                    imagenes.get(actual).setScaleType(ImageView.ScaleType.CENTER);
                    adjuntadas[actual] = false;
                }
                else
                {
                    Bitmap bitmap = ((RoundedDrawable)imagenes.get(siguiente).getDrawable()).getSourceBitmap();

                    imagenes.get(actual).setImageBitmap(bitmap);
                    imagenes.get(actual).setScaleType(ImageView.ScaleType.CENTER_CROP);
                    adjuntadas[actual] = true;
                }
            }

            // Después de copiar todas las imágenes a la izquierda, la última estará repetida, la eliminamos
            imagenes.get(contadorFotos-1).setImageResource(R.drawable.ic_nuevaimagen);
            imagenes.get(contadorFotos-1).setScaleType(ImageView.ScaleType.CENTER);
            adjuntadas[contadorFotos-1] = false;
            borradores.get(contadorFotos-1).setVisibility(View.INVISIBLE);

            // El siguiente al último (si lo hay) estará vacío, se oculta (para no mostrar dos botones vacíos juntos)
            if ( contadorFotos<maxImagenes )
                imagenes.get(contadorFotos).setVisibility(View.INVISIBLE);
        }

        contadorFotos--;
        txt_ContadorFotosObraNueva.setText( Integer.toString(contadorFotos) );
    }


    // Tratamiento de los datos devueltos por ventanas auxiliares (Places Picker y escoger imagen de galería)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Respuesta a la llamada de Google Places Picker
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK)
        {
            // Almacenar las coordenadas del punto seleccionado, si se seleccionó alguno, en txt_MapaObraNueva
            final Place lugarSeleccionado = PlacePicker.getPlace(data, this);

            double latitud  = lugarSeleccionado.getLatLng().latitude;
            double longitud = lugarSeleccionado.getLatLng().longitude;

            txt_MapaObraNueva.setText(Double.toString(latitud) + "," + Double.toString(longitud));
        }

        // Respuesta a la llamada de escoger imagen de galería
        else if (requestCode == REQ_CODE_ESCOGER_IMAGEN_GALERIA && resultCode == Activity.RESULT_OK && contadorFotos < maxImagenes)
        {
            // La imagen seleccionada se encontrará almacenada en un fichero temporal
            Uri tempFileUri = Utils.getTempUri();
            Bitmap bitmap = null;

            try
            {
                bitmap = BitmapFactory.decodeStream( getContentResolver().openInputStream(tempFileUri) );
            }
            catch (FileNotFoundException e)
            {
                Log.e("VentanaNuevaObra", "Fallo al leer el fichero temporal: " + e.toString());
            }

            // Si no pudo construirse un bitmap con los datos del fichero temporal, mostrar mensaje de error
            if ( bitmap == null )
            {
                Log.e("VentanaNuevaObra", "Fallo al decodificar la imagen guardada en el fichero temporal");

                String msg = getResources().getString( R.string.general_txt_msgFalloSeleccionImagen );
                Utils.mostrarMensaje(this,msg, Utils.TipoMensaje.TOAST,null,null);
            }

            // En caso contrario: asignar la imagen al botón correspondiente,
            // mostrar la siguiente imagen de añadir e incrementar el contador de fotos
            else
            {
                RoundedImageView botonImagen = imagenes.get(contadorFotos);

                botonImagen.setImageBitmap(bitmap);
                botonImagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
                adjuntadas[contadorFotos] = true;
                borradores.get(contadorFotos).setVisibility(View.VISIBLE);

                contadorFotos++;
                txt_ContadorFotosObraNueva.setText(Integer.toString(contadorFotos));

                if ( contadorFotos < maxImagenes )
                    imagenes.get(contadorFotos).setVisibility(View.VISIBLE);
            }

            // Por último, eliminar el fichero temporal (si existía)
            Utils.removeTempFile();
        }

        // Si no se trata de responder a una llamada ni de Google Places Picker ni de escoger imagen de galería
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    // Comportamiento al seleccionar un elemento de un spinner
    // Si se selecciona la posición (0) en el spinner de actividades, categorías o provincias, desactivar los sub-spinner correspondientes
    // Si se selecciona otra posición, cargar los valores correspondientes a esa opción en los sub-spinner
    // (no hay acción cuando se selecciona un elemento de tipos o poblaciones, ya que no tienen sub-spinners que dependan de ellos)
    // (tampoco hay acción si la seleccion de provincia se realiza al cargar los datos del servidor en el spinner,
    // es decir, si usuarioSeleccionoUnaProvincia = false)
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int pos, long id)
    {
        Spinner sp;

        if ( parent instanceof Spinner)
            sp = (Spinner) parent;
        else
            return;


        Log.d("Sel. spinner","Valor: " + Long.toString(id) );

        if ( sp == spn_ActObraNueva )
            if ( id==0 )
            {
                desactivarSpinner(spn_CatObraNueva, getResources().getString( R.string.spinner_txt_primeroActividad ) );
                desactivarSpinner(spn_TipObraNueva, getResources().getString( R.string.spinner_txt_primeroCategoria ) );
            }
            else
                cargarSpinner(ClaseParametro.CATEGORIAS, Long.toString(id));

        if ( sp == spn_CatObraNueva )
            if ( id==0 )
                desactivarSpinner(spn_TipObraNueva, getResources().getString( R.string.spinner_txt_primeroCategoria ) );
            else
                cargarSpinner(ClaseParametro.TIPOS, Long.toString(id));

        if ( sp == spn_ProvinciaObraNueva )
            if ( id==0 )
                desactivarSpinner(spn_PoblacionObraNueva, getResources().getString( R.string.spinner_txt_primeroProvincia ));

            // Si se selecciona alguna provincia, cargar sus correspondientes poblaciones
            // (siempre que se trate de una selección de provincia hecha por el usuario)
            else
                if (usuarioSeleccionoUnaProvincia)
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


    // Misma invocación que llamar al método cargarSpinner con 3 parámetros, siendo el último null
    private void cargarSpinner(ClaseParametro claseParametro, String idSuperclase)
    {
        cargarSpinner(claseParametro, idSuperclase, null);
    }


    // Llamar al servicio web que carga los datos iniciales del formulario
    // (información sobre las actividades disponibles, provincias, población y datos de contacto por defecto del usuario)
    // (NOTA: este método solo lanza la petición por red, la carga de los datos recibidos se realiza en procesarResultado)
    // (método de clase privado)
    private void cargarDatosFormulario()
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


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_INITIAL_PARAMETERS_NEW_WORK);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada a enviar por red
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.INITIAL_PARAMETERS_NEW_WORK, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);
                miServicioWeb.addParam("idioma",idioma);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                // (esta tarea no bloqueará el thread principal ni mostrará mensajes mientras dura la operación)
                TareaSegundoPlano tareaGetDatosUsuario = new TareaSegundoPlano(miClienteWeb,estaVentana);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaNuevaObra", "Obteniendo datos del formulario...");
                tareaGetDatosUsuario.execute();
            }
        }
    }


    // Llamar al servicio web que carga los datos a mostrar en alguno de los spinners del formulario
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
                case ACTIVIDADES:   clase = "actividades";  break;
                case CATEGORIAS:    clase = "categorias";   break;
                case TIPOS:         clase = "tipos";        break;
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
                // cuya posición 0 contiene la clase y cuya posición 1 contiene idClase
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
                Log.d("VentanaNuevaObra", "Obteniendo datos de la clase "+ clase +"...");
                tareaGetDatosSpinner.execute();
            }
        }
    }



    // Restaura los valores por defecto en el formulario de nueva obra
    public void limpiarFormulario()
    {
        // Limpiar los campos de texto
        txt_TituloObraNueva.setText("");
        txt_FechaObraNueva.setText("");
        txt_DescripcionObraNueva.setText("");
        txt_MapaObraNueva.setText("");


        // Datos de contacto por defecto
        txt_NombreObraNueva.setText( nombre_defecto );
        txt_TelefonoObraNueva.setText( telef_defecto );
        txt_EmailObraNueva.setText( email_defecto );


        // Limpiar fotos (en orden decreciente)
        for (int i=contadorFotos-1; i>=0; i--)
        {
            borrarImagen(i);
        }


        // Activar el radiobutton de Ubicación por población y mostrar los spinners de provincia y población
        // Limpiar la seleccion de punto del mapa y ocultar el campo de texto y el botón de mapa
        rbtn_PoblacionObraNueva.setChecked(true);
        layout_PoblacionObraNueva.setVisibility(View.VISIBLE);

        //rbtn_MapaObraNueva.setChecked(false);
        txt_MapaObraNueva.setText("");
        layout_MapaObraNueva.setVisibility(View.GONE);


        // Seleccionar los valores por defecto en los spinners principales (actividades y provincias)
        // Desactivar y vaciar los spinners de categorias, tipos y poblaciones
        spn_ActObraNueva.setSelection(0);
        desactivarSpinner(spn_CatObraNueva, getResources().getString( R.string.spinner_txt_primeroActividad ) );
        desactivarSpinner(spn_TipObraNueva, getResources().getString( R.string.spinner_txt_primeroCategoria ) );


        // Resetear el flag de provincia seleccionada por el usuario
        // (para que al seleccionar la provincia y población por defecto por defecto no se seleccione automáticamente la población "0")
        usuarioSeleccionoUnaProvincia = false;

        // Si el usuario no tiene definida ninguna población por defecto en su perfil,
        // no seleccionar ninguna provincia y desactivar el spinner de población
        if ( idPoblacion_defecto == 0 )
        {
            spn_ProvinciaObraNueva.setSelection(0);
            desactivarSpinner(spn_PoblacionObraNueva, getResources().getString( R.string.spinner_txt_primeroProvincia ) );
        }

        // Si el usuario tiene definida una población por defecto en su perfil,
        // seleccionar la provincia correspondiente, cargar las y desactivar el spinner de población
        else
        {
            // Obtener la posición de la provincia por defecto en el spinner de provincias
            Adapter_Spinner miAdapter = (Adapter_Spinner) spn_ProvinciaObraNueva.getAdapter();
            int pos = miAdapter.getPosicionClave( Long.toString(idProvincia_defecto) );

            if (pos < 0)
                Log.e("ProcesarDatosSpinner","No se pudo seleccionar la clave por defecto "+ idProvincia_defecto +" en el spinner de provincias");
            else
            {
                spn_ProvinciaObraNueva.setSelection(pos);

                // Cargar en el spinner de Poblaciones la lista de poblaciones de esa provincia, y seleccionar la población por defecto del usuario
                // (sin volver a consultar al servidor)
                miAdapter = new Adapter_Spinner(estaVentana,R.layout.elemento_spinner,poblaciones_defecto, getResources().getString( R.string.spinner_txt_seleccionaPoblacion ) );
                spn_PoblacionObraNueva.setAdapter(miAdapter);

                pos = miAdapter.getPosicionClave( Long.toString(idPoblacion_defecto) );

                if (pos < 0)
                    Log.e("ProcesarDatosSpinner","No se pudo seleccionar la clave por defecto "+ idPoblacion_defecto +" en el spinner de poblaciones");
                else
                    spn_PoblacionObraNueva.setSelection(pos);
            }
        }

    }


    // Comprueba si los datos indicados en el formulario son correctos para solicitar una búsqueda al servidor
    // Si lo son, devuelve un String = null
    // Si no son correctos, devuelve un String con la explicación del problema
    // (método de clase interno)
    private String validarDatosObra()
    {
        String msg = null;

        // Comprobar los campos de la ventana
        String tituloObra = txt_TituloObraNueva.getText().toString();

        long tipoObra = spn_TipObraNueva.getSelectedItemId();
        //long tipoObra = 4007003; // Otros trabajos Ingeniería

        String fechaObra = txt_FechaObraNueva.getText().toString();
        String nombreContacto = txt_NombreObraNueva.getText().toString();
        String telContacto = txt_TelefonoObraNueva.getText().toString();
        String mailContacto = txt_EmailObraNueva.getText().toString();
        String descripcionObra = txt_DescripcionObraNueva.getText().toString();

        long idPoblacion = 0;
        String coordenadas = "";

        // Ubicación de la obra -> población
        if ( rbtn_PoblacionObraNueva.isChecked() )
            idPoblacion = spn_PoblacionObraNueva.getSelectedItemId();
            //idPoblacion = 4965;

        // Ubicación de la obra -> punto del mapa
        else
            coordenadas = txt_MapaObraNueva.getText().toString();
            //coordenadas = Utils.ubicacionActual(estaVentana,gestorUbicacion);


        if ( tituloObra.length() == 0 )
            msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario1 );

        else if ( tipoObra == 0 )
            msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario2 );

        else if ( fechaObra.length() == 0 )
            msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario3 );

        else if ( nombreContacto.length() == 0 )
            msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario4 );

        else if ( telContacto.length() == 0 && mailContacto.length() == 0 )
            msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario5 );

        else if ( descripcionObra.length() == 0 )
            msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario6 );

        else if ( rbtn_PoblacionObraNueva.isChecked() && idPoblacion == 0 )
            msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario7 );

        else if ( rbtn_MapaObraNueva.isChecked() && coordenadas.equals("") )
            msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario8 );

        // Comprobación de que la fecha de ejecución es correcta
        else
        {
            boolean error = false;

            if (!Utils.validarFecha(fechaObra, Utils.idiomaAplicacion()))
                msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario9 );

            else
            {
                // La fecha de ejecución debe ser posterior a la actual
                SimpleDateFormat formateadorFecha = Utils.nuevoSimpleDateFormat(Utils.idiomaAplicacion());

                Date hoy = new Date();
                Date fechaIndicada = null;

                try {
                    fechaIndicada = formateadorFecha.parse(fechaObra);
                } catch (ParseException e) {
                    error = true;
                }

                if (error)
                    msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario9 );

                else if (fechaIndicada.compareTo(hoy) <= 0)
                    msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgFormulario10 );
            }

        }


        return msg;
    }


    // Devuelve un ArrayList<BasicNameValuePair> con los parámetros de la obra a publicar mediante una petición al servicio web NEW_WORK
    // Esta función no realiza ninguna comprobación, asumiendo que todos los datos del formulario son correctos (esto debe comprobarse antes)
    // (método de clase interno)
    private ArrayList<BasicNameValuePair> getParametrosObraNueva()
    {
        ArrayList<BasicNameValuePair> lista = new ArrayList();

        lista.add( new BasicNameValuePair("titulo",txt_TituloObraNueva.getText().toString()) );

        lista.add( new BasicNameValuePair("tipo_obra", Long.toString(spn_TipObraNueva.getSelectedItemId())) );

        // Convertir la fecha de ejecución al formato de la BBDD
        String ejecucion = txt_FechaObraNueva.getText().toString();
        ejecucion = Utils.formatearFechaBBDD(ejecucion, Utils.idiomaAplicacion());
        lista.add(new BasicNameValuePair("fecha", ejecucion));

        lista.add(new BasicNameValuePair("nombre_cont", txt_NombreObraNueva.getText().toString()));

        if ( !txt_EmailObraNueva.getText().toString().equals("") )
            lista.add(new BasicNameValuePair("email_cont", txt_EmailObraNueva.getText().toString()));

        if ( !txt_TelefonoObraNueva.getText().toString().equals("") )
            lista.add(new BasicNameValuePair("telef_cont", txt_TelefonoObraNueva.getText().toString()));

        lista.add(new BasicNameValuePair("detalle", txt_DescripcionObraNueva.getText().toString()));


        long idPoblacion = 0;
        String coordenadas = "";

        // Ubicación de la obra -> población
        if (rbtn_PoblacionObraNueva.isChecked())
        {
            idPoblacion = spn_PoblacionObraNueva.getSelectedItemId();
            coordenadas = null;
        }
        // Ubicación de la obra -> punto del mapa
        else
        {
            idPoblacion = -1;
            coordenadas = txt_MapaObraNueva.getText().toString();
        }

        if ( idPoblacion == -1 )  lista.add( new BasicNameValuePair("ubicacion", coordenadas) );
        else                      lista.add( new BasicNameValuePair("poblacion", Long.toString(idPoblacion)) );


        // Fotos de la obra
        for (int i=0; i<contadorFotos; i++)
        {
            Bitmap imagen = ((RoundedDrawable)imagenes.get(i).getDrawable()).getSourceBitmap();
            String imagen_base64 = Utils.codifica_jpeg_base64(imagen);
            String imgN = "img"+Integer.toString(i+1);  // img1, img2, img3, ...

            lista.add( new BasicNameValuePair(imgN, imagen_base64) );
        }

        return lista;
    }


    // Envía los datos de la obra nueva al servidor, si son correctos, a través de una petición al servicio web NEW_WORK
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    public void publicarObra()
    {
        String res = validarDatosObra();

        // Si los valores introducidos por el usuario no son correctos, mostrar un toast de aviso y no hacer nada más
        if ( res != null )
        {
            Utils.mostrarMensaje(estaVentana, res, Utils.TipoMensaje.TOAST, null, null);
            return;
        }


        // Si los valores introducidos por el usuario son correctos, preparar y enviar la petición de actualización al servidor

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

            // Resto de parámetros de la petición
            // (listado con todos los datos de la obra a enviar al servidor, incluyendo imágenes adjuntas)
            ArrayList<BasicNameValuePair> listaParametros = getParametrosObraNueva();


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_NEW_WORK);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.NEW_WORK, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion",id_sesion);

                miServicioWeb.addParamList(listaParametros);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaNuevaObra = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaNuevaObra","Publicando nueva obra...");
                tareaNuevaObra.execute();
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


        // Lista de posibles respuestas de error que puede recibir esta ventana y el tratamiento que debe darse a cada una
        // (sin incluir las respuestas de sesión expirada, sesión inválida o usuario deshabilitado, que ya se tratan por defecto)
        ArrayList<ErrorServicioWeb> listaErrores = new ArrayList();

        // Posibles errores del servicio SPINNER_VALUES a los que se desea dar un tratamiento particular

        // Posibles errores del servicio INITIAL_PARAMETERS_NEW_WORK a los que se desea dar un tratamiento particular

        // Posibles errores del servicio NEW_WORK a los que se desea dar un tratamiento particular

        // Comprobar si la respuesta del servidor fue satisfactoria (OK)
        // Si no lo es, la función le dará al usuario la respuesta que corresponda
        boolean respuesta_OK = ! Utils.procesar_respuesta_erronea(estaVentana, tarea, idTituloOperacion, email, listaErrores, GestorSesiones.TipoUsuario.PARTICULAR);

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

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.INITIAL_PARAMETERS_NEW_WORK )
            {
                procesarDatosFormulario( miServicioWeb.getRespuesta() );
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.NEW_WORK )
            {
                procesarNuevaObra(miServicioWeb.getRespuesta());
            }
        }
    }


    // Procesamiento de la respuesta OK al servicio SPINNER_VALUES
    // (para cargar los valores de un spinner)
    // El Object parametro puede ser un String indicando la clase del spinner a cargar,
    // o un ArrayList<String> indicando la clase del spinner a cargar (posición 0) y el id de la opción a seleccionar (posición 1)
    private void procesarDatosSpinner(RespuestaServicioWeb respuesta, Object parametro)
    {
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

        if ( clase.equals("actividades") )
        {
            opcionDefecto = getResources().getString( R.string.spinner_txt_seleccionaActividad );
            spinner = spn_ActObraNueva;
        }

        else if ( clase.equals("categorias") )
        {
            opcionDefecto = getResources().getString( R.string.spinner_txt_seleccionaCategoria );
            spinner = spn_CatObraNueva;
        }

        else if ( clase.equals("tipos") )
        {
            opcionDefecto = getResources().getString( R.string.spinner_txt_seleccionaTipo );
            spinner = spn_TipObraNueva;
        }

        else if ( clase.equals("provincias") )
        {
            opcionDefecto = getResources().getString( R.string.spinner_txt_seleccionaProvincia );
            spinner = spn_ProvinciaObraNueva;
        }

        else if ( clase.equals("poblaciones") )
        {
            opcionDefecto = getResources().getString( R.string.spinner_txt_seleccionaPoblacion );
            spinner = spn_PoblacionObraNueva;
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


    // Procesamiento de la respuesta OK al servicio INITIAL_PARAMETERS_NEW_WORK
    private void procesarDatosFormulario (RespuestaServicioWeb respuesta)
    {
        Adapter_Spinner miAdapter;

        Info_ParametrosNuevaObra info = (Info_ParametrosNuevaObra) respuesta.getContenido().get(0);

        // Datos de contacto por defecto
        txt_NombreObraNueva.setText( info.getNombre() );
        txt_TelefonoObraNueva.setText( info.getTel1() );
        txt_EmailObraNueva.setText( info.getEmail() );

        // Datos del spinner de Actividades
        miAdapter = new Adapter_Spinner(estaVentana,R.layout.elemento_spinner,info.getActividades(), getResources().getString( R.string.spinner_txt_seleccionaActividad ) );
        spn_ActObraNueva.setAdapter(miAdapter);

        spn_ActObraNueva.setEnabled(true);


        // Datos del spinner de Provincias
        miAdapter = new Adapter_Spinner(estaVentana,R.layout.elemento_spinner,info.getProvincias(), getResources().getString( R.string.spinner_txt_seleccionaProvincia ) );
        spn_ProvinciaObraNueva.setAdapter(miAdapter);

        spn_ProvinciaObraNueva.setEnabled(true);

        // Si el usuario tiene alguna población indicada por defecto (no es 0)
        // cargamos los datos de las provincias y de las poblaciones de la provincia en cuestión,
        // seleccionándolas en ambos spinners
        if ( !info.getPoblacion().equals("0") )
        {
            int pos;

            // Seleccionar la provincia por defecto en el spinner de provincias
            pos = miAdapter.getPosicionClave( info.getProvincia() );
            spn_ProvinciaObraNueva.setSelection(pos);


            // Cargar en el spinner de Poblaciones la lista de poblaciones de esa provincia, y seleccionar la población del usuario
            miAdapter = new Adapter_Spinner(estaVentana,R.layout.elemento_spinner,info.getPoblaciones(), getResources().getString( R.string.spinner_txt_seleccionaPoblacion ) );
            spn_PoblacionObraNueva.setAdapter(miAdapter);

            spn_PoblacionObraNueva.setEnabled(true);

            pos = miAdapter.getPosicionClave( info.getPoblacion() );
            spn_PoblacionObraNueva.setSelection(pos);
        }


        // Guardar los datos recibidos
        // (por si el usuario resetea el formulario, para que no haya que volver a solicitarlos al servidor)
        nombre_defecto = info.getNombre();
        telef_defecto = info.getTel1();
        email_defecto = info.getEmail();

        idProvincia_defecto = Long.parseLong( info.getProvincia() );
        idPoblacion_defecto = Long.parseLong( info.getPoblacion() );

        poblaciones_defecto = info.getPoblaciones();


        // Actualizar en el gestor de sesiones los datos de la sesión con el nombre y email recibidos
        // (por si hubieran cambiado en algún momento)
        gestorSesion.actualizarDatos( info.getNombre(), info.getEmail() );
    }


    // Procesamiento de la respuesta OK al servicio NEW_WORK
    private void procesarNuevaObra (RespuestaServicioWeb respuesta)
    {
        // mostrar mensaje de confirmación y cerrar la ventana

        String msg = getResources().getString( R.string.VentanaNuevaObra_txt_msgObraPublicada );
        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);

        finish();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ventana_nueva_obra, menu);
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

                return true;


            case R.id.action_settings:
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


    @Override
    public void onPause()
    {
        // Interrumpir las actualizaciones cuando la ventana entra en estado onPause
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
