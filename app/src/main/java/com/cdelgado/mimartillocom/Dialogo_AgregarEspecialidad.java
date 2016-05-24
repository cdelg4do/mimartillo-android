package com.cdelgado.mimartillocom;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

public class Dialogo_AgregarEspecialidad extends Dialog implements AdapterView.OnItemSelectedListener
{
    private VentanaPerfilProfesional ventanaPadre;   // Ventana desde la que se invoca el cuadro de diálogo

    // Los spinners de este cuadro de diálogo tienen que ser public, para poder ser manipulados desde la ventana padre
    public Spinner spnActividad;
    public Spinner spnCategoria;
    public Spinner spnTipo;

    private Button btnOk;
    private Button btnCancelar;



    public Dialogo_AgregarEspecialidad(VentanaPerfilProfesional padre)
    {
        super(padre);
        ventanaPadre = padre;

        String titulo = ventanaPadre.getResources().getString(R.string.VentanaPerfil_txt_agregarEspecialidad);
        this.setTitle(titulo);
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogo_agregar_especialidad);

        // Esto es necesario para que el cuadro de diálogo ocupe toda la ventana
        // (debe invocarse después de la llamada a setContentView() )
        WindowManager.LayoutParams params = getWindow().getAttributes();
        //params.height = ViewGroup.LayoutParams.FILL_PARENT;
        params.width = ViewGroup.LayoutParams.FILL_PARENT;
        getWindow().setAttributes((WindowManager.LayoutParams) params);


        // Referencia a los objetos de la interfaz
        spnActividad = (Spinner) findViewById (R.id.spnActividad);
        spnCategoria = (Spinner) findViewById (R.id.spnCategoria);
        spnTipo = (Spinner) findViewById (R.id.spnTipo);

        btnOk = (Button) findViewById(R.id.btnOk);
        btnCancelar = (Button) findViewById(R.id.btnCancelar);


        // Guardar las referencias de los spinners en la ventana padre (para poder manipularlos desde ella)
        ventanaPadre.spnActividad = this.spnActividad;
        ventanaPadre.spnCategoria = this.spnCategoria;
        ventanaPadre.spnTipo = this.spnTipo;


        // Listener de los spinners al escoger un elemento de la lista
        // (requiere que la ventana implemente AdapterView.OnItemSelectedListener)
        spnActividad.setOnItemSelectedListener(this);
        spnCategoria.setOnItemSelectedListener(this);
        spnTipo.setOnItemSelectedListener(this);


        // Inicialmente, los spinners de Categorías y Tipos están vacíos y deshabilitados
        String txtSinOpciones_categorias = ventanaPadre.getResources().getString( R.string.spinner_txt_primeroActividad );
        String txtSinOpciones_tipos = ventanaPadre.getResources().getString( R.string.spinner_txt_primeroCategoria );

        ventanaPadre.desactivarSpinner(spnCategoria,txtSinOpciones_categorias);
        ventanaPadre.desactivarSpinner(spnTipo, txtSinOpciones_tipos);


        // Inicialmente el botón de OK estará deshabilitado (hasta que se seleccione algo en el spinner de tipos)
        btnOk.setEnabled(false);


        // Cargar los datos de las actividades en el spinner de actividades
        ventanaPadre.cargarSpinner(VentanaPerfilProfesional.ClaseParametro.ACTIVIDADES,"9999");


        // Comportamiento del botón de OK al pulsarlo
        btnOk.setOnClickListener
        (
            new android.view.View.OnClickListener()
            {
                public void onClick(View v)
                {
                    // Si el spinner de tipos contiene más de un elemento, podemos añadir la selección correspondiente a las
                    // especialidades ya seleccionadas en la ventana padre.
                    // Si por alguna razón, el spinner de tipos solo contuviera solo un elemento (0 -> todos los tipos),
                    // iremos directamente a cerrar el cuadro de diálogo sin hacer ninguna operación.
                    if ( spnTipo.getAdapter().getCount() > 1 )
                    {
                        // Nombre e id de la actividad y categoría seleccionadas por el usuario
                        String idAct = Long.toString( spnActividad.getItemIdAtPosition(spnActividad.getSelectedItemPosition()) );
                        String nombreAct = (String) spnActividad.getSelectedItem();

                        String idCat = Long.toString( spnCategoria.getItemIdAtPosition(spnCategoria.getSelectedItemPosition()) );
                        String nombreCat = (String) spnCategoria.getSelectedItem();

                        Info_ElementoSpinner actividad = new Info_ElementoSpinner(idAct, nombreAct);
                        Info_ElementoSpinner categoria = new Info_ElementoSpinner(idCat, nombreCat);

                        // Si se seleccionó la primera opción (Todos los tipos) en el spinner de Tipos, se añadirán todos los subtipos de esa categoría.
                        // Si no, solo se añadirá el tipo seleccionado
                        ArrayList<Info_ElementoSpinner> tipos = new ArrayList();

                        if (spnTipo.getSelectedItemPosition() == 0)
                        {
                            for (int i = 1; i < spnTipo.getAdapter().getCount(); i++)
                            {
                                String idTip = Long.toString( spnTipo.getItemIdAtPosition(i) );
                                String nombreTip = (String) spnTipo.getItemAtPosition(i);

                                Info_ElementoSpinner tipo = new Info_ElementoSpinner(idTip, nombreTip);
                                tipos.add(tipo);
                            }
                        }
                        else
                        {
                            String idTip = Long.toString(spnTipo.getItemIdAtPosition( spnTipo.getSelectedItemPosition()) );
                            String nombreTip = (String) spnTipo.getSelectedItem();

                            Info_ElementoSpinner tipo = new Info_ElementoSpinner(idTip, nombreTip);
                            tipos.add(tipo);
                        }

                        // Agregar el tipo seleccionado en los spinners a la lista de especialidades de la ventana padre
                        Utils.agregarEspecialidad(ventanaPadre.actividadesSeleccionadas, actividad, categoria, tipos);

                        // Actualizar el layout de especialidades de la ventana padre
                        ventanaPadre.actualizarLayoutEspecialidades();

                        // Mostrar un mensaje de confirmación al usuario
                        String msg = ventanaPadre.getResources().getString(R.string.dialogo_agregarEspecialidad_txt_msgConfirmacion);
                        Utils.mostrarMensaje(ventanaPadre, msg, Utils.TipoMensaje.TOAST, null, null);
                    }

                    // Cerrar el cuadro de diálogo
                    Dialogo_AgregarEspecialidad.this.dismiss();
                }
            }
        );


        // Comportamiento del botón de Cancelar al pulsarlo
        btnCancelar.setOnClickListener
        (
            new android.view.View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Dialogo_AgregarEspecialidad.this.dismiss();
                }
            }
        );

    }


    // Comportamiento al seleccionar un elemento de un spinner
    // Si se selecciona la posición (0) en el spinner de actividades o de categorías, desactivar el sub-spinner correspondiente y el botón de OK
    // Si se selecciona otra posición, cargar los valores correspondientes a esa opción en los sub-spinner
    // Si se selecciona una posición distinta de 0 en el spinner de Categorías, además se activa el botón de OK
    // (no hay acción cuando se selecciona un elemento de tipos, ya que no tiene sub-spinners que dependan de él)
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int pos, long id)
    {
        Spinner sp;

        if ( parent instanceof Spinner)
            sp = (Spinner) parent;
        else
            return;


        Log.d("Seleccion spinner", "Valor: " + Long.toString(id));

        if ( sp == spnActividad )
            if ( id==0 )
            {
                String txtSinOpciones_categorias = ventanaPadre.getResources().getString( R.string.spinner_txt_primeroActividad );
                String txtSinOpciones_tipos = ventanaPadre.getResources().getString( R.string.spinner_txt_primeroCategoria );

                ventanaPadre.desactivarSpinner(spnCategoria,txtSinOpciones_categorias);
                ventanaPadre.desactivarSpinner(spnTipo,txtSinOpciones_tipos);

                btnOk.setEnabled(false);
            }
            else
                ventanaPadre.cargarSpinner(VentanaPerfilProfesional.ClaseParametro.CATEGORIAS, Long.toString(id));

        if ( sp == spnCategoria )
            if ( id==0 )
            {
                String txtSinOpciones_tipos = ventanaPadre.getResources().getString( R.string.spinner_txt_primeroCategoria );
                ventanaPadre.desactivarSpinner(spnTipo,txtSinOpciones_tipos);

                btnOk.setEnabled(false);
            }
            else
            {
                ventanaPadre.cargarSpinner(VentanaPerfilProfesional.ClaseParametro.TIPOS, Long.toString(id));

                btnOk.setEnabled(true);
            }

    }


    // Comportamiento al seleccionar ningún elemento de un spinner (no hacer nada)
    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        // TODO Auto-generated method stub
    }



}
