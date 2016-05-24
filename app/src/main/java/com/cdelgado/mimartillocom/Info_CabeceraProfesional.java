package com.cdelgado.mimartillocom;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class Info_CabeceraProfesional extends ContenidoServicioWeb
{
    private String id;
    private String nombre;
    private String email;
    private String telefono1;
    private String telefono2;
    private String direccion;
    private String poblacion;
    private String provincia;
    private String avatar;
    private int votos;
    private double media_calidad;
    private double media_precio;
    private boolean favorito;
    private double distancia;  // siempre en millas

    private boolean adjudicatario;  // Usado solo en los casos en que se lista el profesional como seguidor de una obra
                                    // Este valor no se incluye en la respuesta JSON recibida del servidor, por defecto es false


    public Info_CabeceraProfesional(String i, String n, String e, String t1, String t2, String d, String pob, String pro, String imagen_base64, int v, double cal, double prec, double dist, boolean f)
    {
        id              = i;
        nombre          = n;
        email           = e;
        telefono1       = t1;
        telefono2       = t2;
        direccion       = d;
        poblacion       = pob;
        provincia       = pro;
        avatar          = imagen_base64;
        votos           = v;
        media_calidad   = cal;
        media_precio    = prec;
        favorito        = f;
        distancia       = dist;

        adjudicatario   = false;
    }

    public String getId()
    {
        return id;
    }

    public String getNombre()
    {
        return nombre;
    }

    public String getEmail()
    {
        return email;
    }

    public String getTelefono1()
    {
        return telefono1;
    }

    public String getTelefono2()
    {
        return telefono2;
    }

    public String getDireccion()
    {
        return direccion;
    }

    public String getPoblacion()
    {
        return poblacion;
    }

    public String getProvincia()
    {
        return provincia;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public int getVotos()
    {
        return votos;
    }

    public double getMediaCalidad()
    {
        return media_calidad;
    }

    public boolean esFavorito()
    {
        return favorito;
    }

    public void setFavorito(boolean b)
    {
        favorito = b;
    }

    public double getMediaPrecio()
    {
        return media_precio;
    }

    public void setAdjudicatario(boolean esAdjudicatario)
    {
        adjudicatario = esAdjudicatario;
    }

    public double getDistancia()
    {
        return distancia;
    }

    public boolean esAdjudicatario()
    {
        return adjudicatario;
    }




    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.PROFESSIONAL_SEARCH;
    }

    @Override
    public String toString()
    {
        // No se incluye el campo adjudicatario, ya que no forma parte de la respuesta JSON original
        // Tampoco se incluye el campo avatar, por motivos de eficiencia
        return "{\"id\":\""+id+"\",\"nombre\":\""+nombre+"\",\"email\":\""+email+"\",\"telefono1\":\""+telefono1+"\",\"telefono2\":\""+telefono2+"\",\"direccion\":\""+direccion+"\",\"poblacion\":\""+poblacion+"\",\"provincia\":\""+provincia+"\",\"votos\":\""+votos+"\",\"calidad\":\""+media_calidad+"\",\"precio\":\""+media_precio+"\",\"esFavorito\":\""+favorito+"\"}";
    }


    // Clase estática que define los comparadores que permitan ordenar listas de Info_CabeceraProfesional según diferentes criterios de ordenación
    public static class Comparadores
    {
        public static enum Criterio
        {
            NOMBRE_ASCENDENTE,
            POBLACION_ASCENDENTE,
            PROVINCIA_ASCENDENTE,
            VOTOS_ASCENDENTE,
            VOTOS_DESCENDENTE,
            CALIDAD_ASCENDENTE,
            CALIDAD_DESCENDENTE,
            PRECIO_ASCENDENTE,
            PRECIO_DESCENDENTE,
            FAVORITOS_PRIMERO,
            FAVORITOS_DESPUES,
            DISTANCIA_ASCENDENTE,
            DISTANCIA_DESCENDENTE
        }


        public static Comparator<Info_CabeceraProfesional> ComparadorPorNombreAsc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)    {   return p1.getNombre().toLowerCase().compareTo(p2.getNombre().toLowerCase() );  }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorPoblacionAsc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)    {   return p1.getPoblacion().compareTo( p2.getPoblacion() );  }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorProvinciaAsc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)    {   return p1.getProvincia().compareTo( p2.getProvincia() );  }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorVotosAsc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)    {   return p1.getVotos() - p2.getVotos();  }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorVotosDesc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)    {   return p2.getVotos() - p1.getVotos();  }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorCalidadAsc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)
            {
                Double c1 = new Double( p1.getMediaCalidad() );
                Double c2 = new Double( p2.getMediaCalidad() );

                return c1.compareTo( c2 );
            }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorCalidadDesc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)
            {
                Double c1 = new Double( p1.getMediaCalidad() );
                Double c2 = new Double( p2.getMediaCalidad() );

                return c2.compareTo( c1 );
            }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorPrecioAsc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)
            {
                Double pr1 = new Double( p1.getMediaPrecio() );
                Double pr2 = new Double( p2.getMediaPrecio() );

                return pr1.compareTo( pr2 );
            }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorPrecioDesc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)
            {
                Double pr1 = new Double( p1.getMediaPrecio() );
                Double pr2 = new Double( p2.getMediaPrecio() );

                return pr2.compareTo( pr1 );
            }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorFavoritosPrimero = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)
            {
                boolean f1 = p1.esFavorito();
                boolean f2 = p2.esFavorito();

                if ( f1 == f2 ) return 0;
                else if ( f2 )  return 1;
                else            return -1;
            }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorFavoritosDespues = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)
            {
                boolean f1 = p1.esFavorito();
                boolean f2 = p2.esFavorito();

                if ( f1 == f2 ) return 0;
                else if ( f1 )  return 1;
                else            return -1;
            }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorDistanciaAsc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)
            {
                Double d1 = new Double( p1.getDistancia() );
                Double d2 = new Double( p2.getDistancia() );

                return d1.compareTo( d2 );
            }
        };

        public static Comparator<Info_CabeceraProfesional> ComparadorPorDistanciaDesc = new Comparator<Info_CabeceraProfesional>()
        {
            @Override public int compare(Info_CabeceraProfesional p1, Info_CabeceraProfesional p2)
            {
                Double d1 = new Double( p1.getDistancia() );
                Double d2 = new Double( p2.getDistancia() );

                return d2.compareTo( d1 );
            }
        };

    }

}
