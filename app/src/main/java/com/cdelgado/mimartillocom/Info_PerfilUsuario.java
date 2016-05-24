package com.cdelgado.mimartillocom;


import android.graphics.Bitmap;

public class Info_PerfilUsuario extends ContenidoServicioWeb
{
    private String nombre;
    private String email;
    private String tel1;
    private String tel2;
    private String idProvincia;
    private String idPoblacion;
    private String nacimiento;
    private String sexo;
    private boolean publicidad;
    private String idioma;
    private Bitmap avatar;



    public Info_PerfilUsuario(String nom, String e, String t1, String t2, String idProv, String idPob, String nac, String s, boolean pub, String i, Bitmap a)
    {
        nombre      = nom;
        email       = e;
        tel1        = t1;
        tel2        = t2;
        idProvincia = idProv;
        idPoblacion = idPob;
        nacimiento  = nac;
        sexo        = s;
        publicidad  = pub;
        idioma      = i;
        avatar      = a;
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

    public String getNacimiento()
    {
        return nacimiento;
    }

    public String getSexo()
    {
        return sexo;
    }

    public boolean getPublicidad()
    {
        return publicidad;
    }

    public String getIdioma()
    {
        return idioma;
    }

    public Bitmap getAvatar()
    {
        return avatar;
    }

    @Override
    public ServicioWeb.Tipo getTipoServicio()
    {
        return ServicioWeb.Tipo.USER_PROFILE;
    }

    @Override
    public String toString()
    {
        // Por razones de eficiencia, no se incluye el Bitmap del avatar
        return "{\"nombre\":\""+nombre+"\",\"email\":\""+email+"\",\"tel1\":\""+tel1+"\",\"tel2\":\""+tel2+"\",\"provincia\":\""+idProvincia+"\",\"poblacion\":\""+idPoblacion+"\",\"nacimiento\":\""+nacimiento+"\",\"sexo\":\""+sexo+"\",\"publicidad\":\""+Boolean.toString(publicidad)+"\"}";
    }
}
