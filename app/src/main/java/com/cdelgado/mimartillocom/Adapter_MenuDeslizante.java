package com.cdelgado.mimartillocom;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class Adapter_MenuDeslizante extends BaseAdapter
{
    private String[] imagenes;
    private String[] datos;
    private Context contexto;
    private int layoutPerfil;
    private int layoutOpciones;
    private GestorSesiones.TipoUsuario tipoUsuario;

    private LayoutInflater inflater;



    public Adapter_MenuDeslizante(Context c, GestorSesiones.TipoUsuario t, int p, int l, String[] i, String[] d)
    {
        contexto         = c;
        tipoUsuario      = t;
        imagenes         = i;
        datos            = d;
        layoutPerfil     = p;   // R.layout.elemento_perfil_particular ó R.layout.elemento_perfil_profesional
        layoutOpciones   = l;   // R.layout.elemento_desplegable

        inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount()
    {
        return datos.length;
    }


    @Override
    public String getItem(int num)
    {
        return datos[num];
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
        //Info_Valoracion valoracion = datos.get(pos);

        // Si el objeto contenedorFila indicado es null, crear uno nuevo con el layout correspondiente
        // (la posición 0 corresponde a la foto del usuario, el resto a opciones de menú)
        if( contenedorFila==null )
        {
            if ( pos==0 )
                contenedorFila = inflater.inflate(layoutPerfil, null);
            else
                contenedorFila = inflater.inflate(layoutOpciones, null);
        }

        // Datos del perfil (si es la primera posición) u opción de menú (en el resto de posiciones)
        if ( pos==0 )
        {
            GestorSesiones gestorSesion = new GestorSesiones(contexto, tipoUsuario);

            // Referencia a los elementos del layout del perfil
            ImageView perfil_imgAvatar = (ImageView) contenedorFila.findViewById(R.id.perfil_imgAvatar) ;
            TextView perfil_txtNombre = (TextView) contenedorFila.findViewById(R.id.perfil_txtNombre) ;
            TextView perfil_txtCorreo = (TextView) contenedorFila.findViewById(R.id.perfil_txtCorreo) ;

            // Asignar a los elementos del layout los valores del perfil
            String imagen_base64 = gestorSesion.getDatosSesion().get(gestorSesion.KEY_AVATAR);

            if ( ! imagen_base64.equals("default_profile_pic") )
                perfil_imgAvatar.setImageBitmap( Utils.descodifica_imagen_base64(imagen_base64) );

            perfil_txtNombre.setText( gestorSesion.getDatosSesion().get(gestorSesion.KEY_NOMBRE) );
            perfil_txtCorreo.setText( gestorSesion.getDatosSesion().get(gestorSesion.KEY_EMAIL) );
        }

        else
        {
            // Referencia a los elementos del layout de opciones
            ImageView imgOpcion = (ImageView) contenedorFila.findViewById(R.id.imgOpcion);
            TextView txtOpcion = (TextView) contenedorFila.findViewById(R.id.txtOpcion);

            // Asignar a los elementos del layout los valores de la opción
            int imagen = contexto.getResources().getIdentifier( imagenes[pos], "drawable", contexto.getPackageName() );
            imgOpcion.setImageResource( imagen );

            txtOpcion.setText( datos[pos] );
        }


        // Por último, añadir una referencia al id de la obra en el objeto view devuelto
        // (para poder identificar la obra cuando el usuario pulse sobre la valoración)
        //contenedorFila.setTag( valoracion.getId_obra() );

        // Devolver referencia al objeto View creado
        return contenedorFila;
    }

}
