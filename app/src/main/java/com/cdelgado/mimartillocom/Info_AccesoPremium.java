package com.cdelgado.mimartillocom;


import android.graphics.Bitmap;

public class Info_AccesoPremium extends ContenidoServicioWeb
{
    private boolean premium;

    public Info_AccesoPremium(boolean p)
    {
        premium = p;
    }

    public boolean esPremium()
    {
        return premium;
    }

    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.PREMIUM_ACCESS;
    }

    @Override
    public String toString()
    {
        return "{\"premium\":\""+Boolean.toString(premium)+"\"}";
    }
}
