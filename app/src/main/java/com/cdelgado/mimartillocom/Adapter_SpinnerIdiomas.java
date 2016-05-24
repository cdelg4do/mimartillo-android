package com.cdelgado.mimartillocom;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;


public class Adapter_SpinnerIdiomas extends BaseAdapter
{
    private Context contexto;
    private int layout;

    private final String[] claves;
    private final String[] imagenes;
    private final String[] valores;

    private LayoutInflater inflater;


    public Adapter_SpinnerIdiomas(Context c)
    {
        contexto = c;
        layout   = R.layout.elemento_spinner_imagen;

        inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Creamos los arrays de claves/imagenes/valores y cargamos los datos de los idiomas (0 -> Español , 1 -> Inglés)
        claves   = new String[ 2 ];
        imagenes = new String[ 2 ];
        valores  = new String[ 2 ];

        // Idioma Español (0)
        claves[0]   = "0";
        imagenes[0] = "flag_es";
        valores[0]  = contexto.getResources().getString(R.string.VentanaPerfil_txt_idiomaNotificacionesEsp);

        // Idioma Inglés (1)
        claves[1]   = "1";
        imagenes[1] = "flag_en";
        valores[1]  = contexto.getResources().getString(R.string.VentanaPerfil_txt_idiomaNotificacionesEng);
    }


    // Tamaño de los arrays de claves/valores
    @Override
    public int getCount()
    {
        return claves.length;
    }

/*
    // Devuelve la posición de la clave indicada en el array de claves
    // (si no existe la clave indicada, devuelve -1)
    public int getPosicionClave(String clave)
    {
        for (int i=0; i< claves.length; i++)
        {
            if ( claves[i].equals(clave) )
                return i;
        }
        return -1;
    }
*/

    // Devuelve el valor guardado en la posicion pos
    @Override
    public String getItem(int pos)
    {
        return valores[pos];
    }


    // Devuelve la clave guardada en la posicion pos
    @Override
    public long getItemId(int pos)
    {
        return Long.parseLong( claves[pos] );
    }


    public View getView(int pos, View contenedorFila, ViewGroup contenedorPadre)
    {
        // Si el objeto contenedorFila indicado es null, crear uno nuevo con el layout de cabecera de obra
        if( contenedorFila==null )
        {
            contenedorFila = inflater.inflate(layout, contenedorPadre, false);
        }

        // Referencia a los elementos del layout
        ImageView imgElemento   = (ImageView) contenedorFila.findViewById(R.id.imgElementoSpinner);
        TextView textoElemento  = (TextView) contenedorFila.findViewById(R.id.txtElementoSpinner);

        // Asignar a los elementos del layout los valores correspondientes
        int id_imagen = contexto.getResources().getIdentifier( imagenes[pos], "drawable", contexto.getPackageName() );
        imgElemento.setImageResource( id_imagen );

        textoElemento.setText( valores[pos] );


        return contenedorFila;
    }

}
