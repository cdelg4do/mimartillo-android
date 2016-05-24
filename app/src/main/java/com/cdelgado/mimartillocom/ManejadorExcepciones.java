package com.cdelgado.mimartillocom;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

public class ManejadorExcepciones implements java.lang.Thread.UncaughtExceptionHandler
{
    private final Activity ventanaOrigen;

    public ManejadorExcepciones(Activity origen)
    {
        ventanaOrigen = origen;
    }

    public void uncaughtException(Thread thread, Throwable ex)
    {
        final String FIN_LINEA = "\n";

        // Volcar la traza de error en un objeto StringWriter
        StringWriter stackTrace = new StringWriter();
        ex.printStackTrace( new PrintWriter(stackTrace) );

        // Construir una secuencia de caracteres con los detalles a mostrar
        // (traza del error, información del dispositivo y de firmware)
        StringBuilder errorReport = new StringBuilder();

        errorReport.append( ventanaOrigen.getResources().getString( R.string.ManejadorExcepciones_txt_trazaError ) );
        errorReport.append(stackTrace.toString());
        errorReport.append( ventanaOrigen.getResources().getString( R.string.ManejadorExcepciones_txt_infoDispositivo ) );
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(FIN_LINEA);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(FIN_LINEA);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(FIN_LINEA);
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append(FIN_LINEA);
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append(FIN_LINEA);
        errorReport.append( ventanaOrigen.getResources().getString( R.string.ManejadorExcepciones_txt_firmware ) );
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK);
        errorReport.append(FIN_LINEA);
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append(FIN_LINEA);
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append(FIN_LINEA);

        // Invocar a la ventana de errores, pasándole la información a mostrar
        Intent intent = new Intent(ventanaOrigen, VentanaErrores.class);
        intent.putExtra("debugInfo", errorReport.toString());
        ventanaOrigen.startActivity(intent);

        // Despues de mostrar la ventana de error, finalizar la aplicación
        android.os.Process.killProcess( android.os.Process.myPid() );
        System.exit(10);
    }

}
