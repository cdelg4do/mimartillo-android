package com.cdelgado.mimartillocom;


import android.graphics.Bitmap;

public class Info_Login extends ContenidoServicioWeb
{
    private String nombre;
    private String id;
    private String sesion;
    private String avatar;

    public Info_Login(String n, String i, String s, String imagen_base64)
    {
        nombre          = n;
        id              = i;
        sesion          = s;
        avatar          = imagen_base64;    // si es "default_profile_pic", significa que usa la imagen de perfil por defecto
    }

    public String getNombre()
    {
        return nombre;
    }

    public String getId()
    {
        return id;
    }

    public String getSesion()
    {
        return sesion;
    }

    public String getAvatar()
    {
        return avatar;
    }

    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.LOGIN;
    }

    @Override
    public String toString()
    {
        return "{\"nombre\":\""+nombre+"\",\"id\":\""+id+"\",\"sesion\":\""+sesion+"\",\"avatar\":\""+avatar+"\"}";
    }
}
