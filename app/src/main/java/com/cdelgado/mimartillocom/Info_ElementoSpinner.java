package com.cdelgado.mimartillocom;



public class Info_ElementoSpinner extends ContenidoServicioWeb
{
    private String clave;
    private String valor;

    public Info_ElementoSpinner(String i, String n)
    {
        clave   = i;
        valor   = n;
    }

    public String getClave()
    {
        return clave;
    }

    public String getValor()
    {
        return valor;
    }

    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.SPINNER_VALUES;
    }

    @Override
    public String toString()
    {
        return "{\"i\":\""+clave+"\",\"n\":\""+valor+"\"}";
    }
}
