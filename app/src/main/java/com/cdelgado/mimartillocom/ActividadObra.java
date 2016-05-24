package com.cdelgado.mimartillocom;

import java.util.ArrayList;

public class ActividadObra
{
    private String id, nombre;
    private ArrayList<CategoriaObra> categorias;

    public ActividadObra(String i, String n, ArrayList<CategoriaObra> c)
    {
        id = i;
        nombre = n;
        categorias = c;
    }

    public String getId()
    {
        return id;
    }

    public String getNombre()
    {
        return nombre;
    }

    public ArrayList<CategoriaObra> getCategorias()
    {
        return categorias;
    }
}
