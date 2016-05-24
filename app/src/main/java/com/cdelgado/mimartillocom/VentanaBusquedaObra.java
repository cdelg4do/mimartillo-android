package com.cdelgado.mimartillocom;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;

import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import im.delight.android.location.SimpleLocation;


public class VentanaBusquedaObra extends VentanaBase implements AdapterView.OnItemSelectedListener
{
    public static enum ClaseParametro
    {
        ACTIVIDADES,
        CATEGORIAS,
        TIPOS,
        PROVINCIAS,
        POBLACIONES
    }



    private GestorSesiones gestorSesion;

    protected ProgressDialog pDialog;
    private VentanaBusquedaObra estaVentana;

    private Menu menuActionBar;

    private SimpleLocation gestorUbicacion;


    TabHost tabHost;

    LinearLayout tabCriterioBusqueda;

    FrameLayout layoutChkTipologia;
    CheckBox chkTipologia;

    LinearLayout layoutOpcionesTipologia;
    Spinner spnActividad;
    Spinner spnCategoria;
    Spinner spnTipo;

    FrameLayout layoutChkProximidad;
    CheckBox chkProximidad;

    TableLayout layoutDistancia;
    SeekBar skbDistancia;
    TextView txtDistancia;
    TextView txtUnidad;

    RadioGroup rgrOpcionesUbicacion;
    RadioButton rbtnUbicacion;
    RadioButton rbtnPoblacion;

    TableLayout layoutPoblaciones;
    Spinner spnProvincia;
    Spinner spnPoblacion;

    FrameLayout layoutChkTexto;
    CheckBox chkTexto;

    TableLayout layoutTextoBusqueda;
    EditText etxtTextoBusqueda;


    Button btnReset;
    Button btnBuscar;


    ListView listaObras;

    TextView titulo_tab0;
    TextView titulo_tab1;

    private CharSequence tituloVentana;

    private boolean hayResultados;  // Indica si la pestaña de resultados muestra alguna obra

    // Lista de elementos que se pliegan/despliegan al hacer click sobre los checkboxes de los criterios de búsqueda
    private ArrayList<View> elementosPlegables_tipologia;
    private ArrayList<View> elementosPlegables_proximidad;
    private ArrayList<View> elementosPlegables_texto;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventana_busqueda_obra);


        // Referencia al propio objeto activity
        estaVentana = this;

        // Cuadro de progreso (para mostrar durante las consultas por red)
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // Referencia a los elementos de la ventana
        tabCriterioBusqueda     = (LinearLayout)findViewById(R.id.tabCriterioBusqueda);
        layoutChkTipologia      = (FrameLayout) findViewById(R.id.layoutChkTipologia);
        chkTipologia            = (CheckBox)    findViewById(R.id.chkTipologia);
        layoutOpcionesTipologia = (LinearLayout)findViewById(R.id.layoutOpcionesTipologia);
        spnActividad            = (Spinner)     findViewById(R.id.spnActividad);
        spnCategoria            = (Spinner)     findViewById(R.id.spnCategoria);
        spnTipo                 = (Spinner)     findViewById(R.id.spnTipo);
        layoutChkProximidad     = (FrameLayout) findViewById(R.id.layoutChkProximidad);
        chkProximidad           = (CheckBox)    findViewById(R.id.chkProximidad);
        layoutDistancia         = (TableLayout) findViewById(R.id.layoutDistancia);
        skbDistancia            = (SeekBar)     findViewById(R.id.skbDistancia);
        txtDistancia            = (TextView)    findViewById(R.id.txtDistancia);
        txtUnidad               = (TextView)    findViewById(R.id.txtUnidad);
        rgrOpcionesUbicacion    = (RadioGroup)  findViewById(R.id.rgrOpcionesUbicacion);
        rbtnUbicacion           = (RadioButton) findViewById(R.id.rbtnUbicacion);
        rbtnPoblacion           = (RadioButton) findViewById(R.id.rbtnPoblacion);
        layoutPoblaciones       = (TableLayout) findViewById(R.id.layoutPoblaciones);
        spnProvincia            = (Spinner)     findViewById(R.id.spnProvincia);
        spnPoblacion            = (Spinner)     findViewById(R.id.spnPoblacion);
        layoutChkTexto          = (FrameLayout) findViewById(R.id.layoutChkTexto);
        chkTexto                = (CheckBox)    findViewById(R.id.chkTexto);
        layoutTextoBusqueda     = (TableLayout) findViewById(R.id.layoutTextoBusqueda);
        etxtTextoBusqueda       = (EditText)    findViewById(R.id.etxtTextoBusqueda);
        btnReset                = (Button)      findViewById(R.id.btnReset);
        btnBuscar               = (Button)      findViewById(R.id.btnBuscar);

        listaObras              = (ListView)    findViewById(R.id.listaObras);


        // Configuración de las pestañas
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("Tab Criterio Busqueda");
        spec.setContent(R.id.tabCriterioBusqueda);
        spec.setIndicator(getResources().getString(R.string.VentanaBusqueda_txt_tabCriterios));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Tab Resultado Busqueda");
        spec.setContent(R.id.tabResultadoBusqueda);
        spec.setIndicator(getResources().getString(R.string.VentanaBusqueda_txt_tabResultados));
        tabHost.addTab(spec);

        // Inicialmente no se muestra ninguna obra en la pestaña de resultados
        hayResultados = false;

        // Guardaremos una referencia al menú de la ventana cuando se invoque el método onCreateOptionsMenu()
        menuActionBar = null;

        // Color del título de cada pestaña
        titulo_tab0 = (TextView) tabHost.getTabWidget().getChildTabViewAt(0).findViewById(android.R.id.title);
        titulo_tab1 = (TextView) tabHost.getTabWidget().getChildTabViewAt(1).findViewById(android.R.id.title);
        titulo_tab0.setTextColor(this.getResources().getColorStateList(R.color.texto_tabs));
        titulo_tab1.setTextColor(this.getResources().getColorStateList(R.color.texto_tabs));

        // Por defecto, la ventana abre siempre con la pestaña de citerios de búsqueda
        tabHost.setCurrentTab(0);

        // Por defecto, el criterio de búsqueda por proximidad es desde la ubicación actual del dispositivo
        rbtnUbicacion.setChecked(true);

        // Por defecto, los layouts con las opciones de búsqueda aparecen "plegados"
        layoutOpcionesTipologia.setVisibility(View.GONE);
        layoutDistancia.setVisibility(View.GONE);
        rgrOpcionesUbicacion.setVisibility(View.GONE);
        layoutPoblaciones.setVisibility(View.GONE);
        layoutTextoBusqueda.setVisibility(View.GONE);


        // Inicialmente, el layout con los criterios de búsqueda está oculto (hasta que se carguen los datos del servidor)
        tabCriterioBusqueda.setVisibility(View.GONE);


        // Gestor de sesion de usuario profesional
        gestorSesion = new GestorSesiones(this, GestorSesiones.TipoUsuario.PROFESIONAL);


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
        String tituloActionBar = getResources().getString(R.string.VentanaBusqueda_txt_titulo_obras);

        setTitle(tituloActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fondo_actionBar)));
        getSupportActionBar().show();

        // Mostrar el icono de volver hacia atrás en la action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Comportamiento de los spinners al escoger un elemento de la lista
        // (requiere que la ventana implemente AdapterView.OnItemSelectedListener)
        spnActividad.setOnItemSelectedListener(this);
        spnCategoria.setOnItemSelectedListener(this);
        spnTipo.setOnItemSelectedListener(this);
        spnProvincia.setOnItemSelectedListener(this);
        spnPoblacion.setOnItemSelectedListener(this);

        // Llamar al servicio web que devuelva información sobre las actividades de obra
        // y sobre las provincias disponibles, y cargarla en sus respectivos spinners
        // (NOTA: aunque en el caso de ACTIVIDADES y PROVINCIAS el segundo parámetro no se usará,
        // debe especificarse uno igualmente, siendo un String que representa un entero mayor que cero)
        cargarSpinner_ParametrosBusqueda(ClaseParametro.ACTIVIDADES, "9999");
        cargarSpinner_ParametrosBusqueda(ClaseParametro.PROVINCIAS, "9999");

        // Para el resto de spinners, mostrar solo la opcion por defecto y deshabilitarlos
        desactivarSpinner(spnCategoria, getResources().getString( R.string.spinner_txt_cualquiera ) );
        desactivarSpinner(spnTipo, getResources().getString( R.string.spinner_txt_cualquiera ) );
        desactivarSpinner(spnPoblacion, getResources().getString( R.string.spinner_txt_primeroProvincia ) );


        // Elementos que se plegarán/desplegarán al hacer click en los checkboxes de criterios de búsqueda
        elementosPlegables_tipologia = new ArrayList();     elementosPlegables_tipologia.add(layoutOpcionesTipologia);

        elementosPlegables_proximidad = new ArrayList();    elementosPlegables_proximidad.add(layoutDistancia);
        elementosPlegables_proximidad.add(rgrOpcionesUbicacion);
        elementosPlegables_proximidad.add(layoutPoblaciones);

        elementosPlegables_texto = new ArrayList();         elementosPlegables_texto.add(layoutTextoBusqueda);

        // Comportamiento de los checkBox para los criterios de búsqueda
        definirListener_CheckBox(chkTipologia);
        definirListener_CheckBox(chkProximidad);
        definirListener_CheckBox(chkTexto);


        // Inicialmente, el tab de resultados de la búsqueda solo muestra un mensaje de que no hay resultados aún
        // (se sobreescribe el método getView del ArrayList generico para poder especificar un color del texto)
        String[] titulosProfesionales = new String[1];
        titulosProfesionales[0] = estaVentana.getResources().getString(R.string.VentanaBusqueda_txt_sinBusquedas);

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


        listaObras.setAdapter(miAdapter);

        // Deshabilitar el click sobre el ListView
        listaObras.setSelector(android.R.color.transparent);



        // Comportamiento del botón de reset
        btnReset.setOnClickListener
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


        // Comportamiento del botón de buscar
        btnBuscar.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    iniciarBusqueda();
                }
            }
        );


        // Comportamiento al seleccionar uno de los radioButtons del criterio de proximidad
        rgrOpcionesUbicacion.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                // Si se seleccionó el radioButton de ubicación actual,
                // resetear y ocultar los spinners de provincia y población
                if (checkedId == R.id.rbtnUbicacion)
                {
                    spnProvincia.setSelection(0);
                    desactivarSpinner(spnPoblacion, estaVentana.getResources().getString(R.string.spinner_txt_primeroProvincia) );

                    layoutPoblaciones.setVisibility(View.GONE);
                }

                // Si se seleccíonó el radioButton de población,
                // mostrar los spinners de provincia y poblacion
                else if (checkedId == R.id.rbtnPoblacion)
                {
                    layoutPoblaciones.setVisibility(View.VISIBLE);
                }

            }
        });


        // Comportamiento de la barra de distancia al moverla
        // (actualizar el textView de la pantalla con el valor seleccionado en la barra)
        skbDistancia.setOnSeekBarChangeListener
        (
            new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int valor, boolean fromUser)
                {
                    mostrarDistancia(valor, getResources().getString(R.string.general_txt_unidadDistancia));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                }
            }
        );


        // Comportamiento al pulsar en la ListView de las cabeceras de las obras que devolvió la búsqueda
        // Abrir una nueva ventana para mostrar los datos de la obra (si el elemento pulsado representa una obra)
        listaObras.setOnItemClickListener
        (
            new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView parent, View v, int pos, long id)
                {
                    String idObra = (String) v.getTag();

                    if (idObra != null)
                    {
                        Intent intent = new Intent(estaVentana, VentanaDatosObra.class);

                        Bundle info = new Bundle();
                        info.putString("id_obra", idObra);
                        info.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
                        intent.putExtras(info);

                        startActivity(intent);
                    }
                }
            }
        );


        // Al mantener presionado sobre el ListView, se invocará al método onCreateContextMenu de la ventana
        // para mostrar el menú contextual correspondiente
        registerForContextMenu(listaObras);

    }



    // Comportamiento al seleccionar un elemento de un spinner
    // Si se selecciona la posición (0) en el spinner de actividades, categorías o provincias, desactivar los sub-spinner correspondientes
    // Si se selecciona otra posición, cargar los valores correspondientes a esa opción en los sub-spinner
    // (no hay acción cuando se selecciona un elemento de tipos o poblaciones, ya que no tienen sub-spinners que dependan de ellos)
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int pos, long id)
    {
        Spinner sp;

        if ( parent instanceof Spinner)
            sp = (Spinner) parent;
        else
            return;


        Log.d("Sel. spinner","Valor: " + Long.toString(id) );

        if ( sp == spnActividad )
            if ( id==0 )
            {
                desactivarSpinner(spnCategoria, estaVentana.getString(R.string.spinner_txt_cualquiera) );
                desactivarSpinner(spnTipo, estaVentana.getString(R.string.spinner_txt_cualquiera) );
            }
            else
                cargarSpinner_ParametrosBusqueda(ClaseParametro.CATEGORIAS, Long.toString(id));

        if ( sp == spnCategoria )
            if ( id==0 )
                desactivarSpinner(spnTipo, estaVentana.getString(R.string.spinner_txt_cualquiera) );
            else
                cargarSpinner_ParametrosBusqueda(ClaseParametro.TIPOS, Long.toString(id));

        if ( sp == spnProvincia )
            if ( id==0 )
                desactivarSpinner(spnPoblacion, estaVentana.getString(R.string.spinner_txt_primeroProvincia) );
            else
                cargarSpinner_ParametrosBusqueda(ClaseParametro.POBLACIONES, Long.toString(id));
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


    // Muestra en los textview correspondiente el valor de la distancia seleccionada en la barra de distancia,
    // seguido de la unidad de longitud indicada
    // (método de clase privado)
    private void mostrarDistancia(int valor, String unidades)
    {
        txtDistancia.setText(String.valueOf(valor));
        txtUnidad.setText(unidades);
    }


    // Repliega o despliega las secciones de búsqueda correspondientes, según el estado del checkBox indicado
    // Si marcado -----> hacer visibles los elementos de la lista
    // Si desmarcado --> ocultar los elementos de la lista
    // (método de clase privado)
    private void des_plegar_seccionesBusqueda(CheckBox chk)
    {
        ArrayList<View> elementos;

        if ( chk == chkTipologia )
            elementos = elementosPlegables_tipologia;
        else if ( chk == chkProximidad )
            elementos = elementosPlegables_proximidad;
        else if ( chk == chkTexto )
            elementos = elementosPlegables_texto;
        else
            return;

        if (chk.isChecked())
            for (int i = 0; i < elementos.size(); i++)
            {
                // Si hay que desplegar la sección de proximidad y el radioButton de ubicación está marcado,
                // entonces no desplegamos la parte de los spinners de provincia y población
                if ( chk == chkProximidad && rbtnUbicacion.isChecked() && elementos.get(i) instanceof TableLayout
                        && ((TableLayout)elementos.get(i))==layoutPoblaciones ) ;
                else
                    elementos.get(i).setVisibility(View.VISIBLE);
            }
        else
            for (int i = 0; i < elementos.size(); i++)
                elementos.get(i).setVisibility(View.GONE);
    }


    // Comportamiento de un checkbox de criterio de búsqueda al pulsarlo
    // (llamada al método des_plegar_seccionesBusqueda)
    // (método de clase privado)
    private void definirListener_CheckBox(final CheckBox chk)
    {
        final CheckBox c = chk;

        chk.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                des_plegar_seccionesBusqueda(chk);
                            }
                        }
                );
    }


    // Comportamiento de los FrameLayout que contienen los checkboxes de cada sección de búsqueda
    // (replica el comportamiento que tendría si se hace click en el checkBox correspondiente)
    // Este método se define en la propiedad onClick de esos layouts
    public void onClick_LayoutCheckBox(View v)
    {
        if (!(v instanceof FrameLayout))
            return;

        FrameLayout layout = (FrameLayout) v;

        if (layout == layoutChkTipologia)
        {
            chkTipologia.setChecked( !chkTipologia.isChecked() );
            des_plegar_seccionesBusqueda(chkTipologia);
        }

        if (layout == layoutChkProximidad)
        {
            chkProximidad.setChecked( !chkProximidad.isChecked() );
            des_plegar_seccionesBusqueda(chkProximidad);
        }

        if (layout == layoutChkTexto)
        {
            chkTexto.setChecked( !chkTexto.isChecked() );
            des_plegar_seccionesBusqueda(chkTexto);
        }
    }


    // Llamar al servicio web que carga los datos a mostrar en alguno de los spinners de búsqueda
    // (aunque para las clases ACTIVIDADES y PROVINCIAS se ignorará, el valor de id debe indicarse siempre como un String con un entero mayor que 0)
    // (NOTA: este método solo lanza la petición por red, la carga de los datos recibidos se realiza en procesarResultado)
    // (método de clase privado)
    private void cargarSpinner_ParametrosBusqueda(ClaseParametro claseParametro, String id)
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
            String tipo_usuario = "profesional";

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

            String id_superClase = id;


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
                // (incluyendo "clase" también como parámetro local, para poder recuperarlo a posteriori)
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.SPINNER_VALUES, estaVentana, clase);

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
                TareaSegundoPlano tareaGetParametrosBusqueda = new TareaSegundoPlano(miClienteWeb,estaVentana);

                // Ejecutar la tarea de validar las credenciales (en un hilo aparte del principal)
                Log.d("VentanaBusquedaObra", "Solicitando datos de spinner para la clase " + clase + "...");
                tareaGetParametrosBusqueda.execute();
            }
        }
    }


    // Elimina todos los resultados que pudiera haber de una búsqueda anterior en la pestaña de resultados
    // (método de clase privado)
    private void eliminarResultados()
    {
        // Eliminar los datos de la última búsqueda, si los había
        String[] titulosObras = new String[1];
        titulosObras[0] = estaVentana.getString(R.string.VentanaBusqueda_txt_sinBusquedas);

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

        listaObras.setAdapter(miAdapter);

        // Deshabilitar el click sobre el ListView de resultados
        listaObras.setSelector(android.R.color.transparent);

        // Actualizar el título de la pestaña de resultados, para quitar el número de coincidencias (si lo había)
        String txt = getResources().getString(R.string.VentanaBusqueda_txt_tabResultados);
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        ((TextView) tabHost.getTabWidget().getChildTabViewAt(1).findViewById(android.R.id.title)).setText(txt);

        // Actualizar el menú de la ventana (para ocultar la opción de ordenar resultados)
        hayResultados = false;
        onCreateOptionsMenu(menuActionBar);
    }



    // Restaura los valores por defecto en la ventana de búsqueda
    // (tanto en la pestaña de criterios como en la de resultados de búsqueda)
    public void limpiarFormulario()
    {
        // Cambios en la pestaña de criterios de búsqueda
        // ---------------------------------------------------------------------------------------------

        // Seleccionar los valores por defecto en los spinners principales (actividades y provincias)
        // Desactivar y vaciar los spinners de categorias, tipos y poblaciones
        spnActividad.setSelection(0);
        desactivarSpinner(spnCategoria, estaVentana.getString(R.string.spinner_txt_cualquiera) );
        desactivarSpinner(spnTipo, estaVentana.getString(R.string.spinner_txt_cualquiera) );
        spnProvincia.setSelection(0);
        desactivarSpinner(spnPoblacion, estaVentana.getString(R.string.spinner_txt_primeroProvincia) );

        // Poner la barra de distancia y el texto indicativo a 0
        skbDistancia.setProgress(0);
        mostrarDistancia(0, estaVentana.getString(R.string.general_txt_unidadDistancia) );

        // Seleccionar la ubicación actual como criterio de proximidad por defecto
        rbtnUbicacion.setChecked(true);

        // Borrar los valores introducidos en el campo de texto (si había alguno)
        etxtTextoBusqueda.setText("");

        // Desmarcar los checkboxes y plegar las secciones de búsqueda
        chkTipologia.setChecked( false );
        chkProximidad.setChecked( false );
        chkTexto.setChecked( false );
        des_plegar_seccionesBusqueda(chkTipologia);
        des_plegar_seccionesBusqueda(chkProximidad);
        des_plegar_seccionesBusqueda(chkTexto);


        // Cambios en la pestaña de resultados de búsqueda
        // ---------------------------------------------------------------------------------------------

        eliminarResultados();

    }


    // Comprueba si los datos indicados en el formulario son correctos para solicitar una búsqueda al servidor
    // Si lo son, devuelve un String = null
    // Si no son correctos, devuelve un String con la explicación del problema
    // (método de clase interno)
    private String validarDatosBusqueda()
    {
        String msg = null;

        if ( !chkTipologia.isChecked() && !chkProximidad.isChecked() && !chkTexto.isChecked() )
        {
            msg = getResources().getString(R.string.VentanaBusqueda_txt_msgFormulario1);
        }

        else if ( chkTipologia.isChecked() && spnActividad.getSelectedItemId()==0 )
        {
            msg = getResources().getString(R.string.VentanaBusqueda_txt_msgFormulario2);
        }

        else if ( chkProximidad.isChecked() && rbtnPoblacion.isChecked() && spnPoblacion.getSelectedItemId()==0 )
        {
            msg = getResources().getString(R.string.VentanaBusqueda_txt_msgFormulario3);
        }

        else if ( chkTexto.isChecked() )
        {
            String texto = etxtTextoBusqueda.getText().toString();
            ArrayList<String> palabras = new ArrayList<String>();

            for( String p : texto.split("\\W") )
                palabras.add(p);

            String masCorta = "";
            if ( palabras.size()>0 )
            {
                masCorta = palabras.get(0);
                for (String p : palabras)
                    if (p.length() < masCorta.length())
                        masCorta = p;
            }

            if ( masCorta.length() < 4 )
                msg = getResources().getString(R.string.VentanaBusqueda_txt_msgFormulario4);
        }

        return msg;
    }


    // Devuelve un ArrayList<BasicNameValuePair> con los parámetros de búsqueda para una petición al servicio web WORK_SEARCH
    // Esta función no realiza ninguna comprobación, asumiendo que todos los datos del formulario son correctos (esto debe comprobarse antes)
    // (método de clase interno)
    private ArrayList<BasicNameValuePair> getParametrosBusqueda()
    {
        ArrayList<BasicNameValuePair> lista = new ArrayList();


        // Independiéntemente del criterio de búsqueda, enviaremos siempre la ubicación actual del usuario
        String ubicacion = Utils.ubicacionActual(estaVentana, gestorUbicacion);
        lista.add( new BasicNameValuePair("ubicacion",ubicacion) );


        // Si uno de los criterios seleccionados es la búsqueda por Tipología de obra
        if ( chkTipologia.isChecked() )
        {
            lista.add( new BasicNameValuePair("criterioTipologia","true") );

            // Seleccionar el nivel de tipología más bajo seleccionado por el usuario para la búsqueda (tipo < categoría < actividad)
            String nivelTipologia;
            String valorTipologia;

            if ( spnTipo.getSelectedItemId()!=0 )
            {
                nivelTipologia = "tipo";
                valorTipologia = Long.toString( spnTipo.getSelectedItemId() );
            }

            else if ( spnCategoria.getSelectedItemId()!=0 )
            {
                nivelTipologia = "categoria";
                valorTipologia = Long.toString(spnCategoria.getSelectedItemId());
            }

            else
            {
                nivelTipologia = "actividad";
                valorTipologia = Long.toString(spnActividad.getSelectedItemId());
            }

            lista.add( new BasicNameValuePair("nivelTipologia",nivelTipologia) );
            lista.add( new BasicNameValuePair("valorTipologia",valorTipologia) );
        }
        else
            lista.add( new BasicNameValuePair("criterioTipologia","false") );


        // Si uno de los criterios seleccionados es la búsqueda por Proximidad
        if ( chkProximidad.isChecked() )
        {
            lista.add( new BasicNameValuePair("criterioProximidad","true") );

            // Si el valor de la distancia indicada es 0, cambiarlo por 1
            // (nunca se envía al servidor un valor de distancia = 0)
            String dist = txtDistancia.getText().toString();
            if ( dist.equals("0") )
                dist = "1";

            lista.add( new BasicNameValuePair("distancia", dist) );
            lista.add( new BasicNameValuePair("unidad", txtUnidad.getText().toString()) );


            String poblacion;

            if ( rbtnUbicacion.isChecked() )
            {
                poblacion = "00";
            }
            else
            {
                poblacion = Long.toString( spnPoblacion.getSelectedItemId() );
            }

            lista.add( new BasicNameValuePair("poblacion",poblacion) );
        }
        else
            lista.add( new BasicNameValuePair("criterioProximidad","false") );


        // Si uno de los criterios seleccionados es la búsqueda por Texto
        if (chkTexto.isChecked()) {
            lista.add(new BasicNameValuePair("criterioTexto", "true"));
            lista.add(new BasicNameValuePair("textoBusqueda", etxtTextoBusqueda.getText().toString()));
        }
        else
            lista.add( new BasicNameValuePair("criterioTexto","false") );


        return lista;
    }


    // Envía los criterios de búsqueda al servidor, si son correctos, a través de una petición al servicio web PROFESSIONAL_SEARCH
    // (NOTA: este método solo lanza la petición por red, la respuesta y carga de los datos recibidos se realiza en procesarResultado)
    public void iniciarBusqueda()
    {
        String res = validarDatosBusqueda();

        // Si los criterios de búsqueda no están correctos, mostrar un toast de aviso y no hacer nada más
        if ( res != null )
        {
            Utils.mostrarMensaje(estaVentana, res, Utils.TipoMensaje.TOAST, null, null);
            return;
        }

        // Si los criterios de búsqueda son correctos, preparar y enviar la petición de búsqueda al servidor
        //Utils.mostrarMensaje(estaVentana,"Buscando coincidencias...", Utils.TipoMensaje.TOAST, null, null);

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

            // Idioma del cliente
            String idioma = Utils.idiomaAplicacion();

            // Resto de parámetros de la petición
            // (listado con todos los criterios y valores de búsqueda especificados por el usuario)
            ArrayList<BasicNameValuePair> parametrosBusqueda = getParametrosBusqueda();


            // Si el dispositivo no está conectado a la red, mostrar aviso al usuario
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( !Utils.dispositivoConectado(cm) )
            {
                String titulo = getResources().getString(R.string.titulo_servicio_WORK_SEARCH);
                String msg = getResources().getString(R.string.general_txt_dispositivoSinConexion);
                Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.DIALOGO, Utils.CategoriaDialogo.ADVERTENCIA, titulo);
            }

            // Si el dispositivo está conectado a la red, conectar con el servidor para:
            // 1) intentar iniciar validar esas credenciales.
            // 2) si las credenciales son válidas, hacer la petición
            else
            {
                // Construir el servicio web con los parámetros de entrada
                ServicioWeb miServicioWeb = new ServicioWeb(gestorSesion.usarConexionSegura(), ServicioWeb.Tipo.WORK_SEARCH, estaVentana);

                miServicioWeb.addParam("usuario",id_usuario);
                miServicioWeb.addParam("sesion", id_sesion);
                miServicioWeb.addParam("idioma", idioma);

                miServicioWeb.addParamList(parametrosBusqueda);


                // Construir el cliente para la consulta Http(s)
                ClienteServicioWeb miClienteWeb = new ClienteServicioWeb(estaVentana,miServicioWeb);


                // Crear una tarea asíncrona para conectar con el servicio web
                TareaSegundoPlano tareaBuscarObrass = new TareaSegundoPlano(miClienteWeb,estaVentana,pDialog,null,null);

                // Ejecutar la tarea de buscar profesionales que conicidan con los criterios indicados (en un hilo aparte del principal)
                Log.d("VentanaBusquedaObra", "Buscando obras... : " + parametrosBusqueda);
                tareaBuscarObrass.execute();
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
                Log.d("VentanaBusquedaObra", "Modificando seguimiento ("+ Boolean.toString(seguir) +") para la obra "+ id_obra + "...");
                tareaModificarSeguimiento.execute();
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


        // Lista de posibles respuestas de error que puede recibir esta ventana y el tratamiento que debe dárse a cada una
        // (sin incluir las respuestas de sesion expirada, sesión inválida o usuario deshabilitado, que ya se tratan por defecto)
        ArrayList<ErrorServicioWeb> listaErrores = new ArrayList();

        // Posibles errores del servicio SPINNER_VALUES a los que se desea dar un tratamiento particular
        // ...

        // Posibles errores del servicio WORK_SEARCH a los que se desea dar un tratamiento particular
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
        if ( respuesta_OK )
        {
            // Servicio web asociado a la tarea, que contiene la respuesta del servidor
            ServicioWeb miServicioWeb = tarea.getCliente().getServicioWeb();

            if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.SPINNER_VALUES )
            {
                procesarParametrosBusqueda(miServicioWeb.getRespuesta(), (String) miServicioWeb.getParametroLocal());
            }

            else if ( miServicioWeb.getTipo()==ServicioWeb.Tipo.WORK_SEARCH )
            {
                procesarBusquedaObras(miServicioWeb.getRespuesta());
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


    // Procesamiento de la respuesta OK al servicio SPINNER_VALUES
    private void procesarParametrosBusqueda (RespuestaServicioWeb respuesta, String clase)
    {
        Adapter_Spinner miAdapter;

        // La respuesta devuelve un ArrayList<ContenidoServicioWeb>,
        // pero el adapter para el spinner necesita un ArrayList<Info_ElementoSpinner>
        ArrayList<Info_ElementoSpinner> items = new ArrayList();

        int tam = respuesta.getContenido().size();

        for (int i=0; i<tam; i++)
        {
            Info_ElementoSpinner item = (Info_ElementoSpinner) respuesta.getContenido().get(i);
            items.add(item);
        }

        // Dependiendo de que clase de datos se trate, seleccionamos un spinner distinto
        // (y el texto por defecto también será distino, aunque su clave siempre será 0)
        String opcionDefecto = "";
        Spinner spinner = null;

        if ( clase.equals("actividades") )
        {
            opcionDefecto = getResources().getString(R.string.spinner_txt_seleccionaActividad);
            spinner = spnActividad;
        }

        else if ( clase.equals("categorias") )
        {
            opcionDefecto = getResources().getString(R.string.spinner_txt_cualquiera);
            spinner = spnCategoria;
        }

        else if ( clase.equals("tipos") )
        {
            opcionDefecto = getResources().getString(R.string.spinner_txt_cualquiera);
            spinner = spnTipo;
        }

        else if ( clase.equals("provincias") )
        {
            opcionDefecto = getResources().getString(R.string.spinner_txt_seleccionaProvincia);
            spinner = spnProvincia;
        }

        else if ( clase.equals("poblaciones") )
        {
            opcionDefecto = getResources().getString(R.string.spinner_txt_seleccionaPoblacion);
            spinner = spnPoblacion;
        }


        if ( spinner != null)
        {
            miAdapter = new Adapter_Spinner(estaVentana,R.layout.elemento_spinner,items,opcionDefecto);
            spinner.setAdapter(miAdapter);
            spinner.setEnabled(true);
        }


        // Una vez que se han cargado los datos de la ventana, mostrar el layout de la pestaña de búsqueda
        // (si no estaba visible ya)
        tabCriterioBusqueda.setVisibility(View.VISIBLE);
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
            infoObra = (Info_CabeceraObra) listaObras.getItemAtPosition(pos);
        }
        catch (ClassCastException ex)
        {
            Log.e("VentanaBusquedaObra","Error al hacer un cast a Info_CabeceraObra desde el adapter (pos: "+ Integer.toString(pos) +")");
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
        ((Adapter_CabeceraObra) listaObras.getAdapter()).notifyDataSetChanged();

        // Mensaje de confirmación al usuario
        Utils.mostrarMensaje(estaVentana, msg, Utils.TipoMensaje.TOAST, null, null);
    }


    // Procesamiento de la respuesta OK al servicio WORK_SEARCH
    private void procesarBusquedaObras (RespuestaServicioWeb respuesta)
    {
        ArrayList<ContenidoServicioWeb> contenido = respuesta.getContenido();
        int tam = contenido.size();

        // Actualizar el título de la pestaña, para incluir el número de seguidores
        String txt = getResources().getString(R.string.VentanaBusqueda_txt_tabResultados) +" ("+ Integer.toString(tam) +")";
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        ((TextView) tabHost.getTabWidget().getChildTabViewAt(1).findViewById(android.R.id.title)).setText(txt);

        // Si no hay elementos en la lista, mostrar un único elemento con un mensaje usando un ArrayAdapter de Strings
        if (tam < 1)
        {
            String[] titulosProfesionales = new String[1];

            titulosProfesionales[0] = getResources().getString(R.string.VentanaBusqueda_txt_sinResultados_obras);

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

            listaObras.setAdapter(miAdapter);

            // Deshabilitar el click sobre el ListView (si no lo estaba ya)
            listaObras.setSelector(android.R.color.transparent);

            // Registramos que no hay resultados, y actualizamos el menú de la ventana (sin la opción de ordenar resultados)
            hayResultados = false;
            onCreateOptionsMenu(menuActionBar);

            // Mostrar un toast de información al usuario
            Utils.mostrarMensaje(estaVentana, getResources().getString(R.string.msg_BuscarObras_OK_SinCoincidencias), Utils.TipoMensaje.TOAST, null, null);
        }

        // Si hay al menos una cabecera de obra, mostrar sus datos usando un Adapter personalizado de Info_CabeceraObra
        else
        {
            // Creamos el adapter de cabeceras de obra para un usuario profesional, mostrando obras abiertas y la distancia actual a las obras
            Adapter_CabeceraObra miAdapter = new Adapter_CabeceraObra(estaVentana, R.layout.elemento_cabecera_obra, respuesta.getContenido(), false, GestorSesiones.TipoUsuario.PROFESIONAL, true);
            listaObras.setAdapter(miAdapter);

            // Actualizar el menú de la ventana (para incluir las opciones de ordenar los resultados)
            hayResultados = true;
            onCreateOptionsMenu(menuActionBar);


            // Una vez cargados los datos, llevar al usuario a la pestaña de resultados
            tabHost.setCurrentTab(1);

            // Mostrar un toast de información al usuario
            Utils.mostrarMensaje(estaVentana, tam + " " + getResources().getString(R.string.msg_BuscarObras_OK_HayCoincidencias), Utils.TipoMensaje.TOAST, null, null);
        }
    }


    // Método invocado para mostrar el menú contextual de algún elemento de la página
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        // Si el elemento en cuestión es el ListView que contiene las obras encontrados por la búsqueda
        if ( v.getId()==R.id.listaObras)
        {
            // En ese caso, el objeto menuInfo será del subtipo AdapterContextMenuInfo
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            // Título y textos de las opciones del menú a mostrar
            String tituloMenu = "";
            String[] opcionesMenu = null;

            // Obtener una referencia a los datos del objeto de la lista, a traves de su adapter
            // (será un objeto Info_CabeceraObra)
            // Si el listView no contiene elementos (solo un texto indicando que está vacío),
            // entonces al hacer el casting saltará una excepcion que hemos de capturar
            Object datosElementoSeleccionado = listaObras.getAdapter().getItem(info.position);

            try
            {
                tituloMenu = ((Info_CabeceraObra) datosElementoSeleccionado).getTitulo();

                // Dependiendo de si ya estamos siguiendo esa obra, se mostrara la opción de Seguir o de Olvidar la obra
                boolean seguida = ((Info_CabeceraObra) datosElementoSeleccionado).esSeguidor();

                if ( seguida )
                    opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaBusquedaObra_cabeceraObra_seguida);
                else
                    opcionesMenu = getResources().getStringArray(R.array.menuContextual_ventanaBusquedaObra_cabeceraObra_noSeguida);
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
        // (será un objeto Info_CabeceraObra)
        // Si el listView no contiene elementos (solo un texto indicando que está vacío),
        // entonces al hacer el casting saltará una excepcion que hemos de capturar
        Object datosElementoSeleccionado = listaObras.getItemAtPosition(posLista);

        try
        {
            Info_CabeceraObra infoObra = (Info_CabeceraObra) datosElementoSeleccionado;
            String idObra =  infoObra.getId();

            // Opción de ver detalles de la obra
            if (indiceMenu == 0)
            {
                Intent intent = new Intent(estaVentana, VentanaDatosObra.class);

                Bundle b = new Bundle();
                b.putString("id_obra", idObra);
                b.putSerializable("tipo_usuario", GestorSesiones.TipoUsuario.PROFESIONAL);
                intent.putExtras(b);

                startActivity(intent);
            }

            // Opción de seguir/dejar de seguir obra
            else if (indiceMenu == 1)
            {
                boolean siguiendoObra = infoObra.esSeguidor();

                modificarSeguimiento(idObra, !siguiendoObra, posLista);
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

        getSupportActionBar().setTitle(Html.fromHtml("<font color='" + color + "'>" + tituloVentana + "</font>") );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if ( menuActionBar == null )
            menuActionBar = menu;

        // Limpiar el menú desplegable de la ventana (si lo había)
        menu.clear();

        if ( hayResultados )
            getMenuInflater().inflate(R.menu.menu_ventana_busqueda_obra_con_resultados, menu);
        else
            getMenuInflater().inflate(R.menu.menu_ventana_busqueda_obra_sin_resultados, menu);

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
                return true;

            // Opción de repetir la búsqueda
            case R.id.menu_repetir_busqueda:

                eliminarResultados();
                iniciarBusqueda();

                return true;


            // Opción de resetear el formulario de búsqueda
            case R.id.menu_resetear_formulario:

                limpiarFormulario();
                tabHost.setCurrentTab(0);

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


            // Opción de ordenar las obras encontradas por título ascendente
            case R.id.menu_ordenar_titulo_asc:

                if ( listaObras.getAdapter() instanceof Adapter_CabeceraObra )
                {
                    Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaObras.getAdapter();
                    a.ordenar(Info_CabeceraObra.Comparadores.Criterio.TITULO_ASCENDENTE);
                }

                return true;


            // Opción de ordenar las obras encontradas por tipo (por su id) ascendente
            case R.id.menu_ordenar_tipo_asc:

                if ( listaObras.getAdapter() instanceof Adapter_CabeceraObra )
                {
                    Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaObras.getAdapter();
                    a.ordenar(Info_CabeceraObra.Comparadores.Criterio.TIPO_ASCENDENTE);
                }

                return true;


            // Opción de ordenar las obras encontradas por antiguedad descendente
            case R.id.menu_ordenar_antiguedad_desc:

                if ( listaObras.getAdapter() instanceof Adapter_CabeceraObra )
                {
                    Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaObras.getAdapter();
                    a.ordenar(Info_CabeceraObra.Comparadores.Criterio.ANTIGUEDAD_DESCENDENTE);
                }

                return true;


            // Opción de ordenar las obras encontradas por fecha de realización ascendente
            case R.id.menu_ordenar_realizacion_asc:

                if ( listaObras.getAdapter() instanceof Adapter_CabeceraObra )
                {
                    Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaObras.getAdapter();
                    a.ordenar(Info_CabeceraObra.Comparadores.Criterio.REALIZACION_ASCENDENTE);
                }

                return true;


            // Opción de ordenar las obras encontradas por fecha de realización descendente
            case R.id.menu_ordenar_realizacion_desc:

                if ( listaObras.getAdapter() instanceof Adapter_CabeceraObra )
                {
                    Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaObras.getAdapter();
                    a.ordenar(Info_CabeceraObra.Comparadores.Criterio.REALIZACION_DESCENDENTE);
                }

                return true;


            // Opción de ordenar las obras encontradas por seguidores descendente
            case R.id.menu_ordenar_visitas_desc:

                if ( listaObras.getAdapter() instanceof Adapter_CabeceraObra )
                {
                    Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaObras.getAdapter();
                    a.ordenar(Info_CabeceraObra.Comparadores.Criterio.VISITAS_DESCENDENTE);
                }

                return true;


            // Opción de ordenar las obras encontradas por distancia ascendente
            case R.id.menu_ordenar_distancia_asc:

                if ( listaObras.getAdapter() instanceof Adapter_CabeceraObra )
                {
                    Adapter_CabeceraObra a = (Adapter_CabeceraObra) listaObras.getAdapter();
                    a.ordenar(Info_CabeceraObra.Comparadores.Criterio.DISTANCIA_ASCENDENTE);
                }

                return true;


            default:
                return super.onOptionsItemSelected(item);
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
