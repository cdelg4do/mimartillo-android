package com.cdelgado.mimartillocom;


import android.app.Activity;


public class ErrorServicioWeb
{
    private ServicioWeb.Tipo tipo;
    private String detalle;
    private boolean cerrarVentanaActual;
    private int idMensaje;      // Id del recurso String que contiene el mensaje
    private int idTitulo;       // Id del recurso String que contiene el título del diálogo de error
    private Utils.TipoMensaje tipoMensaje;


    // Constructor: muestra un mensaje de error en un cuadro de diálogo, y cierra la ventana actual segun corresponda
    public ErrorServicioWeb(ServicioWeb.Tipo tip, String det, boolean cerrar, int idMsg, int idTit)
    {
        tipo                = tip;
        detalle             = det;
        cerrarVentanaActual = cerrar;
        idMensaje           = idMsg;
        idTitulo            = idTit;
        tipoMensaje         = Utils.TipoMensaje.DIALOGO;
    }


    // Constructor: muestra un mensaje de error en un toast, y cierra la ventana actual segun corresponda
    public ErrorServicioWeb(ServicioWeb.Tipo tip, String det, boolean cerrar, int idMsg)
    {
        tipo                = tip;
        detalle             = det;
        cerrarVentanaActual = cerrar;
        idMensaje           = idMsg;
        idTitulo            = 0;
        tipoMensaje         = Utils.TipoMensaje.TOAST;
    }


    // Constructor: no muestra ningún mensaje, y cierra la ventana actual segun corresponda
    public ErrorServicioWeb(ServicioWeb.Tipo tip, String det, boolean cerrar)
    {
        tipo                = tip;
        detalle             = det;
        cerrarVentanaActual = cerrar;
        idMensaje           = 0;
        tipoMensaje         = Utils.TipoMensaje.NINGUNO;
    }


    // Indica el tipo de servicio a que pertenece el error (para comparar con el tipo de respuesta recibida del servidor)
    public ServicioWeb.Tipo getTipo()
    {
        return tipo;
    }

    // Indica el detalle del error (para comparar con la cadena recibida del servidor)
    public String getDetalle()
    {
        return detalle;
    }


    // Realiza el procesamiento del error,
    // mostrando el mensaje de error y/o cerrando la ventana actual según proceda
    public void procesar(Activity ventana)
    {
        if ( tipoMensaje == Utils.TipoMensaje.NINGUNO )
        {
            if ( cerrarVentanaActual )
                ventana.finish();
        }

        else if ( tipoMensaje == Utils.TipoMensaje.TOAST )
        {
            String msg = ventana.getResources().getString(idMensaje);

            Utils.mostrarMensaje(ventana, msg, tipoMensaje, null, null);

            if ( cerrarVentanaActual )
                ventana.finish();
        }

        else if ( tipoMensaje == Utils.TipoMensaje.DIALOGO )
        {
            String msg = ventana.getResources().getString(idMensaje);
            String tituloDialogo = ventana.getResources().getString(idTitulo);

            Utils.mostrarMensaje(ventana, msg, tipoMensaje, Utils.CategoriaDialogo.ERROR, tituloDialogo, cerrarVentanaActual);
        }
    }

}
