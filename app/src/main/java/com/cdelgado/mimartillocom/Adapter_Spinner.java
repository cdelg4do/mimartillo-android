package com.cdelgado.mimartillocom;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class Adapter_Spinner extends BaseAdapter
{
    private Context contexto;
    private int layout;

    private final String[] claves;
    private final String[] valores;

    private final int tam;
    private LayoutInflater inflater;


    public Adapter_Spinner(Context c, int l, ArrayList<Info_ElementoSpinner> datos, String txtDefecto)
    {
        contexto = c;
        layout   = l;    // R.layout.elemento_spinner

        tam = datos.size();

        inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Creamos los arrays de pares/valores
        // (un elemento más para almacenar el valor por defecto)
        claves  = new String[ tam + 1 ];
        valores = new String[ tam + 1 ];

        // Valor por defecto
        claves[0]  = "0";
        valores[0] = txtDefecto;


        for (int i=0; i<tam; i++)
        {
            Info_ElementoSpinner item = datos.get(i);

            claves[i+1]  = item.getClave();
            valores[i+1] = item.getValor();
        }
    }


    // Tamaño de los arrays de claves/valores (incluido el valor por defecto)
    @Override
    public int getCount()
    {
        return tam + 1;
    }


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
        TextView textoElemento  = (TextView) contenedorFila.findViewById(R.id.txtElementoSpinner);

        // Asignar a los elementos del layout los valores correspondientes
        textoElemento.setText( valores[pos] );

        // Si se trata de la opción por defecto, mostrar en distinto color
        //if ( pos==0 )
        //    textoElemento.setTextColor(R.color.texto_spinnerDefecto);

        return contenedorFila;
    }

}
