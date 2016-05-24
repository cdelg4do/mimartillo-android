package com.cdelgado.mimartillocom;


import java.util.ArrayList;


public class RespuestaServicioWeb
{
    private String resultado;
    private String detalle;
    private ArrayList<ContenidoServicioWeb> contenido;

    public RespuestaServicioWeb(String r, String d, ArrayList<ContenidoServicioWeb> c)
    {
        resultado   = r;
        detalle     = d;
        contenido   = c;
    }

    public RespuestaServicioWeb(String r, String d)
    {
        resultado   = r;
        detalle     = d;
        contenido   = null;
    }

    public String getResultado()
    {
        return resultado;
    }

    public String getDetalle()
    {
        return detalle;
    }

    public ArrayList<ContenidoServicioWeb> getContenido()
    {
        return contenido;
    }

    @Override
    public String toString()
    {
        // Primeros dos elementos del objeto JSON (Resultado y Detalle)
        // y comienzo del tercer elemento Contenido (array de objetos JSON)
        String s = "{\"resultado\":\"" + resultado + "\",\"detalle\":\"" + detalle + "\",\"contenido\":[";

        // Objetos JSON del array Contenido
        for (int i = 0; i < contenido.size(); i++)
        {
            ContenidoServicioWeb c = contenido.get(i);
            s += c.toString();

            // Si no es el último elemento, añadir una coma para separarlo del siguiente
            if ( (i+1) < contenido.size() )     s += ",";
        }

        // Cierre del array y del objeto JSON global
        s += "]}";

        return s;
    }
}
