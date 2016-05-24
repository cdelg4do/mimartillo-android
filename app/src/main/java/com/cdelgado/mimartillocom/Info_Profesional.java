package com.cdelgado.mimartillocom;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;



public class Info_Profesional extends ContenidoServicioWeb
{
    private String id;
    private String nombre;
    private String descripcion;
    private String web;
    private String email_contacto;
    private String telefono1;
    private String telefono2;
    private String direccion;
    private String id_poblacion;
    private String poblacion;
    private String provincia;
    private String avatar;
    private boolean favorito;

    private ArrayList <ActividadObra> actividades;

    private ArrayList <Info_Valoracion> valoraciones;

    private double media_calidad;

    private double media_precio;



    public Info_Profesional(String i, String n, String desc, String w, String e, String t1, String t2, String dir, String id_p,
                            String pob, String prov, String imagen_base64, boolean fav, ArrayList<ActividadObra> acts, ArrayList<Info_Valoracion> vals)
    {
        id             = i;
        nombre         = n;
        descripcion    = desc;
        web            = w;
        email_contacto = e;
        telefono1      = t1;
        telefono2      = t2;
        direccion      = dir;
        id_poblacion   = id_p;
        poblacion      = pob;
        provincia      = prov;
        avatar         = imagen_base64;
        favorito       = fav;

        actividades = acts;
        valoraciones = vals;

        calcularMediasCalidadPrecio();
    }

    public String getId()
    {
        return id;
    }

    public String getNombre()
    {
        return nombre;
    }

    public String getDescripcion()
    {
        return descripcion;
    }

    public String getWeb()
    {
        return web;
    }

    public String getEmail_contacto()
    {
        return email_contacto;
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

    public String getId_poblacion()
    {
        return id_poblacion;
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

    public boolean esFavorito()
    {
        return favorito;
    }

    public ArrayList<ActividadObra> getActividades()
    {
        return actividades;
    }

    public String getEspecialidades()
    {
        String e = "";

        for (int i=0; i<actividades.size(); i++)
        {
            ActividadObra act = actividades.get(i);
            e += "\r\n"+ act.getNombre() + "\r\n";

            ArrayList<CategoriaObra> categorias = act.getCategorias();
            for (int j=0; j<categorias.size(); j++)
            {
                CategoriaObra cat = categorias.get(j);
                e += "\t\t"+ cat.getNombre() +"\r\n";

                ArrayList<TipoObra> tipos = cat.getTipos();
                for (int k=0; k<tipos.size(); k++)
                {
                    TipoObra tip = tipos.get(k);
                    e += "\t\t\t\t"+ tip.getNombre() +"\r\n";
                }

            }

        }

        return e;
    }

    public ArrayList<Info_Valoracion> getValoraciones()
    {
        return valoraciones;
    }

    public double getMedia_calidad()
    {
        return media_calidad;
    }

    public double getMedia_precio()
    {
        return media_precio;
    }



    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.PROFESSIONAL_INFO;
    }

    @Override
    public String toString()
    {
/*        String s = "{\"solicitante\":\"" + solicitante + "\",\"titulo\":\"" + titulo + "\",\"detalle\":\"" + detalle + "\",\"tipo\":\"" + tipo + "\"," +
                "\"fecha_solicitud\":\"" + fecha_solicitud + "\",\"fecha_realizacion\":\"" + fecha_realizacion + "\",\"poblacion\":\"" + poblacion + "\"," +
                "\"latitud\":\"" + Double.toString(latitud) + "\",\"longitud\":\"" + Double.toString(longitud) + "\",\"nombre_contacto\":\"" + nombre_contacto + "\"," +
                "\"email_contacto\":\"" + email_contacto + "\",\"telef_contacto\":\"" + telef_contacto + "\",\"visitas\":\"" + Integer.toString(visitas) + "\"," +
                "\"cerrada\":\"" + (cerrada ? "1" : "0") +
                "\",\"adjudicatario\":\"" + adjudicatario +
                "\",\"presupuesto\":\"" + presupuesto.toString() + "\"," +
                "\"categoria\":\"" + categoria + "\",\"actividad\":\"" + actividad + "\",\"seguidores\":[";

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
*/

        return "{\"id\":\"" + id + "\"}";
    }


    // Método privado, se invoca desde el constructor
    private void calcularMediasCalidadPrecio()
    {
        media_calidad = 0;
        media_precio  = 0;

        int totalVotos = valoraciones.size();

        if ( totalVotos>0 )
        {
            int totalCalidad = 0;
            int totalPrecio = 0;

            for (int i = 0; i < totalVotos; i++) {
                Info_Valoracion v = valoraciones.get(i);

                totalCalidad += v.getNota_calidad();
                totalPrecio += v.getNota_precio();
            }

            media_calidad = ((double) totalCalidad) / totalVotos;
            media_precio  = ((double) totalPrecio) / totalVotos;
        }
    }







}

