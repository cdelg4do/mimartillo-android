package com.cdelgado.mimartillocom;


import java.util.ArrayList;

public class Info_ParametrosNuevaObra extends ContenidoServicioWeb
{
    private String nombre;
    private String email;
    private String tel1;
    private String tel2;
    private String idProvincia;
    private String idPoblacion;

    private ArrayList<Info_ElementoSpinner> actividades;
    private ArrayList<Info_ElementoSpinner> provincias;
    private ArrayList<Info_ElementoSpinner> poblaciones;



    public Info_ParametrosNuevaObra(String nom, String e, String t1, String t2, String idProv, String idPob, ArrayList<Info_ElementoSpinner> act, ArrayList<Info_ElementoSpinner> prov, ArrayList<Info_ElementoSpinner> pob)
    {
        nombre      = nom;
        email       = e;
        tel1        = t1;
        tel2        = t2;
        idProvincia = idProv;
        idPoblacion = idPob;

        actividades = act;
        provincias  = prov;
        poblaciones = pob;
    }

    public String getNombre()
    {
        return nombre;
    }

    public String getEmail()
    {
        return email;
    }

    public String getTel1()
    {
        return tel1;
    }

    public String getTel2()
    {
        return tel2;
    }

    public String getProvincia()
    {
        return idProvincia;
    }

    public String getPoblacion()
    {
        return idPoblacion;
    }

    public ArrayList<Info_ElementoSpinner> getActividades()
    {
        return actividades;
    }

    public ArrayList<Info_ElementoSpinner> getProvincias()
    {
        return provincias;
    }

    public ArrayList<Info_ElementoSpinner> getPoblaciones()
    {
        return poblaciones;
    }

    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.INITIAL_PARAMETERS_NEW_WORK;
    }

    @Override
    public String toString()
    {
        int i;

        String s = "{\"nombre\":\""+nombre+"\",\"email\":\""+email+"\",\"tel1\":\""+tel1+"\",\"tel2\":\""+tel2+
                "\",\"provincia\":\""+idProvincia+"\",\"poblacion\":\""+idPoblacion+"\",\"actividades\":[";

        // Objetos JSON del array de actividades
        for (i = 0; i < actividades.size(); i++)
        {
            Info_ElementoSpinner e = actividades.get(i);
            s += e.toString();

            // Si no es el último elemento del array, añadir una coma para separarlo del siguiente
            if ((i + 1) < actividades.size()) s += ",";
        }

        s += "],\"provincias\":[";

        // Objetos JSON del array de provincias
        for (i = 0; i < provincias.size(); i++)
        {
            Info_ElementoSpinner pr = provincias.get(i);
            s += pr.toString();

            if ((i + 1) < provincias.size()) s += ",";
        }

        s += "],\"poblaciones\":[";

        // Objetos JSON del array de poblaciones
        for (i = 0; i < poblaciones.size(); i++)
        {
            Info_ElementoSpinner po = poblaciones.get(i);
            s += po.toString();

            if ((i + 1) < poblaciones.size()) s += ",";
        }

        // Cierre del array y del objeto JSON global
        s += "]}";


        return s;
    }

}
