package com.cdelgado.mimartillocom;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class Info_CabeceraObra extends ContenidoServicioWeb
{
    private String id;
    private String titulo;
    private int id_tipo;
    private String tipo_obra;
    private String fecha_realizacion;   // En formato AAAA-MM-DD
    private int visitas;
    private String adjudicatario;
    private int interesados;
    private boolean seguidor;
    private double distancia;  // siempre en millas
    private boolean falta_valoracion;

    public Info_CabeceraObra(String i, String tit, int id_tip, String tip, String fecha, int v, String a, int inter, boolean s, double dist, boolean falta)
    {
        id                  = i;
        titulo              = tit;
        id_tipo             = id_tip;
        tipo_obra           = tip;
        fecha_realizacion   = fecha;
        visitas             = v;
        adjudicatario       = a;
        interesados         = inter;
        seguidor            = s;
        distancia           = dist;
        falta_valoracion    = falta;
    }

    public String getId()
    {
        return id;
    }

    public String getTitulo()
    {
        return titulo;
    }

    public int getIdTipo()
    {
        return id_tipo;
    }

    public String getTipo()
    {
        return tipo_obra;
    }

    public String getFechaRealizacion()
    {
        return fecha_realizacion;
    }

    public int getVisitas()
    {
        return visitas;
    }

    public String getAdjudicatario()
    {
        return adjudicatario;
    }

    public int getInteresados()
    {
        return interesados;
    }

    public boolean esSeguidor()
    {
        return seguidor;
    }

    public void setSeguidor(boolean s)
    {
        seguidor = s;
    }

    public double getDistancia()
    {
        return distancia;
    }

    public boolean sinValoracion()
    {
        return falta_valoracion;
    }

    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.MY_WORKS;
    }

    @Override
    public String toString()
    {
        return "{\"id\":\""+id+"\",\"titulo\":\""+titulo+"\",\"tipo_obra\":\""+tipo_obra+"\",\"fecha_realizacion\":\""+fecha_realizacion+"\",\"visitas\":\""+Integer.toString(visitas)+"\",\"adjudicatario\":\""+adjudicatario+"\",\"interesados\":\""+Integer.toString(interesados)+"\",\"falta_valoracion\":\""+Boolean.toString(falta_valoracion)+"\"}";
    }


    // Clase estática que define los comparadores que permitan ordenar listas de Info_CabeceraObra según diferentes criterios de ordenación
    public static class Comparadores
    {
        public static enum Criterio
        {
            TITULO_ASCENDENTE,
            TIPO_ASCENDENTE,
            ANTIGUEDAD_ASCENDENTE,
            ANTIGUEDAD_DESCENDENTE,
            VISITAS_ASCENDENTE,
            VISITAS_DESCENDENTE,
            SEGUIDORES_ASCENDENTE,
            SEGUIDORES_DESCENDENTE,
            DISTANCIA_ASCENDENTE,
            DISTANCIA_DESCENDENTE,
            REALIZACION_ASCENDENTE,
            REALIZACION_DESCENDENTE
        }


        public static Comparator<Info_CabeceraObra> ComparadorPorTituloAsc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)    {   return o1.getTitulo().toLowerCase().compareTo( o2.getTitulo().toLowerCase() );  }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorTipoAsc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)    {   return o1.getIdTipo() - o2.getIdTipo(); }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorAntiguedadAsc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)    {   return o1.getId().compareTo( o2.getId() );  }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorAntiguedadDesc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)    {   return o2.getId().compareTo( o1.getId() );  }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorVisitasAsc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)    {   return o1.getVisitas() - o2.getVisitas();   }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorVisitasDesc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)    {   return o2.getVisitas() - o1.getVisitas();   }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorSeguidoresAsc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)    {   return o1.getInteresados() - o2.getInteresados();   }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorSeguidoresDesc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)    {   return o2.getInteresados() - o1.getInteresados();   }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorDistanciaAsc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)
            {
                Double d1 = new Double( o1.getDistancia() );
                Double d2 = new Double( o2.getDistancia() );

                return d1.compareTo( d2 );
            }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorDistanciaDesc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)
            {
                Double d1 = new Double( o1.getDistancia() );
                Double d2 = new Double( o2.getDistancia() );

                return d2.compareTo( d1 );
            }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorRealizacionAsc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                Date fecha1, fecha2;
                try
                {
                    fecha1 = sdf.parse(o1.getFechaRealizacion());
                    fecha2 = sdf.parse(o2.getFechaRealizacion());
                }
                catch (ParseException e)
                {
                    return 0;
                }

                return fecha1.compareTo( fecha2 );
            }
        };

        public static Comparator<Info_CabeceraObra> ComparadorPorRealizacionDesc = new Comparator<Info_CabeceraObra>()
        {
            @Override public int compare(Info_CabeceraObra o1, Info_CabeceraObra o2)
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                Date fecha1, fecha2;
                try
                {
                    fecha1 = sdf.parse(o1.getFechaRealizacion());
                    fecha2 = sdf.parse(o2.getFechaRealizacion());
                }
                catch (ParseException e)
                {
                    return 0;
                }

                return fecha2.compareTo( fecha1 );
            }
        };

    }

}

