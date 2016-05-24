package com.cdelgado.mimartillocom;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;


public class Adapter_Valoraciones extends BaseAdapter
{
    private ArrayList<Info_Valoracion> datos;
    private Context contexto;
    private int layout;
    private GestorSesiones gestorSesion;

    private LayoutInflater inflater;



    public Adapter_Valoraciones(Context c, int l, ArrayList<Info_Valoracion> d, GestorSesiones g)
    {
        contexto     = c;
        datos        = d;
        layout       = l;     // R.layout.elemento_valoracion
        gestorSesion = g;

        inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount()
    {
        return datos.size();
    }


    @Override
    public Info_Valoracion getItem(int num)
    {
        return datos.get(num);
    }


    @Override
    public long getItemId(int num)
    {
        return num;
    }


    @Override
    public View getView(int pos, View contenedorFila, ViewGroup contenedorPadre)
    {
        // Obtener los datos del profesional indicado
        Info_Valoracion valoracion = datos.get(pos);

        // Si el objeto contenedorFila indicado es null, crear uno nuevo con el layout de cabecera de obra
        if( contenedorFila==null )
        {
            //contenedorFila = inflater.inflate(layout, contenedorPadre, false);
            contenedorFila = inflater.inflate(layout, null);
        }

        // Referencia a los elementos del layout de la cabecera
        ImageView imgValAvatar          = (ImageView) contenedorFila.findViewById(R.id.imgValAvatar);
        TextView txtValNombre           = (TextView) contenedorFila.findViewById(R.id.txtValNombre);
        TextView txtValEmail            = (TextView) contenedorFila.findViewById(R.id.txtValEmail);
        TextView txtValFecha            = (TextView) contenedorFila.findViewById(R.id.txtValFecha);
        TextView txtValTituloObra       = (TextView) contenedorFila.findViewById(R.id.txtValTituloObra);
        TextView txtValTipoObra         = (TextView) contenedorFila.findViewById(R.id.txtValTipoObra);
        TextView txtValCalidadDatos     = (TextView) contenedorFila.findViewById(R.id.txtValCalidadDatos);
        TextView txtValPrecioDatos      = (TextView) contenedorFila.findViewById(R.id.txtValPrecioDatos);
        RatingBar rtnValCalidad         = (RatingBar) contenedorFila.findViewById(R.id.rtnValCalidad);
        RatingBar rtnValPrecio          = (RatingBar) contenedorFila.findViewById(R.id.rtnValPrecio);
        TextView txtValPrecioMoneda     = (TextView) contenedorFila.findViewById(R.id.txtValPrecioMoneda);
        TextView txtValComentarioDatos  = (TextView) contenedorFila.findViewById(R.id.txtValComentarioDatos);


        // Asignar a los elementos del layout los valores del profesional
        txtValNombre.setText( valoracion.getNombre_usuario() );

        if ( valoracion.getAvatar().equals("default_profile_pic") )
            imgValAvatar.setImageResource(R.drawable.default_avatar);
        else
        {
            // Si el usuario tiene un avatar personalizado, mostrarlo
            imgValAvatar.setImageBitmap(Utils.descodifica_imagen_base64(valoracion.getAvatar()));

            // Comportamiento de la imagen con avatar personalizado al pulsarlo
            // (abrir la imagen en grande en una ventana nueva)
            imgValAvatar.setOnClickListener
            (
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Bitmap bitmap = ((BitmapDrawable) ((ImageView)v).getDrawable()).getBitmap();
                        String msg = contexto.getResources().getString(R.string.general_txt_imagenPerfil);

                        gestorSesion.setDatosTemporales( Utils.codifica_jpeg_base64(bitmap) );

                        Intent intent = new Intent(contexto, VentanaMostrarImagen.class);
                        Bundle info = new Bundle();
                        info.putString("mensaje",msg);
                        intent.putExtras(info);

                        contexto.startActivity(intent);
                    }
                }
            );
        }

        txtValEmail.setText( "("+ valoracion.getEmail_usuario() +")" );
        txtValFecha.setText( valoracion.getFecha_valoracion() );
        txtValTituloObra.setText( valoracion.getTitulo_obra() );
        txtValTipoObra.setText(valoracion.getTipo_obra());
        txtValCalidadDatos.setText(Integer.toString(valoracion.getNota_calidad()));
        txtValPrecioDatos.setText(Integer.toString(valoracion.getNota_precio()));
        rtnValCalidad.setRating((float) valoracion.getNota_calidad());
        rtnValPrecio.setRating((float) valoracion.getNota_precio());


        // Si la obra tiene un presupuesto especificado (mayor que 0), mostrarlo
        if ( valoracion.getPresupuesto_obra().compareTo( new BigDecimal("0.00") ) == 1 )
        {
            String moneda = contexto.getResources().getString(R.string.simbolo_moneda);
            String presupuesto = Utils.formatearCantidadMonetaria( valoracion.getPresupuesto_obra() , moneda, Utils.idiomaAplicacion() );
            txtValPrecioMoneda.setText( presupuesto );
        }
        // Si no, no mostrar nada en el campo presupuesto
        else
            txtValPrecioMoneda.setText( "" );


        txtValComentarioDatos.setText( valoracion.getComentario() );


        // Por último, añadir una referencia al id de la obra en el objeto view devuelto
        // (para poder identificar la obra cuando el usuario pulse sobre la valoración)
        contenedorFila.setTag( valoracion.getId_obra() );

        // Devolver referencia al objeto View creado
        return contenedorFila;
    }


    // Método para ordenar los datos que contiene el adapter, de acuerdo a diferentes criterios
    public void ordenar(Info_Valoracion.Comparadores.Criterio criterio)
    {
        if ( criterio == Info_Valoracion.Comparadores.Criterio.SOLICITANTE_ASCENDENTE )
            Collections.sort(datos, Info_Valoracion.Comparadores.ComparadorPorSolicitanteAsc);

        else if ( criterio == Info_Valoracion.Comparadores.Criterio.CALIDAD_DESCENDENTE )
            Collections.sort(datos, Info_Valoracion.Comparadores.ComparadorPorCalidadDesc);

        else if ( criterio == Info_Valoracion.Comparadores.Criterio.PRECIO_DESCENDENTE )
            Collections.sort(datos, Info_Valoracion.Comparadores.ComparadorPorPrecioDesc);

        else if ( criterio == Info_Valoracion.Comparadores.Criterio.FECHA_DESCENDENTE )
            Collections.sort(datos, Info_Valoracion.Comparadores.ComparadorPorFechaDesc);

        else
            return;

        notifyDataSetChanged();
    }

}
