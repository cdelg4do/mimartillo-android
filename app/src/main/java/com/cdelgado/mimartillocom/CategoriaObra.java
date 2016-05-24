package com.cdelgado.mimartillocom;

import java.util.ArrayList;

public class CategoriaObra
{
    private String id, nombre;
    private ArrayList<TipoObra> tipos;

    public CategoriaObra(String i, String n, ArrayList<TipoObra> t)
    {
        id = i;
        nombre = n;
        tipos = t;
    }

    public String getId()
    {
        return id;
    }

    public String getNombre()
    {
        return nombre;
    }

    public ArrayList<TipoObra> getTipos()
    {
        return tipos;
    }
}
