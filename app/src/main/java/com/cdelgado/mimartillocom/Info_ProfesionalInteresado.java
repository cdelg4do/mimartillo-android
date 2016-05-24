package com.cdelgado.mimartillocom;


import java.util.ArrayList;



public class Info_ProfesionalInteresado extends ContenidoServicioWeb
{
    private String id;
    private String nombre;



    public Info_ProfesionalInteresado(String i, String n)
    {
        id = i;
        nombre = n;
    }

    public String getId()
    {
        return id;
    }

    public String getNombre()
    {
        return nombre;
    }



    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.WORK_FOLLOWERS;
    }

    @Override
    public String toString()
    {
        return "{\"i\":\"" + id + "\",\"n\":\"" + nombre + "\"}";
    }


}

