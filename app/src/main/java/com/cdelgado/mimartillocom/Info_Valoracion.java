package com.cdelgado.mimartillocom;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class Info_Valoracion extends ContenidoServicioWeb
{
    private String id_usuario;
    private String nombre_usuario;
    private String email_usuario;
    private String comentario;
    private int nota_calidad;
    private int nota_precio;
    private String fecha_valoracion;
    private String id_obra;
    private String titulo_obra;
    private String id_tipo;
    private String tipo_obra;
    private BigDecimal presupuesto_obra;
    private String avatar;

    public Info_Valoracion(String id_u, String nombre_u, String email_u, String com, int cal, int prec, String fecha,
                           String id_o, String titulo_o, String id_tipo_o, String tipo_o, BigDecimal pres, String imagen_base64)
    {
        id_usuario        = id_u;
        nombre_usuario    = nombre_u;
        email_usuario     = email_u;
        comentario        = com;
        nota_calidad      = cal;
        nota_precio       = prec;
        fecha_valoracion  = fecha;
        id_obra           = id_o;
        titulo_obra       = titulo_o;
        id_tipo           = id_tipo_o;
        tipo_obra         = tipo_o;
        presupuesto_obra  = pres;
        avatar            = imagen_base64;
    }

    public String getId_usuario()
    {
        return id_usuario;
    }

    public String getNombre_usuario()
    {
        return nombre_usuario;
    }

    public String getEmail_usuario()
    {
        return email_usuario;
    }

    public String getComentario()
    {
        return comentario;
    }

    public int getNota_calidad()
    {
        return nota_calidad;
    }

    public int getNota_precio()
    {
        return nota_precio;
    }

    public String getFecha_valoracion()
    {
        return fecha_valoracion;
    }

    public String getId_obra()
    {
        return id_obra;
    }

    public String getTitulo_obra()
    {
        return titulo_obra;
    }

    public String getId_tipo()
    {
        return id_tipo;
    }

    public String getTipo_obra()
    {
        return tipo_obra;
    }

    public BigDecimal getPresupuesto_obra()
    {
        return presupuesto_obra;
    }

    public String getAvatar()
    {
        return avatar;
    }

    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.MY_VOTES;
    }

    @Override
    public String toString()
    {
        return "{\"id_obra\":\"" + id_obra + "\"}";
    }


    // Clase estática que define los comparadores que permitan ordenar listas de Info_Valoracion según diferentes criterios de ordenación
    public static class Comparadores
    {
        public static enum Criterio
        {
            SOLICITANTE_ASCENDENTE,
            CALIDAD_DESCENDENTE,
            PRECIO_DESCENDENTE,
            FECHA_DESCENDENTE
        }


        public static Comparator<Info_Valoracion> ComparadorPorSolicitanteAsc = new Comparator<Info_Valoracion>()
        {
            @Override public int compare(Info_Valoracion v1, Info_Valoracion v2)    {   return v1.getId_usuario().compareTo( v2.getId_usuario() );  }
        };

        public static Comparator<Info_Valoracion> ComparadorPorCalidadDesc = new Comparator<Info_Valoracion>()
        {
            @Override public int compare(Info_Valoracion v1, Info_Valoracion v2)    {   return v2.getNota_calidad() - v1.getNota_calidad(); }
        };

        public static Comparator<Info_Valoracion> ComparadorPorPrecioDesc = new Comparator<Info_Valoracion>()
        {
            @Override public int compare(Info_Valoracion v1, Info_Valoracion v2)    {   return v2.getNota_precio() - v1.getNota_precio(); }
        };

        public static Comparator<Info_Valoracion> ComparadorPorFechaDesc = new Comparator<Info_Valoracion>()
        {
            @Override public int compare(Info_Valoracion v1, Info_Valoracion v2)
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                Date fecha1, fecha2;
                try
                {
                    fecha1 = sdf.parse( v1.getFecha_valoracion() );
                    fecha2 = sdf.parse( v2.getFecha_valoracion() );
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
