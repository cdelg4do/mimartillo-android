package com.cdelgado.mimartillocom;


import java.math.BigDecimal;
import java.util.ArrayList;



public class Info_Obra extends ContenidoServicioWeb
{
    private String solicitante;
    private String titulo;
    private String detalle;
    private String actividad;
    private String categoria;
    private String tipo;
    private String fecha_solicitud;
    private String fecha_realizacion;
    private String poblacion;
    private double latitud;
    private double longitud;
    private String nombre_contacto;
    private String email_contacto;
    private String telef_contacto;
    private int visitas;
    private boolean cerrada;
    private String adjudicatario;
    private BigDecimal presupuesto;
    private String avatar;
    private ArrayList<Info_CabeceraProfesional> seguidores;
    private boolean falta_valoracion;
    private ArrayList<String> imagenes;


    public Info_Obra(String s, String tit, String d, String act, String cat, String tip, String fSol, String fRea, String pob, double lat, double lon, String nom, String email, String tel, int v, boolean c, String a, BigDecimal p, String imagen_base64, ArrayList<Info_CabeceraProfesional> seg, boolean sinValoracion, ArrayList<String> imgs)
    {
        solicitante = s;
        titulo = tit;
        detalle = d;
        actividad = act;
        categoria = cat;
        tipo = tip;
        fecha_solicitud = fSol;
        fecha_realizacion = fRea;
        poblacion = pob;
        latitud = lat;
        longitud = lon;
        nombre_contacto = nom;
        email_contacto = email;
        telef_contacto = tel;
        visitas = v;
        cerrada = c;
        adjudicatario = a;
        presupuesto = p;
        avatar = imagen_base64;
        seguidores = seg;
        falta_valoracion = sinValoracion;
        imagenes = imgs;
    }

    public String getSolicitante()
    {
        return solicitante;
    }

    public String getTitulo()
    {
        return titulo;
    }

    public String getDetalle()
    {
        return detalle;
    }

    public String getActividad()
    {
        return actividad;
    }

    public String getCategoria()
    {
        return categoria;
    }

    public String getTipo()
    {
        return tipo;
    }

    public String getFechaSolicitud()
    {
        return fecha_solicitud;
    }

    public String getFechaRealizacion()
    {
        return fecha_realizacion;
    }

    public String getPoblacion()
    {
        return poblacion;
    }

    public double getLatitud()
    {
        return latitud;
    }

    public double getLongitud()
    {
        return longitud;
    }

    public String getNombreContacto()
    {
        return nombre_contacto;
    }

    public String getEmailContacto()
    {
        return email_contacto;
    }

    public String getTelefContacto()
    {
        return telef_contacto;
    }

    public int getVisitas()
    {
        return visitas;
    }

    public boolean estaCerrada()
    {
        return cerrada;
    }

    public String getAdjudicatario()
    {
        return adjudicatario;
    }

    public BigDecimal getPresupuesto()
    {
        return presupuesto;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public ArrayList<Info_CabeceraProfesional> getSeguidores()
    {
        return seguidores;
    }

    public boolean faltaValoracion()
    {
        return falta_valoracion;
    }

    public ArrayList<String> getImagenes()
    {
        return imagenes;
    }


    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.WORK_INFO;
    }

    @Override
    public String toString()
    {
        // Por razones de eficiencia, no se incluye el campo "avatar" ni la lista de imágenes

        String s = "{\"solicitante\":\"" + solicitante + "\",\"titulo\":\"" + titulo + "\",\"detalle\":\"" + detalle + "\",\"tipo\":\"" + tipo + "\"," +
                    "\"fecha_solicitud\":\"" + fecha_solicitud + "\",\"fecha_realizacion\":\"" + fecha_realizacion + "\",\"poblacion\":\"" + poblacion + "\"," +
                    "\"latitud\":\"" + Double.toString(latitud) + "\",\"longitud\":\"" + Double.toString(longitud) + "\",\"nombre_contacto\":\"" + nombre_contacto + "\"," +
                    "\"email_contacto\":\"" + email_contacto + "\",\"telef_contacto\":\"" + telef_contacto + "\",\"visitas\":\"" + Integer.toString(visitas) + "\"," +
                    "\"cerrada\":\"" + (cerrada ? "1" : "0") +
                    "\",\"adjudicatario\":\"" + adjudicatario +
                    "\",\"presupuesto\":\"" + presupuesto.toString() + "\"," +
                    "\"falta_valoracion\":\"" + (falta_valoracion ? "true" : "false") + "\",\"categoria\":\"" + categoria + "\",\"actividad\":\"" + actividad + "\",\"seguidores\":[";

        // Objetos JSON del array seguidores
        for (int i = 0; i < seguidores.size(); i++)
        {
            Info_CabeceraProfesional c = seguidores.get(i);
            s += c.toString();

            // Si no es el último elemento, añadir una coma para separarlo del siguiente
            if ((i + 1) < seguidores.size()) s += ",";
        }

        // Cierre del array y del objeto JSON global
        s += "]}";

        return s;
    }
}
