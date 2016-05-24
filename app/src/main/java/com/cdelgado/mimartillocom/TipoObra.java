package com.cdelgado.mimartillocom;

public class TipoObra
{
    private String id, nombre;

    public TipoObra(String i, String n)
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
}
