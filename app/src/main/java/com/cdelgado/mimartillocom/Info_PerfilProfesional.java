package com.cdelgado.mimartillocom;


import android.graphics.Bitmap;

import java.util.ArrayList;

public class Info_PerfilProfesional extends ContenidoServicioWeb
{
    private boolean premium;
    private String email_usuario;
    private String nombre;
    private String descripcion;
    private String direccion;
    private String idPoblacion;
    private String idProvincia;
    private String tel1;
    private String tel2;
    private String web;
    private String email_contacto;
    private String idioma;
    private boolean publicidad;
    private int votos;
    private double media_calidad;
    private double media_precio;
    private ArrayList<ActividadObra> especialidades;
    private Bitmap avatar;



    public Info_PerfilProfesional(boolean prem, String usuario, String nom, String desc, String dir, String idPob, String idProv,
                                  String t1, String t2, String www, String email, String lang, boolean pub, int vot, double cal,
                                  double pre, ArrayList<ActividadObra> esp, Bitmap img)
    {
        premium	        = prem;
        email_usuario   = usuario;
        nombre	        = nom;
        descripcion	    = desc;
        direccion	    = dir;
        idPoblacion	    = idPob;
        idProvincia	    = idProv;
        tel1	        = t1;
        tel2	        = t2;
        web	            = www;
        email_contacto	= email;
        idioma	        = lang;
        publicidad	    = pub;
        votos	        = vot;
        media_calidad	= cal;
        media_precio	= pre;
        especialidades	= esp;
        avatar	        = img;
    }

    public boolean getPremium()
    {
        return premium;
    }

    public String getUsuario()
    {
        return email_usuario;
    }

    public String getNombre()
    {
        return nombre;
    }

    public String getDescripcion()
    {
        return descripcion;
    }

    public String getDireccion()
    {
        return direccion;
    }

    public String getPoblacion()
    {
        return idPoblacion;
    }

    public String getProvincia()
    {
        return idProvincia;
    }

    public String getTel1()
    {
        return tel1;
    }

    public String getTel2()
    {
        return tel2;
    }

    public String getWeb()
    {
        return web;
    }

    public String getEmailContacto()
    {
        return email_contacto;
    }

    public String getIdioma()
    {
        return idioma;
    }

    public boolean getPublicidad()
    {
        return publicidad;
    }

    public int getVotos()
    {
        return votos;
    }

    public double getCalidad()
    {
        return media_calidad;
    }

    public double getPrecio()
    {
        return media_precio;
    }

    public ArrayList<ActividadObra> getEspecialidades()
    {
        return especialidades;
    }

    public Bitmap getAvatar()
    {
        return avatar;
    }

    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.PROFESSIONAL_PROFILE;
    }

    @Override
    public String toString()
    {
        // Por razones de eficiencia, no se incluye el Bitmap del avatar ni la lista de especialidades
        return "{\"premium\":\""+Boolean.toString(premium)+"\",\"email_usuario\":\""+email_usuario+"\",\"nombre\":\""+nombre+"\",\"descripcion\":\""+descripcion+"\",\"direccion\":\""+direccion+"\",\"provincia\":\""+idProvincia+"\",\"poblacion\":\""+idPoblacion+"\",\"tel1\":\""+tel1+"\",\"tel2\":\""+tel2+"\",\"web\":\""+web+"\",\"email_contacto\":\""+email_contacto+"\",\"idioma\":\""+idioma+"\",\"publicidad\":\""+Boolean.toString(publicidad)+"\",\"votos\":\""+Integer.toString(votos)+"\",\"calidad\":\""+Double.toString(media_calidad)+"\",\"precio\":\""+Double.toString(media_precio)+"\"}";
    }
}
