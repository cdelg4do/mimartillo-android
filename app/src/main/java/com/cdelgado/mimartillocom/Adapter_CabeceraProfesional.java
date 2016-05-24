package com.cdelgado.mimartillocom;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;


public class Adapter_CabeceraProfesional extends BaseAdapter
{
    private ArrayList<Info_CabeceraProfesional> datos;
    private Context contexto;
    private int layout;

    private String id_adjudicatario;

    private boolean mostrarDistancia;

    private LayoutInflater inflater;

    private static final String TAG_FAVORITO    = "favorito";
    private static final String TAG_NO_FAVORITO = "noEsFavorito";


    // Constructor de la clase (con el parámetro del id del adjudicatario de una obra)
    // Se utiliza para mostrar los profesionales interesados en una obra (en VentanaDatosObra).
    // Si encuentra una coincidencia en el id de la lista de datos con el adjudicatario indicado, lo mostrará
    public Adapter_CabeceraProfesional(Context c, int l, ArrayList<Info_CabeceraProfesional> d, String adjudicatario)
    {
        contexto = c;
        datos    = d;
        layout   = l;     // R.layout.elemento_cabecera_profesional
        id_adjudicatario = adjudicatario;
        mostrarDistancia = false;   // No se mostrará ninguna distancia al profesional en la ventana de datos de una obra.

        inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    // Constructor de la clase (no realizará ninguna búsqueda de adjudicatario en la lista de datos)
    // Se utiliza para mostrar los profesionales encontrados en una búsqueda y en la lista de favoritos (en VentanaBusquedaProfesional y VentanaParticular, respectivamente).
    public Adapter_CabeceraProfesional(Context c, int l, ArrayList<Info_CabeceraProfesional> d)
    {
        contexto = c;
        datos    = d;
        layout   = l;     // R.layout.elemento_cabecera_profesional
        id_adjudicatario = null;
        mostrarDistancia = true;   // Se mostrará la distancia al profesional

        inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount()
    {
        return datos.size();
    }


    @Override
    public Info_CabeceraProfesional getItem(int num)
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
        Info_CabeceraProfesional profesional = datos.get(pos);

        // Si el objeto contenedorFila indicado es null, crear uno nuevo con el layout de cabecera de obra
        if( contenedorFila==null )
        {
            //contenedorFila = inflater.inflate(layout, contenedorPadre, false);
            contenedorFila = inflater.inflate(layout, null);
        }

        // Referencia a los elementos del layout de la cabecera
        ImageView imgCabProAvatar        = (ImageView) contenedorFila.findViewById(R.id.imgCabProAvatar);
        TextView txtCabProNombre         = (TextView) contenedorFila.findViewById(R.id.txtCabProNombre);
        TextView txtCabProPoblacion      = (TextView) contenedorFila.findViewById(R.id.txtCabProPoblacion);
        TextView txtCabProOpiniones      = (TextView) contenedorFila.findViewById(R.id.txtCabProVotos);
        TextView txtCabProVotosDatos     = (TextView) contenedorFila.findViewById(R.id.txtCabProVotosDatos);
        TextView txtCabProCalidad        = (TextView) contenedorFila.findViewById(R.id.txtCabProCalidad);
        TextView txtCabProCalidadDatos   = (TextView) contenedorFila.findViewById(R.id.txtCabProCalidadDatos);
        RatingBar rtnCabProCalidad       = (RatingBar) contenedorFila.findViewById(R.id.rtnCabProCalidad);
        TextView txtCabProPrecio         = (TextView) contenedorFila.findViewById(R.id.txtCabProPrecio);
        TextView txtCabProPrecioDatos    = (TextView) contenedorFila.findViewById(R.id.txtCabProPrecioDatos);
        RatingBar rtnCabProPrecio        = (RatingBar) contenedorFila.findViewById(R.id.rtnCabProPrecio);
        ImageView imgCabProFavorito       = (ImageView) contenedorFila.findViewById(R.id.imgCabProFavorito);

        TextView txtCabProAdjudicatario  = (TextView) contenedorFila.findViewById(R.id.txtCabProAdjudicatario);

        // Layout y contador para mostrar la distancia a la obra (solo se usará si mostrarDistancia es true)
        TableRow tbrCabProDistancia = (TableRow) contenedorFila.findViewById(R.id.tbrCabProDistancia);
        TextView distancia    = (TextView) contenedorFila.findViewById(R.id.txtCabProDistanciaDatos);


        // Asignar a los elementos del layout los valores del profesional
        txtCabProNombre.setText( profesional.getNombre() );

        if ( profesional.getAvatar().equals("default_profile_pic") )
            imgCabProAvatar.setImageResource(R.drawable.default_avatar);
        else
            imgCabProAvatar.setImageBitmap( Utils.descodifica_imagen_base64(profesional.getAvatar()) );

        // Si no hay indicada una población, ocultar su widget correspondiente
        // En caso contrario, mostrar la población y la provincia
        if ( profesional.getPoblacion().equals("n/d") )
            txtCabProPoblacion.setVisibility(View.GONE);
        else
            txtCabProPoblacion.setText( profesional.getPoblacion() +" ("+ profesional.getProvincia() +")" );

        txtCabProVotosDatos.setText( Integer.toString(profesional.getVotos()) );

        // Comprobar si hay que mostrar la distancia hasta el profesional
        if ( mostrarDistancia )
        {
            String textoDistancia = Utils.distanciaUnidadesLocales( contexto , profesional.getDistancia() );  // distancia redondeada + unidades
            distancia.setText( textoDistancia );

            tbrCabProDistancia.setVisibility(View.VISIBLE);
        }
        else
        {
            tbrCabProDistancia.setVisibility(View.GONE);
        }

        // Si el profesional aún no ha sido calificado por nadie,
        // se ocultan los valores y las barras de rating y se muestra un mensaje en su lugar
        if ( profesional.getVotos() < 1 )
        {
            txtCabProCalidad.setVisibility(View.GONE);
            rtnCabProCalidad.setVisibility(View.GONE);
            txtCabProPrecio.setVisibility(View.GONE);
            txtCabProPrecioDatos.setVisibility(View.GONE);
            rtnCabProPrecio.setVisibility(View.GONE);

            txtCabProCalidadDatos.setText( contexto.getResources().getString(R.string.general_txt_profesionalSinValoraciones) );
        }
        // Si tiene algún voto, mostrar el valor medio de su calidad y precio, y las barras de rating correspondientes
        else
        {
            txtCabProCalidadDatos.setText(Utils.formatearReal(profesional.getMediaCalidad(), Utils.idiomaAplicacion() ));
            txtCabProPrecioDatos.setText(Utils.formatearReal(profesional.getMediaPrecio(), Utils.idiomaAplicacion() ));

            rtnCabProCalidad.setRating((float) profesional.getMediaCalidad() );
            rtnCabProPrecio.setRating((float) profesional.getMediaPrecio() );
        }


        // Si se indicó algún id de adjudicatario de obra en el constructor del adapter y coincide con el del profesional en cuestión
        // indicar que este es el adjudicatario en cuestión
        if ( id_adjudicatario != null && id_adjudicatario.equals(profesional.getId()) )
        {
            txtCabProAdjudicatario.setVisibility(View.VISIBLE);
        }

        // en caso contrario, ocultar el texto
        else
            txtCabProAdjudicatario.setVisibility(View.GONE);



        // Si el profesional en cuestión está en la lista de favoritos del usuario, mostrar la estrella amarilla a la derecha.
        // Si no es un favorito, mostrar la estrella vacía en su lugar.
        // Indicar el estado de la imagen en Tag(R.id.TAG_FAVORITES_STATUS) de la estrella de favorito
        if ( profesional.esFavorito() )
        {
            imgCabProFavorito.setImageResource(R.drawable.ic_estrella);
            imgCabProFavorito.setTag( R.id.TAG_FAVORITES_STATUS , TAG_FAVORITO);
        }
        else
        {
            imgCabProFavorito.setImageResource(R.drawable.ic_estrellavacia);
            imgCabProFavorito.setTag( R.id.TAG_FAVORITES_STATUS , TAG_NO_FAVORITO);
        }

        // Guardar el id del profesional asociado en Tag(R.id.TAG_FAVORITES_ID) de la estrella de favorito
        // (para saber a qué profesional nos referimos cuando pulsamos la estrella)
        imgCabProFavorito.setTag( R.id.TAG_FAVORITES_ID , profesional.getId() );

        // Guardar la posicion dentro del ListView del elmento que contiene la estrella en Tag(R.id.TAG_FAVORITES_POS) de la estrella de favorito
        // (para saber qué posición del ListView hay que actualizar cuando pulsamos la estrella)
        imgCabProFavorito.setTag( R.id.TAG_FAVORITES_POS , new Integer(pos) );


        // Comportamiento al pulsar la estrella de Favoritos: se llama al método modificarFavorito de la ventana
        // (se pasan como argumentos los datos del id del profesional y la acción a realizar, obtenidos a partir
        // de los tags de la imagen estrella de favorito, así como el propio objeto ImageView)
        imgCabProFavorito.setOnClickListener
        (
            new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    ImageView i = (ImageView) v;
                    String id_profesional = (String) v.getTag(R.id.TAG_FAVORITES_ID);
                    int pos = ((Integer) v.getTag(R.id.TAG_FAVORITES_POS)).intValue();

                    boolean agregarFavorito = true;
                    if ( ((String)v.getTag(R.id.TAG_FAVORITES_STATUS)).equals(TAG_FAVORITO) )
                        agregarFavorito = false;


                    // Esta clase de adapter puede ser utilizado bien en una actividad VentanaDatosObra, una VentanaParticular o una VentanaBusquedaProfesional
                    // así que hay discriminar desde que tipo de ventana fue invocado

                    // Desde la lista de seguidores de una obra
                    if (contexto instanceof VentanaDatosObra)
                    {
                        VentanaDatosObra ctx = (VentanaDatosObra) contexto;
                        ctx.modificarFavorito(id_profesional,agregarFavorito,pos);
                    }

                    // Desde la lista de favoritos del usuario
                    else if (contexto instanceof VentanaParticular)
                    {
                        VentanaParticular ctx = (VentanaParticular) contexto;
                        ctx.modificarFavorito(id_profesional,agregarFavorito,pos);
                    }

                    // Desde la lista de resultados de búsqueda de profesionales
                    else if (contexto instanceof VentanaBusquedaProfesional)
                    {
                        VentanaBusquedaProfesional ctx = (VentanaBusquedaProfesional) contexto;
                        ctx.modificarFavorito(id_profesional,agregarFavorito,pos);
                    }
                }
            }
        );


        // Por último, añadir una referencia al id de la obra en el objeto view devuelto
        // (para poder identificar la obra cuando el usuario pulse en un elemento de la lista)
        contenedorFila.setTag( profesional.getId() );

        // Devolver referencia al objeto View creado
        return contenedorFila;
    }


    // Método para ordenar los datos que contiene el adapter, de acuerdo a diferentes criterios
    public void ordenar(Info_CabeceraProfesional.Comparadores.Criterio criterio)
    {
        if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.NOMBRE_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorNombreAsc);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.POBLACION_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorPoblacionAsc);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.PROVINCIA_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorProvinciaAsc);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.VOTOS_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorVotosAsc);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.VOTOS_DESCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorVotosDesc);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.CALIDAD_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorCalidadAsc);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.CALIDAD_DESCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorCalidadDesc);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.PRECIO_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorPrecioAsc);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.PRECIO_DESCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorPrecioDesc);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.FAVORITOS_PRIMERO )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorFavoritosPrimero);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.FAVORITOS_DESPUES )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorFavoritosDespues);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.DISTANCIA_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorDistanciaAsc);

        else if ( criterio == Info_CabeceraProfesional.Comparadores.Criterio.DISTANCIA_DESCENDENTE )
            Collections.sort(datos, Info_CabeceraProfesional.Comparadores.ComparadorPorDistanciaDesc);

        else
            return;

        notifyDataSetChanged();
    }

}
