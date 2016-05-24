package com.cdelgado.mimartillocom;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;


public class Adapter_CabeceraObra extends BaseAdapter
{
    private ArrayList<Info_CabeceraObra> datos;
    private Context contexto;
    private int layout;

    private boolean vistaParticular;

    private boolean mostrandoObrasCerradas;
    private boolean mostrarDistancia;  // Solo se tendrá en cuenta si vistaParticular = false

    private LayoutInflater inflater;

    private static final String TAG_SEGUIDOR    = "seguidor";
    private static final String TAG_NO_SEGUIDOR = "noEsSeguidor";




    public Adapter_CabeceraObra(Context c, int l, ArrayList<ContenidoServicioWeb> d, boolean cerradas, GestorSesiones.TipoUsuario tipo, boolean dist)
    {
        contexto                = c;
        layout                  = l;     // R.layout.elemento_cabecera_obra
        mostrandoObrasCerradas  = cerradas;
        mostrarDistancia        = dist;

        // El constructor recibe una lista de ContenidoServicioWeb,
        // pero el adapter trabaja con una lista de Info_CabeceraObra, así que generamos la nueva lista
        datos = new ArrayList<>();
        for (int i=0; i<d.size(); i++)
        {
            if ( d.get(i) instanceof Info_CabeceraObra )
                datos.add( (Info_CabeceraObra)d.get(i) );
        }

        if ( tipo == GestorSesiones.TipoUsuario.PARTICULAR )
            vistaParticular = true;
        else
            vistaParticular = false;

        inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount()
    {
        return datos.size();
    }


    @Override
    public Info_CabeceraObra getItem(int num)
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
        // Obtener los datos de la obra de cabecera indicada
        Info_CabeceraObra obra = datos.get(pos);

        // Si el objeto contenedorFila indicado es null, crear uno nuevo con el layout de cabecera de obra
        if( contenedorFila==null )
        {
            contenedorFila = inflater.inflate(layout, contenedorPadre, false);
        }

        // Referencia a los elementos del layout de la cabecera
        ImageView imagenItem = (ImageView)contenedorFila.findViewById(R.id.img_cabeceraItem);
        TextView tituloObra  = (TextView) contenedorFila.findViewById(R.id.txt_cabeceraTituloObra);
        TextView tipoObra    = (TextView) contenedorFila.findViewById(R.id.txt_cabeceraTipoObra);
        TextView fecha       = (TextView) contenedorFila.findViewById(R.id.txt_cabeceraFechaDatos);
        TextView visitas     = (TextView) contenedorFila.findViewById(R.id.txt_cabeceraVisitasDatos);

        // Layout y contador de seguidores de la obra
        LinearLayout layoutCabObraSeguidores = (LinearLayout) contenedorFila.findViewById(R.id.layoutCabObraSeguidores);
        TextView contador    = (TextView) contenedorFila.findViewById(R.id.txt_cabeceraContadorSeguidores);

        // Layout y contador para mostrar la distancia a la obra (solo se usará en la ventana de búsqueda de obras)
        TableRow tbrCabObraDistancia = (TableRow) contenedorFila.findViewById(R.id.tbrCabObraDistancia);
        TextView distancia    = (TextView) contenedorFila.findViewById(R.id.txt_cabeceraDistanciaDatos);

        // Layout para el aviso de valoración pendiente de la obra
        TextView txtValoracionPendiente = (TextView) contenedorFila.findViewById(R.id.txtCabObraValoracionPendiente);

        // Layout e imagen de seguimiento de la obra
        FrameLayout layoutCabObraSeguimiento = (FrameLayout) contenedorFila.findViewById(R.id.layoutCabObraSeguimiento);
        ImageView imgCabObraSeguimiento = (ImageView) contenedorFila.findViewById(R.id.imgCabObraSeguimiento);


        // Asignar a los elementos de la vista los valores de la obra
        // -----------------------------------------------------------

        imagenItem.setImageResource(Utils.imagenActividadObra(obra.getIdTipo()));
        tituloObra.setText(obra.getTitulo());
        tipoObra.setText( obra.getTipo() );
        visitas.setText( Integer.toString(obra.getVisitas()) );

        // Fecha de realización de la obra (formateada para el idioma actual)
        fecha.setText( Utils.formatearFechaVisible(obra.getFechaRealizacion(), Utils.idiomaAplicacion()) );


        // Si el usuario es un particular:
        //  - El número de seguidores de la obra se mostrará solo si es mayor que 0
        //  - Puede que sea necesario mostrar el aviso de valoración pendiente de la obra (si son cerradas y a la obra aún no fue valorada)
        //  - Puede que sea necesario mostrar la distancia a las obras
        //  - No se muestra el layout con el botón de seguir/olvidar obra
        if ( vistaParticular )
        {
            contador.setText(Integer.toString(obra.getInteresados()));

            if (obra.getInteresados() < 1)  layoutCabObraSeguidores.setVisibility(View.GONE);
            else                            layoutCabObraSeguidores.setVisibility(View.VISIBLE);

            if (mostrandoObrasCerradas && obra.sinValoracion()) txtValoracionPendiente.setVisibility(View.VISIBLE);
            else                                                txtValoracionPendiente.setVisibility(View.GONE);

            if ( mostrarDistancia )
            {
                String textoDistancia = Utils.distanciaUnidadesLocales( contexto , obra.getDistancia() );  // distancia redondeada + unidades
                distancia.setText( textoDistancia );

                tbrCabObraDistancia.setVisibility(View.VISIBLE);
            }
            else
            {
                tbrCabObraDistancia.setVisibility(View.GONE);
            }

            layoutCabObraSeguimiento.setVisibility(View.GONE);
        }

        // Si el usuario es profesional
        //  - No se mostrará el layout con el número de seguidores
        //  - No se mostrará el layout con el aviso de valoración pendiente
        //  - Puede que sea necesario mostrar la distancia a las obras
        //  - Puede que sea necesario mostrar el botón de seguir/olvidar obra (si son cerradas)
        else
        {
            layoutCabObraSeguidores.setVisibility(View.GONE);

            txtValoracionPendiente.setVisibility(View.GONE);


            if ( mostrarDistancia )
            {
                String textoDistancia = Utils.distanciaUnidadesLocales( contexto , obra.getDistancia() );  // distancia redondeada + unidades
                distancia.setText( textoDistancia );

                tbrCabObraDistancia.setVisibility(View.VISIBLE);
            }
            else
            {
                tbrCabObraDistancia.setVisibility(View.GONE);
            }


            if ( mostrandoObrasCerradas )
            {
                layoutCabObraSeguimiento.setVisibility(View.GONE);
            }
            else
            {
                layoutCabObraSeguimiento.setVisibility(View.VISIBLE);

                if (obra.esSeguidor())
                {
                    imgCabObraSeguimiento.setImageResource(R.drawable.ic_bandera);
                    imgCabObraSeguimiento.setTag(R.id.TAG_FOLLOWING_STATUS, TAG_SEGUIDOR);
                }
                else
                {
                    imgCabObraSeguimiento.setImageResource(R.drawable.ic_banderadesactivada);
                    imgCabObraSeguimiento.setTag(R.id.TAG_FOLLOWING_STATUS, TAG_NO_SEGUIDOR);
                }

                // Guardar el id de la obra asociada en Tag(R.id.TAG_WORK_ID) de la bandera de seguimiento
                // (para saber a qué obra nos referimos cuando pulsamos la bandera)
                imgCabObraSeguimiento.setTag(R.id.TAG_WORK_ID, obra.getId());

                // Guardar la posicion dentro del ListView del elmento que contiene la bandera en Tag(R.id.TAG_WORK_POS) de la bandera de seguimiento
                // (para saber qué posición del ListView hay que actualizar cuando pulsamos la bandera)
                imgCabObraSeguimiento.setTag( R.id.TAG_WORK_POS , new Integer(pos) );


                // Comportamiento al pulsar la bandera de seguimiento: se llama al método modificarSeguimiento de la ventana
                // (se pasan como argumentos los datos del id de la obra y la acción a realizar, obtenidos a partir
                // de los tags de la imagen bandera de seguimiento)
                imgCabObraSeguimiento.setOnClickListener
                (
                    new View.OnClickListener()
                    {
                        public void onClick(View v)
                        {
                            String id_profesional = (String) v.getTag(R.id.TAG_WORK_ID);
                            int pos = ((Integer) v.getTag(R.id.TAG_WORK_POS)).intValue();

                            boolean seguirObra = true;
                            if (((String) v.getTag(R.id.TAG_FOLLOWING_STATUS)).equals(TAG_SEGUIDOR))
                                seguirObra = false;


                            // Esta clase de adapter puede ser utilizado bien en una actividad VentanaProfesional o una VentanaBusquedaObra
                            // así que hay discriminar desde qué tipo de ventana fue invocado

                            // Desde la lista de obras en seguimiento del profesional
                            if (contexto instanceof VentanaProfesional)
                            {
                                VentanaProfesional ctx = (VentanaProfesional) contexto;
                                ctx.modificarSeguimiento(id_profesional, seguirObra, pos);
                            }

                            // Desde la lista de resultados de búsqueda de obras
                            else if (contexto instanceof VentanaBusquedaObra)
                            {
                                VentanaBusquedaObra ctx = (VentanaBusquedaObra) contexto;
                                ctx.modificarSeguimiento(id_profesional, seguirObra, pos);
                            }
                        }
                    }
                );

            }

        }


        // Por último, añadir una referencia al id de la obra en el objeto view devuelto
        // (para poder identificar la obra cuando el usuario pulse en un elemento de la lista)
        contenedorFila.setTag( obra.getId() );

        // Devolver referencia al objeto View creado
        return contenedorFila;
    }


    // Método para ordenar los datos que contiene el adapter, de acuerdo a diferentes criterios
    public void ordenar(Info_CabeceraObra.Comparadores.Criterio criterio)
    {
        if ( criterio == Info_CabeceraObra.Comparadores.Criterio.TITULO_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorTituloAsc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.TIPO_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorTipoAsc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.ANTIGUEDAD_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorAntiguedadAsc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.ANTIGUEDAD_DESCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorAntiguedadDesc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.VISITAS_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorVisitasAsc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.VISITAS_DESCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorVisitasDesc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.SEGUIDORES_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorSeguidoresAsc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.SEGUIDORES_DESCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorSeguidoresDesc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.DISTANCIA_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorDistanciaAsc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.DISTANCIA_DESCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorDistanciaDesc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.REALIZACION_ASCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorRealizacionAsc);

        else if ( criterio == Info_CabeceraObra.Comparadores.Criterio.REALIZACION_DESCENDENTE )
            Collections.sort(datos, Info_CabeceraObra.Comparadores.ComparadorPorRealizacionDesc);

        else
            return;

        notifyDataSetChanged();
    }

}
